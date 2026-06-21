package com.liman.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/* --------------------------------------------------------------------------
 * Liman renk paleti
 * Ana tema: turuncu "zindelik". Sosyal alan (Bağlar) için yeşil aksan.
 * Açık temada saf beyaz yerine sıcak krem yüzeyler.
 * ------------------------------------------------------------------------ */

// Turuncu (primary) — zindelik
val Ember500 = Color(0xFFD9641A)
val Ember600 = Color(0xFFBE5413)
val Ember200 = Color(0xFFFFB077)
val Ember100 = Color(0xFFFFD9BE)
val EmberContainerLight = Color(0xFFFFDCC4)
val EmberContainerDark = Color(0xFF5C2C0A)

// Yeşil (secondary) — sosyal bağlar
val Sage500 = Color(0xFF4E8D6E)
val Sage300 = Color(0xFF8FC2A6)
val Sage200 = Color(0xFFA8D8BE)
val SageContainerLight = Color(0xFFCDEBD9)
val SageContainerDark = Color(0xFF1E3D2E)

// Tersiyer — sakin mavi-mor (içgörü / sakinleşme)
val Dusk500 = Color(0xFF6C6CA8)
val Dusk200 = Color(0xFFC2C2EC)
val DuskContainerLight = Color(0xFFE2E2F6)
val DuskContainerDark = Color(0xFF333357)

// Sıcak krem yüzeyler (açık tema)
val Cream = Color(0xFFFBF5EA)
val CreamHigh = Color(0xFFFFFCF4)
val CreamCard = Color(0xFFFFFFFF)
val CreamOutline = Color(0xFFE6DDCB)
val CreamOnSurface = Color(0xFF2A2620)
val CreamOnSurfaceVariant = Color(0xFF6E665A)

// Koyu tema yüzeyleri (sıcak siyah-kahve)
val Ink = Color(0xFF14110D)
val InkHigh = Color(0xFF1E1A14)
val InkCard = Color(0xFF24201A)
val InkOutline = Color(0xFF433C32)
val InkOnSurface = Color(0xFFEDE5D8)
val InkOnSurfaceVariant = Color(0xFFBDB3A2)

val ErrorLight = Color(0xFFB3261E)
val ErrorDark = Color(0xFFF2B8B5)

// Akşam / gece "yıldızlı gökyüzü" — hafif koyu mavi paleti
val NightBlueDeep = Color(0xFF12224A)   // gökyüzünün en üstü
val NightBlueMid = Color(0xFF24386E)
val NightBlueSoft = Color(0xFF3C5390)
val StarLight = Color(0xFFFDF6E3)       // sıcak yıldız ışığı
val StarCool = Color(0xFFCBD8FF)

/**
 * Material 3 dışındaki, Liman'a özel marka renkleri.
 * [LocalLimanColors] üzerinden erişilir.
 */
@Immutable
data class LimanColors(
    val bondAccent: Color,          // Bağlar yeşili
    val bondContainer: Color,
    val onBondContainer: Color,
    val streakGold: Color,          // seri / başarı
    val lockTint: Color,            // kilit rozeti
    val moodVeryLow: Color,
    val moodLow: Color,
    val moodNeutral: Color,
    val moodGood: Color,
    val moodGreat: Color,
    val warmGradientTop: Color,
    val warmGradientBottom: Color,
    // Akşam yıldızlı gökyüzü
    val nightSkyTop: Color,
    val nightSkyMid: Color,
    val starColor: Color,
) {
    /** Ruh hali skoruna (1..5) karşılık gelen renk. */
    fun moodColor(score: Int): Color = when (score.coerceIn(1, 5)) {
        1 -> moodVeryLow
        2 -> moodLow
        3 -> moodNeutral
        4 -> moodGood
        else -> moodGreat
    }
}

val LightLimanColors = LimanColors(
    bondAccent = Sage500,
    bondContainer = SageContainerLight,
    onBondContainer = SageContainerDark,
    streakGold = Color(0xFFE0A41F),
    lockTint = Ember600,
    moodVeryLow = Color(0xFF7E8AA6),
    moodLow = Color(0xFF8FA0B8),
    moodNeutral = Color(0xFFE0B354),
    moodGood = Color(0xFFE8893F),
    moodGreat = Color(0xFFD9641A),
    warmGradientTop = Color(0xFFFFE9D6),
    warmGradientBottom = Cream,
    nightSkyTop = NightBlueDeep,
    nightSkyMid = NightBlueMid,
    starColor = StarLight,
)

val DarkLimanColors = LimanColors(
    bondAccent = Sage300,
    bondContainer = SageContainerDark,
    onBondContainer = Sage200,
    streakGold = Color(0xFFF0C357),
    lockTint = Ember200,
    moodVeryLow = Color(0xFF9AA6C2),
    moodLow = Color(0xFFA6B4CC),
    moodNeutral = Color(0xFFE7C374),
    moodGood = Color(0xFFF0A565),
    moodGreat = Color(0xFFFFB077),
    warmGradientTop = Color(0xFF2E2218),
    warmGradientBottom = Ink,
    nightSkyTop = Color(0xFF0B1530),
    nightSkyMid = Color(0xFF182A52),
    starColor = StarCool,
)
