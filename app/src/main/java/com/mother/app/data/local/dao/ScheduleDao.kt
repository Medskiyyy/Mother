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

    @Query("SELECT * FROM schedule WHERE id = :id")
    suspend fun getById(id: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: ScheduleEntity)

    @Query("DELETE FROM schedule WHERE id = :id")
    suspend fun deleteById(id: String)
}