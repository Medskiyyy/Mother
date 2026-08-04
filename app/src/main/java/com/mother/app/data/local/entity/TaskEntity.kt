package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.Priority
import com.mother.app.data.model.StatusTask

/** A work item with an optional deadline. */
@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["deadline"]),
        Index(value = ["status"]),
        Index(value = ["priority"]),
        Index(value = ["categoryId"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val categoryId: String,
    val priority: Priority,
    val deadline: Long?,
    val status: StatusTask,
    val note: String?,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)