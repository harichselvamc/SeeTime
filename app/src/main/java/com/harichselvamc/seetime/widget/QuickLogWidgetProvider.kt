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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class QuickLogWidgetProvider : AppWidgetProvider() {

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
            val streak = gamificationRepo.streakState.value
            val now = ZonedDateTime.now()

            val data = WidgetDataEngine.computeQuickLogWidgetData(streak, now)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_quick_log)

                // 1. Streak count & action text
                views.setTextViewText(R.id.widget_quick_streak_text, data.streakText)
                views.setTextViewText(R.id.widget_quick_log_label, data.actionText)

                // 2. Click Intent -> Open Add Activity Dialog in MainActivity
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_SHORTCUT_ACTION, SHORTCUT_ACTION_QUICK_ADD)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_quick_log_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuickLogWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, QuickLogWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
