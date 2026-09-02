package com.harichselvamc.seetime.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.harichselvamc.seetime.data.local.BadgeEntity
import com.harichselvamc.seetime.data.local.StreakEntity
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * App Icon Scenarios representing dynamic adaptive launcher icon states.
 */
enum class AppIconScenario(
    val aliasSuffix: String,
    val title: String,
    val description: String
) {
    DEFAULT(
        aliasSuffix = "MainActivityDefault",
        title = "Classic Chronometer",
        description = "Minimalist monochrome clock face"
    ),
    STREAK_FIRE(
        aliasSuffix = "MainActivityStreakFire",
        title = "Streak Blaze",
        description = "Active fire streak ablaze (3+ consecutive days)"
    ),
    STREAK_WARNING_MELTING(
        aliasSuffix = "MainActivityStreakWarningMelting",
        title = "Urgent Melting",
        description = "Urgent! Daily streak expires in less than 4 hours"
    ),
    STREAK_FROZEN(
        aliasSuffix = "MainActivityStreakFrozen",
        title = "Ice Shield",
        description = "Streak freeze activated to preserve your streak"
    ),
    GOLDEN_MILESTONE(
        aliasSuffix = "MainActivityGoldenMilestone",
        title = "Golden Milestone",
        description = "Prestigious 30+ day streak master & champion"
    );

    companion object {
        val FIERY_STREAK = STREAK_FIRE
        val URGENT_MELTING = STREAK_WARNING_MELTING
        val FROZEN_SHIELD = STREAK_FROZEN
    }
}

/** Backward compatibility alias for earlier references */
typealias AppIconMode = AppIconScenario

object DynamicIconManager {

    /**
     * Pure scenario evaluation function determining which app icon should be active.
     */
    fun evaluateScenario(
        streak: StreakEntity,
        hourOfDay: Int,
        hasCompletedToday: Boolean,
        hasGoldBadge: Boolean = false
    ): AppIconScenario {
        return when {
            // Milestone achieved (30+ day streak or gold trophy badge)
            streak.currentStreak >= 30 || hasGoldBadge -> AppIconScenario.GOLDEN_MILESTONE

            // Streak is active from previous days, but user hasn't logged today and it's late evening (<4 hours left in day: >= 20:00)
            streak.currentStreak > 0 && !hasCompletedToday && hourOfDay >= 20 -> AppIconScenario.STREAK_WARNING_MELTING

            // Active streak >= 3 days
            streak.currentStreak >= 3 -> AppIconScenario.STREAK_FIRE

            // Streak freeze used / active
            streak.streakFreezeCount < 2 && streak.currentStreak > 0 -> AppIconScenario.STREAK_FROZEN

            // Default initial state
            else -> AppIconScenario.DEFAULT
        }
    }

    /**
     * Compatibility alias for evaluateScenario.
     */
    fun evaluateIconMode(
        streak: StreakEntity,
        hourOfDay: Int,
        hasCompletedToday: Boolean,
        hasGoldBadge: Boolean = false
    ): AppIconScenario = evaluateScenario(streak, hourOfDay, hasCompletedToday, hasGoldBadge)

    /**
     * Overloaded scenario evaluator accepting timestamp, timezone, and badges list.
     */
    fun evaluateScenario(
        streak: StreakEntity,
        badges: List<BadgeEntity> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        epochMillis: Long = System.currentTimeMillis()
    ): AppIconScenario {
        val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)
        val todayEpochDay = zonedDateTime.toLocalDate().toEpochDay()
        val hasCompletedToday = (streak.lastActiveEpochDay == todayEpochDay)
        val hourOfDay = zonedDateTime.hour
        val hasGoldBadge = badges.any { it.isUnlocked && (it.id == "streak_30_days" || it.id.contains("milestone", ignoreCase = true)) }

        return evaluateScenario(
            streak = streak,
            hourOfDay = hourOfDay,
            hasCompletedToday = hasCompletedToday,
            hasGoldBadge = hasGoldBadge
        )
    }

    /**
     * Switches the launcher icon to [scenario] using Android's PackageManager.
     * Enables the target alias and disables all other aliases with DONT_KILL_APP.
     */
    fun setIcon(context: Context, scenario: AppIconScenario) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        for (targetScenario in AppIconScenario.values()) {
            val componentName = ComponentName(packageName, "$packageName.${targetScenario.aliasSuffix}")
            val targetState = if (targetScenario == scenario) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            try {
                val currentState = packageManager.getComponentEnabledSetting(componentName)
                if (currentState != targetState) {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        targetState,
                        PackageManager.DONT_KILL_APP
                    )
                }
            } catch (_: Exception) {
                // Graceful fallback for test or sandbox environments
            }
        }
    }

    /**
     * Alias for setIcon to maintain compatibility.
     */
    fun setAppIcon(context: Context, mode: AppIconScenario) = setIcon(context, mode)

    /**
     * Returns the currently active [AppIconScenario].
     */
    fun getCurrentIcon(context: Context): AppIconScenario {
        val packageManager = context.packageManager
        val packageName = context.packageName

        for (scenario in AppIconScenario.values()) {
            val componentName = ComponentName(packageName, "$packageName.${scenario.aliasSuffix}")
            try {
                val state = packageManager.getComponentEnabledSetting(componentName)
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return scenario
                }
            } catch (_: Exception) {
                // Ignore missing alias lookup
            }
        }
        return AppIconScenario.DEFAULT
    }

    /**
     * Evaluates gamification state (streak and badges) and applies the matching launcher icon.
     */
    fun syncIconWithGamificationState(
        context: Context,
        streak: StreakEntity,
        badges: List<BadgeEntity> = emptyList(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        epochMillis: Long = System.currentTimeMillis()
    ): AppIconScenario {
        val scenario = evaluateScenario(
            streak = streak,
            badges = badges,
            zoneId = zoneId,
            epochMillis = epochMillis
        )
        setIcon(context, scenario)
        return scenario
    }
}
