package com.mother.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mother.app.R
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.ui.screens.repeatLabel
import com.mother.app.ui.screens.statusLabel
import com.mother.app.util.PriorityUtils
import com.mother.app.util.TimeUtils

/**
 * Neobrutalist Task Overview Dialog displaying title, priority, deadline,
 * description, and an action to navigate to the edit screen.
 */
@Composable
fun TaskDetailDialog(
    task: TaskEntity,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val dynamicPriority = PriorityUtils.calculatePriority(task.deadline)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.weight(1f)
                )
                NeoPriorityBadge(priority = dynamicPriority)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                task.deadline?.let {
                    Column {
                        Text(
                            text = stringResource(R.string.field_deadline) + ":",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = TimeUtils.formatFullDate(it),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.field_description) + ":",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Tidak ada deskripsi tambahan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            NeoButton(
                text = stringResource(R.string.action_edit_task),
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onDelete()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        }
    )
}

/**
 * Neobrutalist Schedule Overview Dialog displaying title, status, priority, time span,
 * repeat frequency, location, description, and an action to navigate to the edit screen.
 */
@Composable
fun ScheduleDetailDialog(
    schedule: ScheduleEntity,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.weight(1f)
                )
                NeoPriorityBadge(priority = schedule.priority)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Status & Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status: " + statusLabel(schedule.status),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = repeatLabel(schedule.repeatType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time Span
                Column {
                    Text(
                        text = "Waktu:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = TimeUtils.formatFullDate(schedule.startTime) + " (" +
                            TimeUtils.formatTime(schedule.startTime) + " - " +
                            TimeUtils.formatTime(schedule.endTime) + ")",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Location if present
                if (!schedule.location.isNullOrBlank()) {
                    Column {
                        Text(
                            text = "Lokasi:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = schedule.location,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Description if present
                Column {
                    Text(
                        text = stringResource(R.string.field_description) + ":",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    if (!schedule.description.isNullOrBlank()) {
                        Text(
                            text = schedule.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Tidak ada deskripsi tambahan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            NeoButton(
                text = stringResource(R.string.action_edit_schedule),
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onDelete()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        }
    )
}
