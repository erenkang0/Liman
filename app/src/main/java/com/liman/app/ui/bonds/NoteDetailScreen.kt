package com.liman.app.ui.bonds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.FullscreenPhotoViewer
import com.liman.app.ui.components.PhotoThumbStrip
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.VoiceNotePlayer
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.theme.JournalBodyStyle
import com.liman.app.ui.theme.LocalLimanColors
import java.time.format.DateTimeFormatter
import java.util.Locale

private val noteDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))

@Composable
fun NoteDetailScreen(
    viewModel: LimanViewModel,
    contactId: String,
    noteId: String,
    onBack: () -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val entry = contact?.entries?.firstOrNull { it.id == noteId }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(entry?.title?.ifBlank { "Not" } ?: "Not") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = {
                            viewModel.repository.deleteEntry(contactId, noteId)
                            onBack()
                        }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (entry == null) {
            Box(Modifier.padding(padding).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Not bulunamadı.")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            entry.coverPhoto?.let { cover ->
                AsyncImage(
                    model = cover,
                    contentDescription = "Kapak",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewerIndex = 0 },
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.date.format(noteDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                entry.feeling?.let {
                    Icon(
                        moodIcon(it),
                        contentDescription = it.label,
                        tint = LocalLimanColors.current.moodColor(it.score),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(it.label, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (entry.title.isNotBlank()) {
                Text(entry.title, style = MaterialTheme.typography.headlineSmall)
            }

            entry.voice?.let { voice ->
                VoiceNotePlayer(voice.path, voice.durationMs, Modifier.fillMaxWidth())
            }

            if (entry.text.isNotBlank()) {
                Text(entry.text, style = JournalBodyStyle)
            }

            if (entry.photos.size > 1) {
                SectionHeader("Fotoğraflar", subtitle = "${entry.photos.size}")
                PhotoThumbStrip(entry.photos, onClick = { viewerIndex = it })
            }
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
