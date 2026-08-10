package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mother.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task WHERE status = :status ORDER BY deadline ASC")
    fun observeByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE status != 'COMPLETED' ORDER BY (deadline IS NULL), deadline ASC, createdAt ASC")
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM task WHERE status = 'COMPLETED'")
    suspend fun countCompleted(): Int

    @Query("UPDATE task SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, completedAt: Long?, updatedAt: Long)

    @Query("SELECT * FROM task WHERE status != 'COMPLETED' AND deadline IS NOT NULL ORDER BY deadline ASC LIMIT :limit")
    fun observeUpcomingDeadlines(limit: Int): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM task WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: String): Int

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun deleteById(id: String)
}