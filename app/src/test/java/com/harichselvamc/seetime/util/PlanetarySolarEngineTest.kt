package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanetarySolarEngineTest {

    @Test
    fun `calculateYearEvents returns 4 astronomical events with valid dates`() {
        val events = PlanetarySolarEngine.calculateYearEvents(2026)

        assertEquals(4, events.size)
        assertEquals("Spring Equinox (Vernal)", events[0].name)
        assertEquals(LocalDate.of(2026, 3, 20), events[0].date)

        assertEquals("Summer Solstice", events[1].name)
        assertEquals(LocalDate.of(2026, 6, 21), events[1].date)

        assertEquals("Autumn Equinox", events[2].name)
        assertEquals(LocalDate.of(2026, 9, 22), events[2].date)

        assertEquals("Winter Solstice", events[3].name)
        assertEquals(LocalDate.of(2026, 12, 21), events[3].date)

        events.forEach {
            assertTrue(it.daysUntil >= 0)
            assertNotNull(it.daylightCharacteristic)
        }
    }

    @Test
    fun `calculateSeasonalProgress determines correct Northern hemisphere seasons`() {
        // May 1 -> Spring
        val springDetails = PlanetarySolarEngine.calculateSeasonalProgress(LocalDate.of(2026, 5, 1), 40.0)
        assertEquals(AstronomicalSeason.SPRING, springDetails.currentSeason)
        assertTrue(springDetails.isDaylightGaining)
        assertTrue(springDetails.seasonProgressPercent in 0f..100f)

        // July 15 -> Summer
        val summerDetails = PlanetarySolarEngine.calculateSeasonalProgress(LocalDate.of(2026, 7, 15), 40.0)
        assertEquals(AstronomicalSeason.SUMMER, summerDetails.currentSeason)

        // October 10 -> Autumn
        val autumnDetails = PlanetarySolarEngine.calculateSeasonalProgress(LocalDate.of(2026, 10, 10), 40.0)
        assertEquals(AstronomicalSeason.AUTUMN, autumnDetails.currentSeason)

        // January 20 -> Winter
        val winterDetails = PlanetarySolarEngine.calculateSeasonalProgress(LocalDate.of(2026, 1, 20), 40.0)
        assertEquals(AstronomicalSeason.WINTER, winterDetails.currentSeason)
    }

    @Test
    fun `calculateVisiblePlanets provides complete 5 naked-eye planetary guide`() {
        val planets = PlanetarySolarEngine.calculateVisiblePlanets(LocalDate.of(2026, 9, 2))

        assertEquals(5, planets.size)
        val planetNames = planets.map { it.name }
        assertTrue(planetNames.contains("Venus"))
        assertTrue(planetNames.contains("Jupiter"))
        assertTrue(planetNames.contains("Saturn"))
        assertTrue(planetNames.contains("Mars"))
        assertTrue(planetNames.contains("Mercury"))

        planets.forEach {
            assertNotNull(it.magnitude)
            assertNotNull(it.bestViewingTime)
            assertNotNull(it.skyPosition)
            assertNotNull(it.opticalRecommendation)
        }
    }
}
