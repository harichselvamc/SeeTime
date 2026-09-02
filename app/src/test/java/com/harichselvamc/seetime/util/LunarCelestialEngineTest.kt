package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class LunarCelestialEngineTest {

    @Test
    fun `calculateMoonPhase calculates accurate New Moon on reference epoch`() {
        val newMoonZdt = ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneId.of("UTC"))
        val details = LunarCelestialEngine.calculateMoonPhase(newMoonZdt)

        assertEquals(LunarPhaseType.NEW_MOON, details.phaseType)
        assertTrue(details.illuminationPercentage < 1.0)
        assertTrue(details.ageDays < 0.5)
        assertEquals(10, details.stargazingScore)
        assertTrue(details.stargazingRating.contains("Pristine"))
    }

    @Test
    fun `calculateMoonPhase calculates accurate Full Moon half-cycle later`() {
        // ~14.765 days after Jan 6 2000 is Full Moon (Jan 21 2000)
        val fullMoonZdt = ZonedDateTime.of(2000, 1, 21, 12, 0, 0, 0, ZoneId.of("UTC"))
        val details = LunarCelestialEngine.calculateMoonPhase(fullMoonZdt)

        assertEquals(LunarPhaseType.FULL_MOON, details.phaseType)
        assertTrue(details.illuminationPercentage > 95.0)
        assertTrue(details.ageDays in 14.0..16.0)
        assertEquals(2, details.stargazingScore)
        assertTrue(details.stargazingRating.contains("Moonlit"))
    }

    @Test
    fun `calculateMoonPhase produces valid daysUntilFullMoon and daysUntilNewMoon`() {
        val now = ZonedDateTime.of(2026, 9, 2, 21, 0, 0, 0, ZoneId.of("UTC"))
        val details = LunarCelestialEngine.calculateMoonPhase(now)

        assertTrue(details.illuminationPercentage in 0.0..100.0)
        assertTrue(details.ageDays in 0.0..LunarCelestialEngine.SYNODIC_MONTH_DAYS)
        assertTrue(details.daysUntilFullMoon >= 0)
        assertTrue(details.daysUntilNewMoon >= 0)
        assertNotNull(details.nextFullMoonDate)
        assertNotNull(details.nextNewMoonDate)
    }

    @Test
    fun `generateMultiDayForecast produces continuous multi-day summary`() {
        val startDate = LocalDate.of(2026, 9, 2)
        val forecast = LunarCelestialEngine.generateMultiDayForecast(startDate, 7)

        assertEquals(7, forecast.size)
        assertEquals(startDate, forecast[0].date)
        assertEquals(startDate.plusDays(6), forecast[6].date)

        forecast.forEach {
            assertTrue(it.illuminationPercentage in 0..100)
            assertNotNull(it.phaseType)
        }
    }
}
