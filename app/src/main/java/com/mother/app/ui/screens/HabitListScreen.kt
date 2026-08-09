package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.R
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.RestoreHistoryEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.HabitStats
import com.mother.app.data.repository.RestoreStreakRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.ValidationException
import com.mother.app.di.AppContainer
import com.mother.app.ui.components.EmptyState
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mother.app.ui.components.NeoOutlinedButton

data class HabitProgress(
    val habit: HabitEntity,
    val todayMinutes: Int,
    val dayStatus: HabitStats.DayStatus,
    val currentStreak: Int,
    val bestStreak: Int,
    val canRestore: Boolean
)

data class HabitListUiState(
    val items: List<HabitProgress> = emptyList(),
    val remainingRestores: Int = 0,
    val errorMessage: String? = null
)

class HabitListViewModel(
    private val habitRepository: HabitRepository,
    private val studySessionRepository: StudySessionRepository,
    private val restoreStreakRepository: RestoreStreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    private var habits: List<HabitEntity> = emptyList()
    private var sessions: List<StudySessionEntity> = emptyList()
    private var restores: List<RestoreHistoryEntity> = emptyList()

    init {
        viewModelScope.launch {
            habitRepository.observeActive().collect { list ->
                habits = list
                rebuild()
            }
        }
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { list ->
                sessions = list
                rebuild()
            }
        }
        viewModelScope.launch {
            restoreStreakRepository.observeAll().collect { list ->
                restores = list
                rebuild()
            }
        }
    }

    private suspend fun rebuild() {
        val now = System.currentTimeMillis()
        val todayStart = TimeUtils.startOfDay(now)
        val todayEnd = TimeUtils.endOfDay(now)
        val todayMinutesByHabit = sessions.asSequence()
            .filter { it.startTime in todayStart until todayEnd }
            .groupBy { it.habitId }
            .mapValues { (_, list) -> list.sumOf { it.durationMinute } }

        val remaining = restoreStreakRepository.remainingRestores()
        _uiState.update {
            it.copy(
                remainingRestores = remaining,
                items = habits.map { habit ->
                    val todayMinutes = todayMinutesByHabit[habit.id] ?: 0
                    HabitProgress(
                        habit = habit,
                        todayMinutes = todayMinutes,
                        dayStatus = HabitStats.dayStatus(habit, todayMinutes),
                        currentStreak = HabitStats.currentStreak(habit.id, sessions, restores),
                        bestStreak = HabitStats.bestStreak(habit.id, sessions, restores),
                        canRestore = HabitStats.canRestore(habit.id, sessions, restores, remaining)
                    )
                }
            )
        }
    }

    /** Uses one monthly restore to reconnect the habit's broken streak (PRD §14). */
    fun restoreStreak(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                restoreStreakRepository.restore(reason = habit.id)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: ValidationException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HabitListViewModel(
                    container.habitRepository,
                    container.studySessionRepository,
                    container.restoreStreakRepository
                )
            }
        }
    }
}

@Composable
fun HabitListScreen(
    viewModel: HabitListViewModel,
    onStartTimer: ((HabitEntity) -> Unit)? = null,
    onEditHabit: ((String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        if (state.items.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Repeat,
                title = stringResource(R.string.habits_empty_title),
                description = stringResource(R.string.habits_empty_description),
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.habit.id }) { item ->
                    HabitRow(
                        item = item,
                        remainingRestores = state.remainingRestores,
                        onRestore = { viewModel.restoreStreak(item.habit) },
                        onStartTimer = onStartTimer?.let { start -> { start(item.habit) } },
                        onClick = { onEditHabit?.invoke(item.habit.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitRow(
    item: HabitProgress,
    remainingRestores: Int,
    onRestore: () -> Unit,
    onStartTimer: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val target = item.habit.targetMinute.coerceAtLeast(1)
    val progress = (item.todayMinutes.toFloat() / target).coerceIn(0f, 1f)
    val statusLabel = when (item.dayStatus) {
        HabitStats.DayStatus.NOT_STARTED -> stringResource(R.string.habit_status_not_started)
        HabitStats.DayStatus.IN_PROGRESS -> stringResource(R.string.habit_status_in_progress)
        HabitStats.DayStatus.COMPLETED -> stringResource(R.string.habit_status_completed)
    }
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.habit.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                text = "${TimeUtils.formatDurationCompact(item.todayMinutes)} / ${TimeUtils.formatDurationCompact(target)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFFFF9F43),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.currentStreak} hari",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = stringResource(R.string.habit_best_streak, item.bestStreak),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onStartTimer != null) {
                        NeoOutlinedButton(
                            text = stringResource(R.string.habit_start_timer),
                            onClick = onStartTimer,
                            fullWidth = false,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    if (item.canRestore) {
                        NeoOutlinedButton(
                            text = stringResource(R.string.habit_restore, remainingRestores),
                            onClick = onRestore,
                            enabled = remainingRestores > 0,
                            fullWidth = false,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
