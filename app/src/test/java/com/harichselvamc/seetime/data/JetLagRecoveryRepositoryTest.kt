package com.harichselvamc.seetime.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JetLagRecoveryRepositoryTest {

    @Test
    fun `initial state and plans are populated`() {
        val repo = JetLagRecoveryRepository(null)
        val state = repo.userState.value
        val plans = repo.plans.value

        assertEquals("America/New_York", state.originZoneId)
        assertEquals("Asia/Tokyo", state.destZoneId)
        assertEquals(5, plans.size)
    }

    @Test
    fun `setOriginZone and setDestZone recalculate plans`() {
        val repo = JetLagRecoveryRepository(null)
        repo.setOriginZone("Europe/London")
        repo.setDestZone("America/Los_Angeles")

        assertEquals("Europe/London", repo.userState.value.originZoneId)
        assertEquals("America/Los_Angeles", repo.userState.value.destZoneId)

        val firstPlan = repo.plans.value.first()
        assertEquals("Europe/London", firstPlan.originZoneId)
        assertEquals("America/Los_Angeles", firstPlan.destZoneId)
    }

    @Test
    fun `logWaterIntake and resetWaterIntake manipulate water count`() {
        val repo = JetLagRecoveryRepository(null)
        assertEquals(0, repo.userState.value.loggedWaterMl)

        repo.logWaterIntake(250)
        assertEquals(250, repo.userState.value.loggedWaterMl)

        repo.logWaterIntake(500)
        assertEquals(750, repo.userState.value.loggedWaterMl)

        repo.resetWaterIntake()
        assertEquals(0, repo.userState.value.loggedWaterMl)
    }

    @Test
    fun `toggleMealCompleted flips meal bitmask state`() {
        val repo = JetLagRecoveryRepository(null)
        assertFalse(repo.isMealCompleted(0))

        repo.toggleMealCompleted(0)
        assertTrue(repo.isMealCompleted(0))

        repo.toggleMealCompleted(0)
        assertFalse(repo.isMealCompleted(0))
    }
}
