package com.liman.app.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/* --------------------------- Sesli not oynatıcı --------------------------- */

@Composable
fun VoiceNotePlayer(
    path: String,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val player = remember { MediaPlayer() }
    var prepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }

    DisposableEffect(path) {
        runCatching {
            player.reset()
            player.setDataSource(path)
            player.prepare()
            prepared = true
        }
        player.setOnCompletionListener {
            isPlaying = false
            positionMs = 0L
            runCatching { player.seekTo(0) }
        }
        onDispose { runCatching { player.release() } }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            positionMs = runCatching { player.currentPosition.toLong() }.getOrDefault(positionMs)
            delay(120)
        }
    }

    val total = (if (durationMs > 0) durationMs else runCatching { player.duration.toLong() }.getOrDefault(1L))
        .coerceAtLeast(1L)
    val progress = (positionMs.toFloat() / total).coerceIn(0f, 1f)

    LimanCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .bounceClick {
                        if (!prepared) return@bounceClick
                        if (isPlaying) {
                            runCatching { player.pause() }
                            isPlaying = false
                        } else {
                            runCatching { player.start() }
                            isPlaying = true
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.GraphicEq, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sesli not", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(8.dp))
                // Zaman çizgisi
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.20f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${formatMs(positionMs)} / ${formatMs(total)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}

/* ------------------------------ Fotoğraf şeridi --------------------------- */

@Composable
fun PhotoThumbStrip(
    photos: List<String>,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(photos) { index, uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Fotoğraf ${index + 1}",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .bounceClick { onClick(index) },
            )
        }
    }
}

/* --------------------------- Tam ekran fotoğraf --------------------------- */

@Composable
fun FullscreenPhotoViewer(
    photos: List<String>,
    startIndex: Int,
    onClose: () -> Unit,
) {
    if (photos.isEmpty()) return
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f)),
        ) {
            val pagerState = rememberPagerState(
                initialPage = startIndex.coerceIn(0, photos.lastIndex),
                pageCount = { photos.size },
            )
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember(page) { mutableFloatStateOf(1f) }
                var offsetX by remember(page) { mutableFloatStateOf(0f) }
                var offsetY by remember(page) { mutableFloatStateOf(0f) }
                val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 4f)
                    if (scale > 1f) {
                        offsetX += panChange.x
                        offsetY += panChange.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = photos[page],
                        contentDescription = "Fotoğraf ${page + 1}",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                            .transformable(transformState),
                    )
                }
            }

            // Sayfa göstergesi
            if (photos.size > 1) {
                Text(
                    "${pagerState.currentPage + 1} / ${photos.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp),
                )
            }

            // Kapat tuşu
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Kapat", tint = Color.White)
            }
        }
    }
}

/** Afiş/kapak fotoğrafı (oran korumalı). */
@Composable
fun CoverPhoto(
    uri: String,
    modifier: Modifier = Modifier,
    aspect: Float = 16f / 10f,
) {
    AsyncImage(
        model = uri,
        contentDescription = "Kapak fotoğrafı",
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect),
    )
}
