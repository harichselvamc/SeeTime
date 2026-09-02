package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class PlanetId(
    val displayName: String,
    val colorHex: Long,
    val symbol: String,
    val radiusKm: Double,
    val semiMajorAxisAu: Double,
    val orbitalPeriodDays: Double,
    val eccentricity: Double,
    val longitudeOfPerihelionDeg: Double,
    val meanLongitudeJ2000Deg: Double,
    val isInnerPlanet: Boolean,
    val funFact: String
) {
    MERCURY(
        displayName = "Mercury",
        colorHex = 0xFFA0AAB2,
        symbol = "☿",
        radiusKm = 2439.7,
        semiMajorAxisAu = 0.387098,
        orbitalPeriodDays = 87.969,
        eccentricity = 0.205630,
        longitudeOfPerihelionDeg = 77.456,
        meanLongitudeJ2000Deg = 252.251,
        isInnerPlanet = true,
        funFact = "Fastest planet in the solar system, completing an orbit every 88 Earth days."
    ),
    VENUS(
        displayName = "Venus",
        colorHex = 0xFFE0C17B,
        symbol = "♀",
        radiusKm = 6051.8,
        semiMajorAxisAu = 0.723332,
        orbitalPeriodDays = 224.701,
        eccentricity = 0.006773,
        longitudeOfPerihelionDeg = 131.572,
        meanLongitudeJ2000Deg = 181.979,
        isInnerPlanet = true,
        funFact = "Has the most circular orbit of any planet (e=0.0068) and rotates backwards."
    ),
    EARTH(
        displayName = "Earth",
        colorHex = 0xFF3B82F6,
        symbol = "♁",
        radiusKm = 6371.0,
        semiMajorAxisAu = 1.000000,
        orbitalPeriodDays = 365.256,
        eccentricity = 0.016708,
        longitudeOfPerihelionDeg = 102.947,
        meanLongitudeJ2000Deg = 100.464,
        isInnerPlanet = true,
        funFact = "Our home planet moves at an average velocity of 29.78 km/s around the Sun."
    ),
    MARS(
        displayName = "Mars",
        colorHex = 0xFFEF4444,
        symbol = "♂",
        radiusKm = 3389.5,
        semiMajorAxisAu = 1.523679,
        orbitalPeriodDays = 686.980,
        eccentricity = 0.093400,
        longitudeOfPerihelionDeg = 336.040,
        meanLongitudeJ2000Deg = 355.453,
        isInnerPlanet = true,
        funFact = "The Red Planet has noticeable orbital eccentricity, varying from 1.38 AU to 1.67 AU."
    ),
    JUPITER(
        displayName = "Jupiter",
        colorHex = 0xFFD97706,
        symbol = "♃",
        radiusKm = 69911.0,
        semiMajorAxisAu = 5.204267,
        orbitalPeriodDays = 4332.589, // 11.86 years
        eccentricity = 0.048498,
        longitudeOfPerihelionDeg = 14.728,
        meanLongitudeJ2000Deg = 34.404,
        isInnerPlanet = false,
        funFact = "Largest planet, containing more mass than all other solar system planets combined."
    ),
    SATURN(
        displayName = "Saturn",
        colorHex = 0xFFFBBF24,
        symbol = "♄",
        radiusKm = 58232.0,
        semiMajorAxisAu = 9.582017,
        orbitalPeriodDays = 10759.22, // 29.46 years
        eccentricity = 0.055546,
        longitudeOfPerihelionDeg = 92.598,
        meanLongitudeJ2000Deg = 49.944,
        isInnerPlanet = false,
        funFact = "Spectacular ring system spanning 282,000 km but only ~10 meters thick."
    ),
    URANUS(
        displayName = "Uranus",
        colorHex = 0xFF06B6D4,
        symbol = "♅",
        radiusKm = 25362.0,
        semiMajorAxisAu = 19.229411,
        orbitalPeriodDays = 30685.4, // 84.01 years
        eccentricity = 0.046381,
        longitudeOfPerihelionDeg = 170.964,
        meanLongitudeJ2000Deg = 313.232,
        isInnerPlanet = false,
        funFact = "Tilted 97.8 degrees on its side, rolling around the Sun like a ball."
    ),
    NEPTUNE(
        displayName = "Neptune",
        colorHex = 0xFF4F46E5,
        symbol = "♆",
        radiusKm = 24622.0,
        semiMajorAxisAu = 30.057268,
        orbitalPeriodDays = 60189.0, // 164.79 years
        eccentricity = 0.009456,
        longitudeOfPerihelionDeg = 44.971,
        meanLongitudeJ2000Deg = 304.880,
        isInnerPlanet = false,
        funFact = "Discovered via mathematical prediction before it was directly observed by telescope in 1846."
    )
}

data class PlanetOrbitState(
    val planet: PlanetId,
    val xAu: Double,
    val yAu: Double,
    val distanceAu: Double,
    val distanceKm: Double,
    val trueAnomalyDeg: Double,
    val heliocentricLongitudeDeg: Double,
    val orbitalVelocityKmPerSec: Double,
    val zodiacSign: String,
    val zodiacEmoji: String,
    val orbitProgressPercent: Float
)

object SolarOrreryEngine {

