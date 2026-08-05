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
    onPrimary = Ink,
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = AccentRed,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    outline = Ink,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentYellow,
    onPrimary = Ink,
    secondary = DarkAccentBlue,
    onSecondary = Ink,
    tertiary = DarkAccentRed,
    onTertiary = Ink,
    background = DarkPaper,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkSand,
    onSurfaceVariant = DarkInkSoft,
    outline = DarkInk,
    error = Color(0xFFFFB4AB),
    onError = Ink
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
