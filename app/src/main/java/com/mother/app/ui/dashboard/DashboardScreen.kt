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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mother.app.R
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.Priority
import com.mother.app.ui.components.ErrorState
import com.mother.app.ui.components.LoadingState
import com.mother.app.ui.components.NeoCard
import com.mother.app.ui.components.NeoOutlinedButton
import com.mother.app.ui.screens.priorityLabel
import com.mother.app.ui.screens.statusLabel
import com.mother.app.ui.theme.Ink
import com.mother.app.ui.theme.InkSoft
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
                color = MaterialTheme.colorScheme.onSurface
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

/** Streak hero card: yellow block, huge tabular number, flame badge. */
@Composable
private fun StreakCard(streak: Int) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_streak_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.dashboard_streak_value, streak),
                    style = MaterialTheme.typography.displaySmall
                )
            }
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun TargetCard(currentMinutes: Int, targetMinutes: Int) {
    val progress = if (targetMinutes > 0) (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f) else 0f
    NeoCard(modifier = Modifier.fillMaxWidth()) {
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
    NeoCard(modifier = Modifier.fillMaxWidth()) {
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
                    NeoOutlinedButton(
                        text = stringResource(R.string.dashboard_start),
                        onClick = onStartTimer
                    )
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

/** Deadline card colored by priority level (user revision 7). */
@Composable
private fun DeadlineRow(task: TaskEntity) {
    val backgroundColor = when (task.priority) {
        Priority.AMAN -> Color(0xFFBBE5C0)
        Priority.WASPADA -> Color(0xFFFFE08A)
        Priority.MEPET -> Color(0xFFFFC79B)
        Priority.URGENT -> Color(0xFFFF9B9B)
    }
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = backgroundColor,
        contentColor = Ink
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                task.deadline?.let {
                    Text(
                        text = TimeUtils.formatFullDate(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
            }
            Text(
                text = priorityLabel(task.priority),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ScheduleRow(schedule: ScheduleEntity) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
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
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${TimeUtils.formatTime(schedule.startTime)} - ${TimeUtils.formatTime(schedule.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = statusLabel(schedule.status),
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

/** Neobrutalist icon-on-top quick action; label never overflows. */
@Composable
private fun QuickActionButton(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    NeoCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

