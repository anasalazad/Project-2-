package com.thefelineco.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Widest a content column grows on big tablets, so lines of text stay readable. */
val MaxContentWidth = 1280.dp

/** True on tablets and large phones in landscape (600dp+ wide). */
@Composable
@ReadOnlyComposable
fun isWideLayout(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** True on large tablets (840dp+ wide), where two-pane layouts are used. */
@Composable
@ReadOnlyComposable
fun isExpandedLayout(): Boolean = LocalConfiguration.current.screenWidthDp >= 840

/** Centres [content] horizontally and caps its width at [maxWidth]. */
@Composable
fun CenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = MaxContentWidth,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/** App-wide snackbar host, so any screen (or an activity result) can show a message. */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}
