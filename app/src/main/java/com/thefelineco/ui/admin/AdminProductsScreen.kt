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
import androidx.compose.material3.IconButton
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
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.ProductQuery
import com.thefelineco.domain.ProductSort
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.ui.adopt.FelineFilterChip
import com.thefelineco.ui.common.isWideLayout
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.QuantityStepper
import com.thefelineco.ui.components.SearchField
import com.thefelineco.ui.components.SortMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProductsUiState(
    val isLoading: Boolean = true,
    val query: ProductQuery = ProductQuery(),
    val products: List<Product> = emptyList(),
    val total: Int = 0,
)

/** Product catalogue management with quick stock adjustment. */
class AdminProductsViewModel(private val shopRepository: ShopRepository) : ViewModel() {
    private val query = MutableStateFlow(ProductQuery())

    val uiState: StateFlow<AdminProductsUiState> = combine(shopRepository.observeProducts(), query) { products, q ->
        AdminProductsUiState(isLoading = false, query = q, products = q.apply(products), total = products.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminProductsUiState())

    fun onSearch(text: String) = query.update { it.copy(text = text) }
    fun onCategory(category: ProductCategory?) = query.update { it.copy(category = category) }
    fun onSort(sort: ProductSort) = query.update { it.copy(sort = sort) }

    /** Saves a new stock level straight away; the list updates from the database. */
    fun setStock(product: Product, stock: Int) {
        viewModelScope.launch { shopRepository.saveProduct(product.copy(stock = stock.coerceIn(0, MAX_STOCK))) }
    }

    companion object {
        const val MAX_STOCK = 999
    }
}

@Composable
fun AdminProductsScreen(
    onEditProduct: (Long) -> Unit,
    onAddProduct: () -> Unit,
    viewModel: AdminProductsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wide = isWideLayout()
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AdminHeader("Manage products", "${state.total} products in the shop")
            Row(
                Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SearchField(state.query.text, viewModel::onSearch, "Search products", Modifier.weight(1f))
                SortMenu(state.query.sort, ProductSort.entries, ProductSort::label, viewModel::onSort, compact = !wide)
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FelineFilterChip("All", state.query.category == null) { viewModel.onCategory(null) } }
                items(ProductCategory.entries) { c -> FelineFilterChip(c.label, state.query.category == c) { viewModel.onCategory(c) } }
            }
            Box(Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingState()
                    state.products.isEmpty() -> EmptyState(
                        "No products found", "Try another search or category.", Modifier.align(Alignment.Center), icon = Icons.Filled.SearchOff,
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(420.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            AdminProductRow(
                                product = product,
                                onStock = { viewModel.setStock(product, it) },
                                onEdit = { onEditProduct(product.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddProduct,
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("Add product") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        )
    }
}

@Composable
private fun AdminProductRow(product: Product, onStock: (Int) -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AssetImage(product.imageName, null, Modifier.size(64.dp).clip(MaterialTheme.shapes.medium), placeholderLabel = null)
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                Text(
                    "${product.category.label} · ${product.priceCredits} credits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (product.inStock) "${product.stock} in stock" else "Out of stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.stock <= AdminDashboardViewModel.LOW_STOCK) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            QuantityStepper(
                value = product.stock,
                onValueChange = onStock,
                range = 0..AdminProductsViewModel.MAX_STOCK,
                itemName = "${product.name} in stock",
            )
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit ${product.name}") }
        }
    }
}
