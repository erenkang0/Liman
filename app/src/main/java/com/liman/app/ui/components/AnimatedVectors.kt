package com.liman.app.ui.components

import androidx.annotation.DrawableRes
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
 * Bir AnimatedVectorDrawable'ı ilk göründüğünde bir kez oynatan genel yardımcı.
 * [tint] verilirse vektör o renge boyanır.
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun AnimatedVector(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null,
) {
    val image = AnimatedImageVector.animatedVectorResource(resId)
    var atEnd by remember(resId) { mutableStateOf(false) }
    LaunchedEffect(resId) { atEnd = true }
    Image(
        painter = rememberAnimatedVectorPainter(image, atEnd),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint),
    )
}

/** İlk göründüğünde nazikçe "açan" (bloom) animasyonlu vektör — Liman dünyası. */
@Composable
fun BloomIcon(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) =
    AnimatedVector(R.drawable.avd_bloom, modifier, tint)

/** Yazı/kalem temalı animasyonlu vektör — Psikolog Defteri dünyası. */
@Composable
fun QuillIcon(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) =
    AnimatedVector(R.drawable.avd_quill, modifier, tint)
