package com.liman.app.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.insight.moodAverage
import com.liman.app.ui.insight.moodsInRange
import com.liman.app.ui.insight.topTriggers
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate

@Composable
fun WeeklyReportScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
) {
    val moods by viewModel.repository.moods.collectAsStateWithLifecycle()
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val gratitude by viewModel.repository.gratitude.collectAsStateWithLifecycle()
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()

    val today = LocalDate.now()
    val from = today.minusDays(6)

    val weekMoods = moodsInRange(moods, 7)
    val avg = moodAverage(weekMoods)
    val avgFace = avg?.let { MoodFace.fromScore(Math.round(it).toInt()) }
    val weekJournals = journals.count { !it.timestamp.toLocalDate().isBefore(from) }
    val weekGratitude = gratitude.count { !it.timestamp.toLocalDate().isBefore(from) }
    val bondsTouched = contacts.count { (it.lastContact?.isBefore(from) == false) }
    val triggers = topTriggers(weekMoods, 3)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Haftalık rapor") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Son 7 gün — kendine nazikçe bir bakış.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            // Ruh hali özeti
            LimanCard(
                Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (avgFace != null) {
                        Icon(
                            moodIcon(avgFace),
                            contentDescription = avgFace.label,
                            tint = LocalLimanColors.current.moodColor(avgFace.score),
                            modifier = Modifier.size(44.dp),
                        )
                    } else {
                        Icon(Icons.Rounded.Mood, contentDescription = null, modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            avg?.let { "Ortalama: ${avgFace?.label} (%.1f/5)".format(it) } ?: "Bu hafta ruh hali kaydı yok",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text("${weekMoods.size} ruh hali kaydı", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Sayaçlar
            SectionHeader("Bu hafta")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Rounded.EditNote, "$weekJournals", "günlük", Modifier.weight(1f))
                StatCard(Icons.Rounded.VolunteerActivism, "$weekGratitude", "şükran", Modifier.weight(1f))
                StatCard(Icons.Rounded.Favorite, "$bondsTouched", "bağ", Modifier.weight(1f))
            }

            // Tetikleyiciler
            if (triggers.isNotEmpty()) {
                SectionHeader("En sık ne etkiledi?")
                LimanCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        triggers.forEach { (trigger, count) ->
                            Text("• ${trigger.label} · $count", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Text(
                "Kayıt sayısı bir başarı ölçütü değil — yalnızca kendine ayırdığın alanın bir yansıması.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    LimanCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
