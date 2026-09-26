package com.thefelineco.ui.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.CartItem
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.common.isExpandedLayout
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.QuantityStepper

/**
 * The basket. "Checkout" hands the lines to MainActivity, which launches CheckoutActivity with
 * them as an `ArrayList<CartItem>` of Parcelables.
 */
@Composable
fun BasketScreen(
    onCheckout: (List<CartItem>) -> Unit,
    onShop: () -> Unit,
    viewModel: BasketViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    BasketContent(state, viewModel::setQuantity, viewModel::remove, onCheckout, onShop)
}

@Composable
fun BasketContent(
    state: BasketUiState,
    onQuantity: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit,
    onCheckout: (List<CartItem>) -> Unit,
    onShop: () -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)) {
            Text("Your basket", style = MaterialTheme.typography.headlineLarge)
            Text(
                if (state.itemCount == 1) "1 item" else "${state.itemCount} items",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingState()
                state.items.isEmpty() -> EmptyState(
                    title = "Your basket is empty",
                    message = "Treat your cat to Royal Feline nutrition, toys and more.",
                    icon = Icons.Filled.ShoppingBag,
                    actionLabel = "Visit the shop",
                    onAction = onShop,
                    modifier = Modifier.align(Alignment.Center),
                )
                isExpandedLayout() -> Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    BasketLines(state.items, onQuantity, onRemove, Modifier.weight(1.4f).fillMaxHeight())
                    Box(Modifier.weight(1f)) {
                        OrderSummary(state, onCheckout = { onCheckout(state.items) }, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                else -> Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    BasketLines(state.items, onQuantity, onRemove, Modifier.weight(1f))
                    OrderSummary(state, onCheckout = { onCheckout(state.items) }, modifier = Modifier.padding(vertical = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun BasketLines(
    items: List<CartItem>,
    onQuantity: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(modifier, contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.productId }) { item ->
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.animateItem(),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AssetImage(item.imageName, null, Modifier.size(84.dp).clip(MaterialTheme.shapes.medium))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text("${item.unitPrice} credits each", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.quantity > item.stock) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(
                                    if (item.stock == 0) "Now out of stock. Please remove." else "Only ${item.stock} left. Please reduce.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        QuantityStepper(
                            value = item.quantity,
                            onValueChange = { onQuantity(item, it) },
                            range = 1..maxOf(item.stock, item.quantity),
                            itemName = item.name,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(onClick = { onRemove(item) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove ${item.name}")
                        }
                        Text("${item.lineTotal}", style = MaterialTheme.typography.titleLarge)
                        Text("credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummary(state: BasketUiState, onCheckout: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Order summary", style = MaterialTheme.typography.titleLarge)
            SummaryLine("Subtotal", "${state.subtotal} credits")
            SummaryLine("Delivery", "Chosen at checkout")
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            SummaryLine("Your balance", "${state.credits} credits")
            SummaryLine("Balance after", "${state.balanceAfter} credits", warn = state.shortfall > 0)
            if (state.shortfall > 0) {
                Text(
                    "You need ${state.shortfall} more credits. Complete an adoption to earn 250!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onCheckout,
                enabled = state.canCheckout,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("  Checkout", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, warn: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}
