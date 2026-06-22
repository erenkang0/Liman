package com.liman.app.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.MoodEntry
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.moodIcon
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val weekdays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCalendarScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
) {
    val moods by viewModel.repository.moods.collectAsStateWithLifecycle()
    var month by remember { mutableStateOf(YearMonth.now()) }

    val byDate: Map<LocalDate, MoodFace> = remember(moods) {
        moods.groupBy { it.date }
            .mapValues { (_, list) -> list.maxByOrNull { it.timestamp }!!.face }
    }

    val monthMoods = moods.filter { YearMonth.from(it.date) == month }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ruh Hali Takvimi") },
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
            // Ay gezinme
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.Rounded.ChevronLeft, "Önceki ay")
                }
                Text(
                    month.format(monthFmt).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.Rounded.ChevronRight, "Sonraki ay")
                }
            }

            // Hafta başlıkları
            Row(Modifier.fillMaxWidth()) {
                weekdays.forEach { d ->
                    Text(
                        d,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            CalendarGrid(month = month, byDate = byDate)

            // Ayın özeti
            MonthSummary(monthMoods)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarGrid(month: YearMonth, byDate: Map<LocalDate, MoodFace>) {
    val firstDay = month.atDay(1)
    val leadingBlanks = (firstDay.dayOfWeek.value - 1) // Pazartesi = 1
    val daysInMonth = month.lengthOfMonth()
    val cells = buildList {
        repeat(leadingBlanks) { add(null) }
        for (d in 1..daysInMonth) add(month.atDay(d))
    }
    val colors = LocalLimanColors.current
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { date ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            val face = byDate[date]
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        face?.let { colors.moodColor(it.score).copy(alpha = 0.22f) }
                                            ?: MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (face != null) {
                                        Icon(
                                            moodIcon(face),
                                            contentDescription = face.label,
                                            tint = colors.moodColor(face.score),
                                            modifier = Modifier.size(22.dp),
                                        )
                                    } else {
                                        Text(
                                            "${date.dayOfMonth}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (date == today) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Eksik hücreleri doldur (son hafta hizası)
                repeat(7 - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MonthSummary(monthMoods: List<MoodEntry>) {
    LimanCard(
        Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ayın özeti", style = MaterialTheme.typography.titleLarge)
            if (monthMoods.isEmpty()) {
                Text("Bu ay henüz kayıt yok.", style = MaterialTheme.typography.bodyMedium)
            } else {
                val avg = monthMoods.map { it.face.score }.average()
                val face = MoodFace.fromScore(Math.round(avg).toInt())
                val loggedDays = monthMoods.map { it.date }.distinct().size
                val top = monthMoods.groupingBy { it.face }.eachCount().maxByOrNull { it.value }?.key

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        moodIcon(face),
                        contentDescription = face.label,
                        tint = LocalLimanColors.current.moodColor(face.score),
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Ortalama: ${face.label} (%.1f/5)".format(avg), style = MaterialTheme.typography.titleMedium)
                        Text("$loggedDays gün kayıt · en sık ${top?.label ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
