package com.liman.app.ui.bonds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.liman.app.ui.theme.JournalBodyStyle
import java.time.format.DateTimeFormatter
import java.util.Locale

private val memoryDate: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))

@Composable
fun MemoryDetailScreen(
    viewModel: LimanViewModel,
    contactId: String,
    memoryId: String,
    onBack: () -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val memory = contact?.memories?.firstOrNull { it.id == memoryId }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(memory?.title ?: "Anı") },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (memory == null) {
            Box(Modifier.padding(padding).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Anı bulunamadı.")
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
            memory.coverPhoto?.let { cover ->
                AsyncImage(
                    model = cover,
                    contentDescription = "Anı afişi",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewerIndex = 0 },
                )
            }

            Text(
                memory.date.format(memoryDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(memory.title, style = MaterialTheme.typography.headlineSmall)

            memory.voice?.let { voice ->
                VoiceNotePlayer(voice.path, voice.durationMs, Modifier.fillMaxWidth())
            }

            if (memory.note.isNotBlank()) {
                Text(memory.note, style = JournalBodyStyle)
            }

            if (memory.photos.size > 1) {
                SectionHeader("Fotoğraflar", subtitle = "${memory.photos.size}")
                PhotoThumbStrip(memory.photos, onClick = { viewerIndex = it })
            }
        }
    }

    viewerIndex?.let { idx ->
        FullscreenPhotoViewer(
            photos = memory?.photos ?: emptyList(),
            startIndex = idx,
            onClose = { viewerIndex = null },
        )
    }
}
