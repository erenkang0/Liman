package com.liman.app.ui.bonds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.AnimatedEntrance
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.QuillIcon
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.screenPadding
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate

@Composable
fun DefterSummaryScreen(
    viewModel: LimanViewModel,
    onOpenContact: (String) -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    val totalNotes = contacts.sumOf { it.entries.size }
    val distant = contacts
        .filter { it.daysSinceContact(today) != null }
        .sortedByDescending { it.daysSinceContact(today) }
        .take(5)
    val tagCounts = contacts.flatMap { it.tags }
        .groupingBy { it }.eachCount()
        .entries.sortedByDescending { it.value }
        .take(10)
    val closest = contacts.sortedByDescending { it.closeness }.take(5)

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding(extraBottom = 90)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("Özet", style = MaterialTheme.typography.displaySmall)

        if (contacts.isEmpty()) {
            LimanCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    QuillIcon(modifier = Modifier.padding(end = 14.dp).height(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Kişi ekledikçe burada defterinin özetini görürsün.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Column
        }

        AnimatedEntrance {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Kişi", "${contacts.size}", Modifier.weight(1f))
                StatCard("Not", "$totalNotes", Modifier.weight(1f))
            }
        }

        if (distant.isNotEmpty()) {
            AnimatedEntrance(delayMillis = 60) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Uzak kalanlar", subtitle = "En uzun süredir görüşmediklerin")
                    distant.forEach { c ->
                        val days = c.daysSinceContact(today) ?: 0
                        LimanCard(Modifier.fillMaxWidth(), onClick = { onOpenContact(c.id) }) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Avatar(c, size = 40)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(c.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        if (c.title.isNotBlank()) c.title else c.relationship.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    when (days) { 0L -> "bugün"; 1L -> "dün"; else -> "$days gün" },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (tagCounts.isNotEmpty()) {
            AnimatedEntrance(delayMillis = 110) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Etiketler")
                    LimanCard(Modifier.fillMaxWidth()) {
                        FlowRow(
                            Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            tagCounts.forEach { (tag, count) ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$tag · $count") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = LocalLimanColors.current.bondContainer,
                                        labelColor = LocalLimanColors.current.onBondContainer,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedEntrance(delayMillis = 160) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("En yakınların")
                closest.forEach { c ->
                    LimanCard(Modifier.fillMaxWidth(), onClick = { onOpenContact(c.id) }) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c, size = 40)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.name, style = MaterialTheme.typography.titleSmall)
                                if (c.title.isNotBlank()) {
                                    Text(c.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text("${c.closeness}/5", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    LimanCard(modifier) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
