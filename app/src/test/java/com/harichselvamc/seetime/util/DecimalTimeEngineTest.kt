package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DecimalTimeEngineTest {

    @Test
    fun `calculateSwatchBeats produces exact BMT synchronized beats`() {
        // Noon BMT (12:00:00 UTC+1 = 11:00:00 UTC) should be @500
        val noonBmt = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.ofHours(1))
        val details = DecimalTimeEngine.calculateSwatchBeats(noonBmt)

        assertEquals(500, details.integerBeats)
        assertTrue(details.formattedString.startsWith("@500"))
        assertEquals(50.0f, details.dayProgressPercent, 0.01f)

        // Midnight BMT (00:00:00 UTC+1) should be @000
        val midnightBmt = ZonedDateTime.of(2026, 9, 2, 0, 0, 0, 0, ZoneOffset.ofHours(1))
        val midDetails = DecimalTimeEngine.calculateSwatchBeats(midnightBmt)

        assertEquals(0, midDetails.integerBeats)
        assertTrue(midDetails.formattedString.startsWith("@000"))
    }

    @Test
    fun `calculateMetricDecimalTime computes French revolutionary decimal time`() {
        // Midnight -> 0:00:00
        val midnight = ZonedDateTime.of(2026, 9, 2, 0, 0, 0, 0, ZoneOffset.UTC)
        val decMidnight = DecimalTimeEngine.calculateMetricDecimalTime(midnight)
        assertEquals(0, decMidnight.decimalHours)
        assertEquals(0, decMidnight.decimalMinutes)
        assertEquals(0, decMidnight.decimalSeconds)

        // Standard Noon (12:00:00) -> 5:00:00 decimal time
        val noon = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC)
        val decNoon = DecimalTimeEngine.calculateMetricDecimalTime(noon)
        assertEquals(5, decNoon.decimalHours)
        assertEquals(0, decNoon.decimalMinutes)
        assertEquals(0, decNoon.decimalSeconds)

        // 18:00:00 -> 7:50:00 decimal time
        val evening = ZonedDateTime.of(2026, 9, 2, 18, 0, 0, 0, ZoneOffset.UTC)
        val decEvening = DecimalTimeEngine.calculateMetricDecimalTime(evening)
        assertEquals(7, decEvening.decimalHours)
        assertEquals(50, decEvening.decimalMinutes)
        assertEquals(0, decEvening.decimalSeconds)
    }

    @Test
    fun `beatsToUtcTime converts beats back to UTC clock time`() {
        val utcNoon = DecimalTimeEngine.beatsToUtcTime(500.0)
        assertEquals(LocalTime.of(11, 0, 0), utcNoon)

        val utcMidnight = DecimalTimeEngine.beatsToUtcTime(0.0)
        assertEquals(LocalTime.of(23, 0, 0), utcMidnight)
    }

    @Test
    fun `popular beat presets contain valid internet times`() {
        assertTrue(DecimalTimeEngine.POPULAR_BEAT_PRESETS.isNotEmpty())
        DecimalTimeEngine.POPULAR_BEAT_PRESETS.forEach { preset ->
            assertNotNull(preset.title)
            assertTrue(preset.beats in 0..999)
            assertNotNull(preset.bmtUtcTime)
        }
    }
}
