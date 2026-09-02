package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class NauticalWatchEngineTest {

    @Test
    fun `calculateBellCount calculates standard 4-hour watch bells`() {
        assertEquals(8, NauticalWatchEngine.calculateBellCount(0, 0))
        assertEquals(1, NauticalWatchEngine.calculateBellCount(0, 30))
        assertEquals(2, NauticalWatchEngine.calculateBellCount(1, 0))
        assertEquals(3, NauticalWatchEngine.calculateBellCount(1, 30))
        assertEquals(4, NauticalWatchEngine.calculateBellCount(2, 0))
        assertEquals(5, NauticalWatchEngine.calculateBellCount(2, 30))
        assertEquals(6, NauticalWatchEngine.calculateBellCount(3, 0))
        assertEquals(7, NauticalWatchEngine.calculateBellCount(3, 30))
        assertEquals(8, NauticalWatchEngine.calculateBellCount(4, 0))
    }

    @Test
    fun `calculateBellCount respects traditional 2-hour dog watch bell strikes`() {
        // First Dog Watch (16:00 to 18:00)
        assertEquals(8, NauticalWatchEngine.calculateBellCount(16, 0)) // Change of watch
        assertEquals(1, NauticalWatchEngine.calculateBellCount(16, 30))
        assertEquals(2, NauticalWatchEngine.calculateBellCount(17, 0))
        assertEquals(3, NauticalWatchEngine.calculateBellCount(17, 30))
        assertEquals(4, NauticalWatchEngine.calculateBellCount(18, 0)) // End of First Dog

        // Last Dog Watch (18:00 to 20:00)
        assertEquals(1, NauticalWatchEngine.calculateBellCount(18, 30))
        assertEquals(2, NauticalWatchEngine.calculateBellCount(19, 0))
        assertEquals(3, NauticalWatchEngine.calculateBellCount(19, 30))
        assertEquals(8, NauticalWatchEngine.calculateBellCount(20, 0)) // 8 bells at 20:00 (watch change)
    }

    @Test
    fun `getWatchType accurately maps all 7 traditional maritime watches`() {
        assertEquals(NauticalWatchType.MIDDLE_WATCH, NauticalWatchEngine.getWatchType(2))
        assertEquals(NauticalWatchType.MORNING_WATCH, NauticalWatchEngine.getWatchType(6))
        assertEquals(NauticalWatchType.FORENOON_WATCH, NauticalWatchEngine.getWatchType(10))
        assertEquals(NauticalWatchType.AFTERNOON_WATCH, NauticalWatchEngine.getWatchType(14))
        assertEquals(NauticalWatchType.FIRST_DOG_WATCH, NauticalWatchEngine.getWatchType(17))
        assertEquals(NauticalWatchType.LAST_DOG_WATCH, NauticalWatchEngine.getWatchType(19))
        assertEquals(NauticalWatchType.FIRST_WATCH, NauticalWatchEngine.getWatchType(22))
    }

    @Test
    fun `bellsToNotation and phonetic text produce paired bell rhythms`() {
        // 6 bells = 3 pairs
        val notation6 = NauticalWatchEngine.bellsToNotation(6)
        assertTrue(notation6.contains("🔔🔔  🔔🔔  🔔🔔"))

        val phonetic6 = NauticalWatchEngine.bellsToPhonetic(6)
        assertEquals("ding-ding, ding-ding, ding-ding", phonetic6)

        // 5 bells = 2 pairs + 1 single
        val notation5 = NauticalWatchEngine.bellsToNotation(5)
        assertTrue(notation5.contains("🔔🔔  🔔🔔  🔔"))

        val phonetic5 = NauticalWatchEngine.bellsToPhonetic(5)
        assertEquals("ding-ding, ding-ding, ding", phonetic5)
    }

    @Test
    fun `generate24HourTimetable produces 48 half-hour entries`() {
        val timetable = NauticalWatchEngine.generate24HourTimetable()
        assertEquals(48, timetable.size)
        assertEquals("00:00", timetable[0].timeFormatted)
        assertEquals("23:30", timetable[47].timeFormatted)
    }
}
