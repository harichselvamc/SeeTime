package com.harichselvamc.seetime.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class DateDifferenceResult(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val remainingDaysOfWeek: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,
    val percentageOfYear: Double,
    val formattedDetailedString: String
)

data class BusinessDaysResult(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalCalendarDays: Long,
    val businessDaysCount: Long,
    val weekendDaysCount: Long,
    val publicHolidaysCount: Long,
    val holidaysList: List<PublicHoliday>,
    val workingHoursEstimate: Long,
    val countryCode: String
)

data class AddSubtractDateResult(
    val originalDate: LocalDate,
    val calculatedDate: LocalDate,
    val dayOfWeek: DayOfWeek,
    val isWeekend: Boolean,
    val isHoliday: Boolean,
    val holidayName: String?,
    val deltaDays: Long
)

object DateTimeCalculatorEngine {

    /**
     * Calculates the exact calendar difference and comprehensive time units between [start] and [end].
     */
    fun calculateDifference(start: LocalDate, end: LocalDate): DateDifferenceResult {
        val isNegative = end.isBefore(start)
        val from = if (isNegative) end else start
        val to = if (isNegative) start else end

        val period = Period.between(from, to)
        val totalDays = ChronoUnit.DAYS.between(from, to)
        val totalWeeks = totalDays / 7
        val remainingDaysOfWeek = totalDays % 7
        val totalHours = totalDays * 24
        val totalMinutes = totalHours * 60
        val totalSeconds = totalMinutes * 60
        val percentageOfYear = (totalDays.toDouble() / 365.2425) * 100.0

        val formatted = when {
            period.years > 0 && period.months > 0 -> "${period.years} years, ${period.months} months, ${period.days} days"
            period.years > 0 -> "${period.years} years, ${period.days} days"
            period.months > 0 -> "${period.months} months, ${period.days} days"
            else -> "$totalDays days"
        }

        return DateDifferenceResult(
            startDate = start,
            endDate = end,
            years = if (isNegative) -period.years else period.years,
            months = if (isNegative) -period.months else period.months,
            days = if (isNegative) -period.days else period.days,
            totalDays = if (isNegative) -totalDays else totalDays,
            totalWeeks = totalWeeks,
            remainingDaysOfWeek = remainingDaysOfWeek,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds,
            percentageOfYear = percentageOfYear,
            formattedDetailedString = formatted
        )
    }

    /**
     * Calculates working business days (Monday-Friday) between [start] and [end],
     * excluding weekends and regional statutory public holidays from [countryCode].
     */
    fun calculateBusinessDays(
        start: LocalDate,
        end: LocalDate,
        countryCode: String = "US",
        includeEndDay: Boolean = true
    ): BusinessDaysResult {
        val isReversed = end.isBefore(start)
        val from = if (isReversed) end else start
        val to = if (isReversed) start else end

        val startYear = from.year
        val endYear = to.year
        val allHolidays = mutableListOf<PublicHoliday>()
        for (y in startYear..endYear) {
            allHolidays.addAll(HolidayCalculationEngine.getHolidaysForCountry(countryCode, y))
        }

        val holidayDateMap = allHolidays.associateBy { it.date }

        var businessDays = 0L
        var weekendDays = 0L
        var publicHolidaysOnWorkdays = 0L
        val holidaysEncountered = mutableListOf<PublicHoliday>()

        var current = from
        val lastDay = if (includeEndDay) to else to.minusDays(1)
        val totalDays = if (lastDay.isBefore(from)) 0L else ChronoUnit.DAYS.between(from, lastDay) + 1L

        while (!current.isAfter(lastDay)) {
            val isWeekend = current.dayOfWeek == DayOfWeek.SATURDAY || current.dayOfWeek == DayOfWeek.SUNDAY
            if (isWeekend) {
                weekendDays++
            } else {
                val holiday = holidayDateMap[current]
                if (holiday != null && holiday.isOffWork) {
                    publicHolidaysOnWorkdays++
                    holidaysEncountered.add(holiday)
                } else {
                    businessDays++
                }
            }
            current = current.plusDays(1)
        }

        return BusinessDaysResult(
            startDate = start,
            endDate = end,
            totalCalendarDays = totalDays,
            businessDaysCount = businessDays,
            weekendDaysCount = weekendDays,
            publicHolidaysCount = publicHolidaysOnWorkdays,
            holidaysList = holidaysEncountered,
            workingHoursEstimate = businessDays * 8, // 8-hour workday standard
            countryCode = countryCode
        )
    }

