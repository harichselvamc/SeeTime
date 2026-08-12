@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.harichselvamc.seetime.reminder.TimeReminderWorker
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
val DAY_FULL_NAMES = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun SmartReminderDialog(
    fromZone: String,
    toZone: String,
    onDismiss: () -> Unit
) {
    var reminderTitle by remember { mutableStateOf("Team Sync") }
    var targetHour by remember { mutableStateOf("9") }
    var targetMinute by remember { mutableStateOf("0") }
    var selectedZone by remember { mutableStateOf(toZone) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Selected repeat days (indices 0..6: Mon=0, Sun=6)
    val selectedDays = remember { mutableStateListOf<Int>() }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Dynamic "fires in" preview calculation
    val firesInText by remember(targetHour, targetMinute, selectedZone, selectedDays.toList()) {
        derivedStateOf {
            try {
                val h = targetHour.toIntOrNull() ?: return@derivedStateOf ""
                val m = targetMinute.toIntOrNull() ?: return@derivedStateOf ""
                if (h !in 0..23 || m !in 0..59) return@derivedStateOf ""

                val zoneId = ZoneId.of(selectedZone)
                val nowInZone = ZonedDateTime.now(zoneId)
                var targetInZone = nowInZone.withHour(h).withMinute(m).withSecond(0).withNano(0)
                if (targetInZone.isBefore(nowInZone)) {
                    targetInZone = targetInZone.plusDays(1)
                }
                val duration = Duration.between(nowInZone, targetInZone)
                val totalMin = duration.toMinutes()
                val hrs = totalMin / 60
                val mins = totalMin % 60
                val timeStr = if (hrs > 0) "Alarm in ${hrs}h ${mins}m" else "Alarm in ${mins}m"
                if (selectedDays.isEmpty()) timeStr
                else "$timeStr · Repeats ${selectedDays.sorted().joinToString(", ") { DAY_FULL_NAMES[it] }}"
            } catch (_: Exception) {
                ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Set Regular Meeting Alarm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Zone toggle
                Text(
                    text = "Target timezone",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedZone == fromZone,
                        onClick = { selectedZone = fromZone },
                        label = {
                            Text(
                                shortZoneName(fromZone),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = selectedZone == toZone,
                        onClick = { selectedZone = toZone },
                        label = {
                            Text(
                                shortZoneName(toZone),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reminderTitle,
                    onValueChange = { reminderTitle = it },
                    label = { Text("Meeting Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = targetHour,
                        onValueChange = { targetHour = it },
                        label = { Text("Hour (0-23)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = targetMinute,
                        onValueChange = { targetMinute = it },
                        label = { Text("Minute (0-59)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Samsung Clock Style Day Pills
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DAY_LABELS.forEachIndexed { index, label ->
                        val isSelected = selectedDays.contains(index)
                        Surface(
                            onClick = {
                                if (isSelected) selectedDays.remove(index)
                                else selectedDays.add(index)
                            },
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { selectedDays.clear() }) {
                        Text("Once", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = {
                        selectedDays.clear()
                        selectedDays.addAll(listOf(0, 1, 2, 3, 4))
                    }) {
                        Text("Mon–Fri", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = {
                        selectedDays.clear()
                        selectedDays.addAll(listOf(0, 1, 2, 3, 4, 5, 6))
                    }) {
                        Text("Every day", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Dynamic preview
                if (firesInText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = firesInText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = targetHour.toIntOrNull()
                    val m = targetMinute.toIntOrNull()
                    if (h == null || h !in 0..23 || m == null || m !in 0..59) {
                        errorText = "Please enter a valid hour (0-23) and minute (0-59)."
                        return@Button
                    }

                    try {
                        val zoneId = ZoneId.of(selectedZone)
                        val nowInZone = ZonedDateTime.now(zoneId)
                        var targetInZone = nowInZone.withHour(h).withMinute(m).withSecond(0).withNano(0)
                        if (targetInZone.isBefore(nowInZone)) {
                            targetInZone = targetInZone.plusDays(1)
                        }
                        val delayMillis = Duration.between(nowInZone, targetInZone).toMillis()

                        val finalTitle = if (reminderTitle.isBlank()) "SeeTime Meeting" else reminderTitle.trim()
                        val targetTimeStr = String.format("%02d:%02d", h, m)
                        val firesAtEpoch = System.currentTimeMillis() + delayMillis
                        val scheduledAtEpoch = System.currentTimeMillis()
                        val daysStr = selectedDays.sorted().joinToString(",")

                        // meta format: title|targetTime|zone|firesAt|scheduledAt|repeatDays|enabled
                        val metaTag = "meta::${finalTitle}|$targetTimeStr|${selectedZone}|$firesAtEpoch|$scheduledAtEpoch|$daysStr|true"

                        val workRequest = OneTimeWorkRequestBuilder<TimeReminderWorker>()
                            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                            .addTag(TimeReminderWorker.TAG)
                            .addTag(metaTag)
                            .setInputData(
                                workDataOf(
                                    "title" to finalTitle,
                                    "message" to "Meeting at $targetTimeStr in ${shortZoneName(selectedZone)}."
                                )
                            )
                            .build()

                        val newAlarm = AlarmUi(
                            id = workRequest.id,
                            title = finalTitle,
                            targetTime = targetTimeStr,
                            zone = selectedZone,
                            scheduledAt = scheduledAtEpoch,
                            firesAt = firesAtEpoch,
                            repeatDays = selectedDays.sorted(),
                            isEnabled = true
                        )
                        com.harichselvamc.seetime.data.AlarmRepository.getInstance(context).addAlarm(newAlarm)

                        WorkManager.getInstance(context).enqueue(workRequest)
                        onDismiss()
                    } catch (e: Exception) {
                        errorText = "Failed: ${e.message}"
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Schedule Alarm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shortZoneName(tz: String): String {
    val idx = tz.lastIndexOf('/')
    return if (idx >= 0 && idx < tz.length - 1) {
        tz.substring(idx + 1).replace('_', ' ')
    } else {
        tz.replace('_', ' ')
    }
}
