package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.AttachmentType

/**
 * An attachment belonging to a Task. Only the file path is stored;
 * the actual file lives in local storage.
 */
@Entity(
    tableName = "task_attachment",
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
data class TaskAttachmentEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val type: AttachmentType,
    val fileName: String,
    val filePath: String,
    val createdAt: Long
)
