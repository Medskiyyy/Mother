package com.mother.app.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.R
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.ProgressStatistics
import com.mother.app.data.repository.ProgressStatisticsCalculator
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StatPeriod { DAY, WEEK, MONTH, YEAR }

data class StatisticsUiState(
    val period: StatPeriod = StatPeriod.WEEK,
    val stats: ProgressStatistics = ProgressStatistics()
)

/** Progress statistics computed on demand (PRD §23, AGENT_RULES §17). */
class StatisticsViewModel(
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository,
    private val studySessionRepository: StudySessionRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val calculator = ProgressStatisticsCalculator()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var tasks: List<TaskEntity> = emptyList()
    private var schedules: List<ScheduleEntity> = emptyList()
    private var sessions: List<StudySessionEntity> = emptyList()
    private var habits: List<HabitEntity> = emptyList()

    init {
        viewModelScope.launch {
            taskRepository.observeAll().collect { tasks = it; recompute() }
        }
        viewModelScope.launch {
            scheduleRepository.observeAll().collect { schedules = it; recompute() }
        }
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { sessions = it; recompute() }
        }
        viewModelScope.launch {
            habitRepository.observeActive().collect { habits = it; recompute() }
        }
    }

    fun onPeriodChange(period: StatPeriod) {
        _uiState.update { it.copy(period = period) }
        recompute()
    }

    private fun recompute() {
        val now = System.currentTimeMillis()
        val windowStart = when (_uiState.value.period) {
            StatPeriod.DAY -> TimeUtils.startOfDay(now)
            StatPeriod.WEEK -> TimeUtils.startOfWeek(now)
            StatPeriod.MONTH -> TimeUtils.startOfMonth(now)
            StatPeriod.YEAR -> TimeUtils.startOfYear(now)
        }
        val stats = calculator.compute(sessions, tasks, schedules, habits, windowStart, now)
        _uiState.update { it.copy(stats = stats) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StatisticsViewModel(
                    container.taskRepository,
                    container.scheduleRepository,
                    container.studySessionRepository,
                    container.habitRepository
                )
            }
        }
    }
}

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { PeriodDropdown(selected = state.period, onSelected = viewModel::onPeriodChange) }
        item {
            StatCard(stringResource(R.string.stat_study)) {
                StatRow(stringResource(R.string.stat_total_hours), TimeUtils.formatDurationCompact(state.stats.totalStudyMinutes.toInt()))
                StatRow(stringResource(R.string.stat_sessions), state.stats.totalSessions.toString())
                StatRow(stringResource(R.string.stat_avg), TimeUtils.formatDurationCompact(state.stats.averageSessionMinutes.toInt()))
            }
        }
        item {
            StatCard(stringResource(R.string.stat_habit)) {
                StatRow(stringResource(R.string.stat_habit_done), state.stats.habitsCompletedDays.toString())
                StatRow(stringResource(R.string.stat_habit_failed), state.stats.habitsMissedDays.toString())
                StatRow(stringResource(R.string.stat_streak_current), state.stats.currentStreak.toString())
                StatRow(stringResource(R.string.stat_streak_best), state.stats.bestStreak.toString())
            }
        }
        item {
            StatCard(stringResource(R.string.stat_task)) {
                StatRow(stringResource(R.string.stat_task_total), state.stats.totalTasks.toString())
                StatRow(stringResource(R.string.stat_task_completed), state.stats.completedTasks.toString())
                StatRow(stringResource(R.string.stat_task_overdue), state.stats.overdueTasks.toString())
                StatRow(stringResource(R.string.stat_task_active), state.stats.activeTasks.toString())
            }
        }
        item {
            StatCard(stringResource(R.string.stat_schedule)) {
                StatRow(stringResource(R.string.stat_schedule_completed), state.stats.completedSchedules.toString())
                StatRow(stringResource(R.string.stat_schedule_missed), state.stats.missedSchedules.toString())
                StatRow(stringResource(R.string.stat_schedule_cancelled), state.stats.cancelledSchedules.toString())
            }
        }
    }
}

@Composable
private fun StatCard(title: String, content: @Composable () -> Unit) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDropdown(selected: StatPeriod, onSelected: (StatPeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = periodLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_date)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StatPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(periodLabel(period)) },
                    onClick = {
                        onSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun periodLabel(period: StatPeriod): String = stringResource(
    when (period) {
        StatPeriod.DAY -> R.string.stat_period_day
        StatPeriod.WEEK -> R.string.stat_period_week
        StatPeriod.MONTH -> R.string.stat_period_month
        StatPeriod.YEAR -> R.string.stat_period_year
    }
)
