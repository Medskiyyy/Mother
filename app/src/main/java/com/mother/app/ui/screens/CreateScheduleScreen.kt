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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mother.app.R
import com.mother.app.data.local.entity.CategoryEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.model.Priority
import com.mother.app.data.model.RepeatType
import com.mother.app.data.model.StatusSchedule
import com.mother.app.data.repository.CategoryRepository
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

data class CreateScheduleUiState(
    val title: String = "",
    val date: Long = TimeUtils.todayStart(),
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 10,
    val endMinute: Int = 0,
    val priority: Priority = Priority.AMAN,
    val categoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val showCategoryPicker: Boolean = false,
    val showConflictDialog: Boolean = false,
    val titleError: Boolean = false,
    val categoryError: Boolean = false,
    val timeError: String? = null,
    val saving: Boolean = false,
    val errorMessage: String? = null
)

class CreateScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val categoryRepository: CategoryRepository,
    private val onSaved: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, titleError = false) }

    fun onDateChange(value: Long) = _uiState.update { it.copy(date = value) }

    fun onStartTimeChange(hour: Int, minute: Int) =
        _uiState.update { it.copy(startHour = hour, startMinute = minute, timeError = null) }

    fun onEndTimeChange(hour: Int, minute: Int) =
        _uiState.update { it.copy(endHour = hour, endMinute = minute, timeError = null) }

    fun onPriorityChange(value: Priority) =
        _uiState.update { it.copy(priority = value) }

    fun openCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = true) }

    fun closeCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = false) }

    fun selectCategory(category: CategoryEntity) =
        _uiState.update { it.copy(categoryId = category.id, showCategoryPicker = false, categoryError = false) }

    private fun startTimeOf(state: CreateScheduleUiState): Long =
        atLocalTime(state.date, state.startHour, state.startMinute)

    private fun endTimeOf(state: CreateScheduleUiState): Long =
        atLocalTime(state.date, state.endHour, state.endMinute)

    private fun atLocalTime(dayStart: Long, hour: Int, minute: Int): Long {
        val zone = java.time.ZoneId.systemDefault()
        return java.time.Instant.ofEpochMilli(dayStart)
            .atZone(zone)
            .toLocalDate()
            .atTime(LocalTime.of(hour, minute))
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun save() {
        val state = _uiState.value
        val titleBlank = state.title.isBlank()
        val categoryMissing = state.categoryId == null
        if (titleBlank || categoryMissing) {
            _uiState.update { it.copy(titleError = titleBlank, categoryError = categoryMissing) }
            return
        }
        val start = startTimeOf(state)
        val end = endTimeOf(state)
        if (end <= start) {
            _uiState.update { it.copy(timeError = "time_order") }
            return
        }
        _uiState.update { it.copy(saving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                if (scheduleRepository.hasConflict(start, end)) {
                    _uiState.update { it.copy(saving = false, showConflictDialog = true) }
                } else {
                    upsertSchedule(start, end)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, errorMessage = e.message) }
            }
        }
    }

    /** Continues saving after the user accepts the conflict warning (PRD §12). */
    fun confirmConflictSave() {
        val state = _uiState.value
        _uiState.update { it.copy(showConflictDialog = false, saving = true) }
        viewModelScope.launch {
            try {
                upsertSchedule(startTimeOf(state), endTimeOf(state))
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, errorMessage = e.message) }
            }
        }
    }

    fun dismissConflictDialog() = _uiState.update { it.copy(showConflictDialog = false) }

    private suspend fun upsertSchedule(start: Long, end: Long) {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        scheduleRepository.upsert(
            ScheduleEntity(
                id = UUID.randomUUID().toString(),
                title = state.title.trim(),
                description = null,
                categoryId = state.categoryId!!,
                priority = state.priority,
                startTime = start,
                endTime = end,
                repeatType = RepeatType.NONE,
                customRepeatRule = null,
                location = null,
                note = null,
                status = StatusSchedule.UPCOMING,
                createdAt = now,
                updatedAt = now
            )
        )
        onSaved()
    }

    fun refresh() = _uiState.update { it.copy(errorMessage = null, saving = false) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    container: AppContainer,
    onSaved: () -> Unit,
    onCancelled: () -> Unit
) {
    val viewModel: CreateScheduleViewModel = viewModel {
        CreateScheduleViewModel(container.scheduleRepository, container.categoryRepository, onSaved)
    }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.refresh()
        }
    }

    val timeErrorText = when (state.timeError) {
        "time_order" -> stringResource(R.string.error_time_order)
        "time_required" -> stringResource(R.string.error_time_required)
        else -> null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_schedule_title)) },
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
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.field_title)) },
                isError = state.titleError,
                supportingText = {
                    if (state.titleError) Text(stringResource(R.string.error_title_required))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DeadlineRow(
                deadline = state.date,
                onPicked = viewModel::onDateChange,
                onCleared = {}
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeRow(
                    labelRes = R.string.field_start_time,
                    hour = state.startHour,
                    minute = state.startMinute,
                    onPicked = viewModel::onStartTimeChange,
                    modifier = Modifier.weight(1f)
                )
                TimeRow(
                    labelRes = R.string.field_end_time,
                    hour = state.endHour,
                    minute = state.endMinute,
                    onPicked = viewModel::onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
            if (timeErrorText != null) {
                Text(
                    text = timeErrorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            PriorityDropdown(
                selected = state.priority,
                onSelected = viewModel::onPriorityChange
            )
            CategoryRow(
                categories = state.categories,
                selectedId = state.categoryId,
                isError = state.categoryError,
                onOpenPicker = viewModel::openCategoryPicker
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = viewModel::save,
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.form_save))
            }
        }
    }

    if (state.showCategoryPicker) {
        CategoryPicker(
            categories = state.categories,
            onSelected = viewModel::selectCategory,
            onDismiss = viewModel::closeCategoryPicker
        )
    }

    if (state.showConflictDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissConflictDialog,
            title = { Text(stringResource(R.string.dialog_conflict_title)) },
            text = { Text(stringResource(R.string.dialog_conflict_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmConflictSave) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConflictDialog) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        )
    }
}

@Composable
private fun TimeRow(
    labelRes: Int,
    hour: Int,
    minute: Int,
    onPicked: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clickable { showPicker = true }
            .padding(vertical = 8.dp)
    ) {
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium)
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
