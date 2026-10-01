package com.thefelineco.ui.adopt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.CatSort
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.ui.common.isExpandedLayout
import com.thefelineco.ui.common.isWideLayout
import com.thefelineco.ui.components.CatCard
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.SearchField
import com.thefelineco.ui.components.SortMenu

/** Browse every cat up for adoption, with search, filters and sorting. */
@Composable
fun AdoptScreen(
    onOpenCat: (Long) -> Unit,
    viewModel: AdoptViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AdoptContent(state, viewModel::onEvent, onOpenCat)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptContent(state: AdoptUiState, onEvent: (AdoptEvent) -> Unit, onOpenCat: (Long) -> Unit) {
    val expanded = isExpandedLayout()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    Row(Modifier.fillMaxSize()) {
        // Large tablets: filters are always visible in a side panel.
        if (expanded) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.width(300.dp).fillMaxHeight()) {
                CatFilterPanel(
                    query = state.query,
                    onEvent = onEvent,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(24.dp),
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
            AdoptHeader(
                state = state,
                onEvent = onEvent,
                showFiltersButton = !expanded,
                onOpenFilters = { showFilterSheet = true },
            )
            QuickFilters(state, onEvent)
            Box(Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingState()
                    state.results.isEmpty() -> EmptyState(
                        title = if (state.query.favouritesOnly && state.favourites.isEmpty()) "No favourites yet" else "No cats match",
                        message = if (state.query.favouritesOnly && state.favourites.isEmpty()) {
                            "Tap the heart on any cat to save them here."
                        } else {
                            "Try a different search or remove a filter. New cats arrive every week!"
                        },
                        icon = Icons.Filled.SearchOff,
                        actionLabel = "Clear filters",
                        onAction = {
                            onEvent(AdoptEvent.SearchChanged(""))
                            onEvent(AdoptEvent.ClearFilters)
                        },
                        modifier = Modifier.align(Alignment.Center),
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(if (isWideLayout()) 220.dp else 160.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.results, key = { it.id }) { cat ->
                            // animateItem() makes cards slide into place as filters change.
                            CatCard(
                                cat = cat,
                                onClick = { onOpenCat(cat.id) },
                                isFavourite = cat.id in state.favourites,
                                onToggleFavourite = { onEvent(AdoptEvent.ToggleFavourite(cat.id)) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet && !expanded) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                CatFilterPanel(state.query, onEvent)
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).heightIn(min = 52.dp),
                ) { Text("Show ${state.results.size} cats") }
            }
        }
    }
}

@Composable
private fun AdoptHeader(
    state: AdoptUiState,
    onEvent: (AdoptEvent) -> Unit,
    showFiltersButton: Boolean,
    onOpenFilters: () -> Unit,
) {
    val wide = isWideLayout()
    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column {
            Text("Adopt a cat", style = MaterialTheme.typography.headlineLarge)
            Text(
                if (state.results.size == state.totalCount) "${state.totalCount} cats looking for a home"
                else "Showing ${state.results.size} of ${state.totalCount} cats",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchField(
                value = state.query.text,
                onValueChange = { onEvent(AdoptEvent.SearchChanged(it)) },
                placeholder = "Search name, breed, colour or personality",
                modifier = Modifier.weight(1f),
            )
            SortMenu(
                selected = state.query.sort,
                options = CatSort.entries,
                label = CatSort::label,
                onSelect = { onEvent(AdoptEvent.SortChanged(it)) },
                compact = !wide,
            )
            if (showFiltersButton) {
                val count = state.query.activeFilterCount
                FilledTonalButton(onClick = onOpenFilters, modifier = Modifier.heightIn(min = 56.dp)) {
                    BadgedBox(badge = { if (count > 0) Badge { Text("$count") } }) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filters")
                    }
                    if (wide) Text("   Filters")
                }
            }
        }
    }
}

/** One-tap chips for the most common filters: fee and age. */
@Composable
private fun QuickFilters(state: AdoptUiState, onEvent: (AdoptEvent) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FelineFilterChip("♥ Favourites (${state.favourites.size})", state.query.favouritesOnly) {
                onEvent(AdoptEvent.ToggleFavouritesOnly)
            }
        }
        item {
            FelineFilterChip("Free to adopt", state.query.freeOnly) { onEvent(AdoptEvent.ToggleFreeOnly) }
        }
        items(AgeGroup.entries) { group ->
            FelineFilterChip(group.pluralLabel, group in state.query.ageGroups) { onEvent(AdoptEvent.ToggleAgeGroup(group)) }
        }
    }
}
