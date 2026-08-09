package com.mother.app.data.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mother.app.MotherApplication
import com.mother.app.MainActivity
import com.mother.app.R
import com.mother.app.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Fires when a reminder alarm rings and shows the notification the user must
 * respond to: Mulai / Snooze / Lewati (PRD §27). Snooze reschedules the same
 * reminder a few minutes later; a reminder never disappears on its own.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ownerType = intent.getStringExtra(ReminderScheduler.EXTRA_OWNER_TYPE) ?: return
        val ownerId = intent.getStringExtra(ReminderScheduler.EXTRA_OWNER_ID) ?: return
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: return

        when (intent.action) {
            ACTION_SNOOZE -> handleSnooze(context, ownerType, ownerId, reminderId)
            ACTION_DISMISS -> handleDismiss(context, ownerType, ownerId, reminderId)
            ACTION_START -> handleStart(context, ownerType, ownerId, reminderId)
            else -> handleRing(context, ownerType, ownerId, reminderId)
        }
    }

    private fun handleRing(context: Context, ownerType: String, ownerId: String, reminderId: String) {
        val pending = goAsync()
        val container = (context.applicationContext as MotherApplication).container

        // Acquire a WakeLock to guarantee the device stays awake during alarm launch
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.FULL_WAKE_LOCK or
                    android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    android.os.PowerManager.ON_AFTER_RELEASE,
            "Mother:AlarmWakeLock"
        )
        wakeLock.acquire(60_000L) // 60 seconds max

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val title = loadTitle(container.reminderRepository, container, ownerType, ownerId)
                if (ownerType == ReminderScheduler.OWNER_HABIT) {
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra(ReminderScheduler.EXTRA_OWNER_TYPE, ownerType)
                        putExtra(ReminderScheduler.EXTRA_OWNER_ID, ownerId)
                        putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
                        putExtra("title", title)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(alarmIntent)

                    // Re-schedule for the next day since setAlarmClock is one-shot
                    val nextTrigger = System.currentTimeMillis() + android.app.AlarmManager.INTERVAL_DAY
                    ReminderScheduler.schedule(context, ownerType, ownerId, reminderId, nextTrigger)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                ensureChannel(notificationManager, context)
                notificationManager.notify(
                    ReminderScheduler.requestCode(reminderId),
                    buildNotification(context, ownerType, ownerId, reminderId, title)
                )
            } finally {
                try { wakeLock.release() } catch (_: Exception) {}
                pending.finish()
            }
        }
    }

    /** Re-arms the alarm [snoozeMinute] from now (fallback: the default setting). */
    private fun handleSnooze(context: Context, ownerType: String, ownerId: String, reminderId: String) {
        dismissNotification(context, reminderId)
        val pending = goAsync()
        val container = (context.applicationContext as MotherApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snoozeMinute = snoozeMinuteFor(container.reminderRepository, ownerType, reminderId)
                    ?: container.settingRepository.getSetting()?.defaultSnoozeMinute ?: DEFAULT_SNOOZE_MINUTE
                val newTrigger = System.currentTimeMillis() + snoozeMinute * 60_000L
                ReminderScheduler.schedule(context, ownerType, ownerId, reminderId, newTrigger)
            } finally {
                pending.finish()
            }
        }
    }

    private fun handleDismiss(context: Context, ownerType: String, ownerId: String, reminderId: String) {
        dismissNotification(context, reminderId)
    }

    /** "Mulai": dismiss the reminder and bring the user into the app. */
    private fun handleStart(context: Context, ownerType: String, ownerId: String, reminderId: String) {
        dismissNotification(context, reminderId)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(openIntent)
    }

    private fun dismissNotification(context: Context, reminderId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ReminderScheduler.requestCode(reminderId))
    }

    private suspend fun loadTitle(
        reminderRepository: ReminderRepository,
        container: com.mother.app.di.AppContainer,
        ownerType: String,
        ownerId: String
    ): String = when (ownerType) {
        ReminderScheduler.OWNER_TASK -> container.taskRepository.getById(ownerId)?.title
        ReminderScheduler.OWNER_SCHEDULE -> container.scheduleRepository.getById(ownerId)?.title
        ReminderScheduler.OWNER_HABIT -> container.habitRepository.getById(ownerId)?.title
        else -> null
    } ?: ""

    private suspend fun snoozeMinuteFor(
        reminderRepository: ReminderRepository,
        ownerType: String,
        reminderId: String
    ): Int? = when (ownerType) {
        ReminderScheduler.OWNER_TASK ->
            reminderRepository.getTaskReminder(reminderId)?.snoozeMinute?.takeIf { it > 0 }
        ReminderScheduler.OWNER_SCHEDULE ->
            reminderRepository.getScheduleReminder(reminderId)?.snoozeMinute?.takeIf { it > 0 }
        ReminderScheduler.OWNER_HABIT ->
            reminderRepository.getHabitReminder(reminderId)?.snoozeMinute?.takeIf { it > 0 }
        else -> null
    }

    private fun ensureChannel(notificationManager: NotificationManager, context: Context) {
        // Delete and recreate to force alarm sound config (Android caches channels
        // and ignores updates, so old installs would keep the silent channel)
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            setSound(
                soundUri,
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(
        context: Context,
        ownerType: String,
        ownerId: String,
        reminderId: String,
        title: String
    ): Notification {
        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title.ifBlank { context.getString(R.string.reminder_notification_title) })
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(openIntent)
            .addAction(
                0,
                context.getString(R.string.action_start),
                actionPendingIntent(context, ACTION_START, ownerType, ownerId, reminderId)
            )
            .addAction(
                0,
                context.getString(R.string.action_snooze),
                actionPendingIntent(context, ACTION_SNOOZE, ownerType, ownerId, reminderId)
            )
            .addAction(
                0,
                context.getString(R.string.action_skip),
                actionPendingIntent(context, ACTION_DISMISS, ownerType, ownerId, reminderId)
            )

        // Full Screen Loud Alarm Intent for HABIT reminders only
        if (ownerType == ReminderScheduler.OWNER_HABIT) {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra(ReminderScheduler.EXTRA_OWNER_TYPE, ownerType)
                putExtra(ReminderScheduler.EXTRA_OWNER_ID, ownerId)
                putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
                putExtra("title", title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                ReminderScheduler.requestCode(reminderId),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        return builder.build()
    }

    private fun actionPendingIntent(
        context: Context,
        action: String,
        ownerType: String,
        ownerId: String,
        reminderId: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderScheduler.EXTRA_OWNER_TYPE, ownerType)
            putExtra(ReminderScheduler.EXTRA_OWNER_ID, ownerId)
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            // Different request codes per action so the three PendingIntents coexist.
            (action + reminderId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "mother_reminder"
        const val ACTION_START = "com.mother.app.action.REMINDER_START"
        const val ACTION_SNOOZE = "com.mother.app.action.REMINDER_SNOOZE"
        const val ACTION_DISMISS = "com.mother.app.action.REMINDER_DISMISS"
        private const val DEFAULT_SNOOZE_MINUTE = 5
    }
}

/** Re-arms every future reminder after a device reboot (AGENT_RULES §12). */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val container = (context.applicationContext as MotherApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                container.reminderRepository.getAllTaskReminders()
                    .filter { it.enabled && it.triggerTime > now }
                    .forEach {
                        ReminderScheduler.schedule(
                            context,
                            ReminderScheduler.OWNER_TASK,
                            it.taskId,
                            it.id,
                            it.triggerTime
                        )
                    }
                container.reminderRepository.getAllScheduleReminders()
                    .filter { it.enabled && it.triggerTime > now }
                    .forEach {
                        ReminderScheduler.schedule(
                            context,
                            ReminderScheduler.OWNER_SCHEDULE,
                            it.scheduleId,
                            it.id,
                            it.triggerTime
                        )
                    }
                container.reminderRepository.getAllHabitReminders()
                    .filter { it.enabled && it.triggerTime > now }
                    .forEach {
                        ReminderScheduler.schedule(
                            context,
                            ReminderScheduler.OWNER_HABIT,
                            it.habitId,
                            it.id,
                            it.triggerTime
                        )
                    }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Generates a reminder UUID (kept here so callers stay terse). */
fun newReminderId(): String = UUID.randomUUID().toString()
