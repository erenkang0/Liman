package com.liman.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WavingHand
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.components.AnimatedEntrance
import com.liman.app.ui.components.LimanCard
import com.liman.app.ui.components.LockBadge
import com.liman.app.ui.components.MoodFaceRow
import com.liman.app.ui.components.SectionHeader
import com.liman.app.ui.components.bounceClick
import com.liman.app.ui.components.screenPadding
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.insight.journalStreak
import com.liman.app.ui.navigation.Routes
import com.liman.app.ui.theme.LocalLimanColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: LimanViewModel,
    onOpenSettings: () -> Unit,
    onQuickMood: () -> Unit,
    onOpenInnerWorld: () -> Unit,
    onOpenBonds: () -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenContact: (String) -> Unit,
    onOpenCalm: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val journals by viewModel.repository.journals.collectAsStateWithLifecycle()
    val contacts by viewModel.repository.contacts.collectAsStateWithLifecycle()
    val limanColors = LocalLimanColors.current
    val copy = LocalCopy.current

    var justLogged by remember { mutableStateOf<MoodFace?>(null) }

    val today = LocalDate.now()
    val name = settings.profile.name
    val streak = remember(journals) { journalStreak(journals.map { it.timestamp.toLocalDate() }) }

    val upcomingBirthday = remember(contacts) {
        contacts.filter { it.birthday != null }
            .minByOrNull { it.daysUntilBirthday(today) ?: Long.MAX_VALUE }
    }
    val distant = remember(contacts) {
        contacts.filter { (it.daysSinceContact(today) ?: 0) > 21 }
            .maxByOrNull { it.daysSinceContact(today) ?: 0 }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding(extraBottom = 72)),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Karşılama
        AnimatedEntrance {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting(name), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        today.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Rounded.Search, contentDescription = "Ara", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(4.dp))
                ProfileAvatar(
                    photoUri = settings.profile.photoUri,
                    initial = name.firstOrNull()?.uppercase() ?: "",
                    onClick = onOpenSettings,
                )
            }
        }

        // Hızlı ruh hali
        AnimatedEntrance(delayMillis = 70) {
            LimanCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text(copy.moodQuestion, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))
                    MoodFaceRow(
                        selected = justLogged,
                        onSelect = { face ->
                            justLogged = face
                            viewModel.repository.addMood(face)
                        },
                    )
                    AnimatedVisibility(visible = justLogged != null) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    copy.moodSaved,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = limanColors.bondAccent,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    "Detay ekle →",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.bounceClick { onQuickMood() },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sakinleş kısayolu
        AnimatedEntrance(delayMillis = 110) {
            LimanCard(
                Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onOpenCalm,
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Spa, null, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Bunaldın mı? Bir an dur.", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Sakinleş — topraklanma, nefes ve hızlı destek",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                }
            }
        }

        // Hızlı araç şeridi
        AnimatedEntrance(delayMillis = 140) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTool("Nefes", Icons.Rounded.SelfImprovement, Modifier.weight(1f)) {
                    onOpenTool(Routes.TOOL_BREATHING)
                }
                QuickTool("Şükran", Icons.Rounded.Favorite, Modifier.weight(1f)) {
                    onOpenTool(Routes.TOOL_GRATITUDE)
                }
                QuickTool("Takvim", Icons.Rounded.CalendarMonth, Modifier.weight(1f)) {
                    onOpenTool(Routes.TOOL_CALENDAR)
                }
            }
        }

        // İç dünyan kartı (kilitli)
        AnimatedEntrance(delayMillis = 210) {
            LimanCard(
                Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onOpenInnerWorld,
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(copy.innerWorldTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        LockBadge(size = 20)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = limanColors.streakGold)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (streak > 0) "$streak günlük seri" else "Bugün ilk satırını yazabilirsin",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        copy.innerWorldDesc,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Bağların kartı
        AnimatedEntrance(delayMillis = 280) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Bağların", subtitle = copy.bondsSubtitle)
                LimanCard(Modifier.fillMaxWidth(), onClick = onOpenBonds) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (upcomingBirthday != null) {
                            val days = upcomingBirthday.daysUntilBirthday(today) ?: 0
                            BondRow(
                                icon = Icons.Rounded.Cake,
                                title = upcomingBirthday.name,
                                subtitle = when (days) {
                                    0L -> "Bugün doğum günü!"
                                    1L -> "Yarın doğum günü"
                                    else -> "$days gün sonra doğum günü"
                                },
                                onClick = { onOpenContact(upcomingBirthday.id) },
                            )
                        }
                        if (distant != null) {
                            val days = distant.daysSinceContact(today) ?: 0
                            BondRow(
                                icon = Icons.Rounded.Favorite,
                                title = distant.name,
                                subtitle = "$days gündür konuşmadınız — bir merhaba?",
                                onClick = { onOpenContact(distant.id) },
                            )
                        }
                        if (upcomingBirthday == null && distant == null) {
                            Text(
                                copy.bondsAllGood,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(photoUri: String?, initial: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .bounceClick { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        when {
            photoUri != null -> AsyncImage(
                model = photoUri,
                contentDescription = "Profil — ayarlar",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            initial.isBlank() -> Icon(Icons.Rounded.WavingHand, "Ayarlar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            else -> Text(
                initial,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun QuickTool(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    LimanCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = onClick,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, null)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun BondRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().bounceClick { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LocalLimanColors.current.bondContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = LocalLimanColors.current.onBondContainer, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM EEEE", Locale("tr"))

private fun greeting(name: String): String {
    val h = LocalTime.now().hour
    val base = when (h) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        in 18..21 -> "İyi akşamlar"
        else -> "İyi geceler"
    }
    return if (name.isBlank()) base else "$base, $name"
}
