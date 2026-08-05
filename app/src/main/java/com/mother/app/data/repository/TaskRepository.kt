package com.mother.app.data.repository

import com.mother.app.data.local.dao.TaskDao
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.StatusTask
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for tasks.
 * Enforces DATABASE_SCHEMA.md rules: title is required.
 */
interface TaskRepository {
    fun observeAll(): Flow<List<TaskEntity>>
    fun observeActiveTasks(): Flow<List<TaskEntity>>
    fun observeByStatus(status: String): Flow<List<TaskEntity>>
    fun observeUpcomingDeadlines(limit: Int): Flow<List<TaskEntity>>
    suspend fun getById(id: String): TaskEntity?
    suspend fun upsert(task: TaskEntity)
    suspend fun deleteById(id: String)
    suspend fun complete(id: String)
    suspend fun reopen(id: String)
}

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {
    override fun observeAll(): Flow<List<TaskEntity>> = dao.observeAll()
    override fun observeActiveTasks(): Flow<List<TaskEntity>> = dao.observeActiveTasks()
    override fun observeByStatus(status: String): Flow<List<TaskEntity>> = dao.observeByStatus(status)
    override fun observeUpcomingDeadlines(limit: Int): Flow<List<TaskEntity>> = dao.observeUpcomingDeadlines(limit)
    override suspend fun getById(id: String): TaskEntity? = dao.getById(id)

    override suspend fun complete(id: String) {
        val now = System.currentTimeMillis()
        dao.updateStatus(id, StatusTask.COMPLETED.name, now, now)
    }

    override suspend fun reopen(id: String) {
        val now = System.currentTimeMillis()
        dao.updateStatus(id, StatusTask.ACTIVE.name, null, now)
    }

    override suspend fun upsert(task: TaskEntity) {
        if (task.title.isBlank()) {
            throw ValidationException(ValidationException.Code.BLANK_TITLE, "Judul wajib diisi.")
        }
        dao.upsert(task.copy(title = task.title.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteById(id: String) = dao.deleteById(id)
}
