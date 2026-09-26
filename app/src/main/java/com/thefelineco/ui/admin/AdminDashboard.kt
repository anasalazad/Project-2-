package com.thefelineco.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.data.repository.BookingRepository
import com.thefelineco.data.repository.CatRepository
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Booking
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.domain.model.Product
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.common.isExpandedLayout
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.BookingCard
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.SectionHeader
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val adminName: String = "",
    val available: Int = 0,
    val pending: Int = 0,
    val adopted: Int = 0,
    val requests: List<Booking> = emptyList(),
    val confirmed: Int = 0,
    val lowStock: List<Product> = emptyList(),
)

/** Overview for admins: listing counts, booking requests to action and low-stock products. */
class AdminDashboardViewModel(
    userRepository: UserRepository,
    catRepository: CatRepository,
    private val bookingRepository: BookingRepository,
    shopRepository: ShopRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        userRepository.currentUser,
        catRepository.observeCats(),
        bookingRepository.observeAllBookings(),
        shopRepository.observeProducts(),
    ) { user, cats, bookings, products ->
        val byStatus = cats.groupingBy { it.status }.eachCount()
        DashboardUiState(
            isLoading = false,
            adminName = user?.firstName.orEmpty(),
            available = byStatus[AdoptionStatus.AVAILABLE] ?: 0,
            pending = byStatus[AdoptionStatus.PENDING] ?: 0,
            adopted = byStatus[AdoptionStatus.ADOPTED] ?: 0,
            requests = bookings.filter { it.status == BookingStatus.REQUESTED },
            confirmed = bookings.count { it.status == BookingStatus.CONFIRMED },
            lowStock = products.filter { it.stock <= LOW_STOCK }.sortedBy { it.stock },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun confirm(booking: Booking) = act { bookingRepository.confirm(booking.id).map { "Confirmed ${booking.fullName}'s visit with ${booking.catName}" } }

    fun decline(booking: Booking) = act { bookingRepository.decline(booking.id).map { "Declined. ${booking.catName} is available again." } }

    private fun act(block: suspend () -> Result<String>) {
        viewModelScope.launch {
            block().onSuccess { _messages.send(it) }.onFailure { _messages.send(it.message ?: "Something went wrong") }
        }
    }

    companion object {
        const val LOW_STOCK = 5
    }
}

@Composable
fun AdminDashboardScreen(
    onAddCat: () -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenProducts: () -> Unit,
    viewModel: AdminDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    if (state.isLoading) {
        LoadingState()
        return
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        AdminHeader(
            title = "Good day${if (state.adminName.isNotEmpty()) ", ${state.adminName}" else ""}",
            subtitle = "Here's what's happening at The Feline Co. today",
        ) {
            Button(onClick = onAddCat) {
                Icon(Icons.Filled.Add, null)
                Text(" Add a cat")
            }
        }
        StatRow(state)
        val expanded = isExpandedLayout()
        val requests: @Composable (Modifier) -> Unit = { m -> RequestsPanel(state.requests, viewModel::confirm, viewModel::decline, onOpenAppointments, m) }
        val stock: @Composable (Modifier) -> Unit = { m -> LowStockPanel(state.lowStock, onOpenProducts, m) }
        if (expanded) {
            Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                requests(Modifier.weight(1.5f))
                stock(Modifier.weight(1f))
            }
        } else {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                requests(Modifier)
                stock(Modifier)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatRow(state: DashboardUiState) {
    FlowRow(
        Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 4,
    ) {
        val tile = Modifier.weight(1f).widthIn(min = 200.dp)
        StatTile(state.available, "Available cats", Icons.Filled.Pets, tile)
        StatTile(state.pending, "Adoptions pending", Icons.Filled.HourglassTop, tile)
        StatTile(state.adopted, "Cats adopted", Icons.Filled.Favorite, tile)
        StatTile(state.requests.size, "New requests", Icons.AutoMirrored.Filled.EventNote, tile, highlight = state.requests.isNotEmpty())
    }
}

@Composable
private fun RequestsPanel(
    requests: List<Booking>,
    onConfirm: (Booking) -> Unit,
    onDecline: (Booking) -> Unit,
    onOpenAppointments: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Needs your attention", subtitle = "New meet & greet requests", actionLabel = "All appointments", onAction = onOpenAppointments)
        if (requests.isEmpty()) {
            EmptyState("All caught up", "No new requests right now.", icon = Icons.AutoMirrored.Filled.EventNote)
        }
        requests.forEach { booking ->
            BookingCard(booking) {
                Text("${booking.fullName} · ${booking.phone}", style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onDecline(booking) }) { Text("Decline") }
                    Button(onClick = { onConfirm(booking) }) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
private fun LowStockPanel(products: List<Product>, onOpenProducts: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Inventory2, null, tint = MaterialTheme.colorScheme.primary)
                Text("  Low stock", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenProducts) { Text("Manage") }
            }
            if (products.isEmpty()) {
                Text("Everything is well stocked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            products.forEach { product ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssetImage(product.imageName, null, Modifier.size(44.dp).clip(MaterialTheme.shapes.small))
                    Text(product.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        if (product.stock == 0) "Out" else "${product.stock} left",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (product.stock == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
