package com.thefelineco.ui

import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.testCat
import com.thefelineco.testing.FakeBookingRepository
import com.thefelineco.testing.FakeUserRepository
import com.thefelineco.testing.MainDispatcherRule
import com.thefelineco.testing.testUser
import com.thefelineco.ui.booking.BookingEvent
import com.thefelineco.ui.booking.BookingField
import com.thefelineco.ui.booking.BookingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class BookingViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 9, 25) // Friday
    private val saturday = today.plusDays(1)
    private val kitten = testCat(id = 1, name = "Mochi", ageMonths = 3, goodWithCats = false)
    private val bookings = FakeBookingRepository()
    private val users = FakeUserRepository()

    private fun viewModel() = BookingViewModel(kitten, users, bookings, today)

    private fun BookingViewModel.fillValidForm() {
        onEvent(BookingEvent.DateSelected(saturday))
        onEvent(BookingEvent.SlotSelected("11:00"))
        onEvent(BookingEvent.PhoneChanged("0412 345 678"))
        onEvent(BookingEvent.HomeTypeSelected("Apartment"))
        onEvent(BookingEvent.TermsChanged(true))
    }

    @Test
    fun `contact details are prefilled from the account`() {
        val state = viewModel().uiState.value
        assertEquals(testUser.fullName, state.fullName)
        assertEquals(testUser.email, state.email)
        assertEquals(testUser.credits, state.credits)
    }

    @Test
    fun `submitting an incomplete form flags every missing field`() {
        val vm = viewModel()
        vm.onEvent(BookingEvent.Submit)
        val errors = vm.uiState.value.errors
        assertEquals(
            setOf(BookingField.DATE, BookingField.SLOT, BookingField.PHONE, BookingField.HOME, BookingField.TERMS),
            errors.keys,
        )
        assertTrue(bookings.requested.isEmpty())
    }

    @Test
    fun `editing a field clears its error`() {
        val vm = viewModel()
        vm.onEvent(BookingEvent.Submit)
        vm.onEvent(BookingEvent.PhoneChanged("0412 345 678"))
        assertNull(vm.uiState.value.errors[BookingField.PHONE])
    }

    @Test
    fun `a valid booking is saved with the right fee and returned`() {
        val vm = viewModel()
        vm.fillValidForm()
        vm.onEvent(BookingEvent.Submit)

        val confirmed = vm.uiState.value.confirmed
        assertNotNull(confirmed)
        assertEquals(CreditRules.KITTEN_FEE, confirmed!!.feeCredits)
        assertEquals(saturday.toEpochDay(), confirmed.dateEpochDay)
        assertEquals(1, bookings.requested.size)
    }

    @Test
    fun `mondays and taken slots are rejected`() {
        bookings.takenSlots = mapOf(saturday.toEpochDay() to setOf("11:00"))
        val vm = viewModel()
        vm.fillValidForm()
        assertEquals(setOf("11:00"), vm.uiState.value.takenSlots)
        vm.onEvent(BookingEvent.Submit)
        assertEquals("That time has just been booked", vm.uiState.value.errors[BookingField.SLOT])

        vm.onEvent(BookingEvent.DateSelected(LocalDate.of(2026, 9, 28))) // Monday
        vm.onEvent(BookingEvent.Submit)
        assertEquals("We're closed on Mondays", vm.uiState.value.errors[BookingField.DATE])
    }

    @Test
    fun `cannot book without enough credits`() {
        users.user.value = testUser.copy(credits = 20)
        val vm = viewModel()
        vm.fillValidForm()
        vm.onEvent(BookingEvent.Submit)
        assertNull(vm.uiState.value.confirmed)
        assertTrue(vm.uiState.value.formError!!.contains("${CreditRules.KITTEN_FEE}"))
    }

    @Test
    fun `repository errors are shown to the user`() {
        bookings.failWith = "Sorry, Mochi has just been reserved by someone else"
        val vm = viewModel()
        vm.fillValidForm()
        vm.onEvent(BookingEvent.Submit)
        assertEquals(bookings.failWith, vm.uiState.value.formError)
        assertNull(vm.uiState.value.confirmed)
    }

    @Test
    fun `household warnings update as the answers change`() {
        val vm = viewModel()
        assertTrue(vm.uiState.value.compatibilityWarnings.isEmpty())
        vm.onEvent(BookingEvent.OtherPetsChanged(true))
        assertEquals(1, vm.uiState.value.compatibilityWarnings.size)
    }
}
