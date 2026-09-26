package com.thefelineco.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.ProductQuery
import com.thefelineco.domain.ProductSort
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopUiState(
    val isLoading: Boolean = true,
    val query: ProductQuery = ProductQuery(),
    val products: List<Product> = emptyList(),
    val credits: Int = 0,
    /** How many of each product (by id) are already in the basket. */
    val inBasket: Map<Long, Int> = emptyMap(),
)

sealed interface ShopEvent {
    data class SearchChanged(val text: String) : ShopEvent
    data class CategorySelected(val category: ProductCategory?) : ShopEvent
    data class SortChanged(val sort: ProductSort) : ShopEvent
    data object ToggleInStockOnly : ShopEvent
    data class AddToBasket(val product: Product, val quantity: Int = 1) : ShopEvent
}

/** The shop catalogue with search, category filter, sorting and add-to-basket. */
@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModel(
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val query = MutableStateFlow(ProductQuery())

    private val basket: Flow<List<CartItem>> = userRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else shopRepository.observeCart(user.id)
    }

    val uiState: StateFlow<ShopUiState> = combine(
        shopRepository.observeProducts(),
        query,
        userRepository.currentUser,
        basket,
    ) { products, q, user, cart ->
        ShopUiState(
            isLoading = false,
            query = q,
            products = q.apply(products),
            credits = user?.credits ?: 0,
            inBasket = cart.associate { it.productId to it.quantity },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.SearchChanged -> query.update { it.copy(text = event.text) }
            is ShopEvent.CategorySelected -> query.update { it.copy(category = event.category) }
            is ShopEvent.SortChanged -> query.update { it.copy(sort = event.sort) }
            ShopEvent.ToggleInStockOnly -> query.update { it.copy(inStockOnly = !it.inStockOnly) }
            is ShopEvent.AddToBasket -> addToBasket(event.product, event.quantity)
        }
    }

    private fun addToBasket(product: Product, quantity: Int) {
        viewModelScope.launch {
            val user = userRepository.currentUser.first() ?: return@launch
            shopRepository.addToCart(user.id, product.id, quantity)
                .onSuccess { _messages.send("Added $quantity × ${product.name} to your basket") }
                .onFailure { _messages.send(it.message ?: "Couldn't add to basket") }
        }
    }
}
