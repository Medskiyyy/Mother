package com.mother.app.ui.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.mother.app.data.model.SessionSource
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class PomodoroPhase { SETUP, FOCUS, BREAK, DONE }

data class PomodoroUiState(
    val focusMinuteText: String = "25",
    val breakMinuteText: String = "5",
    val sessionsText: String = "4",
    val habitId: String? = null,
    val habits: List<HabitEntity> = emptyList(),
    val phase: PomodoroPhase = PomodoroPhase.SETUP,
    val sessionNumber: Int = 1,
    val totalSessions: Int = 0,
    val remainingSeconds: Long = 0,
    val paused: Boolean = false,
    val completedFocus: Int = 0,
    val completedBreaks: Int = 0,
    val focusMinute: Int = 0,
    val breakMinute: Int = 0,
    val errorMessage: String? = null
)

/**
 * Pomodoro timer (PRD §17): configurable focus/break durations and session
 * count. Every finished focus phase can be recorded as a StudySession with
 * source POMODORO when the user links it to a habit. Only one timer may run
 * at a time, so it refuses to start while the study timer is active.
 */
class PomodoroViewModel(
    private val habitRepository: HabitRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var phaseDeadline = 0L
    private var remainingWhenPaused = 0L
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            habitRepository.observeActive().collect { habits ->
                _uiState.update { it.copy(habits = habits) }
            }
        }
    }

    /** True while a focus/break countdown is running or paused. */
    fun isRunning(): Boolean {
        val phase = _uiState.value.phase
        return phase == PomodoroPhase.FOCUS || phase == PomodoroPhase.BREAK
    }

    fun onFocusChange(value: String) =
        _uiState.update { it.copy(focusMinuteText = value.filter(Char::isDigit)) }

    fun onBreakChange(value: String) =
        _uiState.update { it.copy(breakMinuteText = value.filter(Char::isDigit)) }

    fun onSessionsChange(value: String) =
        _uiState.update { it.copy(sessionsText = value.filter(Char::isDigit)) }

    fun onHabitChange(value: String?) = _uiState.update { it.copy(habitId = value) }

    fun start() {
        if (ActiveTimerStore.activeTimer.value != null) {
            _uiState.update { it.copy(errorMessage = "busy") }
            return
        }
        val state = _uiState.value
        val focus = state.focusMinuteText.toIntOrNull() ?: 0
        val breakMinute = state.breakMinuteText.toIntOrNull() ?: 0
        val sessions = state.sessionsText.toIntOrNull() ?: 0
        if (focus <= 0 || breakMinute <= 0 || sessions <= 0) {
            _uiState.update { it.copy(errorMessage = "settings") }
            return
        }
        val now = System.currentTimeMillis()
        phaseDeadline = now + focus * 60_000L
        remainingWhenPaused = 0L
        _uiState.update {
            it.copy(
                phase = PomodoroPhase.FOCUS,
                sessionNumber = 1,
                totalSessions = sessions,
                remainingSeconds = focus * 60L,
                paused = false,
                completedFocus = 0,
                completedBreaks = 0,
                focusMinute = focus,
                breakMinute = breakMinute,
                errorMessage = null
            )
        }
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                tick()
                delay(250L)
            }
        }
    }

    private suspend fun tick() {
        val state = _uiState.value
        if (state.paused || !isRunning()) return
        val remaining = (phaseDeadline - System.currentTimeMillis()) / 1000L
        if (remaining <= 0) {
            advancePhase()
        } else {
            _uiState.update { it.copy(remainingSeconds = remaining) }
        }
    }

    private suspend fun advancePhase() {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        when (state.phase) {
            PomodoroPhase.FOCUS -> {
                val completedFocus = state.completedFocus + 1
                recordFocusSession(state)
                if (completedFocus >= state.totalSessions) {
                    tickerJob?.cancel()
                    _uiState.update {
                        it.copy(phase = PomodoroPhase.DONE, completedFocus = completedFocus, remainingSeconds = 0)
                    }
                } else {
                    phaseDeadline = now + state.breakMinute * 60_000L
                    _uiState.update {
                        it.copy(
                            phase = PomodoroPhase.BREAK,
                            completedFocus = completedFocus,
                            remainingSeconds = state.breakMinute * 60L
                        )
                    }
                }
            }
            PomodoroPhase.BREAK -> {
                phaseDeadline = now + state.focusMinute * 60_000L
                _uiState.update {
                    it.copy(
                        phase = PomodoroPhase.FOCUS,
                        sessionNumber = it.sessionNumber + 1,
                        completedBreaks = it.completedBreaks + 1,
                        remainingSeconds = state.focusMinute * 60L
                    )
                }
            }
            else -> Unit
        }
    }

    private suspend fun recordFocusSession(state: PomodoroUiState) {
        val habitId = state.habitId ?: return
        val now = System.currentTimeMillis()
        val start = now - state.focusMinute * 60_000L
        studySessionRepository.upsert(
            StudySessionEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                startTime = start,
                endTime = now,
                durationMinute = state.focusMinute,
                source = SessionSource.POMODORO,
                note = null,
                createdAt = now
            )
        )
    }

    fun pause() {
        if (!isRunning() || _uiState.value.paused) return
        remainingWhenPaused = ((phaseDeadline - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
        _uiState.update { it.copy(paused = true, remainingSeconds = remainingWhenPaused) }
    }

    fun resume() {
        if (!isRunning() || !_uiState.value.paused) return
        phaseDeadline = System.currentTimeMillis() + remainingWhenPaused * 1000L
        _uiState.update { it.copy(paused = false) }
    }

    fun reset() {
        tickerJob?.cancel()
        _uiState.update { it.copy(phase = PomodoroPhase.SETUP, paused = false, remainingSeconds = 0) }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { PomodoroViewModel(container.habitRepository, container.studySessionRepository) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(viewModel: PomodoroViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val busyMessage = stringResource(R.string.timer_busy_message)
    val settingsMessage = stringResource(R.string.pomodoro_error_settings)

    state.errorMessage?.let { code ->
        LaunchedEffect(code) {
            snackbarHostState.showSnackbar(if (code == "busy") busyMessage else settingsMessage)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.phase) {
                PomodoroPhase.SETUP -> PomodoroSetup(state, viewModel)
                PomodoroPhase.FOCUS, PomodoroPhase.BREAK -> PomodoroRunning(state, viewModel)
                PomodoroPhase.DONE -> PomodoroDone(state, viewModel)
            }
        }
    }
}

@Composable
private fun PomodoroSetup(state: PomodoroUiState, viewModel: PomodoroViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PomodoroNumberField(
            labelRes = R.string.pomodoro_focus_minutes,
            value = state.focusMinuteText,
            onChange = viewModel::onFocusChange,
            modifier = Modifier.weight(1f)
        )
        PomodoroNumberField(
            labelRes = R.string.pomodoro_break_minutes,
            value = state.breakMinuteText,
            onChange = viewModel::onBreakChange,
            modifier = Modifier.weight(1f)
        )
    }
    PomodoroNumberField(
        labelRes = R.string.pomodoro_sessions,
        value = state.sessionsText,
        onChange = viewModel::onSessionsChange,
        modifier = Modifier.fillMaxWidth()
    )
    PomodoroHabitDropdown(
        habits = state.habits,
        selectedId = state.habitId,
        onSelected = viewModel::onHabitChange
    )
    Button(
        onClick = viewModel::start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.pomodoro_start))
    }
}

