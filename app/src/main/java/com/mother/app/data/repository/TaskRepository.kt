package com.mother.app.data.repository

import com.mother.app.data.local.dao.TaskDao
import com.mother.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for tasks. */
interface TaskRepository {
    fun observeByStatus(status: String): Flow<List<TaskEntity>>
    fun observeUpcomingDeadlines(limit: Int): Flow<List<TaskEntity>>
    suspend fun getById(id: String): TaskEntity?
    suspend fun upsert(task: TaskEntity)
    suspend fun deleteById(id: String)
}

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {
    override fun observeByStatus(status: String): Flow<List<TaskEntity>> = dao.observeByStatus(status)
    override fun observeUpcomingDeadlines(limit: Int): Flow<List<TaskEntity>> = dao.observeUpcomingDeadlines(limit)
    override suspend fun getById(id: String): TaskEntity? = dao.getById(id)
    override suspend fun upsert(task: TaskEntity) = dao.upsert(task)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}