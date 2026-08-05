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
import com.mother.app.R
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service keeping the active timer alive and visible while the app
 * is closed or the screen is off (PRD §16). Duration itself is derived from
 * epoch millis in [ActiveTimerStore]; the service only keeps the process and
 * the notification current.
 */
class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_TIMER) {
            // FocusModeViewModel.stop() is the full flow (saves the session);
            // from the notification we only release the timer and the service.
            ActiveTimerStore.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            // Reflect store changes (start/pause/resume/stop) in the notification.
            ActiveTimerStore.activeTimer.collect { timer ->
                if (timer == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    notificationManager().notify(NOTIFICATION_ID, buildNotification())
                }
            }
        }
        scope.launch {
            // Tick the elapsed-time text once per 30s while a timer exists.
            while (true) {
                delay(30_000L)
                if (ActiveTimerStore.activeTimer.value != null) {
                    notificationManager().notify(NOTIFICATION_ID, buildNotification())
                }
            }
        }
        return START_STICKY
    }

    private fun notificationManager(): NotificationManager =
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureChannel() {
        val manager = notificationManager()
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.timer_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TimerService::class.java).setAction(ACTION_STOP_TIMER),
            PendingIntent.FLAG_IMMUTABLE
        )
        val timer = ActiveTimerStore.activeTimer.value
        val text = if (timer == null) {
            getString(R.string.timer_notification_idle)
        } else {
            val minutes = ActiveTimerStore.elapsedMinute().toInt()
            "${timer.habitTitle} - ${TimeUtils.formatDurationCompact(minutes)}"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.action_stop), stopIntent)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "mother_timer"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_TIMER = "com.mother.app.action.STOP_TIMER"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
