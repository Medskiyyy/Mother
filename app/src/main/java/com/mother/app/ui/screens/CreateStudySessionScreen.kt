package com.mother.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mother.app.R
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.model.SessionSource
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import com.mother.app.ui.components.NeoButton

data class CreateStudySessionUiState(
    val habitId: String? = null,
    val habits: List<HabitEntity> = emptyList(),
    val date: Long = TimeUtils.todayStart(),
    val startHour: Int = TimeUtils.hourOf(System.currentTimeMillis()),
    val startMinute: Int = TimeUtils.minuteOf(System.currentTimeMillis()),
    val durationText: String = "",
    val note: String = "",
    val habitError: Boolean = false,
    val durationError: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null
)

/** Creates or edits a StudySession. Manual sessions behave like timer ones (PRD §15). */
class CreateStudySessionViewModel(
    private val studySessionRepository: StudySessionRepository,
    private val habitRepository: HabitRepository,
    private val sessionId: String?,
    private val onSaved: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateStudySessionUiState())
    val uiState: StateFlow<CreateStudySessionUiState> = _uiState

    /** Existing entity when editing; null for create. */
    private var existing: StudySessionEntity? = null

    init {
        viewModelScope.launch {
            habitRepository.observeActive().collect { habits ->
                _uiState.update { it.copy(habits = habits) }
            }
        }
        if (sessionId != null) {
            viewModelScope.launch {
                val session = studySessionRepository.getById(sessionId) ?: return@launch
                existing = session
                _uiState.update {
                    it.copy(
                        habitId = session.habitId,
                        date = TimeUtils.startOfDay(session.startTime),
                        startHour = TimeUtils.hourOf(session.startTime),
                        startMinute = TimeUtils.minuteOf(session.startTime),
                        durationText = session.durationMinute.toString(),
                        note = session.note.orEmpty()
                    )
                }
            }
        }
    }

    fun onHabitChange(value: String) =
        _uiState.update { it.copy(habitId = value, habitError = false) }

    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun onStartTimeChange(hour: Int, minute: Int) =
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }

    fun onDurationChange(value: String) =
        _uiState.update { it.copy(durationText = value.filter(Char::isDigit), durationError = false) }

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun save() {
        val state = _uiState.value
        val habitMissing = state.habitId == null
        val duration = state.durationText.toIntOrNull() ?: 0
        val durationInvalid = duration <= 0
        if (habitMissing || durationInvalid) {
            _uiState.update { it.copy(habitError = habitMissing, durationError = durationInvalid) }
            return
        }
        _uiState.update { it.copy(saving = true, errorMessage = null) }
        val startTime = TimeUtils.atLocalTime(state.date, state.startHour, state.startMinute)
        val endTime = startTime + duration * 60_000L
        viewModelScope.launch {
            try {
                studySessionRepository.upsert(
                    StudySessionEntity(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        habitId = state.habitId!!,
                        startTime = startTime,
                        endTime = endTime,
                        durationMinute = duration,
                        source = existing?.source ?: SessionSource.MANUAL,
                        note = state.note.trim().ifBlank { null },
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
                onSaved()
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, errorMessage = e.message) }
            }
        }
    }

    fun refresh() = _uiState.update { it.copy(errorMessage = null, saving = false) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStudySessionScreen(
    container: AppContainer,
    sessionId: String? = null,
    onSaved: () -> Unit,
    onCancelled: () -> Unit
) {
    val viewModel: CreateStudySessionViewModel = viewModel {
        CreateStudySessionViewModel(
            container.studySessionRepository,
            container.habitRepository,
            sessionId,
            onSaved
        )
    }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.refresh()
        }
    }

    val titleRes = if (sessionId == null) R.string.session_form_title_manual else R.string.edit_session_title

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onCancelled) {
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
            HabitDropdown(
                habits = state.habits,
                selectedId = state.habitId,
                isError = state.habitError,
                onSelected = viewModel::onHabitChange
            )
            DeadlineRow(
                deadline = state.date,
                onPicked = viewModel::onDateChange,
                onCleared = {}
            )
            SessionTimeRow(
                hour = state.startHour,
                minute = state.startMinute,
                onPicked = viewModel::onStartTimeChange
            )
            OutlinedTextField(
                value = state.durationText,
                onValueChange = viewModel::onDurationChange,
                label = { Text(stringResource(R.string.field_duration)) },
                isError = state.durationError,
                supportingText = {
                    if (state.durationError) Text(stringResource(R.string.error_target_positive))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChange,
                label = { Text(stringResource(R.string.field_note)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            NeoButton(
                text = stringResource(R.string.form_save),
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDropdown(
    habits: List<HabitEntity>,
    selectedId: String?,
    isError: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = habits.firstOrNull { it.id == selectedId }?.title
        ?: stringResource(R.string.field_not_selected)
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.field_habit)) },
                isError = isError,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
        if (isError) {
            Text(
                text = stringResource(R.string.error_habit_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SessionTimeRow(hour: Int, minute: Int, onPicked: (Int, Int) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(vertical = 8.dp)
    ) {
        Text(stringResource(R.string.field_start_time), style = MaterialTheme.typography.labelMedium)
        Text(
            text = "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.bodyLarge
        )
    }
    if (showPicker) {
        TimePickerDialog(
            title = stringResource(R.string.field_pick_time),
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showPicker = false },
            onConfirm = { pickedHour, pickedMinute ->
                showPicker = false
                onPicked(pickedHour, pickedMinute)
            }
        )
    }
}
