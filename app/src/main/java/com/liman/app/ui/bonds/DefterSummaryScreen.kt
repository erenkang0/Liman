package com.liman.app.ui.bonds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.ClientStatus
import com.liman.app.data.model.Contact
import com.liman.app.data.model.RiskLevel
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.AnimatedEntrance
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.QuillIcon
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.screenPadding
import java.time.LocalDate

@Composable
fun DefterSummaryScreen(
    viewModel: LimanViewModel,
    onOpenContact: (String) -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    val active = contacts.count { it.status == ClientStatus.ACTIVE }
    val totalSessions = contacts.sumOf { it.sessionCount }
    val thisWeek = contacts
        .filter { (it.daysUntilNextSession(today) ?: -1L) in 0..7 }
        .sortedBy { it.daysUntilNextSession(today) }
    val highRisk = contacts.filter { it.risk == RiskLevel.HIGH || it.risk == RiskLevel.MEDIUM }
        .sortedByDescending { it.risk.ordinal }
    val neglected = contacts
        .filter { it.status == ClientStatus.ACTIVE }
        .sortedByDescending { it.daysSinceContact(today) ?: Long.MAX_VALUE }
        .take(5)

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding(extraBottom = 90)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("Pano", style = MaterialTheme.typography.displaySmall)

        if (contacts.isEmpty()) {
            LimanCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    QuillIcon(modifier = Modifier.size(40.dp).padding(end = 14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Danışan ekledikçe burada klinik panonu görürsün: randevular, riskli dosyalar ve seans özeti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Column
        }

        AnimatedEntrance {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Aktif danışan", "$active", Modifier.weight(1f))
                StatCard("Bu hafta", "${thisWeek.size}", Modifier.weight(1f))
                StatCard("Toplam seans", "$totalSessions", Modifier.weight(1f))
            }
        }

        if (thisWeek.isNotEmpty()) {
            AnimatedEntrance(delayMillis = 60) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Bu haftaki randevular")
                    thisWeek.forEach { c ->
                        val d = c.daysUntilNextSession(today) ?: 0
                        ClientRow(
                            c,
                            trailing = when (d) { 0L -> "bugün"; 1L -> "yarın"; else -> "$d gün" },
                            onOpenContact = onOpenContact,
                        )
                    }
                }
            }
        }

        if (highRisk.isNotEmpty()) {
            AnimatedEntrance(delayMillis = 110) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Risk takibi", subtitle = "Orta/yüksek risk dosyaları")
                    highRisk.forEach { c ->
                        ClientRow(c, trailing = c.risk.label, onOpenContact = onOpenContact)
                    }
                }
            }
        }

        if (neglected.isNotEmpty()) {
            AnimatedEntrance(delayMillis = 160) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Uzun süredir görülmeyen", subtitle = "Aktif ama uzak kalan danışanlar")
                    neglected.forEach { c ->
                        val since = c.daysSinceContact(today)
                        ClientRow(
                            c,
                            trailing = when (since) { null -> "seans yok"; 0L -> "bugün"; 1L -> "dün"; else -> "$since gün" },
                            onOpenContact = onOpenContact,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientRow(contact: Contact, trailing: String, onOpenContact: (String) -> Unit) {
    LimanCard(Modifier.fillMaxWidth(), onClick = { onOpenContact(contact.id) }) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Avatar(contact, size = 40)
                if (contact.risk == RiskLevel.HIGH) {
                    Box(
                        Modifier.size(12.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .align(Alignment.BottomEnd),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${contact.status.label} · ${contact.sessionCount} seans",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    LimanCard(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
