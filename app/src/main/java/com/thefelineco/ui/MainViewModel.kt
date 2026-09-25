package com.thefelineco.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Who is using the app right now. MainActivity picks the auth flow or the main shell from this. */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User) : SessionState

    /** Stable key for animations: it changes only when the signed-in account changes. */
    val contentKey: String
        get() = when (this) {
            Loading -> "loading"
            SignedOut -> "signed-out"
            is SignedIn -> "user-${user.id}"
        }
}

/** App-wide state for MainActivity: the session and the basket badge count. */
class MainViewModel(
    private val userRepository: UserRepository,
    shopRepository: ShopRepository,
) : ViewModel() {

    val session: StateFlow<SessionState> = userRepository.currentUser
        .map { user -> if (user == null) SessionState.SignedOut else SessionState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val basketCount: StateFlow<Int> = userRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(0)
            else shopRepository.observeCart(user.id).map { lines -> lines.sumOf { it.quantity } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun logout() {
        viewModelScope.launch { userRepository.logout() }
    }
}
