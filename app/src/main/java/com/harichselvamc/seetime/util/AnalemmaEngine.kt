package com.harichselvamc.seetime.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class AnalemmaPoint(
    val dayOfYear: Int,
    val eotMinutes: Double, // X coordinate (-15 to +17 min)
    val declinationDeg: Double, // Y coordinate (-23.5° to +23.5°)
    val dateFormatted: String
)

data class DailySolarDeviationDetails(
    val date: LocalDate,
    val eotMinutes: Double,
    val eotFormatted: String,
    val isSunFast: Boolean,
    val solarDeclinationDeg: Double,
    val solarNoonTimeUtc: LocalTime,
    val solarNoonTimeLocal: LocalTime,
    val explanation: String
)

data class AnalemmaMilestone(
    val name: String,
    val approxDate: String,
    val eotDrift: String,
    val significance: String
)

object AnalemmaEngine {

    val ANALEMMA_MILESTONES = listOf(
        AnalemmaMilestone("Max Winter Sun Slowdown", "February 11-12", "-14.2 min", "Sun arrives at highest point 14m 12s AFTER clock noon."),
        AnalemmaMilestone("Spring Zero-Drift Point", "April 15", "0.0 min", "Sundial time matches civil clock time perfectly."),
        AnalemmaMilestone("Spring Peak Fast", "May 14", "+3.7 min", "Sun reaches solar noon 3m 42s BEFORE clock noon."),
        AnalemmaMilestone("Summer Zero-Drift Point", "June 13", "0.0 min", "Second annual exact match between sundial and clock."),
        AnalemmaMilestone("Summer Trough", "July 26", "-6.5 min", "Sun reaches solar noon 6m 30s AFTER clock noon."),
        AnalemmaMilestone("Autumn Zero-Drift Point", "September 1", "0.0 min", "Third annual exact alignment."),
        AnalemmaMilestone("Max Autumn Sun Speedup", "November 3-4", "+16.4 min", "Sun arrives at highest point 16m 24s BEFORE clock noon."),
        AnalemmaMilestone("Winter Zero-Drift Point", "December 25", "0.0 min", "Fourth annual exact alignment.")
    )

    /**
     * Calculates Equation of Time (EoT) in minutes for a given [dayOfYear] (1..365).
     */
    fun calculateEotMinutes(dayOfYear: Int): Double {
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1)
        return 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
    }

    /**
     * Calculates Solar Declination in degrees (-23.44° to +23.44°) for a given [dayOfYear].
     */
    fun calculateSolarDeclination(dayOfYear: Int): Double {
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1)
        val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)
        return Math.toDegrees(declRad)
    }

    /**
     * Generates a continuous 365-day figure-8 Analemma curve.
     */
    fun generate365DayAnalemmaCurve(year: Int = LocalDate.now().year): List<AnalemmaPoint> {
        val points = mutableListOf<AnalemmaPoint>()
        for (day in 1..365) {
            val date = LocalDate.ofYearDay(year, day)
            val eot = calculateEotMinutes(day)
            val decl = calculateSolarDeclination(day)
            val fmt = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            points.add(AnalemmaPoint(dayOfYear = day, eotMinutes = eot, declinationDeg = decl, dateFormatted = fmt))
        }
        return points
    }

    /**
     * Calculates comprehensive solar noon deviation details for [date] and [zoneId].
     */
    fun calculateDailyDeviation(
        date: LocalDate = LocalDate.now(),
        longitudeDeg: Double = 0.0,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DailySolarDeviationDetails {
        val dayOfYear = date.dayOfYear
        val eot = calculateEotMinutes(dayOfYear)
        val decl = calculateSolarDeclination(dayOfYear)

        val isFast = eot > 0
        val absEot = kotlin.math.abs(eot)
        val eotMins = absEot.toInt()
        val eotSecs = ((absEot - eotMins) * 60).toInt()

        val signStr = if (isFast) "+" else "-"
        val fastSlowStr = if (isFast) "Sun Fast (Ahead)" else "Sun Slow (Behind)"
        val formatted = "$signStr${eotMins}m ${String.format(Locale.US, "%02d", eotSecs)}s ($fastSlowStr)"

        // Solar Noon at Greenwich (UTC): 12:00 - EoT
        val noonUtcMinutes = ((12 * 60) - eot).coerceIn(0.0, 1439.9)
        val noonUtcH = (noonUtcMinutes / 60).toInt()
        val noonUtcM = (noonUtcMinutes % 60).toInt()
        val noonUtcS = (((noonUtcMinutes % 60) - noonUtcM) * 60).toInt()
        val noonUtcTime = LocalTime.of(noonUtcH, noonUtcM, noonUtcS)

        // Local Solar Noon adjusted for longitude offset from standard meridian
        val standardOffsetHours = zoneId.rules.getOffset(date.atTime(12, 0).atZone(zoneId).toInstant()).totalSeconds / 3600.0
        val standardMeridian = standardOffsetHours * 15.0
        val lonCorrectionMinutes = 4.0 * (standardMeridian - longitudeDeg)

        val localNoonMinutes = ((12 * 60) - eot + lonCorrectionMinutes + 1440.0).mod(1440.0)
        val localNoonH = (localNoonMinutes / 60).toInt()
        val localNoonM = (localNoonMinutes % 60).toInt()
        val localNoonS = (((localNoonMinutes % 60) - localNoonM) * 60).toInt()
        val localNoonTime = LocalTime.of(localNoonH, localNoonM, localNoonS)

        val explanation = if (isFast) {
            "Earth's orbital speed and axial tilt cause true solar noon to arrive $eotMins min $eotSecs sec BEFORE 12:00 clock noon."
        } else {
            "Earth's orbital speed and axial tilt cause true solar noon to arrive $eotMins min $eotSecs sec AFTER 12:00 clock noon."
        }

        return DailySolarDeviationDetails(
            date = date,
            eotMinutes = eot,
            eotFormatted = formatted,
            isSunFast = isFast,
            solarDeclinationDeg = decl,
            solarNoonTimeUtc = noonUtcTime,
            solarNoonTimeLocal = localNoonTime,
            explanation = explanation
        )
    }

    /**
     * Converts a sundial shadow reading to accurate civil clock time.
     */
    fun convertSundialToCivilTime(
        sundialHour: Int,
        sundialMinute: Int,
        longitudeDeg: Double,
        zoneId: ZoneId,
        date: LocalDate = LocalDate.now()
    ): LocalTime {
        val eot = calculateEotMinutes(date.dayOfYear)
        val standardOffsetHours = zoneId.rules.getOffset(date.atTime(12, 0).atZone(zoneId).toInstant()).totalSeconds / 3600.0
        val standardMeridian = standardOffsetHours * 15.0
        val lonCorrectionMinutes = 4.0 * (standardMeridian - longitudeDeg)

        val sundialTotalMinutes = (sundialHour * 60) + sundialMinute
        val civilMinutes = (sundialTotalMinutes - eot + lonCorrectionMinutes + 1440.0).mod(1440.0)

        val h = (civilMinutes / 60).toInt()
        val m = (civilMinutes % 60).toInt()
        val s = (((civilMinutes % 60) - m) * 60).toInt()

        return LocalTime.of(h, m, s)
    }
}
