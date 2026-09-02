package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class DialHandAngles(
    val hourAngle: Float,
    val minuteAngle: Float,
    val secondAngle: Float,
    val gmt24Angle: Float
)

data class SubDialState(
    val title: String,
    val zoneId: String,
    val hourAngle: Float,
    val minuteAngle: Float,
    val timeFormatted: String,
    val isDaylight: Boolean
)

data class BezelCity(
    val code: String,
    val cityName: String,
    val utcOffsetHours: Double
)

object ChronometerEngine {

    val BEZEL_CITIES = listOf(
        BezelCity("LON", "London", 0.0),
        BezelCity("PAR", "Paris", 1.0),
        BezelCity("CAI", "Cairo", 2.0),
        BezelCity("DXB", "Dubai", 4.0),
        BezelCity("DEL", "New Delhi", 5.5),
        BezelCity("BKK", "Bangkok", 7.0),
        BezelCity("HKG", "Hong Kong", 8.0),
        BezelCity("TYO", "Tokyo", 9.0),
        BezelCity("SYD", "Sydney", 10.0),
        BezelCity("AKL", "Auckland", 12.0),
        BezelCity("HNL", "Honolulu", -10.0),
        BezelCity("ANC", "Anchorage", -9.0),
        BezelCity("LAX", "Los Angeles", -8.0),
        BezelCity("DEN", "Denver", -7.0),
        BezelCity("CHI", "Chicago", -6.0),
        BezelCity("NYC", "New York", -5.0),
        BezelCity("RIO", "Rio de Janeiro", -3.0),
        BezelCity("PDL", "Azores", -1.0)
    )

    /**
     * Calculates smooth continuous sweeping second hand angle (0°..360°).
     */
    fun calculateSweepingSecondAngle(seconds: Int, millis: Int): Float {
        val totalSec = seconds + (millis / 1000f)
        return (totalSec * 6f) % 360f
    }

    /**
     * Calculates minute hand angle (0°..360°).
     */
    fun calculateMinuteAngle(minutes: Int, seconds: Int): Float {
        val totalMin = minutes + (seconds / 60f)
        return (totalMin * 6f) % 360f
    }

    /**
     * Calculates 12-hour main hand angle (0°..360°).
     */
    fun calculateHourAngle(hours: Int, minutes: Int, seconds: Int): Float {
        val totalHour = (hours % 12) + (minutes / 60f) + (seconds / 3600f)
        return (totalHour * 30f) % 360f
    }

    /**
     * Calculates 24-hour red GMT hand angle (0°..360°).
     */
    fun calculateGmt24Angle(hours24: Int, minutes: Int): Float {
        val totalHour = hours24 + (minutes / 60f)
        return (totalHour * 15f) % 360f
    }

    /**
     * Calculates comprehensive angles for a given [zonedDateTime] with [instantMillis].
     */
    fun calculateMainDialAngles(zdt: ZonedDateTime, instantMillis: Int = 0): DialHandAngles {
        val h = zdt.hour
        val m = zdt.minute
        val s = zdt.second

        return DialHandAngles(
            hourAngle = calculateHourAngle(h, m, s),
            minuteAngle = calculateMinuteAngle(m, s),
            secondAngle = calculateSweepingSecondAngle(s, instantMillis),
            gmt24Angle = calculateGmt24Angle(h, m)
        )
    }

    /**
     * Calculates state for mini secondary timezone sub-dials.
     */
    fun calculateSubDialState(
        title: String,
        zoneIdStr: String,
        referenceTime: ZonedDateTime
    ): SubDialState {
        val zone = try { ZoneId.of(zoneIdStr) } catch (_: Exception) { ZoneId.systemDefault() }
        val targetZdt = referenceTime.withZoneSameInstant(zone)

        val h = targetZdt.hour
        val m = targetZdt.minute
        val s = targetZdt.second

        val hAngle = calculateHourAngle(h, m, s)
        val mAngle = calculateMinuteAngle(m, s)

        val formatted = String.format("%02d:%02d", h, m)
        val isDaylight = h in 6..18

        return SubDialState(
            title = title,
            zoneId = zoneIdStr,
            hourAngle = hAngle,
            minuteAngle = mAngle,
            timeFormatted = formatted,
            isDaylight = isDaylight
        )
    }
}
