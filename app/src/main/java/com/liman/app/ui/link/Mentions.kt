package com.liman.app.ui.link

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import com.liman.app.data.model.Contact
import com.liman.app.data.model.EntryKind
import com.liman.app.data.model.JournalEntry
import com.liman.app.data.model.Mention
import com.liman.app.data.model.MentionType
import com.liman.app.ui.navigation.Routes

/* ============================ Motor (engine) ============================ */
/**
 * İki uygulamayı (Defter ↔ Liman) birbirine bağlayan "@bağlantı" motoru.
 * Danışanlar, defter kayıtları ve günlükler arasında arama yapar ve bir
 * [Mention]'ı gidilecek rotaya çözer.
 */

data class MentionCandidate(val mention: Mention, val sub: String)

fun searchMentions(
    query: String,
    contacts: List<Contact>,
    journals: List<JournalEntry>,
    excludeContactId: String? = null,
    limit: Int = 6,
): List<MentionCandidate> {
    val q = query.trim().lowercase()
    val out = mutableListOf<MentionCandidate>()

    contacts.forEach { c ->
        if (q.isEmpty() || c.name.lowercase().contains(q) || c.title.lowercase().contains(q)) {
            out += MentionCandidate(Mention(MentionType.CONTACT, c.id, label = c.name), "Danışan")
        }
    }
    // Defter kayıtları (başlık/içerik) — yalnızca bir şey yazılınca
    if (q.isNotEmpty()) {
        contacts.forEach { c ->
            if (c.id == excludeContactId) return@forEach
            c.entries.forEach { e ->
                val lbl = e.title.ifBlank { if (e.kind == EntryKind.SESSION) "Seans" else "Not" }
                if (lbl.lowercase().contains(q) || e.text.lowercase().contains(q)) {
                    out += MentionCandidate(Mention(MentionType.NOTE, e.id, c.id, lbl), "${c.name} · kayıt")
                }
            }
        }
        journals.forEach { j ->
            val lbl = j.title.ifBlank { "Günlük" }
            if (lbl.lowercase().contains(q) || j.body.lowercase().contains(q)) {
                out += MentionCandidate(Mention(MentionType.JOURNAL, j.id, label = lbl), "Günlük")
            }
        }
    }
    return out.take(limit)
}

/** Bir [Mention]'ı gidilecek rotaya çözer. */
fun mentionRoute(m: Mention): String = when (m.type) {
    MentionType.CONTACT -> Routes.contact(m.id)
    MentionType.JOURNAL -> Routes.journal(m.id)
    MentionType.NOTE -> Routes.note(m.contactId, m.id)
}

/* ----- Metin yardımcıları ----- */

/** İmleçten geriye doğru aktif "@sorgu" varsa onu döndürür. */
private fun activeQuery(text: String, cursor: Int): String? {
    val c = cursor.coerceIn(0, text.length)
    val upTo = text.substring(0, c)
    val m = Regex("(?:^|\\s)@([\\p{L}0-9_]{0,30})$").find(upTo) ?: return null
    return m.groupValues[1]
}

private fun applyMention(tfv: TextFieldValue, mention: Mention): TextFieldValue {
    val cursor = tfv.selection.end.coerceIn(0, tfv.text.length)
    val at = tfv.text.substring(0, cursor).lastIndexOf('@')
    if (at < 0) return tfv
    val insert = "@" + mention.label + " "
    val newText = tfv.text.substring(0, at) + insert + tfv.text.substring(cursor)
    return TextFieldValue(newText, TextRange(at + insert.length))
}

/* ============================ Giriş alanı ============================== */

/**
 * "@" yazınca canlı öneri açan metin alanı. Öneriler yazdıkça filtrelenir;
 * Enter ile (ilk öneri) ya da öneriye dokununca onaylanır ve metne "@Ad"
 * eklenir, [Mention] kaydedilir.
 */
@Composable
fun MentionTextField(
    text: String,
    mentions: List<Mention>,
    onChange: (String, List<Mention>) -> Unit,
    label: String,
    contacts: List<Contact>,
    journals: List<JournalEntry>,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    excludeContactId: String? = null,
) {
    var tfv by remember { mutableStateOf(TextFieldValue(text)) }
    val query = activeQuery(tfv.text, tfv.selection.end)
    val suggestions = if (query != null) searchMentions(query, contacts, journals, excludeContactId) else emptyList()

    fun confirm(c: MentionCandidate) {
        val res = applyMention(tfv, c.mention)
        tfv = res
        val merged = (mentions + c.mention).distinctBy { it.type.name + "|" + it.id + "|" + it.label }
        onChange(res.text, merged)
    }

    Column(modifier) {
        OutlinedTextField(
            value = tfv,
            onValueChange = { nv -> tfv = nv; onChange(nv.text, mentions) },
            label = { Text(label) },
            singleLine = singleLine,
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { e ->
                    if (query != null && suggestions.isNotEmpty() &&
                        e.type == KeyEventType.KeyDown &&
                        (e.key == Key.Enter || e.key == Key.NumPadEnter)
                    ) {
                        confirm(suggestions.first()); true
                    } else false
                },
        )
        if (query != null && suggestions.isNotEmpty()) {
            Surface(
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                    suggestions.forEach { c ->
                        SuggestionRow(c, query) { confirm(c) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(c: MentionCandidate, query: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = when (c.mention.type) {
            MentionType.CONTACT -> Icons.Rounded.Person
            MentionType.JOURNAL -> Icons.AutoMirrored.Rounded.MenuBook
            MentionType.NOTE -> Icons.Rounded.AlternateEmail
        }
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(highlight(c.mention.label, query), style = MaterialTheme.typography.bodyLarge)
            Text(c.sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Eşleşen kısmı kalınlaştırarak öne çıkarır. */
private fun highlight(label: String, query: String): AnnotatedString = buildAnnotatedString {
    val idx = if (query.isBlank()) -1 else label.lowercase().indexOf(query.trim().lowercase())
    if (idx < 0) {
        append(label)
    } else {
        append(label.substring(0, idx))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(label.substring(idx, idx + query.trim().length)) }
        append(label.substring(idx + query.trim().length))
    }
}

/* ============================ Görüntüleme ============================== */

/** Metni gösterir; "@Ad" bağlantılarını renkli + tıklanabilir yapar. */
@Composable
fun MentionText(
    text: String,
    mentions: List<Mention>,
    onOpen: (Mention) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val baseColor = LocalContentColorOrDefault()
    val annotated = remember(text, mentions, linkColor) {
        buildMentionAnnotated(text, mentions, linkColor)
    }
    ClickableText(
        text = annotated,
        style = style.copy(color = baseColor),
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations("mention", offset, offset).firstOrNull()?.let { ann ->
                mentions.getOrNull(ann.item.toIntOrNull() ?: -1)?.let(onOpen)
            }
        },
    )
}

@Composable
private fun LocalContentColorOrDefault(): Color = MaterialTheme.colorScheme.onSurface

private fun buildMentionAnnotated(text: String, mentions: List<Mention>, linkColor: Color): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            if (text[i] == '@') {
                val m = mentions.firstOrNull { text.startsWith("@" + it.label, i) }
                if (m != null) {
                    pushStringAnnotation("mention", mentions.indexOf(m).toString())
                    withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                        append("@" + m.label)
                    }
                    pop()
                    i += 1 + m.label.length
                    continue
                }
            }
            append(text[i])
            i++
        }
    }
