package com.liman.app.ui.tools

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liman.app.data.model.BreathPhase
import com.liman.app.data.model.BreathingTechnique
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.theme.LocalLimanColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var technique by remember { mutableStateOf(BreathingTechnique.FOUR_SEVEN_EIGHT) }
    var running by remember { mutableStateOf(false) }
    var phaseIndex by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    val phases = technique.phases()
    val currentPhase = phases[phaseIndex.coerceIn(0, phases.lastIndex)].first

    val scaleTarget = when {
        !running -> 0.6f
        currentPhase == BreathPhase.INHALE || currentPhase == BreathPhase.HOLD -> 1f
        else -> 0.45f
    }
    val phaseSeconds = phases[phaseIndex.coerceIn(0, phases.lastIndex)].second
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(
            durationMillis = if (running) (phaseSeconds * 1000).coerceAtLeast(400) else 600,
            easing = LinearEasing,
        ),
        label = "breathScale",
    )

    LaunchedEffect(running, technique) {
        if (!running) return@LaunchedEffect
        phaseIndex = 0
        while (true) {
            val (_, secs) = phases[phaseIndex]
            secondsLeft = secs
            repeat(secs) {
                delay(1000)
                secondsLeft -= 1
            }
            phaseIndex = (phaseIndex + 1) % phases.size
        }
    }

    val colors = LocalLimanColors.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nefes & Topraklanma") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreathingTechnique.entries.forEach { t ->
                    FilterChip(
                        selected = technique == t,
                        onClick = {
                            technique = t
                            running = false
                            phaseIndex = 0
                        },
                        label = { Text(t.displayName) },
                    )
                }
            }
            Text(
                technique.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .scale(scale)
                        .size(260.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(colors.warmGradientTop, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                            ),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (running) currentPhase.label else "Hazır mısın?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                        )
                        if (running) {
                            Text(
                                "${secondsLeft.coerceAtLeast(0)}",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }

            if (running) {
                OutlinedButton(onClick = { running = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Durdur")
                }
            } else {
                Button(onClick = { running = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Başlat")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
