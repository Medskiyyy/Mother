package com.mother.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mother.app.R
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.ui.components.ErrorState
import com.mother.app.ui.components.LoadingState
import com.mother.app.util.TimeUtils

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onStartTimer: () -> Unit, onSearch: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(description = state.error!!, onRetry = { viewModel.refresh() })
        else -> DashboardContent(state, onStartTimer, onSearch)
    }
}

@Composable
private fun DashboardContent(state: DashboardUiState, onStartTimer: () -> Unit, onSearch: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(state.greeting, state.dateLabel, onSearch)
        }
        item {
            StreakCard(state.streak)
        }
        item {
            TargetCard(state.todayStudyMinutes, state.dailyTargetMinutes)
        }
        item {
            NextActivityCard(state.nextActivity, onStartTimer)
        }
        item {
            SectionTitle(stringResource(R.string.dashboard_deadlines))
        }
        if (state.deadlines.isEmpty()) {
            item {
                EmptyRow(stringResource(R.string.dashboard_deadlines_empty))
            }
        } else {
            items(state.deadlines, key = { it.id }) { task ->
                DeadlineRow(task)
            }
        }
        item {
            SectionTitle(stringResource(R.string.dashboard_schedule_today))
        }
        if (state.todaySchedule.isEmpty()) {
            item {
                EmptyRow(stringResource(R.string.dashboard_schedule_empty))
            }
        } else {
            items(state.todaySchedule, key = { it.id }) { schedule ->
                ScheduleRow(schedule)
            }
        }
        item {
            QuickActions(onStartTimer)
        }
    }
}

@Composable
private fun Header(greeting: String, dateLabel: String, onSearch: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onSearch) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_title))
        }
    }
}

@Composable
private fun StreakCard(streak: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔥", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.dashboard_streak_days, streak),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun TargetCard(currentMinutes: Int, targetMinutes: Int) {
    val progress = if (targetMinutes > 0) (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f) else 0f
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_target_today),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.dashboard_target_progress,
                    TimeUtils.formatDurationCompact(currentMinutes),
                    TimeUtils.formatDurationCompact(targetMinutes)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NextActivityCard(schedule: ScheduleEntity?, onStartTimer: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_next_activity),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            if (schedule == null) {
                Text(
                    text = stringResource(R.string.dashboard_next_activity_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = TimeUtils.formatTime(schedule.startTime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onStartTimer) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null)
                        Spacer(Modifier.size(4.dp))
                        Text(stringResource(R.string.dashboard_start))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EmptyRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeadlineRow(task: TaskEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                task.deadline?.let {
                    Text(
                        text = TimeUtils.formatFullDate(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = task.priority.name,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ScheduleRow(schedule: ScheduleEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = schedule.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${TimeUtils.formatTime(schedule.startTime)} - ${TimeUtils.formatTime(schedule.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = schedule.status.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActions(onStartTimer: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            stringResource(R.string.dashboard_quick_add_task),
            Icons.Filled.Add,
            Modifier.weight(1f)
        )
        QuickActionButton(
            stringResource(R.string.dashboard_quick_add_schedule),
            Icons.AutoMirrored.Filled.EventNote,
            Modifier.weight(1f)
        )
        QuickActionButton(
            stringResource(R.string.dashboard_quick_start_timer),
            Icons.Filled.Timer,
            Modifier.weight(1f),
            onClick = onStartTimer
        )
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.size(4.dp))
        Text(label)
    }
}

