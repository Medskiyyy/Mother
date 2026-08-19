package com.mother.app.data.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.mother.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground Service that handles habit alarm ringing.
 * This is the only reliable way to show a full-screen alarm on Android 10+.
 *
 * Flow: AlarmManager -> ReminderReceiver -> HabitAlarmService -> fullScreenIntent -> AlarmActivity
 */
class HabitAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timeoutJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                timeoutJob?.cancel()
                timeoutJob = null
                stopAlarm()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val ownerType = intent?.getStringExtra(ReminderScheduler.EXTRA_OWNER_TYPE) ?: ""
        val ownerId = intent?.getStringExtra(ReminderScheduler.EXTRA_OWNER_ID) ?: ""
        val reminderId = intent?.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: ""
        val title = intent?.getStringExtra("title") ?: getString(R.string.reminder_notification_title)

        // Cancel previous timeout if any
        timeoutJob?.cancel()

        // Acquire WakeLock to keep device awake
        wakeLock?.let { if (it.isHeld) it.release() }
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Mother:HabitAlarmWakeLock"
        ).also { it.acquire(5 * 60_000L) }

        // Create notification channel for alarm
        ensureAlarmChannel()

        // Build full-screen notification
        val notification = buildAlarmNotification(ownerType, ownerId, reminderId, title)
        startForeground(ALARM_NOTIFICATION_ID, notification)

        // Start loud alarm sound + vibration
        startAlarmSound()
        startVibration()

        // Auto-timeout after 90 seconds -> Missed Alarm notification
        timeoutJob = serviceScope.launch {
            delay(ALARM_TIMEOUT_MS)
            stopAlarm()
            stopForeground(STOP_FOREGROUND_REMOVE)
            ReminderReceiver.showMissedHabitNotification(
                applicationContext,
                ownerId,
                reminderId,
                title
            )
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun ensureAlarmChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Always recreate to ensure alarm config is applied
        nm.deleteNotificationChannel(ALARM_CHANNEL_ID)
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Alarm Kebiasaan",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            // Sound and vibration are managed exclusively by HabitAlarmService's MediaPlayer & Vibrator
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildAlarmNotification(
        ownerType: String, ownerId: String, reminderId: String, title: String
    ): Notification {
        // Full-screen intent -> AlarmActivity
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_OWNER_TYPE, ownerType)
            putExtra(ReminderScheduler.EXTRA_OWNER_ID, ownerId)
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra("title", title)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPI = PendingIntent.getActivity(
            this,
            ALARM_NOTIFICATION_ID,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(this, HabitAlarmService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPI = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Waktunya membangun kebiasaan!")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(null)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPI, true)
            .setContentIntent(fullScreenPI)
            .addAction(0, "Lewati", stopPI)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startAlarmSound() {
        if (mediaPlayer != null) return // Already playing, avoid duplicate instance!
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                android.os.VibrationEffect.createWaveform(pattern, 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        runCatching {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null

        runCatching {
            vibrator?.cancel()
        }
        vibrator = null

        runCatching {
            wakeLock?.let { if (it.isHeld) it.release() }
        }
        wakeLock = null
    }

    override fun onDestroy() {
        timeoutJob?.cancel()
        timeoutJob = null
        stopAlarm()
        super.onDestroy()
    }

    companion object {
        const val ALARM_CHANNEL_ID = "mother_habit_alarm_v3_silent"
        const val ALARM_NOTIFICATION_ID = 99999
        const val ACTION_STOP = "com.mother.app.action.STOP_HABIT_ALARM"
        const val ALARM_TIMEOUT_MS = 90_000L // 90 seconds timeout

        fun stop(context: Context) {
            val intent = Intent(context, HabitAlarmService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
