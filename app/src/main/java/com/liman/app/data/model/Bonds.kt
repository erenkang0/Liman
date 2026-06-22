package com.liman.app.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Kişiyle ilişkinin "duygusal havası" — renkli nokta olarak gösterilir. */
enum class EmotionalWeather(val label: String, val emoji: String) {
    SUNNY("Güneşli", "☀️"),
    WARM("Ilık", "🌤️"),
    CALM("Sakin", "🌥️"),
    CLOUDY("Bulutlu", "☁️"),
    STORMY("Fırtınalı", "🌧️"),
}

/** İlişki türü etiketi. */
enum class RelationshipType(val label: String) {
    FAMILY("Aile"),
    FRIEND("Arkadaş"),
    PARTNER("Partner"),
    COLLEAGUE("İş arkadaşı"),
    OTHER("Diğer"),
}

/** Kişi profilindeki anılar zaman tüneli öğesi. */
data class Memory(
    val id: String,
    val title: String,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    /** Yerel fotoğraf URI'leri (en fazla 5). İlk fotoğraf "afiş" (kapak) olur. */
    val photos: List<String> = emptyList(),
    val voice: VoiceNote? = null,
) {
    val coverPhoto: String? get() = photos.firstOrNull()
}

data class Contact(
    val id: String,
    val name: String,
    val relationship: RelationshipType = RelationshipType.FRIEND,
    val weather: EmotionalWeather = EmotionalWeather.CALM,
    val bio: String = "",
    val weatherNote: String = "",
    val birthday: LocalDate? = null,
    val lastContact: LocalDate? = null,
    val memories: List<Memory> = emptyList(),
    /** Yerel albüm fotoğrafları (en fazla 5). */
    val photos: List<String> = emptyList(),
    /** Herhangi bir albüm bağlantısı (Google Fotoğraflar, iCloud, vb.). */
    val albumUrl: String? = null,
    /** Avatar baş harfleri için renk tonu (0..1, hue türetmede kullanılır). */
    val avatarSeed: Int = name.hashCode(),
) {
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }

    /** Bugüne kalan gün sayısı (yaklaşan doğum günü için). null ise doğum günü yok. */
    fun daysUntilBirthday(today: LocalDate = LocalDate.now()): Long? {
        val b = birthday ?: return null
        var next = b.withYear(today.year)
        if (next.isBefore(today)) next = next.plusYears(1)
        return ChronoUnit.DAYS.between(today, next)
    }

    fun daysSinceContact(today: LocalDate = LocalDate.now()): Long? {
        val l = lastContact ?: return null
        return ChronoUnit.DAYS.between(l, today)
    }
}
