package com.thefelineco.ui.catdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.thefelineco.ui.common.favouriteIds
import com.thefelineco.ui.common.toggleFavourite

data class CatDetailUiState(
    val isLoading: Boolean = true,
    val cat: Cat? = null,
    val credits: Int = 0,
    val isAdmin: Boolean = false,
    val similar: List<Cat> = emptyList(),
    val isFavourite: Boolean = false,
) {
    val canAfford: Boolean get() = cat != null && CreditRules.canAfford(credits, cat.adoptionFee)
    val creditsShort: Int get() = ((cat?.adoptionFee ?: 0) - credits).coerceAtLeast(0)
    val canBook: Boolean get() = cat?.status == AdoptionStatus.AVAILABLE && canAfford && !isAdmin
}

/** One cat's profile, kept live so a status change (e.g. someone else booking) shows immediately. */
class CatDetailViewModel(
    private val catId: Long,
    private val catRepository: CatRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val uiState: StateFlow<CatDetailUiState> = combine(
        catRepository.observeCats(),
        userRepository.currentUser,
        favouriteIds(userRepository, catRepository),
    ) { cats, user, favourites ->
        val cat = cats.firstOrNull { it.id == catId }
        CatDetailUiState(
            isLoading = false,
            cat = cat,
            credits = user?.credits ?: 0,
            isAdmin = user?.isAdmin == true,
            isFavourite = catId in favourites,
            // Other available cats of the same life stage: "You might also love".
            similar = if (cat == null) emptyList() else cats
                .filter { it.id != cat.id && it.status == AdoptionStatus.AVAILABLE && it.ageGroup == cat.ageGroup }
                .take(SIMILAR_COUNT),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatDetailUiState())

    fun onToggleFavourite() {
        viewModelScope.launch { toggleFavourite(userRepository, catRepository, catId) }
    }

    private companion object {
        const val SIMILAR_COUNT = 8
    }
}
