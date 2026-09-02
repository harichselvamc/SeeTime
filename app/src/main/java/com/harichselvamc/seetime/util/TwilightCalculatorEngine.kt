package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Standard Astronomical Twilight Phase classifications based on Sun elevation relative to the horizon.
 */
enum class TwilightPhase(
    val displayName: String,
    val shortName: String,
    val minElevationDeg: Double,
    val maxElevationDeg: Double,
    val description: String,
    val visualSkyDescription: String,
    val primaryColorHex: Long,
    val gradientTopHex: Long,
    val gradientBottomHex: Long,
    val starVisibilityDescription: String,
    val lightingCondition: String,
    val recommendedActivity: String,
    val iconEmoji: String
) {
    DIRECT_SUNLIGHT(
        displayName = "Direct Sunlight (Day)",
        shortName = "Daylight",
        minElevationDeg = 0.0,
        maxElevationDeg = 90.0,
        description = "Sun is above the geometric horizon. Full ambient daylight.",
        visualSkyDescription = "Bright azure sky, high contrast, vivid natural colors.",
        primaryColorHex = 0xFFF59E0B, // Amber / Sun Yellow
        gradientTopHex = 0xFF38BDF8,  // Sky Blue
        gradientBottomHex = 0xFFFEF08A, // Warm Golden Horizon
        starVisibilityDescription = "No stars visible except the Sun.",
        lightingCondition = "10,000 - 100,000+ Lux",
        recommendedActivity = "Standard outdoor activities, fast shutter speed photography",
        iconEmoji = "☀️"
    ),
    CIVIL_TWILIGHT(
        displayName = "Civil Twilight",
        shortName = "Civil",
        minElevationDeg = -6.0,
        maxElevationDeg = 0.0,
        description = "Sun is between 0° and -6° below horizon. Terrestrial horizon clearly defined.",
        visualSkyDescription = "Vibrant orange, coral, and purple glow spreading along the horizon.",
        primaryColorHex = 0xFFF97316, // Vibrant Orange
        gradientTopHex = 0xFF1E3A8A,  // Deep Navy
        gradientBottomHex = 0xFFF97316, // Sunset Coral / Amber
        starVisibilityDescription = "Venus, Jupiter, and brightest navigation stars emerge.",
        lightingCondition = "10 - 1,000 Lux",
        recommendedActivity = "Streetlights activating, Golden/Blue hour photography, outdoor dining",
        iconEmoji = "🌇"
    ),
    NAUTICAL_TWILIGHT(
        displayName = "Nautical Twilight",
        shortName = "Nautical",
        minElevationDeg = -12.0,
        maxElevationDeg = -6.0,
        description = "Sun is between -6° and -12° below horizon. Sea horizon becomes blurred.",
        visualSkyDescription = "Deep cobalt, indigo, and violet gradient dissolving into dark navy.",
        primaryColorHex = 0xFF3B82F6, // Cobalt Blue
        gradientTopHex = 0xFF0F172A,  // Midnight Slate
        gradientBottomHex = 0xFF4C1D95, // Deep Violet
        starVisibilityDescription = "First- and second-magnitude nautical navigation stars clearly visible.",
        lightingCondition = "1 - 10 Lux",
        recommendedActivity = "Maritime celestial navigation, long-exposure urban light trails",
        iconEmoji = "⛵"
    ),
    ASTRONOMICAL_TWILIGHT(
        displayName = "Astronomical Twilight",
        shortName = "Astronomical",
        minElevationDeg = -18.0,
        maxElevationDeg = -12.0,
        description = "Sun is between -12° and -18° below horizon. Faint atmospheric scatter remains.",
        visualSkyDescription = "Near-black cosmic sky with subtle deep indigo hue at horizon.",
        primaryColorHex = 0xFF6366F1, // Indigo
        gradientTopHex = 0xFF030712,  // Cosmic Void
        gradientBottomHex = 0xFF1E1B4B, // Dark Indigo
        starVisibilityDescription = "Milky Way backbone, faint nebulas, and 6th-magnitude stars emerge.",
        lightingCondition = "0.01 - 1 Lux",
        recommendedActivity = "Telescope calibration, deep space observation & initial astrophotography",
        iconEmoji = "🔭"
    ),
    NIGHT(
        displayName = "Astronomical Night",
        shortName = "True Night",
        minElevationDeg = -90.0,
        maxElevationDeg = -18.0,
        description = "Sun is below -18°. Complete absence of solar illumination across atmosphere.",
        visualSkyDescription = "Total pitch-black sky illuminated solely by celestial bodies.",
        primaryColorHex = 0xFF8B5CF6, // Deep Cosmic Purple
        gradientTopHex = 0xFF020617,  // Pitch Black
        gradientBottomHex = 0xFF0B0F19, // Deep Space Slate
        starVisibilityDescription = "Full celestial sphere, faint galaxies, meteors & zodiacal light.",
        lightingCondition = "< 0.001 Lux",
        recommendedActivity = "Deep-sky astrophotography, meteor watching, uninterrupted stargazing",
        iconEmoji = "🌌"
    )
}

