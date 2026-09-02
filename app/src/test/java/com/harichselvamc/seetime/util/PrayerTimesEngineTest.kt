package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PrayerTimesEngineTest {

    @Test
    fun `calculateQiblaAzimuth computes accurate Great-Circle headings to Kaaba`() {
        // From London (51.5° N, -0.1° E) -> ~ 118.9° (East-South-East)
        val londonQibla = PrayerTimesEngine.calculateQiblaAzimuth(51.5074, -0.1278)
        assertTrue("London Qibla heading should be ~ 119°", londonQibla in 117.0..121.0)

        // From New York (40.7° N, -74.0° E) -> ~ 58.5° (North-East on Great Circle sphere)
        val nyQibla = PrayerTimesEngine.calculateQiblaAzimuth(40.7128, -74.0060)
        assertTrue("New York Qibla heading should be ~ 58.5°", nyQibla in 56.0..61.0)

        // From Tokyo (35.7° N, 139.7° E) -> ~ 293° (West-North-West on Great Circle)
        val tokyoQibla = PrayerTimesEngine.calculateQiblaAzimuth(35.6762, 139.6503)
        assertTrue("Tokyo Qibla heading should be ~ 293°", tokyoQibla in 290.0..296.0)
    }

    @Test
    fun `calculateDistanceToKaabaKm returns accurate spherical distance`() {
        val distLondon = PrayerTimesEngine.calculateDistanceToKaabaKm(51.5074, -0.1278)
        assertTrue("London to Makkah distance is ~4790 km", distLondon in 4700.0..4900.0)
    }

    @Test
    fun `calculatePrayerTimes produces chronological sequence for Makkah`() {
        val date = LocalDate.of(2026, 9, 2)
        val zone = ZoneId.of("Asia/Riyadh")

        val schedule = PrayerTimesEngine.calculatePrayerTimes(
            latitude = PrayerTimesEngine.KAABA_LAT,
            longitude = PrayerTimesEngine.KAABA_LON,
            date = date,
            zoneId = zone,
            method = CalculationMethod.UMM_AL_QURA,
            asrJuristic = AsrJuristicMethod.STANDARD_SHAFII,
            referenceTime = LocalTime.of(12, 0)
        )

        assertNotNull(schedule.fajr)
        assertNotNull(schedule.sunrise)
        assertNotNull(schedule.dhuhr)
        assertNotNull(schedule.asr)
        assertNotNull(schedule.maghrib)
        assertNotNull(schedule.isha)

        // Fajr precedes Sunrise
        assertTrue(schedule.fajr.isBefore(schedule.sunrise))
        // Sunrise precedes Dhuhr
        assertTrue(schedule.sunrise.isBefore(schedule.dhuhr))
        // Dhuhr precedes Asr
        assertTrue(schedule.dhuhr.isBefore(schedule.asr))
        // Asr precedes Maghrib
        assertTrue(schedule.asr.isBefore(schedule.maghrib))
        // Maghrib precedes Isha
        assertTrue(schedule.maghrib.isBefore(schedule.isha))

        // In Makkah at Kaaba, distance should be ~0 km
        assertEquals(0.0, schedule.distanceToKaabaKm, 1.0)
    }

    @Test
    fun `calculation methods contain standard angles and descriptions`() {
        assertEquals(6, CalculationMethod.values().size)
        CalculationMethod.values().forEach { method ->
            assertNotNull(method.title)
            assertTrue(method.fajrAngle in 10.0..25.0)
            assertNotNull(method.description)
        }
    }
}
