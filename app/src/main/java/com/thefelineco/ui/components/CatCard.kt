package com.thefelineco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thefelineco.domain.model.AdoptionStatus
import com.thefelineco.domain.model.Cat
import com.thefelineco.domain.model.Sex
import com.thefelineco.ui.theme.Mist
import com.thefelineco.ui.theme.Onyx
import com.thefelineco.ui.theme.Silver

/**
 * Portrait photo card for a cat: fee badge in the corner, name and details over a dark gradient.
 * Used on Home (carousel) and Adopt (grid).
 */
@Composable
fun CatCard(cat: Cat, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sexLabel = cat.sex.label
    val feeLabel = if (cat.isFree) "free to adopt" else "${cat.adoptionFee} credits"
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "${cat.name}, ${cat.breed}, ${cat.ageLabel}, $sexLabel, $feeLabel"
        },
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.78f)) {
            AssetImage(
                name = cat.imageName,
                contentDescription = null,
                placeholderLabel = null,
                modifier = Modifier.fillMaxSize(),
            )
            // Dark gradient so white text is readable on any photo.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Onyx.copy(alpha = 0.92f),
                        )
                    )
            )
            FeeBadge(cat.adoptionFee, Modifier.align(Alignment.TopStart).padding(12.dp))
            if (cat.status != AdoptionStatus.AVAILABLE) {
                AdoptionStatusChip(cat.status, Modifier.align(Alignment.TopEnd).padding(12.dp))
            }
            Column(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Mist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(
                        if (cat.sex == Sex.MALE) Icons.Filled.Male else Icons.Filled.Female,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    "${cat.breed} · ${cat.ageLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Silver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
