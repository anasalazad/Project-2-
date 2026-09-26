package com.thefelineco.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.components.BookingCard
import com.thefelineco.ui.components.ConfirmDialog
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.SectionHeader

/** "My bookings": upcoming appointments (cancellable) and past ones. */
@Composable
fun BookingsScreen(
    onFindCat: () -> Unit,
    viewModel: BookingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    BookingsContent(state, onCancel = viewModel::cancel, onFindCat = onFindCat)
}

@Composable
fun BookingsContent(state: BookingsUiState, onCancel: (Booking) -> Unit, onFindCat: () -> Unit) {
    var toCancel by remember { mutableStateOf<Booking?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
            Text("My bookings", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Your meet & greet appointments at The Feline Co.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingState()
                state.upcoming.isEmpty() && state.past.isEmpty() -> EmptyState(
                    title = "No bookings yet",
                    message = "Find a cat you love and book a meet & greet. Cats aged 2+ are free to adopt!",
                    icon = Icons.Filled.EventAvailable,
                    actionLabel = "Find a cat",
                    onAction = onFindCat,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(420.dp),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    section("Upcoming", "Held until your visit", state.upcoming) { booking ->
                        BookingCard(booking, Modifier.animateItem()) {
                            Text(
                                if (booking.status == BookingStatus.CONFIRMED) "Confirmed by our team. See you soon!"
                                else "Waiting for our team to confirm. We'll be in touch shortly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(Modifier.align(Alignment.End)) {
                                OutlinedButton(onClick = { toCancel = booking }) { Text("Cancel booking") }
                            }
                        }
                    }
                    section("Past", null, state.past) { booking ->
                        BookingCard(booking, Modifier.animateItem())
                    }
                }
            }
        }
    }

    toCancel?.let { booking ->
        ConfirmDialog(
            title = "Cancel your visit with ${booking.catName}?",
            message = buildString {
                append("${booking.catName} will become available to other families.")
                if (booking.feeCredits > 0) append(" Your ${booking.feeCredits} credits will be refunded straight away.")
            },
            confirmLabel = "Cancel booking",
            dismissLabel = "Keep booking",
            destructive = true,
            icon = Icons.Filled.EventBusy,
            onConfirm = {
                onCancel(booking)
                toCancel = null
            },
            onDismiss = { toCancel = null },
        )
    }
}

/** A full-width section header followed by its cards. Skipped entirely when [items] is empty. */
private fun LazyGridScope.section(
    title: String,
    subtitle: String?,
    items: List<Booking>,
    card: @Composable LazyGridItemScope.(Booking) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = "header-$title", span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(title = title, subtitle = subtitle)
    }
    items(items, key = { it.id }) { booking -> card(this, booking) }
}
