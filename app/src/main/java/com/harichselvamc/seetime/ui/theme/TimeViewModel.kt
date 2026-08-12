package com.harichselvamc.seetime.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harichselvamc.seetime.BuildConfig
import com.harichselvamc.seetime.data.SettingsRepository
import com.harichselvamc.seetime.data.TimeRepository
import com.harichselvamc.seetime.data.local.TimePair
import com.harichselvamc.seetime.util.TimeMath

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimePairUi(
    val id: Long,
    val fromZone: String,
    val toZone: String,
    val label: String,
    val displayFromTime: String,   // now includes date + time (12-hour)
    val displayToTime: String,     // now includes date + time (12-hour)
    val diffText: String,
    val dstText: String,
    val offsetDifferenceMinutes: Int
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val pairs: List<TimePairUi> = emptyList(),
    val error: String? = null
)

class TimeViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val TAG = "TimeViewModel"

        private fun logd(msg: String) {
            if (BuildConfig.DEBUG) Log.d(TAG, msg)
        }
    }

    private val repo = TimeRepository.getInstance(app)
    private val settingsRepo = SettingsRepository.getInstance(app)

    val use24HourFormat: StateFlow<Boolean> = settingsRepo.use24HourFormat
    val showSeconds: StateFlow<Boolean> = settingsRepo.showSeconds


    fun setUse24HourFormat(enabled: Boolean) {
        settingsRepo.setUse24HourFormat(enabled)
        refreshDisplayedTimes()
    }

    fun setShowSeconds(enabled: Boolean) {
        settingsRepo.setShowSeconds(enabled)
        refreshDisplayedTimes()
    }



    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private val _timeOffsetMinutes = MutableStateFlow(0)
    val timeOffsetMinutes: StateFlow<Int> = _timeOffsetMinutes

    // to avoid starting multiple tickers
    @Volatile
    private var tickerStarted: Boolean = false

    fun load() {
        viewModelScope.launch {
            logd("load() called")
            _state.value = _state.value.copy(isLoading = true)
            try {
                val pairs = repo.getPairs()
                logd("load() pairs count=${pairs.size}")
                val uiPairs = toUiList(pairs)
                _state.value = HomeUiState(isLoading = false, pairs = uiPairs)

            } catch (e: Exception) {
                Log.e(TAG, "load() failed -> ${e.message}", e)
                _state.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            logd("refreshAll() called")
            _state.value = _state.value.copy(isLoading = true)
            try {
                repo.refreshAllZones()
                val pairs = repo.getPairs()
                val uiPairs = toUiList(pairs)
                _state.value = HomeUiState(isLoading = false, pairs = uiPairs)

            } catch (e: Exception) {
                Log.e(TAG, "refreshAll() failed -> ${e.message}", e)
                _state.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun addPair(fromZone: String, toZone: String, label: String = "") {
        viewModelScope.launch {
            logd("addPair() from=$fromZone to=$toZone label=$label")
            repo.addPair(fromZone, toZone, label)
            repo.refreshAllZones()
            load()
        }
    }

    fun addDummyPair() {
        addPair("Asia/Kolkata", "Europe/London")
    }

    fun movePair(fromIndex: Int, toIndex: Int) {
        val current = _state.value.pairs.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _state.value = _state.value.copy(pairs = current)

        // Persist the new order so it survives reload/restart.
        viewModelScope.launch {
            try {
                repo.reorderPairs(current.map { it.id })
            } catch (e: Exception) {
                Log.e(TAG, "movePair() failed to persist order -> ${e.message}", e)
            }
        }
    }

    fun startTicker() {
        if (tickerStarted) return
        tickerStarted = true

        viewModelScope.launch {
            logd("startTicker() started")
            settingsRepo.showSeconds.collectLatest { secondsEnabled ->
                while (true) {
                    try {
                        // Only tick if we are in live mode (offset is 0)
                        if (_timeOffsetMinutes.value == 0) {
                            tickOnce()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "startTicker() tick failed -> ${e.message}", e)
                    }
                    delay(nextTickerDelayMillis(secondsEnabled))
                }
            }
        }
    }

    fun setTimeOffset(minutes: Int) {
        _timeOffsetMinutes.value = minutes
        viewModelScope.launch {
            tickOnce()
        }
    }

    fun resetToLive() {
        setTimeOffset(0)
    }

    /**
     * Delete by id.
     */
    fun deletePair(id: Long) {
        viewModelScope.launch {
            try {
                logd("deletePair() id=$id")
                repo.deletePairById(id)
                load()
            } catch (e: Exception) {
                Log.e(TAG, "deletePair() failed -> ${e.message}", e)
            }
        }
    }

    /**
     * Edit an existing pair's zones.
     */
    fun editPair(id: Long, fromZone: String, toZone: String, label: String) {
        viewModelScope.launch {
            try {
                logd("editPair() id=$id from=$fromZone to=$toZone label=$label")
                repo.updatePair(id, fromZone, toZone, label)
                repo.refreshAllZones()
                load()
            } catch (e: Exception) {
                Log.e(TAG, "editPair() failed -> ${e.message}", e)
            }
        }
    }

    private suspend fun tickOnce() {
        val nowUtc = System.currentTimeMillis() + (_timeOffsetMinutes.value * 60_000L)
        val current = _state.value.pairs
        if (current.isEmpty()) return

        logd("tickOnce() nowUtc=$nowUtc count=${current.size}")
        val use24Hour = use24HourFormat.value
        val showSecondsEnabled = showSeconds.value

        val refreshed = current.map { ui ->
            val fromCache = repo.getZoneCache(ui.fromZone)
            val toCache = repo.getZoneCache(ui.toZone)

            val diffMin = if (toCache != null && fromCache != null) toCache.offsetMinutes - fromCache.offsetMinutes else 0
            ui.copy(
                displayFromTime = TimeMath.formatDateTime(
                    nowUtc,
                    fromCache,
                    use24Hour,
                    showSecondsEnabled
                ),
                displayToTime = TimeMath.formatDateTime(
                    nowUtc,
                    toCache,
                    use24Hour,
                    showSecondsEnabled
                ),
                diffText = TimeMath.buildDiffText(fromCache, toCache),
                dstText = TimeMath.buildDstText(fromCache, toCache),
                offsetDifferenceMinutes = diffMin
            )
        }

        _state.value = _state.value.copy(pairs = refreshed)
    }

    private fun refreshDisplayedTimes() {
        viewModelScope.launch {
            try {
                tickOnce()

            } catch (e: Exception) {
                Log.e(TAG, "refreshDisplayedTimes() failed -> ${e.message}", e)
            }
        }
    }

    private fun nextTickerDelayMillis(secondsEnabled: Boolean): Long {
        if (secondsEnabled) return 1000L
        val now = System.currentTimeMillis()
        val millisPastMinute = now % 60_000L
        return (60_000L - millisPastMinute).coerceIn(1000L, 60_000L)
    }

    /* ------------ Helpers ------------ */

    private suspend fun toUiList(pairs: List<TimePair>): List<TimePairUi> {
        val nowUtc = System.currentTimeMillis() + (_timeOffsetMinutes.value * 60_000L)
        logd("toUiList() nowUtc=$nowUtc")
        val use24Hour = use24HourFormat.value
        val showSecondsEnabled = showSeconds.value

        return pairs.map { pair ->
            val fromCache = repo.getZoneCache(pair.fromZone)
            val toCache = repo.getZoneCache(pair.toZone)

            logd(
                "toUiList() pair id=${pair.id} ${pair.fromZone} -> ${pair.toZone}, " +
                        "fromCache=${fromCache?.offsetMinutes}, toCache=${toCache?.offsetMinutes}"
            )

            val diffMin = if (toCache != null && fromCache != null) toCache.offsetMinutes - fromCache.offsetMinutes else 0
            TimePairUi(
                id = pair.id,
                fromZone = pair.fromZone,
                toZone = pair.toZone,
                label = pair.label,
                displayFromTime = TimeMath.formatDateTime(
                    nowUtc,
                    fromCache,
                    use24Hour,
                    showSecondsEnabled
                ),
                displayToTime = TimeMath.formatDateTime(
                    nowUtc,
                    toCache,
                    use24Hour,
                    showSecondsEnabled
                ),
                diffText = TimeMath.buildDiffText(fromCache, toCache),
                dstText = TimeMath.buildDstText(fromCache, toCache),
                offsetDifferenceMinutes = diffMin
            )
        }
    }

}
