package com.thefelineco.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thefelineco.ui.components.AssetImage
import com.thefelineco.ui.components.FelineLogo
import com.thefelineco.ui.components.PawPattern
import com.thefelineco.ui.theme.Mist
import com.thefelineco.ui.theme.Onyx
import com.thefelineco.ui.theme.Silver

/**
 * Shared frame for Login and Register: a photo brand panel beside the form on tablets, or a short
 * banner above it on phones.
 */
@Composable
fun AuthLayout(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp) {
            Row(Modifier.fillMaxSize()) {
                BrandPanel(Modifier.weight(1.1f).fillMaxHeight(), large = true)
                FormColumn(title, subtitle, Modifier.weight(1f).fillMaxHeight(), content)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                BrandPanel(Modifier.fillMaxWidth().height(if (maxHeight > 700.dp) 260.dp else 180.dp), large = false)
                FormColumn(title, subtitle, Modifier.weight(1f), content)
            }
        }
    }
}

@Composable
private fun BrandPanel(modifier: Modifier, large: Boolean) {
    Box(modifier.background(Onyx)) {
        AssetImage("login_background", contentDescription = null, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Onyx.copy(alpha = 0.45f), 0.5f to Color.Transparent, 1f to Onyx.copy(alpha = 0.95f))
            )
        )
        PawPattern(Modifier.fillMaxSize(), color = Color.White, alpha = 0.06f, count = if (large) 18 else 8)
        Column(
            Modifier.align(Alignment.BottomStart).systemBarsPadding().padding(if (large) 48.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FelineLogo(markSize = if (large) 64.dp else 44.dp, textColor = Mist)
            if (large) {
                Text("Every cat deserves a crown.", style = MaterialTheme.typography.displaySmall, color = Mist)
                Text(
                    "Adopt a companion, spoil them with the very best, and earn rewards along the way.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Silver,
                    modifier = Modifier.widthIn(max = 460.dp),
                )
            }
        }
    }
}

@Composable
private fun FormColumn(
    title: String,
    subtitle: String,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        PawPattern(Modifier.fillMaxSize(), count = 10, seed = 21)
        // Wraps its content height, so the Box centres it when short and it scrolls when tall
        // (for example, with the keyboard open).
        Column(
            Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(Modifier.height(8.dp))
                content()
            }
        }
    }
}
