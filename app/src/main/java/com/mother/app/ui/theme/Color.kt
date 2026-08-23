package com.mother.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mother.app.data.model.Priority

/**
 * Neobrutalism Design System Color Tokens.
 * Clearly separated into Light and Dark theme palettes.
 */

// --- Light Theme Palette (Vibrant, high-contrast Neobrutalism) ---
object LightColors {
    val Background = Color(0xFFF8F5EE) // Soft Cream Warm Canvas
    val Surface = Color(0xFFFFFFFF)    // Pure White Card
    val SurfaceVariant = Color(0xFFEFEAD8)
    val TextPrimary = Color(0xFF121212)
    val TextSecondary = Color(0xFF4A463C)
    val Outline = Color(0xFF121212)    // Firm Black Border

    // Text & active tab yellow (Dark Goldenrod - WCAG AA 6.54:1 contrast on white)
    val TextYellow = Color(0xFF7A5200)

    // Solid button yellow with black text
    val SolidYellow = Color(0xFFFFD60A)

    // Streak Hero Card (Vibrant Neon Yellow)
    val StreakCardBg = Color(0xFFFEE140)
    val StreakCardText = Color(0xFF121212)
    val StreakBadgeBg = Color(0xFF121212)
    val StreakBadgeTint = Color(0xFFFEE140)

    // Priority Cards (Vibrant full solid colors with high readability)
    val PriorityUrgentBg = Color(0xFFFF6B6B) // Coral Red Vibrant
    val PriorityUrgentText = Color(0xFF121212)
    val PriorityUrgentSubtitle = Color(0xFF2C2C2C)
    val PriorityUrgentBadgeBg = Color(0xFFFF6B6B)
    val PriorityUrgentBadgeText = Color(0xFF121212)

    val PriorityMepetBg = Color(0xFFFF9F43)  // Bright Orange
    val PriorityMepetText = Color(0xFF121212)
    val PriorityMepetSubtitle = Color(0xFF2C2C2C)
    val PriorityMepetBadgeBg = Color(0xFFFF9F43)
    val PriorityMepetBadgeText = Color(0xFF121212)

    val PriorityWaspadaBg = Color(0xFFFECA57) // Bright Gold/Yellow
    val PriorityWaspadaText = Color(0xFF121212)
    val PriorityWaspadaSubtitle = Color(0xFF2C2C2C)
    val PriorityWaspadaBadgeBg = Color(0xFFFECA57)
    val PriorityWaspadaBadgeText = Color(0xFF121212)

    val PriorityAmanBg = Color(0xFF1DD1A1)   // Mint Green / Teal Vibrant
    val PriorityAmanText = Color(0xFF121212)
    val PriorityAmanSubtitle = Color(0xFF2C2C2C)
    val PriorityAmanBadgeBg = Color(0xFF1DD1A1)
    val PriorityAmanBadgeText = Color(0xFF121212)
}

// --- Dark Theme Palette (Muted, comfortable, Material Dark Gray standard) ---
object DarkColors {
    val Background = Color(0xFF121212) // Material Dark Gray near-black
    val Surface = Color(0xFF1E1E24)    // Soft Dark Card
    val SurfaceVariant = Color(0xFF2A2A34)
    val TextPrimary = Color(0xFFEEEEEE)
    val TextSecondary = Color(0xFFB8B8C4)
    val Outline = Color(0xFF444452)    // Calibrated muted border (not blinding white)

    // Text & active tab yellow (Soft Warm Gold - high contrast against dark backgrounds)
    val TextYellow = Color(0xFFFFD15C)

    // Solid button yellow in dark mode
    val SolidYellow = Color(0xFFFFD15C)

    // Streak Hero Card (Muted rich dark gold container)
    val StreakCardBg = Color(0xFF262215)
    val StreakCardBorder = Color(0xFF6B581B)
    val StreakCardText = Color(0xFFFFEEB0)
    val StreakBadgeBg = Color(0xFF14120B)
    val StreakBadgeTint = Color(0xFFFFD15C)

    // Priority Cards (Muted, deep, desaturated backgrounds, comfortable on dark theme)
    val PriorityUrgentBg = Color(0xFF381A1B)
    val PriorityUrgentBorder = Color(0xFF7A3032)
    val PriorityUrgentText = Color(0xFFFFEAEA)
    val PriorityUrgentSubtitle = Color(0xFFD4A5A5)
    val PriorityUrgentBadgeBg = Color(0xFFD9534F)
    val PriorityUrgentBadgeText = Color.White

    val PriorityMepetBg = Color(0xFF382310)
    val PriorityMepetBorder = Color(0xFF7E4A1E)
    val PriorityMepetText = Color(0xFFFFF0E0)
    val PriorityMepetSubtitle = Color(0xFFD4B394)
    val PriorityMepetBadgeBg = Color(0xFFD98E3C)
    val PriorityMepetBadgeText = Color(0xFF121212)

    val PriorityWaspadaBg = Color(0xFF332B10)
    val PriorityWaspadaBorder = Color(0xFF75631E)
    val PriorityWaspadaText = Color(0xFFFFFDE0)
    val PriorityWaspadaSubtitle = Color(0xFFD4C894)
    val PriorityWaspadaBadgeBg = Color(0xFFC7A824)
    val PriorityWaspadaBadgeText = Color(0xFF121212)

