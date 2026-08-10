package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mother.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_session WHERE startTime >= :from AND startTime < :to ORDER BY startTime DESC")
    fun observeRange(from: Long, to: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_session WHERE habitId = :habitId ORDER BY startTime DESC")
    fun observeForHabit(habitId: String): Flow<List<StudySessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinute), 0) FROM study_session WHERE startTime >= :from AND startTime < :to")
    fun observeTotalMinutesRange(from: Long, to: Long): Flow<Int>

    @Query("SELECT * FROM study_session ORDER BY startTime ASC")
    fun observeAllAsc(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_session WHERE id = :id")
    suspend fun getById(id: String): StudySessionEntity?

    @Upsert
    suspend fun upsert(session: StudySessionEntity)

    @Query("DELETE FROM study_session WHERE id = :id")
    suspend fun deleteById(id: String)
}