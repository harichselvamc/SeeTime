package com.harichselvamc.seetime.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.harichselvamc.seetime.MainActivity
import com.harichselvamc.seetime.data.AlarmRepository
import java.util.UUID

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "seetime_alarms_channel"
        const val NOTIFICATION_ID_BASE = 8000

        const val ACTION_TRIGGER_ALARM = "com.harichselvamc.seetime.ACTION_TRIGGER_ALARM"
        const val ACTION_DISMISS_ALARM = "com.harichselvamc.seetime.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.harichselvamc.seetime.ACTION_SNOOZE_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            ACTION_TRIGGER_ALARM -> handleTriggerAlarm(context, intent)
            ACTION_DISMISS_ALARM -> handleDismissAlarm(context, intent)
            ACTION_SNOOZE_ALARM -> handleSnoozeAlarm(context, intent)
        }
    }

    private fun handleTriggerAlarm(context: Context, intent: Intent) {
        val idStr = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "Meeting Alarm"
        val targetTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TARGET_TIME) ?: "09:00"
        val zone = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ZONE) ?: ""

        val alarmId = try { UUID.fromString(idStr) } catch (_: Exception) { return }
        val repo = AlarmRepository.getInstance(context)
        val alarm = repo.getAlarms().firstOrNull { it.id == alarmId }

        val zoneDisplay = if (zone.isNotBlank()) {
            zone.substringAfterLast('/').replace('_', ' ')
        } else {
            "Local"
        }

        showAlarmNotification(context, alarmId, title, targetTime, zoneDisplay)

        // Reschedule if recurring or disable if one-time
        if (alarm != null) {
            if (alarm.repeatDays.isNotEmpty()) {
                val parts = alarm.targetTime.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val nextTrigger = AlarmScheduler.calculateNextTriggerMillis(
                    targetHour = h,
                    targetMinute = m,
                    targetZone = alarm.zone,
                    repeatDays = alarm.repeatDays,
                    referenceEpochMillis = System.currentTimeMillis() + 60_000L // Ensure next occurrence
                )
                val updated = alarm.copy(firesAt = nextTrigger)
                repo.updateAlarm(updated)
                AlarmScheduler.scheduleAlarm(context, updated)
            } else {
                val updated = alarm.copy(isEnabled = false)
                repo.updateAlarm(updated)
            }
        }
    }

    private fun handleDismissAlarm(context: Context, intent: Intent) {
        val idStr = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val alarmId = try { UUID.fromString(idStr) } catch (_: Exception) { return }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(NOTIFICATION_ID_BASE + alarmId.hashCode())
    }

    private fun handleSnoozeAlarm(context: Context, intent: Intent) {
        val idStr = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val alarmId = try { UUID.fromString(idStr) } catch (_: Exception) { return }
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "Meeting Alarm"
        val targetTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TARGET_TIME) ?: "09:00"
        val zone = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ZONE) ?: ""

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(NOTIFICATION_ID_BASE + alarmId.hashCode())

        // Snooze for 10 minutes
        val snoozeMillis = System.currentTimeMillis() + 10 * 60 * 1000L
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return

        val triggerIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, idStr)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, "$title (Snoozed)")
            putExtra(AlarmScheduler.EXTRA_ALARM_TARGET_TIME, targetTime)
            putExtra(AlarmScheduler.EXTRA_ALARM_ZONE, zone)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode() + 999,
            triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeMillis, pendingIntent)
        } else {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, snoozeMillis, pendingIntent)
        }
    }

    private fun showAlarmNotification(
        context: Context,
        alarmId: UUID,
        title: String,
        targetTime: String,
        zoneDisplay: String
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "SeeTime Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority remote timezone alarms"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(soundUri, audioAttributes)
            }
            nm.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId.toString())
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode() + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId.toString())
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_ALARM_TARGET_TIME, targetTime)
            putExtra(AlarmScheduler.EXTRA_ALARM_ZONE, zoneDisplay)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("Target time $targetTime reached in $zoneDisplay")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze (10m)", snoozePendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID_BASE + alarmId.hashCode(), notification)
    }
}
