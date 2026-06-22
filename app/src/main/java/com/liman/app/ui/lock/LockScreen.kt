package com.liman.app.ui.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.AppSettings
import com.liman.app.ui.components.bounceClick
import com.liman.app.ui.copy.LocalCopy

enum class LockReason(val title: String, val subtitle: String) {
    APP_OPEN("Liman kilitli", "Devam etmek için kimliğini doğrula"),
    INNER_WORLD("İç dünyan kilitli", "Ham, savunmasız duyguların güvende. Devam etmek için doğrula"),
}

/**
 * Sıfırdan, minimal kilit ekranı. Tek hareket: tuşa basışta nazik bounce ve PIN
 * noktalarının yumuşak dolması. Parmak izi/yüz tuşu, tuş takımının sol alt
 * köşesinde — yani numara tarafında — durur.
 */
@Composable
fun LockScreen(
    settings: AppSettings,
    reason: LockReason,
    onUnlock: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val bioAvailable = remember { canUseBiometric(context) } && settings.biometricEnabled
    val pinSet = settings.pinIsSet

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val subtitle = if (reason == LockReason.INNER_WORLD) LocalCopy.current.lockInnerSubtitle
    else reason.subtitle

    fun tryBiometric() {
        if (bioAvailable && activity != null) promptBiometric(activity, onSuccess = onUnlock)
    }

    LaunchedEffect(Unit) { if (bioAvailable) tryBiometric() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(reason.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        if (pinSet) {
            PinDots(length = pin.length, error = error)
            Spacer(Modifier.height(32.dp))
            PinPad(
                showBiometric = bioAvailable,
                onBiometric = { tryBiometric() },
                onDigit = { d ->
                    if (pin.length < 4) {
                        error = false
                        pin += d
                        if (pin.length == 4) {
                            if (pin == settings.pin) onUnlock() else {
                                error = true
                                pin = ""
                            }
                        }
                    }
                },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
            )
            if (error) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "PIN yanlış, tekrar dene",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        } else if (bioAvailable) {
            // PIN yok ama biyometri var: tek büyük parmak izi tuşu.
            BiometricKey(size = 88, onClick = { tryBiometric() })
            Spacer(Modifier.height(12.dp))
            Text(
                "Parmak izi veya yüz ile aç",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Ne PIN ne biyometri: kilitlenip kalmamak için nazik geçiş.
            TextButton(onClick = onUnlock) { Text("Kilidi aç") }
            Text(
                "Daha güçlü koruma için Ayarlar'dan PIN belirleyebilir veya biyometriyi açabilirsin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PinDots(length: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(4) { i ->
            val filled = i < length
            val scale by animateFloatAsState(if (filled) 1f else 0.7f, label = "pinDot")
            Box(
                Modifier
                    .scale(scale)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            error -> MaterialTheme.colorScheme.error
                            filled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
            )
        }
    }
}

@Composable
private fun PinPad(
    showBiometric: Boolean,
    onBiometric: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    PinKey(onClick = { onDigit(key) }) {
                        Text(key, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
        // Son satır: [parmak izi] [0] [sil] — parmak izi numara tarafında.
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            if (showBiometric) {
                PinKey(onClick = onBiometric, tinted = true) {
                    Icon(
                        Icons.Rounded.Fingerprint,
                        contentDescription = "Biyometrik ile aç",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Spacer(Modifier.size(74.dp))
            }
            PinKey(onClick = { onDigit("0") }) {
                Text("0", style = MaterialTheme.typography.headlineSmall)
            }
            PinKey(onClick = onBackspace) {
                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Sil")
            }
        }
    }
}

@Composable
private fun PinKey(onClick: () -> Unit, tinted: Boolean = false, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(
                if (tinted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun BiometricKey(size: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Fingerprint,
            contentDescription = "Biyometrik ile aç",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size((size * 0.5f).dp),
        )
    }
}
