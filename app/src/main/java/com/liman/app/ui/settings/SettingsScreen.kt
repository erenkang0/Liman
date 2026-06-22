package com.liman.app.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.AutoLock
import com.liman.app.data.model.Gender
import com.liman.app.data.model.LockLocation
import com.liman.app.data.export.buildExportText
import com.liman.app.data.model.NotificationPrefs
import com.liman.app.data.model.ThemeMode
import com.liman.app.data.model.ThemePalette
import com.liman.app.ui.LimanViewModel
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.theme.swatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onOpenReminders: () -> Unit = {},
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameDraft by remember(settings.profile.name) { mutableStateOf(settings.profile.name) }
    // Bildirimler şimdilik yalnızca arayüzde (örnek) — gerçek planlama yakında.
    var notifications by remember { mutableStateOf(settings.notifications) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Profil
            SettingsGroup("Profil") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProfilePhotoPicker(
                        photoUri = settings.profile.photoUri,
                        name = settings.profile.name,
                        onPick = { uri -> viewModel.updateProfile(settings.profile.copy(photoUri = uri)) },
                    )
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    label = { Text("Ad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text("Cinsiyet", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.entries.forEach { g ->
                        FilterChip(
                            selected = settings.profile.gender == g,
                            onClick = { viewModel.updateProfile(settings.profile.copy(gender = g)) },
                            label = { Text(g.label) },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = {
                    viewModel.updateProfile(settings.profile.copy(name = nameDraft.trim()))
                }) { Text("Profili kaydet") }
            }

            // Gizlilik & Güvenlik
            SettingsGroup("Gizlilik & Güvenlik") {
                SwitchRow(
                    "Kilit",
                    "İç dünyanı koru",
                    settings.lockEnabled,
                ) { viewModel.setLockEnabled(it) }

                if (settings.lockEnabled) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Kilit nerede devreye girsin?", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    LockLocation.entries.forEach { loc ->
                        ChoiceRow(
                            title = loc.label,
                            subtitle = loc.description,
                            selected = settings.lockLocation == loc,
                        ) { viewModel.setLockLocation(loc) }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SwitchRow(
                        "Biyometrik",
                        "Parmak izi / yüz tanıma ile aç",
                        settings.biometricEnabled,
                    ) { viewModel.setBiometricEnabled(it) }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Otomatik kilit", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AutoLock.entries.forEach { a ->
                            FilterChip(
                                selected = settings.autoLock == a,
                                onClick = { viewModel.setAutoLock(a) },
                                label = { Text(a.label) },
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("PIN", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (settings.pinIsSet) "PIN ayarlı" else "PIN ayarlanmadı",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { showPinDialog = true }) {
                            Text(if (settings.pinIsSet) "Değiştir" else "Belirle")
                        }
                    }
                }
            }

            // Bildirimler
            SettingsGroup("Bildirimler") {
                Text(
                    LocalCopy.current.notificationsNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                SwitchRow("Nazik günlük hatırlatma", notifications.gentleReminder.time, notifications.gentleReminder.enabled) {
                    notifications = notifications.copy(gentleReminder = notifications.gentleReminder.copy(enabled = it))
                }
                SwitchRow("Duygu check-in", notifications.moodCheckIn.time, notifications.moodCheckIn.enabled) {
                    notifications = notifications.copy(moodCheckIn = notifications.moodCheckIn.copy(enabled = it))
                }
                SwitchRow("Doğum günleri", notifications.birthdays.time, notifications.birthdays.enabled) {
                    notifications = notifications.copy(birthdays = notifications.birthdays.copy(enabled = it))
                }
                SwitchRow("Bağ hatırlatması", notifications.bondReminder.time, notifications.bondReminder.enabled) {
                    notifications = notifications.copy(bondReminder = notifications.bondReminder.copy(enabled = it))
                }
                SwitchRow("Sessiz saatler", "${notifications.quietFrom} – ${notifications.quietTo}", notifications.quietHoursEnabled) {
                    notifications = notifications.copy(quietHoursEnabled = it)
                }
                NavRow("Bildirim hatırlatıcıları → planla & test et") { onOpenReminders() }
            }

            // Görünüm
            SettingsGroup("Görünüm") {
                Text("Tema", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Zindelik teması", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePalette.entries.forEach { palette ->
                        FilterChip(
                            selected = settings.themePalette == palette,
                            onClick = { viewModel.setThemePalette(palette) },
                            label = { Text(palette.label) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(palette.swatch()),
                                )
                            },
                        )
                    }
                }
                if (settings.dynamicColor) {
                    Text(
                        "Dinamik renk açıkken zindelik teması uygulanmaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SwitchRow("Dinamik renk", "Material You — duvar kâğıdından renk", settings.dynamicColor) {
                    viewModel.setDynamicColor(it)
                }
            }

            // Veri & hesap
            SettingsGroup("Veri & Hesap") {
                Text(
                    "Tüm verilerin yalnızca bu cihazda ve şifreli saklanır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                NavRow("Dışa aktar") {
                    val repo = viewModel.repository
                    val text = buildExportText(
                        name = settings.profile.name,
                        moods = repo.moods.value,
                        journals = repo.journals.value,
                        contacts = repo.contacts.value,
                        gratitude = repo.gratitude.value,
                        capsules = repo.capsules.value,
                    )
                    runCatching {
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                },
                                "Liman verisini dışa aktar",
                            )
                        )
                    }
                }
                NavRow("Hesabı sil", destructive = true) { showDeleteDialog = true }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPinDialog) {
        PinDialog(
            hasPin = settings.pinIsSet,
            onDismiss = { showPinDialog = false },
            onSetPin = { viewModel.setPin(it); showPinDialog = false },
            onRemovePin = { viewModel.setPin(null); showPinDialog = false },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hesabı sil") },
            text = { Text("Tüm yerel verilerin silinecek. Bu işlem geri alınamaz.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Vazgeç") } },
            dismissButton = {},
        )
    }
}

@Composable
private fun ProfilePhotoPicker(photoUri: String?, name: String, onPick: (String) -> Unit) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPick(uri.toString()) }

    Box(
        Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Profil fotoğrafı",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                name.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.PhotoCamera,
                contentDescription = "Fotoğraf seç",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        LimanCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ChoiceRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.height(0.dp))
        Column(Modifier.padding(start = 4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NavRow(title: String, destructive: Boolean = false, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            modifier = Modifier.fillMaxWidth(),
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun PinDialog(
    hasPin: Boolean,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val valid = pin.length == 4 && pin == confirm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (hasPin) "PIN değiştir" else "PIN belirle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("4 haneli PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirm = it },
                    label = { Text("PIN tekrar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
                if (pin.isNotEmpty() && confirm.isNotEmpty() && pin != confirm) {
                    Text("PIN'ler eşleşmiyor", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (valid) onSetPin(pin) }, enabled = valid) { Text("Kaydet") } },
        dismissButton = {
            if (hasPin) TextButton(onClick = onRemovePin) { Text("PIN'i kaldır") }
            else TextButton(onClick = onDismiss) { Text("Vazgeç") }
        },
    )
}
