package com.thefelineco.ui.checkout

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
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Order
import com.thefelineco.ui.common.setFelineContent

/**
 * Third activity: checkout for shop purchases.
 *
 * Receives the basket as an `ArrayList<CartItem>` ([CheckoutContract.EXTRA_ITEMS]) and returns the
 * placed [Order] to MainActivity with `setResult(RESULT_OK)`.
 */
class CheckoutActivity : ComponentActivity() {

    private val items: List<CartItem> by lazy {
        IntentCompat.getParcelableArrayListExtra(intent, CheckoutContract.EXTRA_ITEMS, CartItem::class.java).orEmpty()
    }

    private val viewModel: CheckoutViewModel by viewModels {
        val container = (application as FelineApplication).container
        viewModelFactory {
            initializer { CheckoutViewModel(items, container.userRepository, container.shopRepository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (items.isEmpty()) {
            finish()
            return
        }
        setFelineContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.placed) {
                state.placed?.let(::finishWithResult)
            }
            CheckoutScreen(state = state, onEvent = viewModel::onEvent, onClose = ::finish)
        }
    }

    private fun finishWithResult(order: Order) {
        setResult(RESULT_OK, Intent().putExtra(CheckoutContract.EXTRA_ORDER, order))
        finish()
    }
}
