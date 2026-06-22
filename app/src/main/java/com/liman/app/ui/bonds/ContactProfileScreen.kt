package com.liman.app.ui.bonds

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.NotebookEntry
import com.liman.app.data.model.RelationshipType
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.AnimatedEntrance
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.FullscreenPhotoViewer
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.components.QuillIcon
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.theme.LocalLimanColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val accentChoices = listOf(
    Color(0xFFC8323C), Color(0xFFD98C5A), Color(0xFFC8A36A),
    Color(0xFF4E8D6E), Color(0xFF0E7C99), Color(0xFF6A4FC4), Color(0xFFC24468),
)

@Composable
fun ContactProfileScreen(
    viewModel: LimanViewModel,
    contactId: String,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val context = LocalContext.current
    val today = LocalDate.now()

    var showNoteDialog by remember { mutableStateOf(false) }
    var showAlbumDialog by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    // Satır içi düzenleme (kişi ekranının içinde)
    var editing by remember { mutableStateOf(false) }
    var nameDraft by remember(contactId) { mutableStateOf("") }
    var titleDraft by remember(contactId) { mutableStateOf("") }
    var bioDraft by remember(contactId) { mutableStateOf("") }
    var relDraft by remember(contactId) { mutableStateOf(RelationshipType.FRIEND) }
    var closenessDraft by remember(contactId) { mutableStateOf(3) }
    var accentDraft by remember(contactId) { mutableStateOf<Color?>(null) }
    var birthdayDraft by remember(contactId) { mutableStateOf<LocalDate?>(null) }
    val tagDrafts = remember(contactId) { mutableListOf<String>().toMutableStateList() }
    var tagInput by remember(contactId) { mutableStateOf("") }
    var showEditDate by remember { mutableStateOf(false) }

    val albumPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5),
    ) { uris ->
        if (contact != null && uris.isNotEmpty()) {
            val merged = (contact.photos + uris.map { it.toString() }).distinct().take(5)
            viewModel.repository.upsertContact(contact.copy(photos = merged))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(contact?.name ?: "Kişi") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (contact != null) {
                        IconButton(onClick = {
                            if (editing) {
                                viewModel.repository.upsertContact(
                                    contact.copy(
                                        name = nameDraft.trim().ifBlank { contact.name },
                                        title = titleDraft.trim(),
                                        bio = bioDraft.trim(),
                                        relationship = relDraft,
                                        closeness = closenessDraft,
                                        accentColorArgb = accentDraft?.toArgb(),
                                        birthday = birthdayDraft,
                                        tags = tagDrafts.toList(),
                                    )
                                )
                                editing = false
                            } else {
                                nameDraft = contact.name
                                titleDraft = contact.title
                                bioDraft = contact.bio
                                relDraft = contact.relationship
                                closenessDraft = contact.closeness
                                accentDraft = contact.accentColorArgb?.let { Color(it) }
                                birthdayDraft = contact.birthday
                                tagDrafts.clear(); tagDrafts.addAll(contact.tags)
                                editing = true
                            }
                        }) {
                            Icon(
                                if (editing) Icons.Rounded.Done else Icons.Rounded.Edit,
                                contentDescription = if (editing) "Kaydet" else "Düzenle",
                                tint = if (editing) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        if (contact == null) {
            Box(Modifier.padding(padding).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Kişi bulunamadı.")
            }
            return@Scaffold
        }

        val accent = contact.accentColorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Kapak + avatar
            AnimatedEntrance {
                Box(Modifier.fillMaxWidth()) {
                    val coverPhoto = contact.coverPhoto
                    if (coverPhoto != null) {
                        AsyncImage(
                            model = coverPhoto,
                            contentDescription = "Kapak",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accent.copy(alpha = 0.65f), MaterialTheme.colorScheme.surfaceContainerHigh)
                                    )
                                ),
                        )
                    }
                    Box(Modifier.padding(start = 20.dp, top = 86.dp)) {
                        Box(
                            Modifier.size(92.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).padding(4.dp),
                        ) {
                            Avatar(contact, size = 84)
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedEntrance(delayMillis = 60) {
                    Crossfade(targetState = editing, label = "contactHeader") { edit ->
                        if (edit) {
                            EditFields(
                                name = nameDraft, onName = { nameDraft = it },
                                title = titleDraft, onTitle = { titleDraft = it },
                                bio = bioDraft, onBio = { bioDraft = it },
                                rel = relDraft, onRel = { relDraft = it },
                                closeness = closenessDraft, onCloseness = { closenessDraft = it },
                                accent = accentDraft, onAccent = { accentDraft = it },
                                tags = tagDrafts, tagInput = tagInput, onTagInput = { tagInput = it },
                                onAddTag = {
                                    val t = tagInput.trim()
                                    if (t.isNotEmpty() && !tagDrafts.contains(t)) tagDrafts.add(t)
                                    tagInput = ""
                                },
                                onRemoveTag = { tagDrafts.remove(it) },
                                birthday = birthdayDraft, onPickDate = { showEditDate = true },
                            )
                        } else {
                            Column {
                                Text(contact.name, style = MaterialTheme.typography.headlineMedium)
                                if (contact.title.isNotBlank()) {
                                    Text(
                                        contact.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        contact.relationship.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    ClosenessDots(contact.closeness)
                                }
                                if (contact.tags.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        contact.tags.forEach { tag ->
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = LocalLimanColors.current.bondContainer,
                                                    labelColor = LocalLimanColors.current.onBondContainer,
                                                ),
                                            )
                                        }
                                    }
                                }
                                if (contact.bio.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(contact.bio, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                AnimatedEntrance(delayMillis = 110) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton("Görüştük", Icons.Rounded.CheckCircle, Modifier.weight(1f)) {
                            viewModel.repository.logContact(contact.id)
                            notice = "Son temas bugüne güncellendi"
                        }
                        ActionButton("Mesaj", Icons.Rounded.Chat, Modifier.weight(1f)) {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))) }
                                .onFailure { notice = "Mesaj uygulaması açılamadı" }
                        }
                    }
                }

                if (notice != null) {
                    Text(notice!!, style = MaterialTheme.typography.bodySmall, color = LocalLimanColors.current.bondAccent)
                }

                AnimatedEntrance(delayMillis = 160) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCard(
                            "Doğum günü",
                            contact.birthday?.format(dayMonth) ?: "—",
                            contact.daysUntilBirthday(today)?.let { d ->
                                when (d) { 0L -> "Bugün!"; 1L -> "Yarın"; else -> "$d gün sonra" }
                            } ?: "Eklenmedi",
                            Modifier.weight(1f),
                        )
                        InfoCard(
                            "Son temas",
                            contact.lastContact?.format(dayMonth) ?: "—",
                            contact.daysSinceContact(today)?.let { d ->
                                when (d) { 0L -> "Bugün"; 1L -> "Dün"; else -> "$d gün önce" }
                            } ?: "Henüz yok",
                            Modifier.weight(1f),
                        )
                    }
                }

                // İletişim ritmi (keep-in-touch)
                AnimatedEntrance(delayMillis = 210) {
                    LimanCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("İletişim ritmi", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Bu kişiyle ne sıklıkta haberleşmek istersin?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf<Pair<String, Int?>>(
                                    "Kapalı" to null,
                                    "Haftalık" to 7,
                                    "İki haftada" to 14,
                                    "Aylık" to 30,
                                ).forEach { (label, days) ->
                                    FilterChip(
                                        selected = contact.keepInTouchDays == days,
                                        onClick = { viewModel.repository.upsertContact(contact.copy(keepInTouchDays = days)) },
                                        label = { Text(label) },
                                    )
                                }
                            }
                            contact.keepInTouchOverdueDays(today)?.let { over ->
                                Text(
                                    "Hedefini $over gün aştın — kısa bir merhaba iyi gelebilir.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                // Kişiye özel defter (notlar)
                AnimatedEntrance(delayMillis = 250) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            "Defter",
                            subtitle = "${contact.entries.size} not",
                            trailing = { TextButton(onClick = { showNoteDialog = true }) { Text("Not ekle") } },
                        )
                        if (contact.entries.isEmpty()) {
                            LimanCard(Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    QuillIcon(modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "${contact.name} hakkında düşündüklerini buraya yaz. Yalnızca sana ait.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        } else {
                            contact.entries.forEach { entry ->
                                NoteRow(entry) { onOpenNote(entry.id) }
                            }
                        }
                    }
                }

                // Fotoğraf albümü
                AnimatedEntrance(delayMillis = 300) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Fotoğraf albümü", subtitle = "${contact.photos.size}/5")
                        if (contact.photos.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                contact.photos.forEachIndexed { index, uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Fotoğraf ${index + 1}",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(104.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewerIndex = index },
                                    )
                                }
                            }
                        } else {
                            Text(
                                "Henüz fotoğraf yok. En fazla 5 fotoğraf ekleyebilirsin.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (contact.photos.size < 5) {
                                OutlinedButton(onClick = {
                                    albumPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Fotoğraf ekle")
                                }
                            }
                            OutlinedButton(onClick = { showAlbumDialog = true }) {
                                Icon(Icons.Rounded.Link, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (contact.albumUrl.isNullOrBlank()) "Albüm linki" else "Albümü aç")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialog && contact != null) {
        AddNoteDialog(
            onDismiss = { showNoteDialog = false },
            onAdd = { title, text, feeling ->
                viewModel.repository.addEntry(contactId, title = title, text = text, feeling = feeling)
                showNoteDialog = false
            },
        )
    }

    if (showAlbumDialog && contact != null) {
        AlbumLinkDialog(
            initial = contact.albumUrl.orEmpty(),
            onDismiss = { showAlbumDialog = false },
            onSave = { url ->
                viewModel.repository.upsertContact(contact.copy(albumUrl = url.ifBlank { null }))
                showAlbumDialog = false
            },
            onOpen = { url ->
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    .onFailure { notice = "Bağlantı açılamadı" }
                showAlbumDialog = false
            },
        )
    }

    if (showEditDate) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEditDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        birthdayDraft = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEditDate = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showEditDate = false }) { Text("Vazgeç") } },
        ) { DatePicker(state = state) }
    }

    viewerIndex?.let { idx ->
        FullscreenPhotoViewer(
            photos = contact?.photos ?: emptyList(),
            startIndex = idx,
            onClose = { viewerIndex = null },
        )
    }
}

