package com.thefelineco.ui.booking

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.Cat

/**
 * Activity Result contract for the meet & greet flow. Input: the [Cat] to meet, sent as a
 * Parcelable extra. Output: the confirmed [Booking], or null if the user backed out.
 *
 * Usage: `rememberLauncherForActivityResult(BookMeetAndGreet()) { booking -> … }`.
 */
class BookMeetAndGreet : ActivityResultContract<Cat, Booking?>() {

    override fun createIntent(context: Context, input: Cat): Intent =
        Intent(context, BookingActivity::class.java).putExtra(EXTRA_CAT, input)

    override fun parseResult(resultCode: Int, intent: Intent?): Booking? =
        if (resultCode == Activity.RESULT_OK && intent != null) {
            IntentCompat.getParcelableExtra(intent, EXTRA_BOOKING, Booking::class.java)
        } else {
            null
        }

    companion object {
        const val EXTRA_CAT = "com.thefelineco.extra.CAT"
        const val EXTRA_BOOKING = "com.thefelineco.extra.BOOKING"
    }
}
