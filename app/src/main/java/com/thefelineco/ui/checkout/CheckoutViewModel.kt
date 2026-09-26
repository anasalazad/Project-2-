package com.thefelineco.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefelineco.data.repository.ShopRepository
import com.thefelineco.data.repository.UserRepository
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.CartItem
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.domain.model.Order
import com.thefelineco.domain.model.subtotal
import com.thefelineco.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckoutField { NAME, ADDRESS, SUBURB, POSTCODE }

data class CheckoutUiState(
    val items: List<CartItem>,
    val userId: Long? = null,
    val credits: Int = 0,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD,
    val fullName: String = "",
    val address: String = "",
    val suburb: String = "",
    val postcode: String = "",
    val errors: Map<CheckoutField, String> = emptyMap(),
    val formError: String? = null,
    val isSubmitting: Boolean = false,
    /** Set once the order is saved. CheckoutActivity then returns it as its result. */
    val placed: Order? = null,
) {
    val subtotal: Int get() = items.subtotal
    val total: Int get() = subtotal + deliveryMethod.extraCredits
    val balanceAfter: Int get() = credits - total
    val canAfford: Boolean get() = CreditRules.canAfford(credits, total)

    /** Click & collect needs no delivery address. */
    val needsAddress: Boolean get() = deliveryMethod != DeliveryMethod.CLICK_AND_COLLECT
}

sealed interface CheckoutEvent {
    data class NameChanged(val value: String) : CheckoutEvent
    data class AddressChanged(val value: String) : CheckoutEvent
    data class SuburbChanged(val value: String) : CheckoutEvent
    data class PostcodeChanged(val value: String) : CheckoutEvent
    data class MethodSelected(val method: DeliveryMethod) : CheckoutEvent
    data object Submit : CheckoutEvent
}

/** Delivery details and order placement for CheckoutActivity. */
class CheckoutViewModel(
    items: List<CartItem>,
    private val userRepository: UserRepository,
    private val shopRepository: ShopRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState(items = items))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            var prefilled = false
            userRepository.currentUser.collect { user ->
                _uiState.update { s ->
                    val base = s.copy(userId = user?.id, credits = user?.credits ?: 0)
                    if (!prefilled && user != null) {
                        prefilled = true
                        base.copy(fullName = s.fullName.ifEmpty { user.fullName })
                    } else {
                        base
                    }
                }
            }
        }
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            is CheckoutEvent.NameChanged -> edit(CheckoutField.NAME) { it.copy(fullName = event.value) }
            is CheckoutEvent.AddressChanged -> edit(CheckoutField.ADDRESS) { it.copy(address = event.value) }
            is CheckoutEvent.SuburbChanged -> edit(CheckoutField.SUBURB) { it.copy(suburb = event.value) }
            // Only digits, at most four: invalid input can't even be typed.
            is CheckoutEvent.PostcodeChanged -> edit(CheckoutField.POSTCODE) {
                it.copy(postcode = event.value.filter(Char::isDigit).take(4))
            }
            is CheckoutEvent.MethodSelected -> _uiState.update { it.copy(deliveryMethod = event.method, formError = null) }
            CheckoutEvent.Submit -> submit()
        }
    }

    private fun edit(field: CheckoutField, change: (CheckoutUiState) -> CheckoutUiState) {
        _uiState.update { change(it).copy(errors = it.errors - field, formError = null) }
    }

    internal fun validate(s: CheckoutUiState): Map<CheckoutField, String> = buildMap {
        Validators.name(s.fullName)?.let { put(CheckoutField.NAME, it) }
        if (s.needsAddress) {
            when {
                s.address.isBlank() -> put(CheckoutField.ADDRESS, "Street address is required")
                s.address.trim().length < 5 -> put(CheckoutField.ADDRESS, "Enter your full street address")
            }
            Validators.required(s.suburb, "Suburb")?.let { put(CheckoutField.SUBURB, it) }
            Validators.postcode(s.postcode)?.let { put(CheckoutField.POSTCODE, it) }
        }
    }

    private fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val errors = validate(s)
        when {
            errors.isNotEmpty() -> {
                _uiState.update { it.copy(errors = errors, formError = "Please check the highlighted details") }
                return
            }
            !s.canAfford -> {
                _uiState.update { it.copy(formError = "This order costs ${s.total} credits but you have ${s.credits}") }
                return
            }
        }
        val userId = s.userId ?: run {
            _uiState.update { it.copy(formError = "Please sign in again") }
            return
        }
        val order = Order(
            userId = userId,
            items = s.items,
            subtotal = s.subtotal,
            deliveryMethod = s.deliveryMethod,
            deliveryName = s.fullName,
            address = if (s.needsAddress) "${s.address.trim()}, ${s.suburb.trim()} ${s.postcode}" else STORE_ADDRESS,
        )
        _uiState.update { it.copy(isSubmitting = true, formError = null) }
        viewModelScope.launch {
            shopRepository.checkout(order)
                .onSuccess { placed -> _uiState.update { it.copy(isSubmitting = false, placed = placed) } }
                .onFailure { error -> _uiState.update { it.copy(isSubmitting = false, formError = error.message ?: "Couldn't place your order") } }
        }
    }

    companion object {
        const val STORE_ADDRESS = "Click & collect · The Feline Co. store"
    }
}
