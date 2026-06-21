package com.liman.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/* --------------------------------------------------------------------------
 * Tipografi
 *
 * Tasarım niyeti:
 *  - Başlıklar: modern, geometrik bir sans (Plus Jakarta Sans ruhu).
 *  - Günlük / serbest metin: zarif bir serif (Lora ruhu).
 *
 * Burada güvenilir çalışması için sistem font aileleri (SansSerif / Serif)
 * kullanılır. Plus Jakarta Sans & Lora'yı eklemek için:
 *   1) .ttf dosyalarını res/font/ altına koyun ve
 *      `val Jakarta = FontFamily(Font(R.font.plus_jakarta_sans_*))` tanımlayın,
 *   ya da
 *   2) `androidx.compose.ui:ui-text-google-fonts` ile indirilebilir font
 *      sağlayıcısını kullanın.
 * Aşağıdaki [DisplayFamily] / [SerifFamily] referanslarını değiştirmeniz yeterli.
 * ------------------------------------------------------------------------ */

// UI / başlık ailesi (modern sans)
val DisplayFamily: FontFamily = FontFamily.SansSerif

// Günlük / serbest yazı ailesi (zarif serif)
val SerifFamily: FontFamily = FontFamily.Serif

val LimanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Gövde metni — UI etiketleri için sans
    bodyLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.3.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp,
    ),
)

/** Günlük / serbest metin için zarif serif stiller (Lora ruhu). */
val JournalBodyStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.2.sp,
)

val JournalTitleStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
)

val QuoteStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 27.sp,
    letterSpacing = 0.2.sp,
)
