package com.liman.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 5 yüz ifadeli hızlı ruh hali seçici için temel ifadeler.
 * Görsel (ikon) eşlemesi UI katmanındadır (bkz. ui/components/MoodVisuals).
 * [score] 1 (çok düşük) .. 5 (çok iyi).
 */
enum class MoodFace(val score: Int, val label: String) {
    VERY_LOW(1, "Zor"),
    LOW(2, "İniş"),
    NEUTRAL(3, "Dengede"),
    GOOD(4, "İyi"),
    GREAT(5, "Harika");

    companion object {
        fun fromScore(score: Int): MoodFace = entries.firstOrNull { it.score == score } ?: NEUTRAL
    }
}

/** Duygu yoğunluğu ölçeği (etiketli). */
enum class MoodIntensity(val label: String, val value: Int) {
    VERY_MILD("Çok hafif", 1),
    MILD("Hafif", 2),
    MODERATE("Orta", 3),
    STRONG("Güçlü", 4),
    VERY_STRONG("Çok güçlü", 5),
}

/** Çoklu seçilebilen tetikleyici etiketleri. */
enum class MoodTrigger(val label: String) {
    WORK("İş"),
    FAMILY("Aile"),
    RELATIONSHIP("İlişki"),
    HEALTH("Sağlık"),
    MONEY("Para"),
    SLEEP("Uyku"),
    SELF("Kendim"),
    FRIENDS("Arkadaşlar"),
    WEATHER("Hava"),
    NEWS("Haberler"),
    REST("Dinlenme"),
    ACHIEVEMENT("Başarı"),
}

data class MoodEntry(
    val id: String,
    val face: MoodFace,
    val intensity: MoodIntensity = MoodIntensity.MODERATE,
    val triggers: List<MoodTrigger> = emptyList(),
    val note: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    val date: LocalDate get() = timestamp.toLocalDate()
}
