package com.thefelineco.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thefelineco.ui.common.formatLong
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * A horizontal strip of day "pills" for picking an appointment date. Friendlier on a tablet than a
 * calendar dialog, because every option is visible and one tap away.
 *
 * @param isEnabled days that can't be booked (e.g. Mondays) are shown greyed out with [disabledLabel].
 */
@Composable
fun DateStrip(
    dates: List<LocalDate>,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    isEnabled: (LocalDate) -> Boolean,
    modifier: Modifier = Modifier,
    disabledLabel: String = "Closed",
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyRow(modifier, contentPadding = contentPadding, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(dates, key = { it.toEpochDay() }) { date ->
            val enabled = isEnabled(date)
            val isSelected = date == selected
            val colors = MaterialTheme.colorScheme
            Surface(
                onClick = { onSelect(date) },
                enabled = enabled,
                shape = MaterialTheme.shapes.large,
                color = when {
                    isSelected -> colors.primary
                    enabled -> colors.surfaceContainerHigh
                    else -> colors.surfaceContainerLowest
                },
                contentColor = when {
                    isSelected -> colors.onPrimary
                    enabled -> colors.onSurface
                    else -> colors.outline
                },
                border = if (isSelected) null else BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier
                    .width(76.dp)
                    .semantics {
                        this.selected = isSelected
                        contentDescription = date.formatLong() + if (enabled) "" else ", $disabledLabel"
                    },
            ) {
                Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(), style = MaterialTheme.typography.labelSmall)
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (enabled) date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) else disabledLabel,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
