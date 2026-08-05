package com.mother.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mother.app.R

/** IBM Plex Sans (Docs/design_system.md), bundled for offline use. */
val PlexSans = FontFamily(Font(R.font.ibm_plex_sans))

/** Tabular figures for timers and statistics (design_system.md §Typography). */
private const val TABULAR_NUMBERS = "tnum"

private fun plex(
    size: Int,
    weight: FontWeight,
    lineHeight: Int = size + 8,
    tabular: Boolean = false
) = TextStyle(
    fontFamily = PlexSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontFeatureSettings = if (tabular) TABULAR_NUMBERS else null
)

val Typography = Typography(
    displayLarge = plex(57, FontWeight.Bold, tabular = true),
    displayMedium = plex(45, FontWeight.Bold, tabular = true),
    displaySmall = plex(36, FontWeight.Bold, tabular = true),
    headlineLarge = plex(32, FontWeight.Bold),
    headlineMedium = plex(28, FontWeight.Bold),
    headlineSmall = plex(24, FontWeight.Bold, tabular = true),
    titleLarge = plex(22, FontWeight.Bold),
    titleMedium = plex(16, FontWeight.Bold, tabular = true),
    titleSmall = plex(14, FontWeight.Medium),
    bodyLarge = plex(16, FontWeight.Medium),
    bodyMedium = plex(14, FontWeight.Medium, tabular = true),
    bodySmall = plex(12, FontWeight.Medium, tabular = true),
    labelLarge = plex(14, FontWeight.Bold),
    labelMedium = plex(12, FontWeight.Bold),
    labelSmall = plex(11, FontWeight.Medium)
)
