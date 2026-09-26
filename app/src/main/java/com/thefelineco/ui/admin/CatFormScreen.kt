package com.thefelineco.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex
import com.thefelineco.ui.adopt.FelineFilterChip
import com.thefelineco.ui.auth.FormErrorBanner
import com.thefelineco.ui.common.CenteredContent
import com.thefelineco.ui.components.ConfirmDialog
import com.thefelineco.ui.components.FeeBadge
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.Pill
import com.thefelineco.ui.components.ValidatedTextField

/** Add or edit a cat listing. [onDone] receives a message for the snackbar after saving or deleting. */
@Composable
fun CatFormScreen(
    onBack: () -> Unit,
    onDone: (String) -> Unit,
    viewModel: CatFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.outcome) {
        when (val outcome = state.outcome) {
            is FormOutcome.Saved -> onDone(outcome.message)
            is FormOutcome.Deleted -> onDone(outcome.message)
            null -> Unit
        }
    }
    if (state.isLoading) LoadingState() else CatForm(state, viewModel, onBack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatForm(state: CatFormState, vm: CatFormViewModel, onBack: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val e = state.errors

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        FormTopBar(
            title = if (state.isNew) "Add a cat" else "Edit ${state.name.ifBlank { "cat" }}",
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
                            onValueChange = { v -> vm.update(CatField.IMAGE) { it.copy(imageName = v) } },
                            error = e[CatField.IMAGE],
                            placeholderLabel = state.name,
                        )
                    }
                    FormCard("Basics") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedTextField(state.name, { v -> vm.update(CatField.NAME) { it.copy(name = v) } }, "Name", e[CatField.NAME], Modifier.weight(1f))
                            ValidatedTextField(state.breed, { v -> vm.update(CatField.BREED) { it.copy(breed = v) } }, "Breed", e[CatField.BREED], Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ValidatedTextField(
                                state.ageYears, { v -> vm.update(CatField.AGE_YEARS) { it.copy(ageYears = v.filter(Char::isDigit).take(2)) } },
                                "Age (years)", e[CatField.AGE_YEARS], Modifier.weight(1f), keyboardType = KeyboardType.Number,
                            )
                            ValidatedTextField(
                                state.ageMonths, { v -> vm.update(CatField.AGE_MONTHS) { it.copy(ageMonths = v.filter(Char::isDigit).take(2)) } },
                                "+ months", e[CatField.AGE_MONTHS], Modifier.weight(1f), keyboardType = KeyboardType.Number,
                            )
                            ValidatedTextField(
                                state.weight, { v -> vm.update(CatField.WEIGHT) { it.copy(weight = v) } },
                                "Weight (kg)", e[CatField.WEIGHT], Modifier.weight(1f), keyboardType = KeyboardType.Decimal,
                            )
                        }
                        state.feePreview?.let { fee ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Adoption fee for this age:", style = MaterialTheme.typography.bodyMedium)
                                FeeBadge(fee)
                            }
                        }
                        ValidatedTextField(state.colour, { v -> vm.update(CatField.COLOUR) { it.copy(colour = v) } }, "Colour / markings", e[CatField.COLOUR])
                        ChipGroup("Sex") {
                            Sex.entries.forEach { sex -> FelineFilterChip(sex.label, state.sex == sex) { vm.update { it.copy(sex = sex) } } }
                        }
                        ChipGroup("Coat") {
                            CoatLength.entries.forEach { coat -> FelineFilterChip(coat.label, state.coat == coat) { vm.update { it.copy(coat = coat) } } }
                        }
                    }
                    FormCard("Personality & story") {
                        ValidatedTextField(
                            state.personality, { v -> vm.update(CatField.PERSONALITY) { it.copy(personality = v) } },
                            "Personality traits", e[CatField.PERSONALITY], helper = "Separate with commas, e.g. Cuddly, Playful",
                        )
                        if (state.traits.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.traits.forEach { trait ->
                                    Pill(trait, MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface, showPaw = true)
                                }
                            }
                        }
                        ValidatedTextField(
                            state.description, { v -> vm.update(CatField.DESCRIPTION) { it.copy(description = v) } },
                            "About this cat", e[CatField.DESCRIPTION], singleLine = false, minLines = 4,
                            helper = "${state.description.trim().length} characters",
                        )
                    }
                    FormCard("Care & compatibility") {
                        ChipGroup("Good with") {
                            FelineFilterChip("Children", state.goodWithKids) { vm.update { it.copy(goodWithKids = !it.goodWithKids) } }
                            FelineFilterChip("Other cats", state.goodWithCats) { vm.update { it.copy(goodWithCats = !it.goodWithCats) } }
                            FelineFilterChip("Dogs", state.goodWithDogs) { vm.update { it.copy(goodWithDogs = !it.goodWithDogs) } }
                        }
                        ChipGroup("Health & home") {
                            FelineFilterChip("Vaccinated", state.vaccinated) { vm.update { it.copy(vaccinated = !it.vaccinated) } }
                            FelineFilterChip("Desexed", state.desexed) { vm.update { it.copy(desexed = !it.desexed) } }
                            FelineFilterChip("Microchipped", state.microchipped) { vm.update { it.copy(microchipped = !it.microchipped) } }
                            FelineFilterChip("Indoor only", state.indoorOnly) { vm.update { it.copy(indoorOnly = !it.indoorOnly) } }
                        }
                        ValidatedTextField(
                            state.specialNeeds, { v -> vm.update { it.copy(specialNeeds = v) } },
                            "Special care (optional)", null, helper = "e.g. Daily brushing, kidney-friendly diet",
                        )
                    }
                    FormCard("Listing status") {
                        if (state.status == AdoptionStatus.PENDING) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Adoption pending: this is managed from Appointments (confirm, decline or complete the booking).",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        } else {
                            ChipGroup("Status") {
                                // Pending is only ever set by a booking, so it isn't offered here.
                                listOf(AdoptionStatus.AVAILABLE, AdoptionStatus.ADOPTED).forEach { status ->
                                    FelineFilterChip(status.label, state.status == status) { vm.update { it.copy(status = status) } }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(visible = state.formError != null) { FormErrorBanner(state.formError.orEmpty()) }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Remove ${state.name}?",
            message = "This listing will be deleted permanently. Past bookings stay in the history.",
            confirmLabel = "Delete listing",
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

/** Back button, title, optional delete and a Save button. Shared by the cat and product forms. */
@Composable
fun FormTopBar(title: String, onBack: () -> Unit, onDelete: (() -> Unit)?, onSave: () -> Unit, saving: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f).padding(start = 8.dp))
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
        Button(onClick = onSave, enabled = !saving, modifier = Modifier.padding(start = 8.dp, end = 12.dp)) {
            Text(if (saving) "Saving…" else "Save")
        }
    }
}

/** A titled card grouping related form fields. */
@Composable
fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(label: String, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { chips() }
    }
}
