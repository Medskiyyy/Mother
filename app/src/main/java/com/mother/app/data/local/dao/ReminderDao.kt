package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.HabitReminderEntity
import com.mother.app.data.local.entity.ScheduleReminderEntity
import com.mother.app.data.local.entity.TaskReminderEntity
import kotlinx.coroutines.flow.Flow

/** DAOs for the per-owner reminder tables (DATABASE_SCHEMA.md part 3). */

@Dao
interface TaskReminderDao {

    @Query("SELECT * FROM task_reminder WHERE id = :id")
    suspend fun getById(id: String): TaskReminderEntity?

    @Query("SELECT * FROM task_reminder WHERE taskId = :taskId ORDER BY triggerTime ASC")
    fun observeForTask(taskId: String): Flow<List<TaskReminderEntity>>

    @Query("SELECT * FROM task_reminder WHERE enabled = 1 AND triggerTime <= :until ORDER BY triggerTime ASC")
    fun observeDue(until: Long): Flow<List<TaskReminderEntity>>

    @Query("SELECT * FROM task_reminder")
    suspend fun getAll(): List<TaskReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: TaskReminderEntity)

    @Query("DELETE FROM task_reminder WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: String)
}

@Dao
interface ScheduleReminderDao {

    @Query("SELECT * FROM schedule_reminder WHERE id = :id")
    suspend fun getById(id: String): ScheduleReminderEntity?

    @Query("SELECT * FROM schedule_reminder WHERE scheduleId = :scheduleId ORDER BY triggerTime ASC")
    fun observeForSchedule(scheduleId: String): Flow<List<ScheduleReminderEntity>>

    @Query("SELECT * FROM schedule_reminder WHERE enabled = 1 AND triggerTime <= :until ORDER BY triggerTime ASC")
    fun observeDue(until: Long): Flow<List<ScheduleReminderEntity>>

    @Query("SELECT * FROM schedule_reminder")
    suspend fun getAll(): List<ScheduleReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: ScheduleReminderEntity)

    @Query("DELETE FROM schedule_reminder WHERE scheduleId = :scheduleId")
    suspend fun deleteForSchedule(scheduleId: String)
}

@Dao
interface HabitReminderDao {

    @Query("SELECT * FROM habit_reminder WHERE id = :id")
    suspend fun getById(id: String): HabitReminderEntity?

    @Query("SELECT * FROM habit_reminder WHERE habitId = :habitId ORDER BY triggerTime ASC")
    fun observeForHabit(habitId: String): Flow<List<HabitReminderEntity>>

    @Query("SELECT * FROM habit_reminder WHERE enabled = 1 AND triggerTime <= :until ORDER BY triggerTime ASC")
    fun observeDue(until: Long): Flow<List<HabitReminderEntity>>

    @Query("SELECT * FROM habit_reminder")
    suspend fun getAll(): List<HabitReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: HabitReminderEntity)

    @Query("DELETE FROM habit_reminder WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: String)
}
