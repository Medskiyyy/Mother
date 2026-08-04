package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Records a Restore Streak usage, used to limit to 2 per month. */
@Entity(
    tableName = "restore_history",
    indices = [Index(value = ["restoreDate"])]
)
data class RestoreHistoryEntity(
    @PrimaryKey val id: String,
    val restoreDate: Long,
    val reason: String?
)