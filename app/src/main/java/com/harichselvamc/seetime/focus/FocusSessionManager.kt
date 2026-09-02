package com.harichselvamc.seetime.focus

import android.content.Context
import com.harichselvamc.seetime.data.GamificationAction
import com.harichselvamc.seetime.data.GamificationRepository
import com.harichselvamc.seetime.data.TimeRepository
import com.harichselvamc.seetime.data.local.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FocusMode(val defaultMinutes: Int, val title: String) {
    WORK(25, "Deep Focus"),
    SHORT_BREAK(5, "Short Break"),
    LONG_BREAK(15, "Restorative Break")
}

enum class FocusStatus {
    STOPPED,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class FocusSessionState(
    val mode: FocusMode = FocusMode.WORK,
    val status: FocusStatus = FocusStatus.STOPPED,
    val secondsRemaining: Int = FocusMode.WORK.defaultMinutes * 60,
    val totalSeconds: Int = FocusMode.WORK.defaultMinutes * 60,
    val completedSessions: Int = 0,
    val sessionsBeforeLongBreak: Int = 4,
    val currentSessionStartTimeMillis: Long = 0L,
    val currentTaskLabel: String = "Deep Work Focus"
) {
    val progress: Float
        get() = if (totalSeconds > 0) {
            ((totalSeconds - secondsRemaining).toFloat() / totalSeconds).coerceIn(0f, 1f)
        } else 0f

    val formattedTime: String
        get() {
            val minutes = secondsRemaining / 60
            val seconds = secondsRemaining % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

class FocusSessionManager(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val _state = MutableStateFlow(FocusSessionState())
    val state: StateFlow<FocusSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null

    companion object {
        @Volatile
        private var INSTANCE: FocusSessionManager? = null

        fun getInstance(context: Context): FocusSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FocusSessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun createForTesting(): FocusSessionManager {
            return FocusSessionManager(null, CoroutineScope(Dispatchers.Unconfined))
        }
    }

    fun setMode(mode: FocusMode, minutes: Int = mode.defaultMinutes) {
        pause()
        val totalSec = minutes * 60
        _state.value = _state.value.copy(
            mode = mode,
            status = FocusStatus.STOPPED,
            secondsRemaining = totalSec,
            totalSeconds = totalSec
        )
    }

    fun setTaskLabel(label: String) {
        _state.value = _state.value.copy(currentTaskLabel = label)
    }

    fun start() {
        if (_state.value.status == FocusStatus.RUNNING) return

        val startTime = if (_state.value.status == FocusStatus.STOPPED) {
            System.currentTimeMillis()
        } else {
            _state.value.currentSessionStartTimeMillis
        }

        _state.value = _state.value.copy(
            status = FocusStatus.RUNNING,
            currentSessionStartTimeMillis = startTime
        )

        timerJob?.cancel()
        timerJob = scope.launch {
            while (_state.value.status == FocusStatus.RUNNING && _state.value.secondsRemaining > 0) {
                delay(1000L)
                tick()
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        if (_state.value.status == FocusStatus.RUNNING) {
            _state.value = _state.value.copy(status = FocusStatus.PAUSED)
        }
    }

    fun resume() {
        if (_state.value.status == FocusStatus.PAUSED) {
            start()
        }
    }

    fun reset() {
        timerJob?.cancel()
        val totalSec = _state.value.mode.defaultMinutes * 60
        _state.value = _state.value.copy(
            status = FocusStatus.STOPPED,
            secondsRemaining = totalSec,
            totalSeconds = totalSec
        )
    }

    fun skip() {
        timerJob?.cancel()
        handleSessionCompletion()
    }

    /**
     * Pure internal step advancing the timer by 1 second.
     */
    fun tick() {
        val currentSec = _state.value.secondsRemaining
        if (currentSec > 1) {
            _state.value = _state.value.copy(secondsRemaining = currentSec - 1)
        } else {
            _state.value = _state.value.copy(secondsRemaining = 0)
            handleSessionCompletion()
        }
    }

    private fun handleSessionCompletion() {
        timerJob?.cancel()
        val completedMode = _state.value.mode
        val completedSessions = if (completedMode == FocusMode.WORK) {
            _state.value.completedSessions + 1
        } else {
            _state.value.completedSessions
        }

        // Record completed work session to Room database & gamification
        if (completedMode == FocusMode.WORK && context != null) {
            val endTime = System.currentTimeMillis()
            val startTime = _state.value.currentSessionStartTimeMillis.takeIf { it > 0 }
                ?: (endTime - (_state.value.totalSeconds * 1000L))

            scope.launch(Dispatchers.IO) {
                try {
                    TimeRepository.getInstance(context).addActivity(
                        label = _state.value.currentTaskLabel,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        category = "Deep Work"
                    )
                    GamificationRepository.getInstance(context).recordAction(GamificationAction.LOG_ACTIVITY)
                } catch (_: Exception) {}
            }
        }

        // Determine next mode
        val nextMode = when (completedMode) {
            FocusMode.WORK -> {
                if (completedSessions % _state.value.sessionsBeforeLongBreak == 0) {
                    FocusMode.LONG_BREAK
                } else {
                    FocusMode.SHORT_BREAK
                }
            }
            FocusMode.SHORT_BREAK, FocusMode.LONG_BREAK -> FocusMode.WORK
        }

        val nextTotalSec = nextMode.defaultMinutes * 60
        _state.value = _state.value.copy(
            mode = nextMode,
            status = FocusStatus.COMPLETED,
            completedSessions = completedSessions,
            secondsRemaining = nextTotalSec,
            totalSeconds = nextTotalSec,
            currentSessionStartTimeMillis = 0L
        )
    }
}
