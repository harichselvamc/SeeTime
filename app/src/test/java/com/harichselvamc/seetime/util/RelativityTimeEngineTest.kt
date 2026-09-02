package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelativityTimeEngineTest {

    @Test
    fun `calculateOrbitalDilation computes accurate GPS satellite time dilation`() {
        val gpsResult = RelativityTimeEngine.calculateOrbitalDilation(20200.0, "GPS")

        // General Relativity (weaker gravity) gain: ~ +45.9 us/day
        assertTrue("GR gain should be ~ +45.9 us/day", gpsResult.gravitationalGainMicrosecondsPerDay in 44.0..48.0)

        // Special Relativity (3.87 km/s velocity) loss: ~ -7.2 us/day
        assertTrue("SR loss should be ~ -7.2 us/day", gpsResult.velocityLossMicrosecondsPerDay in -8.5..-6.0)

        // Net GPS dilation: ~ +38.7 us/day
        assertTrue("Net GPS dilation should be ~ +38.7 us/day", gpsResult.netDilationMicrosecondsPerDay in 36.0..41.0)

        // Daily drift: ~ 11.6 km/day
        assertTrue("Daily drift should be ~ 11.6 km/day", gpsResult.dailyPositioningErrorKm in 10.5..12.5)
    }

    @Test
    fun `calculateOrbitalDilation confirms ISS time dilation is net negative`() {
        val issResult = RelativityTimeEngine.calculateOrbitalDilation(420.0, "ISS")

        // In LEO (ISS), velocity dilation dominates -> clocks run slower
        assertTrue("ISS net dilation should be negative", issResult.netDilationMicrosecondsPerDay < 0.0)
        assertTrue("ISS orbital speed is ~7.66 km/s", issResult.orbitalVelocityKmS in 7.4..7.9)
    }

    @Test
    fun `calculateLorentzFactor computes correct relativistic dilation factors`() {
        assertEquals(1.0, RelativityTimeEngine.calculateLorentzFactor(0.0), 0.001)

        // At beta = 0.866c, gamma = 1 / sqrt(1 - 0.75) = 1 / 0.5 = 2.0
        val gamma866 = RelativityTimeEngine.calculateLorentzFactor(0.866025)
        assertEquals(2.0, gamma866, 0.01)

        // At beta = 0.99c, gamma ~ 7.088
        val gamma99 = RelativityTimeEngine.calculateLorentzFactor(0.99)
        assertTrue(gamma99 in 7.0..7.2)
    }

    @Test
    fun `calculateShipTravelTime accurately scales astronaut subjective time`() {
        // 10 Earth years at gamma = 2.0 (beta = 0.866) -> 5.0 ship years
        val shipYears = RelativityTimeEngine.calculateShipTravelTime(10.0, 0.866025)
        assertEquals(5.0, shipYears, 0.05)
    }

    @Test
    fun `celestial light speed targets have realistic one-way communication lags`() {
        val moon = RelativityTimeEngine.CELESTIAL_TARGETS.find { it.targetName.contains("Moon") }
        assertNotNull(moon)
        assertEquals(1.28, moon!!.oneWayDelaySeconds, 0.05)

        val sun = RelativityTimeEngine.CELESTIAL_TARGETS.find { it.targetName.contains("Sun") }
        assertNotNull(sun)
        assertEquals(499.0, sun!!.oneWayDelaySeconds, 5.0) // ~8.32 minutes = 499s
    }
}
