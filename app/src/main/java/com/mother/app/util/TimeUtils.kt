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

    /**
     * Local start of yesterday, exclusive-end, as epoch millis. Use it with
     * [startOfDay] as a half-open [start, end) range: "sessions from the
     * beginning of time until the end of yesterday".
     */
    fun endOfYesterday(now: Long = System.currentTimeMillis()): Long = startOfDay(now)

    /** Start of the month containing [epochMillis], in the device's local zone. */
    fun startOfMonth(epochMillis: Long): Long =
        toLocalDate(epochMillis).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Start of the month after [epochMillis], in the device's local zone. */
    fun startOfNextMonth(epochMillis: Long): Long =
        toLocalDate(epochMillis).withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Combines the local date of [dayEpochMillis] with a wall-clock time into epoch millis. */
    fun atLocalTime(dayEpochMillis: Long, hour: Int, minute: Int): Long =
        toLocalDate(dayEpochMillis).atTime(java.time.LocalTime.of(hour, minute))
            .atZone(zone).toInstant().toEpochMilli()

    /** Hour-of-day of [epochMillis] in the local zone. */
    fun hourOf(epochMillis: Long): Int =
        Instant.ofEpochMilli(epochMillis).atZone(zone).hour

    /** Minute-of-hour of [epochMillis] in the local zone. */
    fun minuteOf(epochMillis: Long): Int =
        Instant.ofEpochMilli(epochMillis).atZone(zone).minute

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
        val days = sessionDayStarts.map { toLocalDate(it) }.toSet()
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

    /** Longest run of consecutive days that each have at least one study session. */
    fun computeBestStreak(sessionDayStarts: List<Long>): Int {
        if (sessionDayStarts.isEmpty()) return 0
        val days = sessionDayStarts.map { toLocalDate(it) }.toSortedSet()
        var best = 1
        var run = 1
        var previous: LocalDate? = null
        for (day in days) {
            if (previous != null && day == previous.plusDays(1)) {
                run++
                if (run > best) best = run
            } else if (previous != null) {
                run = 1
            }
            previous = day
        }
        return best
    }

    fun toLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
}