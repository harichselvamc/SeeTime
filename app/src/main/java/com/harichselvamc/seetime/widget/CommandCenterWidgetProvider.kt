package com.harichselvamc.seetime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.harichselvamc.seetime.EXTRA_SHORTCUT_ACTION
import com.harichselvamc.seetime.MainActivity
import com.harichselvamc.seetime.R
import com.harichselvamc.seetime.SHORTCUT_ACTION_QUICK_ADD
import com.harichselvamc.seetime.data.GamificationRepository
import com.harichselvamc.seetime.data.TimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class CommandCenterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val gamificationRepo = GamificationRepository.getInstance(context)
            val timeRepo = TimeRepository.getInstance(context)

            val streak = gamificationRepo.streakState.value
            val pairs = runCatching { timeRepo.getPairs() }.getOrDefault(emptyList())
            val now = ZonedDateTime.now()

            val data = WidgetDataEngine.computeCommandCenterWidgetData(streak, pairs, now)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_command_center)

                // 1. Local clock
                views.setTextViewText(R.id.widget_local_city, data.localCity)
                views.setTextViewText(R.id.widget_local_time, data.localTimeFormatted)
                views.setImageViewResource(
                    R.id.widget_local_sun_moon,
                    if (data.localIsDaytime) R.drawable.ic_widget_sun else R.drawable.ic_widget_moon
                )

                // 2. Remote clock
                views.setTextViewText(R.id.widget_remote_city, data.remoteCity)
                views.setTextViewText(R.id.widget_remote_time, data.remoteTimeFormatted)
                views.setImageViewResource(
                    R.id.widget_remote_sun_moon,
                    if (data.remoteIsDaytime) R.drawable.ic_widget_sun else R.drawable.ic_widget_moon
                )

                // 3. Streak
                views.setTextViewText(R.id.widget_command_streak, "${data.currentStreak}d")

                // 4. Overlap & Time difference
                views.setTextViewText(R.id.widget_command_overlap_status, data.overlapHeadline)
                views.setTextViewText(R.id.widget_command_time_diff, data.timeDiffFormatted)

                // 5. Main click intent
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val mainPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_command_center_root, mainPendingIntent)

                // 6. Quick add button click intent
                val addIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_SHORTCUT_ACTION, SHORTCUT_ACTION_QUICK_ADD)
                }
                val addPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId + 1000,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_add_btn, addPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CommandCenterWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, CommandCenterWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