/**
 * High-value photography lighting windows.
 */
enum class PhotoLightingWindow(
    val displayName: String,
    val description: String,
    val badgeColorHex: Long,
    val emoji: String
) {
    GOLDEN_HOUR(
        displayName = "Golden Hour",
        description = "Sun elevation between -4.0° and +6.0°. Warm directional rim light.",
        badgeColorHex = 0xFFF59E0B,
        emoji = "✨"
    ),
    BLUE_HOUR(
        displayName = "Blue Hour",
        description = "Sun elevation between -6.0° and -4.0°. Cool blue ambient gradient.",
        badgeColorHex = 0xFF2563EB,
        emoji = "💙"
    ),
    STANDARD_DAY(
        displayName = "Direct Sun",
        description = "Sun elevation above +6.0°. Sharp contrast and high dynamic range.",
        badgeColorHex = 0xFFEAB308,
        emoji = "☀️"
    ),
    DARK_SKY(
        displayName = "Night / Astrophotography",
        description = "Sun elevation below -6.0°. Dark sky ideal for long exposures.",
        badgeColorHex = 0xFF7C3AED,
        emoji = "🌠"
    )
}

/**
 * Pre-configured world city for multi-timezone twilight comparisons.
 */
data class TwilightCity(
    val id: String,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId,
    val flagEmoji: String,
    val region: String
)

/**
 * Instantaneous solar altitude telemetry and active twilight status.
 */
data class SolarElevationTelemetry(
    val elevationDegrees: Double,
    val azimuthDegrees: Double,
    val isAboveHorizon: Boolean,
    val isRising: Boolean,
    val phase: TwilightPhase,
    val photoWindow: PhotoLightingWindow,
    val solarNoonElevation: Double,
    val solarNadirElevation: Double,
    val approximateLux: Double,
    val starVisibilityRating: Int, // 0 to 5 stars
    val nextTransitionEvent: TwilightTransitionEvent?,
    val localTime: ZonedDateTime
)

/**
 * Upcoming twilight or solar phase crossing event.
 */
data class TwilightTransitionEvent(
    val name: String,
    val targetElevation: Double,
    val timestamp: Instant,
    val localTime: LocalTime,
    val minutesUntil: Long,
    val formattedCountdown: String,
    val description: String,
    val phaseColorHex: Long
)

/**
 * Key daily solar and twilight milestones for a specific date and location.
 */
data class DailyTwilightMilestone(
    val name: String,
    val elevationDeg: Double,
    val time: LocalTime?,
    val description: String,
    val colorHex: Long,
    val isPassed: Boolean = false
)

/**
 * Daily schedule of twilight transitions.
 */
data class DailyTwilightSchedule(
    val date: LocalDate,
    val milestones: List<DailyTwilightMilestone>,
    val peakElevationDeg: Double,
    val nadirElevationDeg: Double
)

object TwilightCalculatorEngine {

