package com.harichselvamc.seetime.util

import java.time.Instant
import java.util.Locale

data class CelestialStar(
    val name: String,
    val constellation: String,
    val rightAscensionHours: Double, // RA in decimal hours (0.0 to 24.0)
    val declinationDeg: Double, // Dec in degrees (-90.0 to +90.0)
    val magnitude: Double,
    val spectralType: String,
    val emoji: String,
    val description: String
)

data class StarTransitStatus(
    val star: CelestialStar,
    val hourAngleHours: Double,
    val hoursUntilTransit: Double,
    val meridianAltitudeDeg: Double,
    val isCulminatingNow: Boolean,
    val formattedTransitIn: String
)

data class SiderealTimeDetails(
    val gmstHours: Double,
    val lstHours: Double,
    val gmstFormatted: String, // "18:42:15.340 GMST"
    val lstFormatted: String,  // "22:15:08.120 LST"
    val vernalEquinoxAngleDeg: Double, // RA = 0h angle
    val dailySiderealGainSeconds: Double
)

object SiderealTimeEngine {

    const val SIDEREAL_DAY_SECONDS = 86164.0905382 // 23h 56m 4.091s
    const val DAILY_SIDEREAL_GAIN_SECONDS = 235.9094618 // 3m 55.909s gain per solar day
    const val J2000_JD = 2451545.0 // Jan 1, 2000 12:00 UTC

    val MAJOR_CELESTIAL_STARS = listOf(
        CelestialStar("Sirius", "Canis Major", 6.7525, -16.7167, -1.46, "A1V (Blue-White)", "🌟", "Brightest star in Earth's night sky; double star system."),
        CelestialStar("Vega", "Lyra", 18.6156, 38.7836, 0.03, "A0V (Pure White)", "✨", "Standard calibration zero-magnitude star in the Northern sky."),
        CelestialStar("Betelgeuse", "Orion", 5.9194, 7.4071, 0.50, "M1-2 (Red Supergiant)", "🔴", "Massive red supergiant marking Orion's shoulder."),
        CelestialStar("Polaris", "Ursa Minor", 2.5303, 89.2641, 1.98, "F7 (Supergiant)", "🧭", "Current North Star aligned within 1° of celestial pole."),
        CelestialStar("Arcturus", "Boötes", 14.2611, 19.1822, -0.05, "K1.5 (Orange Giant)", "🟠", "Brightest star in northern celestial hemisphere."),
        CelestialStar("Rigel", "Orion", 5.2422, -8.2016, 0.13, "B8 (Blue Supergiant)", "💎", "Luminous blue supergiant at Orion's foot."),
        CelestialStar("Capella", "Auriga", 5.2781, 45.9980, 0.08, "G3 (Golden Giant)", "⭐", "Northern golden quadruplet star system."),
        CelestialStar("Antares", "Scorpius", 16.4900, -26.4320, 1.06, "M1 (Red Supergiant)", "🩸", "Heart of the Scorpion, fiery rival to Mars.")
    )

    /**
     * Calculates Greenwich Mean Sidereal Time (GMST) in decimal hours (0.0 to 24.0).
     */
    fun calculateGmstHours(epochMillis: Long): Double {
        val jd = (epochMillis / 86400000.0) + EpochConverterEngine.UNIX_TO_JULIAN_OFFSET
        val d = jd - J2000_JD

        // Standard IAU GMST polynomial formula
        var gmst = 18.697374558 + (24.06570982441908 * d)
        gmst = ((gmst % 24.0) + 24.0) % 24.0

        return gmst
    }

    /**
     * Calculates Local Sidereal Time (LST) in decimal hours for [longitudeDeg].
     */
    fun calculateLstHours(epochMillis: Long, longitudeDeg: Double): Double {
        val gmst = calculateGmstHours(epochMillis)
        val lonOffsetHours = longitudeDeg / 15.0
        var lst = gmst + lonOffsetHours
        lst = ((lst % 24.0) + 24.0) % 24.0
        return lst
    }

    /**
     * Formats decimal sidereal hours into HH:mm:ss.mmm string.
     */
    fun formatSiderealHours(decimalHours: Double): String {
        val safeH = ((decimalHours % 24.0) + 24.0) % 24.0
        val totalSec = safeH * 3600.0
        val h = (totalSec / 3600.0).toInt().coerceIn(0, 23)
        val m = ((totalSec % 3600.0) / 60.0).toInt().coerceIn(0, 59)
        val s = (totalSec % 60.0).toInt().coerceIn(0, 59)
        val ms = (((totalSec % 60.0) - s) * 1000.0).toInt().coerceIn(0, 999)

        return String.format(Locale.US, "%02d:%02d:%02d.%03d", h, m, s, ms)
    }

    /**
     * Calculates comprehensive sidereal time parameters for [epochMillis] and [longitudeDeg].
     */
    fun calculateSiderealTimeDetails(epochMillis: Long, longitudeDeg: Double = 0.0): SiderealTimeDetails {
        val gmst = calculateGmstHours(epochMillis)
        val lst = calculateLstHours(epochMillis, longitudeDeg)

        val vernalAngle = (lst / 24.0) * 360.0

        return SiderealTimeDetails(
            gmstHours = gmst,
            lstHours = lst,
            gmstFormatted = "${formatSiderealHours(gmst)} GMST",
            lstFormatted = "${formatSiderealHours(lst)} LST",
            vernalEquinoxAngleDeg = vernalAngle,
            dailySiderealGainSeconds = DAILY_SIDEREAL_GAIN_SECONDS
        )
    }

    /**
     * Calculates transit culmination status for a given celestial star.
     */
    fun calculateStarTransit(
        star: CelestialStar,
        lstHours: Double,
        observerLatDeg: Double
    ): StarTransitStatus {
        // Hour Angle = LST - RA (modulo 24 hours)
        var ha = lstHours - star.rightAscensionHours
        ha = ((ha % 24.0) + 24.0) % 24.0

        val hoursUntil = (24.0 - ha) % 24.0
        val isCulminating = ha < 0.15 || ha > 23.85 // Within ~9 minutes of meridian

        // Meridian Altitude = 90° - |Latitude - Declination|
        val alt = (90.0 - kotlin.math.abs(observerLatDeg - star.declinationDeg)).coerceIn(-90.0, 90.0)

        val untilHrs = hoursUntil.toInt()
        val untilMins = ((hoursUntil - untilHrs) * 60.0).toInt()

        val transitText = if (isCulminating) "CULMINATING NOW (Meridian Transit 🎯)"
                          else "Transit in ${untilHrs}h ${untilMins}m"

        return StarTransitStatus(
            star = star,
            hourAngleHours = ha,
            hoursUntilTransit = hoursUntil,
            meridianAltitudeDeg = alt,
            isCulminatingNow = isCulminating,
            formattedTransitIn = transitText
        )
    }
}
