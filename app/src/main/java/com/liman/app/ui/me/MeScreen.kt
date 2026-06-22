package com.liman.app.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.JournalEntry
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.bounceClick
import com.liman.app.ui.components.screenPadding
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.navigation.Routes
import com.liman.app.ui.theme.JournalBodyStyle
import com.liman.app.ui.theme.LocalLimanColors
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ToolItem(val label: String, val icon: ImageVector, val route: String)

private val tools = listOf(
    ToolItem("Nefes", Icons.Rounded.SelfImprovement, Routes.TOOL_BREATHING),
    ToolItem("Düşünce", Icons.Rounded.Psychology, Routes.TOOL_THOUGHT),
    ToolItem("Şükran", Icons.Rounded.VolunteerActivism, Routes.TOOL_GRATITUDE),
    ToolItem("Kapsül", Icons.Rounded.Schedule, Routes.TOOL_CAPSULE),
    ToolItem("Takvim", Icons.Rounded.CalendarMonth, Routes.TOOL_CALENDAR),
    ToolItem("Sakinleş", Icons.Rounded.Spa, Routes.CALM),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    viewModel: LimanViewModel,
    onNewJournal: () -> Unit,
    onOpenTool: (String) -> Unit,
) {
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val copy = LocalCopy.current
    var reading by remember { mutableStateOf<JournalEntry?>(null) }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding(extraBottom = 72)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ben", style = MaterialTheme.typography.displaySmall, modifier = Modifier.weight(1f))
            EncryptedBadge()
        }

        // Birincil aksiyon: Bugüne yaz
        LimanCard(
            Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onNewJournal,
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Bugüne yaz", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        copy.meWriteDesc,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.EditNote, null)
                }
            }
        }

        // Araç şeridi
        SectionHeader("Araçlar", subtitle = copy.toolsSubtitle)
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tools.forEach { tool ->
                ToolChip(tool) { onOpenTool(tool.route) }
            }
        }

        // Geçmiş yazılar
        SectionHeader("Geçmiş yazılar", subtitle = "${journals.size} kayıt")
        if (journals.isEmpty()) {
            LimanCard(Modifier.fillMaxWidth()) {
                Text(
                    copy.meEmptyJournals,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                journals.forEach { entry ->
                    JournalCard(entry) { reading = entry }
                }
            }
        }
    }

    if (reading != null) {
        val entry = reading!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { reading = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.title.ifBlank { "Günlük" },
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    entry.moodFace?.let { Text(it.emoji, style = MaterialTheme.typography.headlineSmall) }
                }
                Text(
                    entry.timestamp.format(longDateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(entry.body, style = JournalBodyStyle)
            }
        }
    }
}

@Composable
private fun EncryptedBadge() {
    Row(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .background(LocalLimanColors.current.bondContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Lock,
            null,
            tint = LocalLimanColors.current.onBondContainer,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Uçtan uca şifreli",
            style = MaterialTheme.typography.labelMedium,
            color = LocalLimanColors.current.onBondContainer,
        )
    }
}

@Composable
private fun ToolChip(tool: ToolItem, onClick: () -> Unit) {
    Column(
        Modifier
            .width(84.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .bounceClick { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(tool.label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun JournalCard(entry: JournalEntry, onClick: () -> Unit) {
    LimanCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.title.ifBlank { "Günlük" },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                entry.moodFace?.let { Text(it.emoji) }
            }
            Text(
                entry.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.timestamp.format(shortDateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (entry.photoCount > 0) {
                    Icon(Icons.Rounded.PhotoLibrary, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text("${entry.photoCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                }
                if (entry.hasAudio) {
                    Icon(Icons.Rounded.Mic, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
private val longDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy · HH:mm", Locale("tr"))
