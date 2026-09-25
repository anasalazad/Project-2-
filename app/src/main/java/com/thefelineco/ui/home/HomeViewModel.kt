package com.thefelineco.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val newArrivals: List<Cat> = emptyList(),
    val availableCount: Int = 0,
    val freeCount: Int = 0,
    val kittenCount: Int = 0,
    val shopHighlights: List<Product> = emptyList(),
)

/** Builds the Home dashboard from live cat, product and user data. */
class HomeViewModel(
    userRepository: UserRepository,
    catRepository: CatRepository,
    shopRepository: ShopRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.currentUser,
        catRepository.observeCats(),
        shopRepository.observeProducts(),
    ) { user, cats, products ->
        val available = cats.filter { it.status == AdoptionStatus.AVAILABLE }
        HomeUiState(
            isLoading = false,
            user = user,
            newArrivals = available.sortedByDescending { it.listedAt }.take(NEW_ARRIVALS),
            availableCount = available.size,
            freeCount = available.count { it.isFree },
            kittenCount = available.count { it.ageGroup == AgeGroup.KITTEN },
            shopHighlights = products.filter { it.inStock }.take(SHOP_HIGHLIGHTS),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private companion object {
        const val NEW_ARRIVALS = 10
        const val SHOP_HIGHLIGHTS = 8
    }
}
