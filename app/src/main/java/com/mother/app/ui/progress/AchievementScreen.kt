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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.mother.app.data.local.entity.AchievementEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.AchievementEvaluator
import com.mother.app.data.repository.AchievementRepository
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AchievementUiState(
    val achievements: List<AchievementEntity> = emptyList()
)

/**
 * Achievement list (PRD §20). Progress is recomputed from the source tables
 * every time; newly unlocked achievements are persisted so their date sticks.
 */
class AchievementViewModel(
    private val achievementRepository: AchievementRepository,
    private val studySessionRepository: StudySessionRepository,
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val evaluator = AchievementEvaluator()

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    private var sessions: List<StudySessionEntity> = emptyList()
    private var tasks: List<TaskEntity> = emptyList()
    private var schedules: List<ScheduleEntity> = emptyList()

    init {
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { sessions = it; reevaluate() }
        }
        viewModelScope.launch {
            taskRepository.observeAll().collect { tasks = it; reevaluate() }
        }
        viewModelScope.launch {
            scheduleRepository.observeAll().collect { schedules = it; reevaluate() }
        }
        viewModelScope.launch {
            achievementRepository.observeAll().collect { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
                reevaluate()
            }
        }
    }

    private fun reevaluate() {
        val achievements = _uiState.value.achievements
        if (achievements.isEmpty()) return
        val results = evaluator.evaluate(achievements, sessions, tasks, schedules)
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            achievements.forEach { achievement ->
                val result = results[achievement.id] ?: return@forEach
                val progressChanged = achievement.currentProgress != result.progress
                val unlockChanged = achievement.unlocked != result.unlocked
                if (progressChanged || unlockChanged) {
                    achievementRepository.updateProgress(
                        id = achievement.id,
                        progress = result.progress,
                        unlocked = result.unlocked,
                        unlockedAt = if (result.unlocked && !achievement.unlocked) now else achievement.unlockedAt
                    )
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AchievementViewModel(
                    container.achievementRepository,
                    container.studySessionRepository,
                    container.taskRepository,
                    container.scheduleRepository
                )
            }
        }
    }
}

@Composable
fun AchievementScreen(viewModel: AchievementViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.achievements, key = { it.id }) { achievement ->
            AchievementRow(achievement = achievement)
        }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementEntity) {
    val progress = if (achievement.target > 0) {
        (achievement.currentProgress.toFloat() / achievement.target).coerceIn(0f, 1f)
    } else {
        0f
    }
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (achievement.unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (achievement.unlocked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(text = achievement.title, style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
                Text(
                    text = stringResource(R.string.achievement_progress, achievement.currentProgress, achievement.target),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(
                    if (achievement.unlocked) R.string.achievement_unlocked else R.string.achievement_locked
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (achievement.unlocked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
