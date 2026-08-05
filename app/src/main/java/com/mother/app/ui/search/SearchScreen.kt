package com.mother.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.HabitRepository
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

data class SearchUiState(
    val query: String = "",
    val tasks: List<TaskEntity> = emptyList(),
    val schedules: List<ScheduleEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val sessions: List<StudySessionEntity> = emptyList()
)

/** Global search across tasks, schedules, habits, and study sessions (PRD §28). */
class SearchViewModel(
    taskRepository: TaskRepository,
    scheduleRepository: ScheduleRepository,
    habitRepository: HabitRepository,
    studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var tasks: List<TaskEntity> = emptyList()
    private var schedules: List<ScheduleEntity> = emptyList()
    private var habits: List<HabitEntity> = emptyList()
    private var sessions: List<StudySessionEntity> = emptyList()
    private var habitTitles: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch {
            taskRepository.observeAll().collect { tasks = it; recompute() }
        }
        viewModelScope.launch {
            scheduleRepository.observeAll().collect { schedules = it; recompute() }
        }
        viewModelScope.launch {
            habitRepository.observeActive().collect { list ->
                habits = list
                habitTitles = list.associate { it.id to it.title }
                recompute()
            }
        }
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { sessions = it; recompute() }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        recompute()
    }

    private fun recompute() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.update { it.copy(tasks = emptyList(), schedules = emptyList(), habits = emptyList(), sessions = emptyList()) }
            return
        }
        _uiState.update {
            it.copy(
                tasks = tasks.filter { item -> item.title.contains(query, true) || item.description?.contains(query, true) == true },
                schedules = schedules.filter { item -> item.title.contains(query, true) || item.description?.contains(query, true) == true },
                habits = habits.filter { item -> item.title.contains(query, true) },
                sessions = sessions.filter { item ->
                    val note = item.note
                    (note != null && note.contains(query, true)) ||
                        habitTitles[item.habitId]?.contains(query, true) == true
                }
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SearchViewModel(
                    container.taskRepository,
                    container.scheduleRepository,
                    container.habitRepository,
                    container.studySessionRepository
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hasResults = state.tasks.isNotEmpty() || state.schedules.isNotEmpty() ||
        state.habits.isNotEmpty() || state.sessions.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.form_cancel))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            when {
                state.query.isBlank() -> Unit
                !hasResults -> Text(
                    text = stringResource(R.string.search_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (state.tasks.isNotEmpty()) {
                        item { GroupHeader(stringResource(R.string.search_group_tasks)) }
                        items(state.tasks, key = { "t_" + it.id }) { task ->
                            ResultRow(title = task.title, subtitle = task.deadline?.let { TimeUtils.formatFullDate(it) })
                        }
                    }
                    if (state.schedules.isNotEmpty()) {
                        item { GroupHeader(stringResource(R.string.search_group_schedules)) }
                        items(state.schedules, key = { "s_" + it.id }) { schedule ->
                            ResultRow(title = schedule.title, subtitle = TimeUtils.formatFullDate(schedule.startTime))
                        }
                    }
                    if (state.habits.isNotEmpty()) {
                        item { GroupHeader(stringResource(R.string.search_group_habits)) }
                        items(state.habits, key = { "h_" + it.id }) { habit ->
                            ResultRow(title = habit.title, subtitle = null)
                        }
                    }
                    if (state.sessions.isNotEmpty()) {
                        item { GroupHeader(stringResource(R.string.search_group_sessions)) }
                        items(state.sessions, key = { "ss_" + it.id }) { session ->
                            ResultRow(
                                title = TimeUtils.formatFullDate(session.startTime),
                                subtitle = TimeUtils.formatDurationCompact(session.durationMinute)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ResultRow(title: String, subtitle: String?) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
