package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.Priority
import com.mother.app.data.model.RepeatType
import com.mother.app.data.model.StatusSchedule

/** A time-bound activity. */
@Entity(
    tableName = "schedule",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["endTime"]),
        Index(value = ["categoryId"]),
        Index(value = ["status"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val categoryId: String,
    val priority: Priority,
    val startTime: Long,
    val endTime: Long,
    val repeatType: RepeatType,
    val customRepeatRule: String?,
    val location: String?,
    val note: String?,
    val status: StatusSchedule,
    val createdAt: Long,
    val updatedAt: Long
)