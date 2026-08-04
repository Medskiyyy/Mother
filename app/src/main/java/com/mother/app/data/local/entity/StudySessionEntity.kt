package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.SessionSource

/** One study session. All study statistics derive from this table. */
@Entity(
    tableName = "study_session",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId"]),
        Index(value = ["startTime"]),
        Index(value = ["createdAt"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinute: Int,
    val source: SessionSource,
    val note: String?,
    val createdAt: Long
)