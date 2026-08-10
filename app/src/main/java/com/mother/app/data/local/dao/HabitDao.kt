package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mother.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit WHERE archived = 0 ORDER BY title ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT COUNT(*) FROM habit WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: String): Int

    @Query("UPDATE habit SET archived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateArchived(id: String, archived: Boolean, updatedAt: Long)

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Upsert
    suspend fun upsert(habit: HabitEntity)

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun deleteById(id: String)
}