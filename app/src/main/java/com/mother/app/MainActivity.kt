package com.mother.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mother.app.data.model.Theme
import com.mother.app.ui.dashboard.DashboardScreen
import com.mother.app.ui.dashboard.DashboardViewModel
import com.mother.app.ui.navigation.Routes
import com.mother.app.ui.navigation.TopLevelDestination
import com.mother.app.ui.screens.CreateHabitScreen
import com.mother.app.ui.screens.CreateScheduleScreen
import com.mother.app.ui.screens.CreateTaskScreen
import com.mother.app.ui.screens.HabitListScreen
import com.mother.app.ui.screens.HabitListViewModel
import com.mother.app.ui.screens.PlaceholderScreen
import com.mother.app.ui.screens.ScheduleListScreen
import com.mother.app.ui.screens.ScheduleListViewModel
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

    private val scheduleListViewModel: ScheduleListViewModel by viewModels {
        ScheduleListViewModel.factory((application as MotherApplication).container)
    }

    private val habitListViewModel: HabitListViewModel by viewModels {
        HabitListViewModel.factory((application as MotherApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotherApp()
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

        MotherTheme(darkTheme = darkTheme) {
            MotherContent()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MotherContent() {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }
        var showQuickAdd by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
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
            },
            // Global FAB (UI_SPEC): shown on top-level screens, opens the quick-add sheet.
            floatingActionButton = {
                if (isTopLevel) {
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
                    DashboardScreen(viewModel = dashboardViewModel)
                }
                composable(TopLevelDestination.Calendar.route) {
                    ScheduleListScreen(viewModel = scheduleListViewModel)
                }
                composable(TopLevelDestination.Tasks.route) {
                    TasksScreen(viewModel = tasksViewModel)
                }
                composable(TopLevelDestination.Progress.route) {
                    HabitListScreen(viewModel = habitListViewModel)
                }
                composable(TopLevelDestination.Settings.route) {
                    PlaceholderScreen(stringResource(TopLevelDestination.Settings.labelRes))
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun QuickAddSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            QuickAddRow(Icons.Filled.Event, R.string.quick_add_schedule) { onPick(Routes.CREATE_SCHEDULE) }
            QuickAddRow(Icons.Filled.AddTask, R.string.quick_add_task) { onPick(Routes.CREATE_TASK) }
            QuickAddRow(Icons.Filled.Repeat, R.string.quick_add_habit) { onPick(Routes.CREATE_HABIT) }
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
