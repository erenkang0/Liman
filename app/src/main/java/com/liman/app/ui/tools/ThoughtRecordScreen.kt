package com.liman.app.ui.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.ThoughtRecord
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThoughtRecordScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    var situation by remember { mutableStateOf("") }
    var autoThought by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(50f) }
    var evidenceFor by remember { mutableStateOf("") }
    var evidenceAgainst by remember { mutableStateOf("") }
    var balanced by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Düşünce Kaydı") },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            // İlerleme noktaları
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(totalSteps) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Adım ${step + 1} / $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "thoughtStep",
                ) { s ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (s) {
                            0 -> StepBlock(
                                "Durum",
                                "Ne oldu? Nerede, kiminle, ne zaman?",
                            ) { Field(situation, "Durumu yaz…") { situation = it } }

                            1 -> StepBlock(
                                "Otomatik düşünce",
                                "O an aklından geçen ilk düşünce neydi?",
                            ) { Field(autoThought, "Aklından geçen…") { autoThought = it } }

                            2 -> StepBlock(
                                "Duygu",
                                "Ne hissettin ve ne kadar güçlüydü?",
                            ) {
                                Field(emotion, "Örn. kaygı, üzüntü, öfke…", single = true) { emotion = it }
                                Spacer(Modifier.height(8.dp))
                                Text("Yoğunluk: ${intensity.roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
                                Slider(value = intensity, onValueChange = { intensity = it }, valueRange = 0f..100f)
                            }

                            3 -> StepBlock(
                                "Kanıtlar",
                                "Bu düşünceyi destekleyen ve çürüten neler var?",
                            ) {
                                Field(evidenceFor, "Lehine kanıtlar…") { evidenceFor = it }
                                Spacer(Modifier.height(8.dp))
                                Field(evidenceAgainst, "Aleyhine kanıtlar…") { evidenceAgainst = it }
                            }

                            else -> StepBlock(
                                "Dengeli düşünce",
                                "Şimdi daha dengeli, şefkatli bir bakış nasıl olurdu?",
                            ) { Field(balanced, "Daha dengeli bir düşünce…") { balanced = it } }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                if (step > 0) {
                    OutlinedButton(onClick = { step-- }) { Text("Geri") }
                    Spacer(Modifier.width(12.dp))
                }
                Button(
                    onClick = {
                        if (step < totalSteps - 1) {
                            step++
                        } else {
                            viewModel.repository.addThoughtRecord(
                                ThoughtRecord(
                                    id = "",
                                    situation = situation.trim(),
                                    automaticThought = autoThought.trim(),
                                    emotion = emotion.trim(),
                                    emotionIntensity = intensity.roundToInt(),
                                    evidenceFor = evidenceFor.trim(),
                                    evidenceAgainst = evidenceAgainst.trim(),
                                    balancedThought = balanced.trim(),
                                )
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (step < totalSteps - 1) "Devam" else "Tamamla")
                }
            }
        }
    }
}

@Composable
private fun StepBlock(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun Field(value: String, placeholder: String, single: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder) },
        singleLine = single,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (single) 64.dp else 140.dp),
    )
}
