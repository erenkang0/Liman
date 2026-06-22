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

/** Danışanın klinik risk düzeyi. */
@Serializable
enum class RiskLevel(val label: String) {
    NONE("Risk yok"),
    LOW("Düşük"),
    MEDIUM("Orta"),
    HIGH("Yüksek"),
}

/** Danışan dosyasının durumu. */
@Serializable
enum class ClientStatus(val label: String) {
    ACTIVE("Aktif"),
    PAUSED("Beklemede"),
    COMPLETED("Tamamlandı"),
}

/** Defter kaydının türü: serbest not ya da yapılandırılmış seans (SOAP). */
@Serializable
enum class EntryKind(val label: String) {
    NOTE("Not"),
    SESSION("Seans"),
}

/** @bağlantı hedef türü — iki uygulamayı (Defter/Liman) birbirine bağlar. */
@Serializable
enum class MentionType { CONTACT, NOTE, JOURNAL }

/**
 * Metin içinde "@..." ile kurulan, başka bir kayda işaret eden bağlantı.
 * [label] @ ile gösterilen ad; tıklanınca ilgili kayda gidilir.
 */
@Serializable
data class Mention(
    val type: MentionType,
    /** Hedef id (CONTACT: kişi id, JOURNAL: günlük id, NOTE: defter kaydı id). */
    val id: String,
    /** NOTE için kaydın ait olduğu kişi id'si. */
    val contactId: String = "",
    val label: String,
)

/** Tedavi planı hedefi. */
@Serializable
data class TreatmentGoal(
    val id: String,
    val text: String,
    val done: Boolean = false,
)

/**
 * Kişiye özel defterdeki tek bir kayıt — o kişi hakkında belirli bir günde
 * yazdığın görüş/gözlem/anı. Metin esastır; foto/ses/his opsiyoneldir.
 */
@Serializable
data class NotebookEntry(
    val id: String,
    val kind: EntryKind = EntryKind.NOTE,
    val title: String = "",
    val text: String = "",
    /** Yapılandırılmış seans notu (SOAP). Yalnızca [kind] == SESSION iken kullanılır. */
    val subjective: String = "",   // S — Danışanın aktardıkları
    val objective: String = "",    // O — Gözlemlerin
    val assessment: String = "",   // A — Değerlendirme / formülasyon
    val plan: String = "",         // P — Plan / ödev
    val durationMin: Int? = null,  // Seans süresi (dk)
    /** O an o kişiye/duruma dair hissin/danışanın ruh hali (opsiyonel). */
    val feeling: MoodFace? = null,
    val date: LocalDate = LocalDate.now(),
    /** Yerel fotoğraf URI'leri (en fazla 5). İlk fotoğraf kapak olur. */
    val photos: List<String> = emptyList(),
    val voice: VoiceNote? = null,
    val tags: List<String> = emptyList(),
    /** Metin içindeki "@" bağlantıları (diğer kayıtlara). */
    val mentions: List<Mention> = emptyList(),
) {
    val coverPhoto: String? get() = photos.firstOrNull()

    /** Liste önizlemesi için kısa özet. */
    val preview: String
        get() = listOf(text, subjective, assessment, plan, objective)
            .firstOrNull { it.isNotBlank() }.orEmpty()
            .replace("\n", " ").trim().let {
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
    /** Başvuru nedeni / kısa klinik not. */
    val bio: String = "",
    /** Serbest etiketler / temalar (örn. "anksiyete", "yas", "ilişki"). */
    val tags: List<String> = emptyList(),
    /** Yakınlık/önem (1..5). */
    val closeness: Int = 3,
    /* ----- Profesyonel (klinik) alanlar ----- */
    val risk: RiskLevel = RiskLevel.NONE,
    val status: ClientStatus = ClientStatus.ACTIVE,
    /** İlk görüşme / dosya açılış tarihi. */
    val intakeDate: LocalDate? = null,
    /** Planlanan sonraki seans. */
    val nextSession: LocalDate? = null,
    /** Tedavi planı hedefleri. */
    val goals: List<TreatmentGoal> = emptyList(),
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

    /** Sonraki seansa kalan gün (geçmişse negatif). null = randevu yok. */
    fun daysUntilNextSession(today: LocalDate = LocalDate.now()): Long? {
        val n = nextSession ?: return null
        return ChronoUnit.DAYS.between(today, n)
    }

    /** Toplam seans (yapılandırılmış) kaydı sayısı. */
    val sessionCount: Int get() = entries.count { it.kind == EntryKind.SESSION }
}
