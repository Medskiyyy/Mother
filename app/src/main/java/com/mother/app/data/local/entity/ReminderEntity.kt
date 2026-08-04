package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.OwnerType

/**
 * A generic reminder shared by Task, Schedule, and Habit.
 * No FK because the owner can live in several tables; owner validity is enforced in the repository.
 */
@Entity(
    tableName = "reminder",
    indices = [Index(value = ["ownerType", "ownerId"])]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val ownerType: OwnerType,
    val ownerId: String,
    val triggerTime: Long,
    val snoozeMinute: Int,
    val enabled: Boolean,
    val createdAt: Long
)