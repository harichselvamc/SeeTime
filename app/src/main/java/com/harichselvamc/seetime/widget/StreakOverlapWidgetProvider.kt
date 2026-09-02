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

class StreakOverlapWidgetProvider : AppWidgetProvider() {

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

            val data = WidgetDataEngine.computeStreakWidgetData(streak, pairs, now)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_streak_overlap)

                // 1. Streak count & flame
                views.setTextViewText(R.id.widget_streak_count, "${data.currentStreak} Days")
                views.setTextViewText(R.id.widget_freeze_badge, data.badgeLabel)

                // 2. Overlap Card
                views.setTextViewText(R.id.widget_overlap_headline, data.overlapHeadline)
                views.setTextViewText(R.id.widget_overlap_subtext, data.overlapSubtext)

                // 3. Footer text
                val footerText = if (data.isCompletedToday) "Today's goal completed!" else "Tap to log today's time"
                views.setTextViewText(R.id.widget_action_footer, footerText)

                // 4. Click Intent -> Open MainActivity
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
                views.setOnClickPendingIntent(R.id.widget_streak_overlap_root, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, StreakOverlapWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, StreakOverlapWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
