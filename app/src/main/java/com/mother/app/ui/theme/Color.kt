package com.mother.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mother.app.data.model.Priority

/**
 * Neobrutalism palette: neutral base, flat solids, high contrast.
 * Tuned with WCAG AA compliance (contrast ratio >= 4.5:1).
 */

// Ink & neutrals (Light)
val Ink = Color(0xFF1B1B1B)
val InkSoft = Color(0xFF4A463C)
val Paper = Color(0xFFF5F1E8)
val CardWhite = Color(0xFFFFFFFF)
val Sand = Color(0xFFEFE9DC)

// Dark neutrals (Dark - Material Design #121212 standard, near-black)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E24)
val DarkSurfaceVariant = Color(0xFF2A2A34)
val DarkOutline = Color(0xFF444452) // Softened high-contrast border
val DarkInk = Color(0xFFF0EBDF)
val DarkInkSoft = Color(0xFFC8C2B2)

// Yellow Brand Tokens
// 1. Text / Accent Yellow: High-contrast Dark Goldenrod for text & active tabs on light backgrounds (WCAG AA 6.5:1)
val TextYellowLight = Color(0xFF7A5200) // Deep Dark Goldenrod, readable on light surfaces
val TextYellowDark = Color(0xFFFFD15C)  // Soft warm gold, readable on dark surfaces

// 2. Solid Yellow: For solid button / pill fills with black text (unchanged)
val SolidYellow = Color(0xFFFFD60A)

// Accents (seeded category colors: PRD §31)
val AccentYellow = Color(0xFFFFD60A)
val AccentBlue = Color(0xFF3E63DD)
val AccentPurple = Color(0xFF8E4EC6)
val AccentGreen = Color(0xFF30A46C)
val AccentRed = Color(0xFFE5484D)

// Dark-mode accents (tuned for dark backgrounds)
val DarkAccentBlue = Color(0xFF7C93FF)
val DarkAccentRed = Color(0xFFFF8080)
val DarkAccentGreen = Color(0xFF5CD698)
val DarkAccentPurple = Color(0xFFB580EB)

// --- Priority Color Palettes (Light vs Dark) ---

// Light Mode Priority Colors (Vibrant, high-contrast, black text)
val PriorityUrgent = Color(0xFFFF6B6B) // Coral Red Vibrant
val PriorityMepet = Color(0xFFFF9F43)  // Bright Orange
val PriorityWaspada = Color(0xFFFECA57) // Bright Gold/Yellow
val PriorityAman = Color(0xFF1DD1A1)   // Mint Green / Teal Vibrant

// Dark Mode Priority Colors (Muted, deep surfaces, desaturated values, comfortable on dark background)
val DarkPriorityUrgentBg = Color(0xFF381A1B)
val DarkPriorityUrgentBorder = Color(0xFF7A3032)
val DarkPriorityUrgentText = Color(0xFFFFEAEA)
val DarkPriorityUrgentSubtitle = Color(0xFFD4A5A5)
val DarkPriorityUrgentBadgeBg = Color(0xFFD9534F)

val DarkPriorityMepetBg = Color(0xFF382310)
val DarkPriorityMepetBorder = Color(0xFF7E4A1E)
val DarkPriorityMepetText = Color(0xFFFFF0E0)
val DarkPriorityMepetSubtitle = Color(0xFFD4B394)
val DarkPriorityMepetBadgeBg = Color(0xFFD98E3C)

val DarkPriorityWaspadaBg = Color(0xFF332B10)
val DarkPriorityWaspadaBorder = Color(0xFF75631E)
val DarkPriorityWaspadaText = Color(0xFFFFFDE0)
val DarkPriorityWaspadaSubtitle = Color(0xFFD4C894)
val DarkPriorityWaspadaBadgeBg = Color(0xFFC7A824)

val DarkPriorityAmanBg = Color(0xFF102E29)
val DarkPriorityAmanBorder = Color(0xFF1F6358)
val DarkPriorityAmanText = Color(0xFFE0FFF8)
val DarkPriorityAmanSubtitle = Color(0xFF94D4C6)
val DarkPriorityAmanBadgeBg = Color(0xFF3AA89F)

// Shadows & Badges
val NeoShadowColor = Color(0xFF121212)
val NeoStreakYellow = Color(0xFFFEE140)

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
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        when (priority) {
            Priority.URGENT -> PriorityPalette(
                cardBg = DarkPriorityUrgentBg,
                cardBorder = DarkPriorityUrgentBorder,
                textPrimary = DarkPriorityUrgentText,
                textSecondary = DarkPriorityUrgentSubtitle,
                badgeBg = DarkPriorityUrgentBadgeBg,
                badgeText = Color.White
            )
            Priority.MEPET -> PriorityPalette(
                cardBg = DarkPriorityMepetBg,
                cardBorder = DarkPriorityMepetBorder,
                textPrimary = DarkPriorityMepetText,
                textSecondary = DarkPriorityMepetSubtitle,
                badgeBg = DarkPriorityMepetBadgeBg,
                badgeText = Color(0xFF121212)
            )
            Priority.WASPADA -> PriorityPalette(
                cardBg = DarkPriorityWaspadaBg,
                cardBorder = DarkPriorityWaspadaBorder,
                textPrimary = DarkPriorityWaspadaText,
                textSecondary = DarkPriorityWaspadaSubtitle,
                badgeBg = DarkPriorityWaspadaBadgeBg,
                badgeText = Color(0xFF121212)
            )
            Priority.AMAN -> PriorityPalette(
                cardBg = DarkPriorityAmanBg,
                cardBorder = DarkPriorityAmanBorder,
                textPrimary = DarkPriorityAmanText,
                textSecondary = DarkPriorityAmanSubtitle,
                badgeBg = DarkPriorityAmanBadgeBg,
                badgeText = Color(0xFF121212)
            )
        }
    } else {
        when (priority) {
            Priority.URGENT -> PriorityPalette(
                cardBg = PriorityUrgent,
                cardBorder = Color(0xFF121212),
                textPrimary = Color(0xFF121212),
                textSecondary = Color(0xFF2C2C2C),
                badgeBg = PriorityUrgent,
                badgeText = Color(0xFF121212)
            )
            Priority.MEPET -> PriorityPalette(
                cardBg = PriorityMepet,
                cardBorder = Color(0xFF121212),
                textPrimary = Color(0xFF121212),
                textSecondary = Color(0xFF2C2C2C),
                badgeBg = PriorityMepet,
                badgeText = Color(0xFF121212)
            )
            Priority.WASPADA -> PriorityPalette(
                cardBg = PriorityWaspada,
                cardBorder = Color(0xFF121212),
                textPrimary = Color(0xFF121212),
                textSecondary = Color(0xFF2C2C2C),
                badgeBg = PriorityWaspada,
                badgeText = Color(0xFF121212)
            )
            Priority.AMAN -> PriorityPalette(
                cardBg = PriorityAman,
                cardBorder = Color(0xFF121212),
                textPrimary = Color(0xFF121212),
                textSecondary = Color(0xFF2C2C2C),
                badgeBg = PriorityAman,
                badgeText = Color(0xFF121212)
            )
        }
    }
}
