package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class GeochronEngineTest {

    @Test
    fun `calculateSunDeclination respects axial tilt bounds across solstices and equinoxes`() {
        // Summer Solstice around day 172 (June 21) -> declination should be positive near +23.44°
        val summerDecl = GeochronEngine.calculateSunDeclination(172, 12.0)
        assertTrue("Summer declination should be > 20 deg", summerDecl > 20.0)
        assertTrue("Summer declination <= 23.44 deg", summerDecl <= 23.44)

        // Winter Solstice around day 355 (Dec 21) -> declination should be negative near -23.44°
        val winterDecl = GeochronEngine.calculateSunDeclination(355, 12.0)
        assertTrue("Winter declination should be < -20 deg", winterDecl < -20.0)
        assertTrue("Winter declination >= -23.44 deg", winterDecl >= -23.44)

        // Equinoxes around day 80 (March 21) and day 264 (Sept 21) -> declination near 0°
        val springDecl = GeochronEngine.calculateSunDeclination(80, 12.0)
        assertTrue("Spring equinox declination near 0", kotlin.math.abs(springDecl) < 2.0)

        val autumnDecl = GeochronEngine.calculateSunDeclination(264, 12.0)
        assertTrue("Autumn equinox declination near 0", kotlin.math.abs(autumnDecl) < 2.0)
    }

    @Test
    fun `calculateEquationOfTime returns realistic astronomical drift values`() {
        // EoT ranges from approx -14.2 min (Feb) to +16.4 min (Nov)
        for (day in 1..365 step 15) {
            val eot = GeochronEngine.calculateEquationOfTime(day, 12.0)
            assertTrue("EoT should be between -16 and +18 mins: $eot on day $day", eot in -16.0..18.0)
        }
    }

    @Test
    fun `calculateSubsolarPoint places sun in correct longitude and latitude`() {
        // At 12:00 UTC on equinox, subsolar longitude should be very close to 0° (Greenwich)
        val subsolarNoon = GeochronEngine.calculateSubsolarPoint(80, 12.0)
        assertTrue("Subsolar latitude near 0 on equinox", kotlin.math.abs(subsolarNoon.latitude) < 2.0)
        assertTrue("Subsolar longitude near 0 at 12 UTC", kotlin.math.abs(subsolarNoon.longitude) < 5.0)

        // At 00:00 UTC (Midnight in London), subsolar point is near +180/-180 deg (Pacific)
        val subsolarMidnight = GeochronEngine.calculateSubsolarPoint(80, 0.0)
        assertTrue("Subsolar lon near 180/-180 at 00 UTC", kotlin.math.abs(subsolarMidnight.longitude) > 170.0)
    }

    @Test
    fun `calculateSolarElevation returns 90 degrees at subsolar point and negative at antipodes`() {
        val subsolar = SubsolarPoint(latitude = 10.0, longitude = 20.0)

        // At exact subsolar point
        val elevationZenith = GeochronEngine.calculateSolarElevation(10.0, 20.0, subsolar)
        assertEquals(90.0, elevationZenith, 0.01)

        // At antipodal point (lat = -10, lon = 20 - 180 = -160)
        val elevationNadir = GeochronEngine.calculateSolarElevation(-10.0, -160.0, subsolar)
        assertEquals(-90.0, elevationNadir, 0.01)

        // At 90 degrees angular distance (e.g., latitude 10, longitude 110)
        val elevationHorizon = GeochronEngine.calculateSolarElevation(10.0, 110.0, subsolar)
        assertEquals(0.0, elevationHorizon, 0.01)
    }

    @Test
    fun `evaluateDaylightStatus classifies DAY, GOLDEN_HOUR, TWILIGHT, and NIGHT correctly`() {
        val subsolar = SubsolarPoint(latitude = 0.0, longitude = 0.0)

        // Subsolar point -> DAY (> 6 deg)
        assertEquals(GeochronDaylightStatus.DAY, GeochronEngine.evaluateDaylightStatus(0.0, 0.0, subsolar))

        // Near 90 deg distance -> GOLDEN_HOUR / TWILIGHT
        // lat 0, lon 88 -> elevation ~ 2 deg -> GOLDEN_HOUR (-4 to 6)
        assertEquals(GeochronDaylightStatus.GOLDEN_HOUR, GeochronEngine.evaluateDaylightStatus(0.0, 88.0, subsolar))

        // lat 0, lon 95 -> elevation ~ -5 deg -> TWILIGHT (-18 to -4)
        assertEquals(GeochronDaylightStatus.TWILIGHT, GeochronEngine.evaluateDaylightStatus(0.0, 95.0, subsolar))

        // lat 0, lon 150 -> elevation ~ -60 deg -> NIGHT (<-18)
        assertEquals(GeochronDaylightStatus.NIGHT, GeochronEngine.evaluateDaylightStatus(0.0, 150.0, subsolar))
    }

    @Test
    fun `evaluateSeasonState detects Midnight Sun in Arctic during summer`() {
        val summerSubsolar = SubsolarPoint(latitude = 23.44, longitude = 0.0)
        val state = GeochronEngine.evaluateSeasonState(172, summerSubsolar)

        assertEquals("Summer Solstice Phase", state.seasonName)
        assertTrue(state.arcticCircleStatus.contains("Midnight Sun"))
        assertTrue(state.antarcticCircleStatus.contains("Polar Night"))
    }

    @Test
    fun `generateMeridianTimeScales produces 27 standard ribbons with 24-hour cycle`() {
        val scales = GeochronEngine.generateMeridianTimeScales(12.0)
        assertEquals(27, scales.size) // -12 to +14

        val utcZeroSlot = scales.first { it.utcOffsetLabel == "UTC+0" }
        assertEquals(12, utcZeroSlot.hour)
        assertTrue(utcZeroSlot.isDaylightHour)

        val utcMinus12Slot = scales.first { it.utcOffsetLabel == "UTC-12" }
        assertEquals(0, utcMinus12Slot.hour)
        assertFalse(utcMinus12Slot.isDaylightHour)
    }

    @Test
    fun `catalog cities contains essential global metros with correct telemetry`() {
        assertTrue(GeochronEngine.CATALOG_CITIES.size >= 20)

        val tokyo = GeochronEngine.CATALOG_CITIES.first { it.id == "TYO" }
        val london = GeochronEngine.CATALOG_CITIES.first { it.id == "LON" }
        val ny = GeochronEngine.CATALOG_CITIES.first { it.id == "NYC" }

        val subsolar = SubsolarPoint(latitude = 10.0, longitude = 0.0)
        val utcTime = ZonedDateTime.of(2026, 6, 21, 12, 0, 0, 0, ZoneOffset.UTC)

        val londonTelemetry = GeochronEngine.calculateCityTelemetry(london, utcTime, subsolar)
        assertNotNull(londonTelemetry)
        assertTrue(londonTelemetry.isDaylight)

        val tokyoTelemetry = GeochronEngine.calculateCityTelemetry(tokyo, utcTime, subsolar)
        assertNotNull(tokyoTelemetry)
        assertNotNull(tokyoTelemetry.localStandardTime)
    }

    @Test
    fun `calculateTerminatorPoints generates valid boundary array across longitudes`() {
        val subsolar = SubsolarPoint(latitude = 15.0, longitude = 30.0)
        val points = GeochronEngine.calculateTerminatorPoints(subsolar, stepDeg = 10)

        assertTrue(points.isNotEmpty())
        points.forEach { (lat, lon) ->
            assertTrue("Terminator latitude within -90..90: $lat", lat in -90.0..90.0)
            assertTrue("Longitude within -180..180: $lon", lon in -180.0..180.0)
        }
    }
}
