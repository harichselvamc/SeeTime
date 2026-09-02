package com.harichselvamc.seetime.widget

import android.content.Context

object SeeTimeWidgetManager {

    fun updateAllWidgets(context: Context) {
        runCatching {
            StreakOverlapWidgetProvider.updateAll(context)
            QuickLogWidgetProvider.updateAll(context)
            CommandCenterWidgetProvider.updateAll(context)
        }
    }
}
