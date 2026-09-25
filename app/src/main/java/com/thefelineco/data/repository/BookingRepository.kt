package com.thefelineco.data.repository

import androidx.room.withTransaction
import com.thefelineco.data.local.FelineDatabase
import com.thefelineco.data.local.entity.toDomain
import com.thefelineco.data.local.entity.toEntity
import com.thefelineco.data.local.recordCredits
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Meet & greet bookings and the adoption lifecycle (CONTEXT.md §4).
 * Every state change that touches credits or cat status runs in one database transaction.
 */
interface BookingRepository {
    fun observeBookingsForUser(userId: Long): Flow<List<Booking>>
    fun observeAllBookings(): Flow<List<Booking>>
    suspend fun takenSlots(epochDay: Long): Set<String>

    /** Creates the booking, charges the fee and marks the cat Pending. */
    suspend fun requestBooking(booking: Booking): Result<Booking>

    /** Customer cancels: the fee is refunded and the cat becomes available again. */
    suspend fun cancel(bookingId: Long): Result<Unit>

    /** Admin declines: the fee is refunded and the cat becomes available again. */
    suspend fun decline(bookingId: Long): Result<Unit>

    /** Admin confirms the appointment time. */
    suspend fun confirm(bookingId: Long): Result<Unit>

    /** Admin completes the adoption: cat Adopted, customer rewarded. */
    suspend fun completeAdoption(bookingId: Long): Result<Unit>
}

class OfflineBookingRepository(private val db: FelineDatabase) : BookingRepository {
    private val bookingDao = db.bookingDao()
    private val catDao = db.catDao()
    private val userDao = db.userDao()

    override fun observeBookingsForUser(userId: Long): Flow<List<Booking>> =
        bookingDao.observeForUser(userId).map { list -> list.map { it.toDomain() } }

    override fun observeAllBookings(): Flow<List<Booking>> =
        bookingDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun takenSlots(epochDay: Long): Set<String> = bookingDao.takenSlots(epochDay).toSet()

    override suspend fun requestBooking(booking: Booking): Result<Booking> = runCatching {
        db.withTransaction {
            val cat = catDao.getById(booking.catId) ?: throw FelineException("This cat is no longer listed")
            if (cat.status != AdoptionStatus.AVAILABLE) {
                throw FelineException("Sorry, ${cat.name} has just been reserved by someone else")
            }
            if (booking.timeSlot in bookingDao.takenSlots(booking.dateEpochDay)) {
                throw FelineException("That time slot was just taken. Please pick another")
            }
            val user = userDao.getById(booking.userId) ?: throw FelineException("Please sign in again")
            val fee = CreditRules.adoptionFee(cat.ageMonths)
            if (!CreditRules.canAfford(user.credits, fee)) {
                throw FelineException("You need $fee credits to adopt ${cat.name}. You have ${user.credits}")
            }

            val saved = booking.copy(feeCredits = fee, status = BookingStatus.REQUESTED)
            val id = bookingDao.insert(saved.toEntity())
            db.recordCredits(user.id, -fee, TransactionType.ADOPTION_FEE, "Adoption fee for ${cat.name} (held)")
            catDao.updateStatus(cat.id, AdoptionStatus.PENDING)
            saved.copy(id = id)
        }
    }

    override suspend fun cancel(bookingId: Long) = release(bookingId, BookingStatus.CANCELLED)

    override suspend fun decline(bookingId: Long) = release(bookingId, BookingStatus.DECLINED)

    override suspend fun confirm(bookingId: Long): Result<Unit> = runCatching {
        val booking = requireActive(bookingId)
        bookingDao.updateStatus(booking.id, BookingStatus.CONFIRMED)
    }

    override suspend fun completeAdoption(bookingId: Long): Result<Unit> = runCatching {
        db.withTransaction {
            val booking = requireActive(bookingId)
            bookingDao.updateStatus(booking.id, BookingStatus.COMPLETED)
            catDao.updateStatus(booking.catId, AdoptionStatus.ADOPTED)
            db.recordCredits(
                booking.userId,
                CreditRules.ADOPTION_REWARD,
                TransactionType.ADOPTION_REWARD,
                "Thank you for adopting ${booking.catName}!",
            )
        }
    }

    /** Ends an active booking without an adoption: refund the fee and free up the cat. */
    private suspend fun release(bookingId: Long, newStatus: BookingStatus): Result<Unit> = runCatching {
        db.withTransaction {
            val booking = requireActive(bookingId)
            bookingDao.updateStatus(booking.id, newStatus)
            db.recordCredits(
                booking.userId,
                booking.feeCredits,
                TransactionType.ADOPTION_REFUND,
                "Refund for ${booking.catName} (${newStatus.label.lowercase()})",
            )
            if (catDao.getById(booking.catId)?.status == AdoptionStatus.PENDING) {
                catDao.updateStatus(booking.catId, AdoptionStatus.AVAILABLE)
            }
        }
    }

    private suspend fun requireActive(bookingId: Long): Booking {
        val booking = bookingDao.getById(bookingId)?.toDomain() ?: throw FelineException("Booking not found")
        if (!booking.status.isActive) throw FelineException("This booking is already ${booking.status.label.lowercase()}")
        return booking
    }
}
