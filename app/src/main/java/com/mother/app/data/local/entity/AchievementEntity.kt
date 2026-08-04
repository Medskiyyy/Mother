package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A predefined achievement. Progress is computed automatically. */
@Entity(
    tableName = "achievement",
    indices = [Index(value = ["unlocked"])]
)
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val target: Int,
    val currentProgress: Int,
    val unlocked: Boolean,
    val unlockedAt: Long?
)