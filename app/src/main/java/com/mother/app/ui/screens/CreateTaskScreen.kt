package com.mother.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mother.app.data.repository.ReminderRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import com.mother.app.ui.components.NeoButton
import com.mother.app.ui.components.NeoPriorityBadge
import com.mother.app.util.PriorityUtils
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
    val deadlineError: Boolean = false,
    val reminderOffsets: Set<Int> = emptySet(),
    val saving: Boolean = false,
    val errorMessage: String? = null
)

class CreateTaskViewModel(
    private val context: android.content.Context,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val reminderRepository: ReminderRepository,
    private val taskId: String?,
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
        if (taskId != null) {
            viewModelScope.launch {
                val existing = taskRepository.getById(taskId)
                if (existing != null) {
                    val existingReminders = reminderRepository.getAllTaskReminders().filter { it.taskId == taskId }
                    val offsets = if (existing.deadline != null) {
                        existingReminders.map { ((existing.deadline - it.triggerTime) / 60_000L).toInt() }.toSet()
                    } else {
                        emptySet()
                    }
                    val autoPriority = PriorityUtils.calculatePriority(existing.deadline)
                    _uiState.update {
                        it.copy(
                            title = existing.title,
                            description = existing.description ?: "",
                            priority = autoPriority,
                            deadline = existing.deadline,
                            categoryId = existing.categoryId,
                            reminderOffsets = offsets
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, titleError = false) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value) }

    fun openCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = true) }

    fun closeCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = false) }

    fun selectCategory(category: CategoryEntity) =
        _uiState.update { it.copy(categoryId = category.id, showCategoryPicker = false, categoryError = false) }

    fun onDeadlineChange(value: Long?) = _uiState.update {
        val autoPriority = PriorityUtils.calculatePriority(value)
        it.copy(deadline = value, priority = autoPriority, deadlineError = false)
    }

    fun toggleReminderOffset(offset: Int) = _uiState.update { state ->
        val offsets = state.reminderOffsets.toMutableSet()
        if (!offsets.add(offset)) offsets.remove(offset)
        state.copy(reminderOffsets = offsets)
    }

    fun setReminderOffsets(offsets: Set<Int>) = _uiState.update {
        it.copy(reminderOffsets = offsets)
    }

    fun deleteTask() {
        if (taskId == null) return
        viewModelScope.launch {
            try {
                val oldReminders = reminderRepository.getAllTaskReminders().filter { it.taskId == taskId }
                oldReminders.forEach { ReminderScheduler.cancel(context, ReminderScheduler.OWNER_TASK, taskId, it.id) }
                reminderRepository.deleteTaskReminders(taskId)
            } catch (e: Exception) {
                android.util.Log.w("CreateTask", "Failed to cancel reminders: ${e.message}")
            }
            try {
                taskRepository.deleteById(taskId)
                onSaved()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus tugas: ${e.message}") }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val titleBlank = state.title.isBlank()
        val categoryMissing = state.categoryId == null
        val deadlineMissing = state.deadline == null
        if (titleBlank || categoryMissing || deadlineMissing) {
            _uiState.update {
                it.copy(
                    titleError = titleBlank,
                    categoryError = categoryMissing,
                    deadlineError = deadlineMissing
                )
            }
            return
        }
        _uiState.update { it.copy(saving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val targetId = taskId ?: UUID.randomUUID().toString()
                val autoPriority = PriorityUtils.calculatePriority(state.deadline, now)
                taskRepository.upsert(
                    TaskEntity(
                        id = targetId,
                        title = state.title.trim(),
                        description = state.description.trim().ifBlank { null },
                        categoryId = state.categoryId!!,
                        priority = autoPriority,
                        deadline = state.deadline,
                        status = StatusTask.ACTIVE,
                        note = null,
                        completedAt = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )

                // Cancel old reminders before saving new set
                val oldReminders = reminderRepository.getAllTaskReminders().filter { it.taskId == targetId }
                oldReminders.forEach {
                    ReminderScheduler.cancel(context, ReminderScheduler.OWNER_TASK, targetId, it.id)
                }

                val deadline = state.deadline
                if (deadline != null) {
                    state.reminderOffsets.forEach { offset ->
                        val triggerTime = deadline - offset * 60_000L
                        if (triggerTime > now) {
                            val reminderId = UUID.randomUUID().toString()
                            reminderRepository.upsertTaskReminder(
                                TaskReminderEntity(
                                    id = reminderId,
                                    taskId = targetId,
                                    triggerTime = triggerTime,
                                    snoozeMinute = 0,
                                    enabled = true
                                )
                            )
                            ReminderScheduler.schedule(
                                context,
                                ReminderScheduler.OWNER_TASK,
                                targetId,
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
    taskId: String? = null,
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
            taskId,
            onSaved
        )
    }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.refresh()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (taskId != null) R.string.create_task_title else R.string.create_task_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelled) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.form_cancel)
                        )
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = viewModel::deleteTask) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus Tugas", tint = MaterialTheme.colorScheme.error)
                        }
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

            // Auto Priority Display Card
            AutoPriorityCard(priority = state.priority)

            DeadlineRow(
                deadline = state.deadline,
                isError = state.deadlineError,
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
                    onToggleOffset = viewModel::toggleReminderOffset,
                    onSetOffsets = viewModel::setReminderOffsets,
                    deadline = state.deadline
                )
            }
            Spacer(Modifier.height(4.dp))
            NeoButton(
                text = stringResource(R.string.form_save),
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )
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

@Composable
internal fun AutoPriorityCard(priority: Priority) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.field_priority),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.priority_auto_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            NeoPriorityBadge(priority = priority)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeadlineRow(
    deadline: Long?,
    isError: Boolean = false,
    onPicked: (Long) -> Unit,
    onCleared: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val label = deadline?.let { TimeUtils.formatFullDate(it) }
        ?: stringResource(R.string.field_not_selected)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.field_deadline) + " *",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            if (deadline != null) {
                TextButton(onClick = onCleared) {
                    Text(stringResource(R.string.form_cancel))
                }
            }
        }
        if (isError) {
            Text(
                text = stringResource(R.string.error_deadline_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadline ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) onPicked(millis)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_pick))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPicker)
            .padding(vertical = 8.dp)
    ) {
        Text(stringResource(R.string.field_category), style = MaterialTheme.typography.labelMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        if (isError) {
            Text(
                text = stringResource(R.string.error_category_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
