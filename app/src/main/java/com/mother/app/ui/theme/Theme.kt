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
    primary = AccentYellow,
    onPrimary = Color(0xFF121212),
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentRed,
    onTertiary = Color.White,
    background = Color(0xFFF8F5EE), // Soft Cream Warm Background
    onBackground = Color(0xFF121212),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFEFEAD8),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFF121212), // Bold Black Border for Neobrutalism
    error = Color(0xFFE5484D),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentYellow,
    onPrimary = Color(0xFF121212),
    secondary = DarkAccentBlue,
    onSecondary = Color(0xFF121212),
    tertiary = DarkAccentRed,
    onTertiary = Color(0xFF121212),
    background = Color(0xFF121212), // OLED Vantablack Dark
    onBackground = Color(0xFFF0F0F0),
    surface = Color(0xFF1E1E22), // Soft Dark Card
    onSurface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFF2C2C32),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFFF0F0F0), // Crisp High-contrast Border in Dark Mode
    error = Color(0xFFFF8080),
    onError = Color(0xFF121212)
)

@Composable
fun MotherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color is off on purpose: the brand palette must stay stable
    // (design_system.md), not follow the wallpaper.
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
