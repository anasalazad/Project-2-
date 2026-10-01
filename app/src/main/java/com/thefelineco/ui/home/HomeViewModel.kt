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
import kotlinx.coroutines.launch
import com.thefelineco.ui.common.favouriteIds
import com.thefelineco.ui.common.toggleFavourite

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val newArrivals: List<Cat> = emptyList(),
    val availableCount: Int = 0,
    val freeCount: Int = 0,
    val kittenCount: Int = 0,
    val shopHighlights: List<Product> = emptyList(),
    /** Hearted cats that haven't been adopted yet, by name. */
    val favouriteCats: List<Cat> = emptyList(),
    val favourites: Set<Long> = emptySet(),
)

/** Builds the Home dashboard from live cat, product and user data. */
class HomeViewModel(
    private val userRepository: UserRepository,
    private val catRepository: CatRepository,
    shopRepository: ShopRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.currentUser,
        catRepository.observeCats(),
        shopRepository.observeProducts(),
        favouriteIds(userRepository, catRepository),
    ) { user, cats, products, favourites ->
        val available = cats.filter { it.status == AdoptionStatus.AVAILABLE }
        HomeUiState(
            isLoading = false,
            user = user,
            newArrivals = available.sortedByDescending { it.listedAt }.take(NEW_ARRIVALS),
            availableCount = available.size,
            freeCount = available.count { it.isFree },
            kittenCount = available.count { it.ageGroup == AgeGroup.KITTEN },
            shopHighlights = products.filter { it.inStock }.take(SHOP_HIGHLIGHTS),
            favouriteCats = cats.filter { it.id in favourites && it.status != AdoptionStatus.ADOPTED }.sortedBy { it.name },
            favourites = favourites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onToggleFavourite(catId: Long) {
        viewModelScope.launch { toggleFavourite(userRepository, catRepository, catId) }
    }

    private companion object {
        const val NEW_ARRIVALS = 10
        const val SHOP_HIGHLIGHTS = 8
    }
}
