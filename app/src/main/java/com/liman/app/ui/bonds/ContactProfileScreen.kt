package com.liman.app.ui.bonds

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.Contact
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.Memory
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.weatherColor
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileScreen(
    viewModel: LimanViewModel,
    contactId: String,
    onBack: () -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    val context = LocalContext.current
    val today = LocalDate.now()

    var showMemoryDialog by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var noteDraft by remember(contactId, contact?.weatherNote) { mutableStateOf(contact?.weatherNote ?: "") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(contact?.name ?: "Kişi") },
                navigationIcon = { BackButton(onBack) },
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

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Kapak + avatar
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(weatherColor(contact.weather).copy(alpha = 0.55f), MaterialTheme.colorScheme.primaryContainer),
                            )
                        ),
                )
                Box(Modifier.padding(start = 20.dp, top = 76.dp)) {
                    Box(
                        Modifier.size(92.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).padding(4.dp),
                    ) {
                        Avatar(contact, size = 84)
                    }
                }
            }

            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(contact.name, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "${contact.weather.emoji} ${contact.relationship.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (contact.bio.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(contact.bio, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Aksiyonlar
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("Görüştük", Icons.Rounded.CheckCircle, Modifier.weight(1f)) {
                        viewModel.repository.logContact(contact.id)
                        notice = "Son temas bugüne güncellendi 💛"
                    }
                    ActionButton("Mesaj", Icons.Rounded.Chat, Modifier.weight(1f)) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                            )
                        }.onFailure { notice = "Mesaj uygulaması açılamadı" }
                    }
                    ActionButton("Hatırlat", Icons.Rounded.NotificationsActive, Modifier.weight(1f)) {
                        notice = "Hatırlatıcı kuruldu (yakında bildirimle)"
                    }
                }

                if (notice != null) {
                    Text(notice!!, style = MaterialTheme.typography.bodySmall, color = LocalLimanColors.current.bondAccent)
                }

                // Doğum günü & son temas
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

                // Duygusal hava notu + seçici
                LimanCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Duygusal hava", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EmotionalWeather.entries.forEach { w ->
                                FilterChip(
                                    selected = contact.weather == w,
                                    onClick = { viewModel.repository.upsertContact(contact.copy(weather = w)) },
                                    label = { Text("${w.emoji} ${w.label}") },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = noteDraft,
                            onValueChange = { noteDraft = it },
                            label = { Text("Bu ilişki sana ne hissettiriyor?") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextButton(onClick = {
                            viewModel.repository.upsertContact(contact.copy(weatherNote = noteDraft.trim()))
                            notice = "Not kaydedildi"
                        }) { Text("Notu kaydet") }
                    }
                }

                // Fotoğraf albümü
                SectionHeader("Fotoğraf albümü", subtitle = "${contact.photoCount} fotoğraf")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items((0 until 4).toList()) { i ->
                        Box(
                            Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
                            if (intent != null) context.startActivity(intent)
                            else notice = "Google Fotoğraflar bulunamadı"
                        }.onFailure { notice = "Google Fotoğraflar açılamadı" }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Google Fotoğraflar'a bağla")
                }

                // Anılar zaman tüneli
                SectionHeader(
                    "Anılar",
                    trailing = { TextButton(onClick = { showMemoryDialog = true }) { Text("Anı ekle") } },
                )
                if (contact.memories.isEmpty()) {
                    Text(
                        "Birlikte biriktirdiğiniz anlar burada yaşar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    contact.memories.forEach { memory ->
                        MemoryRow(memory)
                    }
                }
            }
        }
    }

    if (showMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showMemoryDialog = false },
            onAdd = { title, note ->
                viewModel.repository.addMemory(contactId, title, note)
                showMemoryDialog = false
            },
        )
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
private fun MemoryRow(memory: Memory) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.width(2.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(bottom = 12.dp)) {
            Text(memory.title, style = MaterialTheme.typography.titleSmall)
            Text(memory.date.format(dayMonthYear), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (memory.note.isNotBlank()) {
                Text(memory.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AddMemoryDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anı ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Başlık") }, singleLine = true)
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Not (opsiyonel)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (title.isNotBlank()) onAdd(title.trim(), note.trim()) }, enabled = title.isNotBlank()) {
                Text("Ekle")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    )
}

private val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("tr"))
private val dayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
