package com.liman.app.ui.bonds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.Contact
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.QuillIcon
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate

@Composable
fun BondsScreen(
    viewModel: LimanViewModel,
    onAddContact: () -> Unit,
    onOpenContact: (String) -> Unit,
) {
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var editMode by remember { mutableStateOf(false) }

    val upcoming = contacts
        .filter { (it.daysUntilBirthday(today) ?: Long.MAX_VALUE) <= 60 }
        .sortedBy { it.daysUntilBirthday(today) }

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Defterim", style = MaterialTheme.typography.displaySmall)
                    Text(
                        "Hakkında yazdığın kişiler",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (contacts.isNotEmpty()) {
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(
                            if (editMode) Icons.Rounded.Done else Icons.Rounded.Edit,
                            contentDescription = if (editMode) "Bitti" else "Düzenle",
                            tint = if (editMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (upcoming.isNotEmpty()) {
            item { SectionHeader("Yaklaşan doğum günleri") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(upcoming, key = { it.id }) { contact ->
                        BirthdayCard(contact, today) { onOpenContact(contact.id) }
                    }
                }
            }
        }

        if (contacts.isEmpty()) {
            item {
                LimanCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        QuillIcon(modifier = Modifier.size(40.dp), tint = LocalLimanColors.current.bondAccent)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "Henüz kimseyi eklemedin. Sağ alttaki + ile bir kişi ekle; ona özel bir defter açılır.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        } else {
            item { SectionHeader("Kişiler", subtitle = "${contacts.size} defter") }
            items(contacts, key = { it.id }) { contact ->
                ContactRow(
                    contact = contact,
                    today = today,
                    editMode = editMode,
                    onClick = { onOpenContact(contact.id) },
                    onDelete = { viewModel.repository.deleteContact(contact.id) },
                )
            }
        }
    }
}

@Composable
private fun BirthdayCard(contact: Contact, today: LocalDate, onClick: () -> Unit) {
    val days = contact.daysUntilBirthday(today) ?: 0
    LimanCard(
        Modifier.width(140.dp),
        containerColor = LocalLimanColors.current.bondContainer,
        contentColor = LocalLimanColors.current.onBondContainer,
        onClick = onClick,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Avatar(contact, size = 52)
            Icon(Icons.Rounded.Cake, null, modifier = Modifier.height(18.dp))
            Text(contact.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 1)
            Text(
                when (days) {
                    0L -> "Bugün!"
                    1L -> "Yarın"
                    else -> "$days gün"
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    today: LocalDate,
    editMode: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    LimanCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(contact, size = 50)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(contact.name, style = MaterialTheme.typography.titleMedium)
                    if (contact.title.isNotBlank()) {
                        Text(
                            contact.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    val since = contact.daysSinceContact(today)
                    Text(
                        when {
                            since == null -> "${contact.relationship.label} · ${contact.entries.size} not"
                            since == 0L -> "${contact.relationship.label} · bugün"
                            since == 1L -> "${contact.relationship.label} · dün"
                            else -> "${contact.relationship.label} · $since gün önce"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (editMode) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    ClosenessDots(contact.closeness)
                }
            }
            if (contact.tags.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    contact.tags.take(4).forEach { tag ->
                        AssistChip(
                            onClick = onClick,
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
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

/** Yakınlık/önem seviyesi — 5 nokta. */
@Composable
private fun ClosenessDots(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(5) { i ->
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < level) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
            )
        }
    }
}
