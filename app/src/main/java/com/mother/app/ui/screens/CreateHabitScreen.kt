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

data class CreateHabitUiState(
    val title: String = "",
    val targetMinute: String = "",
    val repeatType: RepeatType = RepeatType.DAILY,
    val categoryId: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val showCategoryPicker: Boolean = false,
    /** Null means the habit has no reminder. */
    val reminderHour: Int? = null,
    val reminderMinute: Int = 0,
    val titleError: Boolean = false,
    val targetError: Boolean = false,
    val categoryError: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null
)

class CreateHabitViewModel(
    private val habitRepository: HabitRepository,
    private val categoryRepository: CategoryRepository,
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

    fun onReminderChange(hour: Int?, minute: Int) =
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute) }

    fun deleteHabit() {
        if (habitId == null) return
        viewModelScope.launch {
            habitRepository.deleteById(habitId)
            onSaved()
        }
    }

    fun save() {
        val state = _uiState.value
        val titleBlank = state.title.isBlank()
        val target = state.targetMinute.toIntOrNull() ?: 0
        val targetInvalid = target <= 0
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
                habitRepository.upsert(
                    HabitEntity(
                        id = targetId,
                        categoryId = state.categoryId!!,
                        title = state.title.trim(),
                        targetMinute = target,
                        repeatType = state.repeatType,
                        customRepeatRule = null,
                        reminderEnabled = state.reminderHour != null,
                        color = category?.color ?: "#FF9F43",
                        icon = category?.icon ?: "tag",
                        note = null,
                        archived = false,
                        createdAt = now,
                        updatedAt = now
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
fun CreateHabitScreen(
    container: AppContainer,
    habitId: String? = null,
    onSaved: () -> Unit,
    onCancelled: () -> Unit
) {
    val viewModel: CreateHabitViewModel = viewModel {
        CreateHabitViewModel(
            container.habitRepository,
            container.categoryRepository,
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
                label = { Text(stringResource(R.string.field_target_minutes)) },
                isError = state.targetError,
                supportingText = {
                    if (state.targetError) Text(stringResource(R.string.error_target_positive))
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
            HabitReminderRow(
                hour = state.reminderHour,
                minute = state.reminderMinute,
                onPicked = { hour, minute -> viewModel.onReminderChange(hour, minute) },
                onCleared = { viewModel.onReminderChange(null, 0) }
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

/**
 * Daily reminder time for a habit (PRD §14). Tap to pick; the "remove" action
 * clears it (reminderEnabled = false).
 */
@Composable
private fun HabitReminderRow(
    hour: Int?,
    minute: Int,
    onPicked: (Int, Int) -> Unit,
    onCleared: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val label = if (hour == null) {
        stringResource(R.string.reminder_none)
    } else {
        "%02d:%02d".format(hour, minute)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(R.string.field_reminder), style = MaterialTheme.typography.labelMedium)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        if (hour != null) {
            TextButton(onClick = onCleared) {
                Text(stringResource(R.string.form_cancel))
            }
        }
    }
    if (showPicker) {
        TimePickerDialog(
            title = stringResource(R.string.field_pick_time),
            initialHour = hour ?: 20,
            initialMinute = if (hour == null) 0 else minute,
            onDismiss = { showPicker = false },
            onConfirm = { pickedHour, pickedMinute ->
                showPicker = false
                onPicked(pickedHour, pickedMinute)
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
