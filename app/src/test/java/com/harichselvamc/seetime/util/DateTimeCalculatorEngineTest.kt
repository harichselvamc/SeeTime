package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateTimeCalculatorEngineTest {

    @Test
    fun `calculateDifference computes exact years months days and hours`() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 7, 15)

        val result = DateTimeCalculatorEngine.calculateDifference(start, end)
        assertEquals(195L, result.totalDays)
        assertEquals(0, result.years)
        assertEquals(6, result.months)
        assertEquals(14, result.days)
        assertEquals(27L, result.totalWeeks)
        assertEquals(6L, result.remainingDaysOfWeek)
        assertEquals(195L * 24L, result.totalHours)
        assertEquals(195L * 24L * 60L, result.totalMinutes)
    }

    @Test
    fun `calculateDifference handles negative reverse order gracefully`() {
        val start = LocalDate.of(2026, 12, 31)
        val end = LocalDate.of(2026, 1, 1)

        val result = DateTimeCalculatorEngine.calculateDifference(start, end)
        assertEquals(-364L, result.totalDays)
    }

    @Test
    fun `calculateBusinessDays excludes weekends and statutory public holidays`() {
        // July 1, 2026 (Wed) to July 10, 2026 (Fri) in US
        // July 4 (Sat) - Independence Day
        // Total calendar days = 10 (July 1..10)
        // Weekend days = 2 (July 4 Sat, July 5 Sun)
        // Business days = 8 (July 1, 2, 3, 6, 7, 8, 9, 10)
        val start = LocalDate.of(2026, 7, 1)
        val end = LocalDate.of(2026, 7, 10)

        val result = DateTimeCalculatorEngine.calculateBusinessDays(start, end, "US", includeEndDay = true)
        assertEquals(10L, result.totalCalendarDays)
        assertEquals(2L, result.weekendDaysCount)
        assertEquals(8L, result.businessDaysCount)
        assertEquals(64L, result.workingHoursEstimate)
    }

    @Test
    fun `calculateBusinessDays excludes weekday Christmas in US`() {
        // Dec 21, 2026 (Mon) to Dec 28, 2026 (Mon)
        // Dec 25 (Fri) is Christmas Day (Public Holiday on workday)
        // Workdays: Dec 21, 22, 23, 24, 28 = 5 business days
        // Weekend: Dec 26, 27 = 2 weekend days
        // Holiday: Dec 25 = 1 holiday on workday
        val start = LocalDate.of(2026, 12, 21)
        val end = LocalDate.of(2026, 12, 28)

        val result = DateTimeCalculatorEngine.calculateBusinessDays(start, end, "US", includeEndDay = true)
        assertEquals(8L, result.totalCalendarDays)
        assertEquals(2L, result.weekendDaysCount)
        assertEquals(1L, result.publicHolidaysCount)
        assertEquals(5L, result.businessDaysCount)
        assertTrue(result.holidaysList.any { it.name.contains("Christmas") })
    }

    @Test
    fun `addTime and subtractTime calculate calendar shifts`() {
        val start = LocalDate.of(2026, 3, 1)
        val added = DateTimeCalculatorEngine.addTime(start, years = 1, months = 2, days = 10)
        assertEquals(LocalDate.of(2027, 5, 11), added.calculatedDate)

        val subtracted = DateTimeCalculatorEngine.subtractTime(start, days = 5)
        assertEquals(LocalDate.of(2026, 2, 24), subtracted.calculatedDate)
    }

    @Test
    fun `addBusinessDays skips weekends and regional public holidays`() {
        // Start Friday July 3, 2026. Add 1 business day -> Monday July 6, 2026
        val start = LocalDate.of(2026, 7, 3)
        val result = DateTimeCalculatorEngine.addBusinessDays(start, 1, "US")
        assertEquals(LocalDate.of(2026, 7, 6), result.calculatedDate)
        assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
        assertFalse(result.isWeekend)

        // Add 5 business days from Friday July 3 -> Mon 6, Tue 7, Wed 8, Thu 9, Fri 10
        val result5 = DateTimeCalculatorEngine.addBusinessDays(start, 5, "US")
        assertEquals(LocalDate.of(2026, 7, 10), result5.calculatedDate)
    }
}
