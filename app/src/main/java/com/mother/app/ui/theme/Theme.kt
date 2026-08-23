package com.mother.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** Sharp, flat surfaces with firm borders (design_system.md §Karakter UI). */
val NeoShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * Returns whether the active app composition is in Dark Theme,
 * respecting user preferences in Settings (LIGHT / DARK / SYSTEM).
 */
@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDarkTheme.current

private val LightColorScheme = lightColorScheme(
    primary = LightColors.TextYellow, // Dark Goldenrod (contrast ratio 6.5:1 on white, WCAG AA compliant)
    onPrimary = Color.White,
    primaryContainer = LightColors.SolidYellow, // Solid yellow for buttons/pills
    onPrimaryContainer = Color(0xFF121212),
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentRed,
    onTertiary = Color.White,
    background = LightColors.Background, // Soft Cream Warm Background (#F8F5EE)
    onBackground = LightColors.TextPrimary,
    surface = LightColors.Surface,       // Pure White Card (#FFFFFF)
    onSurface = LightColors.TextPrimary,
    surfaceVariant = LightColors.SurfaceVariant,
    onSurfaceVariant = LightColors.TextSecondary,
    outline = LightColors.Outline,       // Bold Black Border for Neobrutalism (#121212)
    error = Color(0xFFD9383A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.TextYellow, // Soft warm gold (#FFD15C) for text & active tabs (high contrast on dark)
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF383015),
    onPrimaryContainer = Color(0xFFFFEAA0),
    secondary = DarkAccentBlue,
    onSecondary = Color(0xFF121212),
    tertiary = DarkAccentRed,
    onTertiary = Color(0xFF121212),
    background = DarkColors.Background, // Material Dark Theme near-black (#121212)
    onBackground = DarkColors.TextPrimary,
    surface = DarkColors.Surface,       // Soft Dark Gray Card (#1E1E24)
    onSurface = DarkColors.TextPrimary,
    surfaceVariant = DarkColors.SurfaceVariant, // #2A2A34
    onSurfaceVariant = DarkColors.TextSecondary,
    outline = DarkColors.Outline,       // Calibrated muted border (#444452), not blinding white
    error = Color(0xFFFF8080),
    onError = Color(0xFF121212)
)

@Composable
fun MotherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = NeoShapes,
            content = content
        )
    }
}
