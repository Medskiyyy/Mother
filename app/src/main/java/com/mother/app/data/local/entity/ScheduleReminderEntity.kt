package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A reminder belonging to a Schedule. */
@Entity(
    tableName = "schedule_reminder",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scheduleId"])]
)
data class ScheduleReminderEntity(
    @PrimaryKey val id: String,
    val scheduleId: String,
    val triggerTime: Long,
    val snoozeMinute: Int,
    val enabled: Boolean
)
