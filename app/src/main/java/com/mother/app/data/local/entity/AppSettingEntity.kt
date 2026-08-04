package com.mother.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mother.app.data.model.Theme

/** Single-row application configuration. */
@Entity(tableName = "app_setting")
data class AppSettingEntity(
    @PrimaryKey val id: Int = 1,
    val theme: Theme,
    val reminderEnabled: Boolean,
    val aggressiveReminder: Boolean,
    val defaultSnoozeMinute: Int,
    val defaultStudyTargetMinute: Int,
    val lastBackup: Long?,
    val onboardingFinished: Boolean
)