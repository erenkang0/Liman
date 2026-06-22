package com.liman.app.data.export

import com.liman.app.data.model.Contact
import com.liman.app.data.model.GratitudeEntry
import com.liman.app.data.model.JournalEntry
import com.liman.app.data.model.MoodEntry
import com.liman.app.data.model.TimeCapsule
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr"))
private val dateTimeFmt = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("tr"))

/**
 * Tüm yerel verinin insan-okur düz metin dışa aktarımı. Cihaz dışına yalnızca
 * kullanıcı paylaş'a basınca çıkar.
 */
fun buildExportText(
    name: String,
    moods: List<MoodEntry>,
    journals: List<JournalEntry>,
    contacts: List<Contact>,
    gratitude: List<GratitudeEntry>,
    capsules: List<TimeCapsule>,
): String = buildString {
    appendLine("LİMAN — Veri Dışa Aktarımı")
    if (name.isNotBlank()) appendLine("Kişi: $name")
    appendLine("=".repeat(28))
    appendLine()

    appendLine("RUH HALİ (${moods.size})")
    moods.take(60).forEach {
        appendLine("• ${it.timestamp.format(dateTimeFmt)} — ${it.face.label} (${it.intensity.label})")
        if (it.note.isNotBlank()) appendLine("    ${it.note}")
    }
    appendLine()

    appendLine("GÜNLÜK (${journals.size})")
    journals.forEach {
        appendLine("• ${it.timestamp.format(dateTimeFmt)} — ${it.title.ifBlank { "Günlük" }}")
        if (it.body.isNotBlank()) appendLine("    ${it.preview}")
    }
    appendLine()

    appendLine("KİŞİLER / DEFTER (${contacts.size})")
    contacts.forEach { c ->
        val titlePart = if (c.title.isNotBlank()) " — ${c.title}" else ""
        appendLine("• ${c.name}$titlePart (${c.relationship.label})")
        if (c.tags.isNotEmpty()) appendLine("    Etiketler: ${c.tags.joinToString(", ")}")
        c.birthday?.let { appendLine("    Doğum günü: ${it.format(dateFmt)}") }
        c.lastContact?.let { appendLine("    Son temas: ${it.format(dateFmt)}") }
        if (c.entries.isNotEmpty()) appendLine("    Defter notları: ${c.entries.size}")
    }
    appendLine()

    appendLine("ŞÜKRAN (${gratitude.size})")
    gratitude.forEach { g ->
        appendLine("• ${g.timestamp.format(dateFmt)}: ${g.items.joinToString(", ")}")
    }
    appendLine()

    appendLine("ZAMAN KAPSÜLLERİ (${capsules.size})")
    capsules.forEach { cap ->
        appendLine("• ${cap.title} — açılış ${cap.openOn.format(dateFmt)}${if (cap.sealed) " (mühürlü)" else ""}")
    }
}
