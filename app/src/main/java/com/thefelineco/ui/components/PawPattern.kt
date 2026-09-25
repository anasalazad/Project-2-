package com.thefelineco.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * A faint scatter of paw prints used as a decorative background. The positions come from a fixed
 * seed, so they never jump around between recompositions.
 *
 * @param count number of paws. @param seed change for a different arrangement.
 */
@Composable
fun PawPattern(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    alpha: Float = 0.05f,
    count: Int = 14,
    pawSize: Dp = 34.dp,
    seed: Int = 7,
) {
    val painter = rememberVectorPainter(Icons.Filled.Pets)
    val sizePx = with(LocalDensity.current) { pawSize.toPx() }
    val paws = remember(count, seed) {
        val random = Random(seed)
        List(count) {
            Paw(
                x = random.nextFloat(),
                y = random.nextFloat(),
                scale = 0.6f + random.nextFloat() * 0.8f,
                angle = random.nextFloat() * 70f - 35f,
            )
        }
    }
    Canvas(modifier) {
        paws.forEach { paw ->
            val s = sizePx * paw.scale
            translate(left = paw.x * (size.width - s), top = paw.y * (size.height - s)) {
                rotate(paw.angle, pivot = Offset(s / 2, s / 2)) {
                    with(painter) { draw(Size(s, s), alpha = alpha, colorFilter = ColorFilter.tint(color)) }
                }
            }
        }
    }
}

private data class Paw(val x: Float, val y: Float, val scale: Float, val angle: Float)
