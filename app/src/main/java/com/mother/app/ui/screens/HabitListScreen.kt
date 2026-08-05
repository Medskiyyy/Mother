package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.di.AppContainer
import com.mother.app.ui.components.EmptyState
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitProgress(
    val habit: HabitEntity,
    val todayMinutes: Int
)

class HabitListViewModel(
    private val habitRepository: HabitRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<HabitProgress>>(emptyList())
    val items: StateFlow<List<HabitProgress>> = _items.asStateFlow()

    private var habits: List<HabitEntity> = emptyList()
    private var sessions: List<StudySessionEntity> = emptyList()

    init {
        viewModelScope.launch {
            habitRepository.observeActive().collect { list ->
                habits = list
                rebuild()
            }
        }
        val dayStart = TimeUtils.todayStart()
        val dayEnd = TimeUtils.todayEnd()
        viewModelScope.launch {
            studySessionRepository.observeRange(dayStart, dayEnd).collect { list ->
                sessions = list
                rebuild()
            }
        }
    }

    private fun rebuild() {
        val minutesByHabit = sessions.groupBy { it.habitId }
            .mapValues { (_, list) -> list.sumOf { it.durationMinute } }
        _items.update {
            habits.map { habit ->
                HabitProgress(habit = habit, todayMinutes = minutesByHabit[habit.id] ?: 0)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HabitListViewModel(container.habitRepository, container.studySessionRepository) }
        }
    }
}

@Composable
fun HabitListScreen(viewModel: HabitListViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    if (items.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Repeat,
            title = stringResource(R.string.habits_empty_title),
            description = stringResource(R.string.habits_empty_description)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.habit.id }) { item ->
            HabitRow(item = item)
        }
    }
}

@Composable
private fun HabitRow(item: HabitProgress) {
    val target = item.habit.targetMinute.coerceAtLeast(1)
    val progress = (item.todayMinutes.toFloat() / target).coerceIn(0f, 1f)
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.habit.title, style = MaterialTheme.typography.titleMedium)
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
        }
    }
}
