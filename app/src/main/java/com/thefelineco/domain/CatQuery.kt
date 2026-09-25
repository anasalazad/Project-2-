package com.thefelineco.domain

import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex

/** Sort orders offered on the Adopt screen. */
enum class CatSort(val label: String) {
    NEWEST("Newest arrivals"),
    NAME("Name (A–Z)"),
    YOUNGEST("Youngest first"),
    OLDEST("Oldest first"),
    FEE_LOW("Lowest fee first"),
}

/**
 * Everything the user has chosen on the Adopt screen. Empty sets mean "any".
 *
 * Kept as plain data with a pure [apply] function so search, filtering and sorting can be unit-tested
 * without Android.
 */
data class CatQuery(
    val text: String = "",
    val ageGroups: Set<AgeGroup> = emptySet(),
    val sexes: Set<Sex> = emptySet(),
    val coats: Set<CoatLength> = emptySet(),
    val goodWithKids: Boolean = false,
    val goodWithCats: Boolean = false,
    val goodWithDogs: Boolean = false,
    val freeOnly: Boolean = false,
    val statuses: Set<AdoptionStatus> = setOf(AdoptionStatus.AVAILABLE, AdoptionStatus.PENDING),
    val sort: CatSort = CatSort.NEWEST,
) {
    /** Number of active filters (not counting text or sort), shown on the "Filters" button. */
    val activeFilterCount: Int
        get() = ageGroups.size + sexes.size + coats.size +
            listOf(goodWithKids, goodWithCats, goodWithDogs, freeOnly).count { it }

    /** Returns the cats that match this query, in the chosen order. */
    fun apply(cats: List<Cat>): List<Cat> = cats
        .asSequence()
        .filter { it.status in statuses }
        .filter { it.matchesText(text) }
        .filter { ageGroups.isEmpty() || it.ageGroup in ageGroups }
        .filter { sexes.isEmpty() || it.sex in sexes }
        .filter { coats.isEmpty() || it.coat in coats }
        .filter { !goodWithKids || it.goodWithKids }
        .filter { !goodWithCats || it.goodWithCats }
        .filter { !goodWithDogs || it.goodWithDogs }
        .filter { !freeOnly || it.isFree }
        .sortedWith(sort.comparator)
        .toList()
}

/** Case-insensitive match on name, breed, colour and personality traits. */
fun Cat.matchesText(text: String): Boolean {
    val needle = text.trim()
    if (needle.isEmpty()) return true
    return listOf(name, breed, colour).any { it.contains(needle, ignoreCase = true) } ||
        personality.any { it.contains(needle, ignoreCase = true) }
}

private val CatSort.comparator: Comparator<Cat>
    get() = when (this) {
        CatSort.NEWEST -> compareByDescending<Cat> { it.listedAt }.thenBy { it.name }
        CatSort.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        CatSort.YOUNGEST -> compareBy<Cat> { it.ageMonths }.thenBy { it.name }
        CatSort.OLDEST -> compareByDescending<Cat> { it.ageMonths }.thenBy { it.name }
        CatSort.FEE_LOW -> compareBy<Cat> { it.adoptionFee }.thenBy { it.ageMonths }
    }
