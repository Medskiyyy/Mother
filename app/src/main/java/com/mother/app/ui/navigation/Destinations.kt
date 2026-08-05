package com.mother.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.mother.app.R

/** Bottom navigation destinations (UI_SPEC: Navigation). */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    Dashboard("dashboard", R.string.nav_dashboard, Icons.Filled.Home),
    Calendar("calendar", R.string.nav_calendar, Icons.Filled.CalendarMonth),
    Tasks("tasks", R.string.nav_tasks, Icons.Filled.TaskAlt),
    Progress("progress", R.string.nav_progress, Icons.Filled.Insights),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings)
}

/** Secondary routes reachable from the FAB and list screens. */
object Routes {
    const val CREATE_TASK = "task/create"
    const val CREATE_SCHEDULE = "schedule/create"
    const val CREATE_HABIT = "habit/create"
    const val SESSIONS = "sessions"
    const val CREATE_SESSION = "session/create"
    const val EDIT_SESSION = "session/edit/{sessionId}"
    const val FOCUS = "focus"
    const val POMODORO = "pomodoro"

    fun editSession(sessionId: String): String = "session/edit/$sessionId"
}
