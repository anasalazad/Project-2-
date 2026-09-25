package com.thefelineco.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thefelineco.ui.components.EmptyState
import com.thefelineco.ui.components.PawPattern

/** Temporary screen for destinations that are built in a later phase (see CONTEXT.md §9). */
@Composable
fun ComingSoonScreen(
    title: String,
    message: String = "This part of The Feline Co. is on its way.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
        PawPattern(Modifier.fillMaxSize())
        EmptyState(
            title = title,
            message = message,
            icon = Icons.Filled.Construction,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }
}
