package com.thefelineco.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.SettingsStore
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.CreditTransaction
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val transactions: List<CreditTransaction> = emptyList(),
    val orders: List<Order> = emptyList(),
    val darkTheme: Boolean = true,
) {
    val totalEarned: Int get() = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalSpent: Int get() = -transactions.filter { it.amount < 0 }.sumOf { it.amount }
}

/** Account, credit wallet history, orders and the theme setting. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val userRepository: UserRepository,
    shopRepository: ShopRepository,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val transactions: Flow<List<CreditTransaction>> = userRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else userRepository.observeTransactions(user.id)
    }

    private val orders: Flow<List<Order>> = userRepository.currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else shopRepository.observeOrders(user.id)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.currentUser,
        transactions,
        orders,
        settingsStore.darkTheme,
    ) { user, history, orderList, dark ->
        ProfileUiState(isLoading = false, user = user, transactions = history, orders = orderList, darkTheme = dark)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setDarkTheme(enabled) }
    }
}
