package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.BadgeEntity
import com.harichselvamc.seetime.data.local.StreakEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DynamicIconManagerTest {

    @Test
    fun `evaluateScenario returns DEFAULT for new user with zero streak`() {
        val streak = StreakEntity(currentStreak = 0)
        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            hourOfDay = 10,
            hasCompletedToday = false,
            hasGoldBadge = false
        )
        assertEquals(AppIconScenario.DEFAULT, scenario)
    }

    @Test
    fun `evaluateScenario returns STREAK_FIRE for 3+ day streak`() {
        val streak = StreakEntity(currentStreak = 5, streakFreezeCount = 2)
        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            hourOfDay = 14,
            hasCompletedToday = true,
            hasGoldBadge = false
        )
        assertEquals(AppIconScenario.STREAK_FIRE, scenario)
    }

    @Test
    fun `evaluateScenario returns STREAK_WARNING_MELTING at night when streak is at risk`() {
        // User has a 7-day streak, but hasn't logged today and it is 9:00 PM (21:00)
        val streak = StreakEntity(currentStreak = 7, streakFreezeCount = 2)
        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            hourOfDay = 21,
            hasCompletedToday = false,
            hasGoldBadge = false
        )
        assertEquals(AppIconScenario.STREAK_WARNING_MELTING, scenario)
    }

    @Test
    fun `evaluateScenario returns GOLDEN_MILESTONE for 30+ day streak or gold badge`() {
        val streak30 = StreakEntity(currentStreak = 30)
        val scenario30 = DynamicIconManager.evaluateScenario(
            streak = streak30,
            hourOfDay = 12,
            hasCompletedToday = true,
            hasGoldBadge = false
        )
        assertEquals(AppIconScenario.GOLDEN_MILESTONE, scenario30)

        val streakShort = StreakEntity(currentStreak = 2)
        val scenarioGoldBadge = DynamicIconManager.evaluateScenario(
            streak = streakShort,
            hourOfDay = 12,
            hasCompletedToday = true,
            hasGoldBadge = true
        )
        assertEquals(AppIconScenario.GOLDEN_MILESTONE, scenarioGoldBadge)
    }

    @Test
    fun `evaluateScenario returns STREAK_FROZEN when freeze count is reduced`() {
        val streak = StreakEntity(currentStreak = 2, streakFreezeCount = 1)
        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            hourOfDay = 15,
            hasCompletedToday = true,
            hasGoldBadge = false
        )
        assertEquals(AppIconScenario.STREAK_FROZEN, scenario)
    }

    @Test
    fun `evaluateScenario with badges list detects milestone unlocked`() {
        val zone = ZoneId.of("UTC")
        val epochMillis = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
        val streak = StreakEntity(currentStreak = 5, lastActiveEpochDay = LocalDate.of(2026, 9, 2).toEpochDay())
        val badges = listOf(
            BadgeEntity(
                id = "streak_30_days",
                title = "Time Master",
                description = "30-day streak",
                iconName = "military_tech",
                category = "Streak",
                isUnlocked = true
            )
        )

        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            badges = badges,
            zoneId = zone,
            epochMillis = epochMillis
        )
        assertEquals(AppIconScenario.GOLDEN_MILESTONE, scenario)
    }

    @Test
    fun `evaluateScenario with timestamp evaluates evening urgency correctly`() {
        val zone = ZoneId.of("UTC")
        // 10:30 PM (22:30 UTC)
        val eveningMillis = ZonedDateTime.of(2026, 9, 2, 22, 30, 0, 0, zone).toInstant().toEpochMilli()
        // Last active was yesterday (2026-09-01)
        val streak = StreakEntity(
            currentStreak = 4,
            lastActiveEpochDay = LocalDate.of(2026, 9, 1).toEpochDay(),
            streakFreezeCount = 2
        )

        val scenario = DynamicIconManager.evaluateScenario(
            streak = streak,
            badges = emptyList(),
            zoneId = zone,
            epochMillis = eveningMillis
        )
        assertEquals(AppIconScenario.STREAK_WARNING_MELTING, scenario)
    }

    @Test
    fun `backward compatibility aliases work seamlessly`() {
        assertEquals(AppIconScenario.STREAK_FIRE, AppIconScenario.Companion.FIERY_STREAK)
        assertEquals(AppIconScenario.STREAK_WARNING_MELTING, AppIconScenario.Companion.URGENT_MELTING)
        assertEquals(AppIconScenario.STREAK_FROZEN, AppIconScenario.Companion.FROZEN_SHIELD)

        val streak = StreakEntity(currentStreak = 5)
        val mode = DynamicIconManager.evaluateIconMode(streak, 12, true, false)
        assertEquals(AppIconScenario.STREAK_FIRE, mode)
    }
}
