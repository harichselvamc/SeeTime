package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class EpochConverterEngineTest {

    @Test
    fun `convertEpochMillis produces exact astronomical and computational epochs on Unix zero`() {
        val formats = EpochConverterEngine.convertEpochMillis(0L, ZoneId.of("UTC"))

        assertEquals(0L, formats.unixSeconds)
        assertEquals(0L, formats.unixMilliseconds)
        assertEquals(2440587.5, formats.julianDate, 0.001)
        assertEquals(40587.0, formats.modifiedJulianDate, 0.001)
        assertEquals(25569.0, formats.excelSerial, 0.001)
        assertTrue(formats.gregorianUtc.contains("1970-01-01 00:00:00"))
        assertEquals("0x0", formats.unixHexadecimal)
    }

    @Test
    fun `convertEpochMillis converts modern 2026 timestamp with full format coverage`() {
        // 1788307200s = 2026-09-02 00:00:00 UTC = 0x6A976700
        val millis = 1788307200000L
        val formats = EpochConverterEngine.convertEpochMillis(millis, ZoneId.of("UTC"))

        assertEquals(1788307200L, formats.unixSeconds)
        assertEquals(1788307200000L, formats.unixMilliseconds)
        assertEquals("0x6A976700", formats.unixHexadecimal)
        assertTrue(formats.gregorianUtc.contains("2026-09-02"))
        assertTrue(formats.isoOrdinalDate.startsWith("2026-"))
        assertTrue(formats.gpsWeek > 2000)
    }

    @Test
    fun `parseInputToEpochMillis accurately parses diverse timestamp representations`() {
        val expectedMillis = 1788307200000L

        // 1. Seconds string
        assertEquals(expectedMillis, EpochConverterEngine.parseInputToEpochMillis("1788307200"))

        // 2. Milliseconds string
        assertEquals(expectedMillis, EpochConverterEngine.parseInputToEpochMillis("1788307200000"))

        // 3. Hexadecimal (1788307200 = 0x6A976700)
        assertEquals(expectedMillis, EpochConverterEngine.parseInputToEpochMillis("0x6A976700"))
        assertEquals(expectedMillis, EpochConverterEngine.parseInputToEpochMillis("0X6A976700"))

        // 4. ISO Date
        val parsedDate = EpochConverterEngine.parseInputToEpochMillis("2026-09-02")
        assertNotNull(parsedDate)
        assertEquals(expectedMillis, parsedDate)
    }

    @Test
    fun `calculateMilestoneCountdowns calculates Year 2038 and 2-Billion targets`() {
        val currentSec = 1788307200L // Sep 2026
        val milestones = EpochConverterEngine.calculateMilestoneCountdowns(currentSec)

        assertEquals(2, milestones.size)
        val y2k38 = milestones.find { it.title.contains("2038") }
        assertNotNull(y2k38)
        assertTrue(y2k38!!.daysRemaining > 4000)
        assertEquals(2147483647L, y2k38.targetTimestampSeconds)

        val twoBil = milestones.find { it.title.contains("2 Billion") }
        assertNotNull(twoBil)
        assertTrue(twoBil!!.daysRemaining > 2000)
        assertEquals(2000000000L, twoBil.targetTimestampSeconds)
    }
}
