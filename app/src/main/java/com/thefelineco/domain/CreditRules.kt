package com.thefelineco.domain

import com.thefelineco.domain.model.AgeGroup

/**
 * The credit economy in one place (see CONTEXT.md §4), so it is easy to tune and test.
 */
object CreditRules {
    /** Credits every new customer starts with. */
    const val WELCOME_BONUS = 200

    /** Credits added when an adoption is completed. */
    const val ADOPTION_REWARD = 250

    const val KITTEN_FEE = 150
    const val YOUNG_FEE = 100

    /** Adoption fee for a cat of [ageMonths]. Cats aged two and over are free. */
    fun adoptionFee(ageMonths: Int): Int = when (AgeGroup.of(ageMonths)) {
        AgeGroup.KITTEN -> KITTEN_FEE
        AgeGroup.YOUNG -> YOUNG_FEE
        AgeGroup.ADULT, AgeGroup.SENIOR -> 0
    }

    /** Whether a balance of [credits] covers [cost]. */
    fun canAfford(credits: Int, cost: Int): Boolean = credits >= cost
}
