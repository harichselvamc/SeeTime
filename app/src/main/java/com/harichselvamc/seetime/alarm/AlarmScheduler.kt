package com.harichselvamc.seetime.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.harichselvamc.seetime.MainActivity
import com.harichselvamc.seetime.data.AlarmRepository
import com.harichselvamc.seetime.ui.AlarmUi
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

object AlarmScheduler {

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_TITLE = "extra_alarm_title"
    const val EXTRA_ALARM_TARGET_TIME = "extra_alarm_target_time"
    const val EXTRA_ALARM_ZONE = "extra_alarm_zone"

    /**
     * Calculates the next trigger epoch millis for an alarm configured in [targetZone].
     * [repeatDays]: 0=Mon, 1=Tue, ..., 6=Sun.
     */
    fun calculateNextTriggerMillis(
        targetHour: Int,
        targetMinute: Int,
        targetZone: String,
        repeatDays: List<Int> = emptyList(),
        referenceEpochMillis: Long = System.currentTimeMillis()
    ): Long {
        val zoneId = try {
            ZoneId.of(targetZone)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        val nowInZone = ZonedDateTime.ofInstant(Instant.ofEpochMilli(referenceEpochMillis), zoneId)

        if (repeatDays.isEmpty()) {
            // One-time alarm
            var candidate = nowInZone.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
            if (!candidate.isAfter(nowInZone)) {
                candidate = candidate.plusDays(1)
            }
            return candidate.toInstant().toEpochMilli()
        }

        // Recurring alarm across repeat days (0 = Monday, ..., 6 = Sunday)
        for (dayOffset in 0..7) {
            val checkDate = nowInZone.plusDays(dayOffset.toLong())
            val dayIndex = checkDate.dayOfWeek.value - 1 // 0=Mon..6=Sun
            if (repeatDays.contains(dayIndex)) {
                val candidate = checkDate.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
                if (candidate.isAfter(nowInZone)) {
                    return candidate.toInstant().toEpochMilli()
                }
            }
        }

        // Fallback: 1 week later if not found
        return nowInZone.plusDays(7).withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0).toInstant().toEpochMilli()
    }

    /**
     * Formats the target time in user's local timezone for glanceable comparison.
     */
    fun formatLocalEquivalent(
        targetHour: Int,
        targetMinute: Int,
        targetZone: String,
        use24Hour: Boolean = false
    ): String {
        return try {
            val remoteZone = ZoneId.of(targetZone)
            val localZone = ZoneId.systemDefault()
            val nowRemote = ZonedDateTime.now(remoteZone)
            val remoteDateTime = nowRemote.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
            val localDateTime = remoteDateTime.withZoneSameInstant(localZone)

            val h = localDateTime.hour
            val m = localDateTime.minute
            if (use24Hour) {
                String.format("%02d:%02d", h, m)
            } else {
                val ampm = if (h < 12) "AM" else "PM"
                val h12 = when {
                    h == 0 -> 12
                    h <= 12 -> h
                    else -> h - 12
                }
                String.format("%02d:%02d %s", h12, m, ampm)
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Checks if exact alarms can be scheduled on Android 12+ (API 31+).
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * Schedules or reschedules an alarm using Android's AlarmManager.
     */
    fun scheduleAlarm(context: Context, alarm: AlarmUi) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val parts = alarm.targetTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val nextTriggerMillis = calculateNextTriggerMillis(
            targetHour = h,
            targetMinute = m,
            targetZone = alarm.zone,
            repeatDays = alarm.repeatDays
        )

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(EXTRA_ALARM_ID, alarm.id.toString())
            putExtra(EXTRA_ALARM_TITLE, alarm.title)
            putExtra(EXTRA_ALARM_TARGET_TIME, alarm.targetTime)
            putExtra(EXTRA_ALARM_ZONE, alarm.zone)
        }

        val requestCode = alarm.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for AlarmClockInfo
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(nextTriggerMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Fallback for API 31+ if exact alarm permission is not granted
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent)
        }
    }

    /**
     * Cancels an alarm in AlarmManager.
     */
    fun cancelAlarm(context: Context, alarmId: UUID) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        val requestCode = alarmId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Reschedules all enabled alarms from repository (e.g. after reboot or timezone change).
     */
    fun rescheduleAllEnabledAlarms(context: Context) {
        val repo = AlarmRepository.getInstance(context)
        val alarms = repo.getAlarms()
        for (alarm in alarms) {
            if (alarm.isEnabled) {
                scheduleAlarm(context, alarm)
            }
        }
    }
}
