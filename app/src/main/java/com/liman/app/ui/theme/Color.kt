package com.liman.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.liman.app.data.model.ThemePalette

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

/* --------------------------------------------------------------------------
 * 5 "zindelik" teması — yalnızca primary (ana zindelik rengi) ailesini değiştirir;
 * sosyal yeşil aksan ve ruh hali renkleri sabit kalır.
 * ------------------------------------------------------------------------ */
@Immutable
data class PaletteSpec(
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
)

fun paletteSpec(palette: ThemePalette): PaletteSpec = when (palette) {
    ThemePalette.EMBER -> PaletteSpec(
        primaryLight = Color(0xFFD9641A), onPrimaryLight = Color.White,
        primaryContainerLight = Color(0xFFFFDCC4), onPrimaryContainerLight = Color(0xFF3B1700),
        primaryDark = Color(0xFFFFB077), onPrimaryDark = Color(0xFF4A1E00),
        primaryContainerDark = Color(0xFF5C2C0A), onPrimaryContainerDark = Color(0xFFFFD9BE),
    )
    ThemePalette.OCEAN -> PaletteSpec(
        primaryLight = Color(0xFF0E7C99), onPrimaryLight = Color.White,
        primaryContainerLight = Color(0xFFB8E7F4), onPrimaryContainerLight = Color(0xFF00323F),
        primaryDark = Color(0xFF74D2EC), onPrimaryDark = Color(0xFF00323F),
        primaryContainerDark = Color(0xFF063D4D), onPrimaryContainerDark = Color(0xFFB8E7F4),
    )
    ThemePalette.FOREST -> PaletteSpec(
        primaryLight = Color(0xFF2E8B57), onPrimaryLight = Color.White,
        primaryContainerLight = Color(0xFFBBEBCB), onPrimaryContainerLight = Color(0xFF06301B),
        primaryDark = Color(0xFF86D6A6), onPrimaryDark = Color(0xFF06351E),
        primaryContainerDark = Color(0xFF13432B), onPrimaryContainerDark = Color(0xFFBBEBCB),
    )
    ThemePalette.LAVENDER -> PaletteSpec(
        primaryLight = Color(0xFF6A4FC4), onPrimaryLight = Color.White,
        primaryContainerLight = Color(0xFFE5DEFF), onPrimaryContainerLight = Color(0xFF23055E),
        primaryDark = Color(0xFFC9BCFF), onPrimaryDark = Color(0xFF2B0E6F),
        primaryContainerDark = Color(0xFF3C2E7C), onPrimaryContainerDark = Color(0xFFE5DEFF),
    )
    ThemePalette.ROSE -> PaletteSpec(
        primaryLight = Color(0xFFC24468), onPrimaryLight = Color.White,
        primaryContainerLight = Color(0xFFFFD9E1), onPrimaryContainerLight = Color(0xFF3E0720),
        primaryDark = Color(0xFFF6A8BD), onPrimaryDark = Color(0xFF5A0A2A),
        primaryContainerDark = Color(0xFF6E1437), onPrimaryContainerDark = Color(0xFFFFD9E2),
    )
}

/** Tema seçici için örnek (swatch) rengi. */
fun ThemePalette.swatch(): Color = paletteSpec(this).primaryLight

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
