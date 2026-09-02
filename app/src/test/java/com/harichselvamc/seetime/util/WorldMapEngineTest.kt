package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldMapEngineTest {

    @Test
    fun `projectEquirectangular accurately maps corner and center coordinates`() {
        val w = 360f
        val h = 180f

        // Prime Meridian at Equator (0, 0) -> Center (180, 90)
        val center = WorldMapEngine.projectEquirectangular(0.0, 0.0, w, h)
        assertEquals(180f, center.x, 0.01f)
        assertEquals(90f, center.y, 0.01f)

        // North-West corner (90, -180) -> (0, 0)
        val nw = WorldMapEngine.projectEquirectangular(90.0, -180.0, w, h)
        assertEquals(0f, nw.x, 0.01f)
        assertEquals(0f, nw.y, 0.01f)

        // South-East corner (-90, 180) -> (360, 180)
        val se = WorldMapEngine.projectEquirectangular(-90.0, 180.0, w, h)
        assertEquals(360f, se.x, 0.01f)
        assertEquals(180f, se.y, 0.01f)
    }

    @Test
    fun `calculateTerminatorWave generates complete sinusoidal wave points`() {
        val subsolar = SubsolarPoint(latitude = 15.0, longitude = 45.0)
        val wave = WorldMapEngine.calculateTerminatorWave(subsolar, stepDegrees = 5)

        assertTrue(wave.isNotEmpty())
        wave.forEach { (lat, lon) ->
            assertTrue(lat in -90.0..90.0)
            assertTrue(lon in -180.0..180.0)
        }
    }

    @Test
    fun `generateTimezoneBands produces standard 25 UTC bands`() {
        val bands = WorldMapEngine.generateTimezoneBands()

        assertEquals(25, bands.size) // -12 to +12 inclusive
        bands.forEach { band ->
            assertNotNull(band.label)
            assertTrue(band.label.startsWith("UTC"))
            assertTrue(band.endLonDeg > band.startLonDeg)
        }
    }
}
