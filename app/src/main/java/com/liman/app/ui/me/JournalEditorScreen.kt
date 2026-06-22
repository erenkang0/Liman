package com.liman.app.ui.me

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatClear
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.liman.app.data.media.AudioRecorder
import com.liman.app.data.model.JournalFont
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.VoiceNote
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.components.VoiceNotePlayer
import com.liman.app.ui.components.bounceClick
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.me.rich.RichTextState
import com.liman.app.ui.me.rich.RichVisualTransformation
import com.liman.app.ui.me.rich.toFontFamily
import com.liman.app.ui.theme.JournalBodyStyle
import com.liman.app.ui.theme.JournalTitleStyle
import com.liman.app.ui.theme.LocalLimanColors
import kotlinx.coroutines.delay

@Composable
fun JournalEditorScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val copy = LocalCopy.current

    var title by remember { mutableStateOf("") }
    val rich = remember { RichTextState() }
    var mood by remember { mutableStateOf<MoodFace?>(null) }

    val photos = remember { mutableStateListOf<String>() }
    val tags = remember { mutableStateListOf<String>() }
    var tagInput by remember { mutableStateOf("") }
    var voicePath by remember { mutableStateOf<String?>(null) }
    var voiceDurationMs by remember { mutableLongStateOf(0L) }

    // Sesli kayıt
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    fun startRecording() {
        if (recorder.start()) {
            isRecording = true
            recordSeconds = 0
        }
    }
    fun stopRecording() {
        val path = recorder.stop()
        isRecording = false
        if (path != null) {
            voicePath = path
            voiceDurationMs = recordSeconds * 1000L
        }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordSeconds++
        }
    }
    DisposableEffect(Unit) {
        onDispose { if (isRecording) recorder.cancel() }
    }

    val audioPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) startRecording() }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5),
    ) { uris ->
        val merged = (photos + uris.map { it.toString() }).distinct().take(5)
        photos.clear()
        photos.addAll(merged)
    }

    val canSave = rich.text.isNotBlank() || photos.isNotEmpty() || voicePath != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Lock,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = LocalLimanColors.current.lockTint,
                        )
                        Text("  Uçtan uca şifreli", style = MaterialTheme.typography.titleSmall)
                    }
                },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.repository.addJournal(
                                title = title.trim(),
                                body = rich.text.trim(),
                                spans = rich.spans,
                                font = rich.font,
                                moodFace = mood,
                                voice = voicePath?.let { VoiceNote(it, voiceDurationMs) },
                                photos = photos.toList(),
                                tags = tags.toList(),
                            )
                            onSaved()
                        },
                        enabled = canSave,
                    ) { Text("Kaydet") }
                },
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

            // Sesli not — başlığın hemen altında
            if (voicePath != null) {
                Box {
                    VoiceNotePlayer(voicePath!!, voiceDurationMs, Modifier.fillMaxWidth())
                    IconButton(
                        onClick = { voicePath = null; voiceDurationMs = 0L },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(Icons.Rounded.Close, "Sesli notu kaldır", modifier = Modifier.size(18.dp))
                    }
                }
            } else if (isRecording) {
                RecordingBar(seconds = recordSeconds, onStop = { stopRecording() })
            }

            // Fotoğraflar — ilk fotoğraf afiş (kapak) olur
            if (photos.isNotEmpty()) {
                EditorPhotoGrid(
                    photos = photos,
                    onRemove = { idx -> if (idx in photos.indices) photos.removeAt(idx) },
                )
            }

            // Ekleme aksiyonları
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (photos.size < 5) {
                    AddChip(Icons.Rounded.AddPhotoAlternate, if (photos.isEmpty()) "Fotoğraf (afiş)" else "Fotoğraf ekle") {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }
                if (voicePath == null && !isRecording) {
                    AddChip(Icons.Rounded.Mic, "Sesli not") {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) startRecording() else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }

            // Biçimlendirme araç çubuğu (klavye açıkken hemen yazının üstünde)
            FormattingToolbar(rich)

            // Gövde — anlık biçimli (yıldız görünmez)
            BasicTextField(
                value = rich.value,
                onValueChange = rich::onValueChange,
                textStyle = JournalBodyStyle.copy(
                    fontFamily = rich.font.toFontFamily(),
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = RichVisualTransformation(rich.spans),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp),
                decorationBox = { inner ->
                    if (rich.text.isEmpty()) {
                        Text(
                            copy.journalPlaceholder,
                            style = JournalBodyStyle.copy(
                                fontFamily = rich.font.toFontFamily(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    inner()
                },
            )

            // Ruh hali etiketi
            Text("Ruh halini etiketle", style = MaterialTheme.typography.titleSmall)
            MoodFaceRow(selected = mood, onSelect = { mood = it }, bubbleSize = 48)

            // Etiketler
            Text("Etiketler", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = { Text("Etiket ekle") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val t = tagInput.trim().removePrefix("#")
                        if (t.isNotEmpty() && !tags.contains(t)) tags.add(t)
                        tagInput = ""
                    },
                    enabled = tagInput.isNotBlank(),
                ) { Text("Ekle") }
            }
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { tags.remove(tag) },
                            label = { Text("#$tag") },
                            trailingIcon = { Icon(Icons.Rounded.Close, "Kaldır", modifier = Modifier.size(16.dp)) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormattingToolbar(rich: RichTextState) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FormatToggle(Icons.Rounded.FormatBold, "Kalın", rich.boldActive) { rich.toggleBold() }
            FormatToggle(Icons.Rounded.FormatItalic, "İtalik", rich.italicActive) { rich.toggleItalic() }
            FormatToggle(Icons.Rounded.FormatUnderlined, "Altı çizili", rich.underlineActive) { rich.toggleUnderline() }
            IconButton(onClick = { rich.clearFormatting() }) {
                Icon(Icons.Rounded.FormatClear, "Biçimi temizle")
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            JournalFont.entries.forEach { f ->
                FilterChip(
                    selected = rich.font == f,
                    onClick = { rich.font = f },
                    label = { Text(f.label) },
                )
            }
        }
    }
}

@Composable
private fun FormatToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    FilledIconToggleButton(checked = checked, onCheckedChange = { onToggle() }) {
        Icon(icon, contentDescription = label)
    }
}

@Composable
private fun AddChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .bounceClick { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun RecordingBar(seconds: Int, onStop: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
        Spacer(Modifier.width(10.dp))
        Text(
            "Kaydediliyor… %d:%02d".format(seconds / 60, seconds % 60),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error)
                .bounceClick { onStop() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Stop, "Durdur", tint = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
private fun EditorPhotoGrid(photos: List<String>, onRemove: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        photos.forEachIndexed { index, uri ->
            Box {
                AsyncImage(
                    model = uri,
                    contentDescription = "Fotoğraf ${index + 1}",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                if (index == 0) {
                    Text(
                        "Afiş",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                IconButton(
                    onClick = { onRemove(index) },
                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp),
                ) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, "Kaldır", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun immersiveColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)
