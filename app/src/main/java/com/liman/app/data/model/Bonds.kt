@file:UseSerializers(LocalDateSerializer::class)

package com.liman.app.data.model

import com.liman.app.data.local.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** İlişki türü etiketi — küçük bir rozet olarak gösterilir. */
@Serializable
enum class RelationshipType(val label: String) {
    FAMILY("Aile"),
    FRIEND("Arkadaş"),
    PARTNER("Partner"),
    COLLEAGUE("İş arkadaşı"),
    OTHER("Diğer"),
}

/**
 * Kişiye özel defterdeki tek bir kayıt — o kişi hakkında belirli bir günde
 * yazdığın görüş/gözlem/anı. Metin esastır; foto/ses/his opsiyoneldir.
 */
@Serializable
data class NotebookEntry(
    val id: String,
    val title: String = "",
    val text: String = "",
    /** O an o kişiye/duruma dair hissin (opsiyonel). */
    val feeling: MoodFace? = null,
    val date: LocalDate = LocalDate.now(),
    /** Yerel fotoğraf URI'leri (en fazla 5). İlk fotoğraf kapak olur. */
    val photos: List<String> = emptyList(),
    val voice: VoiceNote? = null,
    val tags: List<String> = emptyList(),
) {
    val coverPhoto: String? get() = photos.firstOrNull()

    /** Liste önizlemesi için kısa özet. */
    val preview: String
        get() = text.replace("\n", " ").trim().let {
            if (it.length > 120) it.take(120).trimEnd() + "…" else it
        }
}

@Serializable
data class Contact(
    val id: String,
    val name: String,
    /** Senin verdiğin serbest başlık (örn. "en iyi arkadaşım"). Öne çıkar. */
    val title: String = "",
    val relationship: RelationshipType = RelationshipType.FRIEND,
    val bio: String = "",
    /** Serbest etiketler (örn. "huzur", "sıkıntı", "mesafe"). */
    val tags: List<String> = emptyList(),
    /** Yakınlık/önem (1..5). */
    val closeness: Int = 3,
    /** Kişiye özel aksan rengi (ARGB). null = avatarSeed'den türetilir. */
    val accentColorArgb: Int? = null,
    val birthday: LocalDate? = null,
    val lastContact: LocalDate? = null,
    /** İletişimde kalma hedefi (gün). null = kapalı. */
    val keepInTouchDays: Int? = null,
    /** Kişiye özel defter kayıtları (en yeni başta). */
    val entries: List<NotebookEntry> = emptyList(),
    /** Yerel albüm fotoğrafları (en fazla 5). */
    val photos: List<String> = emptyList(),
    /** Kapak fotoğrafı; null ise [photos] ilki kullanılır. */
    val cover: String? = null,
    /** Herhangi bir albüm bağlantısı (Google Fotoğraflar, iCloud, vb.). */
    val albumUrl: String? = null,
    /** Avatar baş harfleri için renk tonu (0..1, hue türetmede kullanılır). */
    val avatarSeed: Int = name.hashCode(),
) {
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }

    val coverPhoto: String? get() = cover ?: photos.firstOrNull()

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

    /** İletişim hedefini ne kadar aştın (gün)? null = hedef yok / aşılmadı. */
    fun keepInTouchOverdueDays(today: LocalDate = LocalDate.now()): Long? {
        val cadence = keepInTouchDays ?: return null
        val since = daysSinceContact(today) ?: return null
        val over = since - cadence
        return if (over > 0) over else null
    }
}
