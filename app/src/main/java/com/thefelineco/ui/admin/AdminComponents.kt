package com.thefelineco.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.ValidatedTextField
import com.thefelineco.ui.components.drawableIdOrNull
import com.thefelineco.ui.theme.FelineTheme

/** Page title row used by every admin screen, with optional content on the right. */
@Composable
fun AdminHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

/** Big number + label tile for the dashboard. */
@Composable
fun StatTile(value: Int, label: String, icon: ImageVector, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary) }
            }
            Column {
                Text("$value", style = MaterialTheme.typography.headlineLarge)
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Android resource names: lowercase letters, digits and underscores, starting with a letter. */
private val RESOURCE_NAME = Regex("^[a-z][a-z0-9_]*$")

/** Validates a drawable file name typed by an admin. Returns null when valid. */
fun validateImageName(name: String): String? = when {
    name.isBlank() -> "Image file name is required"
    !RESOURCE_NAME.matches(name.trim()) -> "Use lowercase letters, numbers and _ only (e.g. cat_mochi)"
    else -> null
}

/**
 * Image file-name field with a live preview. It tells the admin straight away whether a photo with
 * that name exists in res/drawable-nodpi, so typos are caught before saving.
 */
@Composable
fun ImageNameField(value: String, onValueChange: (String) -> Unit, error: String?, placeholderLabel: String) {
    val context = LocalContext.current
    val exists = remember(value) { value.isNotBlank() && context.drawableIdOrNull(value.trim()) != null }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        AssetImage(
            value.trim(),
            contentDescription = "Image preview",
            placeholderLabel = placeholderLabel,
            modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.medium),
        )
        Column(Modifier.weight(1f)) {
            ValidatedTextField(
                value = value,
                onValueChange = onValueChange,
                label = "Image file name",
                error = error,
                helper = if (exists) "Photo found" else "No photo with this name yet. A placeholder will show.",
            )
            if (exists && error == null) {
                Text(
                    "✓ Matches a file in drawable-nodpi",
                    style = MaterialTheme.typography.bodySmall,
                    color = FelineTheme.colors.success,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}
