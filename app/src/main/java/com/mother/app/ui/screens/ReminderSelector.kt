package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mother.app.R

enum class ReminderUnit { MINUTES, HOURS, DAYS }

/** Standard reminder offsets in minutes: at time, 15m, 30m, 1h, 2h, 1d, 2d, 3d, 1w */
val DEFAULT_REMINDER_OFFSETS = listOf(0, 15, 30, 60, 120, 1440, 2880, 4320, 10080)

/** Localized label for a reminder offset. */
@Composable
fun reminderOffsetLabel(offsetMinutes: Int): String = when {
    offsetMinutes == 0 -> stringResource(R.string.reminder_at_time)
    offsetMinutes >= 10080 && offsetMinutes % 10080 == 0 ->
        stringResource(R.string.reminder_weeks_before, offsetMinutes / 10080)
    offsetMinutes >= 1440 && offsetMinutes % 1440 == 0 ->
        stringResource(R.string.reminder_days_before, offsetMinutes / 1440)
    offsetMinutes >= 60 && offsetMinutes % 60 == 0 ->
        stringResource(R.string.reminder_hours_before, offsetMinutes / 60)
    else -> stringResource(R.string.reminder_minutes_before, offsetMinutes)
}

/**
 * Lets the user pick one or more reminder offsets. Supports minutes, hours, days,
 * weeks, daily recurring reminders leading to deadline, and multi-unit custom offsets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSelector(
    selectedOffsets: Set<Int>,
    onToggleOffset: (Int) -> Unit,
    onAddCustomOffset: ((Int) -> Unit)? = null,
    onSetOffsets: ((Set<Int>) -> Unit)? = null,
    deadline: Long? = null,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(ReminderUnit.DAYS) }

    // Combine default offsets and any extra custom offsets from selectedOffsets
    val allOffsets = (DEFAULT_REMINDER_OFFSETS + selectedOffsets).distinct().sorted()

    // Daily offsets calculation if deadline is set
    val now = System.currentTimeMillis()
    val daysUntilDeadline = if (deadline != null && deadline > now) {
        ((deadline - now) / (24 * 60 * 60 * 1000L)).toInt()
    } else {
        0
    }

    val dailyOffsets = remember(daysUntilDeadline) {
        if (daysUntilDeadline >= 1) {
            val count = minOf(daysUntilDeadline, 30)
            (1..count).map { it * 1440 }.toSet() + setOf(0)
        } else {
            emptySet()
        }
    }

    val isAllDailySelected = dailyOffsets.isNotEmpty() && dailyOffsets.all { it in selectedOffsets }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.field_reminder),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(4.dp))

        // Quick button for Daily Reminder until deadline (if deadline is at least 1 day away)
        if (daysUntilDeadline >= 1) {
            FilterChip(
                selected = isAllDailySelected,
                onClick = {
                    if (onSetOffsets != null) {
                        if (isAllDailySelected) {
                            onSetOffsets(selectedOffsets - dailyOffsets)
                        } else {
                            onSetOffsets(selectedOffsets + dailyOffsets)
                        }
                    } else {
                        dailyOffsets.forEach { offset ->
                            if (isAllDailySelected) {
                                if (offset in selectedOffsets) onToggleOffset(offset)
                            } else {
                                if (offset !in selectedOffsets) onToggleOffset(offset)
                            }
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.reminder_daily_until_deadline) + " ($daysUntilDeadline hari)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allOffsets.forEach { offset ->
                FilterChip(
                    selected = offset in selectedOffsets,
                    onClick = { onToggleOffset(offset) },
                    label = { Text(reminderOffsetLabel(offset)) }
                )
            }
            FilterChip(
                selected = false,
                onClick = { showCustomDialog = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Custom")
                    }
                }
            )
        }
    }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Pengingat Custom", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tentukan waktu pengingat sebelum deadline / jadwal:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Jumlah Waktu") },
                        placeholder = { Text("misal: 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Satuan Waktu:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedUnit == ReminderUnit.MINUTES,
                            onClick = { selectedUnit = ReminderUnit.MINUTES },
                            label = { Text(stringResource(R.string.unit_minutes)) }
                        )
                        FilterChip(
                            selected = selectedUnit == ReminderUnit.HOURS,
                            onClick = { selectedUnit = ReminderUnit.HOURS },
                            label = { Text(stringResource(R.string.unit_hours)) }
                        )
                        FilterChip(
                            selected = selectedUnit == ReminderUnit.DAYS,
                            onClick = { selectedUnit = ReminderUnit.DAYS },
                            label = { Text(stringResource(R.string.unit_days)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val number = customInput.toIntOrNull()
                        if (number != null && number >= 0) {
                            val totalMinutes = when (selectedUnit) {
                                ReminderUnit.MINUTES -> number
                                ReminderUnit.HOURS -> number * 60
                                ReminderUnit.DAYS -> number * 1440
                            }
                            if (totalMinutes !in selectedOffsets) {
                                onToggleOffset(totalMinutes)
                            }
                            onAddCustomOffset?.invoke(totalMinutes)
                        }
                        customInput = ""
                        showCustomDialog = false
                    }
                ) {
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
