package com.mother.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.mother.app.MotherApplication
import com.mother.app.R
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Medium home-screen widget (PRD §25): streak plus today's study progress.
 * Display-only in v1 (no interaction). Refreshed on the system update cycle.
 */
class MotherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        val container = (context.applicationContext as MotherApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val dayStart = TimeUtils.startOfDay(now)
                val dayEnd = TimeUtils.endOfDay(now)

                val todayMinutes = container.studySessionRepository
                    .observeTotalMinutesRange(dayStart, dayEnd)
                    .firstOrNull() ?: 0
                val targetMinutes = container.settingRepository.getSetting()?.defaultStudyTargetMinute ?: 120
                val sessions = container.studySessionRepository.observeAllAsc().firstOrNull().orEmpty()
                val streak = TimeUtils.computeStreak(sessions.map { it.startTime })

                val views = RemoteViews(context.packageName, R.layout.widget_mother)
                views.setTextViewText(
                    R.id.widget_streak,
                    context.getString(R.string.dashboard_streak_days, streak)
                )
                views.setTextViewText(
                    R.id.widget_study,
                    "${TimeUtils.formatDurationCompact(todayMinutes)} / ${TimeUtils.formatDurationCompact(targetMinutes)}"
                )
                val progress = if (targetMinutes > 0) (todayMinutes * 100 / targetMinutes).coerceIn(0, 100) else 0
                views.setInt(R.id.widget_progress, "setProgress", progress)

                appWidgetIds.forEach { id ->
                    appWidgetManager.updateAppWidget(id, views)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
