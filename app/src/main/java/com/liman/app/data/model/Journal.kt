package com.liman.app.data.model

import java.time.LocalDateTime

/**
 * İç dünya günlüğü kaydı.
 * [body] depolama katmanında uçtan uca şifreli tutulur (bkz. CryptoManager).
 * Bellekte/UI'da düz metin olarak taşınır.
 */
data class JournalEntry(
    val id: String,
    val title: String = "",
    val body: String,
    val moodFace: MoodFace? = null,
    val hasAudio: Boolean = false,
    val photoCount: Int = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    /** Liste önizlemesi için kısa özet. */
    val preview: String
        get() = body.replace("\n", " ").trim().let {
            if (it.length > 120) it.take(120).trimEnd() + "…" else it
        }
}
