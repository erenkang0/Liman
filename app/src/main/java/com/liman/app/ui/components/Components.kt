package com.liman.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liman.app.data.model.Contact
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.MoodFace
import com.liman.app.ui.theme.LocalLimanColors
import kotlinx.coroutines.delay
import kotlin.math.abs

/** Buton/karta basışta yumuşak ölçek — nazik mikro etkileşim. */
@Composable
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, label = "bounce")
    return this
        .scale(scale)
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

/** Yumuşak köşeli, hafif yükseltili temel kart. */
@Composable
fun LimanCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = if (onClick != null) modifier.bounceClick { onClick() } else modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        content()
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}

/** Kişi avatarı — baş harfler + tohumdan türetilmiş sıcak renk. */
@Composable
fun Avatar(
    contact: Contact,
    size: Int = 48,
    modifier: Modifier = Modifier,
) {
    val color = avatarColor(contact.avatarSeed)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            contact.initials,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

fun avatarColor(seed: Int): Color {
    val hue = (abs(seed) % 360).toFloat()
    return Color.hsl(hue, 0.42f, 0.52f)
}

/** Kişinin "duygusal hava" rengini veren nokta. */
@Composable
fun weatherColor(weather: EmotionalWeather): Color = when (weather) {
    EmotionalWeather.SUNNY -> LocalLimanColors.current.moodGreat
    EmotionalWeather.WARM -> LocalLimanColors.current.moodGood
    EmotionalWeather.CALM -> LocalLimanColors.current.bondAccent
    EmotionalWeather.CLOUDY -> LocalLimanColors.current.moodLow
    EmotionalWeather.STORMY -> LocalLimanColors.current.moodVeryLow
}

@Composable
fun WeatherDot(weather: EmotionalWeather, size: Int = 12, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .background(weatherColor(weather)),
    )
}

/** Küçük kilit rozeti. */
@Composable
fun LockBadge(modifier: Modifier = Modifier, size: Int = 16) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(LocalLimanColors.current.lockTint),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Lock,
            contentDescription = "Kilitli",
            tint = Color.White,
            modifier = Modifier.size((size * 0.62f).dp),
        )
    }
}

/** Tek bir yüz ifadeli ruh hali balonu. */
@Composable
fun MoodFaceBubble(
    face: MoodFace,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 54,
) {
    val color = LocalLimanColors.current.moodColor(face.score)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(
                    if (selected) color.copy(alpha = 0.22f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                )
                .then(
                    if (selected) Modifier.border(2.dp, color, CircleShape) else Modifier
                )
                .bounceClick { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                moodIcon(face),
                contentDescription = face.label,
                tint = color,
                modifier = Modifier.size((size * 0.56f).dp),
            )
        }
        Text(
            face.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 5 yüz ifadeli yatay seçici. */
@Composable
fun MoodFaceRow(
    selected: MoodFace?,
    onSelect: (MoodFace) -> Unit,
    modifier: Modifier = Modifier,
    bubbleSize: Int = 54,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MoodFace.entries.forEach { face ->
            MoodFaceBubble(
                face = face,
                selected = selected == face,
                onClick = { onSelect(face) },
                size = bubbleSize,
            )
        }
    }
}

/**
 * İçerik ilk göründüğünde nazikçe belirir (yumuşak fade + yukarı kayma).
 * [delayMillis] ile kademeli (staggered) giriş sağlanır.
 */
@Composable
fun AnimatedEntrance(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        state.targetState = true
    }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 6 },
    ) {
        content()
    }
}

@Composable
fun SpacerH(height: Int) = Spacer(Modifier.height(height.dp))

/** Boş durum yer tutucusu — şefkatli dil. */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, style = MaterialTheme.typography.displaySmall)
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Detay ekranları için sade üst çubuk başlığı + geri düğmesi. */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri")
    }
}

/** İçerik için ortak yatay/dikey iç boşluk. */
fun screenPadding(extraBottom: Int = 0): PaddingValues =
    PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = (24 + extraBottom).dp)

/** Metni güvenle tek/çift satıra kırpan yardımcı. */
@Composable
fun EllipsizedText(
    text: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
