package com.mother.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.model.Theme
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.data.timer.TimerService
import com.mother.app.ui.dashboard.DashboardScreen
import com.mother.app.ui.dashboard.DashboardViewModel
import com.mother.app.ui.focus.FocusModeScreen
import com.mother.app.ui.focus.FocusModeViewModel
import com.mother.app.ui.navigation.Routes
import com.mother.app.ui.navigation.TopLevelDestination
import com.mother.app.ui.screens.CreateHabitScreen
import com.mother.app.ui.screens.CreateScheduleScreen
import com.mother.app.ui.screens.CreateStudySessionScreen
import com.mother.app.ui.screens.CreateTaskScreen
import com.mother.app.ui.calendar.CalendarScreen
import com.mother.app.ui.calendar.CalendarViewModel
import com.mother.app.ui.pomodoro.PomodoroScreen
import com.mother.app.ui.pomodoro.PomodoroViewModel
import com.mother.app.ui.progress.AchievementViewModel
import com.mother.app.ui.progress.HeatmapViewModel
import com.mother.app.ui.progress.ProgressScreen
import com.mother.app.ui.progress.StatisticsViewModel
import com.mother.app.data.backup.BackupManager
import com.mother.app.ui.onboarding.OnboardingScreen
import com.mother.app.ui.screens.HabitListViewModel
import com.mother.app.ui.screens.StudySessionListViewModel
import com.mother.app.ui.search.SearchScreen
import com.mother.app.ui.search.SearchViewModel
import com.mother.app.ui.settings.SettingsScreen
import com.mother.app.ui.settings.SettingsViewModel
import com.mother.app.ui.tasks.TasksScreen
import com.mother.app.ui.tasks.TasksViewModel
import com.mother.app.ui.theme.MotherTheme

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.factory((application as MotherApplication).container)
    }

    private val tasksViewModel: TasksViewModel by viewModels {
        TasksViewModel.factory((application as MotherApplication).container)
    }

    private val habitListViewModel: HabitListViewModel by viewModels {
        HabitListViewModel.factory((application as MotherApplication).container)
    }

    private val studySessionListViewModel: StudySessionListViewModel by viewModels {
        StudySessionListViewModel.factory((application as MotherApplication).container)
    }

    private val focusModeViewModel: FocusModeViewModel by viewModels {
        FocusModeViewModel.factory((application as MotherApplication).container)
    }

    private val pomodoroViewModel: PomodoroViewModel by viewModels {
        PomodoroViewModel.factory((application as MotherApplication).container)
    }

    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModel.factory((application as MotherApplication).container)
    }

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        StatisticsViewModel.factory((application as MotherApplication).container)
    }

    private val heatmapViewModel: HeatmapViewModel by viewModels {
        HeatmapViewModel.factory((application as MotherApplication).container)
    }

    private val achievementViewModel: AchievementViewModel by viewModels {
        AchievementViewModel.factory((application as MotherApplication).container)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val container = (application as MotherApplication).container
        SettingsViewModel.factory(container, BackupManager(applicationContext, container))
    }

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModel.factory((application as MotherApplication).container)
    }

    /** Requests POST_NOTIFICATIONS once, on first run (PRD §27). */
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            showNotificationDeniedInfo = !granted
        }

    private var showNotificationDeniedInfo by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            MotherApp()
            NotificationDeniedDialog()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Reminders cannot ring without the notification permission (UI_SPEC §27). */
    @Composable
    private fun NotificationDeniedDialog() {
        if (showNotificationDeniedInfo) {
            AlertDialog(
                onDismissRequest = { showNotificationDeniedInfo = false },
                title = { Text(stringResource(R.string.reminder_channel_name)) },
                text = { Text(stringResource(R.string.permission_notification_denied)) },
                confirmButton = {
                    TextButton(onClick = { showNotificationDeniedInfo = false }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }

    @Composable
    private fun MotherApp() {
        // Theme follows the user's AppSetting (PRD §29); SYSTEM tracks the OS.
        val container = (application as MotherApplication).container
        val setting by container.settingRepository.observeSetting().collectAsStateWithLifecycle(null)
        val systemDark = isSystemInDarkTheme()
        val darkTheme = when (setting?.theme) {
            Theme.LIGHT -> false
            Theme.DARK -> true
            else -> systemDark
        }
        // Show onboarding once, before the main UI (UI_SPEC §Onboarding).
        val onboardingDone = setting?.onboardingFinished ?: false
        var finishedThisRun by rememberSaveable { mutableStateOf(false) }

        MotherTheme(darkTheme = darkTheme) {
            if (onboardingDone || finishedThisRun) {
                MotherContent()
            } else {
                OnboardingScreen(
                    onFinished = {
                        finishedThisRun = true
                        lifecycleScope.launch {
                            container.settingRepository.setOnboardingFinished(true)
                        }
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MotherContent() {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }
        // Focus Mode hides the bottom navigation entirely (PRD §18).
        val isFocusMode = currentRoute == Routes.FOCUS
        var showQuickAdd by rememberSaveable { mutableStateOf(false) }
        var showTimerBusyDialog by rememberSaveable { mutableStateOf(false) }

        /**
         * Starts the single timer for [habit] and enters Focus Mode (PRD §16/§18).
         * Refuses when another timer is still running (PRD §12).
         */
        val startTimer: (HabitEntity) -> Unit = { habit ->
            if (ActiveTimerStore.activeTimer.value != null) {
                showTimerBusyDialog = true
            } else {
                ActiveTimerStore.start(habit.id, habit.title, habit.targetMinute)
                TimerService.start(this@MainActivity)
                navController.navigate(Routes.FOCUS)
            }
        }

        /** Opens Focus Mode when a timer runs; otherwise points to the habit list. */
        val openTimerOrStart: () -> Unit = {
            if (ActiveTimerStore.activeTimer.value != null) {
                navController.navigate(Routes.FOCUS)
            } else {
                navController.navigate(TopLevelDestination.Progress.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (!isFocusMode) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
                }
            },
            // Global FAB (UI_SPEC): shown on top-level screens, opens the quick-add sheet.
            floatingActionButton = {
                if (isTopLevel && !isFocusMode) {
                    FloatingActionButton(onClick = { showQuickAdd = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.quick_add))
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TopLevelDestination.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(TopLevelDestination.Dashboard.route) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onStartTimer = openTimerOrStart,
                        onSearch = { navController.navigate(Routes.SEARCH) }
                    )
                }
                composable(TopLevelDestination.Calendar.route) {
                    CalendarScreen(viewModel = calendarViewModel)
                }
                composable(TopLevelDestination.Tasks.route) {
                    TasksScreen(viewModel = tasksViewModel)
                }
                composable(TopLevelDestination.Progress.route) {
                    ProgressScreen(
                        habitListViewModel = habitListViewModel,
                        studySessionListViewModel = studySessionListViewModel,
                        statisticsViewModel = statisticsViewModel,
                        heatmapViewModel = heatmapViewModel,
                        achievementViewModel = achievementViewModel,
                        onEditSession = { sessionId ->
                            navController.navigate(Routes.editSession(sessionId))
                        },
                        onStartTimer = startTimer
                    )
                }
                composable(TopLevelDestination.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
                composable(Routes.CREATE_TASK) {
                    CreateTaskScreen(
                        container = (application as MotherApplication).container,
                        onSaved = { navController.popBackStack() },
                        onCancelled = { navController.popBackStack() }
                    )
                }
                composable(Routes.CREATE_SCHEDULE) {
                    CreateScheduleScreen(
                        container = (application as MotherApplication).container,
                        onSaved = { navController.popBackStack() },
                        onCancelled = { navController.popBackStack() }
                    )
                }
                composable(Routes.CREATE_HABIT) {
                    CreateHabitScreen(
                        container = (application as MotherApplication).container,
                        onSaved = { navController.popBackStack() },
                        onCancelled = { navController.popBackStack() }
                    )
                }
                composable(Routes.CREATE_SESSION) {
                    CreateStudySessionScreen(
                        container = (application as MotherApplication).container,
                        onSaved = { navController.popBackStack() },
                        onCancelled = { navController.popBackStack() }
                    )
                }
                composable(Routes.EDIT_SESSION) { backStackEntry ->
                    CreateStudySessionScreen(
                        container = (application as MotherApplication).container,
                        sessionId = backStackEntry.arguments?.getString("sessionId"),
                        onSaved = { navController.popBackStack() },
                        onCancelled = { navController.popBackStack() }
                    )
                }
                composable(Routes.POMODORO) {
                    PomodoroScreen(
                        viewModel = pomodoroViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.FOCUS) {
                    FocusModeScreen(
                        viewModel = focusModeViewModel,
                        onExit = { navController.popBackStack() },
                        onGoToHabits = {
                            navController.popBackStack()
                            navController.navigate(TopLevelDestination.Progress.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }

        if (showQuickAdd) {
            QuickAddSheet(
                onDismiss = { showQuickAdd = false },
                onPick = { route ->
                    showQuickAdd = false
                    navController.navigate(route)
                }
            )
        }

        if (showTimerBusyDialog) {
            AlertDialog(
                onDismissRequest = { showTimerBusyDialog = false },
                title = { Text(stringResource(R.string.timer_busy_title)) },
                text = { Text(stringResource(R.string.timer_busy_message)) },
                confirmButton = {
                    TextButton(onClick = { showTimerBusyDialog = false }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun QuickAddSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            QuickAddRow(Icons.Filled.Event, R.string.quick_add_schedule) { onPick(Routes.CREATE_SCHEDULE) }
            QuickAddRow(Icons.Filled.AddTask, R.string.quick_add_task) { onPick(Routes.CREATE_TASK) }
            QuickAddRow(Icons.Filled.Repeat, R.string.quick_add_habit) { onPick(Routes.CREATE_HABIT) }
            QuickAddRow(Icons.Filled.School, R.string.quick_add_session) { onPick(Routes.CREATE_SESSION) }
            QuickAddRow(Icons.Filled.Timer, R.string.quick_add_pomodoro) { onPick(Routes.POMODORO) }
        }
    }

    @Composable
    private fun QuickAddRow(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        labelRes: Int,
        onClick: () -> Unit
    ) {
        ListItem(
            headlineContent = { Text(stringResource(labelRes)) },
            leadingContent = { Icon(icon, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
