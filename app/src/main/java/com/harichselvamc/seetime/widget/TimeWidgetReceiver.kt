package com.harichselvamc.seetime.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimeWidget()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WidgetUpdater.ACTION_REFRESH) {
            launchWidgetUpdate(context, updateEvenIfNoPinnedWidget = false)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        launchWidgetUpdate(context, updateEvenIfNoPinnedWidget = true)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdater.cancelScheduledRefresh(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        launchWidgetUpdate(context, appWidgetIds)
    }

    private fun launchWidgetUpdate(context: Context, updateEvenIfNoPinnedWidget: Boolean) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                WidgetUpdater.requestUpdate(
                    context = context.applicationContext,
                    updateEvenIfNoPinnedWidget = updateEvenIfNoPinnedWidget
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun launchWidgetUpdate(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                WidgetUpdater.requestUpdate(
                    context = context.applicationContext,
                    appWidgetIds = appWidgetIds
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
