package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Optical and radio visibility categorization for an ISS overhead pass.
 */
enum class IssVisibilityType(
    val displayName: String,
    val description: String,
    val isOpticallyVisible: Boolean,
    val badgeColorHex: Long,
    val emoji: String
) {
    BRIGHT_VISIBLE(
        displayName = "Visible Pass (Bright Stargaze)",
        description = "Sunlit space station against dark/twilight sky. Magnificent naked-eye satellite flare.",
        isOpticallyVisible = true,
        badgeColorHex = 0xFF22C55E, // Vibrant Emerald Green
        emoji = "🌟"
    ),
    FAINT_TWILIGHT(
        displayName = "Twilight Pass (Moderate)",
        description = "Sunlit station visible during civil twilight against moderate sky glow.",
        isOpticallyVisible = true,
        badgeColorHex = 0xFFF59E0B, // Amber
        emoji = "✨"
    ),
    DAYLIGHT_PASS(
        displayName = "Daylight Pass (Radio Only)",
        description = "Sunlit station passing in direct daytime sky. Detectable via amateur radio (145.80 MHz).",
        isOpticallyVisible = false,
        badgeColorHex = 0xFF3B82F6, // Blue
        emoji = "☀️"
    ),
    ECLIPSED_PASS(
        displayName = "Eclipsed / Earth Shadow",
        description = "Station is inside Earth's umbral shadow cone. Radio reception possible.",
        isOpticallyVisible = false,
        badgeColorHex = 0xFF64748B, // Slate
        emoji = "🌑"
    )
}

/**
 * Instantaneous sub-satellite coordinates and orbital velocities for the ISS.
 */
data class IssCoordinates(
    val latitude: Double, // Sub-satellite latitude in degrees (-51.64° to +51.64°)
    val longitude: Double, // Sub-satellite longitude in degrees (-180° to +180°)
    val altitudeKm: Double, // Orbital altitude above WGS84 ellipsoid (~418..422 km)
    val orbitalVelocityKmS: Double, // Orbital velocity (~7.66 km/s)
    val groundSpeedKmH: Double, // Ground speed (~27,600 km/h)
    val isSunlit: Boolean, // True if outside Earth umbral shadow
    val timestamp: Instant
)

/**
 * Observer topocentric look-angles toward the ISS from a ground location.
 */
data class ObserverLookAngle(
    val azimuthDeg: Double, // 0°..360° clockwise from True North
    val elevationDeg: Double, // -90°..+90° above horizon
    val slantRangeKm: Double, // Line-of-sight distance in km
    val isAboveHorizon: Boolean, // True if elevation > 0°
    val compassDirection: String // 16-point cardinal compass ("NW", "ESE", etc.)
)

/**
 * Predicted overhead pass details for ground observers.
 */
data class IssOverheadPass(
    val id: String,
    val aosTime: Instant, // Acquisition of Signal (Rise above threshold)
    val culminationTime: Instant, // Max Elevation
    val losTime: Instant, // Loss of Signal (Set below threshold)
    val durationSeconds: Long,
    val maxElevationDeg: Double,
    val riseAzimuthDeg: Double,
    val riseDirection: String,
    val setAzimuthDeg: Double,
    val setDirection: String,
    val visibilityType: IssVisibilityType,
    val brightnessMagnitudeEstimate: Double, // Visual magnitude (-3.8 to 0.0)
    val observerCity: String
)

/**
 * Sample point along the 2D Mercator orbital ground track.
 */
data class IssGroundTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val isPast: Boolean,
    val minuteOffset: Int,
    val isCurrent: Boolean = false
)

object IssPassPredictorEngine {

    const val EARTH_RADIUS_KM = 6371.0
    const val ISS_MEAN_ALTITUDE_KM = 420.0
    const val ISS_INCLINATION_DEG = 51.6435 // Standard ISS orbital inclination
    const val ISS_ORBITAL_PERIOD_MINUTES = 92.90 // ~15.5 orbits per day
    const val ISS_ORBITAL_VELOCITY_KM_S = 7.66
    const val ISS_GROUND_SPEED_KM_H = 27576.0 // 7.66 * 3600
    const val NODAL_PRECESSION_DEG_PER_DAY = -5.02 // J2 nodal regression
    const val REFERENCE_EPOCH_MILLIS = 1788220800000L // Sep 1, 2026 00:00 UTC

