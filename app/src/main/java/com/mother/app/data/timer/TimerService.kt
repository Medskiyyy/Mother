package com.mother.app.data.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mother.app.MainActivity
import com.mother.app.MotherApplication
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.model.SessionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Foreground service keeping the active focus timer alive and visible with real-time controls.
 * Shows a native Stopwatch notification with Pause/Resume and Stop actions visible on Lock Screen.
 */
class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                ActiveTimerStore.pause()
                updateNotification()
                return START_STICKY
            }
            ACTION_RESUME -> {
                ActiveTimerStore.resume()
                updateNotification()
                return START_STICKY
            }
            ACTION_STOP_TIMER -> {
                handleStopTimer()
                return START_NOT_STICKY
            }
        }

        val timer = ActiveTimerStore.activeTimer.value
        if (timer == null || timer.phase == TimerPhase.IDLE) {
            stopSelf()
            return START_NOT_STICKY
        }

        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startTickingLoop()

        return START_STICKY
    }

    private fun handleStopTimer() {
        val stopped = ActiveTimerStore.stop()
        tickJob?.cancel()

        if (stopped != null) {
            val minutes = stopped.first
            val snapshot = stopped.second
            if (minutes > 0 && snapshot != null) {
                scope.launch {
                    withContext(NonCancellable + Dispatchers.IO) {
                        try {
                            val repo = (application as MotherApplication).container.studySessionRepository
                            val now = System.currentTimeMillis()
                            repo.upsert(
                                StudySessionEntity(
                                    id = UUID.randomUUID().toString(),
                                    habitId = snapshot.habitId,
                                    startTime = now - (minutes * 60_000L),
                                    endTime = now,
                                    durationMinute = minutes.toInt(),
                                    source = SessionSource.TIMER,
                                    note = null,
                                    createdAt = now
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                return
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTickingLoop() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                updateNotification()
                delay(1000L)
            }
        }
    }

    private fun updateNotification() {
        val timer = ActiveTimerStore.activeTimer.value
        if (timer == null || timer.phase == TimerPhase.IDLE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            notificationManager().notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun notificationManager(): NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureChannel() {
        val manager = notificationManager()
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Timer Realtime",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TimerService::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val resumeIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, TimerService::class.java).setAction(ACTION_RESUME),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, TimerService::class.java).setAction(ACTION_STOP_TIMER),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val timer = ActiveTimerStore.activeTimer.value
        val elapsedMillis = ActiveTimerStore.elapsedMillis()
        val elapsedSec = elapsedMillis / 1000L
        val hours = elapsedSec / 3600L
        val minutes = (elapsedSec % 3600L) / 60L
        val seconds = elapsedSec % 60L
        val timeText = if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }

        val title = timer?.habitTitle ?: "Mode Fokus"
        val isPaused = timer?.phase == TimerPhase.PAUSED

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(if (isPaused) "Dijeda • $timeText" else "Mode Fokus • $timeText")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (!isPaused) {
            builder.setUsesChronometer(true)
            builder.setWhen(System.currentTimeMillis() - elapsedMillis)
            builder.setShowWhen(true)
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
        }

        if (isPaused) {
            builder.addAction(
                android.R.drawable.ic_media_play,
                "Lanjutkan",
                resumeIntent
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Jeda",
                pauseIntent
            )
        }

        builder.addAction(
            android.R.drawable.ic_delete,
            "Hentikan",
            stopIntent
        )

        return builder.build()
    }

    override fun onDestroy() {
        tickJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "mother_timer_v3"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_PAUSE = "com.mother.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "com.mother.app.action.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.mother.app.action.STOP_TIMER"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }
}
