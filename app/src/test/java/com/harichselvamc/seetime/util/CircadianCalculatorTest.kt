package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CircadianCalculatorTest {

    @Test
    fun `calculateWakeTimes produces 90 minute ultradian intervals with 15 minute latency`() {
        val bedtime = LocalTime.of(23, 0) // 11:00 PM
        val recommendations = CircadianCalculator.calculateWakeTimes(bedtime = bedtime, latencyMinutes = 15)

        assertEquals(4, recommendations.size)

        // 3 cycles (4.5h) + 15m = 4h 45m after 23:00 -> 03:45
        val rec3 = recommendations.find { it.cycles == 3 }
        assertNotNull(rec3)
        assertEquals(LocalTime.of(3, 45), rec3!!.targetTime)

        // 4 cycles (6.0h) + 15m -> 05:15
        val rec4 = recommendations.find { it.cycles == 4 }
        assertNotNull(rec4)
        assertEquals(LocalTime.of(5, 15), rec4!!.targetTime)

        // 5 cycles (7.5h) + 15m -> 06:45 (Optimal recommendation)
        val rec5 = recommendations.find { it.cycles == 5 }
        assertNotNull(rec5)
        assertEquals(LocalTime.of(6, 45), rec5!!.targetTime)
        assertTrue(rec5.isRecommended)

        // 6 cycles (9.0h) + 15m -> 08:15
        val rec6 = recommendations.find { it.cycles == 6 }
        assertNotNull(rec6)
        assertEquals(LocalTime.of(8, 15), rec6!!.targetTime)
    }

    @Test
    fun `calculateBedtimesForWakeTarget computes accurate bedtimes`() {
        val wakeTarget = LocalTime.of(7, 0) // 07:00 AM
        val recommendations = CircadianCalculator.calculateBedtimesForWakeTarget(wakeTarget, latencyMinutes = 15)

        assertEquals(4, recommendations.size)

        // 5 cycles (7.5h + 15m = 7h 45m before 07:00 -> 23:15 / 11:15 PM)
        val rec5 = recommendations.find { it.cycles == 5 }
        assertNotNull(rec5)
        assertEquals(LocalTime.of(23, 15), rec5!!.targetTime)

        // 4 cycles (6h + 15m = 6h 15m before 07:00 -> 00:45)
        val rec4 = recommendations.find { it.cycles == 4 }
        assertNotNull(rec4)
        assertEquals(LocalTime.of(0, 45), rec4!!.targetTime)
    }

    @Test
    fun `calculateCaffeineCutoff is 9 hours before planned bedtime`() {
        val bedtime = LocalTime.of(23, 0) // 11:00 PM
        val cutoff = CircadianCalculator.calculateCaffeineCutoff(bedtime)

        assertEquals(LocalTime.of(14, 0), cutoff) // 2:00 PM
    }

    @Test
    fun `calculateSolarTimes computes realistic sunrise and sunset`() {
        // London coordinates on Equinox (March 21 or Sep 21)
        val lat = 51.5074
        val lon = -0.1278
        val date = LocalDate.of(2026, 9, 21)
        val zoneId = ZoneId.of("Europe/London")

        val solarTimes = CircadianCalculator.calculateSolarTimes(lat, lon, date, zoneId)

        assertNotNull(solarTimes.sunrise)
        assertNotNull(solarTimes.sunset)
        assertNotNull(solarTimes.solarNoon)
        assertTrue(solarTimes.daylightMinutes > 600) // ~12 hours on equinox
        assertTrue(solarTimes.sunrise.isBefore(solarTimes.sunset))
    }

    @Test
    fun `calculateJetLagPreAdaptation generates 4 day shifting plan`() {
        val origin = "America/New_York"
        val destination = "Europe/London" // +5 hours Eastward

        val plan = CircadianCalculator.calculateJetLagPreAdaptation(origin, destination)

        assertEquals(4, plan.size)
        assertEquals(1, plan[0].dayNumber)
        assertEquals(4, plan[3].dayNumber)

        // Day 1 shift: 45 min earlier bedtime (23:00 -> 22:15)
        assertEquals(LocalTime.of(22, 15), plan[0].recommendedBedtime)
        // Day 4 shift: 180 min (3h) earlier bedtime (23:00 -> 20:00)
        assertEquals(LocalTime.of(20, 0), plan[3].recommendedBedtime)
        assertTrue(plan[0].lightExposureWindow.contains("Morning"))
    }
}
