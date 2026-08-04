package com.mother.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Time helpers. All stored timestamps are UTC epoch millis (DATABASE_SCHEMA.md);
 * display always converts to the device's local zone.
 */
object TimeUtils {

    private val zone: ZoneId get() = ZoneId.systemDefault()

    /** Start of the given epoch-day in the device's local zone, as UTC millis. */
    fun startOfDay(epochMillis: Long): Long {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /** End of the given epoch-day (exclusive) in the device's local zone, as UTC millis. */
    fun endOfDay(epochMillis: Long): Long {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /** Start of the current local day. */
    fun todayStart(): Long = startOfDay(System.currentTimeMillis())

    /** End of the current local day (exclusive). */
    fun todayEnd(): Long = endOfDay(System.currentTimeMillis())

    /** Renders an epoch millis as "HH:mm" in the local zone. */
    fun formatTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone)
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

    /** Renders "E, d MMMM yyyy" (e.g. Selasa, 4 Agustus 2026) in the local zone. */
    fun formatFullDate(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone)
            .format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID")))

    /** True when both epoch millis fall on the same local calendar day. */
    fun isSameDay(a: Long, b: Long): Boolean {
        val da = Instant.ofEpochMilli(a).atZone(zone).toLocalDate()
        val db = Instant.ofEpochMilli(b).atZone(zone).toLocalDate()
        return da == db
    }

    /** Renders a duration in minutes as compact "1j 20m" or "45m". */
    fun formatDurationCompact(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}j ${minutes}m"
            hours > 0 -> "${hours}j"
            else -> "${minutes}m"
        }
    }

    /** Counts consecutive days (ending today) that have at least one study session. */
    fun computeStreak(sessionDayStarts: List<Long>): Int {
        if (sessionDayStarts.isEmpty()) return 0
        val days = sessionDayStarts.map { localDate(it) }.toSet()
        var current = LocalDate.now()
        // If today has no session yet, streak may still be alive from yesterday.
        if (current !in days) current = current.minusDays(1)
        var streak = 0
        while (current in days) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    private fun localDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
}