package com.thefelineco.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.BookingStatus
import com.thefelineco.ui.theme.FelineTheme

/** Small pill used for fees, statuses and counts. */
@Composable
fun Pill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    showPaw: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
) {
    Surface(modifier = modifier, shape = CircleShape, color = containerColor, contentColor = contentColor, border = border) {
        Row(
            Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (showPaw) Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Adoption fee: a solid crimson "FREE" badge, or the credit cost in an outlined pill. */
@Composable
fun FeeBadge(fee: Int, modifier: Modifier = Modifier) {
    if (fee == 0) {
        Pill(
            "FREE",
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier,
            showPaw = true,
        )
    } else {
        Pill(
            "$fee credits",
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = modifier,
            showPaw = true,
        )
    }
}

/** Colour-coded adoption status. */
@Composable
fun AdoptionStatusChip(status: AdoptionStatus, modifier: Modifier = Modifier) {
    val colors = FelineTheme.colors
    val (container, content) = when (status) {
        AdoptionStatus.AVAILABLE -> colors.successContainer to colors.onSuccessContainer
        AdoptionStatus.PENDING -> colors.warningContainer to colors.onWarningContainer
        AdoptionStatus.ADOPTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    Pill(status.label, container, content, modifier)
}

/** Colour-coded booking status. */
@Composable
fun BookingStatusChip(status: BookingStatus, modifier: Modifier = Modifier) {
    val colors = FelineTheme.colors
    val (container, content) = when (status) {
        BookingStatus.REQUESTED -> colors.warningContainer to colors.onWarningContainer
        BookingStatus.CONFIRMED -> colors.successContainer to colors.onSuccessContainer
        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BookingStatus.CANCELLED, BookingStatus.DECLINED ->
            MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Pill(status.label, container, content, modifier)
}

/** The user's credit balance, shown in headers. */
@Composable
fun CreditBalanceChip(credits: Int, modifier: Modifier = Modifier) {
    Pill(
        text = "$credits credits",
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier,
        showPaw = true,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    )
}
