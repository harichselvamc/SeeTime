package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class GlobeMathTest {

    @Test
    fun `calculateSubsolarPoint returns valid geographic bounds`() {
        val now = ZonedDateTime.of(2026, 6, 21, 12, 0, 0, 0, ZoneId.of("UTC")) // Summer solstice noon UTC
        val subsolar = GlobeMath.calculateSubsolarPoint(now)

        // On June solstice, subsolar latitude should be around +23.4° N
        assertTrue(subsolar.latitude in 20.0..24.5)
        // At 12:00 UTC, subsolar longitude should be around 0° (Greenwich)
        assertTrue(subsolar.longitude in -5.0..5.0)
    }

    @Test
    fun `projectOrthographic maps center of view to origin`() {
        val centerLat = 35.6762 // Tokyo
        val centerLon = 139.6503
        val radius = 100f
        val subsolar = SubsolarPoint(latitude = 0.0, longitude = 0.0)

        val pt = GlobeMath.projectOrthographic(
            latDeg = centerLat,
            lonDeg = centerLon,
            centerLatDeg = centerLat,
            centerLonDeg = centerLon,
            radius = radius,
            subsolarPoint = subsolar
        )

        assertTrue(pt.isVisible)
        assertEquals(0f, pt.x, 0.01f)
        assertEquals(0f, pt.y, 0.01f)
    }

    @Test
    fun `projectOrthographic culls points on the opposite side of Earth`() {
        val centerLat = 0.0
        val centerLon = 0.0
        val radius = 100f
        val subsolar = SubsolarPoint(latitude = 0.0, longitude = 0.0)

        // Point at 180° opposite
        val pt = GlobeMath.projectOrthographic(
            latDeg = 0.0,
            lonDeg = 180.0,
            centerLatDeg = centerLat,
            centerLonDeg = centerLon,
            radius = radius,
            subsolarPoint = subsolar
        )

        assertFalse(pt.isVisible)
    }

    @Test
    fun `major world cities have valid coordinates`() {
        assertTrue(GlobeMath.MAJOR_WORLD_CITIES.isNotEmpty())
        GlobeMath.MAJOR_WORLD_CITIES.forEach { city ->
            assertNotNull(city.name)
            assertNotNull(city.zoneId)
            assertTrue(city.latitude in -90.0..90.0)
            assertTrue(city.longitude in -180.0..180.0)
        }
    }
}
