package com.harichselvamc.seetime.util

import kotlin.math.pow
import kotlin.math.sqrt

data class OrbitalRelativityResult(
    val altitudeKm: Double,
    val orbitalVelocityKmS: Double,
    val gravitationalGainMicrosecondsPerDay: Double, // General Relativity (+us/day)
    val velocityLossMicrosecondsPerDay: Double, // Special Relativity (-us/day)
    val netDilationMicrosecondsPerDay: Double, // Net us/day
    val dailyPositioningErrorKm: Double, // Drift without Einstein correction
    val orbitName: String
)

data class PlanetaryLightDelay(
    val targetName: String,
    val emoji: String,
    val distanceKm: Double,
    val oneWayDelaySeconds: Double,
    val formattedDelay: String
)

object RelativityTimeEngine {

    const val SPEED_OF_LIGHT_M_S = 299792458.0 // c (m/s)
    const val SPEED_OF_LIGHT_KM_S = 299792.458 // c (km/s)
    const val GM_EARTH = 3.986004418e14 // GM in m^3 / s^2
    const val EARTH_RADIUS_M = 6371000.0 // r0 in meters
    const val SECONDS_PER_DAY = 86400.0

    val KNOWN_ORBITS = listOf(
        Pair(420.0, "ISS (Low Earth Orbit)"),
        Pair(20200.0, "GPS Satellite Constellation"),
        Pair(35786.0, "Geostationary Orbit (GEO)"),
        Pair(384400.0, "Moon Orbit")
    )

    val CELESTIAL_TARGETS = listOf(
        PlanetaryLightDelay("Moon (Lunar Surface)", "🌕", 384400.0, 384400.0 / SPEED_OF_LIGHT_KM_S, "1.28 seconds"),
        PlanetaryLightDelay("Sun (1 AU)", "☀️", 149597870.7, 149597870.7 / SPEED_OF_LIGHT_KM_S, "8.32 minutes"),
        PlanetaryLightDelay("Mars (Opposition)", "🔴", 54600000.0, 54600000.0 / SPEED_OF_LIGHT_KM_S, "3.04 minutes"),
        PlanetaryLightDelay("Mars (Average)", "🔴", 225000000.0, 225000000.0 / SPEED_OF_LIGHT_KM_S, "12.51 minutes"),
        PlanetaryLightDelay("Jupiter", "🪐", 778500000.0, 778500000.0 / SPEED_OF_LIGHT_KM_S, "43.28 minutes"),
        PlanetaryLightDelay("Voyager 1 (Interstellar)", "🛰️", 24500000000.0, 24500000000.0 / SPEED_OF_LIGHT_KM_S, "22.70 hours")
    )

    /**
     * Calculates combined General Relativity and Special Relativity time dilation for a circular orbit at [altitudeKm].
     */
    fun calculateOrbitalDilation(altitudeKm: Double, orbitName: String = "Custom Orbit"): OrbitalRelativityResult {
        val hMeters = altitudeKm * 1000.0
        val rMeters = EARTH_RADIUS_M + hMeters

        // Orbital velocity: v = sqrt(GM / r)
        val vMetersS = sqrt(GM_EARTH / rMeters)
        val vKmS = vMetersS / 1000.0

        val cSquared = SPEED_OF_LIGHT_M_S.pow(2.0)

        // General Relativity: Gravitational time dilation fractional shift
        // delta_t_GR = (GM / c^2) * (1/r0 - 1/r) * 86400 * 10^6 (microseconds/day)
        val grFraction = (GM_EARTH / cSquared) * ((1.0 / EARTH_RADIUS_M) - (1.0 / rMeters))
        val grMicrosecondsPerDay = grFraction * SECONDS_PER_DAY * 1_000_000.0

        // Special Relativity: Kinematic velocity time dilation fractional shift
        // delta_t_SR = -0.5 * (v^2 / c^2) * 86400 * 10^6 (microseconds/day)
        val srFraction = -0.5 * (vMetersS.pow(2.0) / cSquared)
        val srMicrosecondsPerDay = srFraction * SECONDS_PER_DAY * 1_000_000.0

        val netMicrosecondsPerDay = grMicrosecondsPerDay + srMicrosecondsPerDay

        // Positioning error drift: error = delta_t_net * c (in km)
        val netSeconds = netMicrosecondsPerDay / 1_000_000.0
        val driftKm = kotlin.math.abs(netSeconds * SPEED_OF_LIGHT_KM_S)

        return OrbitalRelativityResult(
            altitudeKm = altitudeKm,
            orbitalVelocityKmS = vKmS,
            gravitationalGainMicrosecondsPerDay = grMicrosecondsPerDay,
            velocityLossMicrosecondsPerDay = srMicrosecondsPerDay,
            netDilationMicrosecondsPerDay = netMicrosecondsPerDay,
            dailyPositioningErrorKm = driftKm,
            orbitName = orbitName
        )
    }

    /**
     * Calculates the Lorentz Factor gamma = 1 / sqrt(1 - (v/c)^2) for a given speed fraction [beta] (0.0 to 0.999c).
     */
    fun calculateLorentzFactor(beta: Double): Double {
        val clampedBeta = beta.coerceIn(0.0, 0.9999)
        return 1.0 / sqrt(1.0 - clampedBeta.pow(2.0))
    }

    /**
     * Calculates time experienced by an astronaut traveling for [earthYears] at [beta] speed of light.
     */
    fun calculateShipTravelTime(earthYears: Double, beta: Double): Double {
        val gamma = calculateLorentzFactor(beta)
        return earthYears / gamma
    }
}
