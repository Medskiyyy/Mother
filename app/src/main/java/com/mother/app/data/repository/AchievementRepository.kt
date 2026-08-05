package com.mother.app.data.repository

import com.mother.app.data.local.dao.AchievementDao
import com.mother.app.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for achievements (PRD §20). */
interface AchievementRepository {
    fun observeAll(): Flow<List<AchievementEntity>>
    suspend fun updateProgress(id: String, progress: Int, unlocked: Boolean, unlockedAt: Long?)
}

class AchievementRepositoryImpl(private val dao: AchievementDao) : AchievementRepository {

    override fun observeAll(): Flow<List<AchievementEntity>> = dao.observeAll()

    override suspend fun updateProgress(id: String, progress: Int, unlocked: Boolean, unlockedAt: Long?) =
        dao.updateProgress(id, progress, unlocked, unlockedAt)
}
