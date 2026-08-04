package com.mother.app.data.repository

import com.mother.app.data.local.dao.ScheduleDao
import com.mother.app.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for schedules. */
interface ScheduleRepository {
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<ScheduleEntity>>
    fun observeRange(from: Long, to: Long): Flow<List<ScheduleEntity>>
    suspend fun getById(id: String): ScheduleEntity?
    suspend fun upsert(schedule: ScheduleEntity)
    suspend fun deleteById(id: String)
}

class ScheduleRepositoryImpl(private val dao: ScheduleDao) : ScheduleRepository {
    override fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<ScheduleEntity>> =
        dao.observeForDay(dayStart, dayEnd)

    override fun observeRange(from: Long, to: Long): Flow<List<ScheduleEntity>> =
        dao.observeRange(from, to)

    override suspend fun getById(id: String): ScheduleEntity? = dao.getById(id)
    override suspend fun upsert(schedule: ScheduleEntity) = dao.upsert(schedule)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}