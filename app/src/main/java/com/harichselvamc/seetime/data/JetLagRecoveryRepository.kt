package com.harichselvamc.seetime.data

import android.content.Context
import android.content.SharedPreferences
import com.harichselvamc.seetime.util.ChronoNutritionEngine
import com.harichselvamc.seetime.util.JetLagChronoPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class JetLagUserState(
    val originZoneId: String = "America/New_York",
    val destZoneId: String = "Asia/Tokyo",
    val currentDayIndex: Int = 0,
    val loggedWaterMl: Int = 0,
    val completedMealsMask: Int = 0 // Bitmask for breakfast, lunch, dinner
)

class JetLagRecoveryRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("seetime_jetlag_recovery", Context.MODE_PRIVATE)

    private val _userState = MutableStateFlow(JetLagUserState())
    val userState: StateFlow<JetLagUserState> = _userState.asStateFlow()

    private val _plans = MutableStateFlow<List<JetLagChronoPlan>>(emptyList())
    val plans: StateFlow<List<JetLagChronoPlan>> = _plans.asStateFlow()

    companion object {
        val POPULAR_ORIGINS = listOf(
            "America/New_York",
            "America/Los_Angeles",
            "America/Chicago",
            "Europe/London",
            "Europe/Paris",
            "Asia/Dubai",
            "Asia/Tokyo",
            "Asia/Singapore",
            "Australia/Sydney"
        )

        val POPULAR_DESTINATIONS = listOf(
            "Asia/Tokyo",
            "Europe/London",
            "Europe/Paris",
            "America/New_York",
            "America/Los_Angeles",
            "Asia/Singapore",
            "Asia/Dubai",
            "Australia/Sydney",
            "Asia/Kolkata"
        )
    }

    init {
        loadFromStorage()
        recalculatePlans()
    }

    fun setOriginZone(zoneId: String) {
        _userState.value = _userState.value.copy(originZoneId = zoneId)
        recalculatePlans()
        saveToStorage()
    }

    fun setDestZone(zoneId: String) {
        _userState.value = _userState.value.copy(destZoneId = zoneId)
        recalculatePlans()
        saveToStorage()
    }

    fun selectDayIndex(dayIndex: Int) {
        _userState.value = _userState.value.copy(currentDayIndex = dayIndex.coerceIn(0, 4))
        saveToStorage()
    }

    fun logWaterIntake(amountMl: Int) {
        val current = _userState.value.loggedWaterMl
        _userState.value = _userState.value.copy(loggedWaterMl = (current + amountMl).coerceAtLeast(0))
        saveToStorage()
    }

    fun resetWaterIntake() {
        _userState.value = _userState.value.copy(loggedWaterMl = 0)
        saveToStorage()
    }

    fun toggleMealCompleted(mealIndex: Int) {
        val currentMask = _userState.value.completedMealsMask
        val bit = 1 shl mealIndex
        val newMask = currentMask xor bit
        _userState.value = _userState.value.copy(completedMealsMask = newMask)
        saveToStorage()
    }

    fun isMealCompleted(mealIndex: Int): Boolean {
        val bit = 1 shl mealIndex
        return (_userState.value.completedMealsMask and bit) != 0
    }

    private fun recalculatePlans() {
        val origin = _userState.value.originZoneId
        val dest = _userState.value.destZoneId
        _plans.value = ChronoNutritionEngine.generateRecoveryPlan(origin, dest)
    }

    private fun saveToStorage() {
        val sp = prefs ?: return
        try {
            val state = _userState.value
            val json = JSONObject().apply {
                put("origin", state.originZoneId)
                put("dest", state.destZoneId)
                put("dayIndex", state.currentDayIndex)
                put("waterMl", state.loggedWaterMl)
                put("mealsMask", state.completedMealsMask)
            }
            sp.edit().putString("state", json.toString()).apply()
        } catch (_: Exception) {
            // Ignore storage errors
        }
    }

    private fun loadFromStorage() {
        val sp = prefs ?: return
        try {
            val raw = sp.getString("state", null) ?: return
            val json = JSONObject(raw)
            _userState.value = JetLagUserState(
                originZoneId = json.optString("origin", "America/New_York"),
                destZoneId = json.optString("dest", "Asia/Tokyo"),
                currentDayIndex = json.optInt("dayIndex", 0),
                loggedWaterMl = json.optInt("waterMl", 0),
                completedMealsMask = json.optInt("mealsMask", 0)
            )
        } catch (_: Exception) {
            // Default
        }
    }
}
