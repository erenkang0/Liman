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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.liman.app.data.model.ClientStatus
import com.liman.app.data.model.EntryKind
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.NotebookEntry
import com.liman.app.data.model.RiskLevel
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.AnimatedEntrance
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.FullscreenPhotoViewer
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.components.QuillIcon
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.link.MentionTextField
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
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val context = LocalContext.current
    val today = LocalDate.now()

    var showSessionDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showAlbumDialog by remember { mutableStateOf(false) }
    var showNextSession by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var goalDraft by remember(contactId) { mutableStateOf("") }

    // Satır içi düzenleme
    var editing by remember { mutableStateOf(false) }
    var nameDraft by remember(contactId) { mutableStateOf("") }
    var bioDraft by remember(contactId) { mutableStateOf("") }
    var riskDraft by remember(contactId) { mutableStateOf(RiskLevel.NONE) }
    var statusDraft by remember(contactId) { mutableStateOf(ClientStatus.ACTIVE) }
    var accentDraft by remember(contactId) { mutableStateOf<Color?>(null) }
    var intakeDraft by remember(contactId) { mutableStateOf<LocalDate?>(null) }
    val tagDrafts = remember(contactId) { mutableListOf<String>().toMutableStateList() }
    var tagInput by remember(contactId) { mutableStateOf("") }
    var showIntakeDate by remember { mutableStateOf(false) }

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
                title = { Text(contact?.name ?: "Danışan") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (contact != null) {
                        IconButton(onClick = {
                            if (editing) {
                                viewModel.repository.upsertContact(
                                    contact.copy(
                                        name = nameDraft.trim().ifBlank { contact.name },
                                        bio = bioDraft.trim(),
                                        risk = riskDraft,
                                        status = statusDraft,
                                        accentColorArgb = accentDraft?.toArgb(),
                                        intakeDate = intakeDraft,
                                        tags = tagDrafts.toList(),
                                    )
                                )
                                editing = false
                            } else {
                                nameDraft = contact.name
                                bioDraft = contact.bio
                                riskDraft = contact.risk
                                statusDraft = contact.status
                                accentDraft = contact.accentColorArgb?.let { Color(it) }
                                intakeDraft = contact.intakeDate
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
                Text("Danışan bulunamadı.")
            }
            return@Scaffold
        }

        val accent = contact.accentColorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
        val sessionNumbers = remember(contact.entries) {
            contact.entries.filter { it.kind == EntryKind.SESSION }
                .sortedBy { it.date }
                .mapIndexed { i, e -> e.id to (i + 1) }
                .toMap()
        }

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
                    Crossfade(targetState = editing, label = "clientHeader") { edit ->
                        if (edit) {
                            EditFields(
                                name = nameDraft, onName = { nameDraft = it },
                                bio = bioDraft, onBio = { bioDraft = it },
                                risk = riskDraft, onRisk = { riskDraft = it },
                                status = statusDraft, onStatus = { statusDraft = it },
                                accent = accentDraft, onAccent = { accentDraft = it },
                                tags = tagDrafts, tagInput = tagInput, onTagInput = { tagInput = it },
                                onAddTag = {
                                    val t = tagInput.trim()
                                    if (t.isNotEmpty() && !tagDrafts.contains(t)) tagDrafts.add(t)
                                    tagInput = ""
                                },
                                onRemoveTag = { tagDrafts.remove(it) },
                                intake = intakeDraft, onPickIntake = { showIntakeDate = true },
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(contact.name, style = MaterialTheme.typography.headlineMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RiskBadge(contact.risk)
                                    StatusChip(contact.status)
                                }
                                if (contact.bio.isNotBlank()) {
                                    Text(contact.bio, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (contact.tags.isNotEmpty()) {
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
                            }
                        }
                    }
                }

                // Hızlı aksiyonlar
                AnimatedEntrance(delayMillis = 110) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton("Seans işle", Icons.Rounded.CheckCircle, Modifier.weight(1f)) {
                            viewModel.repository.logContact(contact.id)
                            notice = "Son seans bugüne işlendi"
                        }
                        ActionButton("Randevu", Icons.Rounded.EventAvailable, Modifier.weight(1f)) {
                            showNextSession = true
                        }
                    }
                }

                if (notice != null) {
                    Text(notice!!, style = MaterialTheme.typography.bodySmall, color = LocalLimanColors.current.bondAccent)
                }

                // Klinik bilgi kartları
                AnimatedEntrance(delayMillis = 150) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoCard("İlk görüşme", contact.intakeDate?.format(dayMonthYear) ?: "—", "${contact.sessionCount} seans", Modifier.weight(1f))
                        InfoCard(
                            "Sonraki randevu",
                            contact.nextSession?.format(dayMonth) ?: "—",
                            contact.daysUntilNextSession(today)?.let { d ->
                                when { d == 0L -> "Bugün"; d == 1L -> "Yarın"; d > 0 -> "$d gün sonra"; else -> "geçti" }
                            } ?: "Planlanmadı",
                            Modifier.weight(1f),
                        )
                    }
                }

                // Tedavi planı / hedefler
                AnimatedEntrance(delayMillis = 200) {
                    LimanCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Tedavi hedefleri", style = MaterialTheme.typography.titleMedium)
                            if (contact.goals.isEmpty()) {
                                Text(
                                    "Henüz hedef eklenmedi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                contact.goals.forEach { goal ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = goal.done,
                                            onCheckedChange = { viewModel.repository.toggleGoal(contact.id, goal.id) },
                                        )
                                        Text(
                                            goal.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textDecoration = if (goal.done) TextDecoration.LineThrough else null,
                                            color = if (goal.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IconButton(onClick = { viewModel.repository.deleteGoal(contact.id, goal.id) }) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Sil", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = goalDraft,
                                    onValueChange = { goalDraft = it },
                                    label = { Text("Yeni hedef") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = {
                                    val t = goalDraft.trim()
                                    if (t.isNotEmpty()) { viewModel.repository.addGoal(contact.id, t); goalDraft = "" }
                                }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Hedef ekle", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Seans & notlar
                AnimatedEntrance(delayMillis = 250) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Seans notları", style = MaterialTheme.typography.titleLarge)
                                Text("${contact.entries.size} kayıt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { showSessionDialog = true }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.NoteAdd, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Seans ekle")
                            }
                            OutlinedButton(onClick = { showNoteDialog = true }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Serbest not")
                            }
                        }
                        if (contact.entries.isEmpty()) {
                            LimanCard(Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    QuillIcon(modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "İlk seans notunu ekle. SOAP biçimi (S/O/A/P) ile yapılandırabilirsin.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        } else {
                            contact.entries.forEach { entry ->
                                NoteRow(entry, sessionNumbers[entry.id]) { onOpenNote(entry.id) }
                            }
                        }
                    }
                }

                // Belgeler / fotoğraflar
                AnimatedEntrance(delayMillis = 300) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Belgeler / fotoğraflar", style = MaterialTheme.typography.titleMedium)
                        if (contact.photos.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                contact.photos.forEachIndexed { index, uri ->
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Belge ${index + 1}",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier
                                            .size(104.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewerIndex = index },
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (contact.photos.size < 5) {
                                OutlinedButton(onClick = {
                                    albumPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Ekle")
                                }
                            }
                            OutlinedButton(onClick = { showAlbumDialog = true }) {
                                Icon(Icons.Rounded.Link, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (contact.albumUrl.isNullOrBlank()) "Bağlantı" else "Bağlantıyı aç")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSessionDialog && contact != null) {
        AddSessionDialog(
            contacts = contacts,
            journals = journals,
            excludeContactId = contactId,
            onDismiss = { showSessionDialog = false },
            onAdd = { s, o, a, p, dur, feeling, mentions ->
                viewModel.repository.addEntry(
                    contactId, kind = EntryKind.SESSION,
                    subjective = s, objective = o, assessment = a, plan = p,
                    durationMin = dur, feeling = feeling, mentions = mentions,
                )
                viewModel.repository.logContact(contactId)
                showSessionDialog = false
            },
        )
    }

    if (showNoteDialog && contact != null) {
        AddNoteDialog(
            contacts = contacts,
            journals = journals,
            excludeContactId = contactId,
            onDismiss = { showNoteDialog = false },
            onAdd = { title, text, feeling, mentions ->
                viewModel.repository.addEntry(contactId, kind = EntryKind.NOTE, title = title, text = text, feeling = feeling, mentions = mentions)
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

    if (showNextSession && contact != null) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showNextSession = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val d = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.repository.upsertContact(contact.copy(nextSession = d))
                        notice = "Randevu ${d.format(dayMonthYear)} olarak ayarlandı"
                    }
                    showNextSession = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.repository.upsertContact(contact.copy(nextSession = null))
                    showNextSession = false
                }) { Text("Temizle") }
            },
        ) { DatePicker(state = state) }
    }

    if (showIntakeDate) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showIntakeDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        intakeDraft = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showIntakeDate = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showIntakeDate = false }) { Text("Vazgeç") } },
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
private fun RiskBadge(risk: RiskLevel) {
    val color = when (risk) {
        RiskLevel.NONE -> MaterialTheme.colorScheme.surfaceContainerHighest
        RiskLevel.LOW -> MaterialTheme.colorScheme.secondary
        RiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
        RiskLevel.HIGH -> MaterialTheme.colorScheme.error
    }
    val onColor = if (risk == RiskLevel.NONE) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(color).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text("Risk: ${risk.label}", style = MaterialTheme.typography.labelMedium, color = onColor)
    }
}

