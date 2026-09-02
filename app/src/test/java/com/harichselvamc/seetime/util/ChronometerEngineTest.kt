package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ChronometerEngineTest {

    @Test
    fun `calculateSweepingSecondAngle produces continuous angles`() {
        assertEquals(0f, ChronometerEngine.calculateSweepingSecondAngle(0, 0), 0.01f)
        assertEquals(90f, ChronometerEngine.calculateSweepingSecondAngle(15, 0), 0.01f)
        assertEquals(180f, ChronometerEngine.calculateSweepingSecondAngle(30, 0), 0.01f)
        assertEquals(270f, ChronometerEngine.calculateSweepingSecondAngle(45, 0), 0.01f)
        assertEquals(183f, ChronometerEngine.calculateSweepingSecondAngle(30, 500), 0.01f)
    }

    @Test
    fun `calculateMinuteAngle computes accurate 60-minute dial positions`() {
        assertEquals(0f, ChronometerEngine.calculateMinuteAngle(0, 0), 0.01f)
        assertEquals(90f, ChronometerEngine.calculateMinuteAngle(15, 0), 0.01f)
        assertEquals(180f, ChronometerEngine.calculateMinuteAngle(30, 0), 0.01f)
        assertEquals(270f, ChronometerEngine.calculateMinuteAngle(45, 0), 0.01f)
    }

    @Test
    fun `calculateHourAngle computes accurate 12-hour positions`() {
        assertEquals(0f, ChronometerEngine.calculateHourAngle(0, 0, 0), 0.01f)
        assertEquals(90f, ChronometerEngine.calculateHourAngle(3, 0, 0), 0.01f)
        assertEquals(180f, ChronometerEngine.calculateHourAngle(6, 0, 0), 0.01f)
        assertEquals(270f, ChronometerEngine.calculateHourAngle(9, 0, 0), 0.01f)
        assertEquals(0f, ChronometerEngine.calculateHourAngle(12, 0, 0), 0.01f)
    }

    @Test
    fun `calculateGmt24Angle computes accurate 24-hour positions`() {
        assertEquals(0f, ChronometerEngine.calculateGmt24Angle(0, 0), 0.01f)
        assertEquals(90f, ChronometerEngine.calculateGmt24Angle(6, 0), 0.01f)
        assertEquals(180f, ChronometerEngine.calculateGmt24Angle(12, 0), 0.01f)
        assertEquals(270f, ChronometerEngine.calculateGmt24Angle(18, 0), 0.01f)
    }

    @Test
    fun `calculateSubDialState formats time and determines daylight`() {
        val noonUtc = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneId.of("UTC"))
        val subDial = ChronometerEngine.calculateSubDialState("London", "Europe/London", noonUtc)

        assertNotNull(subDial.title)
        assertEquals("Europe/London", subDial.zoneId)
        assertTrue(subDial.timeFormatted.isNotEmpty())
        assertTrue(subDial.isDaylight) // 12:00 or 13:00 is daylight
    }

    @Test
    fun `bezel cities list is non-empty with valid offsets`() {
        assertTrue(ChronometerEngine.BEZEL_CITIES.isNotEmpty())
        ChronometerEngine.BEZEL_CITIES.forEach { city ->
            assertNotNull(city.code)
            assertNotNull(city.cityName)
            assertTrue(city.utcOffsetHours in -12.0..14.0)
        }
    }
}
