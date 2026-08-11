package com.mother.app.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.model.SessionSource
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.data.timer.TimerPhase
import com.mother.app.di.AppContainer
import com.mother.app.ui.components.EmptyState
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import com.mother.app.ui.components.NeoButton
import com.mother.app.ui.components.NeoOutlinedButton

data class FocusModeUiState(
    val habitTitle: String = "",
    val targetMinute: Int = 0,
    val phase: TimerPhase = TimerPhase.IDLE,
    /** Elapsed whole minutes. */
    val elapsedMinute: Long = 0,
    /** Elapsed seconds within the running segment, for a live clock. */
    val displayText: String = "00:00",
    val progress: Float = 0f
)

/**
 * Drives the distraction-free Focus Mode (PRD §18). The timer itself lives in
 * [ActiveTimerStore]; this view model only renders it and handles stop, which
 * persists a StudySession (PRD §15).
 */
class FocusModeViewModel(
    private val context: android.content.Context,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusModeUiState())
    val uiState: StateFlow<FocusModeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                render()
                delay(1000L)
            }
        }
    }

    private fun render() {
        val now = System.currentTimeMillis()
        val timer = ActiveTimerStore.activeTimer.value
        if (timer == null) {
            _uiState.update { FocusModeUiState() }
            return
        }
        val elapsedMillis = ActiveTimerStore.elapsedMillis(now)
        val totalSeconds = elapsedMillis / 1000L
        val elapsedMinute = totalSeconds / 60L
        val target = timer.targetMinute.coerceAtLeast(1)
        _uiState.update {
            FocusModeUiState(
                habitTitle = timer.habitTitle,
                targetMinute = timer.targetMinute,
                phase = timer.phase,
                elapsedMinute = elapsedMinute,
                displayText = formatClock(totalSeconds),
                progress = (elapsedMinute.toFloat() / target).coerceIn(0f, 1f)
            )
        }
    }

    private fun formatClock(totalSeconds: Long): String {
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    /** Pauses the timer; the notification updates to show paused state with resume action. */
    fun pause() {
        ActiveTimerStore.pause()
        com.mother.app.data.timer.TimerService.start(context)
    }

    fun resume() {
        ActiveTimerStore.resume()
        com.mother.app.data.timer.TimerService.start(context)
    }

    /** Stops the timer and persists the elapsed time as a StudySession (PRD §15). */
    fun stop(onDone: () -> Unit) {
        val stopped = ActiveTimerStore.stop()
        com.mother.app.data.timer.TimerService.stop(context)
        viewModelScope.launch {
            if (stopped != null) {
                val minutes = stopped.first
                val snapshot = stopped.second
                if (minutes > 0) {
                    val now = System.currentTimeMillis()
                    studySessionRepository.upsert(
                        StudySessionEntity(
                            id = UUID.randomUUID().toString(),
                            habitId = snapshot.habitId,
                            startTime = now - minutes * 60_000L,
                            endTime = now,
                            durationMinute = minutes.toInt(),
                            source = SessionSource.TIMER,
                            note = null,
                            createdAt = now
                        )
                    )
                }
            }
            onDone()
        }
    }

    companion object {
        fun factory(context: android.content.Context, container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FocusModeViewModel(context.applicationContext, container.studySessionRepository) }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    viewModel: FocusModeViewModel,
    onExit: () -> Unit,
    onGoToHabits: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.phase == TimerPhase.IDLE) {
        EmptyState(
            icon = Icons.Filled.Timer,
            title = stringResource(R.string.focus_empty_title),
            description = stringResource(R.string.focus_empty_description),
            actionLabel = stringResource(R.string.focus_start_from_habits),
            onAction = onGoToHabits
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.focus_mode_title)) },
            navigationIcon = {
                IconButton(onClick = onExit) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.focus_mode_exit))
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.habitTitle.ifBlank { stringResource(R.string.focus_mode_no_habit) },
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = state.displayText,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(16.dp))
            if (state.targetMinute > 0) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${TimeUtils.formatDurationCompact(state.elapsedMinute.toInt())} / " +
                        TimeUtils.formatDurationCompact(state.targetMinute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                if (state.phase == TimerPhase.RUNNING) {
                    NeoOutlinedButton(
                        text = stringResource(R.string.action_pause),
                        onClick = viewModel::pause,
                        modifier = Modifier.weight(1f)
                    )
                } else if (state.phase == TimerPhase.PAUSED) {
                    NeoButton(
                        text = stringResource(R.string.action_resume),
                        onClick = viewModel::resume,
                        modifier = Modifier.weight(1f)
                    )
                }
                NeoButton(
                    text = stringResource(R.string.action_stop),
                    onClick = { viewModel.stop(onDone = onExit) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
