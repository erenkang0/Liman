package com.liman.app.ui.insight

import com.liman.app.data.model.MoodEntry
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.MoodTrigger
import java.time.LocalDate

/** Ardışık günlük yazma serisi (bugün ya da dünden geriye). */
fun journalStreak(dates: Collection<LocalDate>, today: LocalDate = LocalDate.now()): Int {
    if (dates.isEmpty()) return 0
    val set = dates.toHashSet()
    var day = if (set.contains(today)) today else today.minusDays(1)
    if (!set.contains(day)) return 0
    var count = 0
    while (set.contains(day)) {
        count++
        day = day.minusDays(1)
    }
    return count
}

/** Son [days] günün ruh hali kayıtları. */
fun moodsInRange(moods: List<MoodEntry>, days: Int, today: LocalDate = LocalDate.now()): List<MoodEntry> {
    val from = today.minusDays((days - 1).toLong())
    return moods.filter { !it.date.isBefore(from) }
}

/** Ortalama ruh hali skoru (1..5), kayıt yoksa null. */
fun moodAverage(moods: List<MoodEntry>): Double? =
    if (moods.isEmpty()) null else moods.map { it.face.score }.average()

/** Günlük ortalama ruh hali eğrisi (eksik günler null). */
fun moodCurve(moods: List<MoodEntry>, days: Int, today: LocalDate = LocalDate.now()): List<Double?> {
    val byDay = moods.groupBy { it.date }
    return (0 until days).map { offset ->
        val day = today.minusDays((days - 1 - offset).toLong())
        byDay[day]?.map { it.face.score }?.average()
    }
}

/** Ruh hali dağılımı (yüz → adet). */
fun moodDistribution(moods: List<MoodEntry>): Map<MoodFace, Int> {
    val counts = moods.groupingBy { it.face }.eachCount()
    return MoodFace.entries.associateWith { counts[it] ?: 0 }
}

/** En sık tetikleyiciler. */
fun topTriggers(moods: List<MoodEntry>, limit: Int = 5): List<Pair<MoodTrigger, Int>> =
    moods.flatMap { it.triggers }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(limit)
        .map { it.key to it.value }

/** Paylaşılabilir kısa içgörü metni. */
fun buildInsightShareText(moods: List<MoodEntry>, name: String): String {
    val recent = moodsInRange(moods, 7)
    val avg = moodAverage(recent)
    val face = avg?.let { MoodFace.fromScore(Math.round(it).toInt()) }
    val who = if (name.isBlank()) "Bu hafta" else "$name · bu hafta"
    return buildString {
        appendLine("🌅 Liman — $who")
        if (avg != null && face != null) {
            appendLine("Ortalama ruh halim: ${face.emoji} ${face.label} (${"%.1f".format(avg)}/5)")
            appendLine("${recent.size} kayıt ile kendime göz kulak oldum.")
        } else {
            appendLine("Bu hafta kendime nazikçe alan açıyorum.")
        }
        append("#Liman")
    }
}
