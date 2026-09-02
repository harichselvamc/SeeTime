package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class SolarPhotographyEngineTest {

    @Test
    fun `calculateSunPosition returns valid coordinates and elevations`() {
        val lat = 35.6762 // Tokyo
        val lon = 139.6503
        val zone = ZoneId.of("Asia/Tokyo")

        // Noon in Tokyo
        val noonTime = ZonedDateTime.of(2026, 9, 21, 12, 0, 0, 0, zone)
        val noonPos = SolarPhotographyEngine.calculateSunPosition(lat, lon, noonTime)

        assertTrue(noonPos.elevationDegrees in 0.0..90.0)
        assertTrue(noonPos.azimuthDegrees in 0.0..360.0)
        assertTrue(noonPos.isAboveHorizon)

        // Midnight in Tokyo
        val midnightTime = ZonedDateTime.of(2026, 9, 21, 0, 0, 0, 0, zone)
        val midPos = SolarPhotographyEngine.calculateSunPosition(lat, lon, midnightTime)

        assertTrue(midPos.elevationDegrees < 0.0)
        assertTrue(!midPos.isAboveHorizon)
        assertTrue(midPos.currentPhaseName.contains("Night") || midPos.currentPhaseName.contains("Twilight"))
    }

    @Test
    fun `calculateDailySchedule computes chronological phase windows`() {
        val lat = 51.5074 // London
        val lon = -0.1278
        val date = LocalDate.of(2026, 6, 21) // Summer Solstice
        val zone = ZoneId.of("Europe/London")

        val schedule = SolarPhotographyEngine.calculateDailySchedule(lat, lon, date, zone)

        assertNotNull(schedule.morningBlueHour)
        assertNotNull(schedule.morningGoldenHour)
        assertNotNull(schedule.sunriseTime)
        assertNotNull(schedule.solarNoonTime)
        assertNotNull(schedule.sunsetTime)
        assertNotNull(schedule.eveningGoldenHour)
        assertNotNull(schedule.eveningBlueHour)

        // Sunrise should precede Solar Noon
        assertTrue(schedule.sunriseTime.isBefore(schedule.solarNoonTime))
        // Solar Noon should precede Sunset
        assertTrue(schedule.solarNoonTime.isBefore(schedule.sunsetTime))
        // Morning Blue Hour begins before Sunrise
        assertTrue(schedule.morningBlueHour.startTime.isBefore(schedule.sunriseTime))
        // Evening Blue Hour begins after Sunset
        assertTrue(schedule.eveningBlueHour.startTime.isAfter(schedule.sunsetTime))
    }

    @Test
    fun `findNextUpcomingWindow identifies active or upcoming window`() {
        val lat = 40.7128 // New York
        val lon = -74.0060
        val date = LocalDate.of(2026, 9, 21)
        val zone = ZoneId.of("America/New_York")

        val schedule = SolarPhotographyEngine.calculateDailySchedule(lat, lon, date, zone)

        // Time right before morning blue hour
        val testTime = schedule.morningBlueHour.startTime.minusMinutes(10)
        val (window, diffMins) = SolarPhotographyEngine.findNextUpcomingWindow(schedule, testTime)

        assertEquals("Morning Blue Hour", window.phaseName)
        assertEquals(10, diffMins)

        // Time at Solar Noon -> next upcoming is Evening Golden Hour
        val noonTime = schedule.solarNoonTime
        val (noonNextWindow, noonDiff) = SolarPhotographyEngine.findNextUpcomingWindow(schedule, noonTime)
        assertEquals("Evening Golden Hour", noonNextWindow.phaseName)
        assertTrue(noonDiff > 0)

        // Time inside evening golden hour
        val insideTime = schedule.eveningGoldenHour.startTime.plusMinutes(5)
        val (activeWindow, remaining) = SolarPhotographyEngine.findNextUpcomingWindow(schedule, insideTime)

        assertEquals("Evening Golden Hour", activeWindow.phaseName)
        assertTrue(remaining < 0) // Negative denotes currently active
    }
}
