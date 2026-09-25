package com.thefelineco.domain

import com.thefelineco.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {

    @Test
    fun `email must look like an address`() {
        assertNull(Validators.email("jordan@thefelineco.com"))
        assertNull(Validators.email("  a.b+c@mail.co.uk  "))
        assertEquals("Email is required", Validators.email(" "))
        assertNotNull(Validators.email("jordan@"))
        assertNotNull(Validators.email("jordan.thefelineco.com"))
    }

    @Test
    fun `password needs length, a letter and a number`() {
        assertNull(Validators.password("Kitten123"))
        assertNotNull(Validators.password("short1"))
        assertNotNull(Validators.password("onlyletters"))
        assertNotNull(Validators.password("12345678"))
    }

    @Test
    fun `confirm password must match`() {
        assertNull(Validators.confirmPassword("Kitten123", "Kitten123"))
        assertEquals("Passwords don't match", Validators.confirmPassword("Kitten123", "Kitten124"))
    }

    @Test
    fun `names reject digits and single letters`() {
        assertNull(Validators.name("Jordan Lee"))
        assertNotNull(Validators.name("J"))
        assertNotNull(Validators.name("R2D2"))
    }

    @Test
    fun `phone numbers accept common formats`() {
        assertNull(Validators.phone("0412 345 678"))
        assertNull(Validators.phone("+61412345678"))
        assertNotNull(Validators.phone("12345"))
        assertNotNull(Validators.phone("04-1234-5678"))
    }

    @Test
    fun `postcode must be four digits`() {
        assertNull(Validators.postcode("3000"))
        assertNotNull(Validators.postcode("300"))
        assertNotNull(Validators.postcode("30a0"))
    }

    @Test
    fun `appointment date must be in the booking window and not a Monday`() {
        val today = LocalDate.of(2026, 9, 25) // a Friday
        assertEquals("Choose a date", Validators.appointmentDate(null, today))
        assertNotNull(Validators.appointmentDate(today, today))
        assertNull(Validators.appointmentDate(today.plusDays(1), today)) // Saturday
        assertEquals("We're closed on Mondays", Validators.appointmentDate(LocalDate.of(2026, 9, 28), today))
        assertNotNull(Validators.appointmentDate(today.plusDays(31), today))
    }

    @Test
    fun `number ranges are enforced`() {
        assertNull(Validators.intInRange("12", "Age", 0..300))
        assertNotNull(Validators.intInRange("abc", "Age", 0..300))
        assertNotNull(Validators.intInRange("301", "Age", 0..300))
        assertNull(Validators.decimalInRange("4.5", "Weight", 0.2..15.0))
        assertNotNull(Validators.decimalInRange("40", "Weight", 0.2..15.0))
    }
}
