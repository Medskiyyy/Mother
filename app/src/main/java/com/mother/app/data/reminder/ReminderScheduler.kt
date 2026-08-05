package com.mother.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService

/**
 * Schedules exact alarms for reminders (PRD §27). Each alarm fires
 * [ReminderReceiver], which shows the notification the user must respond to.
 * Uses [AlarmManager.setExactAndAllowWhileIdle] where allowed so reminders
 * still ring while the device is idle or dozing; falls back to an inexact
 * alarm when the exact-alarm permission is not granted (API 31+).
 */
object ReminderScheduler {

    const val OWNER_TASK = "task"
    const val OWNER_SCHEDULE = "schedule"
    const val OWNER_HABIT = "habit"

    const val EXTRA_OWNER_TYPE = "owner_type"
    const val EXTRA_OWNER_ID = "owner_id"
    const val EXTRA_REMINDER_ID = "reminder_id"

    /**
     * Schedules the alarm. The display title is intentionally NOT an extra: the
     * receiver loads it from the database, keeping schedule/cancel intents
     * identical so [cancel] can find the pending intent.
     */
    fun schedule(context: Context, ownerType: String, ownerId: String, reminderId: String, triggerTime: Long) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val pendingIntent = buildPendingIntent(context, ownerType, ownerId, reminderId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /**
     * Schedules a daily repeating alarm (used for habit reminders, which ring
     * every day at the same wall-clock time). Exact repeating alarms are not
     * available while dozing, so this uses an inexact repeating alarm.
     */
    fun scheduleRepeating(
        context: Context,
        ownerType: String,
        ownerId: String,
        reminderId: String,
        triggerTime: Long
    ) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val pendingIntent = buildPendingIntent(context, ownerType, ownerId, reminderId)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(context: Context, ownerType: String, ownerId: String, reminderId: String) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val pendingIntent = buildPendingIntent(
            context,
            ownerType,
            ownerId,
            reminderId,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildPendingIntent(
        context: Context,
        ownerType: String,
        ownerId: String,
        reminderId: String,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_OWNER_TYPE, ownerType)
            putExtra(EXTRA_OWNER_ID, ownerId)
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(context, requestCode(reminderId), intent, flags)
    }

    /** Stable request code derived from the reminder id (UUID -> Int). */
    fun requestCode(reminderId: String): Int = reminderId.hashCode()
}
