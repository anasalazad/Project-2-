package com.thefelineco.domain

import com.thefelineco.domain.model.Cat
import org.junit.Assert.assertEquals
import org.junit.Test

class CatAgeLabelTest {
    @Test
    fun `ages read naturally`() {
        assertEquals("1 month", Cat.formatAge(1))
        assertEquals("3 months", Cat.formatAge(3))
        assertEquals("1 year", Cat.formatAge(12))
        assertEquals("1 year 2 months", Cat.formatAge(14))
        assertEquals("2 years 6 months", Cat.formatAge(30))
        assertEquals("7 years", Cat.formatAge(84))
        assertEquals("15 years", Cat.formatAge(185))
    }
}
