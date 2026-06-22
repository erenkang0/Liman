package com.liman.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.Avatar
import com.liman.app.ui.components.BackButton
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.SectionHeader

@Composable
fun SearchScreen(
    viewModel: LimanViewModel,
    onBack: () -> Unit,
    onOpenJournal: (String) -> Unit,
    onOpenContact: (String) -> Unit,
) {
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val gratitude by viewModel.repository.gratitude.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val journalHits = if (q.isBlank()) emptyList() else journals.filter {
        it.title.lowercase().contains(q) || it.body.lowercase().contains(q)
    }
    val contactHits = if (q.isBlank()) emptyList() else contacts.filter {
        it.name.lowercase().contains(q) || it.bio.lowercase().contains(q)
    }
    val gratitudeHits = if (q.isBlank()) emptyList() else gratitude.filter { g ->
        g.items.any { it.lowercase().contains(q) }
    }
    val empty = q.isNotBlank() && journalHits.isEmpty() && contactHits.isEmpty() && gratitudeHits.isEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ara") },
                navigationIcon = { BackButton(onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Günlük, bağ veya şükranda ara…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.padding(6.dp))

            if (q.isBlank()) {
                Text(
                    "Yazmaya başla; iç dünyandaki ve bağlarındaki her şeyde arar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (empty) {
                Text(
                    "\"$query\" için sonuç yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (journalHits.isNotEmpty()) {
                        item { SectionHeader("Günlükler", subtitle = "${journalHits.size}") }
                        items(journalHits, key = { "j_${it.id}" }) { entry ->
                            LimanCard(Modifier.fillMaxWidth(), onClick = { onOpenJournal(entry.id) }) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(entry.title.ifBlank { "Günlük" }, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        entry.preview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    if (contactHits.isNotEmpty()) {
                        item { SectionHeader("Bağlar", subtitle = "${contactHits.size}") }
                        items(contactHits, key = { "c_${it.id}" }) { contact ->
                            LimanCard(Modifier.fillMaxWidth(), onClick = { onOpenContact(contact.id) }) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Avatar(contact, size = 40)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(contact.name, style = MaterialTheme.typography.titleSmall)
                                        if (contact.bio.isNotBlank()) {
                                            Text(
                                                contact.bio,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (gratitudeHits.isNotEmpty()) {
                        item { SectionHeader("Şükran", subtitle = "${gratitudeHits.size}") }
                        items(gratitudeHits, key = { "g_${it.id}" }) { g ->
                            LimanCard(Modifier.fillMaxWidth()) {
                                Text(
                                    g.items.joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp),
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.padding(24.dp)) }
                }
            }
        }
    }
}
