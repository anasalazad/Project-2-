package com.thefelineco.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thefelineco.ui.theme.Crimson
import com.thefelineco.ui.theme.Onyx
import com.thefelineco.ui.theme.RubyDeep

/**
 * Shows a photo from res/drawable-nodpi by file name (without extension), e.g. "cat_mochi".
 *
 * Photos are added by hand (see ASSETS.md), so a missing file must not break the build. When the
 * file doesn't exist, a branded placeholder is shown instead. Coil loads the image and downsamples
 * it to the size on screen, which keeps large photos from using too much memory on the tablet.
 */
@Composable
fun AssetImage(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderLabel: String? = null,
) {
    val context = LocalContext.current
    val resId = remember(name) { context.drawableIdOrNull(name) }
    if (resId != null) {
        AsyncImage(
            model = resId,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        ImagePlaceholder(label = placeholderLabel, modifier = modifier)
    }
}

/** Crimson-to-onyx gradient with a paw. Used when a photo hasn't been added yet. */
@Composable
fun ImagePlaceholder(label: String?, modifier: Modifier = Modifier) {
    Box(
        modifier.background(Brush.linearGradient(listOf(Crimson, RubyDeep, Onyx))),
        contentAlignment = Alignment.Center,
    ) {
        PawPattern(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onPrimary, alpha = 0.08f, count = 6, seed = label?.length ?: 3)
        Icon(
            Icons.Filled.Pets,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxSize(0.28f),
        )
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp),
            )
        }
    }
}

/** Resource id of a drawable by name, or null if the file hasn't been added. */
@SuppressLint("DiscouragedApi") // Lookup by name lets photos be dropped in without code changes.
@DrawableRes
fun Context.drawableIdOrNull(name: String): Int? =
    if (name.isBlank()) null
    else resources.getIdentifier(name, "drawable", packageName).takeIf { it != 0 }
