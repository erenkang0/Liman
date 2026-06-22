package com.liman.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.liman.app.ui.navigation.World
import com.liman.app.ui.theme.LocalLimanColors
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Dünyaya göre değişen, içeriğin ALTINDA duran canlı arka plan.
 * - Liman: akşam/gece yıldızları + sakin deniz dalgaları (liman teması).
 * - Defter: koyu kırmızı/sepya parıltı + yukarı süzülen kıvılcım/toz.
 */
@Composable
fun WorldBackground(
    world: World,
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier,
) {
    when (world) {
        World.LIMAN -> LimanBackground(timeOfDay, modifier)
        World.DEFTER -> DefterBackground(modifier)
    }
}

@Composable
private fun LimanBackground(timeOfDay: TimeOfDay, modifier: Modifier) {
    if (timeOfDay.isStarry) {
        StarrySky(modifier = modifier, night = timeOfDay == TimeOfDay.NIGHT)
    }
    val waveColor = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "limanWaves")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // Alt kısımda nazik deniz dalgaları (düşük opaklık).
        val layers = listOf(
            Triple(0.86f, 22f, 0.10f),
            Triple(0.92f, 16f, 0.08f),
        )
        layers.forEachIndexed { i, (baseY, amp, alpha) ->
            val phase = drift * 2f * PI.toFloat() * (if (i == 0) 1f else -1f)
            val path = Path().apply {
                moveTo(0f, h)
                var x = 0f
                val step = w / 48f
                while (x <= w) {
                    val y = h * baseY + sin((x / w) * 4f * PI.toFloat() + phase) * amp
                    lineTo(x, y)
                    x += step
                }
                lineTo(w, h)
                close()
            }
            drawPath(path, waveColor.copy(alpha = alpha))
        }
    }
}

private data class Ember(val x: Float, val drift: Float, val size: Float, val phase: Float, val speed: Float)

@Composable
private fun DefterBackground(modifier: Modifier) {
    val glowTop = LocalLimanColors.current.nightSkyTop
    val ember = MaterialTheme.colorScheme.primary
    val sepia = LocalLimanColors.current.bondAccent

    val embers = remember {
        val rnd = Random(11)
        List(26) {
            Ember(
                x = rnd.nextFloat(),
                drift = (rnd.nextFloat() - 0.5f) * 0.12f,
                size = 1.0f + rnd.nextFloat() * 2.4f,
                phase = rnd.nextFloat(),
                speed = 0.6f + rnd.nextFloat() * 0.8f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "defterEmbers")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "rise",
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // Üstten aşağı koyu kırmızı/sepya parıltı.
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to glowTop.copy(alpha = 0.55f),
                    0.36f to ember.copy(alpha = 0.06f),
                    0.7f to Color.Transparent,
                ),
                startY = 0f,
                endY = h,
            ),
        )
        // Yukarı süzülen kıvılcımlar.
        embers.forEach { e ->
            val prog = (t * e.speed + e.phase) % 1f
            val y = h * (1f - prog)
            val x = (e.x + e.drift * prog) * w
            val twinkle = sin((t + e.phase) * 2f * PI.toFloat()) * 0.5f + 0.5f
            val alpha = (0.10f + 0.40f * twinkle) * (1f - prog)
            drawCircle(sepia.copy(alpha = alpha * 0.5f), radius = e.size * 2.4f, center = Offset(x, y))
            drawCircle(ember.copy(alpha = alpha), radius = e.size, center = Offset(x, y))
        }
    }
}
