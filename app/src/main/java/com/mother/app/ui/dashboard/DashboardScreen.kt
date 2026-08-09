package com.mother.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mother.app.R
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.model.Priority
import com.mother.app.ui.components.ErrorState
import com.mother.app.ui.components.LoadingState
import com.mother.app.ui.components.NeoCard
import com.mother.app.ui.components.NeoOutlinedButton
import com.mother.app.ui.components.NeoPriorityBadge
import com.mother.app.ui.screens.statusLabel
import com.mother.app.ui.theme.NeoShadowColor
import com.mother.app.ui.theme.NeoStreakYellow
import com.mother.app.ui.theme.PriorityAman
import com.mother.app.ui.theme.PriorityMepet
import com.mother.app.ui.theme.PriorityUrgent
import com.mother.app.ui.theme.PriorityWaspada
import com.mother.app.util.TimeUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartTimer: () -> Unit,
    onSearch: () -> Unit,
    onEditTask: ((String) -> Unit)? = null,
    onEditSchedule: ((String) -> Unit)? = null,
    onAddTask: (() -> Unit)? = null,
    onAddSchedule: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(description = state.error!!, onRetry = { viewModel.refresh() })
        else -> DashboardContent(state, onStartTimer, onSearch, onEditTask, onEditSchedule, onAddTask, onAddSchedule)
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onStartTimer: () -> Unit,
    onSearch: () -> Unit,
    onEditTask: ((String) -> Unit)? = null,
    onEditSchedule: ((String) -> Unit)? = null,
    onAddTask: (() -> Unit)? = null,
    onAddSchedule: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                DeadlineRow(task, onClick = { onEditTask?.invoke(task.id) })
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
                ScheduleRow(schedule, onClick = { onEditSchedule?.invoke(schedule.id) })
            }
        }
        item {
            QuickActions(
                onStartTimer = onStartTimer,
                onAddTask = { onAddTask?.invoke() },
                onAddSchedule = { onAddSchedule?.invoke() }
            )
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
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
        ) {
            IconButton(onClick = onSearch) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_title),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Streak Hero Card: Bold Neobrutalist Neon Yellow Block with black flame badge & giant number */
@Composable
private fun StreakCard(streak: Int) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = NeoStreakYellow,
        contentColor = Color(0xFF121212),
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF121212))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_streak_label).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoStreakYellow,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$streak",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF121212),
                        lineHeight = 48.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "HARI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF121212),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF121212))
                    .border(3.dp, Color(0xFF121212), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = null,
                    tint = NeoStreakYellow,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

@Composable
private fun TargetCard(currentMinutes: Int, targetMinutes: Int) {
    val progress = if (targetMinutes > 0) (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f) else 0f
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_target_today),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = stringResource(
                        R.string.dashboard_target_progress,
                        TimeUtils.formatDurationCompact(currentMinutes),
                        TimeUtils.formatDurationCompact(targetMinutes)
                    ),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun NextActivityCard(schedule: ScheduleEntity?, onStartTimer: () -> Unit) {
    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_next_activity).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(4.dp))
                if (schedule == null) {
                    Text(
                        text = stringResource(R.string.dashboard_next_activity_empty),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = TimeUtils.formatTime(schedule.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (schedule != null) {
                Spacer(Modifier.width(8.dp))
                NeoOutlinedButton(
                    text = stringResource(R.string.dashboard_start),
                    onClick = onStartTimer
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EmptyRow(text: String) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Deadline Card: Full Vibrant Color according to priority, black stroke, capsule badge */
@Composable
private fun DeadlineRow(task: TaskEntity, onClick: () -> Unit = {}) {
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
        borderColor = MaterialTheme.colorScheme.outline,
        onClick = onClick
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
                task.deadline?.let {
                    Text(
                        text = TimeUtils.formatFullDate(it),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C2C2C)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            NeoPriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun ScheduleRow(schedule: ScheduleEntity, onClick: () -> Unit = {}) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF121212),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActions(
    onStartTimer: () -> Unit,
    onAddTask: () -> Unit,
    onAddSchedule: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            stringResource(R.string.dashboard_quick_add_task),
            Icons.Filled.Add,
            Modifier.weight(1f).fillMaxSize(),
            onClick = onAddTask
        )
        QuickActionButton(
            stringResource(R.string.dashboard_quick_add_schedule),
            Icons.AutoMirrored.Filled.EventNote,
            Modifier.weight(1f).fillMaxSize(),
            onClick = onAddSchedule
        )
        QuickActionButton(
            stringResource(R.string.dashboard_quick_start_timer),
            Icons.Filled.Timer,
            Modifier.weight(1f).fillMaxSize(),
            onClick = onStartTimer
        )
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    NeoCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


