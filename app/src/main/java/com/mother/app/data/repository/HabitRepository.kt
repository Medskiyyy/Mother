package com.mother.app.data.repository

import com.mother.app.data.local.dao.HabitDao
import com.mother.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for habits. */
interface HabitRepository {
    fun observeActive(): Flow<List<HabitEntity>>
    suspend fun getById(id: String): HabitEntity?
    suspend fun upsert(habit: HabitEntity)
    suspend fun deleteById(id: String)
}

class HabitRepositoryImpl(private val dao: HabitDao) : HabitRepository {
    override fun observeActive(): Flow<List<HabitEntity>> = dao.observeActive()
    override suspend fun getById(id: String): HabitEntity? = dao.getById(id)
    override suspend fun upsert(habit: HabitEntity) = dao.upsert(habit)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}