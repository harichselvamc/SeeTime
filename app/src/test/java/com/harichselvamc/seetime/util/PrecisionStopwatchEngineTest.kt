package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrecisionStopwatchEngineTest {

    @Test
    fun `formatElapsed formats minutes and hours with millisecond precision`() {
        assertEquals("00:00.000", PrecisionStopwatchEngine.formatElapsed(0L))
        assertEquals("01:05.432", PrecisionStopwatchEngine.formatElapsed(65432L))
        assertEquals("01:01:05.432", PrecisionStopwatchEngine.formatElapsed(3665432L))
    }

    @Test
    fun `evaluateLapTelemetry accurately calculates lap durations, deltas, and fastest slowest laps`() {
        // Splits: Lap 1 ends at 10.0s, Lap 2 ends at 22.0s (12.0s), Lap 3 ends at 31.0s (9.0s)
        val splits = listOf(10000L, 22000L, 31000L)
        val summary = PrecisionStopwatchEngine.evaluateLapTelemetry(splits)

        assertEquals(3, summary.lapsCount)
        assertEquals(31000L, summary.totalElapsedTimeMs)
        assertEquals("00:31.000", summary.formattedTotalTime)
        assertEquals(9000L, summary.fastestLapMs)
        assertEquals(12000L, summary.slowestLapMs)
        assertEquals(10333L, summary.averageLapMs)

        // Telemetry laps list is reversed (newest first: Lap 3, Lap 2, Lap 1)
        val lap3 = summary.laps.find { it.lapNumber == 3 }
        assertNotNull(lap3)
        assertEquals(9000L, lap3!!.lapDurationMs)
        assertTrue(lap3.isFastest)
        assertFalse(lap3.isSlowest)

        val lap2 = summary.laps.find { it.lapNumber == 2 }
        assertNotNull(lap2)
        assertEquals(12000L, lap2!!.lapDurationMs)
        assertTrue(lap2.isSlowest)
        assertFalse(lap2.isFastest)
    }

    @Test
    fun `generateLapCsv exports valid CSV telemetry data`() {
        val splits = listOf(5000L, 11000L)
        val summary = PrecisionStopwatchEngine.evaluateLapTelemetry(splits)
        val csv = PrecisionStopwatchEngine.generateLapCsv(summary)

        assertTrue(csv.contains("Lap,Lap Duration (ms)"))
        assertTrue(csv.contains("Total Laps,2"))
        assertTrue(csv.contains("FASTEST") || csv.contains("SLOWEST"))
    }
}
