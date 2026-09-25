package com.thefelineco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = CrimsonBright,
    onPrimary = Color.White,
    primaryContainer = RubyDeep,
    onPrimaryContainer = Blush,
    secondary = Silver,
    onSecondary = Charcoal,
    secondaryContainer = CharcoalHighest,
    onSecondaryContainer = Mist,
    tertiary = Rose,
    onTertiary = Onyx,
    background = Onyx,
    onBackground = Mist,
    surface = Onyx,
    onSurface = Mist,
    surfaceVariant = CharcoalHigh,
    onSurfaceVariant = Silver,
    surfaceContainerLowest = OnyxDeep,
    surfaceContainerLow = Color(0xFF141417),
    surfaceContainer = Charcoal,
    surfaceContainerHigh = CharcoalHigh,
    surfaceContainerHighest = CharcoalHighest,
    surfaceTint = CrimsonBright,
    outline = Steel,
    outlineVariant = Graphite,
    error = Color(0xFFFF6B6B),
    onError = Onyx,
    errorContainer = Color(0xFF5C1111),
    onErrorContainer = Blush,
    inverseSurface = Mist,
    inverseOnSurface = Charcoal,
    inversePrimary = Crimson,
    scrim = Color.Black,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFB00D28),
    onPrimary = Color.White,
    primaryContainer = Blush,
    onPrimaryContainer = RubyDeep,
    secondary = Graphite,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E1E3),
    onSecondaryContainer = Charcoal,
    tertiary = Ruby,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Charcoal,
    surface = Paper,
    onSurface = Charcoal,
    surfaceVariant = Color(0xFFECE8E9),
    onSurfaceVariant = Color(0xFF55525A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF9F9),
    surfaceContainer = Color(0xFFF2EFF0),
    surfaceContainerHigh = Color(0xFFECE8E9),
    surfaceContainerHighest = Color(0xFFE5E1E2),
    surfaceTint = Crimson,
    outline = Color(0xFF8A8590),
    outlineVariant = Color(0xFFD2CDD0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    inverseSurface = Charcoal,
    inverseOnSurface = Mist,
    inversePrimary = Rose,
)

/** Brand colours that Material 3 has no slot for (status chips and so on). */
@Immutable
data class FelineExtendedColors(
    val success: Color,
    val onSuccessContainer: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val brandCrimson: Color,
    val heroScrim: Color,
)

private val DarkExtended = FelineExtendedColors(
    success = Jade,
    successContainer = Color(0xFF0F3322),
    onSuccessContainer = Color(0xFFA6F0C6),
    warning = Amber,
    warningContainer = Color(0xFF3D2A08),
    onWarningContainer = Color(0xFFFFDDA8),
    brandCrimson = Crimson,
    heroScrim = Onyx,
)

private val LightExtended = FelineExtendedColors(
    success = JadeDark,
    successContainer = Color(0xFFD5F5E3),
    onSuccessContainer = Color(0xFF0B3D24),
    warning = AmberDark,
    warningContainer = Color(0xFFFFEBC7),
    onWarningContainer = Color(0xFF4A2C00),
    brandCrimson = Crimson,
    heroScrim = Onyx,
)

val LocalFelineColors = staticCompositionLocalOf { DarkExtended }

/**
 * The Feline Co. theme. The brand is dark-first, so [darkTheme] defaults to true; the light
 * scheme is still complete for users who prefer it.
 */
@Composable
fun FelineTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFelineColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = rememberFelineTypography(),
            shapes = FelineShapes,
            content = content,
        )
    }
}

/** Shortcut: `FelineTheme.colors.success` and so on. */
object FelineTheme {
    val colors: FelineExtendedColors
        @Composable get() = LocalFelineColors.current
}
