package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SolarOrreryEngineTest {

    @Test
    fun `all 8 major planets are configured with physical constants`() {
        val planets = PlanetId.values()
        assertEquals(8, planets.size)

        // Earth constants
        val earth = PlanetId.EARTH
        assertEquals(1.000, earth.semiMajorAxisAu, 0.001)
        assertEquals(365.256, earth.orbitalPeriodDays, 0.01)
        assertTrue(earth.isInnerPlanet)

        // Jupiter constants
        val jupiter = PlanetId.JUPITER
        assertEquals(5.204, jupiter.semiMajorAxisAu, 0.01)
        assertEquals(4332.589, jupiter.orbitalPeriodDays, 0.1)
        assertTrue(!jupiter.isInnerPlanet)
    }

    @Test
    fun `solveKepler converges to accurate eccentric anomaly`() {
        // M = 1.0 rad, e = 0.1
        val mRad = 1.0
        val e = 0.1
        val eccentricAnomaly = SolarOrreryEngine.solveKepler(mRad, e)

        // Verification: E - e*sin(E) should equal M
        val residual = eccentricAnomaly - e * kotlin.math.sin(eccentricAnomaly)
        assertEquals(mRad, residual, 1e-7)
    }

    @Test
    fun `calculatePlanetState computes accurate heliocentric distances and velocities`() {
        val instant = Instant.parse("2026-09-02T12:00:00Z")

        // Earth at 2026-09-02 (early September -> near 1.008 AU, velocity ~29.7 km/s)
        val earthState = SolarOrreryEngine.calculatePlanetState(PlanetId.EARTH, instant)
        assertTrue(earthState.distanceAu in 0.98..1.02)
        assertTrue(earthState.orbitalVelocityKmPerSec in 29.0..30.5)
        assertEquals(PlanetId.EARTH, earthState.planet)

        // Mercury orbital velocity is high (~47 km/s)
        val mercuryState = SolarOrreryEngine.calculatePlanetState(PlanetId.MERCURY, instant)
        assertTrue(mercuryState.distanceAu in 0.30..0.47)
        assertTrue(mercuryState.orbitalVelocityKmPerSec in 38.0..59.0)

        // Neptune orbital velocity is low (~5.4 km/s)
        val neptuneState = SolarOrreryEngine.calculatePlanetState(PlanetId.NEPTUNE, instant)
        assertTrue(neptuneState.distanceAu in 29.5..30.5)
        assertTrue(neptuneState.orbitalVelocityKmPerSec in 5.0..6.0)
    }

    @Test
    fun `calculateAllPlanets returns 8 planetary states`() {
        val instant = Instant.now()
        val allStates = SolarOrreryEngine.calculateAllPlanets(instant)
        assertEquals(8, allStates.size)
        assertTrue(allStates.any { it.planet == PlanetId.MARS })
        assertTrue(allStates.any { it.planet == PlanetId.SATURN })

        // Check zodiac assignment
        allStates.forEach { state ->
            assertNotNull(state.zodiacSign)
            assertTrue(state.zodiacSign.isNotBlank())
            assertTrue(state.heliocentricLongitudeDeg in 0.0..360.0)
        }
    }
}
