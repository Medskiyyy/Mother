package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.mother.app.R

/** Minutes-before offsets offered for reminders; 0 means "at the start". */
val DEFAULT_REMINDER_OFFSETS = listOf(0, 5, 10, 15, 30, 60)

/** Localized label for a reminder offset. */
@Composable
fun reminderOffsetLabel(offsetMinutes: Int): String = when {
    offsetMinutes == 0 -> stringResource(R.string.reminder_at_time)
    offsetMinutes % 60 == 0 -> stringResource(R.string.reminder_hours_before, offsetMinutes / 60)
    else -> stringResource(R.string.reminder_minutes_before, offsetMinutes)
}

/**
 * Lets the user pick one or more reminder offsets (PRD §12: a reminder can
 * have multiple times). Allows adding custom minute offsets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSelector(
    selectedOffsets: Set<Int>,
    onToggleOffset: (Int) -> Unit,
    onAddCustomOffset: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }

    // Combine default offsets and any extra custom offsets from selectedOffsets
    val allOffsets = (DEFAULT_REMINDER_OFFSETS + selectedOffsets).distinct().sorted()

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
                    Text("Berapa menit sebelum deadline/jadwal?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.padding(vertical = 4.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Menit Sebelumnya") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = customInput.toIntOrNull()
                        if (minutes != null && minutes >= 0) {
                            onToggleOffset(minutes)
                            onAddCustomOffset?.invoke(minutes)
                        }
                        customInput = ""
                        showCustomDialog = false
                    }
                ) {
                    Text("Tambah")
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