    /**
     * Adds calendar time intervals (years, months, weeks, days) to [start].
     */
    fun addTime(
        start: LocalDate,
        years: Int = 0,
        months: Int = 0,
        weeks: Int = 0,
        days: Int = 0,
        countryCode: String = "US"
    ): AddSubtractDateResult {
        val calculated = start
            .plusYears(years.toLong())
            .plusMonths(months.toLong())
            .plusWeeks(weeks.toLong())
            .plusDays(days.toLong())

        val delta = ChronoUnit.DAYS.between(start, calculated)
        val isWeekend = calculated.dayOfWeek == DayOfWeek.SATURDAY || calculated.dayOfWeek == DayOfWeek.SUNDAY

        val holidays = HolidayCalculationEngine.getHolidaysForCountry(countryCode, calculated.year)
        val holiday = holidays.find { it.date == calculated }

        return AddSubtractDateResult(
            originalDate = start,
            calculatedDate = calculated,
            dayOfWeek = calculated.dayOfWeek,
            isWeekend = isWeekend,
            isHoliday = holiday != null,
            holidayName = holiday?.name,
            deltaDays = delta
        )
    }

    /**
     * Subtracts calendar time intervals (years, months, weeks, days) from [start].
     */
    fun subtractTime(
        start: LocalDate,
        years: Int = 0,
        months: Int = 0,
        weeks: Int = 0,
        days: Int = 0,
        countryCode: String = "US"
    ): AddSubtractDateResult {
        return addTime(
            start = start,
            years = -years,
            months = -months,
            weeks = -weeks,
            days = -days,
            countryCode = countryCode
        )
    }

    /**
     * Adds [businessDaysToAdd] working days to [start], skipping Saturdays, Sundays, and regional public holidays.
     */
    fun addBusinessDays(
        start: LocalDate,
        businessDaysToAdd: Int,
        countryCode: String = "US"
    ): AddSubtractDateResult {
        if (businessDaysToAdd == 0) {
            val isWeekend = start.dayOfWeek == DayOfWeek.SATURDAY || start.dayOfWeek == DayOfWeek.SUNDAY
            val holiday = HolidayCalculationEngine.getHolidaysForCountry(countryCode, start.year).find { it.date == start }
            return AddSubtractDateResult(
                originalDate = start,
                calculatedDate = start,
                dayOfWeek = start.dayOfWeek,
                isWeekend = isWeekend,
                isHoliday = holiday != null,
                holidayName = holiday?.name,
                deltaDays = 0
            )
        }

        val isForward = businessDaysToAdd > 0
        var remaining = abs(businessDaysToAdd)
        var current = start

        while (remaining > 0) {
            current = if (isForward) current.plusDays(1) else current.minusDays(1)
            val isWeekend = current.dayOfWeek == DayOfWeek.SATURDAY || current.dayOfWeek == DayOfWeek.SUNDAY
            if (!isWeekend) {
                val holidays = HolidayCalculationEngine.getHolidaysForCountry(countryCode, current.year)
                val isHoliday = holidays.any { it.date == current && it.isOffWork }
                if (!isHoliday) {
                    remaining--
                }
            }
        }

        val delta = ChronoUnit.DAYS.between(start, current)
        val isWeekend = current.dayOfWeek == DayOfWeek.SATURDAY || current.dayOfWeek == DayOfWeek.SUNDAY
        val holiday = HolidayCalculationEngine.getHolidaysForCountry(countryCode, current.year).find { it.date == current }

        return AddSubtractDateResult(
            originalDate = start,
            calculatedDate = current,
            dayOfWeek = current.dayOfWeek,
            isWeekend = isWeekend,
            isHoliday = holiday != null,
            holidayName = holiday?.name,
            deltaDays = delta
        )
    }
}
