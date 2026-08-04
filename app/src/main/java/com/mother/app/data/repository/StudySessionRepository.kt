package com.mother.app.data.repository

import com.mother.app.data.local.dao.StudySessionDao
import com.mother.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for study sessions. All study stats derive from here. */
interface StudySessionRepository {
    fun observeRange(from: Long, to: Long): Flow<List<StudySessionEntity>>
    fun observeForHabit(habitId: String): Flow<List<StudySessionEntity>>
    fun observeTotalMinutesRange(from: Long, to: Long): Flow<Int>
    fun observeAllAsc(): Flow<List<StudySessionEntity>>
    suspend fun getById(id: String): StudySessionEntity?
    suspend fun upsert(session: StudySessionEntity)
    suspend fun deleteById(id: String)
}

class StudySessionRepositoryImpl(private val dao: StudySessionDao) : StudySessionRepository {
    override fun observeRange(from: Long, to: Long): Flow<List<StudySessionEntity>> =
        dao.observeRange(from, to)

    override fun observeForHabit(habitId: String): Flow<List<StudySessionEntity>> =
        dao.observeForHabit(habitId)

    override fun observeTotalMinutesRange(from: Long, to: Long): Flow<Int> =
        dao.observeTotalMinutesRange(from, to)

    override fun observeAllAsc(): Flow<List<StudySessionEntity>> = dao.observeAllAsc()
    override suspend fun getById(id: String): StudySessionEntity? = dao.getById(id)
    override suspend fun upsert(session: StudySessionEntity) = dao.upsert(session)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
}