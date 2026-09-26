package com.thefelineco.ui.booking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.thefelineco.FelineApplication
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.Cat
import com.thefelineco.ui.common.setFelineContent

/**
 * Second activity: the meet & greet booking form.
 *
 * Receives a [Cat] as a Parcelable extra ([BookMeetAndGreet.EXTRA_CAT]) and, once the booking is
 * saved, returns the [Booking] to MainActivity with `setResult(RESULT_OK)`.
 */
class BookingActivity : ComponentActivity() {

    private val cat: Cat? by lazy {
        IntentCompat.getParcelableExtra(intent, BookMeetAndGreet.EXTRA_CAT, Cat::class.java)
    }

    private val viewModel: BookingViewModel by viewModels {
        val container = (application as FelineApplication).container
        viewModelFactory {
            initializer { BookingViewModel(checkNotNull(cat), container.userRepository, container.bookingRepository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Defensive: this screen can't work without a cat to book.
        if (cat == null) {
            finish()
            return
        }
        setFelineContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.confirmed) {
                state.confirmed?.let(::finishWithResult)
            }
            BookingScreen(state = state, onEvent = viewModel::onEvent, onClose = ::finish)
        }
    }

    private fun finishWithResult(booking: Booking) {
        setResult(RESULT_OK, Intent().putExtra(BookMeetAndGreet.EXTRA_BOOKING, booking))
        finish()
    }
}
