package com.mother.app.data.repository

import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.model.StatusSchedule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Advances non-terminal schedule statuses based on wall-clock time (PRD §12:
 * statuses change automatically). Only repeats of RepeatType.NONE are treated
 * as one-shot instances; recurring schedules stay live so future occurrences
 * keep showing (repeat expansion lands in a later phase).
 */
class ScheduleStatusSyncer(private val scheduleRepository: ScheduleRepository) {

    /** Expected status of [schedule] at [now], or null when it should not change. */
    fun statusAt(schedule: ScheduleEntity, now: Long): StatusSchedule? = when {
        schedule.repeatType == com.mother.app.data.model.RepeatType.NONE &&
            schedule.status == StatusSchedule.UPCOMING && now >= schedule.endTime ->
            StatusSchedule.MISSED
        schedule.status == StatusSchedule.UPCOMING && now >= schedule.startTime && now < schedule.endTime ->
            StatusSchedule.RUNNING
        schedule.status == StatusSchedule.RUNNING && now >= schedule.endTime ->
            StatusSchedule.COMPLETED
        else -> null
    }

    /** Reconciles all pending schedules once; safe to call repeatedly. */
    suspend fun syncNow(now: Long = System.currentTimeMillis()) = coroutineScope {
        val pending = scheduleRepository.getPending()
        pending.forEach { schedule ->
            val newStatus = statusAt(schedule, now) ?: return@forEach
            launch { scheduleRepository.updateStatus(schedule.id, newStatus, now) }
        }
    }
}
