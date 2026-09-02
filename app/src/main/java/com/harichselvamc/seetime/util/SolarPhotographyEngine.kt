package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class SolarPhaseWindow(
    val phaseName: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
    val photoRecommendation: String,
    val hexColor: Long
)

data class SolarPosition(
    val elevationDegrees: Double,
    val azimuthDegrees: Double,
    val isAboveHorizon: Boolean,
    val currentPhaseName: String
)

data class DailyPhotographySchedule(
    val date: LocalDate,
    val morningBlueHour: SolarPhaseWindow,
    val morningGoldenHour: SolarPhaseWindow,
    val sunriseTime: LocalTime,
    val solarNoonTime: LocalTime,
    val sunsetTime: LocalTime,
    val eveningGoldenHour: SolarPhaseWindow,
    val eveningBlueHour: SolarPhaseWindow,
    val peakSunElevation: Double
)

object SolarPhotographyEngine {

    /**
     * Calculates instantaneous Sun Elevation (-90°..+90°) and Azimuth (0°..360° clockwise from North).
     */
    fun calculateSunPosition(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarPosition {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + (dateTime.minute / 60.0) + (dateTime.second / 3600.0)

        // Fractional year (radians)
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (hour - 12) / 24.0)

        // Equation of time in minutes
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination (radians)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)

        val offsetHours = dateTime.offset.totalSeconds / 3600.0
        val timeOffsetMin = eqTime + (4 * longitude) - (60 * offsetHours)
        val trueSolarTimeMin = (hour * 60.0) + timeOffsetMin
        val solarTimeMinMod = (trueSolarTimeMin.mod(1440.0) + 1440.0).mod(1440.0)

        // Hour angle (degrees)
        val hourAngleDeg = (solarTimeMinMod / 4.0) - 180.0
        val hourAngleRad = Math.toRadians(hourAngleDeg)
        val latRad = Math.toRadians(latitude)

        // Solar zenith angle
        val cosZenith = (sin(latRad) * sin(decl)) + (cos(latRad) * cos(decl) * cos(hourAngleRad))
        val safeCosZenith = cosZenith.coerceIn(-1.0, 1.0)
        val zenithRad = acos(safeCosZenith)
        val elevationDeg = 90.0 - Math.toDegrees(zenithRad)

        // Solar azimuth angle (degrees from North)
        val sinAzimuth = -sin(hourAngleRad) * cos(decl) / sin(zenithRad).coerceAtLeast(0.0001)
        val cosAzimuth = (sin(decl) - sin(latRad) * cos(zenithRad)) / (cos(latRad) * sin(zenithRad)).coerceAtLeast(0.0001)
        var azimuthDeg = Math.toDegrees(atan2(sinAzimuth, cosAzimuth))
        if (azimuthDeg < 0) azimuthDeg += 360.0

        val currentPhase = when {
            elevationDeg > 6.0 -> "Daylight / Direct Sun"
            elevationDeg in -4.0..6.0 -> if (hourAngleDeg < 0) "Morning Golden Hour" else "Evening Golden Hour"
            elevationDeg in -6.0..-4.0 -> if (hourAngleDeg < 0) "Morning Blue Hour" else "Evening Blue Hour"
            elevationDeg in -12.0..-6.0 -> "Nautical Twilight"
            elevationDeg in -18.0..-12.0 -> "Astronomical Twilight"
            else -> "Night Sky / Astrophotography"
        }