@Composable
private fun StatusChip(status: ClientStatus) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp))
            .background(LocalLimanColors.current.bondContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(status.label, style = MaterialTheme.typography.labelMedium, color = LocalLimanColors.current.onBondContainer)
    }
}

@Composable
private fun EditFields(
    name: String, onName: (String) -> Unit,
    bio: String, onBio: (String) -> Unit,
    risk: RiskLevel, onRisk: (RiskLevel) -> Unit,
    status: ClientStatus, onStatus: (ClientStatus) -> Unit,
    accent: Color?, onAccent: (Color?) -> Unit,
    tags: List<String>, tagInput: String, onTagInput: (String) -> Unit,
    onAddTag: () -> Unit, onRemoveTag: (String) -> Unit,
    intake: LocalDate?, onPickIntake: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, onName, label = { Text("Ad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(bio, onBio, label = { Text("Başvuru nedeni / not") }, modifier = Modifier.fillMaxWidth())
        Text("Risk düzeyi", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RiskLevel.entries.forEach { r -> FilterChip(selected = risk == r, onClick = { onRisk(r) }, label = { Text(r.label) }) }
        }
        Text("Durum", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClientStatus.entries.forEach { s -> FilterChip(selected = status == s, onClick = { onStatus(s) }, label = { Text(s.label) }) }
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
        Text("Temalar / etiketler", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(tagInput, onTagInput, label = { Text("örn. anksiyete") }, singleLine = true, modifier = Modifier.weight(1f))
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
        OutlinedButton(onClick = onPickIntake, modifier = Modifier.fillMaxWidth()) {
            Text(intake?.let { "İlk görüşme: ${it.format(dayMonthYear)}" } ?: "İlk görüşme tarihi seç")
        }
        Text("Yukarıdaki ✓ ile kaydet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)) {
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
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteRow(entry: NotebookEntry, sessionNo: Int?, onClick: () -> Unit) {
    LimanCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val heading = if (entry.kind == EntryKind.SESSION) {
                        "Seans" + (sessionNo?.let { " #$it" } ?: "")
                    } else {
                        entry.title.ifBlank { "Not" }
                    }
                    Text(heading, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (entry.kind == EntryKind.SESSION) {
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) { Text("SOAP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                        Spacer(Modifier.width(6.dp))
                    }
                    entry.feeling?.let {
                        Icon(moodIcon(it), contentDescription = it.label, tint = LocalLimanColors.current.moodColor(it.score), modifier = Modifier.size(18.dp))
                    }
                }
                Text(entry.date.format(dayMonthYear), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.preview.isNotBlank()) {
                    Text(entry.preview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun AddSessionDialog(
    contacts: List<com.liman.app.data.model.Contact>,
    journals: List<com.liman.app.data.model.JournalEntry>,
    excludeContactId: String?,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, Int?, MoodFace?, List<com.liman.app.data.model.Mention>) -> Unit,
) {
    var s by remember { mutableStateOf("") }
    var o by remember { mutableStateOf("") }
    var a by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var dur by remember { mutableStateOf("50") }
    var feeling by remember { mutableStateOf<MoodFace?>(null) }
    val mentions = remember { mutableListOf<com.liman.app.data.model.Mention>().toMutableStateList() }
    fun add(m: List<com.liman.app.data.model.Mention>) {
        m.forEach { mm -> if (mentions.none { it.id == mm.id && it.label == mm.label }) mentions.add(mm) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seans notu (SOAP)") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MentionTextField(s, mentions, { t, m -> s = t; add(m) }, "S — Danışanın aktardıkları", contacts, journals, excludeContactId = excludeContactId)
                MentionTextField(o, mentions, { t, m -> o = t; add(m) }, "O — Gözlemler", contacts, journals, excludeContactId = excludeContactId)
                MentionTextField(a, mentions, { t, m -> a = t; add(m) }, "A — Değerlendirme", contacts, journals, excludeContactId = excludeContactId)
                MentionTextField(p, mentions, { t, m -> p = t; add(m) }, "P — Plan / ödev", contacts, journals, excludeContactId = excludeContactId)
                OutlinedTextField(dur, { dur = it.filter { ch -> ch.isDigit() } }, label = { Text("Süre (dk)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Danışanın seans ruh hali (opsiyonel)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MoodFaceRow(selected = feeling, onSelect = { f -> feeling = if (feeling == f) null else f }, bubbleSize = 42)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val any = listOf(s, o, a, p).any { it.isNotBlank() }
                    if (any) onAdd(s.trim(), o.trim(), a.trim(), p.trim(), dur.toIntOrNull(), feeling, mentions.toList())
                },
                enabled = listOf(s, o, a, p).any { it.isNotBlank() },
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

@Composable
private fun AddNoteDialog(
    contacts: List<com.liman.app.data.model.Contact>,
    journals: List<com.liman.app.data.model.JournalEntry>,
    excludeContactId: String?,
    onDismiss: () -> Unit,
    onAdd: (String, String, MoodFace?, List<com.liman.app.data.model.Mention>) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var feeling by remember { mutableStateOf<MoodFace?>(null) }
    val mentions = remember { mutableListOf<com.liman.app.data.model.Mention>().toMutableStateList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Serbest not") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık (opsiyonel)") }, singleLine = true)
                MentionTextField(
                    text = text,
                    mentions = mentions,
                    onChange = { t, m -> text = t; m.forEach { mm -> if (mentions.none { it.id == mm.id && it.label == mm.label }) mentions.add(mm) } },
                    label = "Not — @ ile bağ kur",
                    contacts = contacts,
                    journals = journals,
                    excludeContactId = excludeContactId,
                )
                MoodFaceRow(selected = feeling, onSelect = { f -> feeling = if (feeling == f) null else f }, bubbleSize = 42)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank() || title.isNotBlank()) onAdd(title.trim(), text.trim(), feeling, mentions.toList()) },
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
        title = { Text("Bağlantı") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Bir belge/albüm bağlantısı ekleyebilirsin (Drive, iCloud, vb.).",
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
