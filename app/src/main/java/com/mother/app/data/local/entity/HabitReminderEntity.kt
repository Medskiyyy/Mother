package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A reminder belonging to a Habit. */
@Entity(
    tableName = "habit_reminder",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"])]
)
data class HabitReminderEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val triggerTime: Long,
    val snoozeMinute: Int,
    val enabled: Boolean
)
