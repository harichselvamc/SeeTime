package com.harichselvamc.seetime.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TimeReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        // Tag every reminder work request with this so we can query them
        const val TAG = "seetime_reminder"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_ZONE = "zone"
        const val KEY_TARGET_TIME = "target_time"   // "HH:mm" string for display
        const val KEY_SCHEDULED_AT = "scheduled_at" // epochMillis when it was created
    }

    override suspend fun doWork(): Result {
        val title   = inputData.getString(KEY_TITLE)   ?: "SeeTime Reminder"
        val message = inputData.getString(KEY_MESSAGE) ?: "Target timezone milestone reached."
        showNotification(applicationContext, title, message)
        return Result.success()
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "seetime_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SeeTime Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for timezone milestones and smart reminders"
            }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
