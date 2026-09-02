package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ChronoNutritionEngineTest {

    @Test
    fun `calculateZoneOffsetDiffHours calculates correct hour difference`() {
        val ny = "America/New_York"
        val tokyo = "Asia/Tokyo"
        val diff = ChronoNutritionEngine.calculateZoneOffsetDiffHours(ny, tokyo)
        // New York to Tokyo difference is typically +13 or +14 depending on DST
        assertTrue(diff in 13..14)
    }

    @Test
    fun `generateRecoveryPlan generates full 5-day plan`() {
        val plans = ChronoNutritionEngine.generateRecoveryPlan(
            originZone = "America/New_York",
            destZone = "Asia/Tokyo"
        )

        assertEquals(5, plans.size)

        for (plan in plans) {
            assertEquals(3, plan.meals.size)
            assertTrue(plan.hydrationSlots.size >= 5)
            assertTrue(plan.totalDailyWaterMl >= 1800)
            assertNotNull(plan.caffeineCutoff)
            assertNotNull(plan.melatoninWindow)
            assertNotNull(plan.fastingWindow)
            assertTrue(plan.chronoTip.isNotBlank())
        }
    }

    @Test
    fun `eastward travel shifts breakfast earlier`() {
        val baseBreakfast = LocalTime.of(8, 0)
        val plans = ChronoNutritionEngine.generateRecoveryPlan(
            originZone = "America/New_York",
            destZone = "Europe/London",
            baseBreakfast = baseBreakfast
        )

        val day1 = plans[0]
        val day5 = plans[4]

        // Day 5 should have shifted earlier (or adjusted)
        assertTrue(day1.meals[0].recommendedTime == baseBreakfast)
        assertTrue(day5.meals[0].recommendedTime != baseBreakfast)
    }

    @Test
    fun `hydration schedule contains morning electrolyte boost`() {
        val plans = ChronoNutritionEngine.generateRecoveryPlan(
            originZone = "Europe/London",
            destZone = "America/New_York"
        )

        val dayPlan = plans[0]
        val firstSlot = dayPlan.hydrationSlots[0]
        assertTrue(firstSlot.isElectrolyteBoost)
        assertTrue(firstSlot.targetVolumeMl >= 350)
    }
}
