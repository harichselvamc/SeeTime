package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DateLineEngineTest {

    @Test
    fun `calculateFlightStatus computes eastbound Yesterday time travel for Tokyo to Honolulu`() {
        val hndHnlRoute = DateLineEngine.POPULAR_IDL_ROUTES.find { it.routeCode.contains("HND") }
        assertNotNull(hndHnlRoute)

        // Monday morning 10:00 AM in Tokyo
        val depTime = ZonedDateTime.of(2026, 9, 7, 10, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val status = DateLineEngine.calculateFlightStatus(hndHnlRoute!!, 0.6f, depTime)

        assertTrue(status.hasCrossedDateLine)
        assertEquals(-1, status.calendarDayDelta) // Land on Sunday (Yesterday!)
        assertTrue(status.dayShiftBannerText.contains("Yesterday"))
        assertEquals("Pacific/Honolulu", status.arrivalZdt.zone.id)
    }

    @Test
    fun `calculateFlightStatus computes 24-hour leap for Samoa to American Samoa`() {
        val samoaRoute = DateLineEngine.POPULAR_IDL_ROUTES.find { it.routeCode.contains("APW") }
        assertNotNull(samoaRoute)

        val depTime = ZonedDateTime.of(2026, 9, 7, 9, 0, 0, 0, ZoneId.of("Pacific/Apia"))
        val status = DateLineEngine.calculateFlightStatus(samoaRoute!!, 1.0f, depTime)

        assertEquals(-1, status.calendarDayDelta)
        assertEquals(2026, status.arrivalZdt.year)
        assertEquals(9, status.arrivalZdt.monthValue)
        assertEquals(6, status.arrivalZdt.dayOfMonth) // September 6 (Sunday) vs Sept 7 departure!
    }

    @Test
    fun `calculateFlightStatus computes westbound future jump for SFO to Auckland`() {
        val sfoAklRoute = DateLineEngine.POPULAR_IDL_ROUTES.find { it.routeCode.contains("AKL") }
        assertNotNull(sfoAklRoute)

        val depTime = ZonedDateTime.of(2026, 9, 7, 21, 0, 0, 0, ZoneId.of("America/Los_Angeles")) // Monday 9 PM
        val status = DateLineEngine.calculateFlightStatus(sfoAklRoute!!, 1.0f, depTime)

        assertTrue(status.calendarDayDelta >= 1) // Lands on Wednesday (2 days later calendar jump!)
        assertTrue(status.dayShiftBannerText.contains("Tomorrow") || status.dayShiftBannerText.contains("FUTURE"))
    }

    @Test
    fun `idl zigzag polyline spans Arctic to Antarctic with realistic deviations`() {
        assertTrue(DateLineEngine.IDL_ZIGZAG_POLYLINE.isNotEmpty())
        val first = DateLineEngine.IDL_ZIGZAG_POLYLINE.first()
        val last = DateLineEngine.IDL_ZIGZAG_POLYLINE.last()

        assertTrue(first.first > 80.0)
        assertTrue(last.first < -80.0)

        // Check Kiribati bulge (-150° lon)
        assertTrue(DateLineEngine.IDL_ZIGZAG_POLYLINE.any { it.second == -150.0 })
    }
}
