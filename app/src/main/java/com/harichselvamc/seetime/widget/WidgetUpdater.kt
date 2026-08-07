package com.harichselvamc.seetime.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.harichselvamc.seetime.BuildConfig

/**
 * Pushes an immediate refresh of the home-screen widget after the data it
 * shows changes (add/edit/delete/reorder/refresh), instead of waiting for
 * the OS's periodic update. Safe to call even if no widget is pinned.
 */
object WidgetUpdater {
    private const val TAG = "WidgetUpdater"

    suspend fun requestUpdate(context: Context) {
        try {
            TimeWidget().updateAll(context)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "requestUpdate() failed -> ${e.message}", e)
        }
    }
}
