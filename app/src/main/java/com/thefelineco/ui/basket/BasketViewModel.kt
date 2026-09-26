package com.thefelineco.ui.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.subtotal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BasketUiState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val credits: Int = 0,
) {
    val subtotal: Int get() = items.subtotal
    val itemCount: Int get() = items.sumOf { it.quantity }
    val balanceAfter: Int get() = credits - subtotal

    /** Credits still needed before checkout is possible (0 if affordable). */
    val shortfall: Int get() = (subtotal - credits).coerceAtLeast(0)

    /** Lines asking for more than is in stock (stock may drop after they were added). */
    val overStocked: List<CartItem> get() = items.filter { it.quantity > it.stock }

    val canCheckout: Boolean get() = items.isNotEmpty() && shortfall == 0 && overStocked.isEmpty()
}

/** The signed-in customer's basket. Quantities are saved immediately, so the basket survives restarts. */
@OptIn(ExperimentalCoroutinesApi::class)
class BasketViewModel(
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
) : ViewModel() {

    val uiState: StateFlow<BasketUiState> = userRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(BasketUiState(isLoading = false))
            else shopRepository.observeCart(user.id).map { items ->
                BasketUiState(isLoading = false, items = items, credits = user.credits)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BasketUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun setQuantity(item: CartItem, quantity: Int) {
        viewModelScope.launch {
            val user = userRepository.currentUser.first() ?: return@launch
            shopRepository.setCartQuantity(user.id, item.productId, quantity)
                .onFailure { _messages.send(it.message ?: "Couldn't update your basket") }
        }
    }

    fun remove(item: CartItem) {
        setQuantity(item, 0)
        viewModelScope.launch { _messages.send("Removed ${item.name}") }
    }
}
