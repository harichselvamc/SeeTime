package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class SolarTimes(
    val sunrise: LocalTime,
    val solarNoon: LocalTime,
    val sunset: LocalTime,
    val dawnTwilight: LocalTime,
    val duskTwilight: LocalTime,
    val daylightMinutes: Int
)

data class SleepCycleRecommendation(
    val cycles: Int,
    val durationMinutes: Int,
    val targetTime: LocalTime,
    val description: String,
    val isRecommended: Boolean = false
)

data class JetLagDayPlan(
    val dayNumber: Int,
    val recommendedBedtime: LocalTime,
    val recommendedWakeTime: LocalTime,
    val lightExposureWindow: String,
    val lightAvoidanceWindow: String,
    val notes: String
)

object CircadianCalculator {

    const val SLEEP_CYCLE_MINUTES = 90
    const val AVERAGE_FALL_ASLEEP_LATENCY_MINUTES = 15
    const val CAFFEINE_CUTOFF_HOURS_BEFORE_BED = 9

    /**
     * Approximate representative coordinates for major timezones / regions.
     */
    fun getCoordinatesForZone(zoneIdStr: String): Pair<Double, Double> {
        return when {
            zoneIdStr.contains("Tokyo", ignoreCase = true) || zoneIdStr.contains("Japan", ignoreCase = true) -> Pair(35.6762, 139.6503)
            zoneIdStr.contains("London", ignoreCase = true) || zoneIdStr.contains("GMT", ignoreCase = true) || zoneIdStr.contains("UTC", ignoreCase = true) -> Pair(51.5074, -0.1278)
            zoneIdStr.contains("New_York", ignoreCase = true) || zoneIdStr.contains("Eastern", ignoreCase = true) -> Pair(40.7128, -74.0060)
            zoneIdStr.contains("Los_Angeles", ignoreCase = true) || zoneIdStr.contains("Pacific", ignoreCase = true) -> Pair(34.0522, -118.2437)
            zoneIdStr.contains("Chicago", ignoreCase = true) || zoneIdStr.contains("Central", ignoreCase = true) -> Pair(41.8781, -87.6298)
            zoneIdStr.contains("Denver", ignoreCase = true) || zoneIdStr.contains("Mountain", ignoreCase = true) -> Pair(39.7392, -104.9903)
            zoneIdStr.contains("Paris", ignoreCase = true) || zoneIdStr.contains("Berlin", ignoreCase = true) -> Pair(48.8566, 2.3522)
            zoneIdStr.contains("Dubai", ignoreCase = true) -> Pair(25.2048, 55.2708)
            zoneIdStr.contains("Singapore", ignoreCase = true) -> Pair(1.3521, 103.8198)
            zoneIdStr.contains("Hong_Kong", ignoreCase = true) -> Pair(22.3193, 114.1694)
            zoneIdStr.contains("Sydney", ignoreCase = true) -> Pair(-33.8688, 151.2093)
            zoneIdStr.contains("Auckland", ignoreCase = true) -> Pair(-36.8485, 174.7633)
            zoneIdStr.contains("Kolkata", ignoreCase = true) || zoneIdStr.contains("Calcutta", ignoreCase = true) || zoneIdStr.contains("India", ignoreCase = true) -> Pair(22.5726, 88.3639)
            zoneIdStr.contains("Sao_Paulo", ignoreCase = true) -> Pair(-23.5505, -46.6333)
            else -> Pair(0.0, 0.0) // Equator default
        }
    }

    /**
     * Pure astronomical calculation of solar times for a given day and coordinates.
     */
    fun calculateSolarTimes(
        latitude: Double,
        longitude: Double,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): SolarTimes {
        val dayOfYear = date.dayOfYear
        // Solar declination (radians)
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)

        // Equation of time (minutes)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        val latRad = latitude * PI / 180.0
        val zenithRad = 90.833 * PI / 180.0 // Standard zenith for sunrise/sunset

        var cosH = (cos(zenithRad) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
        cosH = cosH.coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(acos(cosH))

        val offsetHours = zoneId.rules.getOffset(Instant.now()).totalSeconds / 3600.0

        val noonMinutes = 720 - (4 * longitude) - eqTime + (offsetHours * 60)
        val sunriseMinutes = noonMinutes - (hourAngleDeg * 4)
        val sunsetMinutes = noonMinutes + (hourAngleDeg * 4)

        val dawnMinutes = (sunriseMinutes - 30).coerceIn(0.0, 1439.0)
        val duskMinutes = (sunsetMinutes + 30).coerceIn(0.0, 1439.0)

        val safeSunrise = sunriseMinutes.mod(1440.0)
        val safeSunset = sunsetMinutes.mod(1440.0)
        val safeNoon = noonMinutes.mod(1440.0)

        val sunrise = LocalTime.of((safeSunrise / 60).toInt().coerceIn(0, 23), (safeSunrise % 60).toInt().coerceIn(0, 59))
        val sunset = LocalTime.of((safeSunset / 60).toInt().coerceIn(0, 23), (safeSunset % 60).toInt().coerceIn(0, 59))
        val noon = LocalTime.of((safeNoon / 60).toInt().coerceIn(0, 23), (safeNoon % 60).toInt().coerceIn(0, 59))
        val dawn = LocalTime.of((dawnMinutes / 60).toInt().coerceIn(0, 23), (dawnMinutes % 60).toInt().coerceIn(0, 59))
        val dusk = LocalTime.of((duskMinutes / 60).toInt().coerceIn(0, 23), (duskMinutes % 60).toInt().coerceIn(0, 59))

        val daylightMinutes = (sunsetMinutes - sunriseMinutes).toInt().coerceAtLeast(0)

        return SolarTimes(
            sunrise = sunrise,
            solarNoon = noon,
            sunset = sunset,
            dawnTwilight = dawn,
            duskTwilight = dusk,
            daylightMinutes = daylightMinutes
        )
    }

