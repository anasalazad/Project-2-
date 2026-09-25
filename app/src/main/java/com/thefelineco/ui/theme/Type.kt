package com.thefelineco.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Typography: an elegant serif for headlines and a soft rounded sans for everything else.
 *
 * The brand fonts (see ASSETS.md) are optional. They are looked up by name so the app still builds
 * without them and falls back to the system serif and sans-serif.
 */
@Composable
fun rememberFelineTypography(): Typography {
    val context = LocalContext.current
    return remember { felineTypography(context) }
}

private fun felineTypography(context: Context): Typography {
    val display = context.fontFamilyOf("playfair_display_bold" to FontWeight.Bold) ?: FontFamily.Serif
    val body = context.fontFamilyOf(
        "nunito_regular" to FontWeight.Normal,
        "nunito_bold" to FontWeight.Bold,
    ) ?: FontFamily.SansSerif

    fun displayStyle(size: Int, line: Int, spacing: Double = 0.0) = TextStyle(
        fontFamily = display, fontWeight = FontWeight.Bold, fontSize = size.sp, lineHeight = line.sp,
        letterSpacing = spacing.em,
    )

    fun bodyStyle(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, spacing: Double = 0.0) = TextStyle(
        fontFamily = body, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = spacing.em,
    )

    return Typography(
        displayLarge = displayStyle(57, 64, -0.01),
        displayMedium = displayStyle(45, 52),
        displaySmall = displayStyle(36, 44),
        headlineLarge = displayStyle(32, 40),
        headlineMedium = displayStyle(28, 36),
        headlineSmall = displayStyle(24, 32),
        titleLarge = displayStyle(22, 28),
        titleMedium = bodyStyle(16, 24, FontWeight.Bold, 0.01),
        titleSmall = bodyStyle(14, 20, FontWeight.Bold, 0.01),
        bodyLarge = bodyStyle(16, 24),
        bodyMedium = bodyStyle(14, 20),
        bodySmall = bodyStyle(12, 16),
        labelLarge = bodyStyle(14, 20, FontWeight.Bold, 0.02),
        labelMedium = bodyStyle(12, 16, FontWeight.Bold, 0.04),
        labelSmall = bodyStyle(11, 16, FontWeight.Bold, 0.05),
    )
}

/** Builds a family from the fonts in res/font that exist, or returns null if none of them do. */
@SuppressLint("DiscouragedApi") // Lookup by name keeps the fonts optional.
private fun Context.fontFamilyOf(vararg fonts: Pair<String, FontWeight>): FontFamily? {
    val available = fonts.mapNotNull { (name, weight) ->
        resources.getIdentifier(name, "font", packageName).takeIf { it != 0 }?.let { Font(it, weight) }
    }
    return if (available.isEmpty()) null else FontFamily(available)
}
