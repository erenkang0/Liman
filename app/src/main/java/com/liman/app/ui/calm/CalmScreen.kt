package com.liman.app.ui.calm

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.theme.LocalLimanColors

private data class GroundingStep(val count: Int, val sense: String, val prompt: String)

private val groundingSteps = listOf(
    GroundingStep(5, "görme", "Görebildiğin 5 şeyi fark et"),
    GroundingStep(4, "dokunma", "Dokunabildiğin 4 şeyi hisset"),
    GroundingStep(3, "işitme", "Duyabildiğin 3 sesi dinle"),
    GroundingStep(2, "koku", "Koklayabildiğin 2 kokuyu al"),
    GroundingStep(1, "tat", "Tadabildiğin 1 şeyi fark et"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalmScreen(
    onBack: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenBonds: () -> Unit,
) {
    val context = LocalContext.current
    val copy = LocalCopy.current
    val colors = LocalLimanColors.current
    var stepIndex by remember { mutableIntStateOf(0) }
    val finished = stepIndex >= groundingSteps.size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Sakinleş") },
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                copy.calmIntro,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                copy.calmReassure,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 5-4-3-2-1 topraklanma
            LimanCard(
                Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("5-4-3-2-1 Topraklanma", style = MaterialTheme.typography.titleMedium)
                    AnimatedContent(
                        targetState = stepIndex,
                        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith fadeOut() },
                        label = "grounding",
                    ) { idx ->
                        if (idx < groundingSteps.size) {
                            val step = groundingSteps[idx]
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(colors.bondAccent.copy(alpha = 0.30f), colors.bondAccent.copy(alpha = 0f)),
                                            )
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${step.count}",
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(step.prompt, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                            }
                        } else {
                            Text(
                                "Aferin. Şu an buradasın. 💚",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    if (!finished) {
                        Button(onClick = { stepIndex++ }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (stepIndex == groundingSteps.lastIndex) "Tamamla" else "Tamam, sıradaki")
                        }
                    } else {
                        OutlinedButton(onClick = { stepIndex = 0 }, modifier = Modifier.fillMaxWidth()) {
                            Text("Baştan başla")
                        }
                    }
                }
            }

            // Hızlı destek
            Text("Hızlı destek", style = MaterialTheme.typography.titleMedium)
            CalmAction(Icons.Rounded.SelfImprovement, "Nefes egzersizi", "Birkaç derin nefes al", onOpenBreathing)
            CalmAction(Icons.Rounded.Favorite, "Güvendiğin birini ara", "Bir bağ iyi gelebilir", onOpenBonds)
            CalmAction(
                Icons.Rounded.Call,
                "Acil yardım: 112",
                "Tehlikedeysen ya da acil destek gerekiyorsa",
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                    }
                },
            )

            Text(
                "Yalnız değilsin. Zor bir an yaşıyorsan, güvendiğin biriyle konuşmak ya da bir uzmana ulaşmak güçlü bir adımdır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalmAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    LimanCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
