package com.liman.app.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.theme.JournalBodyStyle
import com.liman.app.ui.theme.JournalTitleStyle
import com.liman.app.ui.theme.LocalLimanColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val copy = LocalCopy.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf<MoodFace?>(null) }
    var photoCount by remember { mutableStateOf(0) }
    var hasAudio by remember { mutableStateOf(false) }

    val canSave = body.isNotBlank()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Lock,
                            null,
                            modifier = Modifier.height(16.dp),
                            tint = LocalLimanColors.current.lockTint,
                        )
                        Spacer(Modifier.height(0.dp))
                        Text("  Uçtan uca şifreli", style = MaterialTheme.typography.titleSmall)
                    }
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.repository.addJournal(
                                title = title.trim(),
                                body = body.trim(),
                                moodFace = mood,
                                hasAudio = hasAudio,
                                photoCount = photoCount,
                            )
                            onSaved()
                        },
                        enabled = canSave,
                    ) { Text("Kaydet") }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Başlık (opsiyonel)", style = JournalTitleStyle) },
                textStyle = JournalTitleStyle,
                singleLine = true,
                colors = immersiveColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = body,
                onValueChange = { body = it },
                placeholder = {
                    Text(copy.journalPlaceholder, style = JournalBodyStyle)
                },
                textStyle = JournalBodyStyle,
                colors = immersiveColors(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Default),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )

            // Yazı ipuçları
            Text(copy.journalPromptsHint, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                copy.journalPrompts.forEach { prompt ->
                    AssistChip(
                        onClick = {
                            body = if (body.isBlank()) "$prompt\n" else body
                        },
                        label = { Text(prompt) },
                        colors = AssistChipDefaults.assistChipColors(),
                    )
                }
            }

            // Ruh hali etiketi
            Text("Ruh halini etiketle", style = MaterialTheme.typography.titleSmall)
            MoodFaceRow(selected = mood, onSelect = { mood = it }, bubbleSize = 48)

            // Ek: foto / ses
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = photoCount > 0,
                    onClick = { photoCount = if (photoCount > 0) 0 else 1 },
                    label = { Text(if (photoCount > 0) "Fotoğraf eklendi" else "Fotoğraf ekle") },
                    leadingIcon = { Icon(Icons.Rounded.PhotoCamera, null, modifier = Modifier.height(18.dp)) },
                )
                FilterChip(
                    selected = hasAudio,
                    onClick = { hasAudio = !hasAudio },
                    label = { Text(if (hasAudio) "Ses notu eklendi" else "Ses notu") },
                    leadingIcon = { Icon(Icons.Rounded.Mic, null, modifier = Modifier.height(18.dp)) },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun immersiveColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)
