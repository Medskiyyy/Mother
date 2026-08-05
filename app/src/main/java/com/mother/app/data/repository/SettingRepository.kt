package com.mother.app.data.repository

import com.mother.app.data.local.dao.SettingDao
import com.mother.app.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for app settings. */
interface SettingRepository {
    fun observeSetting(): Flow<AppSettingEntity?>
    suspend fun getSetting(): AppSettingEntity?
    suspend fun updateTheme(theme: String)
    suspend fun updateLastBackup(lastBackup: Long)
    suspend fun setOnboardingFinished(finished: Boolean)
}

class SettingRepositoryImpl(private val dao: SettingDao) : SettingRepository {
    override fun observeSetting(): Flow<AppSettingEntity?> = dao.observeSetting()
    override suspend fun getSetting(): AppSettingEntity? = dao.getSetting()
    override suspend fun updateTheme(theme: String) = dao.updateTheme(theme)
    override suspend fun updateLastBackup(lastBackup: Long) = dao.updateLastBackup(lastBackup)
    override suspend fun setOnboardingFinished(finished: Boolean) =
        dao.updateOnboardingFinished(finished)
}