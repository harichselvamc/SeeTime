package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class DstPredictorEngineTest {

    @Test
    fun `calculateZoneDstStatus identifies DST active regions and upcoming shifts`() {
        // Summer instant (July 15) in London (BST active, UTC+1)
        val summerInstant = ZonedDateTime.of(2026, 7, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
        val londonSummer = DstPredictorEngine.calculateZoneDstStatus("Europe/London", referenceInstant = summerInstant)

        assertTrue(londonSummer.hasDst)
        assertTrue(londonSummer.isDstActiveNow)
        assertEquals(1.0, londonSummer.currentOffsetHours, 0.01)
        assertEquals(0.0, londonSummer.standardOffsetHours, 0.01)
        assertEquals(DstShiftType.FALL_BACK_PLUS_ONE_HOUR_SLEEP, londonSummer.shiftType)

        // Winter instant (January 15) in New York (EST standard, UTC-5)
        val winterInstant = ZonedDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
        val nyWinter = DstPredictorEngine.calculateZoneDstStatus("America/New_York", referenceInstant = winterInstant)

        assertTrue(nyWinter.hasDst)
        assertFalse(nyWinter.isDstActiveNow)
        assertEquals(-5.0, nyWinter.currentOffsetHours, 0.01)
        assertEquals(DstShiftType.SPRING_FORWARD_MINUS_ONE_HOUR_SLEEP, nyWinter.shiftType)
    }

    @Test
    fun `calculateZoneDstStatus correctly recognizes permanent standard No-DST regions`() {
        val tokyoStatus = DstPredictorEngine.calculateZoneDstStatus("Asia/Tokyo")
        assertFalse(tokyoStatus.hasDst)
        assertFalse(tokyoStatus.isDstActiveNow)
        assertEquals(9.0, tokyoStatus.currentOffsetHours, 0.01)
        assertEquals(DstShiftType.NO_DST, tokyoStatus.shiftType)

        val kolkataStatus = DstPredictorEngine.calculateZoneDstStatus("Asia/Kolkata")
        assertFalse(kolkataStatus.hasDst)
        assertEquals(5.5, kolkataStatus.currentOffsetHours, 0.01)
        assertEquals(DstShiftType.NO_DST, kolkataStatus.shiftType)
    }

    @Test
    fun `monitored regions list is populated with global capitals`() {
        assertTrue(DstPredictorEngine.MONITORED_REGIONS.isNotEmpty())
        val zoneIds = DstPredictorEngine.MONITORED_REGIONS.map { it.first }

        assertTrue(zoneIds.contains("Europe/London"))
        assertTrue(zoneIds.contains("America/New_York"))
        assertTrue(zoneIds.contains("Asia/Tokyo"))
        assertTrue(zoneIds.contains("Asia/Kolkata"))
    }
}
