package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievement ORDER BY target ASC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievement SET currentProgress = :progress, unlocked = :unlocked, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, unlocked: Boolean, unlockedAt: Long?)

    @Query("SELECT COUNT(*) FROM achievement")
    suspend fun count(): Int
}