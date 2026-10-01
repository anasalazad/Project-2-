package com.thefelineco.domain

import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Test

class CatQueryTest {

    private val cats = listOf(
        testCat(id = 1, name = "Mochi", breed = "Ragdoll", ageMonths = 3, coat = CoatLength.LONG, listedAt = 300),
        testCat(id = 2, name = "Archie", breed = "Maine Coon", ageMonths = 156, sex = Sex.MALE, goodWithDogs = false, listedAt = 100),
        testCat(id = 3, name = "Luna", breed = "Russian Blue", ageMonths = 10, goodWithKids = false, personality = listOf("Shy"), listedAt = 200),
        testCat(id = 4, name = "Felix", ageMonths = 96, sex = Sex.MALE, status = AdoptionStatus.ADOPTED, listedAt = 400),
        testCat(id = 5, name = "Bella", ageMonths = 40, status = AdoptionStatus.PENDING, listedAt = 50),
    )

    private fun CatQuery.names(): List<String> = apply(cats).map(Cat::name)

    @Test
    fun `default query hides adopted cats and shows newest first`() {
        assertEquals(listOf("Mochi", "Luna", "Archie", "Bella"), CatQuery().names())
    }

    @Test
    fun `text search matches name, breed and personality, ignoring case`() {
        assertEquals(listOf("Archie"), CatQuery(text = "maine").names())
        assertEquals(listOf("Luna"), CatQuery(text = "  SHY ").names())
        assertEquals(emptyList<String>(), CatQuery(text = "dragon").names())
    }

    @Test
    fun `filters combine with AND`() {
        val query = CatQuery(sexes = setOf(Sex.MALE), goodWithDogs = true)
        assertEquals(emptyList<String>(), query.names())
        assertEquals(listOf("Mochi"), CatQuery(coats = setOf(CoatLength.LONG), goodWithKids = true).names())
    }

    @Test
    fun `age group filter accepts several groups`() {
        val query = CatQuery(ageGroups = setOf(AgeGroup.KITTEN, AgeGroup.YOUNG), sort = CatSort.YOUNGEST)
        assertEquals(listOf("Mochi", "Luna"), query.names())
    }

    @Test
    fun `free only keeps cats aged two and over`() {
        assertEquals(listOf("Archie", "Bella"), CatQuery(freeOnly = true).names())
    }

    @Test
    fun `sorting options order results correctly`() {
        assertEquals(listOf("Archie", "Bella", "Luna", "Mochi"), CatQuery(sort = CatSort.NAME).names())
        assertEquals(listOf("Archie", "Bella", "Luna", "Mochi"), CatQuery(sort = CatSort.OLDEST).names())
        assertEquals(listOf("Bella", "Archie", "Luna", "Mochi"), CatQuery(sort = CatSort.FEE_LOW).names())
    }

    @Test
    fun `status filter can include adopted cats`() {
        val all = CatQuery(statuses = AdoptionStatus.entries.toSet(), sort = CatSort.NAME)
        assertEquals(5, all.apply(cats).size)
    }

    @Test
    fun `favourites only keeps hearted cats`() {
        assertEquals(listOf("Archie"), CatQuery(favouritesOnly = true).apply(cats, favourites = setOf(2L, 4L)).map(Cat::name))
        assertEquals(emptyList<String>(), CatQuery(favouritesOnly = true).names())
    }

    @Test
    fun `active filter count ignores text and sort`() {
        val query = CatQuery(text = "x", sort = CatSort.NAME, sexes = setOf(Sex.MALE), freeOnly = true)
        assertEquals(2, query.activeFilterCount)
    }
}
