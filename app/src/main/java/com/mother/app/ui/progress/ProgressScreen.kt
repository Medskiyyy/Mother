package com.mother.app.ui.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
 * Progress tab (UI_SPEC: Progress). Hosts habits, study sessions, statistics,
 * heatmap, and achievements in scrollable tabs.
 */
@Composable
fun ProgressScreen(
    habitListViewModel: HabitListViewModel,
    studySessionListViewModel: StudySessionListViewModel,
    statisticsViewModel: StatisticsViewModel,
    heatmapViewModel: HeatmapViewModel,
    achievementViewModel: AchievementViewModel,
    onEditSession: (String) -> Unit,
    onStartTimer: ((com.mother.app.data.local.entity.HabitEntity) -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        R.string.progress_tab_habits,
        R.string.progress_tab_study,
        R.string.progress_tab_statistics,
        R.string.progress_tab_heatmap,
        R.string.progress_tab_achievement
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, labelRes ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(stringResource(labelRes)) }
                )
            }
        }
        when (selectedTab) {
            0 -> HabitListScreen(viewModel = habitListViewModel, onStartTimer = onStartTimer)
            1 -> StudySessionListScreen(
                viewModel = studySessionListViewModel,
                onEditSession = onEditSession
            )
            2 -> StatisticsScreen(viewModel = statisticsViewModel)
            3 -> HeatmapScreen(viewModel = heatmapViewModel)
            else -> AchievementScreen(viewModel = achievementViewModel)
        }
    }
}
