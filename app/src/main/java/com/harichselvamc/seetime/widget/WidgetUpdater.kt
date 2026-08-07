package com.harichselvamc.seetime.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.harichselvamc.seetime.BuildConfig

/**
 * Pushes an immediate refresh of the home-screen widget after the data it
 * shows changes (add/edit/delete/reorder/refresh), instead of waiting for
 * the OS's periodic update. Safe to call even if no widget is pinned.
 */
object WidgetUpdater {
    private const val TAG = "WidgetUpdater"
    const val ACTION_REFRESH = "com.harichselvamc.seetime.widget.ACTION_REFRESH"

    suspend fun requestUpdate(context: Context, updateEvenIfNoPinnedWidget: Boolean = false) {
        try {
            val appContext = context.applicationContext
            val widgetIds = GlanceAppWidgetManager(appContext).getGlanceIds(TimeWidget::class.java)
            if (widgetIds.isEmpty() && !updateEvenIfNoPinnedWidget) {
                cancelScheduledRefresh(appContext)
            } else {
                TimeWidget().updateAll(appContext)
                scheduleNextMinuteRefresh(appContext)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "requestUpdate() failed -> ${e.message}", e)
        }
    }

    suspend fun requestUpdate(context: Context, appWidgetIds: IntArray) {
        try {
            val appContext = context.applicationContext
            val manager = GlanceAppWidgetManager(appContext)
            val widget = TimeWidget()

            appWidgetIds.forEach { appWidgetId ->
                widget.update(appContext, manager.getGlanceIdBy(appWidgetId))
            }

            if (appWidgetIds.isEmpty()) {
                requestUpdate(appContext, updateEvenIfNoPinnedWidget = true)
            } else {
                scheduleNextMinuteRefresh(appContext)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "requestUpdate(ids) failed -> ${e.message}", e)
        }
    }

    fun scheduleNextMinuteRefresh(context: Context) {
        try {
            val appContext = context.applicationContext
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val now = System.currentTimeMillis()
            val delayMillis = (60_250L - (now % 60_000L)).coerceIn(1000L, 60_250L)
            val triggerAtMillis = SystemClock.elapsedRealtime() + delayMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    refreshPendingIntent(appContext)
                )
            } else {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    refreshPendingIntent(appContext)
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "scheduleNextMinuteRefresh() failed -> ${e.message}", e)
        }
    }

    fun cancelScheduledRefresh(context: Context) {
        try {
            val appContext = context.applicationContext
            val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(refreshPendingIntent(appContext))
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "cancelScheduledRefresh() failed -> ${e.message}", e)
        }
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TimeWidgetReceiver::class.java).apply {
            action = ACTION_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
