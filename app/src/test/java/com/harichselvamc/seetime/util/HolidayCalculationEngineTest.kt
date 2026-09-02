package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HolidayCalculationEngineTest {

    @Test
    fun `calculateEasterSunday computes correct astronomical Easter dates`() {
        assertEquals(LocalDate.of(2024, 3, 31), HolidayCalculationEngine.calculateEasterSunday(2024))
        assertEquals(LocalDate.of(2025, 4, 20), HolidayCalculationEngine.calculateEasterSunday(2025))
        assertEquals(LocalDate.of(2026, 4, 5), HolidayCalculationEngine.calculateEasterSunday(2026))
    }

    @Test
    fun `relative weekday calculations compute precise calendar dates`() {
        // US Thanksgiving: 4th Thursday of November 2026 is Nov 26, 2026
        val thanksgiving2026 = HolidayCalculationEngine.nthDayOfWeekInMonth(2026, 11, DayOfWeek.THURSDAY, 4)
        assertEquals(LocalDate.of(2026, 11, 26), thanksgiving2026)

        // US Memorial Day: Last Monday of May 2026 is May 25, 2026
        val memorialDay2026 = HolidayCalculationEngine.lastDayOfWeekInMonth(2026, 5, DayOfWeek.MONDAY)
        assertEquals(LocalDate.of(2026, 5, 25), memorialDay2026)

        // Japan Coming of Age Day: 2nd Monday of January 2026 is Jan 12, 2026
        val comingOfAge2026 = HolidayCalculationEngine.nthDayOfWeekInMonth(2026, 1, DayOfWeek.MONDAY, 2)
        assertEquals(LocalDate.of(2026, 1, 12), comingOfAge2026)
    }

    @Test
    fun `getHolidaysForCountry returns statutory holidays for major countries`() {
        val usHolidays = HolidayCalculationEngine.getHolidaysForCountry("US", 2026)
        assertTrue(usHolidays.any { it.name.contains("Thanksgiving") })
        assertTrue(usHolidays.any { it.name.contains("Independence Day") })
        assertTrue(usHolidays.any { it.name.contains("Juneteenth") })

        val jpHolidays = HolidayCalculationEngine.getHolidaysForCountry("JP", 2026)
        assertTrue(jpHolidays.any { it.name.contains("Shōwa Day") })
        assertTrue(jpHolidays.any { it.name.contains("Constitution Memorial Day") })
        assertTrue(jpHolidays.any { it.name.contains("Mountain Day") })

        val gbHolidays = HolidayCalculationEngine.getHolidaysForCountry("GB", 2026)
        assertTrue(gbHolidays.any { it.name.contains("Spring Bank Holiday") })
        assertTrue(gbHolidays.any { it.name.contains("Boxing Day") })
    }

    @Test
    fun `checkMeetingHolidayConflicts detects cross-timezone conflicts`() {
        // July 4th in US
        val july4 = LocalDate.of(2026, 7, 4)
        val conflicts = HolidayCalculationEngine.checkMeetingHolidayConflicts(july4, listOf("US", "GB"))

        assertTrue(conflicts.any { it.countryCode == "US" && it.name.contains("Independence Day") })

        // Regular business day (e.g. Wednesday Oct 14, 2026)
        val regularDay = LocalDate.of(2026, 10, 14)
        val noConflicts = HolidayCalculationEngine.checkMeetingHolidayConflicts(regularDay, listOf("US", "GB", "DE"))
        assertTrue(noConflicts.isEmpty())
    }

    @Test
    fun `getNextUpcomingHoliday finds next valid date`() {
        val today = LocalDate.of(2026, 6, 1)
        val nextHoliday = HolidayCalculationEngine.getNextUpcomingHoliday("US", today)

        assertNotNull(nextHoliday)
        assertEquals(LocalDate.of(2026, 6, 19), nextHoliday!!.date)
        assertEquals("Juneteenth", nextHoliday.name)
    }
}
