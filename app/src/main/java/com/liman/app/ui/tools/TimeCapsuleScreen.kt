package com.liman.app.ui.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.TimeCapsule
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.theme.JournalBodyStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCapsuleScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
) {
    val capsules by viewModel.repository.capsules.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    var phase by remember { mutableIntStateOf(0) } // 0 yaz, 1 mühürle
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var openOn by remember { mutableStateOf(today.plusMonths(6)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var openedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Zaman Kapsülü") },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LimanCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "capsulePhase",
                    ) { p ->
                        if (p == 0) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Gelecekteki sana yaz", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Başlık") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                OutlinedTextField(
                                    value = body,
                                    onValueChange = { body = it },
                                    placeholder = { Text("Sevgili gelecekteki ben…") },
                                    modifier = Modifier.fillMaxWidth().height(180.dp),
                                )
                                Button(
                                    onClick = { phase = 1 },
                                    enabled = body.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("Devam → mühürle") }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Ne zaman açılsın?", style = MaterialTheme.typography.titleLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PresetChip("1 ay", openOn == today.plusMonths(1)) { openOn = today.plusMonths(1) }
                                    PresetChip("6 ay", openOn == today.plusMonths(6)) { openOn = today.plusMonths(6) }
                                    PresetChip("1 yıl", openOn == today.plusYears(1)) { openOn = today.plusYears(1) }
                                    PresetChip("Özel tarih", false) { showDatePicker = true }
                                }
                                Text(
                                    "Açılış: ${openOn.format(dateFmt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row {
                                    OutlinedButton(onClick = { phase = 0 }) { Text("Geri") }
                                    Spacer(Modifier.size(12.dp))
                                    Button(
                                        onClick = {
                                            val id = viewModel.repository.addCapsule(title.trim().ifBlank { "Mektup" }, body.trim(), openOn)
                                            viewModel.repository.sealCapsule(id)
                                            title = ""; body = ""; phase = 0; openOn = today.plusMonths(6)
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.size(8.dp))
                                            Text("Mühürle")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SectionHeader("Mühürlü mektuplar", subtitle = "${capsules.size} kapsül")
            if (capsules.isEmpty()) {
                Text(
                    "Henüz mühürlü mektubun yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                capsules.forEach { capsule ->
                    CapsuleCard(
                        capsule = capsule,
                        today = today,
                        expanded = openedId == capsule.id,
                        onToggle = { openedId = if (openedId == capsule.id) null else capsule.id },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        openOn = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Vazgeç") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun CapsuleCard(capsule: TimeCapsule, today: LocalDate, expanded: Boolean, onToggle: () -> Unit) {
    val openable = capsule.isOpenable(today)
    LimanCard(
        Modifier.fillMaxWidth(),
        onClick = if (openable) onToggle else null,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (openable) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(capsule.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (openable) "Açılmaya hazır 🎁" else "${capsule.openOn.format(dateFmt)} tarihinde açılacak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (openable && expanded) {
                Text(capsule.body, style = JournalBodyStyle)
            }
        }
    }
}

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