    /**
     * Calculates the instantaneous Sun Elevation angle (-90°..+90°) relative to the geometric horizon.
     */
    fun calculateSolarElevation(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): Double {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + (dateTime.minute / 60.0) + (dateTime.second / 3600.0)

        // Fractional year gamma (radians)
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (hour - 12) / 24.0)

        // Equation of time in minutes
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination (radians)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)

        val offsetHours = dateTime.offset.totalSeconds / 3600.0
        val timeOffsetMin = eqTime + (4.0 * longitude) - (60.0 * offsetHours)
        val trueSolarTimeMin = (hour * 60.0) + timeOffsetMin
        val solarTimeMinMod = ((trueSolarTimeMin % 1440.0) + 1440.0) % 1440.0

        // Hour angle (degrees)
        val hourAngleDeg = (solarTimeMinMod / 4.0) - 180.0
        val hourAngleRad = Math.toRadians(hourAngleDeg)
        val latRad = Math.toRadians(latitude)

        // Solar zenith angle
        val cosZenith = (sin(latRad) * sin(decl)) + (cos(latRad) * cos(decl) * cos(hourAngleRad))
        val safeCosZenith = cosZenith.coerceIn(-1.0, 1.0)
        val zenithRad = acos(safeCosZenith)

        return 90.0 - Math.toDegrees(zenithRad)
    }

    /**
     * Calculates the instantaneous Sun Azimuth angle (0°..360° clockwise from North).
     */
    fun calculateSolarAzimuth(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): Double {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + (dateTime.minute / 60.0) + (dateTime.second / 3600.0)

        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (hour - 12) / 24.0)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)

        val offsetHours = dateTime.offset.totalSeconds / 3600.0
        val timeOffsetMin = eqTime + (4.0 * longitude) - (60.0 * offsetHours)
        val trueSolarTimeMin = (hour * 60.0) + timeOffsetMin
        val solarTimeMinMod = ((trueSolarTimeMin % 1440.0) + 1440.0) % 1440.0

        val hourAngleDeg = (solarTimeMinMod / 4.0) - 180.0
        val hourAngleRad = Math.toRadians(hourAngleDeg)
        val latRad = Math.toRadians(latitude)

        val cosZenith = (sin(latRad) * sin(decl)) + (cos(latRad) * cos(decl) * cos(hourAngleRad))
        val safeCosZenith = cosZenith.coerceIn(-1.0, 1.0)
        val zenithRad = acos(safeCosZenith)

        val sinAzimuth = -sin(hourAngleRad) * cos(decl) / sin(zenithRad).coerceAtLeast(0.0001)
        val cosAzimuth = (sin(decl) - sin(latRad) * cos(zenithRad)) / (cos(latRad) * sin(zenithRad)).coerceAtLeast(0.0001)
        var azimuthDeg = Math.toDegrees(atan2(sinAzimuth, cosAzimuth))
        if (azimuthDeg < 0) azimuthDeg += 360.0

        return azimuthDeg
    }

    /**
     * Determines the active TwilightPhase based on sun elevation angle.
     */
    fun getTwilightPhase(elevationDeg: Double): TwilightPhase {
        return when {
            elevationDeg > 0.0 -> TwilightPhase.DIRECT_SUNLIGHT
            elevationDeg >= -6.0 -> TwilightPhase.CIVIL_TWILIGHT
            elevationDeg >= -12.0 -> TwilightPhase.NAUTICAL_TWILIGHT
            elevationDeg >= -18.0 -> TwilightPhase.ASTRONOMICAL_TWILIGHT
            else -> TwilightPhase.NIGHT
        }
    }

    /**
     * Determines the photography lighting window based on sun elevation angle.
     */
    fun getPhotoWindow(elevationDeg: Double): PhotoLightingWindow {
        return when {
            elevationDeg > 6.0 -> PhotoLightingWindow.STANDARD_DAY
            elevationDeg in -4.0..6.0 -> PhotoLightingWindow.GOLDEN_HOUR
            elevationDeg in -6.0..-4.0 -> PhotoLightingWindow.BLUE_HOUR
            else -> PhotoLightingWindow.DARK_SKY
        }
    }

    /**
     * Approximates natural ambient illuminance in Lux based on solar elevation.
     */
    fun approximateLux(elevationDeg: Double): Double {
        return when {
            elevationDeg >= 60.0 -> 100000.0
            elevationDeg >= 20.0 -> 40000.0 + (elevationDeg - 20.0) * 1500.0
            elevationDeg >= 0.0 -> 400.0 + (elevationDeg / 20.0) * 39600.0
            elevationDeg >= -6.0 -> {
                // Civil twilight: 400 down to 3.4 lux
                val ratio = (elevationDeg + 6.0) / 6.0
                3.4 + ratio * 396.6
            }
            elevationDeg >= -12.0 -> {
                // Nautical twilight: 3.4 down to 0.008 lux
                val ratio = (elevationDeg + 12.0) / 6.0
                0.008 + ratio * 3.392
            }
            elevationDeg >= -18.0 -> {
                // Astronomical twilight: 0.008 down to 0.0005 lux
                val ratio = (elevationDeg + 18.0) / 6.0
                0.0005 + ratio * 0.0075
            }
            else -> 0.0002 // Starlight / Night
        }
    }

    /**
     * Estimates naked-eye star visibility rating on a 0 to 5 scale.
     */
    fun estimateStarVisibility(elevationDeg: Double): Int {
        return when {
            elevationDeg >= 0.0 -> 0 // Daylight: 0 stars
            elevationDeg >= -6.0 -> 1 // Civil: Brightest planets (Venus/Jupiter)
            elevationDeg >= -12.0 -> 2 // Nautical: Navigational 1st & 2nd mag stars
            elevationDeg >= -18.0 -> 4 // Astro: Milky way, faint stars
            else -> 5 // True Night: Full starry cosmos
        }
    }

    /**
     * Computes full solar telemetry for given coordinates and timestamp.
     */
    fun calculateTelemetry(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarElevationTelemetry {
        val elevation = calculateSolarElevation(latitude, longitude, dateTime)
        val azimuth = calculateSolarAzimuth(latitude, longitude, dateTime)

        // Check 1 minute ahead to evaluate whether sun is rising or setting
        val elevationNext = calculateSolarElevation(latitude, longitude, dateTime.plusMinutes(1))
        val isRising = elevationNext >= elevation

        val phase = getTwilightPhase(elevation)
        val photoWindow = getPhotoWindow(elevation)

        // Calculate peak (solar noon) and nadir (midnight) elevations for this day
        val dayOfYear = dateTime.dayOfYear
        val declDeg = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))
        val solarNoonElevation = (90.0 - abs(latitude - declDeg)).coerceIn(-90.0, 90.0)
        val solarNadirElevation = (-90.0 + abs(latitude + declDeg)).coerceIn(-90.0, 90.0)

        val lux = approximateLux(elevation)
        val starRating = estimateStarVisibility(elevation)
        val nextEvent = findNextTransition(latitude, longitude, dateTime)

        return SolarElevationTelemetry(
            elevationDegrees = elevation,
            azimuthDegrees = azimuth,
            isAboveHorizon = elevation >= 0.0,
            isRising = isRising,
            phase = phase,
            photoWindow = photoWindow,
            solarNoonElevation = solarNoonElevation,
            solarNadirElevation = solarNadirElevation,
            approximateLux = lux,
            starVisibilityRating = starRating,
            nextTransitionEvent = nextEvent,
            localTime = dateTime
        )
    }

    /**
     * Scans forward minute-by-minute over the next 24 hours to locate the next twilight transition event.
     */
    fun findNextTransition(
        latitude: Double,
        longitude: Double,
        startDateTime: ZonedDateTime
    ): TwilightTransitionEvent? {
        var currentElevation = calculateSolarElevation(latitude, longitude, startDateTime)

        // Step forward minute by minute up to 1440 minutes (24h)
        for (minuteOffset in 1..1440) {
            val nextTime = startDateTime.plusMinutes(minuteOffset.toLong())
            val nextElevation = calculateSolarElevation(latitude, longitude, nextTime)

            // Check if any key boundary is crossed
            val event = detectCrossing(currentElevation, nextElevation, nextTime, minuteOffset.toLong())
            if (event != null) {
                return event
            }
            currentElevation = nextElevation
        }
        return null
    }

    private fun detectCrossing(
        prevElev: Double,
        nextElev: Double,
        time: ZonedDateTime,
        minutesUntil: Long
    ): TwilightTransitionEvent? {
        val formattedCountdown = formatCountdown(minutesUntil)
        val localTime = time.toLocalTime()
        val instant = time.toInstant()

        return when {
            // Sunrise (crossing 0.0° going up)
            prevElev <= 0.0 && nextElev > 0.0 -> TwilightTransitionEvent(
                name = "Sunrise",
                targetElevation = 0.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Sun crests geometric horizon into direct daylight",
                phaseColorHex = TwilightPhase.DIRECT_SUNLIGHT.primaryColorHex
            )
            // Sunset (crossing 0.0° going down)
            prevElev >= 0.0 && nextElev < 0.0 -> TwilightTransitionEvent(
                name = "Civil Dusk (Sunset)",
                targetElevation = 0.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Sun dips below horizon, initiating Civil Twilight",
                phaseColorHex = TwilightPhase.CIVIL_TWILIGHT.primaryColorHex
            )
            // Nautical Dusk (crossing -6.0° going down)
            prevElev >= -6.0 && nextElev < -6.0 -> TwilightTransitionEvent(
                name = "Nautical Dusk",
                targetElevation = -6.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Horizon dims, navigation stars appear",
                phaseColorHex = TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex
            )
            // Civil Dawn (crossing -6.0° going up)
            prevElev <= -6.0 && nextElev > -6.0 -> TwilightTransitionEvent(
                name = "Civil Dawn",
                targetElevation = -6.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Sky brightens, civil daybreak begins",
                phaseColorHex = TwilightPhase.CIVIL_TWILIGHT.primaryColorHex
            )
            // Astronomical Dusk (crossing -12.0° going down)
            prevElev >= -12.0 && nextElev < -12.0 -> TwilightTransitionEvent(
                name = "Astronomical Dusk",
                targetElevation = -12.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Indigo sky fades toward true darkness",
                phaseColorHex = TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex
            )
            // Nautical Dawn (crossing -12.0° going up)
            prevElev <= -12.0 && nextElev > -12.0 -> TwilightTransitionEvent(
                name = "Nautical Dawn",
                targetElevation = -12.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Deep violet nautical twilight begins",
                phaseColorHex = TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex
            )
            // True Night (crossing -18.0° going down)
            prevElev >= -18.0 && nextElev < -18.0 -> TwilightTransitionEvent(
                name = "True Night (Astro Dark)",
                targetElevation = -18.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Complete astronomical darkness begins",
                phaseColorHex = TwilightPhase.NIGHT.primaryColorHex
            )
            // Astronomical Dawn (crossing -18.0° going up)
            prevElev <= -18.0 && nextElev > -18.0 -> TwilightTransitionEvent(
                name = "Astronomical Dawn",
                targetElevation = -18.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "First faint glow of morning twilight",
                phaseColorHex = TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex
            )
            // Golden Hour Evening (crossing +6.0° going down)
            prevElev >= 6.0 && nextElev < 6.0 -> TwilightTransitionEvent(
                name = "Evening Golden Hour",
                targetElevation = 6.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Warm golden light for photography",
                phaseColorHex = PhotoLightingWindow.GOLDEN_HOUR.badgeColorHex
            )
            // Blue Hour Evening (crossing -4.0° going down)
            prevElev >= -4.0 && nextElev < -4.0 -> TwilightTransitionEvent(
                name = "Evening Blue Hour",
                targetElevation = -4.0,
                timestamp = instant,
                localTime = localTime,
                minutesUntil = minutesUntil,
                formattedCountdown = formattedCountdown,
                description = "Cool twilight cityscape photography",
                phaseColorHex = PhotoLightingWindow.BLUE_HOUR.badgeColorHex
            )
            else -> null
        }
    }

    /**
     * Calculates the complete 24h milestone progression for given coordinates and date.
     */
    fun calculateDailySchedule(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): DailyTwilightSchedule {
        val startOfDay = date.atStartOfDay(zoneId)
        val currentTime = ZonedDateTime.now(zoneId)

        val milestoneElevationMap = listOf(
            Triple("Astronomical Dawn", -18.0, true),
            Triple("Nautical Dawn", -12.0, true),
            Triple("Civil Dawn", -6.0, true),
            Triple("Sunrise", 0.0, true),
            Triple("Morning Golden Hour", 6.0, true),
            Triple("Evening Golden Hour", 6.0, false),
            Triple("Sunset (Civil Dusk)", 0.0, false),
            Triple("Nautical Dusk", -6.0, false),
            Triple("Astronomical Dusk", -12.0, false),
            Triple("True Night", -18.0, false)
        )

        val milestones = mutableListOf<DailyTwilightMilestone>()

        // Scan the 24 hours of the day
        var prevElevation = calculateSolarElevation(latitude, longitude, startOfDay)
        val discoveredTimes = mutableMapOf<Pair<String, Boolean>, LocalTime>()

        for (minute in 1..1439) {
            val t = startOfDay.plusMinutes(minute.toLong())
            val elevation = calculateSolarElevation(latitude, longitude, t)
            val isRising = elevation >= prevElevation

            // Check crossings for key elevations: -18, -12, -6, 0, 6
            for (target in listOf(-18.0, -12.0, -6.0, 0.0, 6.0)) {
                if (isRising && prevElevation <= target && elevation > target) {
                    val key = Pair(target.toString(), true)
                    if (!discoveredTimes.containsKey(key)) {
                        discoveredTimes[key] = t.toLocalTime()
                    }
                } else if (!isRising && prevElevation >= target && elevation < target) {
                    val key = Pair(target.toString(), false)
                    if (!discoveredTimes.containsKey(key)) {
                        discoveredTimes[key] = t.toLocalTime()
                    }
                }
            }
            prevElevation = elevation
        }

        milestones.add(
            DailyTwilightMilestone(
                name = "Astronomical Dawn",
                elevationDeg = -18.0,
                time = discoveredTimes[Pair("-18.0", true)],
                description = "First hint of morning light",
                colorHex = TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-18.0", true)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Nautical Dawn",
                elevationDeg = -12.0,
                time = discoveredTimes[Pair("-12.0", true)],
                description = "Horizon begins to take shape",
                colorHex = TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-12.0", true)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Civil Dawn",
                elevationDeg = -6.0,
                time = discoveredTimes[Pair("-6.0", true)],
                description = "Bright twilight, outdoor activities possible",
                colorHex = TwilightPhase.CIVIL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-6.0", true)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Sunrise",
                elevationDeg = 0.0,
                time = discoveredTimes[Pair("0.0", true)],
                description = "Sun disc breaches the horizon",
                colorHex = TwilightPhase.DIRECT_SUNLIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("0.0", true)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Sunset (Civil Dusk)",
                elevationDeg = 0.0,
                time = discoveredTimes[Pair("0.0", false)],
                description = "Sun dips below the horizon",
                colorHex = TwilightPhase.CIVIL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("0.0", false)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Nautical Dusk",
                elevationDeg = -6.0,
                time = discoveredTimes[Pair("-6.0", false)],
                description = "Stars appear, sea horizon blurs",
                colorHex = TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-6.0", false)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "Astronomical Dusk",
                elevationDeg = -12.0,
                time = discoveredTimes[Pair("-12.0", false)],
                description = "Milky Way becomes visible",
                colorHex = TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-12.0", false)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )
        milestones.add(
            DailyTwilightMilestone(
                name = "True Night",
                elevationDeg = -18.0,
                time = discoveredTimes[Pair("-18.0", false)],
                description = "Complete celestial darkness",
                colorHex = TwilightPhase.NIGHT.primaryColorHex,
                isPassed = discoveredTimes[Pair("-18.0", false)]?.let { currentTime.toLocalTime().isAfter(it) } ?: false
            )
        )

        val dayOfYear = date.dayOfYear
        val declDeg = 23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))
        val peakElevation = (90.0 - abs(latitude - declDeg)).coerceIn(-90.0, 90.0)
        val nadirElevation = (-90.0 + abs(latitude + declDeg)).coerceIn(-90.0, 90.0)

        return DailyTwilightSchedule(
            date = date,
            milestones = milestones,
            peakElevationDeg = peakElevation,
            nadirElevationDeg = nadirElevation
        )
    }

    /**
     * Formats remaining minutes into human readable string ("2h 15m" or "45m").
     */
    fun formatCountdown(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    /**
     * Catalog of pre-configured major global cities across diverse latitudes and timezones.
     */
    fun getPredefinedCities(): List<TwilightCity> {
        return listOf(
            TwilightCity("london", "London", "United Kingdom", 51.5074, -0.1278, ZoneId.of("Europe/London"), "🇬🇧", "Europe"),
            TwilightCity("tokyo", "Tokyo", "Japan", 35.6762, 139.6503, ZoneId.of("Asia/Tokyo"), "🇯🇵", "Asia-Pacific"),
            TwilightCity("new_york", "New York", "United States", 40.7128, -74.0060, ZoneId.of("America/New_York"), "🇺🇸", "Americas"),
            TwilightCity("reykjavik", "Reykjavik", "Iceland", 64.1466, -21.9426, ZoneId.of("Atlantic/Reykjavik"), "🇮🇸", "Nordic / High Lat"),
            TwilightCity("sydney", "Sydney", "Australia", -33.8688, 151.2093, ZoneId.of("Australia/Sydney"), "🇦🇺", "Southern Hemisphere"),
            TwilightCity("paris", "Paris", "France", 48.8566, 2.3522, ZoneId.of("Europe/Paris"), "🇫🇷", "Europe"),
            TwilightCity("cairo", "Cairo", "Egypt", 30.0444, 31.2357, ZoneId.of("Africa/Cairo"), "🇪🇬", "Middle East & Africa"),
            TwilightCity("singapore", "Singapore", "Singapore", 1.3521, 103.8198, ZoneId.of("Asia/Singapore"), "🇸🇬", "Equatorial"),
            TwilightCity("tromso", "Tromsø", "Norway", 69.6492, 18.9553, ZoneId.of("Europe/Oslo"), "🇳🇴", "Arctic Circle"),
            TwilightCity("san_francisco", "San Francisco", "United States", 37.7749, -122.4194, ZoneId.of("America/Los_Angeles"), "🇺🇸", "Americas"),
            TwilightCity("honolulu", "Honolulu", "United States", 21.3069, -157.8583, ZoneId.of("Pacific/Honolulu"), "🇺🇸", "Pacific"),
            TwilightCity("sao_paulo", "São Paulo", "Brazil", -23.5505, -46.6333, ZoneId.of("America/Sao_Paulo"), "🇧🇷", "Americas"),
            TwilightCity("dubai", "Dubai", "United Arab Emirates", 25.2048, 55.2708, ZoneId.of("Asia/Dubai"), "🇦🇪", "Middle East & Africa"),
            TwilightCity("mumbai", "Mumbai", "India", 19.0760, 72.8777, ZoneId.of("Asia/Kolkata"), "🇮🇳", "Asia-Pacific"),
            TwilightCity("cape_town", "Cape Town", "South Africa", -33.9249, 18.4241, ZoneId.of("Africa/Johannesburg"), "🇿🇦", "Middle East & Africa")
        )
    }
}
