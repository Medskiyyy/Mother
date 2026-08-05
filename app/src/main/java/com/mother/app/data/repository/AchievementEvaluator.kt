package com.mother.app.data.repository

import com.mother.app.data.local.entity.AchievementEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.StatusSchedule
import com.mother.app.data.model.StatusTask
import com.mother.app.util.TimeUtils

/**
 * Evaluates achievements from the source tables (PRD §20). Definitions are
 * seeded in the database; progress is always recomputed, never trusted from
 * stored values.
 */
class AchievementEvaluator {

    data class Result(val progress: Int, val unlocked: Boolean)

    fun evaluate(
        achievements: List<AchievementEntity>,
        sessions: List<StudySessionEntity>,
        tasks: List<TaskEntity>,
        schedules: List<ScheduleEntity>
    ): Map<String, Result> {
        val totalStudyHours = sessions.sumOf { it.durationMinute.toLong() } / 60L
        val bestStreak = TimeUtils.computeBestStreak(sessions.map { it.startTime }).toLong()
        val completedTasks = tasks.count { it.status == StatusTask.COMPLETED }.toLong()
        val completedActivities = schedules.count { it.status == StatusSchedule.COMPLETED }.toLong()

        return achievements.associate { achievement ->
            val metric = when {
                achievement.id.startsWith("ach-study") -> totalStudyHours
                achievement.id.startsWith("ach-streak") -> bestStreak
                achievement.id.startsWith("ach-task") -> completedTasks
                achievement.id.startsWith("ach-activity") -> completedActivities
                else -> 0L
            }
            achievement.id to Result(
                progress = metric.coerceAtMost(achievement.target.toLong()).toInt(),
                unlocked = metric >= achievement.target
            )
        }
    }
}
