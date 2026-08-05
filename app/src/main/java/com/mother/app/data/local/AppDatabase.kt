package com.mother.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mother.app.data.local.dao.AchievementDao
import com.mother.app.data.local.dao.CategoryDao
import com.mother.app.data.local.dao.ChecklistDao
import com.mother.app.data.local.dao.HabitDao
import com.mother.app.data.local.dao.HabitReminderDao
import com.mother.app.data.local.dao.RestoreHistoryDao
import com.mother.app.data.local.dao.ScheduleAttachmentDao
import com.mother.app.data.local.dao.ScheduleDao
import com.mother.app.data.local.dao.ScheduleReminderDao
import com.mother.app.data.local.dao.SettingDao
import com.mother.app.data.local.dao.StudySessionDao
import com.mother.app.data.local.dao.TaskAttachmentDao
import com.mother.app.data.local.dao.TaskDao
import com.mother.app.data.local.dao.TaskReminderDao
import com.mother.app.data.local.entity.AchievementEntity
import com.mother.app.data.local.entity.AppSettingEntity
import com.mother.app.data.local.entity.CategoryEntity
import com.mother.app.data.local.entity.ChecklistEntity
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.HabitReminderEntity
import com.mother.app.data.local.entity.RestoreHistoryEntity
import com.mother.app.data.local.entity.ScheduleAttachmentEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.ScheduleReminderEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskAttachmentEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.local.entity.TaskReminderEntity
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
        ChecklistEntity::class,
        TaskReminderEntity::class,
        ScheduleReminderEntity::class,
        HabitReminderEntity::class,
        TaskAttachmentEntity::class,
        ScheduleAttachmentEntity::class
    ],
    version = 2,
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
    abstract fun checklistDao(): ChecklistDao
    abstract fun taskReminderDao(): TaskReminderDao
    abstract fun scheduleReminderDao(): ScheduleReminderDao
    abstract fun habitReminderDao(): HabitReminderDao
    abstract fun taskAttachmentDao(): TaskAttachmentDao
    abstract fun scheduleAttachmentDao(): ScheduleAttachmentDao

    companion object {
        const val DATABASE_NAME = "mother.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(SeedCallback)
                .addMigrations(MIGRATION_1_2)
                .build()

        /**
         * Replaces the generic reminder/attachment tables with per-owner tables
         * (DATABASE_SCHEMA.md part 3). The generic tables were scaffold-only;
         * no app version has shipped with user data in them.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS reminder")
                db.execSQL("DROP TABLE IF EXISTS attachment")

                db.execSQL(
                    "CREATE TABLE `task_reminder` (" +
                        "`id` TEXT NOT NULL, " +
                        "`taskId` TEXT NOT NULL, " +
                        "`triggerTime` INTEGER NOT NULL, " +
                        "`snoozeMinute` INTEGER NOT NULL, " +
                        "`enabled` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`taskId`) REFERENCES `task`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_reminder_taskId` ON `task_reminder` (`taskId`)")

                db.execSQL(
                    "CREATE TABLE `schedule_reminder` (" +
                        "`id` TEXT NOT NULL, " +
                        "`scheduleId` TEXT NOT NULL, " +
                        "`triggerTime` INTEGER NOT NULL, " +
                        "`snoozeMinute` INTEGER NOT NULL, " +
                        "`enabled` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`scheduleId`) REFERENCES `schedule`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_reminder_scheduleId` ON `schedule_reminder` (`scheduleId`)")

                db.execSQL(
                    "CREATE TABLE `habit_reminder` (" +
                        "`id` TEXT NOT NULL, " +
                        "`habitId` TEXT NOT NULL, " +
                        "`triggerTime` INTEGER NOT NULL, " +
                        "`snoozeMinute` INTEGER NOT NULL, " +
                        "`enabled` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`habitId`) REFERENCES `habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_reminder_habitId` ON `habit_reminder` (`habitId`)")

                db.execSQL(
                    "CREATE TABLE `task_attachment` (" +
                        "`id` TEXT NOT NULL, " +
                        "`taskId` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`fileName` TEXT NOT NULL, " +
                        "`filePath` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`taskId`) REFERENCES `task`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_attachment_taskId` ON `task_attachment` (`taskId`)")

                db.execSQL(
                    "CREATE TABLE `schedule_attachment` (" +
                        "`id` TEXT NOT NULL, " +
                        "`scheduleId` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`fileName` TEXT NOT NULL, " +
                        "`filePath` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`scheduleId`) REFERENCES `schedule`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_attachment_scheduleId` ON `schedule_attachment` (`scheduleId`)")
            }
        }
    }
}

/** Seeds default categories, settings, and achievement definitions on first open. */
private object SeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
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
