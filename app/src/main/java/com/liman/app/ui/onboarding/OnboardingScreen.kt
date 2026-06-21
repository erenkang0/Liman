package com.liman.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.liman.app.data.model.Gender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private data class Slide(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val slides = listOf(
    Slide(
        Icons.Rounded.WavingHand,
        "Liman'a hoş geldin",
        "Zihnine ve bağlarına bir liman. Burada hem kendinle hem sevdiklerinle nazikçe ilgilenebilirsin.",
    ),
    Slide(
        Icons.Rounded.Spa,
        "İki dünya, tek liman",
        "Ben sekmesi tamamen mahrem iç dünyan; Bağlar ise sıcak ilişki günlüğün. İkisi arasında akıcıca geçersin.",
    ),
    Slide(
        Icons.Rounded.Lock,
        "Gizliliğin esas",
        "Tüm bilgiler yalnızca cihazında saklanır. Günlük yazıların uçtan uca şifrelenir; ham duyguların güvende.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (name: String, birthDate: LocalDate?, gender: Gender?) -> Unit,
    onSkip: () -> Unit,
) {
    var step by remember { mutableStateOf(0) }   // 0..2 slaytlar, 3 profil
    val total = 4

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var gender by remember { mutableStateOf<Gender?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp),
    ) {
        // Üst satır: ilerleme noktaları + Atla
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(total) { i ->
                    val active = i == step
                    val w by animateDpAsState(if (active) 22.dp else 8.dp, label = "dot")
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(w)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                    )
                }
            }
            TextButton(onClick = onSkip) { Text("Atla") }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "onboardingStep",
            ) { s ->
                if (s < 3) {
                    SlideContent(slides[s])
                } else {
                    ProfileStep(
                        name = name,
                        onName = { name = it },
                        birthDate = birthDate,
                        onPickDate = { showDatePicker = true },
                        gender = gender,
                        onGender = { gender = it },
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }) { Text("Geri") }
                Spacer(Modifier.width(12.dp))
            }
            Button(
                onClick = {
                    if (step < 3) step++ else onComplete(name, birthDate, gender)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(if (step < 3) "Devam" else "Başla")
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        birthDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Vazgeç") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun SlideContent(slide: Slide) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Box(
            Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                slide.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
            )
        }
        Text(
            slide.title,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            slide.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileStep(
    name: String,
    onName: (String) -> Unit,
    birthDate: LocalDate?,
    onPickDate: () -> Unit,
    gender: Gender?,
    onGender: (Gender) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Favorite, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Seni biraz tanıyalım", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            "Bu bilgiler yalnızca cihazında kalır.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = name,
            onValueChange = onName,
            label = { Text("Adın") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedButton(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) {
            Text(birthDate?.let { "Doğum tarihi: $it" } ?: "Doğum tarihi seç (opsiyonel)")
        }

        Text("Cinsiyet", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Gender.entries.forEach { g ->
                FilterChip(
                    selected = gender == g,
                    onClick = { onGender(g) },
                    label = { Text(g.label) },
                )
            }
        }
    }
}
