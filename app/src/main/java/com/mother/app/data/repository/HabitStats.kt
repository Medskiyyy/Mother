package com.mother.app.data.repository

import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.RestoreHistoryEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.util.TimeUtils
import java.time.LocalDate

/**
 * Derived habit statistics (PRD §14, AGENT_RULES: never persist computed data).
 * A habit's day is "done" when total StudySession minutes >= target. Streak is
 * the run of consecutive study days; a used Restore Streak fills the day before
 * it was consumed, letting a broken chain reconnect.
 */
object HabitStats {

    enum class DayStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

    /** Today's status for a habit given minutes already studied. */
    fun dayStatus(habit: HabitEntity, todayMinutes: Int): DayStatus = when {
        todayMinutes >= habit.targetMinute -> DayStatus.COMPLETED
        todayMinutes > 0 -> DayStatus.IN_PROGRESS
        else -> DayStatus.NOT_STARTED
    }

    /** Local-day set a habit counts toward streaks: session days + restored days. */
    private fun activeDays(habitId: String, sessions: List<StudySessionEntity>, restores: List<RestoreHistoryEntity>): Set<LocalDate> {
        val sessionDays = sessions.asSequence()
            .filter { it.habitId == habitId }
            .map { TimeUtils.toLocalDate(it.startTime) }
        val restoreDays = restores.asSequence()
            .filter { it.reason == habitId }
            // A restore covers the day before it was used (the missed day).
            .map { TimeUtils.toLocalDate(it.restoreDate).minusDays(1) }
        return (sessionDays + restoreDays).toSet()
    }

    /** Current streak ending today (or yesterday if today is still pending). */
    fun currentStreak(
        habitId: String,
        sessions: List<StudySessionEntity>,
        restores: List<RestoreHistoryEntity>
    ): Int {
        val days = activeDays(habitId, sessions, restores)
        if (days.isEmpty()) return 0
        var current = LocalDate.now()
        if (current !in days) current = current.minusDays(1)
        var streak = 0
        while (current in days) {
            streak++
            current = current.minusDays(1)
        }
        return streak
    }

    /** Longest streak ever recorded for the habit. */
    fun bestStreak(
        habitId: String,
        sessions: List<StudySessionEntity>,
        restores: List<RestoreHistoryEntity>
    ): Int {
        val days = activeDays(habitId, sessions, restores).toSortedSet()
        if (days.isEmpty()) return 0
        var best = 1
        var run = 1
        var previous: LocalDate? = null
        for (day in days) {
            if (previous != null && day == previous.plusDays(1)) {
                run++
                if (run > best) best = run
            } else {
                run = 1
            }
            previous = day
        }
        return best
    }

    /**
     * Length of the unbroken chain ending yesterday. Used to decide whether a
     * Restore Streak can actually reconnect a broken streak.
     */
    fun chainEndingYesterday(
        habitId: String,
        sessions: List<StudySessionEntity>,
        restores: List<RestoreHistoryEntity>
    ): Int {
        val days = activeDays(habitId, sessions, restores)
        var current = LocalDate.now().minusDays(1)
        var length = 0
        while (current in days) {
            length++
            current = current.minusDays(1)
        }
        return length
    }

    /** True when using a restore would reconnect a broken streak. */
    fun canRestore(
        habitId: String,
        sessions: List<StudySessionEntity>,
        restores: List<RestoreHistoryEntity>,
        remainingRestores: Int
    ): Boolean {
        val alreadyBroken = currentStreak(habitId, sessions, restores) == 0
        val hasChainToSave = chainEndingYesterday(habitId, sessions, restores) > 0
        return remainingRestores > 0 && alreadyBroken && hasChainToSave
    }
}
