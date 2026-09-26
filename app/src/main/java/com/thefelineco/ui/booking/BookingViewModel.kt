package com.thefelineco.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.BookingRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

/** Fields that can show a validation error. */
enum class BookingField { DATE, SLOT, NAME, EMAIL, PHONE, HOME, TERMS }

data class BookingUiState(
    val cat: Cat,
    val userId: Long? = null,
    val credits: Int = 0,
    val dates: List<LocalDate> = emptyList(),
    val selectedDate: LocalDate? = null,
    val takenSlots: Set<String> = emptySet(),
    val selectedSlot: String? = null,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val homeType: String? = null,
    val hasOtherPets: Boolean = false,
    val hasChildren: Boolean = false,
    val notes: String = "",
    val acceptedTerms: Boolean = false,
    val errors: Map<BookingField, String> = emptyMap(),
    val formError: String? = null,
    val isSubmitting: Boolean = false,
    /** Set once the booking is saved. BookingActivity then returns it as its result. */
    val confirmed: Booking? = null,
) {
    val fee: Int get() = cat.adoptionFee
    val balanceAfter: Int get() = credits - fee
    val canAfford: Boolean get() = CreditRules.canAfford(credits, fee)

    /** Friendly warnings when the household may not suit this cat. */
    val compatibilityWarnings: List<String>
        get() = buildList {
            if (hasOtherPets && !cat.goodWithCats) add("${cat.name} would prefer to be your only pet.")
            if (hasChildren && !cat.goodWithKids) add("${cat.name} is best suited to an adult-only home.")
        }
}

sealed interface BookingEvent {
    data class DateSelected(val date: LocalDate) : BookingEvent
    data class SlotSelected(val slot: String) : BookingEvent
    data class FullNameChanged(val value: String) : BookingEvent
    data class EmailChanged(val value: String) : BookingEvent
    data class PhoneChanged(val value: String) : BookingEvent
    data class HomeTypeSelected(val value: String) : BookingEvent
    data class OtherPetsChanged(val value: Boolean) : BookingEvent
    data class ChildrenChanged(val value: Boolean) : BookingEvent
    data class NotesChanged(val value: String) : BookingEvent
    data class TermsChanged(val value: Boolean) : BookingEvent
    data object Submit : BookingEvent
}

/**
 * The meet & greet form in BookingActivity. It validates every field, checks the slot is still
 * free and the user can afford the fee, then saves through [BookingRepository].
 */
class BookingViewModel(
    cat: Cat,
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BookingUiState(cat = cat, dates = (1..DAYS_AHEAD).map { today.plusDays(it.toLong()) })
    )
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        // Keep the balance live, and pre-fill contact details from the account the first time.
        viewModelScope.launch {
            var prefilled = false
            userRepository.currentUser.collect { user ->
                _uiState.update { s ->
                    val base = s.copy(userId = user?.id, credits = user?.credits ?: 0)
                    if (!prefilled && user != null) {
                        prefilled = true
                        base.copy(fullName = s.fullName.ifEmpty { user.fullName }, email = s.email.ifEmpty { user.email })
                    } else {
                        base
                    }
                }
            }
        }
    }

    fun onEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.DateSelected -> selectDate(event.date)
            is BookingEvent.SlotSelected -> edit(BookingField.SLOT) { it.copy(selectedSlot = event.slot) }
            is BookingEvent.FullNameChanged -> edit(BookingField.NAME) { it.copy(fullName = event.value) }
            is BookingEvent.EmailChanged -> edit(BookingField.EMAIL) { it.copy(email = event.value) }
            is BookingEvent.PhoneChanged -> edit(BookingField.PHONE) { it.copy(phone = event.value) }
            is BookingEvent.HomeTypeSelected -> edit(BookingField.HOME) { it.copy(homeType = event.value) }
            is BookingEvent.OtherPetsChanged -> _uiState.update { it.copy(hasOtherPets = event.value) }
            is BookingEvent.ChildrenChanged -> _uiState.update { it.copy(hasChildren = event.value) }
            is BookingEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value.take(MAX_NOTES)) }
            is BookingEvent.TermsChanged -> edit(BookingField.TERMS) { it.copy(acceptedTerms = event.value) }
            BookingEvent.Submit -> submit()
        }
    }

    /** Applies a change and clears that field's error (and the form-level message). */
    private fun edit(field: BookingField, change: (BookingUiState) -> BookingUiState) {
        _uiState.update { change(it).copy(errors = it.errors - field, formError = null) }
    }

    private fun selectDate(date: LocalDate) {
        edit(BookingField.DATE) { it.copy(selectedDate = date, selectedSlot = null, takenSlots = emptySet()) }
        viewModelScope.launch { refreshTakenSlots(date) }
    }

    private suspend fun refreshTakenSlots(date: LocalDate) {
        val taken = bookingRepository.takenSlots(date.toEpochDay())
        _uiState.update { s -> if (s.selectedDate == date) s.copy(takenSlots = taken) else s }
    }

    private fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val errors = validate(s)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors, formError = "Please check the highlighted details") }
            return
        }
        if (!s.canAfford) {
            _uiState.update { it.copy(formError = "You need ${s.fee} credits but have ${s.credits}") }
            return
        }
        val userId = s.userId ?: run {
            _uiState.update { it.copy(formError = "Please sign in again") }
            return
        }
        val date = checkNotNull(s.selectedDate)
        val booking = Booking(
            userId = userId,
            catId = s.cat.id,
            catName = s.cat.name,
            catImageName = s.cat.imageName,
            dateEpochDay = date.toEpochDay(),
            timeSlot = checkNotNull(s.selectedSlot),
            fullName = s.fullName,
            email = s.email,
            phone = s.phone,
            homeType = checkNotNull(s.homeType),
            hasOtherPets = s.hasOtherPets,
            hasChildren = s.hasChildren,
            notes = s.notes,
            feeCredits = s.fee,
        )
        _uiState.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            bookingRepository.requestBooking(booking)
                .onSuccess { saved -> _uiState.update { it.copy(isSubmitting = false, confirmed = saved) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, formError = error.message ?: "Couldn't save your booking") }
                    // The slot may have been taken meanwhile; show the latest availability.
                    refreshTakenSlots(date)
                }
        }
    }

    /** Returns every problem with the form, keyed by field. Empty means valid. */
    internal fun validate(s: BookingUiState): Map<BookingField, String> = buildMap {
        Validators.appointmentDate(s.selectedDate, today, DAYS_AHEAD.toLong())?.let { put(BookingField.DATE, it) }
        when {
            s.selectedSlot == null -> put(BookingField.SLOT, "Choose a time")
            s.selectedSlot in s.takenSlots -> put(BookingField.SLOT, "That time has just been booked")
        }
        Validators.name(s.fullName)?.let { put(BookingField.NAME, it) }
        Validators.email(s.email)?.let { put(BookingField.EMAIL, it) }
        Validators.phone(s.phone)?.let { put(BookingField.PHONE, it) }
        if (s.homeType == null) put(BookingField.HOME, "Tell us about your home")
        if (!s.acceptedTerms) put(BookingField.TERMS, "Please confirm to continue")
    }

    companion object {
        const val DAYS_AHEAD = 30
        const val MAX_NOTES = 300
        val TIME_SLOTS = listOf("10:00", "11:00", "12:00", "13:30", "14:30", "15:30")
        val HOME_TYPES = listOf("House with garden", "House", "Townhouse", "Apartment")

        /** The shelter is closed on Mondays. */
        fun isOpen(date: LocalDate): Boolean = date.dayOfWeek != DayOfWeek.MONDAY
    }
}
