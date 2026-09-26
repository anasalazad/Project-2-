package com.thefelineco.ui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.ProductSort
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.ui.adopt.FelineFilterChip
import com.thefelineco.ui.common.LocalSnackbarHostState
import com.thefelineco.ui.common.isWideLayout
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.CreditBalanceChip
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.ProductCard
import com.thefelineco.ui.components.QuantityStepper
import com.thefelineco.ui.components.SearchField
import com.thefelineco.ui.components.SortMenu

/** The shop: Royal Feline food, treats, toys and accessories, paid for with credits. */
@Composable
fun ShopScreen(viewModel: ShopViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    ShopContent(state, viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopContent(state: ShopUiState, onEvent: (ShopEvent) -> Unit) {
    // Remember the id, not the product, so the sheet always shows live stock.
    var openProductId by rememberSaveable { mutableStateOf<Long?>(null) }
    val wide = isWideLayout()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("The Shop", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Premium nutrition and play, paid for with your credits",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CreditBalanceChip(state.credits)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SearchField(
                    value = state.query.text,
                    onValueChange = { onEvent(ShopEvent.SearchChanged(it)) },
                    placeholder = "Search food, toys, beds…",
                    modifier = Modifier.weight(1f),
                )
                SortMenu(
                    selected = state.query.sort,
                    options = ProductSort.entries,
                    label = ProductSort::label,
                    onSelect = { onEvent(ShopEvent.SortChanged(it)) },
                    compact = !wide,
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { FelineFilterChip("All", state.query.category == null) { onEvent(ShopEvent.CategorySelected(null)) } }
            items(ProductCategory.entries) { category ->
                FelineFilterChip(category.label, state.query.category == category) {
                    onEvent(ShopEvent.CategorySelected(category))
                }
            }
            item { FelineFilterChip("In stock only", state.query.inStockOnly) { onEvent(ShopEvent.ToggleInStockOnly) } }
        }
        Box(Modifier.weight(1f)) {
            when {
                state.isLoading -> LoadingState()
                state.products.isEmpty() -> EmptyState(
                    title = "Nothing found",
                    message = "Try another search or category.",
                    icon = Icons.Filled.SearchOff,
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(if (wide) 200.dp else 160.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { openProductId = product.id },
                            onAddToBasket = { onEvent(ShopEvent.AddToBasket(product)) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    val openProduct = state.products.firstOrNull { it.id == openProductId }
    if (openProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { openProductId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            ProductSheet(
                product = openProduct,
                alreadyInBasket = state.inBasket[openProduct.id] ?: 0,
                onAdd = { quantity ->
                    onEvent(ShopEvent.AddToBasket(openProduct, quantity))
                    openProductId = null
                },
            )
        }
    }
}

/** Product details with a quantity picker. The maximum is what's left after the basket. */
@Composable
private fun ProductSheet(product: Product, alreadyInBasket: Int, onAdd: (Int) -> Unit) {
    val available = (product.stock - alreadyInBasket).coerceAtLeast(0)
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    val wide = isWideLayout()

    val details: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(product.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(product.name, style = MaterialTheme.typography.headlineMedium)
            Text("${product.priceCredits} credits", style = MaterialTheme.typography.titleLarge)
            Text(product.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                when {
                    !product.inStock -> "Out of stock. Check back soon."
                    alreadyInBasket > 0 -> "${product.stock} in stock · $alreadyInBasket already in your basket"
                    else -> "${product.stock} in stock"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (product.inStock) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
            if (available > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuantityStepper(
                        value = quantity.coerceAtMost(available),
                        onValueChange = { quantity = it },
                        range = 1..available,
                        itemName = product.name,
                    )
                    Button(
                        onClick = { onAdd(quantity.coerceAtMost(available)) },
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    ) { Text("Add to basket · ${product.priceCredits * quantity.coerceAtMost(available)} credits") }
                }
            }
        }
    }

    if (wide) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(bottom = 32.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            AssetImage(product.imageName, product.name, Modifier.width(320.dp).aspectRatio(1f).clip(MaterialTheme.shapes.extraLarge), placeholderLabel = product.category.label)
            details(Modifier.weight(1f))
        }
    } else {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AssetImage(product.imageName, product.name, Modifier.fillMaxWidth().aspectRatio(1.4f).clip(MaterialTheme.shapes.extraLarge), placeholderLabel = product.category.label)
            details(Modifier)
        }
    }
}
