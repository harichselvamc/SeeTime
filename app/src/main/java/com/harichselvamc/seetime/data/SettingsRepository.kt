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
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_LAST_OPENED_VERSION_CODE = "last_opened_version_code"


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

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted



    fun setUse24HourFormat(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_USE_24_HOUR, enabled) }
        _use24HourFormat.value = enabled
    }

    fun setShowSeconds(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_SECONDS, enabled) }
        _showSeconds.value = enabled
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
        _onboardingCompleted.value = completed
    }

    fun getLastOpenedVersionCode(): Int {
        return prefs.getInt(KEY_LAST_OPENED_VERSION_CODE, 0)
    }

    fun setLastOpenedVersionCode(versionCode: Int) {
        prefs.edit { putInt(KEY_LAST_OPENED_VERSION_CODE, versionCode) }
    }
}
