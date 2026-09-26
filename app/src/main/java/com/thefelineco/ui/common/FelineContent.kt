package com.thefelineco.ui.common

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefelineco.FelineApplication
import com.thefelineco.ui.theme.FelineTheme

/**
 * Sets an activity's Compose content inside The Feline Co. theme, edge to edge.
 *
 * All three activities (Main, Booking, Checkout) use this, so they share one look and follow the
 * dark/light setting from the Account screen. System bar icons switch to match the theme.
 */
fun ComponentActivity.setFelineContent(content: @Composable () -> Unit) {
    enableEdgeToEdge()
    val settings = (application as FelineApplication).container.settingsStore
    setContent {
        val dark by settings.darkTheme.collectAsStateWithLifecycle(initialValue = true)
        DisposableEffect(dark) {
            val style = if (dark) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            onDispose { }
        }
        FelineTheme(darkTheme = dark) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}
