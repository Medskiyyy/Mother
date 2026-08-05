package com.mother.app.data.repository

import com.mother.app.data.local.dao.HabitReminderDao
import com.mother.app.data.local.dao.ScheduleReminderDao
import com.mother.app.data.local.dao.TaskReminderDao
import com.mother.app.data.local.entity.HabitReminderEntity
import com.mother.app.data.local.entity.ScheduleReminderEntity
import com.mother.app.data.local.entity.TaskReminderEntity
import kotlinx.coroutines.flow.Flow

/** Single source of truth for reminders of tasks, schedules, and habits. */
interface ReminderRepository {

    fun observeTaskReminders(taskId: String): Flow<List<TaskReminderEntity>>
    fun observeScheduleReminders(scheduleId: String): Flow<List<ScheduleReminderEntity>>
    fun observeHabitReminders(habitId: String): Flow<List<HabitReminderEntity>>

    suspend fun getTaskReminder(id: String): TaskReminderEntity?
    suspend fun getScheduleReminder(id: String): ScheduleReminderEntity?
    suspend fun getHabitReminder(id: String): HabitReminderEntity?

    suspend fun getAllTaskReminders(): List<TaskReminderEntity>
    suspend fun getAllScheduleReminders(): List<ScheduleReminderEntity>
    suspend fun getAllHabitReminders(): List<HabitReminderEntity>

    suspend fun upsertTaskReminder(reminder: TaskReminderEntity)
    suspend fun upsertScheduleReminder(reminder: ScheduleReminderEntity)
    suspend fun upsertHabitReminder(reminder: HabitReminderEntity)

    suspend fun deleteTaskReminders(taskId: String)
    suspend fun deleteScheduleReminders(scheduleId: String)
    suspend fun deleteHabitReminders(habitId: String)
}

class ReminderRepositoryImpl(
    private val taskReminderDao: TaskReminderDao,
    private val scheduleReminderDao: ScheduleReminderDao,
    private val habitReminderDao: HabitReminderDao
) : ReminderRepository {

    override fun observeTaskReminders(taskId: String): Flow<List<TaskReminderEntity>> =
        taskReminderDao.observeForTask(taskId)

    override fun observeScheduleReminders(scheduleId: String): Flow<List<ScheduleReminderEntity>> =
        scheduleReminderDao.observeForSchedule(scheduleId)

    override fun observeHabitReminders(habitId: String): Flow<List<HabitReminderEntity>> =
        habitReminderDao.observeForHabit(habitId)

    override suspend fun getTaskReminder(id: String): TaskReminderEntity? =
        taskReminderDao.getById(id)

    override suspend fun getScheduleReminder(id: String): ScheduleReminderEntity? =
        scheduleReminderDao.getById(id)

    override suspend fun getHabitReminder(id: String): HabitReminderEntity? =
        habitReminderDao.getById(id)

    override suspend fun getAllTaskReminders(): List<TaskReminderEntity> =
        taskReminderDao.getAll()

    override suspend fun getAllScheduleReminders(): List<ScheduleReminderEntity> =
        scheduleReminderDao.getAll()

    override suspend fun getAllHabitReminders(): List<HabitReminderEntity> =
        habitReminderDao.getAll()

    override suspend fun upsertTaskReminder(reminder: TaskReminderEntity) =
        taskReminderDao.upsert(reminder)

    override suspend fun upsertScheduleReminder(reminder: ScheduleReminderEntity) =
        scheduleReminderDao.upsert(reminder)

    override suspend fun upsertHabitReminder(reminder: HabitReminderEntity) =
        habitReminderDao.upsert(reminder)

    override suspend fun deleteTaskReminders(taskId: String) =
        taskReminderDao.deleteForTask(taskId)

    override suspend fun deleteScheduleReminders(scheduleId: String) =
        scheduleReminderDao.deleteForSchedule(scheduleId)

    override suspend fun deleteHabitReminders(habitId: String) =
        habitReminderDao.deleteForHabit(habitId)
}
