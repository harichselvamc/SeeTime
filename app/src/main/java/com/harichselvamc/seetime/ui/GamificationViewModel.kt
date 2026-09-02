package com.harichselvamc.seetime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harichselvamc.seetime.data.GamificationAction
import com.harichselvamc.seetime.data.GamificationRepository
import com.harichselvamc.seetime.data.StreakEvaluationResult
import com.harichselvamc.seetime.data.local.BadgeEntity
import com.harichselvamc.seetime.data.local.StreakEntity
import com.harichselvamc.seetime.util.AppIconScenario
import com.harichselvamc.seetime.util.DynamicIconManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GamificationUiState(
    val streak: StreakEntity = StreakEntity(),
    val badges: List<BadgeEntity> = emptyList(),
    val unlockedBadgeDialog: BadgeEntity? = null,
    val dailyProgress: Float = 0f,
    val activeIconScenario: AppIconScenario = AppIconScenario.DEFAULT
)

class GamificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamificationRepository.getInstance(application)

    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    val streak: StateFlow<StreakEntity> = repository.streakState
    val badges: StateFlow<List<BadgeEntity>> = repository.badgesState

    val dailyProgress: StateFlow<Float> = streak.map { s ->
        if (s.dailyTargetActions > 0) {
            (s.dailyActionsCount.toFloat() / s.dailyTargetActions).coerceIn(0f, 1f)
        } else {
            0f
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    init {
        viewModelScope.launch {
            repository.evaluateDailyActivity()
        }

        viewModelScope.launch {
            repository.streakState.collect { streakEntity ->
                val progress = if (streakEntity.dailyTargetActions > 0) {
                    (streakEntity.dailyActionsCount.toFloat() / streakEntity.dailyTargetActions).coerceIn(0f, 1f)
                } else 0f
                val activeScenario = DynamicIconManager.syncIconWithGamificationState(
                    context = getApplication(),
                    streak = streakEntity,
                    badges = _uiState.value.badges
                )
                _uiState.value = _uiState.value.copy(
                    streak = streakEntity,
                    dailyProgress = progress,
                    activeIconScenario = activeScenario
                )
            }
        }

        viewModelScope.launch {
            repository.badgesState.collect { badgeList ->
                val activeScenario = DynamicIconManager.syncIconWithGamificationState(
                    context = getApplication(),
                    streak = _uiState.value.streak,
                    badges = badgeList
                )
                _uiState.value = _uiState.value.copy(
                    badges = badgeList,
                    activeIconScenario = activeScenario
                )
            }
        }
    }

    fun setAppIconScenario(scenario: AppIconScenario) {
        DynamicIconManager.setIcon(getApplication(), scenario)
        _uiState.value = _uiState.value.copy(activeIconScenario = scenario)
    }

    fun recordAction(action: GamificationAction) {
        viewModelScope.launch {
            val newlyUnlocked = repository.recordAction(action)
            if (newlyUnlocked.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(unlockedBadgeDialog = newlyUnlocked.first())
            }
        }
    }

    fun addStreakFreeze() {
        viewModelScope.launch {
            repository.addStreakFreeze(1)
        }
    }

    fun dismissBadgeDialog() {
        _uiState.value = _uiState.value.copy(unlockedBadgeDialog = null)
    }
}