    val PriorityAmanBg = Color(0xFF102E29)
    val PriorityAmanBorder = Color(0xFF1F6358)
    val PriorityAmanText = Color(0xFFE0FFF8)
    val PriorityAmanSubtitle = Color(0xFF94D4C6)
    val PriorityAmanBadgeBg = Color(0xFF3AA89F)
    val PriorityAmanBadgeText = Color(0xFF121212)
}

// Global Legacy / Generic Accents
val AccentYellow = Color(0xFFFFD60A)
val AccentBlue = Color(0xFF3E63DD)
val AccentPurple = Color(0xFF8E4EC6)
val AccentGreen = Color(0xFF30A46C)
val AccentRed = Color(0xFFE5484D)

val DarkAccentBlue = Color(0xFF7C93FF)
val DarkAccentRed = Color(0xFFFF8080)

val SolidYellow = LightColors.SolidYellow
val NeoStreakYellow = LightColors.StreakCardBg
val NeoShadowColor = Color(0xFF121212)

// Legacy Direct References
val Ink = LightColors.TextPrimary
val InkSoft = LightColors.TextSecondary
val Paper = LightColors.Background
val CardWhite = LightColors.Surface
val Sand = LightColors.SurfaceVariant

val PriorityUrgent = LightColors.PriorityUrgentBg
val PriorityMepet = LightColors.PriorityMepetBg
val PriorityWaspada = LightColors.PriorityWaspadaBg
val PriorityAman = LightColors.PriorityAmanBg

/** Unified Priority Color Palette Model */
data class PriorityPalette(
    val cardBg: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val badgeBg: Color,
    val badgeText: Color
)

@Composable
fun getPriorityPalette(priority: Priority): PriorityPalette {
    val isDark = isAppInDarkTheme()
    return if (isDark) {
        when (priority) {
            Priority.URGENT -> PriorityPalette(
                cardBg = DarkColors.PriorityUrgentBg,
                cardBorder = DarkColors.PriorityUrgentBorder,
                textPrimary = DarkColors.PriorityUrgentText,
                textSecondary = DarkColors.PriorityUrgentSubtitle,
                badgeBg = DarkColors.PriorityUrgentBadgeBg,
                badgeText = DarkColors.PriorityUrgentBadgeText
            )
            Priority.MEPET -> PriorityPalette(
                cardBg = DarkColors.PriorityMepetBg,
                cardBorder = DarkColors.PriorityMepetBorder,
                textPrimary = DarkColors.PriorityMepetText,
                textSecondary = DarkColors.PriorityMepetSubtitle,
                badgeBg = DarkColors.PriorityMepetBadgeBg,
                badgeText = DarkColors.PriorityMepetBadgeText
            )
            Priority.WASPADA -> PriorityPalette(
                cardBg = DarkColors.PriorityWaspadaBg,
                cardBorder = DarkColors.PriorityWaspadaBorder,
                textPrimary = DarkColors.PriorityWaspadaText,
                textSecondary = DarkColors.PriorityWaspadaSubtitle,
                badgeBg = DarkColors.PriorityWaspadaBadgeBg,
                badgeText = DarkColors.PriorityWaspadaBadgeText
            )
            Priority.AMAN -> PriorityPalette(
                cardBg = DarkColors.PriorityAmanBg,
                cardBorder = DarkColors.PriorityAmanBorder,
                textPrimary = DarkColors.PriorityAmanText,
                textSecondary = DarkColors.PriorityAmanSubtitle,
                badgeBg = DarkColors.PriorityAmanBadgeBg,
                badgeText = DarkColors.PriorityAmanBadgeText
            )
        }
    } else {
        when (priority) {
            Priority.URGENT -> PriorityPalette(
                cardBg = LightColors.PriorityUrgentBg,
                cardBorder = LightColors.Outline,
                textPrimary = LightColors.PriorityUrgentText,
                textSecondary = LightColors.PriorityUrgentSubtitle,
                badgeBg = LightColors.PriorityUrgentBadgeBg,
                badgeText = LightColors.PriorityUrgentBadgeText
            )
            Priority.MEPET -> PriorityPalette(
                cardBg = LightColors.PriorityMepetBg,
                cardBorder = LightColors.Outline,
                textPrimary = LightColors.PriorityMepetText,
                textSecondary = LightColors.PriorityMepetSubtitle,
                badgeBg = LightColors.PriorityMepetBadgeBg,
                badgeText = LightColors.PriorityMepetBadgeText
            )
            Priority.WASPADA -> PriorityPalette(
                cardBg = LightColors.PriorityWaspadaBg,
                cardBorder = LightColors.Outline,
                textPrimary = LightColors.PriorityWaspadaText,
                textSecondary = LightColors.PriorityWaspadaSubtitle,
                badgeBg = LightColors.PriorityWaspadaBadgeBg,
                badgeText = LightColors.PriorityWaspadaBadgeText
            )
            Priority.AMAN -> PriorityPalette(
                cardBg = LightColors.PriorityAmanBg,
                cardBorder = LightColors.Outline,
                textPrimary = LightColors.PriorityAmanText,
                textSecondary = LightColors.PriorityAmanSubtitle,
                badgeBg = LightColors.PriorityAmanBadgeBg,
                badgeText = LightColors.PriorityAmanBadgeText
            )
        }
    }
}
