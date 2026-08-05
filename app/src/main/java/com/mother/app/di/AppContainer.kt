package com.mother.app.di

import android.content.Context
import com.mother.app.data.local.AppDatabase
import com.mother.app.data.repository.CategoryRepository
import com.mother.app.data.repository.CategoryRepositoryImpl
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.HabitRepositoryImpl
import com.mother.app.data.repository.ReminderRepository
import com.mother.app.data.repository.ReminderRepositoryImpl
import com.mother.app.data.repository.RestoreStreakRepository
import com.mother.app.data.repository.RestoreStreakRepositoryImpl
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.ScheduleRepositoryImpl
import com.mother.app.data.repository.ScheduleStatusSyncer
import com.mother.app.data.repository.SettingRepository
import com.mother.app.data.repository.SettingRepositoryImpl
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.StudySessionRepositoryImpl
import com.mother.app.data.repository.TaskRepository
import com.mother.app.data.repository.TaskRepositoryImpl

/**
 * Manual dependency container. Avoids pulling in a DI framework for a small app
 * (AGENT_RULES: don't add libraries without reason). Owns the database and all
 * repository singletons.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase by lazy {
        AppDatabase.build(context.applicationContext)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(
            dao = database.categoryDao(),
            taskDao = database.taskDao(),
            scheduleDao = database.scheduleDao(),
            habitDao = database.habitDao()
        )
    }
    val settingRepository: SettingRepository by lazy { SettingRepositoryImpl(database.settingDao()) }
    val scheduleRepository: ScheduleRepository by lazy { ScheduleRepositoryImpl(database.scheduleDao()) }
    val scheduleStatusSyncer: ScheduleStatusSyncer by lazy { ScheduleStatusSyncer(scheduleRepository) }
    val taskRepository: TaskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }
    val habitRepository: HabitRepository by lazy { HabitRepositoryImpl(database.habitDao()) }
    val studySessionRepository: StudySessionRepository by lazy { StudySessionRepositoryImpl(database.studySessionDao()) }
    val restoreStreakRepository: RestoreStreakRepository by lazy { RestoreStreakRepositoryImpl(database.restoreHistoryDao()) }
    val reminderRepository: ReminderRepository by lazy {
        ReminderRepositoryImpl(
            taskReminderDao = database.taskReminderDao(),
            scheduleReminderDao = database.scheduleReminderDao(),
            habitReminderDao = database.habitReminderDao()
        )
    }
}