package com.liman.app.ui.me.rich

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.liman.app.data.model.JournalFont
import com.liman.app.data.model.StyleSpan
import kotlin.math.max
import kotlin.math.min

fun JournalFont.toFontFamily(): FontFamily = when (this) {
    JournalFont.SERIF -> FontFamily.Serif
    JournalFont.SANS -> FontFamily.SansSerif
    JournalFont.MONO -> FontFamily.Monospace
}

fun StyleSpan.toSpanStyle(): SpanStyle = SpanStyle(
    fontWeight = if (bold) FontWeight.Bold else null,
    fontStyle = if (italic) FontStyle.Italic else null,
    textDecoration = if (underline) TextDecoration.Underline else null,
)

/** Düz metin + [spans] → biçimli AnnotatedString (görüntüleme için). */
fun buildJournalAnnotated(text: String, spans: List<StyleSpan>): AnnotatedString =
    buildAnnotatedString {
        append(text)
        val len = text.length
        spans.forEach { sp ->
            val s = sp.start.coerceIn(0, len)
            val e = sp.end.coerceIn(0, len)
            if (e > s) addStyle(sp.toSpanStyle(), s, e)
        }
    }

/**
 * Yazarken biçimi anlık gösterir; alttaki metin düz kalır (yıldız/işaret yok).
 * Uzunluğu değiştirmediği için kimlik (identity) offset eşlemesi kullanır.
 */
class RichVisualTransformation(private val spans: List<StyleSpan>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val len = text.length
        val styled = buildAnnotatedString {
            append(text.text)
            spans.forEach { sp ->
                val s = sp.start.coerceIn(0, len)
                val e = sp.end.coerceIn(0, len)
                if (e > s) addStyle(sp.toSpanStyle(), s, e)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

/**
 * Zengin metin editörü durumu. Metin düz tutulur; [spans] biçimi taşır. Biçim
 * butonları seçili metne uygulanır; seçim yoksa "aktif" bayraklar sonraki yazılan
 * karakterlere uygulanır.
 */
class RichTextState(
    initialText: String = "",
    initialSpans: List<StyleSpan> = emptyList(),
    initialFont: JournalFont = JournalFont.SERIF,
) {
    var value by mutableStateOf(TextFieldValue(initialText))
        private set
    var spans by mutableStateOf(initialSpans)
        private set
    var font by mutableStateOf(initialFont)
    var boldActive by mutableStateOf(false)
        private set
    var italicActive by mutableStateOf(false)
        private set
    var underlineActive by mutableStateOf(false)
        private set

    val text: String get() = value.text

    fun onValueChange(new: TextFieldValue) {
        val oldText = value.text
        val newText = new.text
        if (oldText == newText) {
            value = new
            return
        }

        // En uzun ortak ön/son ekleri bul → değişen aralık.
        var prefix = 0
        val maxPrefix = min(oldText.length, newText.length)
        while (prefix < maxPrefix && oldText[prefix] == newText[prefix]) prefix++

        var suffix = 0
        while (
            suffix < (oldText.length - prefix) &&
            suffix < (newText.length - prefix) &&
            oldText[oldText.length - 1 - suffix] == newText[newText.length - 1 - suffix]
        ) suffix++

        val removedStart = prefix
        val removedEnd = oldText.length - suffix
        val insertedStart = prefix
        val insertedEnd = newText.length - suffix
        val delta = newText.length - oldText.length

        val shifted = spans.mapNotNull { sp -> shift(sp, removedStart, removedEnd, delta) }.toMutableList()
        if (insertedEnd > insertedStart && (boldActive || italicActive || underlineActive)) {
            shifted += StyleSpan(insertedStart, insertedEnd, boldActive, italicActive, underlineActive)
        }
        spans = shifted
        value = new
    }

    fun setFont(choice: JournalFont) {
        font = choice
    }

    fun toggleBold() = toggle(Flag.BOLD)
    fun toggleItalic() = toggle(Flag.ITALIC)
    fun toggleUnderline() = toggle(Flag.UNDERLINE)

    /** Seçili aralığın (veya tüm) biçimini temizler. */
    fun clearFormatting() {
        val sel = value.selection
        if (sel.collapsed) {
            boldActive = false
            italicActive = false
            underlineActive = false
            return
        }
        val a = min(sel.start, sel.end)
        val b = max(sel.start, sel.end)
        spans = spans.flatMap { sp -> subtract(sp, a, b) }
    }

    private fun toggle(flag: Flag) {
        val sel = value.selection
        if (sel.collapsed) {
            when (flag) {
                Flag.BOLD -> boldActive = !boldActive
                Flag.ITALIC -> italicActive = !italicActive
                Flag.UNDERLINE -> underlineActive = !underlineActive
            }
        } else {
            val a = min(sel.start, sel.end)
            val b = max(sel.start, sel.end)
            spans = spans + StyleSpan(
                a, b,
                bold = flag == Flag.BOLD,
                italic = flag == Flag.ITALIC,
                underline = flag == Flag.UNDERLINE,
            )
        }
    }

    private fun shift(sp: StyleSpan, removedStart: Int, removedEnd: Int, delta: Int): StyleSpan? {
        fun adjust(i: Int): Int = when {
            i <= removedStart -> i
            i >= removedEnd -> i + delta
            else -> removedStart
        }
        val ns = adjust(sp.start)
        val ne = adjust(sp.end)
        return if (ne > ns) sp.copy(start = ns, end = ne) else null
    }

    private fun subtract(sp: StyleSpan, a: Int, b: Int): List<StyleSpan> {
        if (sp.end <= a || sp.start >= b) return listOf(sp)
        val parts = mutableListOf<StyleSpan>()
        if (sp.start < a) parts += sp.copy(end = a)
        if (sp.end > b) parts += sp.copy(start = b)
        return parts
    }

    private enum class Flag { BOLD, ITALIC, UNDERLINE }
}
