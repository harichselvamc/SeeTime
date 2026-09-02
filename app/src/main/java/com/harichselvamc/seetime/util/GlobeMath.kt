package com.harichselvamc.seetime.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class SubsolarPoint(
    val latitude: Double,
    val longitude: Double
)

data class ProjectedPoint(
    val x: Float,
    val y: Float,
    val isVisible: Boolean,
    val isDaylit: Boolean,
    val solarElevation: Double
)

data class GlobeCityPin(
    val name: String,
    val zoneId: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
)

object GlobeMath {

    val MAJOR_WORLD_CITIES = listOf(
        GlobeCityPin("Tokyo", "Asia/Tokyo", 35.6762, 139.6503, "Japan"),
        GlobeCityPin("London", "Europe/London", 51.5074, -0.1278, "United Kingdom"),
        GlobeCityPin("New York", "America/New_York", 40.7128, -74.0060, "USA"),
        GlobeCityPin("San Francisco", "America/Los_Angeles", 37.7749, -122.4194, "USA"),
        GlobeCityPin("Paris", "Europe/Paris", 48.8566, 2.3522, "France"),
        GlobeCityPin("Sydney", "Australia/Sydney", -33.8688, 151.2093, "Australia"),
        GlobeCityPin("Singapore", "Asia/Singapore", 1.3521, 103.8198, "Singapore"),
        GlobeCityPin("Kolkata", "Asia/Kolkata", 22.5726, 88.3639, "India"),
        GlobeCityPin("Dubai", "Asia/Dubai", 25.2048, 55.2708, "UAE"),
        GlobeCityPin("Sao Paulo", "America/Sao_Paulo", -23.5505, -46.6333, "Brazil"),
        GlobeCityPin("Cairo", "Africa/Cairo", 30.0444, 31.2357, "Egypt"),
        GlobeCityPin("Berlin", "Europe/Berlin", 52.5200, 13.4050, "Germany")
    )

    // Simplified polygon coordinate chains for major world continents
    val CONTINENT_POLYLINES: List<List<Pair<Double, Double>>> = listOf(
        // North America outline
        listOf(Pair(70.0, -160.0), Pair(60.0, -140.0), Pair(55.0, -130.0), Pair(48.0, -125.0), Pair(35.0, -120.0), Pair(25.0, -110.0), Pair(18.0, -100.0), Pair(15.0, -90.0), Pair(20.0, -85.0), Pair(30.0, -82.0), Pair(40.0, -74.0), Pair(45.0, -65.0), Pair(55.0, -60.0), Pair(65.0, -65.0), Pair(72.0, -95.0), Pair(70.0, -160.0)),
        // South America outline
        listOf(Pair(12.0, -75.0), Pair(5.0, -78.0), Pair(-5.0, -80.0), Pair(-18.0, -70.0), Pair(-35.0, -72.0), Pair(-55.0, -68.0), Pair(-50.0, -65.0), Pair(-35.0, -55.0), Pair(-22.0, -42.0), Pair(-8.0, -35.0), Pair(0.0, -50.0), Pair(8.0, -60.0), Pair(12.0, -75.0)),
        // Eurasia outline
        listOf(Pair(70.0, 30.0), Pair(60.0, 10.0), Pair(45.0, -5.0), Pair(36.0, -5.0), Pair(38.0, 15.0), Pair(35.0, 25.0), Pair(30.0, 35.0), Pair(25.0, 55.0), Pair(15.0, 75.0), Pair(10.0, 80.0), Pair(22.0, 90.0), Pair(15.0, 105.0), Pair(22.0, 115.0), Pair(35.0, 120.0), Pair(40.0, 130.0), Pair(50.0, 140.0), Pair(60.0, 160.0), Pair(70.0, 170.0), Pair(75.0, 100.0), Pair(70.0, 30.0)),
        // Africa outline
        listOf(Pair(35.0, -5.0), Pair(30.0, 10.0), Pair(32.0, 32.0), Pair(12.0, 44.0), Pair(-5.0, 40.0), Pair(-25.0, 32.0), Pair(-34.0, 20.0), Pair(-20.0, 12.0), Pair(5.0, 5.0), Pair(5.0, -10.0), Pair(15.0, -17.0), Pair(28.0, -12.0), Pair(35.0, -5.0)),
        // Australia outline
        listOf(Pair(-12.0, 130.0), Pair(-15.0, 122.0), Pair(-22.0, 114.0), Pair(-32.0, 116.0), Pair(-35.0, 138.0), Pair(-38.0, 145.0), Pair(-28.0, 153.0), Pair(-20.0, 148.0), Pair(-12.0, 142.0), Pair(-12.0, 130.0))
    )

    /**
     * Calculates current subsolar point (point on Earth directly beneath the Sun).
     */
    fun calculateSubsolarPoint(dateTime: ZonedDateTime): SubsolarPoint {
        val dayOfYear = dateTime.dayOfYear
        val hour = dateTime.hour + (dateTime.minute / 60.0) + (dateTime.second / 3600.0)

        // Fractional year (radians)
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (hour - 12) / 24.0)

        // Equation of time in minutes
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination (degrees)
        val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)
        val subsolarLat = Math.toDegrees(declRad)

        // Greenwich Hour Angle / Subsolar Longitude (degrees)
        val utcHour = dateTime.withZoneSameInstant(java.time.ZoneOffset.UTC).toLocalTime()
        val utcDecimalHours = utcHour.hour + (utcHour.minute / 60.0) + (utcHour.second / 3600.0)
        var subsolarLon = -(utcDecimalHours - 12.0) * 15.0 - (eqTime / 4.0)
        subsolarLon = ((subsolarLon + 180.0).mod(360.0) + 360.0).mod(360.0) - 180.0

        return SubsolarPoint(latitude = subsolarLat, longitude = subsolarLon)
    }

    /**
     * Projects a 3D geographic point (lat, lon) onto a 2D Orthographic screen plane.
     * Center of view is (centerLat, centerLon). Radius is globe pixel radius.
     */
    fun projectOrthographic(
        latDeg: Double,
        lonDeg: Double,
        centerLatDeg: Double,
        centerLonDeg: Double,
        radius: Float,
        subsolarPoint: SubsolarPoint
    ): ProjectedPoint {
        val lat = Math.toRadians(latDeg)
        val lon = Math.toRadians(lonDeg)
        val cLat = Math.toRadians(centerLatDeg)
        val cLon = Math.toRadians(centerLonDeg)

        val dLon = lon - cLon

        // Cosine of angular distance from view center
        val cosC = (sin(cLat) * sin(lat)) + (cos(cLat) * cos(lat) * cos(dLon))
        val isVisible = cosC >= 0.0

        val x = (radius * cos(lat) * sin(dLon)).toFloat()
        val y = (-radius * ((cos(cLat) * sin(lat)) - (sin(cLat) * cos(lat) * cos(dLon)))).toFloat()

        // Solar elevation at point
        val subLatRad = Math.toRadians(subsolarPoint.latitude)
        val subLonRad = Math.toRadians(subsolarPoint.longitude)
        val dSubLon = lon - subLonRad
        val cosSunAngle = (sin(lat) * sin(subLatRad)) + (cos(lat) * cos(subLatRad) * cos(dSubLon))
        val sunElevation = 90.0 - Math.toDegrees(acos(cosSunAngle.coerceIn(-1.0, 1.0)))
        val isDaylit = sunElevation >= -6.0 // Includes civil twilight

        return ProjectedPoint(
            x = x,
            y = y,
            isVisible = isVisible,
            isDaylit = isDaylit,
            solarElevation = sunElevation
        )
    }
}
