package com.harichselvamc.seetime.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harichselvamc.seetime.data.TimeRepository
import com.harichselvamc.seetime.data.local.TimePair
import com.harichselvamc.seetime.data.local.ZoneCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class TimePairUi(
    val id: Long,
    val fromZone: String,
    val toZone: String,
    val displayFromTime: String,   // now includes date + time (12-hour)
    val displayToTime: String,     // now includes date + time (12-hour)
    val diffText: String,
    val dstText: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val pairs: List<TimePairUi> = emptyList(),
    val error: String? = null
)

class TimeViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val TAG = "TimeViewModel"
    }

    private val repo = TimeRepository.getInstance(app)

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    // to avoid starting multiple tickers
    @Volatile
    private var tickerStarted: Boolean = false

    fun load() {
        viewModelScope.launch {
            Log.d(TAG, "load() called")
            _state.value = _state.value.copy(isLoading = true)
            try {
                val pairs = repo.getPairs()
                Log.d(TAG, "load() pairs count=${pairs.size}")
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
            Log.d(TAG, "refreshAll() called")
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

    fun addPair(fromZone: String, toZone: String) {
        viewModelScope.launch {
            Log.d(TAG, "addPair() from=$fromZone to=$toZone")
            repo.addPair(fromZone, toZone)
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
    }

    fun startTicker() {
        if (tickerStarted) return
        tickerStarted = true

        viewModelScope.launch {
            Log.d(TAG, "startTicker() started")
            while (true) {
                try {
                    tickOnce()
                } catch (e: Exception) {
                    Log.e(TAG, "startTicker() tick failed -> ${e.message}", e)
                }
                delay(1000L)
            }
        }
    }

    /**
     * Delete by id (used by swipe-to-delete).
     */
    fun deletePair(id: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "deletePair() id=$id")
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
    fun editPair(id: Long, fromZone: String, toZone: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "editPair() id=$id from=$fromZone to=$toZone")
                repo.updatePair(id, fromZone, toZone)
                repo.refreshAllZones()
                load()
            } catch (e: Exception) {
                Log.e(TAG, "editPair() failed -> ${e.message}", e)
            }
        }
    }

    private suspend fun tickOnce() {
        val nowUtc = System.currentTimeMillis()
        val current = _state.value.pairs
        if (current.isEmpty()) return

        Log.d(TAG, "tickOnce() nowUtc=$nowUtc count=${current.size}")

        val refreshed = current.map { ui ->
            val fromCache = repo.getZoneCache(ui.fromZone)
            val toCache = repo.getZoneCache(ui.toZone)

            ui.copy(
                displayFromTime = formatDateTime(nowUtc, fromCache),
                displayToTime = formatDateTime(nowUtc, toCache),
                diffText = buildDiffText(fromCache, toCache),
                dstText = buildDstText(fromCache, toCache)
            )
        }

        _state.value = _state.value.copy(pairs = refreshed)
    }

    /* ------------ Helpers ------------ */

    private suspend fun toUiList(pairs: List<TimePair>): List<TimePairUi> {
        val nowUtc = System.currentTimeMillis()
        Log.d(TAG, "toUiList() nowUtc=$nowUtc")

        return pairs.map { pair ->
            val fromCache = repo.getZoneCache(pair.fromZone)
            val toCache = repo.getZoneCache(pair.toZone)

            Log.d(
                TAG,
                "toUiList() pair id=${pair.id} ${pair.fromZone} -> ${pair.toZone}, " +
                        "fromCache=${fromCache?.offsetMinutes}, toCache=${toCache?.offsetMinutes}"
            )

            TimePairUi(
                id = pair.id,
                fromZone = pair.fromZone,
                toZone = pair.toZone,
                displayFromTime = formatDateTime(nowUtc, fromCache),
                displayToTime = formatDateTime(nowUtc, toCache),
                diffText = buildDiffText(fromCache, toCache),
                dstText = buildDstText(fromCache, toCache)
            )
        }
    }

    /**
     * Format **date + time** in 12-hour format:
     * "dd MMM yyyy, hh:mm:ss AM/PM"
     * Example: "18 Nov 2025, 06:01:32 PM"
     */
    private fun formatDateTime(nowUtc: Long, cache: ZoneCache?): String {
        if (cache == null) {
            Log.d(TAG, "formatDateTime() cache=null -> returning '--'")
            return "--"
        }

        val millis = nowUtc + cache.offsetMinutes * 60_000L
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis

        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) // 0–11
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        val hour24 = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val second = cal.get(java.util.Calendar.SECOND)

        val (hour12, amPm) = when {
            hour24 == 0 -> 12 to "AM"          // 00:xx -> 12 AM
            hour24 < 12 -> hour24 to "AM"      // 01–11 -> AM
            hour24 == 12 -> 12 to "PM"         // 12:xx -> 12 PM
            else -> (hour24 - 12) to "PM"      // 13–23 -> 1–11 PM
        }

        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames[month.coerceIn(0, 11)]

        val result = "%02d %s %04d, %02d:%02d:%02d %s".format(
            day,
            monthName,
            year,
            hour12,
            minute,
            second,
            amPm
        )

        Log.d(
            TAG,
            "formatDateTime() offset=${cache.offsetMinutes} nowUtc=$nowUtc -> $result"
        )
        return result
    }

    /**
     * Show time difference including DST as "+H:MM hrs" or "-H:MM hrs".
     * Example: "Time difference: +1:30 hrs"
     */
    private fun buildDiffText(from: ZoneCache?, to: ZoneCache?): String {
        if (from == null || to == null) {
            Log.d(TAG, "buildDiffText() missing cache -> from=$from to=$to")
            return "Time difference: ?"
        }
        // offsetMinutes already includes DST if active
        val diff = to.offsetMinutes - from.offsetMinutes
        val sign = if (diff >= 0) "+" else "-"
        val absMin = abs(diff)
        val h = absMin / 60
        val m = absMin % 60
        val text = "Time difference: $sign$h:${m.toString().padStart(2, '0')} hrs"
        Log.d(TAG, "buildDiffText() from=${from.offsetMinutes} to=${to.offsetMinutes} -> $text")
        return text
    }

    /**
     * Show DST status for each side.
     * We **don't** use the full offset (which gave you +11:00 hrs),
     * instead we assume a typical DST shift of +1:00 hr when active.
     *
     * Example:
     *   "From DST: active (+1:00 hrs) | To DST: inactive"
     */
    private fun buildDstText(from: ZoneCache?, to: ZoneCache?): String {

        fun formatSide(label: String, cache: ZoneCache?): String {
            if (cache == null) return "$label DST: ?"

            return when (cache.dstActive) {
                true -> "$label DST: active (+1:00 hrs)"   // typical DST shift
                false -> "$label DST: inactive"
                null -> "$label DST: ?"
            }
        }

        val fromPart = formatSide("From", from)
        val toPart = formatSide("To", to)

        val result = "$fromPart | $toPart"
        Log.d(
            TAG,
            "buildDstText() fromDst=${from?.dstActive} toDst=${to?.dstActive} -> $result"
        )
        return result
    }
}
