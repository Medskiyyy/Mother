package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A checklist item belonging to a Task. */
@Entity(
    tableName = "checklist",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["orderIndex"])
    ]
)
data class ChecklistEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val checked: Boolean,
    val orderIndex: Int
)