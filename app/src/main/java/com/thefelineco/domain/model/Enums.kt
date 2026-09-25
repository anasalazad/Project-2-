package com.thefelineco.domain.model

/** Biological sex of a cat, shown on cards and used as a filter. */
enum class Sex(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
}

/** Coat length, used as a filter. */
enum class CoatLength(val label: String) {
    SHORT("Short"),
    MEDIUM("Medium"),
    LONG("Long"),
    HAIRLESS("Hairless"),
}

/**
 * Life stage, derived from age. Drives both the adoption fee and the "Age" filter chips.
 *
 * @property monthRange ages (in months) that belong to this group.
 */
enum class AgeGroup(val label: String, val monthRange: IntRange) {
    KITTEN("Kitten", 0..5),
    YOUNG("Young", 6..23),
    ADULT("Adult", 24..95),
    SENIOR("Senior", 96..Int.MAX_VALUE);

    companion object {
        /** Returns the group that [ageMonths] falls into. */
        fun of(ageMonths: Int): AgeGroup =
            entries.first { ageMonths.coerceAtLeast(0) in it.monthRange }
    }
}

/** Where a cat is in the adoption pipeline. */
enum class AdoptionStatus(val label: String) {
    AVAILABLE("Available"),
    PENDING("Adoption pending"),
    ADOPTED("Adopted"),
}

/** Lifecycle of a meet & greet booking. See CONTEXT.md §4. */
enum class BookingStatus(val label: String) {
    REQUESTED("Requested"),
    CONFIRMED("Confirmed"),
    COMPLETED("Adopted"),
    CANCELLED("Cancelled"),
    DECLINED("Declined");

    /** True while the booking still holds the cat and the customer's credits. */
    val isActive: Boolean get() = this == REQUESTED || this == CONFIRMED
}

/** Shop categories, in the order shown as tabs. */
enum class ProductCategory(val label: String) {
    FOOD("Food"),
    TREATS("Treats"),
    TOYS("Toys"),
    BEDS("Beds"),
    GROOMING("Grooming"),
    ACCESSORIES("Accessories"),
}

/** Account type. Admins get the management screens instead of the shopping ones. */
enum class UserRole { CUSTOMER, ADMIN }

/** Why a customer's credit balance changed. */
enum class TransactionType(val label: String) {
    WELCOME_BONUS("Welcome bonus"),
    ADOPTION_FEE("Adoption fee"),
    ADOPTION_REFUND("Adoption fee refund"),
    ADOPTION_REWARD("Adoption reward"),
    SHOP_PURCHASE("Shop purchase"),
}

/** How a shop order reaches the customer. [extraCredits] is added to the order total. */
enum class DeliveryMethod(val label: String, val extraCredits: Int) {
    STANDARD("Standard delivery (3–5 days)", 0),
    EXPRESS("Express delivery (next day)", 10),
    CLICK_AND_COLLECT("Click & collect", 0),
}
