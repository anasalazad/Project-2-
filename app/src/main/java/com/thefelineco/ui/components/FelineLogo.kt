package com.thefelineco.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.thefelineco.ui.theme.Crimson
import com.thefelineco.ui.theme.FelineTheme
import com.thefelineco.ui.theme.Mist
import com.thefelineco.ui.theme.Onyx
import com.thefelineco.ui.theme.Ruby

/**
 * The brand mark: a crimson cat head wearing a small crown, drawn on a Canvas so it stays sharp
 * at any size. It matches the launcher icon (res/drawable/ic_launcher_foreground.xml).
 */
@Composable
fun FelineMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    headColor: Color = Crimson,
    crownColor: Color = Mist,
    featureColor: Color = Onyx,
) {
    Canvas(modifier.size(size)) {
        // Designed on a 48×48 grid, then scaled to the requested size.
        val s = this.size.width / 48f
        fun p(x: Float, y: Float) = Offset(x * s, y * s)

        fun triangle(a: Offset, b: Offset, c: Offset) = Path().apply {
            moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close()
        }

        // Ears
        drawPath(triangle(p(8.5f, 26f), p(9.5f, 6f), p(21f, 16.5f)), headColor)
        drawPath(triangle(p(39.5f, 26f), p(38.5f, 6f), p(27f, 16.5f)), headColor)
        drawPath(triangle(p(11.5f, 21.5f), p(12.2f, 10.5f), p(18.5f, 16.5f)), Ruby)
        drawPath(triangle(p(36.5f, 21.5f), p(35.8f, 10.5f), p(29.5f, 16.5f)), Ruby)
        // Face
        drawOval(headColor, topLeft = p(6.5f, 14f), size = Size(35f * s, 32f * s))
        // Crown
        val crown = Path().apply {
            moveTo(18.5f * s, 16.5f * s); lineTo(18.5f * s, 8f * s); lineTo(21.8f * s, 11.5f * s)
            lineTo(24f * s, 4.5f * s); lineTo(26.2f * s, 11.5f * s); lineTo(29.5f * s, 8f * s)
            lineTo(29.5f * s, 16.5f * s); close()
        }
        drawPath(crown, crownColor)
        // Eyes and nose
        drawOval(featureColor, topLeft = p(14.8f, 25f), size = Size(5.4f * s, 7f * s))
        drawOval(featureColor, topLeft = p(27.8f, 25f), size = Size(5.4f * s, 7f * s))
        drawPath(triangle(p(21.5f, 34f), p(26.5f, 34f), p(24f, 37f)), featureColor)
        // Whiskers
        val whisker = 1.1f * s
        drawLine(crownColor, p(8f, 35f), p(0.5f, 33f), whisker, StrokeCap.Round)
        drawLine(crownColor, p(8f, 38.5f), p(1f, 39.5f), whisker, StrokeCap.Round)
        drawLine(crownColor, p(40f, 35f), p(47.5f, 33f), whisker, StrokeCap.Round)
        drawLine(crownColor, p(40f, 38.5f), p(47f, 39.5f), whisker, StrokeCap.Round)
    }
}

/** Mark + "The Feline Co." wordmark. [compact] shows it on one line for top bars and rails. */
@Composable
fun FelineLogo(
    modifier: Modifier = Modifier,
    markSize: Dp = 40.dp,
    compact: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val wordmark: @Composable () -> Unit = {
        Column {
            Text(
                "THE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.em),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Feline Co.",
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
    if (compact) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FelineMark(size = markSize)
            wordmark()
        }
    } else {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FelineMark(size = markSize)
            wordmark()
        }
    }
}

@Preview
@Composable
private fun FelineLogoPreview() {
    FelineTheme { FelineLogo() }
}
