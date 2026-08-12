@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.harichselvamc.seetime.data.AlarmRepository
import com.harichselvamc.seetime.reminder.TimeReminderWorker
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

data class AlarmUi(
    val id: UUID,
    val title: String,
    val targetTime: String,
    val zone: String,
    val scheduledAt: Long,
    val firesAt: Long,
    val repeatDays: List<Int>, // 0=Mon, 6=Sun
    val isEnabled: Boolean
)

private val SAMSUNG_DAYS = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun RemindersScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val alarmRepo = remember { AlarmRepository.getInstance(context) }

    var alarms by remember { mutableStateOf(alarmRepo.getAlarms()) }
    var cancelTarget by remember { mutableStateOf<AlarmUi?>(null) }

    // Sync from WorkManager metadata to repository on load
    DisposableEffect(workManager) {
        val liveData = workManager.getWorkInfosByTagLiveData(TimeReminderWorker.TAG)
        val observer = Observer<List<WorkInfo>> { infos ->
            val parsedList = (infos ?: emptyList())
                .mapNotNull { info ->
                    val tags = info.tags.toList()
                    val meta = tags.firstOrNull { it.startsWith("meta::") }
                        ?.removePrefix("meta::")?.split("|") ?: return@mapNotNull null

                    val title       = meta.getOrNull(0) ?: "Meeting Alarm"
                    val targetTime  = meta.getOrNull(1) ?: "09:00"
                    val zone        = meta.getOrNull(2) ?: ""
                    val firesAt     = meta.getOrNull(3)?.toLongOrNull() ?: 0L
                    val scheduledAt = meta.getOrNull(4)?.toLongOrNull() ?: 0L
                    val daysStr     = meta.getOrNull(5) ?: ""
                    val repeatDays  = if (daysStr.isBlank()) emptyList()
                                      else daysStr.split(",").mapNotNull { it.toIntOrNull() }

                    val isEnqueued = info.state == WorkInfo.State.ENQUEUED || info.state == WorkInfo.State.RUNNING

                    AlarmUi(
                        id          = info.id,
                        title       = title,
                        targetTime  = targetTime,
                        zone        = zone,
                        scheduledAt = scheduledAt,
                        firesAt     = firesAt,
                        repeatDays  = repeatDays,
                        isEnabled   = isEnqueued
                    )
                }

            // Add any new alarms from WorkManager into repo
            parsedList.forEach { alarmRepo.addAlarm(it) }
            alarms = alarmRepo.getAlarms()
        }
        liveData.observeForever(observer)
        onDispose {
            liveData.removeObserver(observer)
        }
    }

    fun handleToggle(alarm: AlarmUi, enabled: Boolean) {
        if (!enabled) {
            // Cancel in WorkManager
            workManager.cancelWorkById(alarm.id)
            val updated = alarm.copy(isEnabled = false)
            alarmRepo.updateAlarm(updated)
            alarms = alarmRepo.getAlarms()
        } else {
            // Re-enqueue in WorkManager
            try {
                val parts = alarm.targetTime.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

                val zoneId = ZoneId.of(alarm.zone)
                val nowInZone = ZonedDateTime.now(zoneId)
                var targetInZone = nowInZone.withHour(h).withMinute(m).withSecond(0).withNano(0)
                if (targetInZone.isBefore(nowInZone)) {
                    targetInZone = targetInZone.plusDays(1)
                }
                val delayMillis = Duration.between(nowInZone, targetInZone).toMillis()

                val targetTimeStr = String.format("%02d:%02d", h, m)
                val firesAtEpoch = System.currentTimeMillis() + delayMillis
                val scheduledAtEpoch = System.currentTimeMillis()
                val daysStr = alarm.repeatDays.sorted().joinToString(",")

                val metaTag = "meta::${alarm.title}|$targetTimeStr|${alarm.zone}|$firesAtEpoch|$scheduledAtEpoch|$daysStr|true"

                val workRequest = OneTimeWorkRequestBuilder<TimeReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag(TimeReminderWorker.TAG)
                    .addTag(metaTag)
                    .setInputData(
                        workDataOf(
                            "title" to alarm.title,
                            "message" to "Meeting at $targetTimeStr in ${shortZone(alarm.zone)}."
                        )
                    )
                    .build()

                workManager.enqueue(workRequest)
                val updated = alarm.copy(id = workRequest.id, isEnabled = true, firesAt = firesAtEpoch)
                alarmRepo.deleteAlarm(alarm.id)
                alarmRepo.addAlarm(updated)
                alarms = alarmRepo.getAlarms()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Page Header (Samsung Clock Style) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                "Reminders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Recurring meeting alarms & timezone alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No Alarms Set",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Open any time pair card on the Home tab,\ntap ⋮ and choose \"Set Smart Reminder\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.AddAlarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Alarms sound even when the device is locked",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 16.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    SamsungAlarmCard(
                        alarm = alarm,
                        onToggleEnabled = { enabled ->
                            handleToggle(alarm, enabled)
                        },
                        onDelete = { cancelTarget = alarm }
                    )
                }
            }
        }
    }

    cancelTarget?.let { alarm ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Delete Alarm?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "\"${alarm.title}\" scheduled for ${alarm.targetTime} will be permanently removed.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    workManager.cancelWorkById(alarm.id)
                    alarmRepo.deleteAlarm(alarm.id)
                    alarms = alarmRepo.getAlarms()
                    cancelTarget = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Samsung Clock Inspired Alarm Card ─────────────────────────────────

@Composable
private fun SamsungAlarmCard(
    alarm: AlarmUi,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isEnabled = alarm.isEnabled
    val cardAlpha = if (isEnabled) 1.0f else 0.55f

    val formattedTime = remember(alarm.targetTime) {
        val parts = alarm.targetTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val ampm = if (h < 12) "AM" else "PM"
        val h12 = when {
            h == 0  -> 12
            h <= 12 -> h
            else    -> h - 12
        }
        Pair(String.format("%02d:%02d", h12, m), ampm)
    }

    val timeRemaining = remember(alarm.firesAt, isEnabled) {
        if (!isEnabled) ""
        else {
            val diff = alarm.firesAt - System.currentTimeMillis()
            if (diff <= 0) "Alarm due"
            else {
                val h = diff / 3_600_000
                val m = (diff % 3_600_000) / 60_000
                if (h > 0) "Alarm in ${h}h ${m}m" else "Alarm in ${m}m"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Large Time + Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formattedTime.first,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha),
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime.second,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = cardAlpha),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${alarm.title}  ·  ${shortZone(alarm.zone)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardAlpha)
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day Pills (M T W T F S S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SAMSUNG_DAYS.forEachIndexed { index, day ->
                        val isSelected = alarm.repeatDays.contains(index)
                        val circleColor = when {
                            !isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val dayTextColor = when {
                            !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            shape = CircleShape,
                            color = circleColor,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = dayTextColor
                                )
                            }
                        }
                    }
                }

                // Delete Button
                Surface(
                    onClick = onDelete,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete alarm",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }
            }

            // Time Remaining Countdown Label
            if (isEnabled && timeRemaining.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = timeRemaining,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun shortZone(tz: String): String =
    tz.substringAfterLast('/').replace('_', ' ')
