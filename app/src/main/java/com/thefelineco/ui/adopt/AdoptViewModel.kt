package com.thefelineco.ui.adopt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.domain.CatQuery
import com.thefelineco.domain.CatSort
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex
import com.thefelineco.ui.common.toggle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AdoptUiState(
    val isLoading: Boolean = true,
    val query: CatQuery = CatQuery(),
    val results: List<Cat> = emptyList(),
    /** Cats visible before filters (i.e. not adopted), for "12 of 22". */
    val totalCount: Int = 0,
)

/** Everything the user can do on the Adopt screen. */
sealed interface AdoptEvent {
    data class SearchChanged(val text: String) : AdoptEvent
    data class ToggleAgeGroup(val group: AgeGroup) : AdoptEvent
    data class ToggleSex(val sex: Sex) : AdoptEvent
    data class ToggleCoat(val coat: CoatLength) : AdoptEvent
    data object ToggleKids : AdoptEvent
    data object ToggleCats : AdoptEvent
    data object ToggleDogs : AdoptEvent
    data object ToggleFreeOnly : AdoptEvent
    data class SortChanged(val sort: CatSort) : AdoptEvent
    data object ClearFilters : AdoptEvent
}

/**
 * Search, filter and sort for the cat listings. The query is plain state, and results are
 * recalculated whenever the query or the underlying cats change (so an admin's edit or a new
 * booking shows up live).
 *
 * @param initialFreeOnly true when opened from the "Older cats adopt for free" banner.
 */
class AdoptViewModel(
    initialFreeOnly: Boolean,
    catRepository: CatRepository,
) : ViewModel() {

    private val query = MutableStateFlow(CatQuery(freeOnly = initialFreeOnly))

    val uiState: StateFlow<AdoptUiState> = combine(catRepository.observeCats(), query) { cats, q ->
        AdoptUiState(
            isLoading = false,
            query = q,
            results = q.apply(cats),
            totalCount = cats.count { it.status in q.statuses },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdoptUiState(query = query.value))

    fun onEvent(event: AdoptEvent) {
        query.update { q ->
            when (event) {
                is AdoptEvent.SearchChanged -> q.copy(text = event.text)
                is AdoptEvent.ToggleAgeGroup -> q.copy(ageGroups = q.ageGroups.toggle(event.group))
                is AdoptEvent.ToggleSex -> q.copy(sexes = q.sexes.toggle(event.sex))
                is AdoptEvent.ToggleCoat -> q.copy(coats = q.coats.toggle(event.coat))
                AdoptEvent.ToggleKids -> q.copy(goodWithKids = !q.goodWithKids)
                AdoptEvent.ToggleCats -> q.copy(goodWithCats = !q.goodWithCats)
                AdoptEvent.ToggleDogs -> q.copy(goodWithDogs = !q.goodWithDogs)
                AdoptEvent.ToggleFreeOnly -> q.copy(freeOnly = !q.freeOnly)
                is AdoptEvent.SortChanged -> q.copy(sort = event.sort)
                // Keep the search text and sort order; only reset the filters.
                AdoptEvent.ClearFilters -> CatQuery(text = q.text, sort = q.sort)
            }
        }
    }
}
