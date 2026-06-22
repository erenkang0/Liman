package com.liman.app.ui.bonds

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.ClientStatus
import com.liman.app.data.model.RiskLevel
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val accentChoices = listOf(
    Color(0xFFC8323C), Color(0xFFD98C5A), Color(0xFFC8A36A),
    Color(0xFF4E8D6E), Color(0xFF0E7C99), Color(0xFF6A4FC4), Color(0xFFC24468),
)

@Composable
fun AddContactScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var presenting by remember { mutableStateOf("") }
    var risk by remember { mutableStateOf(RiskLevel.NONE) }
    var status by remember { mutableStateOf(ClientStatus.ACTIVE) }
    var accent by remember { mutableStateOf<Color?>(null) }
    var intake by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val tags = remember { mutableListOf<String>().toMutableStateList() }
    var tagDraft by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Yeni danışan") },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ad / Danışan adı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = presenting,
                onValueChange = { presenting = it },
                label = { Text("Başvuru nedeni / ön görüşme notu") },
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text("Risk düzeyi", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskLevel.entries.forEach { r ->
                        FilterChip(selected = risk == r, onClick = { risk = r }, label = { Text(r.label) })
                    }
                }
            }

            Column {
                Text("Durum", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClientStatus.entries.forEach { s ->
                        FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s.label) })
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(intake?.let { "İlk görüşme: $it" } ?: "İlk görüşme tarihi seç")
            }

            Column {
                Text("Temalar / etiketler", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tagDraft,
                        onValueChange = { tagDraft = it },
                        label = { Text("örn. anksiyete, yas, ilişki") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.size(8.dp))
                    IconButton(onClick = {
                        val t = tagDraft.trim()
                        if (t.isNotEmpty() && !tags.contains(t)) tags.add(t)
                        tagDraft = ""
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Etiket ekle", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { tags.remove(tag) },
                                label = { Text(tag) },
                                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Kaldır", modifier = Modifier.size(16.dp)) },
                            )
                        }
                    }
                }
            }

            Column {
                Text("Renk", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    accentChoices.forEach { c ->
                        val selected = accent == c
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { accent = if (selected) null else c },
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val id = viewModel.repository.addContact(
                            name = name.trim(),
                            bio = presenting.trim(),
                            tags = tags.toList(),
                            risk = risk,
                            status = status,
                            intakeDate = intake,
                            accentColorArgb = accent?.toArgb(),
                        )
                        onSaved(id)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Danışan dosyasını oluştur")
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
                        intake = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Vazgeç") } },
        ) {
            DatePicker(state = state)
        }
    }
}