    /**
     * Calculates instantaneous sub-satellite coordinates (latitude, longitude, altitude) for given epoch timestamp.
     */
    fun calculateIssPosition(epochMillis: Long): IssCoordinates {
        val deltaMillis = epochMillis - REFERENCE_EPOCH_MILLIS
        val deltaMinutes = deltaMillis / 60000.0

        // Mean orbital motion in radians/minute
        val meanMotionRadPerMin = (2.0 * PI) / ISS_ORBITAL_PERIOD_MINUTES
        val argumentOfLatitude = (meanMotionRadPerMin * deltaMinutes) % (2.0 * PI)

        val incRad = Math.toRadians(ISS_INCLINATION_DEG)

        // Sub-satellite latitude: sin(lat) = sin(inc) * sin(u)
        val sinLat = sin(incRad) * sin(argumentOfLatitude)
        val latRad = asin(sinLat.coerceIn(-1.0, 1.0))
        val latDeg = Math.toDegrees(latRad)

        // Right ascension in orbital plane: tan(alpha) = cos(inc) * tan(u)
        val alphaRad = atan2(cos(incRad) * sin(argumentOfLatitude), cos(argumentOfLatitude))

        // Nodal regression (Westward drift of ascending node)
        val nodalPrecessionRad = Math.toRadians((NODAL_PRECESSION_DEG_PER_DAY / 1440.0) * deltaMinutes)

        // Greenwich Hour Angle / Earth rotation rate: ~360° per 1436.068 min
        val earthRotationRad = (2.0 * PI / 1436.06818) * deltaMinutes

        // Initial Greenwich RA offset
        val initialAscendingNodeRad = Math.toRadians(45.0)

        // Sub-satellite longitude
        var lonRad = initialAscendingNodeRad + nodalPrecessionRad + alphaRad - earthRotationRad
        var lonDeg = Math.toDegrees(lonRad)
        lonDeg = ((lonDeg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0

        // Slight orbital altitude eccentricity variation (~418 km at perigee to 422 km at apogee)
        val altitudeKm = ISS_MEAN_ALTITUDE_KM + 2.0 * cos(argumentOfLatitude)

        val isSunlit = isSatelliteSunlit(latDeg, lonDeg, altitudeKm, epochMillis)

        return IssCoordinates(
            latitude = latDeg,
            longitude = lonDeg,
            altitudeKm = altitudeKm,
            orbitalVelocityKmS = ISS_ORBITAL_VELOCITY_KM_S,
            groundSpeedKmH = ISS_GROUND_SPEED_KM_H,
            isSunlit = isSunlit,
            timestamp = Instant.ofEpochMilli(epochMillis)
        )
    }

    /**
     * Calculates topocentric look-angles (Azimuth, Elevation, Slant Range) from a ground observer to the ISS.
     */
    fun calculateLookAngle(
        observerLat: Double,
        observerLon: Double,
        epochMillis: Long
    ): ObserverLookAngle {
        val issPos = calculateIssPosition(epochMillis)

        val obsLatRad = Math.toRadians(observerLat)
        val obsLonRad = Math.toRadians(observerLon)
        val rObs = EARTH_RADIUS_KM

        // Observer ECEF coordinates
        val xObs = rObs * cos(obsLatRad) * cos(obsLonRad)
        val yObs = rObs * cos(obsLatRad) * sin(obsLonRad)
        val zObs = rObs * sin(obsLatRad)

        // Satellite ECEF coordinates
        val satLatRad = Math.toRadians(issPos.latitude)
        val satLonRad = Math.toRadians(issPos.longitude)
        val rSat = EARTH_RADIUS_KM + issPos.altitudeKm

        val xSat = rSat * cos(satLatRad) * cos(satLonRad)
        val ySat = rSat * cos(satLatRad) * sin(satLonRad)
        val zSat = rSat * sin(satLatRad)

        // Range vector: rho = rSat - rObs
        val dx = xSat - xObs
        val dy = ySat - yObs
        val dz = zSat - zObs
        val slantRange = sqrt(dx * dx + dy * dy + dz * dz)

        // Topocentric East, North, Up (ENU) frame:
        // East unit vector
        val eX = -sin(obsLonRad)
        val eY = cos(obsLonRad)
        val eZ = 0.0

        // North unit vector
        val nX = -sin(obsLatRad) * cos(obsLonRad)
        val nY = -sin(obsLatRad) * sin(obsLonRad)
        val nZ = cos(obsLatRad)

        // Up unit vector
        val uX = cos(obsLatRad) * cos(obsLonRad)
        val uY = cos(obsLatRad) * sin(obsLonRad)
        val uZ = sin(obsLatRad)

        // Project range vector onto ENU axes
        val east = dx * eX + dy * eY + dz * eZ
        val north = dx * nX + dy * nY + dz * nZ
        val up = dx * uX + dy * uY + dz * uZ

        val elevationRad = asin((up / slantRange).coerceIn(-1.0, 1.0))
        val elevationDeg = Math.toDegrees(elevationRad)

        var azimuthRad = atan2(east, north)
        var azimuthDeg = Math.toDegrees(azimuthRad)
        if (azimuthDeg < 0.0) azimuthDeg += 360.0

        return ObserverLookAngle(
            azimuthDeg = azimuthDeg,
            elevationDeg = elevationDeg,
            slantRangeKm = slantRange,
            isAboveHorizon = elevationDeg > 0.0,
            compassDirection = getCompassDirection(azimuthDeg)
        )
    }

    /**
     * Determines whether the ISS is outside Earth's umbral shadow cone at given coordinates.
     */
    fun isSatelliteSunlit(
        satLat: Double,
        satLon: Double,
        satAltKm: Double,
        epochMillis: Long
    ): Boolean {
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"))
        val dayOfYear = zdt.dayOfYear
        val hour = zdt.hour + (zdt.minute / 60.0) + (zdt.second / 3600.0)

        // Subsolar point
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (hour - 12) / 24.0)
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)

        val sunLat = Math.toDegrees(decl)
        val sunLon = -((hour - 12.0) * 15.0)

        // Angular distance psi between satellite and subsolar point
        val satLatRad = Math.toRadians(satLat)
        val sunLatRad = Math.toRadians(sunLat)
        val dLonRad = Math.toRadians(satLon - sunLon)

        val cosPsi = sin(satLatRad) * sin(sunLatRad) + cos(satLatRad) * cos(sunLatRad) * cos(dLonRad)

        // If satellite is on illuminated hemisphere
        if (cosPsi >= 0.0) return true

        // Satellite is on nightside: check shadow clearance
        val psi = acos(cosPsi.coerceIn(-1.0, 1.0))
        val rSat = EARTH_RADIUS_KM + satAltKm
        val distToShadowAxis = rSat * sin(psi)

        return distToShadowAxis > EARTH_RADIUS_KM
    }

