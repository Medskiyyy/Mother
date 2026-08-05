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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.local.entity.TaskReminderEntity
import com.mother.app.data.model.Priority
import com.mother.app.data.model.StatusTask
import com.mother.app.data.reminder.ReminderScheduler
import com.mother.app.data.repository.CategoryRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.AMAN,
    val deadline: Long? = null,
    val categoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val showCategoryPicker: Boolean = false,
    val titleError: Boolean = false,
    val categoryError: Boolean = false,
    val reminderOffsets: Set<Int> = emptySet(),
    val saving: Boolean = false,
    val errorMessage: String? = null
)

class CreateTaskViewModel(
    private val context: android.content.Context,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val reminderRepository: com.mother.app.data.repository.ReminderRepository,
    private val onSaved: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, titleError = false) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value) }

    fun onPriorityChange(value: Priority) =
        _uiState.update { it.copy(priority = value) }

    fun openCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = true) }

    fun closeCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = false) }

    fun selectCategory(category: CategoryEntity) =
        _uiState.update { it.copy(categoryId = category.id, showCategoryPicker = false, categoryError = false) }

    fun onDeadlineChange(value: Long?) = _uiState.update { it.copy(deadline = value) }

    fun toggleReminderOffset(offset: Int) = _uiState.update { state ->
        val offsets = state.reminderOffsets.toMutableSet()
        if (!offsets.add(offset)) offsets.remove(offset)
        state.copy(reminderOffsets = offsets)
    }

    fun save() {
        val state = _uiState.value
        val titleBlank = state.title.isBlank()
        val categoryMissing = state.categoryId == null
        if (titleBlank || categoryMissing) {
            _uiState.update { it.copy(titleError = titleBlank, categoryError = categoryMissing) }
            return
        }
        _uiState.update { it.copy(saving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val taskId = UUID.randomUUID().toString()
                taskRepository.upsert(
                    TaskEntity(
                        id = taskId,
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        categoryId = state.categoryId!!,
                        priority = state.priority,
                        deadline = state.deadline,
                        status = StatusTask.ACTIVE,
                        note = null,
                        completedAt = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                // Task reminders anchor to the deadline (PRD §13).
                val deadline = state.deadline
                if (deadline != null) {
                    state.reminderOffsets.forEach { offset ->
                        val triggerTime = deadline - offset * 60_000L
                        if (triggerTime > now) {
                            val reminderId = UUID.randomUUID().toString()
                            reminderRepository.upsertTaskReminder(
                                TaskReminderEntity(
                                    id = reminderId,
                                    taskId = taskId,
                                    triggerTime = triggerTime,
                                    snoozeMinute = 0,
                                    enabled = true
                                )
                            )
                            ReminderScheduler.schedule(
                                context,
                                ReminderScheduler.OWNER_TASK,
                                taskId,
                                reminderId,
                                triggerTime
                            )
                        }
                    }
                }
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
fun CreateTaskScreen(
    container: AppContainer,
    onSaved: () -> Unit,
    onCancelled: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: CreateTaskViewModel = viewModel {
        CreateTaskViewModel(
            context.applicationContext,
            container.taskRepository,
            container.categoryRepository,
            container.reminderRepository,
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_task_title)) },
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
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.field_description)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            PriorityDropdown(
                selected = state.priority,
                onSelected = viewModel::onPriorityChange
            )
            DeadlineRow(
                deadline = state.deadline,
                onPicked = { viewModel.onDeadlineChange(it) },
                onCleared = { viewModel.onDeadlineChange(null) }
            )
            CategoryRow(
                categories = state.categories,
                selectedId = state.categoryId,
                isError = state.categoryError,
                onOpenPicker = viewModel::openCategoryPicker
            )
            // Reminders need a deadline to anchor to (PRD §13).
            if (state.deadline != null) {
                ReminderSelector(
                    selectedOffsets = state.reminderOffsets,
                    onToggleOffset = viewModel::toggleReminderOffset
                )
            }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PriorityDropdown(selected: Priority, onSelected: (Priority) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = priorityLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_priority)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Priority.entries.forEach { priority ->
                DropdownMenuItem(
                    text = { Text(priorityLabel(priority)) },
                    onClick = {
                        onSelected(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeadlineRow(
    deadline: Long?,
    onPicked: (Long) -> Unit,
    onCleared: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val label = deadline?.let { TimeUtils.formatFullDate(it) }
        ?: stringResource(R.string.field_not_selected)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.field_deadline), style = MaterialTheme.typography.labelMedium)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        if (deadline != null) {
            TextButton(onClick = onCleared) {
                Text(stringResource(R.string.form_cancel))
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let(onPicked)
                    showDatePicker = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.form_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
internal fun CategoryRow(
    categories: List<CategoryEntity>,
    selectedId: String?,
    isError: Boolean,
    onOpenPicker: () -> Unit
) {
    val selected = categories.firstOrNull { it.id == selectedId }
    val label = selected?.name ?: stringResource(R.string.field_not_selected)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenPicker)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(R.string.field_category), style = MaterialTheme.typography.labelMedium)
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
            TextButton(onClick = onOpenPicker) {
                Text(stringResource(R.string.dialog_pick))
            }
        }
        if (isError) {
            Text(
                text = stringResource(R.string.error_category_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
