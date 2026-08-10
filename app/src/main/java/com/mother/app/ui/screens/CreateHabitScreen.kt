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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mother.app.R
import com.mother.app.data.local.entity.CategoryEntity
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.HabitReminderEntity
import com.mother.app.data.model.RepeatType
import com.mother.app.data.reminder.ReminderScheduler
import com.mother.app.data.repository.CategoryRepository
import com.mother.app.data.repository.HabitRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import com.mother.app.ui.components.NeoButton

data class HabitTime(val hour: Int, val minute: Int)

data class CreateHabitUiState(
    val title: String = "",
    val targetMinute: String = "",
    val repeatType: RepeatType = RepeatType.DAILY,
    val categoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val showCategoryPicker: Boolean = false,
    val reminderTimes: List<HabitTime> = emptyList(),
    val titleError: Boolean = false,
    val targetError: Boolean = false,
    val categoryError: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null
)

class CreateHabitViewModel(
    private val context: android.content.Context,
    private val habitRepository: HabitRepository,
    private val categoryRepository: CategoryRepository,
    private val reminderRepository: com.mother.app.data.repository.ReminderRepository,
    private val habitId: String?,
    private val onSaved: () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        if (habitId != null) {
            viewModelScope.launch {
                val existing = habitRepository.getById(habitId)
                if (existing != null) {
                    val existingReminders = reminderRepository.observeHabitReminders(habitId)
                    _uiState.update {
                        it.copy(
                            title = existing.title,
                            targetMinute = existing.targetMinute.toString(),
                            repeatType = existing.repeatType,
                            categoryId = existing.categoryId
                        )
                    }
                }
            }
            viewModelScope.launch {
                reminderRepository.observeHabitReminders(habitId).collect { reminders ->
                    val times = reminders.map { reminder ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                        HabitTime(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                    }
                    _uiState.update { it.copy(reminderTimes = times) }
                }
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, titleError = false) }

    fun onTargetChange(value: String) =
        _uiState.update { it.copy(targetMinute = value.filter(Char::isDigit), targetError = false) }

    fun onRepeatChange(value: RepeatType) =
        _uiState.update { it.copy(repeatType = value) }

    fun openCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = true) }

    fun closeCategoryPicker() = _uiState.update { it.copy(showCategoryPicker = false) }

    fun selectCategory(category: CategoryEntity) =
        _uiState.update { it.copy(categoryId = category.id, showCategoryPicker = false, categoryError = false) }

    fun addReminderTime(hour: Int, minute: Int) = _uiState.update { state ->
        val newTime = HabitTime(hour, minute)
        if (state.reminderTimes.contains(newTime)) state else state.copy(reminderTimes = state.reminderTimes + newTime)
    }

    fun removeReminderTime(time: HabitTime) = _uiState.update { state ->
        state.copy(reminderTimes = state.reminderTimes - time)
    }

    fun deleteHabit() {
        if (habitId == null) return
        viewModelScope.launch {
            val oldReminders = reminderRepository.getAllHabitReminders().filter { it.habitId == habitId }
            oldReminders.forEach { old ->
                ReminderScheduler.cancel(
                    context,
                    ReminderScheduler.OWNER_HABIT,
                    habitId,
                    old.id
                )
            }
            reminderRepository.deleteHabitReminders(habitId)
            habitRepository.deleteById(habitId)
            onSaved()
        }
    }

    fun save() {
        val state = _uiState.value
        val titleBlank = state.title.isBlank()
        val target = state.targetMinute.toIntOrNull() ?: 0
        val targetInvalid = target < 0
        val categoryMissing = state.categoryId == null
        if (titleBlank || targetInvalid || categoryMissing) {
            _uiState.update {
                it.copy(
                    titleError = titleBlank,
                    targetError = targetInvalid,
                    categoryError = categoryMissing
                )
            }
            return
        }
        val category = state.categories.firstOrNull { it.id == state.categoryId }
        _uiState.update { it.copy(saving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val targetId = habitId ?: UUID.randomUUID().toString()
                val existingHabit = if (habitId != null) habitRepository.getById(habitId) else null
                val createdAt = existingHabit?.createdAt ?: now

                habitRepository.upsert(
                    HabitEntity(
                        id = targetId,
                        categoryId = state.categoryId!!,
                        title = state.title.trim(),
                        targetMinute = target,
                        repeatType = state.repeatType,
                        customRepeatRule = null,
                        reminderEnabled = state.reminderTimes.isNotEmpty(),
                        color = category?.color ?: "#FF9F43",
                        icon = category?.icon ?: "tag",
                        note = null,
                        archived = existingHabit?.archived ?: false,
                        createdAt = createdAt,
                        updatedAt = now
                    )
                )

                // Clean up existing alarms and reminders before saving updated ones
                val oldReminders = reminderRepository.getAllHabitReminders().filter { it.habitId == targetId }
                oldReminders.forEach { old ->
                    ReminderScheduler.cancel(
                        context,
                        ReminderScheduler.OWNER_HABIT,
                        targetId,
                        old.id
                    )
                }
                reminderRepository.deleteHabitReminders(targetId)

                // Save multiple reminders and schedule exact alarms for each
                state.reminderTimes.forEach { time ->
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, time.hour)
                        set(java.util.Calendar.MINUTE, time.minute)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                        if (timeInMillis <= now) {
                            add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val triggerTime = cal.timeInMillis
                    val reminderId = UUID.randomUUID().toString()
                    reminderRepository.upsertHabitReminder(
                        HabitReminderEntity(
                            id = reminderId,
                            habitId = targetId,
                            triggerTime = triggerTime,
                            snoozeMinute = 0,
                            enabled = true
                        )
                    )
                    ReminderScheduler.scheduleRepeating(
                        context,
                        ReminderScheduler.OWNER_HABIT,
                        targetId,
                        reminderId,
                        triggerTime
                    )
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
fun CreateHabitScreen(
    container: AppContainer,
    habitId: String? = null,
    onSaved: () -> Unit,
    onCancelled: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: CreateHabitViewModel = viewModel {
        CreateHabitViewModel(
            context.applicationContext,
            container.habitRepository,
            container.categoryRepository,
            container.reminderRepository,
            habitId,
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
                title = { Text(if (habitId != null) "Edit Kebiasaan" else stringResource(R.string.create_habit_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancelled) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.form_cancel))
                    }
                },
                actions = {
                    if (habitId != null) {
                        IconButton(onClick = viewModel::deleteHabit) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus Kebiasaan", tint = MaterialTheme.colorScheme.error)
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
                value = state.targetMinute,
                onValueChange = viewModel::onTargetChange,
                label = { Text("Target (menit) - Opsional") },
                isError = state.targetError,
                supportingText = {
                    if (state.targetError) {
                        Text(stringResource(R.string.error_target_positive))
                    } else {
                        Text("Kosongkan atau isi 0 jika hanya pengingat rutinitas")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            RepeatDropdown(
                selected = state.repeatType,
                onSelected = viewModel::onRepeatChange
            )
            CategoryRow(
                categories = state.categories,
                selectedId = state.categoryId,
                isError = state.categoryError,
                onOpenPicker = viewModel::openCategoryPicker
            )
            MultiHabitReminderSection(
                reminderTimes = state.reminderTimes,
                onAddReminder = viewModel::addReminderTime,
                onRemoveReminder = viewModel::removeReminderTime
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

    if (state.showCategoryPicker) {
        CategoryPicker(
            categories = state.categories,
            onSelected = viewModel::selectCategory,
            onDismiss = viewModel::closeCategoryPicker
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MultiHabitReminderSection(
    reminderTimes: List<HabitTime>,
    onAddReminder: (Int, Int) -> Unit,
    onRemoveReminder: (HabitTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pengingat Harian (Bisa Lebih dari 1)",
            style = MaterialTheme.typography.labelMedium
        )
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reminderTimes.forEach { time ->
                androidx.compose.material3.FilterChip(
                    selected = true,
                    onClick = { onRemoveReminder(time) },
                    label = { Text("%02d:%02d ✕".format(time.hour, time.minute)) }
                )
            }
            androidx.compose.material3.FilterChip(
                selected = false,
                onClick = { showTimePicker = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Tambah Jam")
                    }
                }
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            title = stringResource(R.string.field_pick_time),
            initialHour = 8,
            initialMinute = 0,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                onAddReminder(hour, minute)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatDropdown(selected: RepeatType, onSelected: (RepeatType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = repeatLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_repeat)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RepeatType.entries.forEach { repeatType ->
                DropdownMenuItem(
                    text = { Text(repeatLabel(repeatType)) },
                    onClick = {
                        onSelected(repeatType)
                        expanded = false
                    }
                )
            }
        }
    }
}