    /**
     * Scans forward in time to predict all upcoming overhead passes above [minElevationDeg] for ground observer.
     */
    fun predictUpcomingPasses(
        observerLat: Double,
        observerLon: Double,
        startEpochMillis: Long,
        durationHours: Int = 72,
        minElevationDeg: Double = 10.0,
        observerCityName: String = "Observer Location"
    ): List<IssOverheadPass> {
        val passes = mutableListOf<IssOverheadPass>()
        val totalMinutes = durationHours * 60

        var inPass = false
        var aosMillis = 0L
        var aosAzimuth = 0.0
        var maxElevation = -90.0
        var maxElevationMillis = 0L
        var passIndex = 1

        for (m in 0..totalMinutes) {
            val t = startEpochMillis + (m * 60000L)
            val look = calculateLookAngle(observerLat, observerLon, t)

            if (look.elevationDeg >= minElevationDeg) {
                if (!inPass) {
                    // Acquisition of Signal (AOS)
                    inPass = true
                    aosMillis = t
                    aosAzimuth = look.azimuthDeg
                    maxElevation = look.elevationDeg
                    maxElevationMillis = t
                } else {
                    if (look.elevationDeg > maxElevation) {
                        maxElevation = look.elevationDeg
                        maxElevationMillis = t
                    }
                }
            } else {
                if (inPass) {
                    // Loss of Signal (LOS)
                    inPass = false
                    val losMillis = t
                    val losAzimuth = look.azimuthDeg
                    val durationSec = (losMillis - aosMillis) / 1000L

                    // Determine optical visibility category
                    val midZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(maxElevationMillis), ZoneId.of("UTC"))
                    val sunElevation = TwilightCalculatorEngine.calculateSolarElevation(observerLat, observerLon, midZdt)
                    val issSunlit = isSatelliteSunlit(
                        satLat = calculateIssPosition(maxElevationMillis).latitude,
                        satLon = calculateIssPosition(maxElevationMillis).longitude,
                        satAltKm = ISS_MEAN_ALTITUDE_KM,
                        epochMillis = maxElevationMillis
                    )

                    val visType = when {
                        !issSunlit -> IssVisibilityType.ECLIPSED_PASS
                        sunElevation <= -6.0 -> IssVisibilityType.BRIGHT_VISIBLE
                        sunElevation in -6.0..-1.0 -> IssVisibilityType.FAINT_TWILIGHT
                        else -> IssVisibilityType.DAYLIGHT_PASS
                    }

                    // Estimate magnitude: peak overhead (90°) ~ -3.8 mag, 10° ~ -1.0 mag
                    val peakRatio = (maxElevation - 10.0) / 80.0
                    val mag = -1.0 - (peakRatio * 2.8)

                    passes.add(
                        IssOverheadPass(
                            id = "pass-$passIndex-${Instant.ofEpochMilli(aosMillis).epochSecond}",
                            aosTime = Instant.ofEpochMilli(aosMillis),
                            culminationTime = Instant.ofEpochMilli(maxElevationMillis),
                            losTime = Instant.ofEpochMilli(losMillis),
                            durationSeconds = durationSec,
                            maxElevationDeg = maxElevation,
                            riseAzimuthDeg = aosAzimuth,
                            riseDirection = getCompassDirection(aosAzimuth),
                            setAzimuthDeg = losAzimuth,
                            setDirection = getCompassDirection(losAzimuth),
                            visibilityType = visType,
                            brightnessMagnitudeEstimate = mag,
                            observerCity = observerCityName
                        )
                    )
                    passIndex++
                }
            }
        }

