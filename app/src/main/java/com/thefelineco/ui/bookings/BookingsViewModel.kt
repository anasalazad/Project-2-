package com.thefelineco.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.BookingRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.Booking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookingsUiState(
    val isLoading: Boolean = true,
    val upcoming: List<Booking> = emptyList(),
    val past: List<Booking> = emptyList(),
)

/** The signed-in customer's meet & greet appointments. */
class BookingsViewModel(
    userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BookingsUiState> = userRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(BookingsUiState(isLoading = false))
            else bookingRepository.observeBookingsForUser(user.id).map { bookings ->
                val (active, finished) = bookings.partition { it.status.isActive }
                BookingsUiState(
                    isLoading = false,
                    upcoming = active.sortedWith(compareBy({ it.dateEpochDay }, { it.timeSlot })),
                    past = finished,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookingsUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)

    /** One-off messages for the snackbar. */
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun cancel(booking: Booking) {
        viewModelScope.launch {
            bookingRepository.cancel(booking.id)
                .onSuccess {
                    _messages.send(
                        if (booking.feeCredits > 0) "Booking cancelled. ${booking.feeCredits} credits refunded."
                        else "Booking cancelled."
                    )
                }
                .onFailure { _messages.send(it.message ?: "Couldn't cancel the booking") }
        }
    }
}