@Composable
private fun EditFields(
    name: String, onName: (String) -> Unit,
    title: String, onTitle: (String) -> Unit,
    bio: String, onBio: (String) -> Unit,
    rel: RelationshipType, onRel: (RelationshipType) -> Unit,
    closeness: Int, onCloseness: (Int) -> Unit,
    accent: Color?, onAccent: (Color?) -> Unit,
    tags: List<String>, tagInput: String, onTagInput: (String) -> Unit,
    onAddTag: () -> Unit, onRemoveTag: (String) -> Unit,
    birthday: LocalDate?, onPickDate: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, onName, label = { Text("İsim") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(title, onTitle, label = { Text("Başlık (örn. \"en iyi arkadaşım\")") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(bio, onBio, label = { Text("Kısa not / bio") }, modifier = Modifier.fillMaxWidth())
        Text("İlişki", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RelationshipType.entries.forEach { type ->
                FilterChip(selected = rel == type, onClick = { onRel(type) }, label = { Text(type.label) })
            }
        }
        Text("Yakınlık / önem", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (1..5).forEach { level ->
                val on = level <= closeness
                Box(
                    Modifier.size(32.dp).clip(CircleShape)
                        .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable { onCloseness(level) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$level", color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Text("Renk", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            accentChoices.forEach { c ->
                val selected = accent == c
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(c)
                        .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable { onAccent(if (selected) null else c) },
                )
            }
        }
        Text("Etiketler", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(tagInput, onTagInput, label = { Text("örn. huzur, sıkıntı") }, singleLine = true, modifier = Modifier.weight(1f))
            IconButton(onClick = onAddTag) { Icon(Icons.Rounded.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.primary) }
        }
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    InputChip(selected = false, onClick = { onRemoveTag(tag) }, label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Kaldır", modifier = Modifier.size(16.dp)) })
                }
            }
        }
        OutlinedButton(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) {
            Text(birthday?.let { "Doğum günü: ${it.format(dayMonthYear)}" } ?: "Doğum günü seç")
        }
        Text("Yukarıdaki ✓ ile kaydet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ClosenessDots(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { i ->
            Box(
                Modifier.size(7.dp).clip(CircleShape)
                    .background(if (i < level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String, sub: String, modifier: Modifier) {
    LimanCard(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Cake, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteRow(entry: NotebookEntry, onClick: () -> Unit) {
    LimanCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            entry.coverPhoto?.let { cover ->
                AsyncImage(
                    model = cover,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.title.ifBlank { "Not" },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    entry.feeling?.let {
                        Icon(
                            moodIcon(it),
                            contentDescription = it.label,
                            tint = LocalLimanColors.current.moodColor(it.score),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Text(entry.date.format(dayMonthYear), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.preview.isNotBlank()) {
                    Text(
                        entry.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String, MoodFace?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var feeling by remember { mutableStateOf<MoodFace?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Not ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık (opsiyonel)") }, singleLine = true)
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Bu kişi hakkında ne düşünüyorsun?") })
                Text("Şu an nasıl hissediyorsun? (opsiyonel)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MoodFaceRow(
                    selected = feeling,
                    onSelect = { f -> feeling = if (feeling == f) null else f },
                    bubbleSize = 44,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank() || title.isNotBlank()) onAdd(title.trim(), text.trim(), feeling) },
                enabled = text.isNotBlank() || title.isNotBlank(),
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

@Composable
private fun AlbumLinkDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    var url by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Albüm linki") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Herhangi bir albüm bağlantısı yapıştırabilirsin (Google Fotoğraflar, iCloud, vb.).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("https://…") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(url.trim()) }) { Text("Kaydet") } },
        dismissButton = {
            if (initial.isNotBlank()) TextButton(onClick = { onOpen(initial) }) { Text("Aç") }
            else TextButton(onClick = onDismiss) { Text("Vazgeç") }
        },
    )
}

private val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("tr"))
private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
