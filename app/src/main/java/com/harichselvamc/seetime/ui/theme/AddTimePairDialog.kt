@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.time.ZoneId

@Composable
fun AddTimePairDialog(
    onDismiss: () -> Unit,
    onSave: (fromZone: String, toZone: String, label: String) -> Unit,
    initialFrom: String = "Asia/Kolkata",
    initialTo: String = "Europe/London",
    initialLabel: String = "",
    title: String = "Add Time Pair"
) {
    val allZones = remember {
        ZoneId.getAvailableZoneIds().sorted()
    }

    var fromZone by remember { mutableStateOf(initialFrom) }
    var toZone by remember { mutableStateOf(initialTo) }
    var label by remember { mutableStateOf(initialLabel) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var activeField by remember { mutableStateOf("FROM") }

    val suggestions = remember(fromZone, toZone, activeField, allZones) {
        val query = if (activeField == "FROM") fromZone.trim() else toZone.trim()
        if (query.length < 2) {
            emptyList()
        } else {
            allZones
                .filter { it.contains(query, ignoreCase = true) }
                .take(6)
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
        onSave(fromTrimmed, toTrimmed, label.trim())
        onDismiss()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Search IANA time zones (e.g. Asia/Kolkata, Europe/London)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // From field
            OutlinedTextField(
                value = fromZone,
                onValueChange = {
                    fromZone = it
                    activeField = "FROM"
                    errorText = null
                },
                label = { Text("From timezone") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Swap button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val tmp = fromZone
                        fromZone = toZone
                        toZone = tmp
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Swap zones",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // To field
            OutlinedTextField(
                value = toZone,
                onValueChange = {
                    toZone = it
                    activeField = "TO"
                    errorText = null
                },
                label = { Text("To timezone") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Label field
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (e.g. Work, Family, Travel)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick picks
            Text(
                text = "Quick Picks",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
                    label = { Text("IST → London", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors()
                )
                AssistChip(
                    onClick = {
                        fromZone = "Asia/Kolkata"
                        toZone = "America/New_York"
                        activeField = "TO"
                    },
                    label = { Text("IST → NYC", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors()
                )
                AssistChip(
                    onClick = {
                        fromZone = "Asia/Kolkata"
                        toZone = "Asia/Dubai"
                        activeField = "TO"
                    },
                    label = { Text("IST → Dubai", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }

            // Suggestions
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (activeField == "FROM") "From suggestions" else "To suggestions",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(suggestions) { zone ->
                        val query = if (activeField == "FROM") fromZone.trim() else toZone.trim()
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (activeField == "FROM") fromZone = zone
                                else toZone = zone
                                errorText = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = highlightMatch(zone, query),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Error
            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = { handleSave() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Save",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun highlightMatch(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.length < 2) return buildAnnotatedString { append(text) }

    val startIndex = text.indexOf(query, ignoreCase = true)
    if (startIndex < 0) return buildAnnotatedString { append(text) }

    return buildAnnotatedString {
        append(text.substring(0, startIndex))
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            append(text.substring(startIndex, startIndex + query.length))
        }
        append(text.substring(startIndex + query.length))
    }
}
