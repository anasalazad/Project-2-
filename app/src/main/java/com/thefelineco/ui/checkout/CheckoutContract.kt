package com.thefelineco.ui.checkout

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Order

/**
 * Activity Result contract for checkout. Input: the basket lines, sent as an
 * `ArrayList<CartItem>` of Parcelables. Output: the placed [Order], or null if cancelled.
 */
class CheckoutContract : ActivityResultContract<List<CartItem>, Order?>() {

    override fun createIntent(context: Context, input: List<CartItem>): Intent =
        Intent(context, CheckoutActivity::class.java).putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(input))

    override fun parseResult(resultCode: Int, intent: Intent?): Order? =
        if (resultCode == Activity.RESULT_OK && intent != null) {
            IntentCompat.getParcelableExtra(intent, EXTRA_ORDER, Order::class.java)
        } else {
            null
        }

    companion object {
        const val EXTRA_ITEMS = "com.thefelineco.extra.CART_ITEMS"
        const val EXTRA_ORDER = "com.thefelineco.extra.ORDER"
    }
}
