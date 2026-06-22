package com.liman.app.ui.bonds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.RelationshipType
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.WeatherDot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    editContactId: String? = null,
) {
    val existing = remember(editContactId) { editContactId?.let { viewModel.repository.contact(it) } }
    val isEdit = existing != null

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var bio by remember { mutableStateOf(existing?.bio ?: "") }
    var relationship by remember { mutableStateOf(existing?.relationship ?: RelationshipType.FRIEND) }
    var weather by remember { mutableStateOf(existing?.weather ?: EmotionalWeather.CALM) }
    var birthday by remember { mutableStateOf(existing?.birthday) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Kişiyi düzenle" else "Kişi ekle") },
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
                label = { Text("İsim") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Kısa not / bio (opsiyonel)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text("İlişki", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RelationshipType.entries.forEach { type ->
                        FilterChip(
                            selected = relationship == type,
                            onClick = { relationship = type },
                            label = { Text(type.label) },
                        )
                    }
                }
            }

            Column {
                Text("Duygusal hava", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmotionalWeather.entries.forEach { w ->
                        FilterChip(
                            selected = weather == w,
                            onClick = { weather = w },
                            label = { Text(w.label) },
                            leadingIcon = { WeatherDot(w, size = 12) },
                        )
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(birthday?.let { "Doğum günü: $it" } ?: "Doğum günü seç (opsiyonel)")
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (existing != null) {
                            viewModel.repository.upsertContact(
                                existing.copy(
                                    name = name.trim(),
                                    bio = bio.trim(),
                                    relationship = relationship,
                                    weather = weather,
                                    birthday = birthday,
                                )
                            )
                        } else {
                            viewModel.repository.addContact(name.trim(), relationship, birthday, weather)
                        }
                        onBack()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Kaydet")
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
                        birthday = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
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