@Composable
private fun PomodoroRunning(state: PomodoroUiState, viewModel: PomodoroViewModel) {
    val phaseLabel = if (state.phase == PomodoroPhase.FOCUS) {
        stringResource(R.string.pomodoro_phase_focus)
    } else {
        stringResource(R.string.pomodoro_phase_break)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = phaseLabel, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            text = formatCountdown(state.remainingSeconds),
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pomodoro_session_counter, state.sessionNumber, state.totalSessions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = {
                if (state.totalSessions > 0) {
                    (state.completedFocus.toFloat() / state.totalSessions).coerceIn(0f, 1f)
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.paused) {
                Button(onClick = viewModel::resume) {
                    Text(stringResource(R.string.action_resume))
                }
            } else {
                OutlinedButton(onClick = viewModel::pause) {
                    Text(stringResource(R.string.action_pause))
                }
            }
            OutlinedButton(onClick = viewModel::reset) {
                Text(stringResource(R.string.pomodoro_reset))
            }
        }
    }
}

@Composable
private fun PomodoroDone(state: PomodoroUiState, viewModel: PomodoroViewModel) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.pomodoro_done_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.pomodoro_summary,
                    TimeUtils.formatDurationCompact(state.completedFocus * state.focusMinute),
                    TimeUtils.formatDurationCompact(state.completedBreaks * state.breakMinute),
                    state.completedFocus
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    Button(onClick = viewModel::reset, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pomodoro_reset))
    }
}

@Composable
private fun PomodoroNumberField(
    labelRes: Int,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(labelRes)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PomodoroHabitDropdown(
    habits: List<HabitEntity>,
    selectedId: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val noneLabel = stringResource(R.string.field_not_selected)
    val selectedLabel = habits.firstOrNull { it.id == selectedId }?.title ?: noneLabel
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.pomodoro_optional_habit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(noneLabel) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            habits.forEach { habit ->
                DropdownMenuItem(
                    text = { Text(habit.title) },
                    onClick = {
                        onSelected(habit.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatCountdown(totalSeconds: Long): String {
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes.coerceAtLeast(0L), seconds.coerceAtLeast(0L))
}