        return SolarPosition(
            elevationDegrees = elevationDeg,
            azimuthDegrees = azimuthDeg,
            isAboveHorizon = elevationDeg >= 0.0,
            currentPhaseName = currentPhase
        )
    }

    /**
     * Calculates the full daily photography schedule for given coordinates and date.
     */
    fun calculateDailySchedule(
        latitude: Double,
        longitude: Double,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): DailyPhotographySchedule {
        val baseSolar = CircadianCalculator.calculateSolarTimes(latitude, longitude, date, zoneId)

        val sunrise = baseSolar.sunrise
        val sunset = baseSolar.sunset
        val noon = baseSolar.solarNoon

        // Morning Blue Hour: ~30 min before sunrise
        val mBlueStart = sunrise.minusMinutes(35)
        val mBlueEnd = sunrise.minusMinutes(12)

        // Morning Golden Hour: ~12 min before sunrise to 45 min after sunrise
        val mGoldStart = sunrise.minusMinutes(12)
        val mGoldEnd = sunrise.plusMinutes(45)

        // Evening Golden Hour: ~45 min before sunset to 12 min after sunset
        val eGoldStart = sunset.minusMinutes(45)
        val eGoldEnd = sunset.plusMinutes(12)

        // Evening Blue Hour: ~12 min after sunset to 35 min after sunset
        val eBlueStart = sunset.plusMinutes(12)
        val eBlueEnd = sunset.plusMinutes(35)

        // Peak noon elevation approximation
        val dayOfYear = date.dayOfYear
        val declDeg = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))
        val peakElevation = (90.0 - kotlin.math.abs(latitude - declDeg)).coerceIn(0.0, 90.0)

        return DailyPhotographySchedule(
            date = date,
            morningBlueHour = SolarPhaseWindow(
                phaseName = "Morning Blue Hour",
                startTime = mBlueStart,
                endTime = mBlueEnd,
                description = "Cool deep blue gradients & high contrast architectural shadows",
                photoRecommendation = "Ideal for cityscapes, illuminated monuments & long exposure water",
                hexColor = 0xFF1E3A8A // Deep Navy / Blue
            ),
            morningGoldenHour = SolarPhaseWindow(
                phaseName = "Morning Golden Hour",
                startTime = mGoldStart,
                endTime = mGoldEnd,
                description = "Soft, warm directional sunlight with long dramatic shadows",
                photoRecommendation = "Perfect for portraits, misty landscapes & nature backlight",
                hexColor = 0xFFF59E0B // Warm Amber / Gold
            ),
            sunriseTime = sunrise,
            solarNoonTime = noon,
            sunsetTime = sunset,
            eveningGoldenHour = SolarPhaseWindow(
                phaseName = "Evening Golden Hour",
                startTime = eGoldStart,
                endTime = eGoldEnd,
                description = "Rich crimson & honey tones, warm diffusion & silhouette opportunities",
                photoRecommendation = "Outstanding for street photography, flare portraits & sunset horizons",
                hexColor = 0xFFEA580C // Fiery Orange / Gold
            ),
            eveningBlueHour = SolarPhaseWindow(
                phaseName = "Evening Blue Hour",
                startTime = eBlueStart,
                endTime = eBlueEnd,
                description = "Balanced ambient sky light matching urban artificial illumination",
                photoRecommendation = "Best for vibrant urban nightlife, bridges & light trails",
                hexColor = 0xFF2563EB // Vibrant Cobalt Blue
            ),
            peakSunElevation = peakElevation
        )
    }

    /**
     * Calculates time remaining until the next upcoming Golden or Blue hour window.
     */
    fun findNextUpcomingWindow(
        schedule: DailyPhotographySchedule,
        currentTime: LocalTime = LocalTime.now()
    ): Pair<SolarPhaseWindow, Int> {
        val windows = listOf(
            schedule.morningBlueHour,
            schedule.morningGoldenHour,
            schedule.eveningGoldenHour,
            schedule.eveningBlueHour
        )

        for (window in windows) {
            if (currentTime.isBefore(window.startTime)) {
                val diffMins = java.time.Duration.between(currentTime, window.startTime).toMinutes().toInt()
                return Pair(window, diffMins)
            } else if (!currentTime.isAfter(window.endTime)) {
                // Currently active!
                val remainingMins = java.time.Duration.between(currentTime, window.endTime).toMinutes().toInt()
                return Pair(window, -remainingMins) // Negative denotes currently active with minutes left
            }
        }

        // Wrap around to next morning's blue hour
        val diffMins = java.time.Duration.between(currentTime, LocalTime.MAX).toMinutes().toInt() +
                java.time.Duration.between(LocalTime.MIN, schedule.morningBlueHour.startTime).toMinutes().toInt()
        return Pair(schedule.morningBlueHour, diffMins)
    }
}
