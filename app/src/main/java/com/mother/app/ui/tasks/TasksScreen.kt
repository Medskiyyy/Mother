package com.mother.app.ui.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mother.app.R
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.Priority
import com.mother.app.ui.components.EmptyState
import com.mother.app.ui.components.NeoCard
import com.mother.app.ui.components.NeoPriorityBadge
import com.mother.app.ui.theme.NeoShadowColor
import com.mother.app.ui.theme.PriorityAman
import com.mother.app.ui.theme.PriorityMepet
import com.mother.app.ui.theme.PriorityUrgent
import com.mother.app.ui.theme.PriorityWaspada
import com.mother.app.util.TimeUtils

@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val undoComplete = stringResource(R.string.undo_complete_message)
    val undoDelete = stringResource(R.string.undo_delete_message)
    val undoLabel = stringResource(R.string.action_undo)

    val undoEvent = state.undoEvent
    LaunchedEffect(undoEvent) {
        val event = undoEvent ?: return@LaunchedEffect
        val message = when (event.type) {
            UndoType.COMPLETE, UndoType.REOPEN -> undoComplete
            UndoType.DELETE -> undoDelete
        }
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undo()
        } else {
            viewModel.dismissUndo()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(stringResource(R.string.nav_tasks)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                Tab(
                    selected = state.selectedTab == TasksTab.ACTIVE,
                    onClick = { viewModel.selectTab(TasksTab.ACTIVE) },
                    text = { Text(stringResource(R.string.tasks_tab_active), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = state.selectedTab == TasksTab.COMPLETED,
                    onClick = { viewModel.selectTab(TasksTab.COMPLETED) },
                    text = { Text(stringResource(R.string.tasks_tab_completed), fontWeight = FontWeight.Bold) }
                )
            }

            val query = state.query.trim()
            val tasks = (if (state.selectedTab == TasksTab.ACTIVE) state.activeTasks else state.completedTasks)
                .filter { query.isEmpty() || it.title.contains(query, ignoreCase = true) }

            if (tasks.isEmpty()) {
                if (state.selectedTab == TasksTab.ACTIVE) {
                    EmptyState(
                        icon = Icons.Filled.TaskAlt,
                        title = stringResource(R.string.tasks_empty_title),
                        description = stringResource(R.string.tasks_empty_description)
                    )
                } else {
                    EmptyState(
                        icon = Icons.Filled.Check,
                        title = stringResource(R.string.tasks_completed_empty_title),
                        description = stringResource(R.string.tasks_completed_empty_description)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskSwipeRow(
                            task = task,
                            onDismissed = { direction ->
                                when (direction) {
                                    SwipeToDismissBoxValue.StartToEnd ->
                                        if (state.selectedTab == TasksTab.ACTIVE) {
                                            viewModel.completeTask(task)
                                        } else {
                                            viewModel.reopenTask(task)
                                        }
                                    SwipeToDismissBoxValue.EndToStart -> viewModel.deleteTask(task)
                                    else -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSwipeRow(
    task: TaskEntity,
    onDismissed: (SwipeToDismissBoxValue) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDismissed(value)
            }
            true
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isLeading = direction == SwipeToDismissBoxValue.StartToEnd
            val background by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                label = "swipe_background"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isLeading) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = if (isLeading) Icons.Filled.Check else Icons.Filled.Delete,
                        contentDescription = stringResource(
                            if (isLeading) R.string.action_complete else R.string.action_delete
                        ),
                        tint = if (isLeading) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    ) {
        TaskRow(task = task)
    }
}

@Composable
private fun TaskRow(task: TaskEntity) {
    val backgroundColor = when (task.priority) {
        Priority.URGENT -> PriorityUrgent
        Priority.MEPET -> PriorityMepet
        Priority.WASPADA -> PriorityWaspada
        Priority.AMAN -> PriorityAman
    }

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = backgroundColor,
        contentColor = Color(0xFF121212),
        borderColor = NeoShadowColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFF121212),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = task.deadline?.let { TimeUtils.formatFullDate(it) }
                        ?: stringResource(R.string.field_not_selected),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C2C2C)
                )
            }
            Spacer(Modifier.width(8.dp))
            NeoPriorityBadge(priority = task.priority)
        }
    }
}

