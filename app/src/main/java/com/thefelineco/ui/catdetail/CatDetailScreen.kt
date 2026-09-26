package com.thefelineco.ui.catdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.CreditRules
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.Sex
import com.thefelineco.ui.common.isExpandedLayout
import com.thefelineco.ui.common.listedAgo
import com.thefelineco.ui.components.AdoptionStatusChip
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.CatCard
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.FeeBadge
import com.thefelineco.ui.components.LoadingState
import com.thefelineco.ui.components.Pill
import com.thefelineco.ui.components.SectionHeader
import com.thefelineco.ui.theme.FelineTheme
import com.thefelineco.ui.theme.Onyx

/**
 * A cat's full profile. "Book a meet & greet" hands the [Cat] to MainActivity, which launches
 * BookingActivity with it (Intent + Parcelable).
 */
@Composable
fun CatDetailScreen(
    onBack: () -> Unit,
    onBook: (Cat) -> Unit,
    onOpenCat: (Long) -> Unit,
    viewModel: CatDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cat = state.cat
    when {
        state.isLoading -> LoadingState()
        cat == null -> Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "Cat not found",
                message = "This listing may have been removed.",
                actionLabel = "Back",
                onAction = onBack,
            )
        }
        else -> CatDetailContent(state, cat, onBack, onBook, onOpenCat)
    }
}

@Composable
fun CatDetailContent(
    state: CatDetailUiState,
    cat: Cat,
    onBack: () -> Unit,
    onBook: (Cat) -> Unit,
    onOpenCat: (Long) -> Unit,
) {
    if (isExpandedLayout()) {
        // Large tablet: photo on the left, details scroll on the right.
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxHeight().statusBarsPadding().padding(24.dp)) {
                AssetImage(
                    cat.imageName,
                    contentDescription = "Photo of ${cat.name}",
                    placeholderLabel = cat.name,
                    modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge),
                )
                BackButton(onBack, Modifier.padding(16.dp))
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(top = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                CatDetails(state, cat, onBook, onOpenCat)
            }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(420.dp)) {
                AssetImage(
                    cat.imageName,
                    contentDescription = "Photo of ${cat.name}",
                    placeholderLabel = cat.name,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0f to Onyx.copy(alpha = 0.5f), 0.3f to Color.Transparent)
                    )
                )
                BackButton(onBack, Modifier.statusBarsPadding().padding(16.dp))
            }
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                CatDetails(state, cat, onBook, onOpenCat)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(
        onClick = onBack,
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Onyx.copy(alpha = 0.6f), contentColor = Color.White),
        modifier = modifier.size(48.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatDetails(state: CatDetailUiState, cat: Cat, onBook: (Cat) -> Unit, onOpenCat: (Long) -> Unit) {
    // Name, breed and status
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(cat.name, style = MaterialTheme.typography.displaySmall)
            Icon(
                if (cat.sex == Sex.MALE) Icons.Filled.Male else Icons.Filled.Female,
                contentDescription = cat.sex.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            "${cat.breed} · ${cat.ageLabel} · ${cat.sex.label}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FeeBadge(cat.adoptionFee)
            AdoptionStatusChip(cat.status)
            Text(cat.listedAt.listedAgo(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    AdoptionCard(state, cat, onBook)

    // Story and personality
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Meet ${cat.name}", style = MaterialTheme.typography.headlineSmall)
        Text(cat.description, style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cat.personality.forEach { trait ->
                Pill(
                    trait,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    showPaw = true,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }

    cat.specialNeeds?.let { needs ->
        Surface(
            color = FelineTheme.colors.warningContainer,
            contentColor = FelineTheme.colors.onWarningContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Healing, contentDescription = null)
                Column {
                    Text("Special care", style = MaterialTheme.typography.titleSmall)
                    Text(needs, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    // Facts, health and compatibility
    InfoSection("Quick facts") {
        FactTile(Icons.Filled.Palette, "Colour", cat.colour)
        FactTile(Icons.Filled.Pets, "Coat", cat.coat.label)
        FactTile(Icons.Filled.MonitorWeight, "Weight", "${cat.weightKg} kg")
        FactTile(Icons.Filled.Schedule, "Life stage", cat.ageGroup.label)
    }
    InfoSection("Health & care") {
        CheckTile(Icons.Filled.Vaccines, "Vaccinated", cat.vaccinated)
        CheckTile(Icons.Filled.Healing, "Desexed", cat.desexed)
        CheckTile(Icons.Filled.Memory, "Microchipped", cat.microchipped)
        CheckTile(Icons.Filled.Home, if (cat.indoorOnly) "Indoor only" else "Indoor / outdoor", true)
    }
    InfoSection("Good with") {
        CheckTile(Icons.Filled.ChildCare, "Children", cat.goodWithKids)
        CheckTile(Icons.Filled.Pets, "Other cats", cat.goodWithCats)
        CheckTile(Icons.Filled.Pets, "Dogs", cat.goodWithDogs)
    }

    if (state.similar.isNotEmpty()) {
        HorizontalDivider()
        SectionHeader(title = "You might also love", subtitle = "Other ${cat.ageGroup.label.lowercase()} cats waiting for a home")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.similar, key = { it.id }) { other ->
                CatCard(other, onClick = { onOpenCat(other.id) }, modifier = Modifier.width(170.dp))
            }
        }
    }
}

/** Fee, balance and the main call to action. Explains why booking is unavailable when it is. */
@Composable
private fun AdoptionCard(state: CatDetailUiState, cat: Cat, onBook: (Cat) -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("Adoption fee", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (cat.isFree) "Free" else "${cat.adoptionFee} credits",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        when {
                            cat.isFree -> "Cats aged 2 and over are free to adopt"
                            cat.adoptionFee == CreditRules.KITTEN_FEE -> "Kitten fee (under 6 months)"
                            else -> "Young cat fee (6 months – 2 years)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!state.isAdmin) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Your balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.credits} credits", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            val message: String? = when {
                state.isAdmin -> "You're signed in as an admin. Manage this listing from the Cats tab."
                cat.status == AdoptionStatus.PENDING -> "Someone has already booked to meet ${cat.name}. Check back soon!"
                cat.status == AdoptionStatus.ADOPTED -> "${cat.name} has found a forever home."
                !state.canAfford -> "You need ${state.creditsShort} more credits. Adopt a free older cat to earn " +
                    "${CreditRules.ADOPTION_REWARD} credits!"
                else -> null
            }
            if (message != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = { onBook(cat) },
                enabled = state.canBook,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Book a meet & greet", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "Complete the adoption to earn +${CreditRules.ADOPTION_REWARD} credits for the shop.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoSection(title: String, tiles: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tiles()
        }
    }
}

@Composable
private fun FactTile(icon: ImageVector, label: String, value: String) {
    Tile {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun CheckTile(icon: ImageVector, label: String, yes: Boolean) {
    val tint = if (yes) FelineTheme.colors.success else MaterialTheme.colorScheme.outline
    Tile {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.titleSmall)
        Icon(
            if (yes) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = if (yes) "Yes" else "No",
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun Tile(content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}
