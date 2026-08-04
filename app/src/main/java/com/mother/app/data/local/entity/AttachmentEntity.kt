package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mother.app.data.model.AttachmentType
import com.mother.app.data.model.OwnerType

/**
 * A generic attachment for Task and Schedule. Only the file path is stored;
 * the actual file lives in local storage.
 */
@Entity(
    tableName = "attachment",
    indices = [Index(value = ["ownerType", "ownerId"])]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val ownerType: OwnerType,
    val ownerId: String,
    val type: AttachmentType,
    val fileName: String,
    val filePath: String,
    val createdAt: Long
)