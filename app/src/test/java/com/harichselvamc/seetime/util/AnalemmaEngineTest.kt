package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AnalemmaEngineTest {

    @Test
    fun `calculateEotMinutes computes accurate astronomical extrema and zero crossings`() {
        // February 11 (Day ~42) max slow: ~ -14.2 min
        val febEot = AnalemmaEngine.calculateEotMinutes(42)
        assertTrue("Feb EoT should be around -14.2 min", febEot in -15.0..-13.0)

        // November 3 (Day ~307) max fast: ~ +16.4 min
        val novEot = AnalemmaEngine.calculateEotMinutes(307)
        assertTrue("Nov EoT should be around +16.4 min", novEot in +15.0..+17.0)

        // April 15 (Day ~105) zero crossing
        val aprEot = AnalemmaEngine.calculateEotMinutes(105)
        assertTrue("April zero crossing should be near 0", kotlin.math.abs(aprEot) < 1.0)
    }

    @Test
    fun `calculateSolarDeclination computes accurate seasonal solstices and equinoxes`() {
        // Summer Solstice (June 21, Day ~172) -> ~ +23.44°
        val summerDecl = AnalemmaEngine.calculateSolarDeclination(172)
        assertTrue("Summer declination should be ~ +23.4°", summerDecl in 22.0..24.0)

        // Winter Solstice (Dec 21, Day ~355) -> ~ -23.44°
        val winterDecl = AnalemmaEngine.calculateSolarDeclination(355)
        assertTrue("Winter declination should be ~ -23.4°", winterDecl in -24.0..-22.0)

        // Vernal Equinox (March 20, Day ~80) -> near 0°
        val springDecl = AnalemmaEngine.calculateSolarDeclination(80)
        assertTrue("Spring equinox declination should be near 0°", kotlin.math.abs(springDecl) < 1.5)
    }

    @Test
    fun `generate365DayAnalemmaCurve produces complete figure-8 coordinates`() {
        val curve = AnalemmaEngine.generate365DayAnalemmaCurve(2026)

        assertEquals(365, curve.size)
        curve.forEach { pt ->
            assertTrue(pt.eotMinutes in -20.0..20.0)
            assertTrue(pt.declinationDeg in -25.0..25.0)
            assertNotNull(pt.dateFormatted)
        }
    }

    @Test
    fun `convertSundialToCivilTime computes accurate clock time from solar shadow`() {
        // On November 3 (Day ~307), EoT is ~ +16.4 minutes fast -> 12:00 solar shadow is 11:43 clock time
        val nov3 = LocalDate.of(2026, 11, 3)
        val zone = ZoneId.of("UTC")
        val clockTime = AnalemmaEngine.convertSundialToCivilTime(12, 0, 0.0, zone, nov3)

        assertNotNull(clockTime)
        assertEquals(11, clockTime.hour)
        assertTrue(clockTime.minute in 42..45)
    }
}
