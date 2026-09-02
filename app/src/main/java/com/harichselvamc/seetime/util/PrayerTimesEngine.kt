package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaFixedMinutesAfterMaghrib: Int? = null,
    val description: String
) {
    MUSLIM_WORLD_LEAGUE("Muslim World League (MWL)", 18.0, 17.0, null, "Standard Europe, Far East, parts of America"),
    ISNA("ISNA (North America)", 15.0, 15.0, null, "Islamic Society of North America"),
    EGYPT("Egyptian General Authority", 19.5, 17.5, null, "Africa, Syria, Lebanon, Iraq, Malaysia"),
    UMM_AL_QURA("Umm al-Qura (Makkah)", 18.5, 0.0, 90, "Arabian Peninsula, Saudi Arabia"),
    KARACHI("Univ of Islamic Sciences, Karachi", 18.0, 18.0, null, "Pakistan, India, Bangladesh, Afghanistan"),
    TEHRAN("Institute of Geophysics, Tehran", 17.7, 14.0, null, "Iran and Shia communities")
}

enum class AsrJuristicMethod(val title: String, val shadowMultiplier: Double) {
    STANDARD_SHAFII("Standard (Shafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("Hanafi", 2.0)
}

data class DailyPrayerSchedule(
    val date: LocalDate,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val currentActivePrayer: String,
    val nextPrayerName: String,
    val nextPrayerTime: LocalTime,
    val minutesUntilNextPrayer: Int,
    val qiblaAzimuthDeg: Double,
    val distanceToKaabaKm: Double
)

object PrayerTimesEngine {

    const val KAABA_LAT = 21.4225241
    const val KAABA_LON = 39.8261818

    /**
     * Calculates the Great-Circle Qibla azimuth angle (0°..360° clockwise from North) towards Kaaba, Makkah.
     */
    fun calculateQiblaAzimuth(latitude: Double, longitude: Double): Double {
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kLatRad = Math.toRadians(KAABA_LAT)
        val kLonRad = Math.toRadians(KAABA_LON)

        val dLon = kLonRad - lonRad

        val y = sin(dLon)
        val x = (cos(latRad) * tan(kLatRad)) - (sin(latRad) * cos(dLon))

        var qiblaDeg = Math.toDegrees(atan2(y, x))
        if (qiblaDeg < 0) qiblaDeg += 360.0

        return qiblaDeg
    }

    /**
     * Calculates distance to the Kaaba in kilometers using the Haversine formula.
     */
    fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(KAABA_LAT - latitude)
        val dLon = Math.toRadians(KAABA_LON - longitude)

        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LAT)

