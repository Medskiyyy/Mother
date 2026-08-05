package com.mother.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/** Unit tests for [TimeUtils]. All timestamps are local-zone aware. */
class TimeUtilsTest {

    private val zone = ZoneId.systemDefault()

    /** Epoch millis of the given local date at the given hour. */
    private fun millisOf(date: LocalDate, hour: Int = 12, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    // region computeStreak

    @Test
    fun `streak is zero when there are no sessions`() {
        assertEquals(0, TimeUtils.computeStreak(emptyList()))
    }

    @Test
    fun `streak counts consecutive days ending today`() {
        val today = LocalDate.now()
        val sessions = listOf(
            millisOf(today),
            millisOf(today.minusDays(1)),
            millisOf(today.minusDays(2))
        )
        assertEquals(3, TimeUtils.computeStreak(sessions))
    }

    @Test
    fun `streak survives when today has no session yet`() {
        val today = LocalDate.now()
        val sessions = listOf(
            millisOf(today.minusDays(1)),
            millisOf(today.minusDays(2))
        )
        assertEquals(2, TimeUtils.computeStreak(sessions))
    }

    @Test
    fun `streak stops at the first gap`() {
        val today = LocalDate.now()
        val sessions = listOf(
            millisOf(today),
            millisOf(today.minusDays(1)),
            // gap: minusDays(2) missing
            millisOf(today.minusDays(3))
        )
        assertEquals(2, TimeUtils.computeStreak(sessions))
    }

    @Test
    fun `streak is zero when the last session is older than yesterday`() {
        val today = LocalDate.now()
        val sessions = listOf(
            millisOf(today.minusDays(2)),
            millisOf(today.minusDays(3))
        )
        assertEquals(0, TimeUtils.computeStreak(sessions))
    }

    @Test
    fun `multiple sessions on the same day count once`() {
        val today = LocalDate.now()
        val sessions = listOf(
            millisOf(today, 8),
            millisOf(today, 14),
            millisOf(today.minusDays(1), 20)
        )
        assertEquals(2, TimeUtils.computeStreak(sessions))
    }

    // endregion

    // region formatDurationCompact

    @Test
    fun `format duration renders minutes only`() {
        assertEquals("0m", TimeUtils.formatDurationCompact(0))
        assertEquals("45m", TimeUtils.formatDurationCompact(45))
    }

    @Test
    fun `format duration renders hours only`() {
        assertEquals("1j", TimeUtils.formatDurationCompact(60))
        assertEquals("2j", TimeUtils.formatDurationCompact(120))
    }

    @Test
    fun `format duration renders hours and minutes`() {
        assertEquals("1j 20m", TimeUtils.formatDurationCompact(80))
        assertEquals("2j 5m", TimeUtils.formatDurationCompact(125))
    }

    // endregion

    // region isSameDay

    @Test
    fun `same day returns true`() {
        val day = LocalDate.of(2026, 8, 4)
        assertTrue(TimeUtils.isSameDay(millisOf(day, 0, 0), millisOf(day, 23, 59)))
    }

    @Test
    fun `different days return false`() {
        val day = LocalDate.of(2026, 8, 4)
        assertFalse(TimeUtils.isSameDay(millisOf(day, 23, 59), millisOf(day.plusDays(1), 0, 1)))
    }

    // endregion

    // region day boundaries

    @Test
    fun `startOfDay is not after the moment and endOfDay is after it`() {
        val moment = millisOf(LocalDate.of(2026, 8, 4), 15, 30)
        val start = TimeUtils.startOfDay(moment)
        val end = TimeUtils.endOfDay(moment)
        assertTrue(start <= moment)
        assertTrue(moment < end)
    }

    @Test
    fun `startOfDay is idempotent`() {
        val moment = millisOf(LocalDate.of(2026, 8, 4), 15, 30)
        val start = TimeUtils.startOfDay(moment)
        assertEquals(start, TimeUtils.startOfDay(start))
    }

    @Test
    fun `endOfDay lands on the start of the next day`() {
        val moment = millisOf(LocalDate.of(2026, 8, 4), 15, 30)
        val nextDayStart = millisOf(LocalDate.of(2026, 8, 5), 0, 0)
        assertEquals(nextDayStart, TimeUtils.endOfDay(moment))
    }

    // endregion
}
