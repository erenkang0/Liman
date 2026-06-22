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
import com.liman.app.data.model.EntryKind
import com.liman.app.data.model.Mention
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.FullscreenPhotoViewer
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.PhotoThumbStrip
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.VoiceNotePlayer
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.link.MentionText
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
    onOpenMention: (Mention) -> Unit = {},
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val entry = contact?.entries?.firstOrNull { it.id == noteId }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    val isSession = entry?.kind == EntryKind.SESSION

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isSession) "Seans notu" else entry?.title?.ifBlank { "Not" } ?: "Not") },
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
                Text("Kayıt bulunamadı.")
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
                Column(Modifier.weight(1f)) {
                    Text(entry.date.format(noteDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    entry.durationMin?.let { Text("Süre: $it dk", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                entry.feeling?.let {
                    Icon(moodIcon(it), contentDescription = it.label, tint = LocalLimanColors.current.moodColor(it.score), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(it.label, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (!isSession && entry.title.isNotBlank()) {
                Text(entry.title, style = MaterialTheme.typography.headlineSmall)
            }

            entry.voice?.let { voice ->
                VoiceNotePlayer(voice.path, voice.durationMs, Modifier.fillMaxWidth())
            }

            if (isSession) {
                SoapBlock("S — Danışanın aktardıkları", entry.subjective, entry.mentions, onOpenMention)
                SoapBlock("O — Gözlemler", entry.objective, entry.mentions, onOpenMention)
                SoapBlock("A — Değerlendirme", entry.assessment, entry.mentions, onOpenMention)
                SoapBlock("P — Plan / ödev", entry.plan, entry.mentions, onOpenMention)
            } else if (entry.text.isNotBlank()) {
                MentionText(entry.text, entry.mentions, onOpenMention, style = JournalBodyStyle)
            }

            if (entry.photos.size > 1) {
                SectionHeader("Belgeler", subtitle = "${entry.photos.size}")
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

@Composable
private fun SoapBlock(label: String, content: String, mentions: List<Mention>, onOpenMention: (Mention) -> Unit) {
    if (content.isBlank()) return
    LimanCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            MentionText(content, mentions, onOpenMention, style = JournalBodyStyle)
        }
    }
}
