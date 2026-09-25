package com.thefelineco.domain

import com.thefelineco.domain.model.AgeGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditRulesTest {

    @Test
    fun `kittens under six months cost the kitten fee`() {
        assertEquals(CreditRules.KITTEN_FEE, CreditRules.adoptionFee(0))
        assertEquals(CreditRules.KITTEN_FEE, CreditRules.adoptionFee(5))
    }

    @Test
    fun `cats from six months to just under two years cost the young fee`() {
        assertEquals(CreditRules.YOUNG_FEE, CreditRules.adoptionFee(6))
        assertEquals(CreditRules.YOUNG_FEE, CreditRules.adoptionFee(23))
    }

    @Test
    fun `cats aged two and over are free`() {
        assertEquals(0, CreditRules.adoptionFee(24))
        assertEquals(0, CreditRules.adoptionFee(180))
    }

    @Test
    fun `age groups have no gaps at the boundaries`() {
        assertEquals(AgeGroup.KITTEN, AgeGroup.of(5))
        assertEquals(AgeGroup.YOUNG, AgeGroup.of(6))
        assertEquals(AgeGroup.ADULT, AgeGroup.of(24))
        assertEquals(AgeGroup.SENIOR, AgeGroup.of(96))
        assertEquals(AgeGroup.KITTEN, AgeGroup.of(-3)) // defensive: bad data never crashes
    }

    @Test
    fun `affordability is inclusive of the exact balance`() {
        assertTrue(CreditRules.canAfford(150, 150))
        assertFalse(CreditRules.canAfford(149, 150))
        assertTrue(CreditRules.canAfford(0, 0))
    }
}
