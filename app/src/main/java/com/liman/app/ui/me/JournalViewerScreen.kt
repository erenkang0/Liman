package com.liman.app.ui.me

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.FullscreenPhotoViewer
import com.liman.app.ui.components.PhotoThumbStrip
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.VoiceNotePlayer
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.me.rich.buildJournalAnnotated
import com.liman.app.ui.me.rich.toFontFamily
import com.liman.app.ui.theme.JournalBodyStyle
import com.liman.app.ui.theme.LocalLimanColors
import java.time.format.DateTimeFormatter
import java.util.Locale

private val viewerDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy · HH:mm", Locale("tr"))
private const val HEADER_HEIGHT = 300

@Composable
fun JournalViewerScreen(
    viewModel: LimanViewModel,
    journalId: String,
    onBack: () -> Unit,
) {
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val entry = journals.firstOrNull { it.id == journalId }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (entry == null) {
            Text(
                "Yazı bulunamadı.",
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                // Afiş (kapak) — parallax + alta doğru fade; başlık fade içinde
                if (entry.coverPhoto != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(HEADER_HEIGHT.dp),
                    ) {
                        AsyncImage(
                            model = entry.coverPhoto,
                            contentDescription = "Afiş fotoğrafı",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { translationY = scroll.value * 0.5f }
                                .clickable { viewerIndex = 0 },
                        )
                        // Notlara doğru hafif fade
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    )
                                ),
                        )
                        // Başlık, fade alanında
                        Text(
                            entry.title.ifBlank { "Günlük" },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .graphicsLayer {
                                    alpha = (1f - scroll.value / (HEADER_HEIGHT * 1.2f)).coerceIn(0f, 1f)
                                },
                        )
                    }
                } else {
                    Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(64.dp))
                    Text(
                        entry.title.ifBlank { "Günlük" },
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.timestamp.format(viewerDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        entry.moodFace?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    moodIcon(it),
                                    contentDescription = it.label,
                                    tint = LocalLimanColors.current.moodColor(it.score),
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(it.label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    entry.voice?.let { voice ->
                        VoiceNotePlayer(voice.path, voice.durationMs, Modifier.fillMaxWidth())
                    }

                    if (entry.body.isNotBlank()) {
                        Text(
                            buildJournalAnnotated(entry.body, entry.spans),
                            style = JournalBodyStyle.copy(fontFamily = entry.font.toFontFamily()),
                        )
                    }

                    if (entry.photos.size > 1) {
                        SectionHeader("Fotoğraflar", subtitle = "${entry.photos.size}")
                        PhotoThumbStrip(entry.photos, onClick = { viewerIndex = it })
                    }
                }
            }
        }

        // Geri tuşu (afişin üstünde)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.32f)),
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Geri", tint = Color.White)
        }
    }

    viewerIndex?.let { idx ->
        FullscreenPhotoViewer(
            photos = entry?.photos ?: emptyList(),
            startIndex = idx,
            onClose = { viewerIndex = null },
        )
    }
}
