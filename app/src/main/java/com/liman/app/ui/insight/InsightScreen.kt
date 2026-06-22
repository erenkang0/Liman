package com.liman.app.ui.insight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.components.weatherColor
import com.liman.app.ui.components.weatherIcon
import com.liman.app.ui.theme.LocalLimanColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(
    viewModel: LimanViewModel,
    onShare: () -> Unit,
) {
    val moods by viewModel.repository.moods.collectAsStateWithLifecycle()
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()

    var rangeDays by remember { mutableIntStateOf(7) }
    val ranged = remember(moods, rangeDays) { moodsInRange(moods, rangeDays) }
    val avg = moodAverage(ranged)
    val curve = remember(moods, rangeDays) { moodCurve(moods, rangeDays) }
    val distribution = remember(ranged) { moodDistribution(ranged) }
    val triggers = remember(ranged) { topTriggers(ranged) }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("İçgörü", style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onShare) { Text("Paylaş") }
        }

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(7, 30).forEachIndexed { index, days ->
                SegmentedButton(
                    selected = rangeDays == days,
                    onClick = { rangeDays = days },
                    shape = SegmentedButtonDefaults.itemShape(index, 2),
                ) {
                    Text("$days gün")
                }
            }
        }

        // Özet
        LimanCard(Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                val face = avg?.let { MoodFace.fromScore(Math.round(it).toInt()) }
                Icon(
                    if (face != null) moodIcon(face) else Icons.Rounded.Spa,
                    contentDescription = face?.label,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        avg?.let { "Ortalama: %.1f / 5".format(it) } ?: "Henüz veri yok",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "${ranged.size} kayıt · son $rangeDays gün",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Ruh hali eğrisi
        SectionHeader("Ruh hali eğrisi")
        LimanCard(Modifier.fillMaxWidth()) {
            Box(Modifier.padding(16.dp)) {
                MoodCurveChart(curve, Modifier.fillMaxWidth().height(160.dp))
            }
        }

        // Dağılım
        SectionHeader("Ruh hali dağılımı")
        LimanCard(Modifier.fillMaxWidth()) {
            MoodDistribution(distribution, Modifier.padding(16.dp))
        }

        // En sık tetikleyiciler
        SectionHeader("En sık ne etkiledi?")
        LimanCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (triggers.isEmpty()) {
                    Text("Tetikleyici kaydı yok.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    val max = triggers.first().second
                    triggers.forEach { (trigger, count) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(trigger.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(96.dp))
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(count.toFloat() / max)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("$count", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Bağ sıcaklığı
        SectionHeader("Bağ sıcaklığı", subtitle = "İlişkilerinin duygusal havası")
        LimanCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (contacts.isEmpty()) {
                    Text("Henüz bağ yok.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    contacts.take(5).forEach { contact ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(contact, size = 36)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(contact.name, style = MaterialTheme.typography.titleSmall)
                                val warmth = warmthScore(contact.weather)
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(warmth)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(weatherColor(contact.weather)),
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                weatherIcon(contact.weather),
                                contentDescription = contact.weather.label,
                                tint = weatherColor(contact.weather),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun MoodCurveChart(curve: List<Double?>, modifier: Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // ızgara çizgileri (1..5)
        for (i in 0..4) {
            val y = h - (i / 4f) * h
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        val n = curve.size
        if (n < 2) return@Canvas
        fun xFor(i: Int) = w * i / (n - 1)
        fun yFor(v: Double) = h - ((v.toFloat() - 1f) / 4f) * h

        val points = curve.mapIndexedNotNull { i, v -> v?.let { Offset(xFor(i), yFor(it)) } }
        if (points.isEmpty()) return@Canvas

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, lineColor, style = Stroke(width = 5f))
        points.forEach { drawCircle(dotColor, radius = 7f, center = it) }
    }
}

@Composable
private fun MoodDistribution(distribution: Map<MoodFace, Int>, modifier: Modifier) {
    val colors = LocalLimanColors.current
    val max = (distribution.values.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(
        modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        MoodFace.entries.forEach { face ->
            val count = distribution[face] ?: 0
            val fraction = count.toFloat() / max
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(28.dp)
                        .height((10 + fraction * 96).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.moodColor(face.score)),
                )
                Spacer(Modifier.height(6.dp))
                Icon(
                    moodIcon(face),
                    contentDescription = face.label,
                    tint = colors.moodColor(face.score),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun warmthScore(weather: EmotionalWeather): Float = when (weather) {
    EmotionalWeather.SUNNY -> 1f
    EmotionalWeather.WARM -> 0.8f
    EmotionalWeather.CALM -> 0.6f
    EmotionalWeather.CLOUDY -> 0.4f
    EmotionalWeather.STORMY -> 0.2f
}
