package com.thefelineco.ui.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thefelineco.domain.model.DeliveryMethod
import com.thefelineco.ui.auth.FormErrorBanner
import com.thefelineco.ui.common.isExpandedLayout
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.FelineLogo
import com.thefelineco.ui.components.ValidatedTextField

/** The checkout form, shown inside CheckoutActivity. Stateless: state in, events out. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(state: CheckoutUiState, onEvent: (CheckoutEvent) -> Unit, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { FelineLogo(markSize = 32.dp) },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Back to basket") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = { PlaceOrderBar(state, onSubmit = { onEvent(CheckoutEvent.Submit) }) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (isExpandedLayout()) {
            Row(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(
                    Modifier.weight(1.2f).fillMaxHeight().verticalScroll(rememberScrollState()).imePadding().padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) { DeliveryForm(state, onEvent) }
                Column(Modifier.weight(0.8f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(vertical = 16.dp)) {
                    OrderSummaryCard(state)
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OrderSummaryCard(state)
                DeliveryForm(state, onEvent)
            }
        }
    }
}

@Composable
private fun DeliveryForm(state: CheckoutUiState, onEvent: (CheckoutEvent) -> Unit) {
    Column(Modifier.widthIn(max = 720.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Checkout", style = MaterialTheme.typography.headlineLarge)

        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Delivery method", style = MaterialTheme.typography.titleLarge)
            DeliveryMethod.entries.forEach { method ->
                DeliveryOption(method, selected = method == state.deliveryMethod) {
                    onEvent(CheckoutEvent.MethodSelected(method))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (state.needsAddress) "Deliver to" else "Collecting", style = MaterialTheme.typography.titleLarge)
            ValidatedTextField(
                value = state.fullName,
                onValueChange = { onEvent(CheckoutEvent.NameChanged(it)) },
                label = "Full name",
                error = state.errors[CheckoutField.NAME],
                leadingIcon = Icons.Filled.Person,
            )
            // Address fields only appear when they're needed.
            AnimatedVisibility(visible = state.needsAddress) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ValidatedTextField(
                        value = state.address,
                        onValueChange = { onEvent(CheckoutEvent.AddressChanged(it)) },
                        label = "Street address",
                        error = state.errors[CheckoutField.ADDRESS],
                        leadingIcon = Icons.Filled.Home,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValidatedTextField(
                            value = state.suburb,
                            onValueChange = { onEvent(CheckoutEvent.SuburbChanged(it)) },
                            label = "Suburb",
                            error = state.errors[CheckoutField.SUBURB],
                            leadingIcon = Icons.Filled.LocationCity,
                            modifier = Modifier.weight(1.4f),
                        )
                        ValidatedTextField(
                            value = state.postcode,
                            onValueChange = { onEvent(CheckoutEvent.PostcodeChanged(it)) },
                            label = "Postcode",
                            error = state.errors[CheckoutField.POSTCODE],
                            leadingIcon = Icons.Filled.LocalPostOffice,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (!state.needsAddress) {
                Text(
                    "Collect from The Feline Co. store any day except Monday. Bring your order number.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedVisibility(visible = state.formError != null) {
            FormErrorBanner(state.formError.orEmpty())
        }
    }
}

@Composable
private fun DeliveryOption(method: DeliveryMethod, selected: Boolean, onSelect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Text(method.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 8.dp))
            Text(
                if (method.extraCredits == 0) "Free" else "+${method.extraCredits} credits",
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun OrderSummaryCard(state: CheckoutUiState) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Your order", style = MaterialTheme.typography.titleLarge)
            state.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssetImage(item.imageName, null, Modifier.size(52.dp).clip(MaterialTheme.shapes.small))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall)
                        Text("${item.quantity} × ${item.unitPrice} credits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${item.lineTotal}", style = MaterialTheme.typography.titleMedium)
                }
            }
            HorizontalDivider()
            Line("Subtotal", "${state.subtotal} credits")
            Line("Delivery", if (state.deliveryMethod.extraCredits == 0) "Free" else "${state.deliveryMethod.extraCredits} credits")
            Line("Total", "${state.total} credits", strong = true)
            HorizontalDivider()
            Line("Your balance", "${state.credits} credits")
            Line("Balance after", "${state.balanceAfter} credits", warn = !state.canAfford)
        }
    }
}

@Composable
private fun Line(label: String, value: String, strong: Boolean = false, warn: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (strong) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (strong) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PlaceOrderBar(state: CheckoutUiState, onSubmit: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total ${state.total} credits", style = MaterialTheme.typography.titleMedium)
                Text(state.deliveryMethod.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onSubmit,
                enabled = !state.isSubmitting,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                modifier = Modifier.heightIn(min = 52.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Place order", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
