package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HistoricalLeapSecond(
    val dateStr: String,
    val date: LocalDate,
    val taiUtcOffsetSeconds: Int,
    val note: String
)

data class MultiStandardTimeState(
    val utcFormatted: String,
    val taiFormatted: String,
    val gpsFormatted: String,
    val ut1Formatted: String,
    val taiUtcOffsetSeconds: Long,
    val gpsUtcOffsetSeconds: Long,
    val dut1Seconds: Double
)

object LeapSecondEngine {

    const val CURRENT_TAI_OFFSET_SECONDS = 37L // TAI = UTC + 37s (since Jan 1, 2017)
    const val CURRENT_GPS_OFFSET_SECONDS = 18L // GPS = UTC + 18s (GPS = TAI - 19s)
    const val CURRENT_DUT1_SECONDS = -0.065 // UT1 - UTC (~ -65 ms)
    const val EARTH_DECELERATION_MS_CENTURY = 1.8 // +1.8 ms/century length of day increase

    val HISTORICAL_LEAP_SECONDS = listOf(
        HistoricalLeapSecond("1972-06-30", LocalDate.of(1972, 6, 30), 11, "First historical leap second under UTC standard system."),
        HistoricalLeapSecond("1972-12-31", LocalDate.of(1972, 12, 31), 12, "Year-end alignment."),
        HistoricalLeapSecond("1973-12-31", LocalDate.of(1973, 12, 31), 13, "Year-end alignment."),
        HistoricalLeapSecond("1974-12-31", LocalDate.of(1974, 12, 31), 14, "Year-end alignment."),
        HistoricalLeapSecond("1975-12-31", LocalDate.of(1975, 12, 31), 15, "Year-end alignment."),
        HistoricalLeapSecond("1976-12-31", LocalDate.of(1976, 12, 31), 16, "Year-end alignment."),
        HistoricalLeapSecond("1977-12-31", LocalDate.of(1977, 12, 31), 17, "Year-end alignment."),
        HistoricalLeapSecond("1978-12-31", LocalDate.of(1978, 12, 31), 18, "Year-end alignment."),
        HistoricalLeapSecond("1979-12-31", LocalDate.of(1979, 12, 31), 19, "Year-end alignment."),
        HistoricalLeapSecond("1981-06-30", LocalDate.of(1981, 6, 30), 20, "Mid-year alignment."),
        HistoricalLeapSecond("1982-06-30", LocalDate.of(1982, 6, 30), 21, "Mid-year alignment."),
        HistoricalLeapSecond("1983-06-30", LocalDate.of(1983, 6, 30), 22, "Mid-year alignment."),
        HistoricalLeapSecond("1985-06-30", LocalDate.of(1985, 6, 30), 23, "Mid-year alignment."),
        HistoricalLeapSecond("1987-12-31", LocalDate.of(1987, 12, 31), 24, "Year-end alignment."),
        HistoricalLeapSecond("1989-12-31", LocalDate.of(1989, 12, 31), 25, "Year-end alignment."),
        HistoricalLeapSecond("1990-12-31", LocalDate.of(1990, 12, 31), 26, "Year-end alignment."),
        HistoricalLeapSecond("1992-06-30", LocalDate.of(1992, 6, 30), 27, "Mid-year alignment."),
        HistoricalLeapSecond("1993-06-30", LocalDate.of(1993, 6, 30), 28, "Mid-year alignment."),
        HistoricalLeapSecond("1994-06-30", LocalDate.of(1994, 6, 30), 29, "Mid-year alignment."),
        HistoricalLeapSecond("1995-12-31", LocalDate.of(1995, 12, 31), 30, "Year-end alignment."),
        HistoricalLeapSecond("1997-06-30", LocalDate.of(1997, 6, 30), 31, "Mid-year alignment."),
        HistoricalLeapSecond("1998-12-31", LocalDate.of(1998, 12, 31), 32, "Year-end alignment."),
        HistoricalLeapSecond("2005-12-31", LocalDate.of(2005, 12, 31), 33, "First leap second in 7 years due to transient Earth speedup."),
        HistoricalLeapSecond("2008-12-31", LocalDate.of(2008, 12, 31), 34, "Year-end alignment."),
        HistoricalLeapSecond("2012-06-30", LocalDate.of(2012, 6, 30), 35, "Mid-year alignment."),
        HistoricalLeapSecond("2015-06-30", LocalDate.of(2015, 6, 30), 36, "Mid-year alignment."),
        HistoricalLeapSecond("2016-12-31", LocalDate.of(2016, 12, 31), 37, "Most recent leap second inserted by IERS.")
    )

    /**
     * Calculates simultaneous clock times for TAI, UTC, GPS, and UT1 standards.
     */
    fun calculateMultiStandardTime(utcZdt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): MultiStandardTimeState {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)

        val utcTime = utcZdt.toLocalTime()
        val utcStr = utcTime.format(formatter)

        // TAI = UTC + 37s
        val taiInstant = utcZdt.toInstant().plusSeconds(CURRENT_TAI_OFFSET_SECONDS)
        val taiTime = taiInstant.atZone(ZoneOffset.UTC).toLocalTime()
        val taiStr = taiTime.format(formatter)

        // GPS = UTC + 18s
        val gpsInstant = utcZdt.toInstant().plusSeconds(CURRENT_GPS_OFFSET_SECONDS)
        val gpsTime = gpsInstant.atZone(ZoneOffset.UTC).toLocalTime()
        val gpsStr = gpsTime.format(formatter)

        // UT1 = UTC + DUT1 (milliseconds offset)
        val ut1Millis = (CURRENT_DUT1_SECONDS * 1000.0).toLong()
        val ut1Instant = utcZdt.toInstant().plusMillis(ut1Millis)
        val ut1Time = ut1Instant.atZone(ZoneOffset.UTC).toLocalTime()
        val ut1Str = "${ut1Time.format(formatter)}.${String.format(Locale.US, "%03d", ut1Instant.toEpochMilli() % 1000)}"

        return MultiStandardTimeState(
            utcFormatted = "$utcStr UTC",
            taiFormatted = "$taiStr TAI (+37s)",
            gpsFormatted = "$gpsStr GPS (+18s)",
            ut1Formatted = "$ut1Str UT1 (${String.format(Locale.US, "%+.1f", CURRENT_DUT1_SECONDS * 1000)}ms)",
            taiUtcOffsetSeconds = CURRENT_TAI_OFFSET_SECONDS,
            gpsUtcOffsetSeconds = CURRENT_GPS_OFFSET_SECONDS,
            dut1Seconds = CURRENT_DUT1_SECONDS
        )
    }

    /**
     * Returns simulated 23:59:60 leap second transition frames (0..4).
     */
    fun getLeapSecondSimulationFrame(step: Int): Pair<String, String> {
        return when (step % 5) {
            0 -> Pair("23:59:58", "Regular atomic & civil clock alignment")
            1 -> Pair("23:59:59", "Final standard second of the year")
            2 -> Pair("23:59:60", "⚡ LEAP SECOND INSERTED: 61st second of the minute!")
            3 -> Pair("00:00:00", "Midnight rollover with astronomical alignment restored")
            else -> Pair("00:00:01", "Standard clock progression resumes")
        }
    }
}
