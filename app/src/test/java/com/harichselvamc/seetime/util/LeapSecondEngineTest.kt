package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class LeapSecondEngineTest {

    @Test
    fun `calculateMultiStandardTime computes exact TAI and GPS offsets from UTC`() {
        val utcZdt = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC)
        val multiTime = LeapSecondEngine.calculateMultiStandardTime(utcZdt)

        assertEquals(37L, multiTime.taiUtcOffsetSeconds)
        assertEquals(18L, multiTime.gpsUtcOffsetSeconds)
        assertEquals(-0.065, multiTime.dut1Seconds, 0.001)

        assertTrue(multiTime.utcFormatted.contains("12:00:00"))
        assertTrue(multiTime.taiFormatted.contains("12:00:37")) // +37s ahead
        assertTrue(multiTime.gpsFormatted.contains("12:00:18")) // +18s ahead
    }

    @Test
    fun `getLeapSecondSimulationFrame generates 23-59-60 leap transition`() {
        val frame2 = LeapSecondEngine.getLeapSecondSimulationFrame(2)
        assertEquals("23:59:60", frame2.first)
        assertTrue(frame2.second.contains("LEAP SECOND"))

        val frame3 = LeapSecondEngine.getLeapSecondSimulationFrame(3)
        assertEquals("00:00:00", frame3.first)
    }

    @Test
    fun `historical leap seconds archive contains all 27 IERS insertions`() {
        assertEquals(27, LeapSecondEngine.HISTORICAL_LEAP_SECONDS.size)

        val firstLeap = LeapSecondEngine.HISTORICAL_LEAP_SECONDS.first()
        assertEquals("1972-06-30", firstLeap.dateStr)
        assertEquals(11, firstLeap.taiUtcOffsetSeconds)

        val lastLeap = LeapSecondEngine.HISTORICAL_LEAP_SECONDS.last()
        assertEquals("2016-12-31", lastLeap.dateStr)
        assertEquals(37, lastLeap.taiUtcOffsetSeconds)
    }
}
