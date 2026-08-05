package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime ASC")
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule WHERE startTime >= :from AND startTime < :to ORDER BY startTime ASC")
    fun observeRange(from: Long, to: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT COUNT(*) FROM schedule WHERE startTime < :end AND endTime > :start AND id != :excludeId")
    suspend fun countOverlapping(start: Long, end: Long, excludeId: String): Int

    @Query("SELECT COUNT(*) FROM schedule WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: String): Int

    @Query("SELECT * FROM schedule ORDER BY startTime ASC")
    fun observeAll(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule WHERE id = :id")
    suspend fun getById(id: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: ScheduleEntity)

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun deleteById(id: String)
}