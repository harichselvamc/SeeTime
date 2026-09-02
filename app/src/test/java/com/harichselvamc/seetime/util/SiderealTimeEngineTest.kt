package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SiderealTimeEngineTest {

    @Test
    fun `calculateGmstHours computes accurate GMST at J2000 epoch`() {
        // J2000.0 epoch: Jan 1, 2000 12:00 UTC
        val j2000Millis = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        val gmst = SiderealTimeEngine.calculateGmstHours(j2000Millis)

        assertEquals(18.69737, gmst, 0.001)
        assertTrue(gmst in 0.0..24.0)
    }

    @Test
    fun `calculateLstHours accurately adjusts for observer longitude`() {
        val epochMillis = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        val gmst = SiderealTimeEngine.calculateGmstHours(epochMillis)

        // At Prime Meridian (lon = 0°), LST == GMST
        val lstGreenwich = SiderealTimeEngine.calculateLstHours(epochMillis, 0.0)
        assertEquals(gmst, lstGreenwich, 0.0001)

        // At +60° East (+4h), LST = (GMST + 4) % 24
        val lstEast60 = SiderealTimeEngine.calculateLstHours(epochMillis, 60.0)
        val expectedEast = ((gmst + 4.0) % 24.0)
        assertEquals(expectedEast, lstEast60, 0.0001)
    }

    @Test
    fun `calculateStarTransit flags culmination when LST equals Right Ascension`() {
        // Sirius RA = 6.7525h
        val sirius = SiderealTimeEngine.MAJOR_CELESTIAL_STARS.find { it.name == "Sirius" }
        assertNotNull(sirius)

        // When LST = 6.7525h
        val transit = SiderealTimeEngine.calculateStarTransit(sirius!!, 6.7525, 51.5)

        assertTrue(transit.isCulminatingNow)
        assertTrue(transit.formattedTransitIn.contains("CULMINATING NOW"))
        assertTrue(transit.meridianAltitudeDeg > 0.0) // Visible above southern horizon
    }

    @Test
    fun `celestial stars catalog contains standard primary navigation beacons`() {
        assertTrue(SiderealTimeEngine.MAJOR_CELESTIAL_STARS.isNotEmpty())
        val starNames = SiderealTimeEngine.MAJOR_CELESTIAL_STARS.map { it.name }

        assertTrue(starNames.contains("Sirius"))
        assertTrue(starNames.contains("Vega"))
        assertTrue(starNames.contains("Polaris"))
        assertTrue(starNames.contains("Betelgeuse"))
    }
}
