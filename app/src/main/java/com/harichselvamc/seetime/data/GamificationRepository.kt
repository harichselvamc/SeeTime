package com.harichselvamc.seetime.data

import android.content.Context
import com.harichselvamc.seetime.data.local.AppDatabase
import com.harichselvamc.seetime.data.local.BadgeEntity
import com.harichselvamc.seetime.data.local.GamificationDao
import com.harichselvamc.seetime.data.local.StreakEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

enum class GamificationAction {
    OPEN_APP,
    ADD_TIME_PAIR,
    TIME_TRAVEL,
    CHECK_OVERLAP,
    LOG_ACTIVITY
}

data class StreakEvaluationResult(
    val previousStreak: Int,
    val newStreak: Int,
    val isIncremented: Boolean,
    val isFreezeUsed: Boolean,
    val isBroken: Boolean,
    val unlockedBadges: List<BadgeEntity> = emptyList()
)

class GamificationRepository private constructor(
    private val dao: GamificationDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _streakState = MutableStateFlow(StreakEntity())
    val streakState: StateFlow<StreakEntity> = _streakState.asStateFlow()

    private val _badgesState = MutableStateFlow<List<BadgeEntity>>(emptyList())
    val badgesState: StateFlow<List<BadgeEntity>> = _badgesState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: GamificationRepository? = null

        fun getInstance(context: Context): GamificationRepository {
            return INSTANCE ?: synchronized(this) {
                val db = TimeRepository.getInstance(context).database
                INSTANCE ?: GamificationRepository(db.gamificationDao()).also {
                    INSTANCE = it
                    it.initialize()
                }
            }
        }

        fun createForTesting(dao: GamificationDao, scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)): GamificationRepository {
            return GamificationRepository(dao, scope)
        }

        val DEFAULT_BADGES = listOf(
            BadgeEntity(
                id = "first_timezone_added",
                title = "Global Connector",
                description = "Add your first timezone pair",
                iconName = "language",
                category = "General",
                isUnlocked = false,
                progress = 0,
                maxProgress = 1
            ),
            BadgeEntity(
                id = "streak_3_days",
                title = "3-Day Flame",
                description = "Maintain an active 3-day streak",
                iconName = "local_fire_department",
                category = "Streak",
                isUnlocked = false,
                progress = 0,
                maxProgress = 3
            ),
            BadgeEntity(
                id = "streak_7_days",
                title = "Week Champion",
                description = "Maintain an active 7-day streak",
                iconName = "emoji_events",
                category = "Streak",
                isUnlocked = false,
                progress = 0,
                maxProgress = 7
            ),
            BadgeEntity(
                id = "streak_30_days",
                title = "Time Master",
                description = "Maintain an active 30-day streak",
                iconName = "military_tech",
                category = "Streak",
                isUnlocked = false,
                progress = 0,
                maxProgress = 30
            ),
            BadgeEntity(
                id = "time_traveler",
                title = "Time Traveler",
                description = "Use the interactive time slider 5 times",
                iconName = "flight",
                category = "Feature",
                isUnlocked = false,
                progress = 0,
                maxProgress = 5
            ),
            BadgeEntity(
                id = "meeting_pro",
                title = "Master Coordinator",
                description = "Schedule or view meeting overlaps 3 times",
                iconName = "groups",
                category = "Feature",
                isUnlocked = false,
                progress = 0,
                maxProgress = 3
            ),
            BadgeEntity(
                id = "night_owl",
                title = "Night Owl",
                description = "Check international timezones between 10 PM and 4 AM",
                iconName = "dark_mode",
                category = "Discovery",
                isUnlocked = false,
                progress = 0,
                maxProgress = 1
            ),
            BadgeEntity(
                id = "early_bird",
                title = "Early Bird",
                description = "Check international timezones between 5 AM and 8 AM",
                iconName = "light_mode",
                category = "Discovery",
                isUnlocked = false,
                progress = 0,
                maxProgress = 1
            ),
            BadgeEntity(
                id = "activity_tracker",
                title = "Focus Logger",
                description = "Log 5 daily productivity activities",
                iconName = "timer",
                category = "Feature",
                isUnlocked = false,
                progress = 0,
                maxProgress = 5
            )
        )
    }

    fun initialize() {
        scope.launch {
            seedBadgesIfEmpty()
            refreshData()
        }
    }

    suspend fun seedBadgesIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getBadgeCount() == 0) {
            dao.upsertBadges(DEFAULT_BADGES)
        }
        val streak = dao.getStreak()
        if (streak == null) {
            dao.upsertStreak(StreakEntity())
        }
    }

    suspend fun refreshData() = withContext(Dispatchers.IO) {
        val streak = dao.getStreak() ?: StreakEntity().also { dao.upsertStreak(it) }
        val badges = dao.getAllBadges()
        _streakState.value = streak
        _badgesState.value = badges
    }

    fun getStreakFlow(): Flow<StreakEntity?> = dao.getStreakFlow()
    fun getBadgesFlow(): Flow<List<BadgeEntity>> = dao.getAllBadgesFlow()

    /**
     * Evaluates streak logic timezone-resiliently and handles day rollover, streak freezes, and resets.
     */
    suspend fun evaluateDailyActivity(
        zoneId: ZoneId = ZoneId.systemDefault(),
        epochMillis: Long = System.currentTimeMillis()
    ): StreakEvaluationResult = withContext(Dispatchers.IO) {
        seedBadgesIfEmpty()
        val currentStreakEntity = dao.getStreak() ?: StreakEntity()
        val currentLocalDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId).toLocalDate()
        val currentEpochDay = currentLocalDate.toEpochDay()

        var newStreak = currentStreakEntity.currentStreak
        var newMaxStreak = currentStreakEntity.maxStreak
        var newFreezeCount = currentStreakEntity.streakFreezeCount
        var newTotalDays = currentStreakEntity.totalDaysActive
        var newDailyActions = currentStreakEntity.dailyActionsCount
        var isIncremented = false
        var isFreezeUsed = false
        var isBroken = false

        val lastDay = currentStreakEntity.lastActiveEpochDay

        if (lastDay == -1L) {
            // First time active
            newStreak = 1
            newMaxStreak = 1
            newTotalDays = 1
            newDailyActions = 1
            isIncremented = true
        } else if (lastDay == currentEpochDay) {
            // Already recorded active today -> just increment daily actions
            newDailyActions += 1
        } else if (lastDay == currentEpochDay - 1) {
            // Consecutive day activity!
            newStreak += 1
            newMaxStreak = maxOf(newMaxStreak, newStreak)
            newTotalDays += 1
            newDailyActions = 1
            isIncremented = true
        } else {
            // Missed 1 or more days (diff >= 2)
            val missedDays = (currentEpochDay - lastDay) - 1
            if (missedDays == 1L && newFreezeCount > 0) {
                // Streak Freeze consumed for 1 missed day!
                newFreezeCount -= 1
                newStreak += 1
                newMaxStreak = maxOf(newMaxStreak, newStreak)
                newTotalDays += 1
                newDailyActions = 1
                isFreezeUsed = true
                isIncremented = true
            } else {
                // Streak broken
                isBroken = true
                newStreak = 1
                newTotalDays += 1
                newDailyActions = 1
                isIncremented = true
            }
        }

        val updatedStreakEntity = currentStreakEntity.copy(
            currentStreak = newStreak,
            maxStreak = newMaxStreak,
            lastActiveEpochDay = currentEpochDay,
            streakFreezeCount = newFreezeCount,
            totalDaysActive = newTotalDays,
            dailyActionsCount = newDailyActions,
            lastUpdatedEpochMillis = epochMillis
        )

        dao.upsertStreak(updatedStreakEntity)

        // Check time of day badges (Night Owl: 22..23 or 0..4, Early Bird: 5..8)
        val hourOfDay = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId).hour
        val unlockedBadges = mutableListOf<BadgeEntity>()

        if (hourOfDay in 22..23 || hourOfDay in 0..4) {
            val badge = unlockBadgeInternal("night_owl", epochMillis)
            if (badge != null) unlockedBadges.add(badge)
        }
        if (hourOfDay in 5..8) {
            val badge = unlockBadgeInternal("early_bird", epochMillis)
            if (badge != null) unlockedBadges.add(badge)
        }

        // Check streak badges
        if (newStreak >= 3) {
            updateBadgeProgressInternal("streak_3_days", newStreak, epochMillis)?.let { unlockedBadges.add(it) }
        }
        if (newStreak >= 7) {
            updateBadgeProgressInternal("streak_7_days", newStreak, epochMillis)?.let { unlockedBadges.add(it) }
        }
        if (newStreak >= 30) {
            updateBadgeProgressInternal("streak_30_days", newStreak, epochMillis)?.let { unlockedBadges.add(it) }
        }

        refreshData()

        StreakEvaluationResult(
            previousStreak = currentStreakEntity.currentStreak,
            newStreak = newStreak,
            isIncremented = isIncremented,
            isFreezeUsed = isFreezeUsed,
            isBroken = isBroken,
            unlockedBadges = unlockedBadges
        )
    }

    /**
     * Records a user action (e.g. adding pair, time travel, overlap check) and advances badge progress.
     */
    suspend fun recordAction(
        action: GamificationAction,
        zoneId: ZoneId = ZoneId.systemDefault(),
        epochMillis: Long = System.currentTimeMillis()
    ): List<BadgeEntity> = withContext(Dispatchers.IO) {
        seedBadgesIfEmpty()
        val unlocked = mutableListOf<BadgeEntity>()

        // Always check streak activity
        val streakResult = evaluateDailyActivity(zoneId, epochMillis)
        unlocked.addAll(streakResult.unlockedBadges)

        when (action) {
            GamificationAction.ADD_TIME_PAIR -> {
                unlockBadgeInternal("first_timezone_added", epochMillis)?.let { unlocked.add(it) }
            }
            GamificationAction.TIME_TRAVEL -> {
                incrementBadgeProgressInternal("time_traveler", 1, epochMillis)?.let { unlocked.add(it) }
            }
            GamificationAction.CHECK_OVERLAP -> {
                incrementBadgeProgressInternal("meeting_pro", 1, epochMillis)?.let { unlocked.add(it) }
            }
            GamificationAction.LOG_ACTIVITY -> {
                incrementBadgeProgressInternal("activity_tracker", 1, epochMillis)?.let { unlocked.add(it) }
            }
            GamificationAction.OPEN_APP -> {
                // Handled in evaluateDailyActivity
            }
        }

        refreshData()
        unlocked
    }

    suspend fun addStreakFreeze(count: Int = 1) = withContext(Dispatchers.IO) {
        val streak = dao.getStreak() ?: StreakEntity()
        val updated = streak.copy(streakFreezeCount = streak.streakFreezeCount + count)
        dao.upsertStreak(updated)
        refreshData()
    }

    private suspend fun unlockBadgeInternal(badgeId: String, epochMillis: Long): BadgeEntity? {
        val badge = dao.getBadge(badgeId) ?: return null
        if (badge.isUnlocked) return null

        val updated = badge.copy(
            isUnlocked = true,
            progress = badge.maxProgress,
            unlockedAtEpochMillis = epochMillis
        )
        dao.upsertBadge(updated)
        return updated
    }

    private suspend fun incrementBadgeProgressInternal(badgeId: String, delta: Int, epochMillis: Long): BadgeEntity? {
        val badge = dao.getBadge(badgeId) ?: return null
        if (badge.isUnlocked) return null

        val newProgress = minOf(badge.maxProgress, badge.progress + delta)
        val isNowUnlocked = newProgress >= badge.maxProgress
        val updated = badge.copy(
            progress = newProgress,
            isUnlocked = isNowUnlocked,
            unlockedAtEpochMillis = if (isNowUnlocked) epochMillis else null
        )
        dao.upsertBadge(updated)
        return if (isNowUnlocked) updated else null
    }

    private suspend fun updateBadgeProgressInternal(badgeId: String, progress: Int, epochMillis: Long): BadgeEntity? {
        val badge = dao.getBadge(badgeId) ?: return null
        if (badge.isUnlocked) return null

        val newProgress = minOf(badge.maxProgress, progress)
        val isNowUnlocked = newProgress >= badge.maxProgress
        val updated = badge.copy(
            progress = newProgress,
            isUnlocked = isNowUnlocked,
            unlockedAtEpochMillis = if (isNowUnlocked) epochMillis else null
        )
        dao.upsertBadge(updated)
        return if (isNowUnlocked) updated else null
    }
}
