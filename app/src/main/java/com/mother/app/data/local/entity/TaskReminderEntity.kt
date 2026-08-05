package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A reminder belonging to a Task. */
@Entity(
    tableName = "task_reminder",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class TaskReminderEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val triggerTime: Long,
    val snoozeMinute: Int,
    val enabled: Boolean
)
