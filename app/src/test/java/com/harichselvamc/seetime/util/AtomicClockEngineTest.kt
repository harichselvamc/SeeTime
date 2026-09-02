package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicClockEngineTest {

    @Test
    fun `calculateCesiumOscillations computes exact SI quantum transitions`() {
        // Exactly 1 second = 9,192,631,770 Hz
        assertEquals(0L, AtomicClockEngine.calculateCesiumOscillations(0, 0))
        assertEquals(9192631770L, AtomicClockEngine.calculateCesiumOscillations(1, 0))

        // 10.5 seconds
        val tenHalfSec = AtomicClockEngine.calculateCesiumOscillations(10, 500)
        val expected = (10 * 9192631770L) + (9192631770L / 2L)
        assertEquals(expected, tenHalfSec)
    }

    @Test
    fun `calculateQuartzThermalDrift produces parabolic turnover curve`() {
        // At 25°C room temp -> zero drift
        val roomTemp = AtomicClockEngine.calculateQuartzThermalDrift(25.0f)
        assertEquals(0.0, roomTemp.ppmDrift, 0.001)
        assertEquals(0.0, roomTemp.dailyDriftSeconds, 0.001)
        assertEquals(32768.0, roomTemp.frequencyHz, 0.001)
        assertTrue(roomTemp.isOptimalTemp)

        // At 0°C (delta = -25°C) -> -0.035 * 625 = -21.875 PPM
        val coldTemp = AtomicClockEngine.calculateQuartzThermalDrift(0.0f)
        assertEquals(-21.875, coldTemp.ppmDrift, 0.01)
        assertTrue(coldTemp.dailyDriftSeconds in -2.0..-1.8) // ~ -1.89 s/day

        // At 50°C (delta = +25°C) -> symmetric -21.875 PPM
        val hotTemp = AtomicClockEngine.calculateQuartzThermalDrift(50.0f)
        assertEquals(-21.875, hotTemp.ppmDrift, 0.01)
    }

    @Test
    fun `simulateNtpSync computes 4-timestamp RFC 5905 delay and offset`() {
        val ntp = AtomicClockEngine.simulateNtpSync("time.nist.gov", 1)

        assertNotNull(ntp)
        assertEquals("time.nist.gov", ntp.serverHost)
        assertEquals(1, ntp.stratumTier)
        assertTrue(ntp.roundTripDelayMs in 10.0..50.0)
        assertTrue(kotlin.math.abs(ntp.clockOffsetMs) < 10.0)
        assertNotNull(ntp.statusLabel)
    }
}
