package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.abs

class SubsolarEngineTest {

    @Test
    fun `calculateSubsolarPoint computes accurate summer solstice position`() {
        val june21 = ZonedDateTime.of(2026, 6, 21, 12, 0, 0, 0, ZoneOffset.UTC)
        val subsolar = SubsolarEngine.calculateSubsolarPoint(june21)

        assertTrue("Latitude should be near +23.44°", subsolar.latitude in 22.5..24.0)
        assertTrue("Longitude should be near 0°", abs(subsolar.longitude) < 5.0)
        assertTrue(subsolar.tropicZone.contains("Tropic of Cancer"))
        assertNotNull(subsolar.regionDescription)
    }

    @Test
    fun `calculateSubsolarPoint computes accurate winter solstice position`() {
        val dec21 = ZonedDateTime.of(2026, 12, 21, 12, 0, 0, 0, ZoneOffset.UTC)
        val subsolar = SubsolarEngine.calculateSubsolarPoint(dec21)

        assertTrue("Latitude should be near -23.44°", subsolar.latitude in -24.0..-22.5)
        assertTrue("Longitude should be near 0°", abs(subsolar.longitude) < 5.0)
        assertTrue(subsolar.tropicZone.contains("Tropic of Capricorn"))
    }

    @Test
    fun `calculateSubsolarPoint computes accurate vernal equinox position`() {
        val mar20 = ZonedDateTime.of(2026, 3, 20, 12, 0, 0, 0, ZoneOffset.UTC)
        val subsolar = SubsolarEngine.calculateSubsolarPoint(mar20)

        assertTrue("Latitude should be near 0° Equator", abs(subsolar.latitude) < 1.5)
        assertTrue("GHA should be near 0° / 360°", subsolar.ghaDeg < 10.0 || subsolar.ghaDeg > 350.0)
    }

    @Test
    fun `calculateObserverZenith detects direct zenith at subsolar location`() {
        val now = ZonedDateTime.of(2026, 6, 21, 12, 0, 0, 0, ZoneOffset.UTC)
        val subsolar = SubsolarEngine.calculateSubsolarPoint(now)

        val telemetry = SubsolarEngine.calculateObserverZenith(
            observerLat = subsolar.latitude,
            observerLon = subsolar.longitude,
            observerCityName = "Sun Center",
            observerCountry = "Equator",
            dateTime = now
        )

        assertTrue("Distance should be near 0 km", telemetry.greatCircleDistanceKm < 10.0)
        assertTrue("Zenith angle should be near 0°", telemetry.zenithAngleDeg < 0.2)
        assertTrue("Elevation should be near 90°", telemetry.solarElevationDeg > 89.8)
        assertEquals(0.0, telemetry.shadowLength1mPoleMeters, 0.001)
        assertTrue(telemetry.isDaylight)
        assertTrue(telemetry.zenithStatus.contains("ZERO SHADOW"))
    }

    @Test
    fun `calculateObserverZenith detects night-time for antipodal observer`() {
        val now = ZonedDateTime.of(2026, 6, 21, 12, 0, 0, 0, ZoneOffset.UTC)
        val subsolar = SubsolarEngine.calculateSubsolarPoint(now)

        val antiLat = -subsolar.latitude
        val antiLon = if (subsolar.longitude > 0) subsolar.longitude - 180.0 else subsolar.longitude + 180.0

        val telemetry = SubsolarEngine.calculateObserverZenith(
            observerLat = antiLat,
            observerLon = antiLon,
            observerCityName = "Antipode",
            observerCountry = "Opposite",
            dateTime = now
        )

        assertTrue("Distance should be ~ 20,000 km", telemetry.greatCircleDistanceKm > 19000.0)
        assertTrue("Zenith angle should be near 180°", telemetry.zenithAngleDeg > 175.0)
        assertTrue("Elevation should be deeply negative", telemetry.solarElevationDeg < -80.0)
        assertFalse(telemetry.isDaylight)
        assertEquals(-1.0, telemetry.shadowLength1mPoleMeters, 0.001)
    }

    @Test
    fun `calculateLahainaNoon identifies tropical zero-shadow days for Honolulu`() {
        val honolulu = SubsolarEngine.TROPICAL_CITY_PRESETS.first { it.name.contains("Honolulu") }
        val date = LocalDate.of(2026, 5, 1)

        val event = SubsolarEngine.calculateLahainaNoon(
            cityName = honolulu.name,
            country = honolulu.country,
            latitude = honolulu.latitude,
            longitude = honolulu.longitude,
            currentDate = date
        )

        assertTrue(event.isTropical)
        assertNotNull(event.firstDate)
        assertNotNull(event.secondDate)
        assertNotNull(event.nextDate)
        assertTrue("Honolulu zero-shadow dates occur in May and July", event.firstDate?.monthValue in 5..7)
    }

    @Test
    fun `calculateLahainaNoon handles non-tropical cities like London`() {
        val london = SubsolarEngine.TROPICAL_CITY_PRESETS.first { it.name.contains("London") }
        val date = LocalDate.of(2026, 6, 21)

        val event = SubsolarEngine.calculateLahainaNoon(
            cityName = london.name,
            country = london.country,
            latitude = london.latitude,
            longitude = london.longitude,
            currentDate = date
        )

        assertFalse("London is non-tropical", event.isTropical)
        assertEquals(null, event.firstDate)
        assertEquals(null, event.secondDate)
        assertFalse(event.isTodayZeroShadow)
    }

    @Test
    fun `calculateGreatCircleDistanceKm matches known distance between London and Tokyo`() {
        val dist = SubsolarEngine.calculateGreatCircleDistanceKm(51.5074, -0.1278, 35.6762, 139.6503)
        assertTrue("London to Tokyo distance should be ~9560 km", dist in 9400.0..9700.0)
    }

    @Test
    fun `calculateSolarAzimuth computes valid compass direction`() {
        val northBearing = SubsolarEngine.calculateSolarAzimuth(0.0, 0.0, 10.0, 0.0)
        assertEquals(0.0, northBearing, 0.5)

        val eastBearing = SubsolarEngine.calculateSolarAzimuth(0.0, 0.0, 0.0, 10.0)
        assertEquals(90.0, eastBearing, 0.5)
    }

    @Test
    fun `getRegionDescription categorizes global regions properly`() {
        val pacific = SubsolarEngine.getRegionDescription(20.0, -150.0)
        assertTrue(pacific.contains("Pacific"))

        val africa = SubsolarEngine.getRegionDescription(20.0, 10.0)
        assertTrue(africa.contains("Sahara") || africa.contains("Africa"))

        val amazon = SubsolarEngine.getRegionDescription(-5.0, -60.0)
        assertTrue(amazon.contains("Amazon") || amazon.contains("America") || amazon.contains("Brazil"))
    }
}
