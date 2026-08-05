package com.mother.app.data.repository

import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.StatusSchedule
import com.mother.app.data.model.StatusTask
import com.mother.app.util.TimeUtils

/**
 * All progress statistics computed on demand from the source tables
 * (PRD §23, AGENT_RULES §17: never persist derived stats). Every field is
 * derived from StudySession / Task / Schedule / Habit rows, never stored.
 */
data class ProgressStatistics(
    // Study
    val totalStudyMinutes: Long = 0,
    val totalSessions: Int = 0,
    val averageSessionMinutes: Long = 0,
    // Habit
    val habitsCompletedDays: Int = 0,
    val habitsMissedDays: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    // Task
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0,
    val activeTasks: Int = 0,
    // Schedule
    val completedSchedules: Int = 0,
    val missedSchedules: Int = 0,
    val cancelledSchedules: Int = 0
)

/** Computes [ProgressStatistics] over a time window. */
class ProgressStatisticsCalculator {

    fun compute(
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        schedules: List<ScheduleEntity>,
        habits: List<HabitEntity>,
        windowStart: Long,
        windowEnd: Long
    ): ProgressStatistics {
        val sessionsInWindow = sessions.filter { it.startTime in windowStart until windowEnd }
        val totalStudyMinutes = sessionsInWindow.sumOf { it.durationMinute.toLong() }
        val totalSessions = sessionsInWindow.size
        val averageSessionMinutes =
            if (totalSessions > 0) totalStudyMinutes / totalSessions else 0L

        val tasksInWindow = tasks.filter { it.createdAt in windowStart until windowEnd }
        val now = System.currentTimeMillis()
        val completedTasks = tasksInWindow.count { it.status == StatusTask.COMPLETED }
        val overdueTasks = tasksInWindow.count {
            it.status != StatusTask.COMPLETED && it.deadline != null && it.deadline < now
        }
        val activeTasks = tasksInWindow.count { it.status != StatusTask.COMPLETED }

        val schedulesInWindow = schedules.filter { it.startTime in windowStart until windowEnd }
        val completedSchedules = schedulesInWindow.count { it.status == StatusSchedule.COMPLETED }
        val missedSchedules = schedulesInWindow.count { it.status == StatusSchedule.MISSED }
        val cancelledSchedules = schedulesInWindow.count { it.status == StatusSchedule.CANCELLED }

        // Habit day outcomes across the window: for each habit, each day it was
        // active, did the summed study minutes reach the target?
        var habitsCompletedDays = 0
        var habitsMissedDays = 0
        val sessionsByHabitDay = sessionsInWindow.groupBy {
            it.habitId to TimeUtils.startOfDay(it.startTime)
        }.mapValues { (_, list) -> list.sumOf { it.durationMinute } }
        for (habit in habits) {
            val createdDay = TimeUtils.startOfDay(habit.createdAt)
            var day = maxOf(createdDay, windowStart).let { TimeUtils.startOfDay(it) }
            val windowEndDayStart = TimeUtils.startOfDay(windowEnd - 1)
            while (day <= windowEndDayStart && day < now) {
                val minutes = sessionsByHabitDay[habit.id to day] ?: 0
                if (minutes >= habit.targetMinute) habitsCompletedDays++ else habitsMissedDays++
                day = TimeUtils.plusDays(day, 1)
            }
        }

        val sessionStarts = sessionsInWindow.map { it.startTime }
        return ProgressStatistics(
            totalStudyMinutes = totalStudyMinutes,
            totalSessions = totalSessions,
            averageSessionMinutes = averageSessionMinutes,
            habitsCompletedDays = habitsCompletedDays,
            habitsMissedDays = habitsMissedDays,
            currentStreak = TimeUtils.computeStreak(sessionStarts),
            bestStreak = TimeUtils.computeBestStreak(sessionStarts),
            totalTasks = tasksInWindow.size,
            completedTasks = completedTasks,
            overdueTasks = overdueTasks,
            activeTasks = activeTasks,
            completedSchedules = completedSchedules,
            missedSchedules = missedSchedules,
            cancelledSchedules = cancelledSchedules
        )
    }
}
