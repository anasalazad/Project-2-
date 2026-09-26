package com.thefelineco.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.Product
import com.thefelineco.domain.model.ProductCategory
import com.thefelineco.domain.validation.Validators
import com.thefelineco.ui.adopt.FelineFilterChip
import com.thefelineco.ui.auth.FormErrorBanner
import com.thefelineco.ui.common.CenteredContent
import com.thefelineco.ui.components.ConfirmDialog
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.ValidatedTextField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProductField { NAME, BRAND, PRICE, STOCK, DESCRIPTION, IMAGE }

data class ProductFormState(
    val isLoading: Boolean = false,
    val id: Long = 0,
    val name: String = "",
    val brand: String = "Royal Feline",
    val category: ProductCategory = ProductCategory.FOOD,
    val price: String = "",
    val stock: String = "",
    val description: String = "",
    val imageName: String = "product_",
    val errors: Map<ProductField, String> = emptyMap(),
    val formError: String? = null,
    val isSaving: Boolean = false,
    val outcome: FormOutcome? = null,
) {
    val isNew: Boolean get() = id == 0L
}

/** Add a new product (productId = 0) or edit an existing one. */
class ProductFormViewModel(
    productId: Long,
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductFormState(isLoading = productId != 0L))
    val state: StateFlow<ProductFormState> = _state.asStateFlow()

    init {
        if (productId != 0L) {
            viewModelScope.launch {
                val p = shopRepository.observeProduct(productId).first()
                _state.value = p?.let {
                    ProductFormState(
                        id = it.id, name = it.name, brand = it.brand, category = it.category,
                        price = it.priceCredits.toString(), stock = it.stock.toString(),
                        description = it.description, imageName = it.imageName,
                    )
                } ?: ProductFormState(formError = "This product no longer exists")
            }
        }
    }

    fun update(field: ProductField? = null, change: (ProductFormState) -> ProductFormState) {
        _state.update { current ->
            change(current).copy(errors = if (field == null) current.errors else current.errors - field, formError = null)
        }
    }

    internal fun validate(s: ProductFormState): Map<ProductField, String> = buildMap {
        if (s.name.trim().length < 3) put(ProductField.NAME, "Name must be at least 3 characters")
        Validators.required(s.brand, "Brand")?.let { put(ProductField.BRAND, it) }
        Validators.intInRange(s.price, "Price", 1..999)?.let { put(ProductField.PRICE, it) }
        Validators.intInRange(s.stock, "Stock", 0..AdminProductsViewModel.MAX_STOCK)?.let { put(ProductField.STOCK, it) }
        if (s.description.trim().length < 10) put(ProductField.DESCRIPTION, "Add a short description (10+ characters)")
        validateImageName(s.imageName)?.let { put(ProductField.IMAGE, it) }
    }

    fun save() {
        val s = _state.value
        if (s.isSaving) return
        val errors = validate(s)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors, formError = "Please fix the highlighted fields") }
            return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val product = Product(
                id = s.id, name = s.name.trim(), brand = s.brand.trim(), category = s.category,
                priceCredits = s.price.trim().toInt(), description = s.description.trim(),
                imageName = s.imageName.trim(), stock = s.stock.trim().toInt(),
            )
            runCatching { shopRepository.saveProduct(product) }
                .onSuccess { _state.update { it.copy(isSaving = false, outcome = FormOutcome.Saved("Saved ${product.name}")) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, formError = e.message ?: "Couldn't save") } }
        }
    }

    fun delete() {
        val s = _state.value
        viewModelScope.launch {
            runCatching { shopRepository.deleteProduct(s.id) }
                .onSuccess { _state.update { it.copy(outcome = FormOutcome.Deleted("Removed ${s.name}")) } }
                .onFailure { e -> _state.update { it.copy(formError = e.message ?: "Couldn't delete") } }
        }
    }
}

@Composable
fun ProductFormScreen(
    onBack: () -> Unit,
    onDone: (String) -> Unit,
    viewModel: ProductFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.outcome) {
        when (val outcome = state.outcome) {
            is FormOutcome.Saved -> onDone(outcome.message)
            is FormOutcome.Deleted -> onDone(outcome.message)
            null -> Unit
        }
    }
    if (state.isLoading) {
        LoadingState()
        return
    }
    val e = state.errors
    val vm = viewModel

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        FormTopBar(
            title = if (state.isNew) "Add a product" else "Edit product",
            onBack = onBack,
            onDelete = if (state.isNew) null else ({ confirmDelete = true }),
            onSave = vm::save,
            saving = state.isSaving,
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding()) {
            CenteredContent(Modifier.padding(24.dp), maxWidth = 900.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    FormCard("Photo") {
                        ImageNameField(
                            value = state.imageName,
                            onValueChange = { v -> vm.update(ProductField.IMAGE) { it.copy(imageName = v) } },
                            error = e[ProductField.IMAGE],
                            placeholderLabel = state.category.label,
                        )
                    }
                    FormCard("Details") {
                        ValidatedTextField(state.name, { v -> vm.update(ProductField.NAME) { it.copy(name = v) } }, "Product name", e[ProductField.NAME])
                        ValidatedTextField(state.brand, { v -> vm.update(ProductField.BRAND) { it.copy(brand = v) } }, "Brand", e[ProductField.BRAND])
                        ChipGroup("Category") {
                            ProductCategory.entries.forEach { c ->
                                FelineFilterChip(c.label, state.category == c) { vm.update { it.copy(category = c) } }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedTextField(
                                state.price, { v -> vm.update(ProductField.PRICE) { it.copy(price = v.filter(Char::isDigit).take(3)) } },
                                "Price (credits)", e[ProductField.PRICE], Modifier.weight(1f), keyboardType = KeyboardType.Number,
                            )
                            ValidatedTextField(
                                state.stock, { v -> vm.update(ProductField.STOCK) { it.copy(stock = v.filter(Char::isDigit).take(3)) } },
                                "Stock", e[ProductField.STOCK], Modifier.weight(1f), keyboardType = KeyboardType.Number,
                            )
                        }
                        ValidatedTextField(
                            state.description, { v -> vm.update(ProductField.DESCRIPTION) { it.copy(description = v) } },
                            "Description", e[ProductField.DESCRIPTION], singleLine = false, minLines = 3,
                        )
                    }
                    AnimatedVisibility(visible = state.formError != null) { FormErrorBanner(state.formError.orEmpty()) }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Remove ${state.name}?",
            message = "It will disappear from the shop and from any baskets. Past orders are kept.",
            confirmLabel = "Delete product",
            destructive = true,
            icon = Icons.Filled.DeleteOutline,
            onConfirm = {
                confirmDelete = false
                vm.delete()
            },
            onDismiss = { confirmDelete = false },
            dismissLabel = "Cancel",
        )
    }
}
