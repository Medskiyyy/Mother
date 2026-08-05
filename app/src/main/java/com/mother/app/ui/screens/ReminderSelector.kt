package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mother.app.R

/** Minutes-before offsets offered for reminders; 0 means "at the start". */
val REMINDER_OFFSETS = listOf(0, 5, 10, 15, 30, 60)

/** Localized label for a reminder offset. */
@Composable
fun reminderOffsetLabel(offsetMinutes: Int): String = when {
    offsetMinutes == 0 -> stringResource(R.string.reminder_at_time)
    offsetMinutes % 60 == 0 -> stringResource(R.string.reminder_hours_before, offsetMinutes / 60)
    else -> stringResource(R.string.reminder_minutes_before, offsetMinutes)
}

/**
 * Lets the user pick one or more reminder offsets (PRD §12: a reminder can
 * have multiple times). [selectedOffsets] holds minutes-before values; 0 means
 * "at the start". The caller turns these into reminder rows at save time.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSelector(
    selectedOffsets: Set<Int>,
    onToggleOffset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.field_reminder),
            style = MaterialTheme.typography.labelMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            REMINDER_OFFSETS.forEach { offset ->
                FilterChip(
                    selected = offset in selectedOffsets,
                    onClick = { onToggleOffset(offset) },
                    label = { Text(reminderOffsetLabel(offset)) }
                )
            }
        }
    }
}
