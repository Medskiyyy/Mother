package com.mother.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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

data class StudySessionItem(
    val session: StudySessionEntity,
    val habitTitle: String
)

data class StudySessionListUiState(
    val sessions: List<StudySessionItem> = emptyList(),
    val totalMinutes: Int = 0
)

class StudySessionListViewModel(
    private val studySessionRepository: StudySessionRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudySessionListUiState())
    val uiState: StateFlow<StudySessionListUiState> = _uiState.asStateFlow()

    private var sessions: List<StudySessionEntity> = emptyList()
    private var habitTitles: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { list ->
                sessions = list
                rebuild()
            }
        }
        viewModelScope.launch {
            habitRepository.observeActive().collect { habits ->
                habitTitles = habits.associate { it.id to it.title }
                rebuild()
            }
        }
    }

    private fun rebuild() {
        val items = sessions
            .sortedByDescending { it.startTime }
            .map { session ->
                StudySessionItem(
                    session = session,
                    habitTitle = habitTitles[session.habitId]
                        ?: stringResourceFallback()
                )
            }
        _uiState.update {
            it.copy(
                sessions = items,
                totalMinutes = sessions.sumOf { session -> session.durationMinute }
            )
        }
    }

    /** Placeholder for habits that are archived/deleted; resolved lazily in UI. */
    private fun stringResourceFallback(): String = "-"

    fun deleteSession(sessionId: String) {
        viewModelScope.launch { studySessionRepository.deleteById(sessionId) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StudySessionListViewModel(container.studySessionRepository, container.habitRepository)
            }
        }
    }
}

@Composable
fun StudySessionListScreen(
    viewModel: StudySessionListViewModel,
    onEditSession: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.sessions.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.School,
            title = stringResource(R.string.sessions_empty_title),
            description = stringResource(R.string.sessions_empty_description)
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.session_total, TimeUtils.formatDurationCompact(state.totalMinutes)),
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(state.sessions, key = { it.session.id }) { item ->
            SessionRow(
                item = item,
                onClick = { onEditSession(item.session.id) },
                onDelete = { viewModel.deleteSession(item.session.id) }
            )
        }
    }
}

@Composable
private fun SessionRow(item: StudySessionItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val session = item.session
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.habitTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = TimeUtils.formatFullDate(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${TimeUtils.formatTime(session.startTime)} - ${TimeUtils.formatTime(session.endTime)}" +
                        " (${TimeUtils.formatDurationCompact(session.durationMinute)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete)
                )
            }
        }
    }
}
