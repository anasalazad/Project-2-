package com.thefelineco.domain

import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex

/** Builds a cat for tests with sensible defaults, so each test only states what it cares about. */
fun testCat(
    id: Long = 1,
    name: String = "Tester",
    breed: String = "Domestic Shorthair",
    ageMonths: Int = 36,
    sex: Sex = Sex.FEMALE,
    coat: CoatLength = CoatLength.SHORT,
    colour: String = "Tabby",
    personality: List<String> = listOf("Friendly"),
    goodWithKids: Boolean = true,
    goodWithCats: Boolean = true,
    goodWithDogs: Boolean = true,
    status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    listedAt: Long = 0,
) = Cat(
    id = id, name = name, breed = breed, ageMonths = ageMonths, sex = sex, coat = coat, colour = colour,
    weightKg = 4.0, personality = personality, description = "", imageName = "", goodWithKids = goodWithKids,
    goodWithCats = goodWithCats, goodWithDogs = goodWithDogs, indoorOnly = true, status = status, listedAt = listedAt,
)