    /**
     * Calculates wake-up times for going to bed now (or at [bedtime]).
     * 90-minute ultradian sleep cycle aligned + 15-minute fall-asleep latency.
     */
    fun calculateWakeTimes(
        bedtime: LocalTime = LocalTime.now(),
        latencyMinutes: Int = AVERAGE_FALL_ASLEEP_LATENCY_MINUTES
    ): List<SleepCycleRecommendation> {
        val sleepStartTime = bedtime.plusMinutes(latencyMinutes.toLong())
        val cyclesList = listOf(3, 4, 5, 6) // 4.5h, 6h, 7.5h, 9h

        return cyclesList.map { cycles ->
            val durationMin = cycles * SLEEP_CYCLE_MINUTES
            val wakeTime = sleepStartTime.plusMinutes(durationMin.toLong())
            val hours = durationMin / 60.0
            val isOptimal = (cycles == 5) // 7.5h is optimal adult recommendation
            val desc = when (cycles) {
                3 -> "4.5 hrs (3 cycles) · Quick recharge"
                4 -> "6.0 hrs (4 cycles) · Standard minimum"
                5 -> "7.5 hrs (5 cycles) · Optimal restorative"
                6 -> "9.0 hrs (6 cycles) · Deep recovery"
                else -> "$hours hrs ($cycles cycles)"
            }
            SleepCycleRecommendation(
                cycles = cycles,
                durationMinutes = durationMin,
                targetTime = wakeTime,
                description = desc,
                isRecommended = isOptimal
            )
        }
    }

    /**
     * Calculates optimal bedtimes to wake up at a desired [targetWakeTime].
     */
    fun calculateBedtimesForWakeTarget(
        targetWakeTime: LocalTime,
        latencyMinutes: Int = AVERAGE_FALL_ASLEEP_LATENCY_MINUTES
    ): List<SleepCycleRecommendation> {
        val cyclesList = listOf(6, 5, 4, 3)

        return cyclesList.map { cycles ->
            val durationMin = cycles * SLEEP_CYCLE_MINUTES
            val totalSubtracted = durationMin + latencyMinutes
            val bedtime = targetWakeTime.minusMinutes(totalSubtracted.toLong())
            val hours = durationMin / 60.0
            val isOptimal = (cycles == 5)
            val desc = when (cycles) {
                5 -> "7.5 hrs sleep · Optimal"
                6 -> "9.0 hrs sleep · Full recovery"
                4 -> "6.0 hrs sleep · Minimum"
                3 -> "4.5 hrs sleep · Short"
                else -> "$hours hrs ($cycles cycles)"
            }
            SleepCycleRecommendation(
                cycles = cycles,
                durationMinutes = durationMin,
                targetTime = bedtime,
                description = desc,
                isRecommended = isOptimal
            )
        }
    }

    /**
     * Calculates recommended caffeine curfew time (cutoff) given a planned [bedtime].
     */
    fun calculateCaffeineCutoff(bedtime: LocalTime): LocalTime {
        return bedtime.minusHours(CAFFEINE_CUTOFF_HOURS_BEFORE_BED.toLong())
    }

    /**
     * Generates a progressive 4-day sleep pre-adaptation plan for cross-timezone travel.
     */
    fun calculateJetLagPreAdaptation(
        originZone: String,
        destinationZone: String,
        normalBedtime: LocalTime = LocalTime.of(23, 0),
        normalWakeTime: LocalTime = LocalTime.of(7, 0)
    ): List<JetLagDayPlan> {
        val originOffsetHours = try {
            ZoneId.of(originZone).rules.getOffset(Instant.now()).totalSeconds / 3600
        } catch (_: Exception) { 0 }

        val destOffsetHours = try {
            ZoneId.of(destinationZone).rules.getOffset(Instant.now()).totalSeconds / 3600
        } catch (_: Exception) { 0 }

        val diffHours = destOffsetHours - originOffsetHours
        val isEastward = diffHours > 0
        val shiftPerDayMinutes = if (isEastward) 45 else -45 // Shift earlier eastward, later westward

        val plans = mutableListOf<JetLagDayPlan>()

        for (day in 1..4) {
            val shift = (day * shiftPerDayMinutes).toLong()
            val adjustedBed = if (isEastward) normalBedtime.minusMinutes(shift) else normalBedtime.plusMinutes(-shift)
            val adjustedWake = if (isEastward) normalWakeTime.minusMinutes(shift) else normalWakeTime.plusMinutes(-shift)

            val lightWindow = if (isEastward) {
                "Morning: Seek natural sunlight ${adjustedWake} - ${adjustedWake.plusHours(2)}"
            } else {
                "Late Afternoon: Seek natural sunlight ${adjustedBed.minusHours(4)} - ${adjustedBed.minusHours(2)}"
            }

            val avoidWindow = if (isEastward) {
                "Evening: Dim lights after ${adjustedBed.minusHours(2)}"
            } else {
                "Morning: Wear sunglasses before ${adjustedWake.plusHours(1)}"
            }

            val note = "Shift schedule by ${kotlin.math.abs(shift)}m ${if (isEastward) "earlier" else "later"}"

            plans.add(
                JetLagDayPlan(
                    dayNumber = day,
                    recommendedBedtime = adjustedBed,
                    recommendedWakeTime = adjustedWake,
                    lightExposureWindow = lightWindow,
                    lightAvoidanceWindow = avoidWindow,
                    notes = note
                )
            )
        }

        return plans
    }
}
