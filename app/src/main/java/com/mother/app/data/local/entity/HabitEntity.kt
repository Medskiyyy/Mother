package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.RepeatType

/** A habit to build consistency. Progress is derived from StudySession, never stored. */
@Entity(
    tableName = "habit",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["title"]),
        Index(value = ["categoryId"]),
        Index(value = ["archived"])
    ]
)
data class HabitEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val targetMinute: Int,
    val repeatType: RepeatType,
    val customRepeatRule: String?,
    val reminderEnabled: Boolean,
    val color: String,
    val icon: String,
    val note: String?,
    val archived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)