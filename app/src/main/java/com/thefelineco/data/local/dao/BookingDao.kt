package com.thefelineco.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thefelineco.data.local.entity.BookingEntity
import com.thefelineco.domain.model.BookingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Insert
    suspend fun insert(booking: BookingEntity): Long

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getById(id: Long): BookingEntity?

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY dateEpochDay DESC, timeSlot DESC")
    fun observeForUser(userId: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY dateEpochDay ASC, timeSlot ASC")
    fun observeAll(): Flow<List<BookingEntity>>

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: BookingStatus)

    /** Time slots already taken on a day by bookings that are still active. */
    @Query(
        "SELECT timeSlot FROM bookings WHERE dateEpochDay = :epochDay " +
            "AND status IN ('REQUESTED', 'CONFIRMED')"
    )
    suspend fun takenSlots(epochDay: Long): List<String>

    @Query("SELECT COUNT(*) FROM bookings WHERE catId = :catId AND status IN ('REQUESTED', 'CONFIRMED')")
    suspend fun activeCountForCat(catId: Long): Int
}
