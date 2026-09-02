package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class IssPassPredictorEngineTest {

    @Test
    fun testIssPosition_boundedByOrbitalInclination() {
        val startMillis = 1788220800000L // Sep 1, 2026 UTC

        // Test over 24 hours in 10-minute intervals
        for (minute in 0..1440 step 10) {
            val t = startMillis + (minute * 60000L)
            val pos = IssPassPredictorEngine.calculateIssPosition(t)

            assertTrue("Latitude ${pos.latitude} must be within ±51.65°",
                pos.latitude >= -51.65 && pos.latitude <= 51.65)
            assertTrue("Longitude ${pos.longitude} must be within ±180°",
                pos.longitude >= -180.0 && pos.longitude <= 180.0)
            assertTrue("Altitude ${pos.altitudeKm} must be in LEO range ~418..422 km",
                pos.altitudeKm in 415.0..425.0)
            assertTrue("Orbital speed must be ~27,576 km/h", pos.groundSpeedKmH > 25000.0)
        }
    }

    @Test
    fun testIssPosition_orbitalPeriodAndWestwardDrift() {
        val startMillis = 1788220800000L
        val pos1 = IssPassPredictorEngine.calculateIssPosition(startMillis)

        // After exactly 1 orbit (~92.9 minutes)
        val oneOrbitMillis = startMillis + (92.9 * 60000.0).toLong()
        val pos2 = IssPassPredictorEngine.calculateIssPosition(oneOrbitMillis)

        // Latitude should return to approximately the same value (within ±5°)
        val latDiff = kotlin.math.abs(pos1.latitude - pos2.latitude)
        assertTrue("Latitude after 1 orbit should match closely (diff: $latDiff)", latDiff < 6.0)

        // Longitude should have drifted westward (by ~23.2° due to 92.9m of Earth rotation)
        var lonShift = (pos1.longitude - pos2.longitude)
        if (lonShift < 0) lonShift += 360.0
        assertTrue("Longitude shift after 1 orbit should be ~23° (got $lonShift)",
            lonShift in 15.0..30.0)
    }

    @Test
    fun testLookAngle_directOverheadZenith() {
        val epochMillis = 1788220800000L
        val issPos = IssPassPredictorEngine.calculateIssPosition(epochMillis)

        // Ground observer placed exactly at ISS sub-satellite coordinates
        val look = IssPassPredictorEngine.calculateLookAngle(issPos.latitude, issPos.longitude, epochMillis)

        assertTrue("Elevation directly underneath ISS must be close to 90° (got ${look.elevationDeg})",
            look.elevationDeg > 88.0)
        assertTrue("Slant range directly underneath should be close to altitude (~420 km) (got ${look.slantRangeKm})",
            look.slantRangeKm in 410.0..430.0)
        assertTrue("Should be above horizon", look.isAboveHorizon)
    }

    @Test
    fun testLookAngle_oppositeHemisphere() {
        val epochMillis = 1788220800000L
        val issPos = IssPassPredictorEngine.calculateIssPosition(epochMillis)

        // Observer placed on exact opposite side of Earth
        val oppLat = -issPos.latitude
        val oppLon = if (issPos.longitude > 0) issPos.longitude - 180.0 else issPos.longitude + 180.0

        val look = IssPassPredictorEngine.calculateLookAngle(oppLat, oppLon, epochMillis)

        assertTrue("Elevation on opposite side of Earth must be well below horizon (got ${look.elevationDeg})",
            look.elevationDeg < -50.0)
        assertFalse("Must not be above horizon", look.isAboveHorizon)
    }

    @Test
    fun testCompassDirections() {
        assertEquals("N", IssPassPredictorEngine.getCompassDirection(0.0))
        assertEquals("N", IssPassPredictorEngine.getCompassDirection(360.0))
        assertEquals("NE", IssPassPredictorEngine.getCompassDirection(45.0))
        assertEquals("E", IssPassPredictorEngine.getCompassDirection(90.0))
        assertEquals("SE", IssPassPredictorEngine.getCompassDirection(135.0))
        assertEquals("S", IssPassPredictorEngine.getCompassDirection(180.0))
        assertEquals("SW", IssPassPredictorEngine.getCompassDirection(225.0))
        assertEquals("W", IssPassPredictorEngine.getCompassDirection(270.0))
        assertEquals("NW", IssPassPredictorEngine.getCompassDirection(315.0))
    }

    @Test
    fun testPredictUpcomingPasses_generatesValidPasses() {
        val londonLat = 51.5074
        val londonLon = -0.1278
        val startMillis = 1788220800000L // Sep 1, 2026

        val passes = IssPassPredictorEngine.predictUpcomingPasses(
            observerLat = londonLat,
            observerLon = londonLon,
            startEpochMillis = startMillis,
            durationHours = 72,
            minElevationDeg = 10.0,
            observerCityName = "London"
        )

        assertTrue("Must discover upcoming passes over 72 hours for London (got ${passes.size})",
            passes.isNotEmpty())

        for (pass in passes) {
            assertTrue("AOS must precede Culmination", pass.aosTime.toEpochMilli() <= pass.culminationTime.toEpochMilli())
            assertTrue("Culmination must precede LOS", pass.culminationTime.toEpochMilli() <= pass.losTime.toEpochMilli())
            assertTrue("Max elevation must be >= 10°", pass.maxElevationDeg >= 10.0)
            assertTrue("Pass duration must be positive", pass.durationSeconds > 0)
            assertNotNull(pass.visibilityType)
            assertTrue(pass.riseDirection.isNotBlank())
            assertTrue(pass.setDirection.isNotBlank())
        }
    }

    @Test
    fun testGroundTrackGeneration() {
        val currentMillis = 1788220800000L
        val track = IssPassPredictorEngine.calculateGroundTrack(
            currentEpochMillis = currentMillis,
            pastMinutes = 45,
            futureMinutes = 90,
            stepMinutes = 1
        )

        assertEquals("Total points should be 45 + 90 + 1 = 136", 136, track.size)

        val currentPoint = track.firstOrNull { it.isCurrent }
        assertNotNull("Must include current track point", currentPoint)
        assertEquals(0, currentPoint!!.minuteOffset)

        val pastPoints = track.filter { it.isPast }
        val futurePoints = track.filter { !it.isPast && !it.isCurrent }
        assertEquals(45, pastPoints.size)
        assertEquals(90, futurePoints.size)
    }

    @Test
    fun testIsSatelliteSunlit() {
        val epochMillis = 1788220800000L // Sep 1, 2026 00:00 UTC (Noon in Pacific, Midnight in Greenwich)

        // Pacific point (Day side)
        val daysideSunlit = IssPassPredictorEngine.isSatelliteSunlit(0.0, -180.0, 420.0, epochMillis)
        assertTrue("Satellite over Pacific at 00:00 UTC should be sunlit", daysideSunlit)
    }
}
