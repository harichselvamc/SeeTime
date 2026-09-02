package com.harichselvamc.seetime.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

enum class GeochronDaylightStatus(val label: String) {
    DAY("Full Daylight"),
    GOLDEN_HOUR("Golden Hour"),
    TWILIGHT("Twilight"),
    NIGHT("Night")
}

data class GeochronCity(
    val id: String,
    val name: String,
    val country: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val baseUtcOffset: Double
)

data class GeochronCityTelemetry(
    val city: GeochronCity,
    val localStandardTime: String,
    val trueSolarTime: String,
    val solarNoonOffsetMinutes: Int,
    val sunElevationDeg: Double,
    val daylightStatus: GeochronDaylightStatus,
    val isDaylight: Boolean
)

data class GeochronSeasonState(
    val seasonName: String,
    val sunDeclinationDeg: Double,
    val equationOfTimeMinutes: Double,
    val subsolarPoint: SubsolarPoint,
    val arcticCircleStatus: String,
    val antarcticCircleStatus: String,
    val daylightFractionPercent: Int
)

data class GeochronTimeScaleSlot(
    val hour: Int,
    val formattedTime: String,
    val longitudeDeg: Float,
    val isDaylightHour: Boolean,
    val utcOffsetLabel: String
)

object GeochronEngine {

    val AXIAL_TILT_DEG = 23.439
    val ARCTIC_LAT_DEG = 66.561
    val ANTARCTIC_LAT_DEG = -66.561

    val CATALOG_CITIES = listOf(
        GeochronCity("LON", "London", "United Kingdom", "Europe", 51.5074, -0.1278, "Europe/London", 0.0),
        GeochronCity("PAR", "Paris", "France", "Europe", 48.8566, 2.3522, "Europe/Paris", 1.0),
        GeochronCity("BER", "Berlin", "Germany", "Europe", 52.5200, 13.4050, "Europe/Berlin", 1.0),
        GeochronCity("CAI", "Cairo", "Egypt", "Africa", 30.0444, 31.2357, "Africa/Cairo", 2.0),
        GeochronCity("DXB", "Dubai", "UAE", "Middle East", 25.2048, 55.2708, "Asia/Dubai", 4.0),
        GeochronCity("BOM", "Mumbai", "India", "Asia", 19.0760, 72.8777, "Asia/Kolkata", 5.5),
        GeochronCity("SIN", "Singapore", "Singapore", "Asia", 1.3521, 103.8198, "Asia/Singapore", 8.0),
        GeochronCity("TYO", "Tokyo", "Japan", "Asia", 35.6762, 139.6503, "Asia/Tokyo", 9.0),
        GeochronCity("SYD", "Sydney", "Australia", "Oceania", -33.8688, 151.2093, "Australia/Sydney", 10.0),
        GeochronCity("AKL", "Auckland", "New Zealand", "Oceania", -36.8485, 174.7633, "Pacific/Auckland", 12.0),
        GeochronCity("HNL", "Honolulu", "USA", "North America", 21.3069, -157.8583, "Pacific/Honolulu", -10.0),
        GeochronCity("ANC", "Anchorage", "USA", "North America", 61.2181, -149.9003, "America/Anchorage", -9.0),
        GeochronCity("LAX", "Los Angeles", "USA", "North America", 34.0522, -118.2437, "America/Los_Angeles", -8.0),
        GeochronCity("DEN", "Denver", "USA", "North America", 39.7392, -104.9903, "America/Denver", -7.0),
        GeochronCity("CHI", "Chicago", "USA", "North America", 41.8781, -87.6298, "America/Chicago", -6.0),
        GeochronCity("NYC", "New York", "USA", "North America", 40.7128, -74.0060, "America/New_York", -5.0),
        GeochronCity("SAO", "São Paulo", "Brazil", "South America", -23.5505, -46.6333, "America/Sao_Paulo", -3.0),
        GeochronCity("BUE", "Buenos Aires", "Argentina", "South America", -34.6037, -58.3816, "America/Argentina/Buenos_Aires", -3.0),
        GeochronCity("REK", "Reykjavik", "Iceland", "Europe", 64.1466, -21.9426, "Atlantic/Reykjavik", 0.0),
        GeochronCity("MOW", "Moscow", "Russia", "Europe", 55.7558, 37.6173, "Europe/Moscow", 3.0),
        GeochronCity("BJS", "Beijing", "China", "Asia", 39.9042, 116.4074, "Asia/Shanghai", 8.0),
        GeochronCity("BKK", "Bangkok", "Thailand", "Asia", 13.7563, 100.5018, "Asia/Bangkok", 7.0),
        GeochronCity("NBO", "Nairobi", "Kenya", "Africa", -1.2921, 36.8219, "Africa/Nairobi", 3.0),
        GeochronCity("JNB", "Johannesburg", "South Africa", "Africa", -26.2041, 28.0473, "Africa/Johannesburg", 2.0),
        GeochronCity("MEX", "Mexico City", "Mexico", "North America", 19.4326, -99.1332, "America/Mexico_City", -6.0),
        GeochronCity("YVR", "Vancouver", "Canada", "North America", 49.2827, -123.1207, "America/Vancouver", -8.0)
    )

