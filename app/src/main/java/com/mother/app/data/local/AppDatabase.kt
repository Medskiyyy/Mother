package com.mother.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mother.app.data.local.dao.AchievementDao
import com.mother.app.data.local.dao.AttachmentDao
import com.mother.app.data.local.dao.CategoryDao
import com.mother.app.data.local.dao.ChecklistDao
import com.mother.app.data.local.dao.HabitDao
import com.mother.app.data.local.dao.ReminderDao
import com.mother.app.data.local.dao.RestoreHistoryDao
import com.mother.app.data.local.dao.ScheduleDao
import com.mother.app.data.local.dao.SettingDao
import com.mother.app.data.local.dao.StudySessionDao
import com.mother.app.data.local.dao.TaskDao
import com.mother.app.data.local.entity.AchievementEntity
import com.mother.app.data.local.entity.AppSettingEntity
import com.mother.app.data.local.entity.AttachmentEntity
import com.mother.app.data.local.entity.CategoryEntity
import com.mother.app.data.local.entity.ChecklistEntity
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.ReminderEntity
import com.mother.app.data.local.entity.RestoreHistoryEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.Theme

@Database(
    entities = [
        CategoryEntity::class,
        AppSettingEntity::class,
        AchievementEntity::class,
        RestoreHistoryEntity::class,
        ScheduleEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        StudySessionEntity::class,
        ReminderEntity::class,
        ChecklistEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun settingDao(): SettingDao
    abstract fun achievementDao(): AchievementDao
    abstract fun restoreHistoryDao(): RestoreHistoryDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "mother.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(SeedCallback)
                .build()
    }
}

/** Seeds default categories, settings, and achievement definitions on first open. */
private object SeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()

        // Default categories (PRD §31).
        val categories = listOf(
            CategoryEntity("cat-kuliah", "Kuliah", "school", "#4A90D9", now, now),
            CategoryEntity("cat-belajar", "Belajar", "book", "#8E5BD2", now, now),
            CategoryEntity("cat-gym", "Gym", "fitness", "#3FA35C", now, now),
            CategoryEntity("cat-deadline", "Deadline", "alert", "#D9534F", now, now),
            CategoryEntity("cat-ibadah", "Ibadah", "menu_book", "#E0B33E", now, now)
        )
        categories.forEach { c ->
            db.execSQL(
                "INSERT OR IGNORE INTO category (id, name, icon, color, createdAt, updatedAt) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(c.id, c.name, c.icon, c.color, c.createdAt, c.updatedAt)
            )
        }

        // Single app setting row (default study target: 2 hours).
        db.execSQL(
            "INSERT OR IGNORE INTO app_setting (id, theme, reminderEnabled, aggressiveReminder, " +
                "defaultSnoozeMinute, defaultStudyTargetMinute, lastBackup, onboardingFinished) " +
                "VALUES (1, ?, ?, ?, ?, ?, NULL, 0)",
            arrayOf(Theme.SYSTEM.name, false, false, 10, 120)
        )

        // Achievement definitions (PRD §20).
        val achievements = listOf(
            Triple("ach-study-10", "Belajar 10 Jam", 10),
            Triple("ach-study-100", "Belajar 100 Jam", 100),
            Triple("ach-streak-7", "Streak 7 Hari", 7),
            Triple("ach-streak-30", "Streak 30 Hari", 30),
            Triple("ach-streak-100", "Streak 100 Hari", 100),
            Triple("ach-task-100", "100 Task Selesai", 100),
            Triple("ach-task-500", "500 Task Selesai", 500),
            Triple("ach-activity-100", "100 Activity Selesai", 100)
        )
        achievements.forEach { (id, title, target) ->
            db.execSQL(
                "INSERT OR IGNORE INTO achievement (id, title, description, icon, target, " +
                    "currentProgress, unlocked, unlockedAt) VALUES (?, ?, ?, ?, ?, 0, 0, NULL)",
                arrayOf(id, title, title, "emoji_events", target)
            )
        }
    }
}