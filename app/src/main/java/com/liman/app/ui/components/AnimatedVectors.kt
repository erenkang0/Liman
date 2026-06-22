package com.liman.app.ui.components

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.liman.app.R

/**
 * İlk göründüğünde nazikçe "açan" (bloom) animasyonlu vektör. AnimatedVectorDrawable
 * (avd_bloom) Compose'da oynatılır.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun BloomIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    val image = AnimatedImageVector.animatedVectorResource(R.drawable.avd_bloom)
    var atEnd by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { atEnd = true }
    Image(
        painter = rememberAnimatedVectorPainter(image, atEnd),
        contentDescription = null,
        modifier = modifier,
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint),
    )
}
