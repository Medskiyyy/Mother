package com.mother.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

private val LightColorScheme = lightColorScheme(
    primary = TextYellowLight, // Dark Goldenrod (contrast ratio 6.5:1 on white, WCAG AA compliant)
    onPrimary = Color.White,
    primaryContainer = SolidYellow, // Solid yellow for buttons/pills
    onPrimaryContainer = Color(0xFF121212),
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentRed,
    onTertiary = Color.White,
    background = Color(0xFFF8F5EE), // Soft Cream Warm Background
    onBackground = Color(0xFF121212),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFEFEAD8),
    onSurfaceVariant = Color(0xFF4A463C),
    outline = Color(0xFF121212), // Bold Black Border for Neobrutalism in Light Mode
    error = Color(0xFFD9383A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TextYellowDark, // Soft warm gold (#FFD15C) for text & active tabs (high contrast on dark)
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF383015),
    onPrimaryContainer = Color(0xFFFFEAA0),
    secondary = DarkAccentBlue,
    onSecondary = Color(0xFF121212),
    tertiary = DarkAccentRed,
    onTertiary = Color(0xFF121212),
    background = DarkBackground, // Material Dark Theme near-black (#121212)
    onBackground = Color(0xFFEEEEEE),
    surface = DarkSurface, // Soft Dark Gray Card (#1E1E24)
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = DarkSurfaceVariant, // #2A2A34
    onSurfaceVariant = Color(0xFFB8B8C4),
    outline = DarkOutline, // Calibrated muted border (#444452), not blinding white
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NeoShapes,
        content = content
    )
}
