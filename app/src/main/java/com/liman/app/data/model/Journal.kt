@file:UseSerializers(LocalDateTimeSerializer::class)

package com.liman.app.data.model

import com.liman.app.data.local.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

/** Günlük metni için yazı tipi seçenekleri. */
@Serializable
enum class JournalFont(val label: String) {
    SERIF("Zarif"),
    SANS("Modern"),
    MONO("Daktilo"),
}

/**
 * Zengin metin biçimlendirme aralığı. [start, end) yarı-açık aralığı; metin düz
 * (asterisksiz) saklanır, biçim ayrı meta veridir — kullanıcı yazarken anlık olarak
 * uygulanır, ekranda yıldız/işaret görünmez.
 */
@Serializable
data class StyleSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
)

/** Sesli not. [path] cihazdaki dosya yolu. */
@Serializable
data class VoiceNote(
    val path: String,
    val durationMs: Long,
)

/**
 * İç dünya günlüğü kaydı.
 * [body] depolama katmanında uçtan uca şifreli tutulur (bkz. CryptoManager);
 * bellekte/UI'da düz metin olarak taşınır. Biçim [spans] içinde tutulur.
 */
@Serializable
data class JournalEntry(
    val id: String,
    val title: String = "",
    val body: String,
    val spans: List<StyleSpan> = emptyList(),
    val font: JournalFont = JournalFont.SERIF,
    val moodFace: MoodFace? = null,
    val voice: VoiceNote? = null,
    /** Yerel fotoğraf URI'leri (en fazla 5). İlk fotoğraf "afiş" (kapak) olur. */
    val photos: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    val coverPhoto: String? get() = photos.firstOrNull()

    /** Liste önizlemesi için kısa özet. */
    val preview: String
        get() = body.replace("\n", " ").trim().let {
            if (it.length > 120) it.take(120).trimEnd() + "…" else it
        }
}
