package com.liman.app.ui.copy

import androidx.compose.runtime.staticCompositionLocalOf
import com.liman.app.data.model.Gender

/**
 * Cinsiyete duyarlı yazı tonu.
 *
 * Uygulamanın varsayılan dili oldukça nazik/şefkatli. Bu ton bazı kullanıcılara
 * (özellikle "Erkek" seçen kullanıcılara) fazla yumuşak gelebildiği için, daha
 * sade, dolaysız ve yere basan bir alternatif ton sunulur. İçerik aynı sıcaklıkta
 * kalır; yalnızca üslup değişir.
 */
enum class Tone { GENTLE, GROUNDED }

fun toneFor(gender: Gender?): Tone =
    if (gender == Gender.MALE) Tone.GROUNDED else Tone.GENTLE

/** Tona göre değişen tüm kullanıcı metinleri tek yerde. */
interface LimanCopy {
    val moodQuestion: String
    val moodSaved: String
    val innerWorldTitle: String
    val innerWorldDesc: String
    val bondsSubtitle: String
    val bondsAllGood: String
    val meWriteDesc: String
    val meEmptyJournals: String
    val toolsSubtitle: String
    val journalPlaceholder: String
    val journalPromptsHint: String
    val journalPrompts: List<String>
    val gratitudeIntro: String
    val gratitudeEmpty: String
    val lockInnerSubtitle: String
    val notificationsNote: String
    val calmIntro: String
    val calmReassure: String
}

object GentleCopy : LimanCopy {
    override val moodQuestion = "Bugün nasıl hissediyorsun?"
    override val moodSaved = "Kaydedildi 🌱"
    override val innerWorldTitle = "İç dünyan"
    override val innerWorldDesc = "Bugüne yaz — savunmasız, mahrem, yalnızca senin."
    override val bondsSubtitle = "Sıcak tutmaya değer ilişkiler"
    override val bondsAllGood = "Şimdilik her şey yolunda görünüyor. 💛"
    override val meWriteDesc = "Aklından geçeni, olduğu gibi. Burası yalnızca senin."
    override val meEmptyJournals =
        "Henüz bir şey yazmadın — ve bu tamamen senin tempon. Hazır olduğunda buradayız."
    override val toolsSubtitle = "Kendine bakım için küçük duraklar"
    override val journalPlaceholder = "Bugün aklından ne geçiyor? Olduğu gibi yazabilirsin…"
    override val journalPromptsHint = "Bir başlangıç ister misin?"
    override val journalPrompts = listOf(
        "Bugün seni en çok ne zorladı?",
        "Küçük de olsa neye minnettarsın?",
        "Şu an bedeninde ne hissediyorsun?",
        "Kendine söylemek istediğin nazik bir şey?",
        "Bugünü bir kelimeyle anlatsan?",
    )
    override val gratitudeIntro =
        "Seri yok, baskı yok. Sadece bugün fark ettiğin küçük güzellikler."
    override val gratitudeEmpty = "Henüz bir şey eklemedin. Acelesi yok. 🌿"
    override val lockInnerSubtitle =
        "Ham, savunmasız duyguların güvende. Devam etmek için doğrula"
    override val notificationsNote =
        "Tek bir saat yok — her bildirimin kendi ayarı var. Hiçbir şey yazmazsan seni yargılamayız."
    override val calmIntro = "Bir an dur. Buradasın ve nefes alıyorsun. Acelesi yok."
    override val calmReassure =
        "Bu duygu da geçecek. Şu an güvendesin. Kendine biraz şefkat göster."
}

object GroundedCopy : LimanCopy {
    override val moodQuestion = "Bugün ne durumdasın?"
    override val moodSaved = "Kaydedildi 👍"
    override val innerWorldTitle = "Senin alanın"
    override val innerWorldDesc = "Bugüne yaz — tamamen sana özel, kimse görmez."
    override val bondsSubtitle = "İletişimde kalmaya değer kişiler"
    override val bondsAllGood = "Şu an her şey yolunda. 👍"
    override val meWriteDesc = "Aklından geçeni yaz. Burası sana özel, kimse okumaz."
    override val meEmptyJournals = "Henüz yazı yok. Canın istediğinde başlarsın."
    override val toolsSubtitle = "Kafanı toparlamak için araçlar"
    override val journalPlaceholder = "Bugün ne var ne yok? Aklındakini yaz…"
    override val journalPromptsHint = "Başlangıç lazım mı?"
    override val journalPrompts = listOf(
        "Bugün seni en çok ne yordu?",
        "İyi giden bir şey oldu mu?",
        "Şu an kafanda ne var?",
        "Kendine ne söylersin?",
        "Bugünü tek kelimeyle anlat.",
    )
    override val gratitudeIntro = "Seri yok, baskı yok. Bugün iyi giden birkaç şey."
    override val gratitudeEmpty = "Henüz bir şey yok. Acelesi yok. 👍"
    override val lockInnerSubtitle = "Notların sana özel. Devam etmek için doğrula"
    override val notificationsNote =
        "Tek bir saat yok — her bildirimin kendi ayarı var. Yazmazsan da sorun yok."
    override val calmIntro = "Bir dakika dur. Buradasın, nefes al. Acelesi yok."
    override val calmReassure =
        "Bu da geçer. Şu an güvendesin. Kendine biraz alan tanı."
}

fun copyFor(gender: Gender?): LimanCopy =
    if (toneFor(gender) == Tone.GROUNDED) GroundedCopy else GentleCopy

val LocalCopy = staticCompositionLocalOf<LimanCopy> { GentleCopy }