    /**
     * Calculates the Sun's declination angle in degrees for any day of year (1..365) and UTC decimal hour.
     * Ranges from approx -23.44° (Winter Solstice) to +23.44° (Summer Solstice).
     */
    fun calculateSunDeclination(dayOfYear: Int, utcDecimalHour: Double = 12.0): Double {
        val clampedDay = dayOfYear.coerceIn(1, 366)
        val gamma = 2 * PI / 365.0 * (clampedDay - 1 + (utcDecimalHour - 12.0) / 24.0)
        val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)
        return Math.toDegrees(declRad).coerceIn(-AXIAL_TILT_DEG, AXIAL_TILT_DEG)
    }

    /**
     * Calculates the Equation of Time (EoT) in minutes for a given day of year and UTC hour.
     * Represents the discrepancy between apparent solar time and mean clock time (-14.2m to +16.4m).
     */
    fun calculateEquationOfTime(dayOfYear: Int, utcDecimalHour: Double = 12.0): Double {
        val clampedDay = dayOfYear.coerceIn(1, 366)
        val gamma = 2 * PI / 365.0 * (clampedDay - 1 + (utcDecimalHour - 12.0) / 24.0)
        return 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
    }

    /**
     * Calculates the subsolar point (lat, lon) on Earth where the Sun is at zenith (90° elevation).
     */
    fun calculateSubsolarPoint(dayOfYear: Int, utcDecimalHour: Double): SubsolarPoint {
        val declDeg = calculateSunDeclination(dayOfYear, utcDecimalHour)
        val eqTime = calculateEquationOfTime(dayOfYear, utcDecimalHour)

        // Subsolar longitude: -((UTC hour - 12.0) * 15° + EoT/4)
        var subLon = -(utcDecimalHour - 12.0) * 15.0 - (eqTime / 4.0)
        subLon = ((subLon + 180.0).mod(360.0) + 360.0).mod(360.0) - 180.0

        return SubsolarPoint(latitude = declDeg, longitude = subLon)
    }

    /**
     * Calculates instantaneous Sun elevation angle in degrees for any latitude and longitude.
     */
    fun calculateSolarElevation(latitude: Double, longitude: Double, subsolarPoint: SubsolarPoint): Double {
        val latRad = Math.toRadians(latitude)
        val subLatRad = Math.toRadians(subsolarPoint.latitude)
        val dLonRad = Math.toRadians(longitude - subsolarPoint.longitude)

        val sinElevation = sin(latRad) * sin(subLatRad) + cos(latRad) * cos(subLatRad) * cos(dLonRad)
        val clampedSin = sinElevation.coerceIn(-1.0, 1.0)
        return Math.toDegrees(asin(clampedSin))
    }

    /**
     * Classifies daylight status based on sun elevation angle.
     */
    fun evaluateDaylightStatus(latitude: Double, longitude: Double, subsolarPoint: SubsolarPoint): GeochronDaylightStatus {
        val elevation = calculateSolarElevation(latitude, longitude, subsolarPoint)
        return when {
            elevation > 6.0 -> GeochronDaylightStatus.DAY
            elevation >= -4.0 -> GeochronDaylightStatus.GOLDEN_HOUR
            elevation >= -18.0 -> GeochronDaylightStatus.TWILIGHT
            else -> GeochronDaylightStatus.NIGHT
        }
    }

    /**
     * Calculates True Solar Time (Apparent Solar Time) for a given longitude.
     */
    fun calculateTrueSolarTime(
        utcTime: LocalTime,
        longitude: Double,
        dayOfYear: Int
    ): LocalTime {
        val utcDecimal = utcTime.hour + utcTime.minute / 60.0 + utcTime.second / 3600.0
        val eqTime = calculateEquationOfTime(dayOfYear, utcDecimal)
        val solarOffsetHours = (longitude / 15.0) + (eqTime / 60.0)
        var solarDecimal = (utcDecimal + solarOffsetHours).mod(24.0)
        if (solarDecimal < 0) solarDecimal += 24.0

        val sHour = solarDecimal.toInt().coerceIn(0, 23)
        val sMin = ((solarDecimal - sHour) * 60.0).toInt().coerceIn(0, 59)
        val sSec = ((((solarDecimal - sHour) * 60.0) - sMin) * 60.0).toInt().coerceIn(0, 59)

        return LocalTime.of(sHour, sMin, sSec)
    }

    /**
     * Calculates the daylight / twilight boundary terminator latitude for every longitude step.
     */
    fun calculateTerminatorPoints(subsolarPoint: SubsolarPoint, stepDeg: Int = 2): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        val subLatRad = Math.toRadians(subsolarPoint.latitude.coerceIn(-AXIAL_TILT_DEG, AXIAL_TILT_DEG))
        val tanSubLat = tan(subLatRad)

        for (lon in -180..180 step stepDeg) {
            val dLonRad = Math.toRadians(lon - subsolarPoint.longitude)
            val termLatDeg = if (abs(tanSubLat) < 0.0001) {
                if (cos(dLonRad) >= 0) 90.0 else -90.0
            } else {
                val tanTermLat = -cos(dLonRad) / tanSubLat
                Math.toDegrees(kotlin.math.atan(tanTermLat)).coerceIn(-89.9, 89.9)
            }
            list.add(Pair(termLatDeg, lon.toDouble()))
        }
        return list
    }

    /**
     * Evaluates season classification and polar circle daylight conditions.
     */
    fun evaluateSeasonState(dayOfYear: Int, subsolarPoint: SubsolarPoint): GeochronSeasonState {
        val decl = subsolarPoint.latitude
        val eot = calculateEquationOfTime(dayOfYear)

        val seasonName = when (dayOfYear) {
            in 60..151 -> "Spring / Vernal Phase"
            in 152..243 -> "Summer Solstice Phase"
            in 244..334 -> "Autumn / Fall Phase"
            else -> "Winter Solstice Phase"
        }

        val arcticStatus = when {
            decl >= (90.0 - ARCTIC_LAT_DEG) -> "24h Midnight Sun (Continuous Daylight)"
            decl <= -(90.0 - ARCTIC_LAT_DEG) -> "24h Polar Night (Continuous Darkness)"
            else -> "Day/Night Cycle Active"
        }

        val antarcticStatus = when {
            decl <= -(90.0 - ARCTIC_LAT_DEG) -> "24h Midnight Sun (Continuous Daylight)"
            decl >= (90.0 - ARCTIC_LAT_DEG) -> "24h Polar Night (Continuous Darkness)"
            else -> "Day/Night Cycle Active"
        }

        return GeochronSeasonState(
            seasonName = seasonName,
            sunDeclinationDeg = decl,
            equationOfTimeMinutes = eot,
            subsolarPoint = subsolarPoint,
            arcticCircleStatus = arcticStatus,
            antarcticCircleStatus = antarcticStatus,
            daylightFractionPercent = 50
        )
    }

    /**
     * Generates 24 standard meridian rolling time ribbon slots for the top/bottom Geochron bar.
     */
    fun generateMeridianTimeScales(utcDecimalHour: Double): List<GeochronTimeScaleSlot> {
        val slots = mutableListOf<GeochronTimeScaleSlot>()
        for (offset in -12..14) {
            val lon = offset * 15.0f
            var localDecimal = (utcDecimalHour + offset).mod(24.0)
            if (localDecimal < 0) localDecimal += 24.0

            val hour = localDecimal.toInt()
            val min = ((localDecimal - hour) * 60).toInt()
            val formatted = String.format("%02d:%02d", hour, min)
            val isDay = hour in 6..17

            val offsetLabel = if (offset >= 0) "+$offset" else "$offset"

            slots.add(
                GeochronTimeScaleSlot(
                    hour = hour,
                    formattedTime = formatted,
                    longitudeDeg = lon,
                    isDaylightHour = isDay,
                    utcOffsetLabel = "UTC$offsetLabel"
                )
            )
        }
        return slots
    }

    /**
     * Calculates complete city telemetry for a specific city under the given subsolar conditions.
     */
    fun calculateCityTelemetry(
        city: GeochronCity,
        utcDateTime: ZonedDateTime,
        subsolarPoint: SubsolarPoint
    ): GeochronCityTelemetry {
        val zone = try {
            ZoneId.of(city.zoneId)
        } catch (e: Exception) {
            ZoneId.of("UTC")
        }
        val cityTime = utcDateTime.withZoneSameInstant(zone)
        val formattedLocal = String.format("%02d:%02d", cityTime.hour, cityTime.minute)

        val dayOfYear = utcDateTime.dayOfYear
        val solarTime = calculateTrueSolarTime(utcDateTime.toLocalTime(), city.longitude, dayOfYear)
        val formattedSolar = String.format("%02d:%02d", solarTime.hour, solarTime.minute)

        val solarNoonOffsetMins = (city.longitude * 4.0 + calculateEquationOfTime(dayOfYear)).toInt()
        val elevation = calculateSolarElevation(city.latitude, city.longitude, subsolarPoint)
        val status = evaluateDaylightStatus(city.latitude, city.longitude, subsolarPoint)

        return GeochronCityTelemetry(
            city = city,
            localStandardTime = formattedLocal,
            trueSolarTime = formattedSolar,
            solarNoonOffsetMinutes = solarNoonOffsetMins,
            sunElevationDeg = elevation,
            daylightStatus = status,
            isDaylight = elevation >= -4.0
        )
    }
}
