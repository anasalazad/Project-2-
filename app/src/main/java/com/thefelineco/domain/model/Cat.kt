package com.thefelineco.domain.model

import android.os.Parcelable
import com.thefelineco.domain.CreditRules
import kotlinx.parcelize.Parcelize

/**
 * A cat listed for adoption.
 *
 * It is [Parcelable] so it can be sent from the Cat Detail screen to BookingActivity inside an Intent.
 */
@Parcelize
data class Cat(
    val id: Long = 0,
    val name: String,
    val breed: String,
    val ageMonths: Int,
    val sex: Sex,
    val coat: CoatLength,
    val colour: String,
    val weightKg: Double,
    val personality: List<String>,
    val description: String,
    val imageName: String,
    val goodWithKids: Boolean,
    val goodWithCats: Boolean,
    val goodWithDogs: Boolean,
    val indoorOnly: Boolean,
    val vaccinated: Boolean = true,
    val desexed: Boolean = true,
    val microchipped: Boolean = true,
    val specialNeeds: String? = null,
    val status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    val listedAt: Long = System.currentTimeMillis(),
) : Parcelable {

    val ageGroup: AgeGroup get() = AgeGroup.of(ageMonths)

    /** Adoption fee in credits. Zero means free. */
    val adoptionFee: Int get() = CreditRules.adoptionFee(ageMonths)

    val isFree: Boolean get() = adoptionFee == 0

    /** Human-friendly age, e.g. "3 months", "1 year 2 months", "7 years". */
    val ageLabel: String get() = formatAge(ageMonths)

    companion object {
        /** Formats an age in months the way people say it out loud. */
        fun formatAge(months: Int): String {
            val years = months / 12
            val rem = months % 12
            fun plural(n: Int, unit: String) = "$n $unit${if (n == 1) "" else "s"}"
            return when {
                years == 0 -> plural(rem, "month")
                rem == 0 || years >= 3 -> plural(years, "year")
                else -> "${plural(years, "year")} ${plural(rem, "month")}"
            }
        }
    }
}
