package com.thefelineco.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.CatQuery
import com.thefelineco.domain.CatSort
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.ui.adopt.FelineFilterChip
import com.thefelineco.ui.common.isWideLayout
import com.thefelineco.ui.components.AdoptionStatusChip
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.FeeBadge
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.SearchField
import com.thefelineco.ui.components.SortMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AdminCatsUiState(
    val isLoading: Boolean = true,
    val query: CatQuery = CatQuery(statuses = AdoptionStatus.entries.toSet()),
    /** Null means "all statuses". */
    val statusFilter: AdoptionStatus? = null,
    val cats: List<Cat> = emptyList(),
    val counts: Map<AdoptionStatus, Int> = emptyMap(),
    val total: Int = 0,
)

/** Every listing (including adopted cats) with search, status filter and sorting. */
class AdminCatsViewModel(catRepository: CatRepository) : ViewModel() {
    private val query = MutableStateFlow(CatQuery(statuses = AdoptionStatus.entries.toSet()))
    private val statusFilter = MutableStateFlow<AdoptionStatus?>(null)

    val uiState: StateFlow<AdminCatsUiState> = combine(catRepository.observeCats(), query, statusFilter) { cats, q, status ->
        val effective = q.copy(statuses = status?.let(::setOf) ?: AdoptionStatus.entries.toSet())
        AdminCatsUiState(
            isLoading = false,
            query = q,
            statusFilter = status,
            cats = effective.apply(cats),
            counts = cats.groupingBy { it.status }.eachCount(),
            total = cats.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminCatsUiState())

    fun onSearch(text: String) = query.update { it.copy(text = text) }
    fun onSort(sort: CatSort) = query.update { it.copy(sort = sort) }
    fun onStatusFilter(status: AdoptionStatus?) = statusFilter.update { status }
}

@Composable
fun AdminCatsScreen(
    onEditCat: (Long) -> Unit,
    onAddCat: () -> Unit,
    viewModel: AdminCatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wide = isWideLayout()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AdminHeader("Manage cats", "${state.total} listings in total")
            Row(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SearchField(state.query.text, viewModel::onSearch, "Search listings", Modifier.weight(1f))
                SortMenu(state.query.sort, CatSort.entries, CatSort::label, viewModel::onSort, compact = !wide)
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FelineFilterChip("All (${state.total})", state.statusFilter == null) { viewModel.onStatusFilter(null) } }
                items(AdoptionStatus.entries) { status ->
                    FelineFilterChip("${status.label} (${state.counts[status] ?: 0})", state.statusFilter == status) {
                        viewModel.onStatusFilter(status)
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingState()
                    state.cats.isEmpty() -> EmptyState(
                        "No listings found", "Try another search or status.", Modifier.align(Alignment.Center), icon = Icons.Filled.SearchOff,
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(360.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.cats, key = { it.id }) { cat ->
                            AdminCatRow(cat, onClick = { onEditCat(cat.id) }, modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddCat,
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("Add cat") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        )
    }
}

@Composable
private fun AdminCatRow(cat: Cat, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AssetImage(cat.imageName, null, Modifier.size(72.dp).clip(MaterialTheme.shapes.medium))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(cat.name, style = MaterialTheme.typography.titleLarge)
                Text("${cat.breed} · ${cat.ageLabel}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeeBadge(cat.adoptionFee)
                    AdoptionStatusChip(cat.status)
                }
            }
            Icon(Icons.Filled.Edit, contentDescription = "Edit ${cat.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
