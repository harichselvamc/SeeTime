package com.harichselvamc.seetime.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class SubsolarCoordinates(
    val latitude: Double,
    val longitude: Double,
    val declinationDeg: Double,
    val ghaDeg: Double,
    val equationOfTimeMin: Double,
    val regionDescription: String,
    val hemisphere: String,
    val tropicZone: String,
    val utcDateTime: ZonedDateTime
)

data class ObserverZenithTelemetry(
    val observerCityName: String,
    val observerCountry: String,
    val observerLat: Double,
    val observerLon: Double,
    val solarElevationDeg: Double,
    val zenithAngleDeg: Double,
    val greatCircleDistanceKm: Double,
    val solarAzimuthDeg: Double,
    val shadowLength1mPoleMeters: Double,
    val isDaylight: Boolean,
    val zenithStatus: String
)

data class LahainaNoonEvent(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isTropical: Boolean,
    val firstDate: LocalDate?,
    val secondDate: LocalDate?,
    val nextDate: LocalDate?,
    val daysUntilNext: Long?,
    val isTodayZeroShadow: Boolean
)

data class SubsolarCityPreset(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

object SubsolarEngine {

    const val EARTH_RADIUS_KM = 6371.0
    const val TROPIC_OF_CANCER_LAT = 23.4365
    const val TROPIC_OF_CAPRICORN_LAT = -23.4365

    val TROPICAL_CITY_PRESETS = listOf(
        SubsolarCityPreset("Honolulu", "USA (Hawaii)", 21.3069, -157.8583),
        SubsolarCityPreset("Mecca", "Saudi Arabia", 21.4225, 39.8262),
        SubsolarCityPreset("Singapore", "Singapore", 1.3521, 103.8198),
        SubsolarCityPreset("Nairobi", "Kenya", -1.2921, 36.8219),
        SubsolarCityPreset("Mexico City", "Mexico", 19.4326, -99.1332),
        SubsolarCityPreset("Mumbai", "India", 19.0760, 72.8777),
        SubsolarCityPreset("Bangkok", "Thailand", 13.7563, 100.5018),
        SubsolarCityPreset("Rio de Janeiro", "Brazil", -22.9068, -43.1729),
        SubsolarCityPreset("San Juan", "Puerto Rico", 18.4655, -66.1057),
        SubsolarCityPreset("Kuala Lumpur", "Malaysia", 3.1390, 101.6869),
        SubsolarCityPreset("London", "United Kingdom", 51.5074, -0.1278),
        SubsolarCityPreset("Tokyo", "Japan", 35.6762, 139.6503),
        SubsolarCityPreset("New York", "USA", 40.7128, -74.0060)
    )

    /**
     * Calculates the instantaneous subsolar coordinates for a given ZonedDateTime.
     */
    fun calculateSubsolarPoint(dateTime: ZonedDateTime): SubsolarCoordinates {
        val utcZoned = dateTime.withZoneSameInstant(ZoneOffset.UTC)
        val dayOfYear = utcZoned.dayOfYear
        val hour = utcZoned.hour + (utcZoned.minute / 60.0) + (utcZoned.second / 3600.0) + (utcZoned.nano / 3_600_000_000_000.0)

        // Fractional year (radians)
        val gamma = 2.0 * PI / 365.2422 * (dayOfYear - 1.0 + (hour - 12.0) / 24.0)

        // Equation of Time in minutes
        val eqTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2.0 * gamma) -
                0.040849 * sin(2.0 * gamma)
        )

        // Solar declination in radians
        val declRad = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2.0 * gamma) +
            0.000907 * sin(2.0 * gamma)
        val declinationDeg = Math.toDegrees(declRad)

        // Subsolar longitude / Greenwich Hour Angle
        val gha = (hour - 12.0) * 15.0 + (eqTime / 4.0)
        var subsolarLon = -gha
        subsolarLon = ((subsolarLon + 180.0).mod(360.0) + 360.0).mod(360.0) - 180.0

        val normalizedGha = ((gha.mod(360.0)) + 360.0).mod(360.0)
        val region = getRegionDescription(declinationDeg, subsolarLon)

        val hemisphere = when {
            declinationDeg > 0.5 -> "Northern Hemisphere"
            declinationDeg < -0.5 -> "Southern Hemisphere"
            else -> "Equatorial Equinox"
        }

        val tropic = when {
            declinationDeg >= 22.0 -> "Tropic of Cancer (+23.4° N)"
            declinationDeg <= -22.0 -> "Tropic of Capricorn (-23.4° S)"
            abs(declinationDeg) <= 5.0 -> "Thermal Equator"
            declinationDeg > 0 -> "Northern Tropics"
            else -> "Southern Tropics"
        }

        return SubsolarCoordinates(
            latitude = declinationDeg,
            longitude = subsolarLon,
            declinationDeg = declinationDeg,
            ghaDeg = normalizedGha,
            equationOfTimeMin = eqTime,
            regionDescription = region,
            hemisphere = hemisphere,
            tropicZone = tropic,
            utcDateTime = utcZoned
        )
    }

    /**
     * Calculates the observer zenith telemetry relative to the subsolar point.
     */
    fun calculateObserverZenith(
        observerLat: Double,
        observerLon: Double,
        observerCityName: String,
        observerCountry: String,
        dateTime: ZonedDateTime
    ): ObserverZenithTelemetry {
        val subsolar = calculateSubsolarPoint(dateTime)

        val lat1Rad = Math.toRadians(observerLat)
        val lon1Rad = Math.toRadians(observerLon)
        val lat2Rad = Math.toRadians(subsolar.latitude)
        val lon2Rad = Math.toRadians(subsolar.longitude)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        // Great-Circle angular distance (Haversine formula)
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(max(0.0, 1.0 - a)))

        val distanceKm = EARTH_RADIUS_KM * c
        val zenithAngleDeg = Math.toDegrees(c)
        val elevationAngleDeg = 90.0 - zenithAngleDeg

        // Azimuth from observer to subsolar point
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        val azimuth = ((Math.toDegrees(atan2(y, x)) + 360.0).mod(360.0) + 360.0).mod(360.0)

        // Shadow multiplier for a 1.0 meter vertical stick
        val shadowLength = when {
            elevationAngleDeg >= 89.9 -> 0.0
            elevationAngleDeg <= 0.0 -> Double.POSITIVE_INFINITY
            else -> 1.0 / tan(Math.toRadians(elevationAngleDeg))
        }

        val isDaylight = elevationAngleDeg >= 0.0
        val zenithStatus = when {
            zenithAngleDeg <= 0.5 || elevationAngleDeg >= 89.5 -> "Zero Shadow Zenith"
            elevationAngleDeg >= 70.0 -> "High Overhead Sun"
            elevationAngleDeg >= 30.0 -> "Midday Angled Sun"
            elevationAngleDeg > 0.0 -> "Low Horizon Sun"
            elevationAngleDeg >= -6.0 -> "Civil Twilight"
            else -> "Night (Subsolar Opposite)"
        }

        return ObserverZenithTelemetry(
            observerCityName = observerCityName,
            observerCountry = observerCountry,
            observerLat = observerLat,
            observerLon = observerLon,
            solarElevationDeg = elevationAngleDeg,
            zenithAngleDeg = zenithAngleDeg,
            greatCircleDistanceKm = distanceKm,
            solarAzimuthDeg = azimuth,
            shadowLength1mPoleMeters = shadowLength,
            isDaylight = isDaylight,
            zenithStatus = zenithStatus
        )
    }

    /**
     * Calculates the zero-shadow / Lahaina Noon dates for a given observer location.
     */
    fun calculateLahainaNoon(
        cityName: String,
        country: String,
        latitude: Double,
        longitude: Double,
        currentDate: LocalDate
    ): LahainaNoonEvent {
        val isTropical = abs(latitude) <= (TROPIC_OF_CANCER_LAT + 0.5)

        if (!isTropical) {
            return LahainaNoonEvent(
                cityName = cityName,
                country = country,
                latitude = latitude,
                longitude = longitude,
                isTropical = false,
                firstDate = null,
                secondDate = null,
                nextDate = null,
                daysUntilNext = null,
                isTodayZeroShadow = false
            )
        }

        val year = currentDate.year
        var minDiff1 = Double.MAX_VALUE
        var minDiff2 = Double.MAX_VALUE
        var bestDay1 = 1
        var bestDay2 = 180

        for (day in 1..172) {
            val gamma = 2.0 * PI / 365.2422 * (day - 1.0)
            val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2.0 * gamma) + 0.000907 * sin(2.0 * gamma)
            val declDeg = Math.toDegrees(declRad)
            val diff = abs(declDeg - latitude)
            if (diff < minDiff1) {
                minDiff1 = diff
                bestDay1 = day
            }
        }

        for (day in 173..365) {
            val gamma = 2.0 * PI / 365.2422 * (day - 1.0)
            val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2.0 * gamma) + 0.000907 * sin(2.0 * gamma)
            val declDeg = Math.toDegrees(declRad)
            val diff = abs(declDeg - latitude)
            if (diff < minDiff2) {
                minDiff2 = diff
                bestDay2 = day
            }
        }

        val firstDate = LocalDate.ofYearDay(year, bestDay1)
        val secondDate = LocalDate.ofYearDay(year, bestDay2)

        val candidates = listOf(
            firstDate,
            secondDate,
            firstDate.plusYears(1),
            secondDate.plusYears(1)
        ).filter { !it.isBefore(currentDate) }.sorted()

        val nextDate = candidates.firstOrNull()
        val daysUntil = nextDate?.let { ChronoUnit.DAYS.between(currentDate, it) }
        val isToday = (currentDate == firstDate || currentDate == secondDate)

        return LahainaNoonEvent(
            cityName = cityName,
            country = country,
            latitude = latitude,
            longitude = longitude,
            isTropical = true,
            firstDate = firstDate,
            secondDate = secondDate,
            nextDate = nextDate,
            daysUntilNext = daysUntil,
            isTodayZeroShadow = isToday
        )
    }

    /**
     * Identifies the primary ocean, continent, or geographic region for coordinates.
     */
    fun getRegionDescription(lat: Double, lon: Double): String {
        return when {
            lon in -180.0..-120.0 -> if (lat >= 15.0) "Central Pacific (Hawaii Basin)" else if (lat >= 0.0) "Equatorial Pacific Ocean" else "South Pacific"
            lon in -120.0..-80.0 -> if (lat >= 14.0) "Central America / Mexico" else if (lat >= -5.0) "Eastern Tropical Pacific" else "South Pacific"
            lon in -80.0..-35.0 -> if (lat >= 10.0) "Caribbean Sea / Venezuela" else if (lat >= -15.0) "Amazon Rainforest / Brazil" else "South America"
            lon in -35.0..-15.0 -> if (lat >= 0.0) "North Atlantic Ocean" else "South Atlantic Ocean"
            lon in -15.0..45.0 -> when {
                lat >= 18.0 -> "Sahara Desert / North Africa"
                lat >= 0.0 -> "Sahel / Central Africa"
                lat >= -15.0 -> "Congo Basin / East Africa"
                else -> "Southern Africa"
            }
            lon in 45.0..95.0 -> when {
                lat >= 15.0 -> "Arabian Sea / Indian Subcontinent"
                lat >= 0.0 -> "Tropical Indian Ocean"
                else -> "Southern Indian Ocean"
            }
            lon in 95.0..140.0 -> when {
                lat >= 10.0 -> "Southeast Asia"
                lat >= -10.0 -> "Indonesia / Java Sea"
                else -> "Northern Australia"
            }
            else -> if (lat >= 0.0) "Western Pacific Ocean" else "Coral Sea"
        }
    }

    /**
     * Calculates Great-Circle distance between two points in kilometers.
     */
    fun calculateGreatCircleDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(max(0.0, 1.0 - a)))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates initial azimuth bearing from Point 1 to Point 2 in degrees (0..360).
     */
    fun calculateSolarAzimuth(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dLon = lon2Rad - lon1Rad

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        return ((Math.toDegrees(atan2(y, x)) + 360.0).mod(360.0) + 360.0).mod(360.0)
    }
}
