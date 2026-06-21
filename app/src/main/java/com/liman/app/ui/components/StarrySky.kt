package com.liman.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalTime
import kotlin.math.sin
import kotlin.random.Random

/** Günün bölümü — arka plan ambiyansını saate göre değiştirmek için. */
enum class TimeOfDay { MORNING, DAY, EVENING, NIGHT }

fun currentTimeOfDay(hour: Int = LocalTime.now().hour): TimeOfDay = when (hour) {
    in 5..11 -> TimeOfDay.MORNING
    in 12..17 -> TimeOfDay.DAY
    in 18..21 -> TimeOfDay.EVENING
    else -> TimeOfDay.NIGHT
}

/** Akşam ve gece yıldızlı gökyüzü gösterilir. */
val TimeOfDay.isStarry: Boolean get() = this == TimeOfDay.EVENING || this == TimeOfDay.NIGHT

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val baseAlpha: Float,
)

/**
 * Akşamları/geceleri sayfanın üst kısmından aşağıya doğru solan, hafif koyu mavi
 * bir gökyüzü ve nazikçe parıldayan yıldızlar çizer. İçerik elemanlarının
 * ALTINDA durur (yarı saydam; alt yarıda tamamen kaybolur).
 */
@Composable
fun StarrySky(
    modifier: Modifier = Modifier,
    starCount: Int = 46,
    night: Boolean = false,
) {
    val colors = LocalLimanColors.current
    val stars = remember(starCount) {
        val rnd = Random(7)
        List(starCount) {
            Star(
                x = rnd.nextFloat(),
                y = rnd.nextFloat() * 0.52f, // yalnızca üst yarı
                radius = 1.1f + rnd.nextFloat() * 2.3f,
                phase = rnd.nextFloat(),
                baseAlpha = 0.45f + rnd.nextFloat() * 0.55f,
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "starrySky")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4600, easing = LinearEasing), RepeatMode.Restart),
        label = "twinkle",
    )

    val skyTop = colors.nightSkyTop
    val skyMid = colors.nightSkyMid
    val starColor = colors.starColor
    val topAlpha = if (night) 0.97f else 0.8f

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to skyTop.copy(alpha = topAlpha),
                    0.30f to skyMid.copy(alpha = topAlpha * 0.62f),
                    0.62f to Color.Transparent,
                ),
                startY = 0f,
                endY = h,
            ),
        )
        stars.forEach { s ->
            val t = sin((twinkle + s.phase) * 2f * Math.PI).toFloat() * 0.5f + 0.5f
            val alpha = (s.baseAlpha * (0.30f + 0.70f * t)).coerceIn(0f, 1f)
            val center = Offset(s.x * w, s.y * h)
            // yumuşak hâle + parlak çekirdek
            drawCircle(starColor.copy(alpha = alpha * 0.22f), radius = s.radius * 2.6f, center = center)
            drawCircle(starColor.copy(alpha = alpha), radius = s.radius, center = center)
        }
    }
}
