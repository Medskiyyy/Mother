package com.mother.app.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mother.app.R
import com.mother.app.data.model.Priority
import com.mother.app.data.model.RepeatType
import com.mother.app.data.model.StatusSchedule

/** Localized label for a Priority value. */
@Composable
fun priorityLabel(priority: Priority): String = stringResource(
    when (priority) {
        Priority.AMAN -> R.string.priority_aman
        Priority.WASPADA -> R.string.priority_waspada
        Priority.MEPET -> R.string.priority_mepet
        Priority.URGENT -> R.string.priority_urgent
    }
)

/** Localized label for a StatusSchedule value. */
@Composable
fun statusLabel(status: StatusSchedule): String = stringResource(
    when (status) {
        StatusSchedule.UPCOMING -> R.string.status_label_upcoming
        StatusSchedule.RUNNING -> R.string.status_label_running
        StatusSchedule.COMPLETED -> R.string.status_label_completed
        StatusSchedule.MISSED -> R.string.status_label_missed
        StatusSchedule.CANCELLED -> R.string.status_label_cancelled
    }
)

/** Localized label for a RepeatType value. */
@Composable
fun repeatLabel(repeatType: RepeatType): String = stringResource(
    when (repeatType) {
        RepeatType.NONE -> R.string.repeat_none
        RepeatType.DAILY -> R.string.repeat_daily
        RepeatType.WEEKLY -> R.string.repeat_weekly
        RepeatType.MONTHLY -> R.string.repeat_monthly
        RepeatType.CUSTOM -> R.string.repeat_custom
    }
)

/** Simple wrapper placing a [TimePicker] inside a dialog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.form_cancel))
            }
        }
    )
}
