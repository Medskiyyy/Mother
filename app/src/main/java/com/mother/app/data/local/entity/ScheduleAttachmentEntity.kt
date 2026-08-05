package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.AttachmentType

/**
 * An attachment belonging to a Schedule. Only the file path is stored;
 * the actual file lives in local storage.
 */
@Entity(
    tableName = "schedule_attachment",
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
data class ScheduleAttachmentEntity(
    @PrimaryKey val id: String,
    val scheduleId: String,
    val type: AttachmentType,
    val fileName: String,
    val filePath: String,
    val createdAt: Long
)
