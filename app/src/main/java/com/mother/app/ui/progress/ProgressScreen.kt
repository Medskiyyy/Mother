package com.mother.app.ui.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mother.app.R
import com.mother.app.ui.screens.HabitListScreen
import com.mother.app.ui.screens.HabitListViewModel
import com.mother.app.ui.screens.StudySessionListScreen
import com.mother.app.ui.screens.StudySessionListViewModel

/**
 * Progress tab (UI_SPEC: Progress). Hosts the habit list and the study-session
 * history; statistics, heatmap, and achievements arrive in later phases.
 */
@Composable
fun ProgressScreen(
    habitListViewModel: HabitListViewModel,
    studySessionListViewModel: StudySessionListViewModel,
    onEditSession: (String) -> Unit,
    onStartTimer: ((com.mother.app.data.local.entity.HabitEntity) -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.progress_tab_habits)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.progress_tab_sessions)) }
            )
        }
        when (selectedTab) {
            0 -> HabitListScreen(viewModel = habitListViewModel, onStartTimer = onStartTimer)
            else -> StudySessionListScreen(
                viewModel = studySessionListViewModel,
                onEditSession = onEditSession
            )
        }
    }
}
