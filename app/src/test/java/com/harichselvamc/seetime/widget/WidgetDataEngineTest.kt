package com.harichselvamc.seetime.widget

import com.harichselvamc.seetime.data.local.StreakEntity
import com.harichselvamc.seetime.data.local.TimePair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class WidgetDataEngineTest {

    @Test
    fun testStreakTierClassification() {
        assertEquals(WidgetStreakTier.SPARK, WidgetDataEngine.getStreakTier(0))
        assertEquals(WidgetStreakTier.SPARK, WidgetDataEngine.getStreakTier(3))
        assertEquals(WidgetStreakTier.SPARK, WidgetDataEngine.getStreakTier(6))
        assertEquals(WidgetStreakTier.BLAZE, WidgetDataEngine.getStreakTier(7))
        assertEquals(WidgetStreakTier.BLAZE, WidgetDataEngine.getStreakTier(13))
        assertEquals(WidgetStreakTier.INFERNO, WidgetDataEngine.getStreakTier(14))
        assertEquals(WidgetStreakTier.INFERNO, WidgetDataEngine.getStreakTier(29))
        assertEquals(WidgetStreakTier.SUPERNOVA, WidgetDataEngine.getStreakTier(30))
        assertEquals(WidgetStreakTier.SUPERNOVA, WidgetDataEngine.getStreakTier(100))
    }

    @Test
    fun testComputeStreakWidgetData_CompletedToday() {
        val today = LocalDate.of(2026, 9, 2)
        val now = ZonedDateTime.of(2026, 9, 2, 14, 30, 0, 0, ZoneId.of("UTC"))
        val streak = StreakEntity(
            currentStreak = 12,
            lastActiveEpochDay = today.toEpochDay(),
            streakFreezeCount = 2
        )
        val pairs = listOf(
            TimePair(
                id = 1,
                fromZone = "Europe/London",
                toZone = "Europe/Paris"
            )
        )

        val result = WidgetDataEngine.computeStreakWidgetData(streak, pairs, now)

        assertEquals(12, result.currentStreak)
        assertEquals(WidgetStreakTier.BLAZE, result.tier)
        assertTrue(result.isCompletedToday)
        assertFalse(result.isExpiringSoon)
        assertEquals("Completed", result.badgeLabel)
    }

    @Test
    fun testComputeStreakWidgetData_ExpiringSoon() {
        val today = LocalDate.of(2026, 9, 2)
        // 22:30 at night -> 1.5 hours until midnight, not active today
        val now = ZonedDateTime.of(2026, 9, 2, 22, 30, 0, 0, ZoneId.of("UTC"))
        val streak = StreakEntity(
            currentStreak = 5,
            lastActiveEpochDay = today.minusDays(1).toEpochDay(),
            streakFreezeCount = 0
        )
        val pairs = emptyList<TimePair>()

        val result = WidgetDataEngine.computeStreakWidgetData(streak, pairs, now)

        assertEquals(5, result.currentStreak)
        assertFalse(result.isCompletedToday)
        assertTrue(result.isExpiringSoon)
        assertEquals("Expiring Soon!", result.badgeLabel)
    }

    @Test
    fun testComputeQuickLogWidgetData() {
        val today = LocalDate.of(2026, 9, 2)
        val now = ZonedDateTime.of(2026, 9, 2, 10, 0, 0, 0, ZoneId.of("UTC"))

        // Case 1: Incomplete today
        val streakIncomplete = StreakEntity(
            currentStreak = 8,
            lastActiveEpochDay = today.minusDays(1).toEpochDay()
        )
        val data1 = WidgetDataEngine.computeQuickLogWidgetData(streakIncomplete, now)
        assertEquals(8, data1.currentStreak)
        assertFalse(data1.isCompletedToday)
        assertEquals("8d", data1.streakText)
        assertEquals("+ Log", data1.actionText)

        // Case 2: Complete today
        val streakComplete = StreakEntity(
            currentStreak = 9,
            lastActiveEpochDay = today.toEpochDay()
        )
        val data2 = WidgetDataEngine.computeQuickLogWidgetData(streakComplete, now)
        assertEquals(9, data2.currentStreak)
        assertTrue(data2.isCompletedToday)
        assertEquals("9d", data2.streakText)
        assertEquals("Done", data2.actionText)
    }

    @Test
    fun testComputeCommandCenterWidgetData_WithTimezonePair() {
        val now = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneId.of("UTC"))
        val streak = StreakEntity(currentStreak = 15)
        val pairs = listOf(
            TimePair(
                id = 1,
                fromZone = "Europe/London",
                toZone = "Asia/Tokyo"
            )
        )

        val result = WidgetDataEngine.computeCommandCenterWidgetData(streak, pairs, now)

        assertEquals("London", result.localCity)
        assertEquals("Tokyo", result.remoteCity)
        assertEquals(15, result.currentStreak)
        assertTrue(result.localIsDaytime) // London 13:00 / 12:00 BST/UTC is daytime
        assertTrue(result.timeDiffFormatted.contains("Ahead"))
    }

    @Test
    fun testComputeCommandCenterWidgetData_EmptyPairsFallback() {
        val now = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneId.of("UTC"))
        val streak = StreakEntity(currentStreak = 0)
        val pairs = emptyList<TimePair>()

        val result = WidgetDataEngine.computeCommandCenterWidgetData(streak, pairs, now)

        assertEquals("Local", result.localCity)
        assertEquals("Select Pair", result.remoteCity)
        assertEquals("--:--", result.remoteTimeFormatted)
        assertEquals(0, result.currentStreak)
        assertFalse(result.isOverlapActive)
    }
}
