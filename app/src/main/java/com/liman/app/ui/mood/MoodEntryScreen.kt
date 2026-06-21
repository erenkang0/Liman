package com.liman.app.ui.mood

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.MoodIntensity
import com.liman.app.data.model.MoodTrigger
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.theme.LocalLimanColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var face by remember { mutableStateOf(MoodFace.NEUTRAL) }
    var intensityIndex by remember { mutableStateOf(2f) }
    val triggers = remember { mutableStateListOf<MoodTrigger>() }
    var note by remember { mutableStateOf("") }

    val moodColor = LocalLimanColors.current.moodColor(face.score)
    val breathe = rememberInfiniteTransition(label = "breathe")
    val scale by breathe.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "breatheScale",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ruh hali") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Büyük, nefes alan, halo'lu emoji
            Box(
                Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .scale(scale)
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(moodColor.copy(alpha = 0.35f), moodColor.copy(alpha = 0f)),
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(face.emoji, fontSize = 92.sp)
                }
            }

            Text(
                face.label,
                style = MaterialTheme.typography.headlineSmall,
            )

            MoodFaceRow(selected = face, onSelect = { face = it })

            // Yoğunluk ölçeği
            Column(Modifier.fillMaxWidth()) {
                Text("Ne kadar güçlü?", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = intensityIndex,
                    onValueChange = { intensityIndex = it },
                    valueRange = 0f..4f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = moodColor,
                        activeTrackColor = moodColor,
                    ),
                )
                Text(
                    MoodIntensity.entries[intensityIndex.roundToInt().coerceIn(0, 4)].label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Tetikleyiciler
            Column(Modifier.fillMaxWidth()) {
                Text("Neler etkiledi?", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoodTrigger.entries.forEach { trigger ->
                        val selected = trigger in triggers
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) triggers.remove(trigger) else triggers.add(trigger)
                            },
                            label = { Text(trigger.label) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Not (opsiyonel)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )

            Button(
                onClick = {
                    viewModel.repository.addMood(
                        face = face,
                        intensity = MoodIntensity.entries[intensityIndex.roundToInt().coerceIn(0, 4)],
                        triggers = triggers.toList(),
                        note = note.trim(),
                    )
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Kaydet")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
