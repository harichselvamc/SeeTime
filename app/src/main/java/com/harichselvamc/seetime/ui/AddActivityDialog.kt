
package com.harichselvamc.seetime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onSave: (startTime: String, endTime: String, label: String) -> Unit,
    initialLabel: String = ""
) {
    val startTimeState = rememberTimePickerState()
    val endTimeState = rememberTimePickerState()
    var label by remember { mutableStateOf(initialLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Activity", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Start Time", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TimeInput(state = startTimeState)
                Spacer(modifier = Modifier.height(16.dp))
                Text("End Time", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TimeInput(state = endTimeState)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Activity Label") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val startHour = String.format("%02d", startTimeState.hour)
                val startMinute = String.format("%02d", startTimeState.minute)
                val endHour = String.format("%02d", endTimeState.hour)
                val endMinute = String.format("%02d", endTimeState.minute)
                onSave("$startHour:$startMinute", "$endHour:$endMinute", label)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