        val a = sin(dLat / 2).let { it * it } + cos(lat1) * cos(lat2) * sin(dLon / 2).let { it * it }
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return r * c
    }

    /**
     * Calculates the full 5 daily prayer times + Sunrise for a given date, coordinates, and method.
     */
    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        asrJuristic: AsrJuristicMethod = AsrJuristicMethod.STANDARD_SHAFII,
        referenceTime: LocalTime = LocalTime.now()
    ): DailyPrayerSchedule {
        val dayOfYear = date.dayOfYear

        val gamma = 2 * PI / 365.0 * (dayOfYear - 1)
        val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))
        val declRad = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma)
        val latRad = Math.toRadians(latitude)

        val standardOffsetHours = zoneId.rules.getOffset(date.atTime(12, 0).atZone(zoneId).toInstant()).totalSeconds / 3600.0

        // Solar noon in local minutes
        val noonUtcMinutes = 720.0 - (4.0 * longitude) - eqTime
        val noonLocalMinutes = noonUtcMinutes + (standardOffsetHours * 60.0)

        fun hourAngle(altitudeAngleDeg: Double): Double {
            val altRad = Math.toRadians(altitudeAngleDeg)
            val cosH = (sin(altRad) - (sin(latRad) * sin(declRad))) / (cos(latRad) * cos(declRad))
            val safeCosH = cosH.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(safeCosH))
        }

        fun minutesToLocalTime(minutesOfDay: Double): LocalTime {
            val safeMin = (minutesOfDay.mod(1440.0) + 1440.0).mod(1440.0)
            val h = (safeMin / 60.0).toInt().coerceIn(0, 23)
            val m = (safeMin % 60.0).toInt().coerceIn(0, 59)
            return LocalTime.of(h, m, 0)
        }

        // 1. Dhuhr (Solar noon + 1 min safety buffer)
        val dhuhrMin = noonLocalMinutes + 1.0
        val dhuhrTime = minutesToLocalTime(dhuhrMin)

        // 2. Sunrise & Maghrib (-0.833° sun angle for atmospheric refraction + sun semi-diameter)
        val sunRiseSetH = hourAngle(-0.833)
        val sunriseMin = noonLocalMinutes - (4.0 * sunRiseSetH)
        val maghribMin = noonLocalMinutes + (4.0 * sunRiseSetH)
        val sunriseTime = minutesToLocalTime(sunriseMin)
        val maghribTime = minutesToLocalTime(maghribMin)

        // 3. Fajr (-fajrAngle degrees twilight)
        val fajrH = hourAngle(-method.fajrAngle)
        val fajrMin = noonLocalMinutes - (4.0 * fajrH)
        val fajrTime = minutesToLocalTime(fajrMin)

        // 4. Asr (Shafi'i 1x or Hanafi 2x shadow)
        val declDeg = Math.toDegrees(declRad)
        val noonAltRad = Math.toRadians(90.0 - kotlin.math.abs(latitude - declDeg))
        val noonShadow = tan(Math.toRadians(90.0) - noonAltRad)
        val asrAltRad = atan(1.0 / (asrJuristic.shadowMultiplier + noonShadow))
        val asrAltDeg = Math.toDegrees(asrAltRad)
        val asrH = hourAngle(asrAltDeg)
        val asrMin = noonLocalMinutes + (4.0 * asrH)
        val asrTime = minutesToLocalTime(asrMin)

        // 5. Isha (-ishaAngle or fixed minutes after Maghrib)
        val ishaMin = if (method.ishaFixedMinutesAfterMaghrib != null) {
            maghribMin + method.ishaFixedMinutesAfterMaghrib
        } else {
            val ishaH = hourAngle(-method.ishaAngle)
            noonLocalMinutes + (4.0 * ishaH)
        }
        val ishaTime = minutesToLocalTime(ishaMin)

        // Determine current active prayer & next prayer
        val prayerList = listOf(
            Pair("Fajr", fajrTime),
            Pair("Sunrise", sunriseTime),
            Pair("Dhuhr", dhuhrTime),
            Pair("Asr", asrTime),
            Pair("Maghrib", maghribTime),
            Pair("Isha", ishaTime)
        )

        var activeName = "Isha (Night)"
        var nextName = "Fajr"
        var nextTime = fajrTime
        var diffMinutes = 0

        for (i in 0 until prayerList.size - 1) {
            val current = prayerList[i]
            val next = prayerList[i + 1]

            if (!referenceTime.isBefore(current.second) && referenceTime.isBefore(next.second)) {
                activeName = current.first
                nextName = next.first
                nextTime = next.second
                diffMinutes = Duration.between(referenceTime, nextTime).toMinutes().toInt()
                break
            }
        }

        if (diffMinutes == 0) {
            if (referenceTime.isBefore(fajrTime)) {
                activeName = "Late Night (Tahajjud)"
                nextName = "Fajr"
                nextTime = fajrTime
                diffMinutes = Duration.between(referenceTime, fajrTime).toMinutes().toInt()
            } else {
                activeName = "Isha"
                nextName = "Fajr"
                nextTime = fajrTime
                diffMinutes = Duration.between(referenceTime, LocalTime.MAX).toMinutes().toInt() +
                        Duration.between(LocalTime.MIN, fajrTime).toMinutes().toInt()
            }
        }

        val qiblaAzimuth = calculateQiblaAzimuth(latitude, longitude)
        val distKaaba = calculateDistanceToKaabaKm(latitude, longitude)

        return DailyPrayerSchedule(
            date = date,
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            currentActivePrayer = activeName,
            nextPrayerName = nextName,
            nextPrayerTime = nextTime,
            minutesUntilNextPrayer = diffMinutes,
            qiblaAzimuthDeg = qiblaAzimuth,
            distanceToKaabaKm = distKaaba
        )
    }
}
