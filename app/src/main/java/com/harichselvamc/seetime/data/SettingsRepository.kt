package com.harichselvamc.seetime.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Small local-only settings store (SharedPreferences-backed). No network,
 * no analytics — just the user's display preferences.
 */
class SettingsRepository private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "see_time_settings"
        private const val KEY_USE_24_HOUR = "use_24_hour_format"
        private const val KEY_SHOW_SECONDS = "show_seconds"
        private const val KEY_SHOW_EXTRA_WIDGET_PAIRS = "show_extra_widget_pairs"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _use24HourFormat = MutableStateFlow(prefs.getBoolean(KEY_USE_24_HOUR, false))
    val use24HourFormat: StateFlow<Boolean> = _use24HourFormat

    private val _showSeconds = MutableStateFlow(prefs.getBoolean(KEY_SHOW_SECONDS, true))
    val showSeconds: StateFlow<Boolean> = _showSeconds

    private val _showExtraWidgetPairs =
        MutableStateFlow(prefs.getBoolean(KEY_SHOW_EXTRA_WIDGET_PAIRS, true))
    val showExtraWidgetPairs: StateFlow<Boolean> = _showExtraWidgetPairs

    fun setUse24HourFormat(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_USE_24_HOUR, enabled) }
        _use24HourFormat.value = enabled
    }

    fun setShowSeconds(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_SECONDS, enabled) }
        _showSeconds.value = enabled
    }

    fun setShowExtraWidgetPairs(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_EXTRA_WIDGET_PAIRS, enabled) }
        _showExtraWidgetPairs.value = enabled
    }
}
