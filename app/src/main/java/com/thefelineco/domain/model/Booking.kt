package com.thefelineco.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * A meet & greet appointment for adopting a cat.
 *
 * BookingActivity sends it back to MainActivity through the Activity Result API.
 */
@Parcelize
data class Booking(
    val id: Long = 0,
    val userId: Long,
    val catId: Long,
    val catName: String,
    val catImageName: String,
    val dateEpochDay: Long,
    val timeSlot: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val homeType: String,
    val hasOtherPets: Boolean,
    val hasChildren: Boolean,
    val notes: String,
    val feeCredits: Int,
    val status: BookingStatus = BookingStatus.REQUESTED,
    val createdAt: Long = System.currentTimeMillis(),
) : Parcelable {

    val date: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)
}
