package com.harichselvamc.seetime.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.ZoneId

@Composable
fun AddTimePairDialog(
    onDismiss: () -> Unit,
    onSave: (fromZone: String, toZone: String) -> Unit,
    initialFrom: String = "Asia/Kolkata",
    initialTo: String = "Europe/London",
    title: String = "Add Time Pair"
) {
    // Load all timezones ONCE (no API, fully offline)
    val allZones = remember {
        ZoneId.getAvailableZoneIds()
            .sorted() // nice alphabetical list
    }

    var fromZone by remember { mutableStateOf(initialFrom) }
    var toZone by remember { mutableStateOf(initialTo) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // which field user is currently editing: "FROM" or "TO"
    var activeField by remember { mutableStateOf("FROM") }

    // suggestions based on last active field and its text
    val suggestions = remember(fromZone, toZone, activeField, allZones) {
        val query = if (activeField == "FROM") fromZone.trim() else toZone.trim()
        if (query.length < 2) {
            emptyList()
        } else {
            allZones
                .filter { it.contains(query, ignoreCase = true) }
                .take(8)
        }
    }

    fun handleSave() {
        val fromTrimmed = fromZone.trim()
        val toTrimmed = toZone.trim()

        if (fromTrimmed.isEmpty() || toTrimmed.isEmpty()) {
            errorText = "Both timezones are required."
            return
        }

        if (!allZones.contains(fromTrimmed)) {
            errorText = "Invalid 'From' timezone."
            return
        }
        if (!allZones.contains(toTrimmed)) {
            errorText = "Invalid 'To' timezone."
            return
        }

        errorText = null
        onSave(fromTrimmed, toTrimmed)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Search IANA time zones (e.g. Asia/Kolkata, Europe/London).",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fromZone,
                    onValueChange = {
                        fromZone = it
                        activeField = "FROM"
                    },
                    label = { Text("From timezone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = toZone,
                    onValueChange = {
                        toZone = it
                        activeField = "TO"
                    },
                    label = { Text("To timezone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick picks",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = {
                            fromZone = "Asia/Kolkata"
                            toZone = "Europe/London"
                            activeField = "TO"
                        },
                        label = { Text("IST → London") },
                        colors = AssistChipDefaults.assistChipColors()
                    )

                    AssistChip(
                        onClick = {
                            fromZone = "Asia/Kolkata"
                            toZone = "America/New_York"
                            activeField = "TO"
                        },
                        label = { Text("IST → New York") },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = {
                            fromZone = "Asia/Kolkata"
                            toZone = "Asia/Dubai"
                            activeField = "TO"
                        },
                        label = { Text("IST → Dubai") },
                        colors = AssistChipDefaults.assistChipColors()
                    )

                    AssistChip(
                        onClick = {
                            // swap quickly
                            val tmp = fromZone
                            fromZone = toZone
                            toZone = tmp
                            // keep activeField as is
                        },
                        label = { Text("Swap") },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }

                // Suggestions below fields (based on what user typed)
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        suggestions.forEach { zone ->
                            Text(
                                text = zone,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (activeField == "FROM") {
                                            fromZone = zone
                                        } else {
                                            toZone = zone
                                        }
                                        errorText = null
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { handleSave() }) {
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