    // J2000.0 Epoch: Jan 1, 2000, 12:00 UTC (JD 2451545.0)
    private const val J2000_EPOCH_MILLIS = 946728000000L
    private const val KM_PER_AU = 149597870.7

    val ZODIAC_SIGNS = listOf(
        Triple("Aries", "♈", 0.0..30.0),
        Triple("Taurus", "♉", 30.0..60.0),
        Triple("Gemini", "♊", 60.0..90.0),
        Triple("Cancer", "♋", 90.0..120.0),
        Triple("Leo", "♌", 120.0..150.0),
        Triple("Virgo", "♍", 150.0..180.0),
        Triple("Libra", "♎", 180.0..210.0),
        Triple("Scorpio", "♏", 210.0..240.0),
        Triple("Sagittarius", "♐", 240.0..270.0),
        Triple("Capricorn", "♑", 270.0..300.0),
        Triple("Aquarius", "♒", 300.0..330.0),
        Triple("Pisces", "♓", 330.0..360.0)
    )

    /**
     * Solves Kepler's Equation M = E - e * sin(E) using Newton-Raphson iteration.
     */
    fun solveKepler(meanAnomalyRad: Double, eccentricity: Double): Double {
        var eAnomaly = meanAnomalyRad
        for (i in 0 until 12) {
            val delta = (eAnomaly - eccentricity * sin(eAnomaly) - meanAnomalyRad) / (1.0 - eccentricity * cos(eAnomaly))
            eAnomaly -= delta
            if (kotlin.math.abs(delta) < 1e-9) break
        }
        return eAnomaly
    }

    /**
     * Calculates the heliocentric coordinates and orbital state for a specific [planet] at [instant].
     */
    fun calculatePlanetState(planet: PlanetId, instant: Instant): PlanetOrbitState {
        val daysSinceJ2000 = (instant.toEpochMilli() - J2000_EPOCH_MILLIS) / (1000.0 * 86400.0)

        // Mean daily motion n = 360 / T
        val meanDailyMotionDeg = 360.0 / planet.orbitalPeriodDays
        val meanLongitudeDeg = (planet.meanLongitudeJ2000Deg + meanDailyMotionDeg * daysSinceJ2000) % 360.0
        val meanLongitudeNorm = if (meanLongitudeDeg < 0) meanLongitudeDeg + 360.0 else meanLongitudeDeg

        // Mean Anomaly M = L - \varpi
        val meanAnomalyDeg = (meanLongitudeNorm - planet.longitudeOfPerihelionDeg) % 360.0
        val meanAnomalyNorm = if (meanAnomalyDeg < 0) meanAnomalyDeg + 360.0 else meanAnomalyDeg
        val meanAnomalyRad = meanAnomalyNorm * (PI / 180.0)

        // Solve Kepler for Eccentric Anomaly E
        val eAnomalyRad = solveKepler(meanAnomalyRad, planet.eccentricity)

        // True Anomaly \nu = 2 * atan(sqrt((1+e)/(1-e)) * tan(E/2))
        val trueAnomalyRad = 2.0 * atan2(
            sqrt(1.0 + planet.eccentricity) * sin(eAnomalyRad / 2.0),
            sqrt(1.0 - planet.eccentricity) * cos(eAnomalyRad / 2.0)
        )
        val trueAnomalyDeg = ((trueAnomalyRad * (180.0 / PI)) % 360.0).let { if (it < 0) it + 360.0 else it }

        // Heliocentric distance r = a * (1 - e * cos(E))
        val distanceAu = planet.semiMajorAxisAu * (1.0 - planet.eccentricity * cos(eAnomalyRad))
        val distanceKm = distanceAu * KM_PER_AU

        // Heliocentric Ecliptic Longitude \lambda = \nu + \varpi
        val longitudeDeg = (trueAnomalyDeg + planet.longitudeOfPerihelionDeg) % 360.0
        val longitudeNorm = if (longitudeDeg < 0) longitudeDeg + 360.0 else longitudeDeg
        val longitudeRad = longitudeNorm * (PI / 180.0)

        // Coordinates in Heliocentric Orbit Plane
        val xAu = distanceAu * cos(longitudeRad)
        val yAu = distanceAu * sin(longitudeRad)

        // Instantaneous Orbital Velocity: v = 29.784 km/s * sqrt(2/r - 1/a)
        val velocity = 29.784 * sqrt((2.0 / distanceAu) - (1.0 / planet.semiMajorAxisAu))

        // Zodiac Sign matching
        val zodiac = ZODIAC_SIGNS.find { longitudeNorm in it.third } ?: ZODIAC_SIGNS.first()

        val progressPercent = (meanAnomalyNorm / 360.0).toFloat()

        return PlanetOrbitState(
            planet = planet,
            xAu = xAu,
            yAu = yAu,
            distanceAu = distanceAu,
            distanceKm = distanceKm,
            trueAnomalyDeg = trueAnomalyDeg,
            heliocentricLongitudeDeg = longitudeNorm,
            orbitalVelocityKmPerSec = velocity,
            zodiacSign = zodiac.first,
            zodiacEmoji = zodiac.second,
            orbitProgressPercent = progressPercent
        )
    }

    /**
     * Calculates all 8 planets' states simultaneously.
     */
    fun calculateAllPlanets(instant: Instant = Instant.now()): List<PlanetOrbitState> {
        return PlanetId.values().map { calculatePlanetState(it, instant) }
    }
}