        return passes
    }

    /**
     * Generates past and future orbital ground track points for 2D map visualization.
     */
    fun calculateGroundTrack(
        currentEpochMillis: Long,
        pastMinutes: Int = 45,
        futureMinutes: Int = 90,
        stepMinutes: Int = 1
    ): List<IssGroundTrackPoint> {
        val points = mutableListOf<IssGroundTrackPoint>()

        for (m in -pastMinutes..futureMinutes step stepMinutes) {
            val t = currentEpochMillis + (m * 60000L)
            val pos = calculateIssPosition(t)
            points.add(
                IssGroundTrackPoint(
                    latitude = pos.latitude,
                    longitude = pos.longitude,
                    isPast = m < 0,
                    minuteOffset = m,
                    isCurrent = m == 0
                )
            )
        }

        return points
    }

    /**
     * Converts azimuth degrees to standard 16-point cardinal compass directions.
     */
    fun getCompassDirection(azimuthDeg: Double): String {
        val normalized = ((azimuthDeg % 360.0) + 360.0) % 360.0
        val directions = arrayOf(
            "N", "NNE", "NE", "ENE",
            "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW",
            "W", "WNW", "NW", "NNW", "N"
        )
        val index = ((normalized + 11.25) / 22.5).toInt()
        return directions[index.coerceIn(0, 16)]
    }
}
