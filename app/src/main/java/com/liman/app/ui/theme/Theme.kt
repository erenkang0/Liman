package com.liman.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.liman.app.data.model.ThemePalette

private val LightColorScheme = lightColorScheme(
    primary = Ember500,
    onPrimary = Color.White,
    primaryContainer = EmberContainerLight,
    onPrimaryContainer = Color(0xFF3B1700),
    secondary = Sage500,
    onSecondary = Color.White,
    secondaryContainer = SageContainerLight,
    onSecondaryContainer = SageContainerDark,
    tertiary = Dusk500,
    onTertiary = Color.White,
    tertiaryContainer = DuskContainerLight,
    onTertiaryContainer = DuskContainerDark,
    background = Cream,
    onBackground = CreamOnSurface,
    surface = Cream,
    onSurface = CreamOnSurface,
    surfaceVariant = Color(0xFFF1E8D8),
    onSurfaceVariant = CreamOnSurfaceVariant,
    surfaceContainerLowest = CreamHigh,
    surfaceContainerLow = CreamHigh,
    surfaceContainer = Color(0xFFF6EEDF),
    surfaceContainerHigh = Color(0xFFF1E8D7),
    surfaceContainerHighest = Color(0xFFEDE3D1),
    outline = CreamOutline,
    outlineVariant = Color(0xFFEFE6D5),
    error = ErrorLight,
    onError = Color.White,
    inverseSurface = Ink,
    inverseOnSurface = Cream,
    inversePrimary = Ember200,
)

private val DarkColorScheme = darkColorScheme(
    primary = Ember200,
    onPrimary = Color(0xFF4A1E00),
    primaryContainer = EmberContainerDark,
    onPrimaryContainer = Ember100,
    secondary = Sage300,
    onSecondary = Color(0xFF0C2417),
    secondaryContainer = SageContainerDark,
    onSecondaryContainer = Sage200,
    tertiary = Dusk200,
    onTertiary = Color(0xFF1E1E40),
    tertiaryContainer = DuskContainerDark,
    onTertiaryContainer = Dusk200,
    background = Ink,
    onBackground = InkOnSurface,
    surface = Ink,
    onSurface = InkOnSurface,
    surfaceVariant = Color(0xFF332E26),
    onSurfaceVariant = InkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0F0C08),
    surfaceContainerLow = InkHigh,
    surfaceContainer = InkCard,
    surfaceContainerHigh = Color(0xFF2C271F),
    surfaceContainerHighest = Color(0xFF373129),
    outline = InkOutline,
    outlineVariant = Color(0xFF2E2920),
    error = ErrorDark,
    onError = Color(0xFF601410),
    inverseSurface = Cream,
    inverseOnSurface = Ink,
    inversePrimary = Ember500,
)

/** Psikolog Defteri dünyası — sabit kırmızı/siyah/sepya koyu şema. */
private val DefterColorScheme = darkColorScheme(
    primary = DefterCrimson,
    onPrimary = Color(0xFFFFF1EC),
    primaryContainer = DefterCrimsonDeep,
    onPrimaryContainer = DefterCrimsonSoft,
    secondary = DefterSepia,
    onSecondary = Color(0xFF2A1E0C),
    secondaryContainer = DefterSepiaDeep,
    onSecondaryContainer = DefterSepiaSoft,
    tertiary = DefterBrick,
    onTertiary = Color(0xFF2A130C),
    tertiaryContainer = Color(0xFF3E2018),
    onTertiaryContainer = Color(0xFFF6CBBE),
    background = DefterBlack,
    onBackground = DefterParchment,
    surface = DefterSurface,
    onSurface = DefterParchment,
    surfaceVariant = DefterSurface2,
    onSurfaceVariant = DefterParchmentDim,
    surfaceContainerLowest = DefterBlack,
    surfaceContainerLow = DefterSurfaceLow,
    surfaceContainer = DefterSurface1,
    surfaceContainerHigh = DefterSurface2,
    surfaceContainerHighest = DefterSurface3,
    outline = DefterOutline,
    outlineVariant = DefterOutlineDim,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3A0A06),
    inverseSurface = DefterParchment,
    inverseOnSurface = DefterBlack,
    inversePrimary = DefterCrimsonDeep,
)

val LocalLimanColors = staticCompositionLocalOf { LightLimanColors }

/** Seçilen "zindelik" temasının primary ailesini temel şemaya uygular. */
private fun ColorScheme.withPalette(spec: PaletteSpec, dark: Boolean): ColorScheme =
    if (dark) copy(
        primary = spec.primaryDark,
        onPrimary = spec.onPrimaryDark,
        primaryContainer = spec.primaryContainerDark,
        onPrimaryContainer = spec.onPrimaryContainerDark,
        inversePrimary = spec.primaryLight,
    ) else copy(
        primary = spec.primaryLight,
        onPrimary = spec.onPrimaryLight,
        primaryContainer = spec.primaryContainerLight,
        onPrimaryContainer = spec.onPrimaryContainerLight,
        inversePrimary = spec.primaryDark,
    )

@Composable
fun LimanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ThemePalette = ThemePalette.EMBER,
    // Material You — destekleyen cihazlarda dinamik renk
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.withPalette(paletteSpec(palette), dark = true)
        else -> LightColorScheme.withPalette(paletteSpec(palette), dark = false)
    }
    val limanColors = if (darkTheme) DarkLimanColors else LightLimanColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            val lightBars = colorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = lightBars
            controller.isAppearanceLightNavigationBars = lightBars
        }
    }

    CompositionLocalProvider(LocalLimanColors provides limanColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LimanTypography,
            shapes = LimanShapes,
            content = content,
        )
    }
}

/**
 * Psikolog Defteri dünyasının teması — sabit kırmızı/siyah/sepya, dinamik
 * renkten bağımsız ve her zaman koyu. Liman dünyasının teması içine yerleştirilir.
 */
@Composable
fun DefterTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }
    CompositionLocalProvider(LocalLimanColors provides DefterLimanColors) {
        MaterialTheme(
            colorScheme = DefterColorScheme,
            typography = LimanTypography,
            shapes = LimanShapes,
            content = content,
        )
    }
}
