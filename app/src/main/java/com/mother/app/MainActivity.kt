package com.mother.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mother.app.ui.navigation.TopLevelDestination
import com.mother.app.ui.screens.PlaceholderScreen
import com.mother.app.ui.theme.MotherTheme

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.factory((application as MotherApplication).container)
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

    @Composable
    private fun MotherContent() {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

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
                    PlaceholderScreen(stringResource(TopLevelDestination.Calendar.labelRes))
                }
                composable(TopLevelDestination.Tasks.route) {
                    PlaceholderScreen(stringResource(TopLevelDestination.Tasks.labelRes))
                }
                composable(TopLevelDestination.Progress.route) {
                    PlaceholderScreen(stringResource(TopLevelDestination.Progress.labelRes))
                }
                composable(TopLevelDestination.Settings.route) {
                    PlaceholderScreen(stringResource(TopLevelDestination.Settings.labelRes))
                }
            }
        }
    }
}