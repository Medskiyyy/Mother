package com.mother.app.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TasksTab { ACTIVE, COMPLETED }

enum class UndoType { COMPLETE, REOPEN, DELETE }

/** One undoable action. The id changes on every action so the UI can re-trigger. */
data class UndoEvent(
    val id: Long,
    val type: UndoType,
    val task: TaskEntity
)

data class TasksUiState(
    val activeTasks: List<TaskEntity> = emptyList(),
    val completedTasks: List<TaskEntity> = emptyList(),
    val selectedTab: TasksTab = TasksTab.ACTIVE,
    val query: String = "",
    val undoEvent: UndoEvent? = null
)

class TasksViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var eventCounter = 0L

    init {
        viewModelScope.launch {
            taskRepository.observeActiveTasks().collect { tasks ->
                _uiState.update { it.copy(activeTasks = tasks) }
            }
        }
        viewModelScope.launch {
            taskRepository.observeByStatus("COMPLETED").collect { tasks ->
                _uiState.update { it.copy(completedTasks = tasks) }
            }
        }
    }

    fun selectTab(tab: TasksTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.complete(task.id)
            emitUndo(UndoType.COMPLETE, task)
        }
    }

    fun reopenTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.reopen(task.id)
            emitUndo(UndoType.REOPEN, task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.deleteById(task.id)
            emitUndo(UndoType.DELETE, task)
        }
    }

    /** Restores the task affected by the last undoable action. */
    fun undo() {
        val event = _uiState.value.undoEvent ?: return
        viewModelScope.launch {
            when (event.type) {
                UndoType.COMPLETE -> taskRepository.reopen(event.task.id)
                UndoType.REOPEN -> taskRepository.complete(event.task.id)
                UndoType.DELETE -> taskRepository.upsert(event.task)
            }
            _uiState.update { it.copy(undoEvent = null) }
        }
    }

    fun dismissUndo() {
        _uiState.update { it.copy(undoEvent = null) }
    }

    private fun emitUndo(type: UndoType, task: TaskEntity) {
        eventCounter += 1
        _uiState.update { it.copy(undoEvent = UndoEvent(eventCounter, type, task)) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { TasksViewModel(taskRepository = container.taskRepository) }
        }
    }
}
