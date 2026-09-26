package com.thefelineco.ui.adopt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefelineco.domain.CatQuery
import com.thefelineco.domain.model.AgeGroup
import com.thefelineco.domain.model.CoatLength
import com.thefelineco.domain.model.Sex

/**
 * All cat filters, grouped. It sits in a side panel on large tablets and in a bottom sheet on
 * smaller screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatFilterPanel(query: CatQuery, onEvent: (AdoptEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Filters", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            if (query.activeFilterCount > 0) {
                TextButton(onClick = { onEvent(AdoptEvent.ClearFilters) }) { Text("Clear all") }
            }
        }
        FilterGroup("Adoption fee") {
            FelineFilterChip("Free to adopt (2 yrs+)", query.freeOnly) { onEvent(AdoptEvent.ToggleFreeOnly) }
        }
        FilterGroup("Age") {
            AgeGroup.entries.forEach { group ->
                FelineFilterChip("${group.label} · ${group.rangeLabel}", group in query.ageGroups) {
                    onEvent(AdoptEvent.ToggleAgeGroup(group))
                }
            }
        }
        FilterGroup("Sex") {
            Sex.entries.forEach { sex ->
                FelineFilterChip(sex.label, sex in query.sexes) { onEvent(AdoptEvent.ToggleSex(sex)) }
            }
        }
        FilterGroup("Coat") {
            CoatLength.entries.forEach { coat ->
                FelineFilterChip(coat.label, coat in query.coats) { onEvent(AdoptEvent.ToggleCoat(coat)) }
            }
        }
        FilterGroup("Good with") {
            FelineFilterChip("Children", query.goodWithKids) { onEvent(AdoptEvent.ToggleKids) }
            FelineFilterChip("Other cats", query.goodWithCats) { onEvent(AdoptEvent.ToggleCats) }
            FelineFilterChip("Dogs", query.goodWithDogs) { onEvent(AdoptEvent.ToggleDogs) }
        }
    }
}

/** Plural form for quick-filter chips: "Kittens", "Young cats"… */
val AgeGroup.pluralLabel: String
    get() = when (this) {
        AgeGroup.KITTEN -> "Kittens"
        AgeGroup.YOUNG -> "Young cats"
        AgeGroup.ADULT -> "Adults"
        AgeGroup.SENIOR -> "Seniors"
    }

/** Short age range for chips, e.g. "under 6 mo", "8 yrs+". */
val AgeGroup.rangeLabel: String
    get() = when (this) {
        AgeGroup.KITTEN -> "under 6 mo"
        AgeGroup.YOUNG -> "6 mo–2 yrs"
        AgeGroup.ADULT -> "2–8 yrs"
        AgeGroup.SENIOR -> "8 yrs+"
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(title: String, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            chips()
        }
    }
}

/** Filter chip in brand colours, with a tick when selected. */
@Composable
fun FelineFilterChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier,
    )
}
