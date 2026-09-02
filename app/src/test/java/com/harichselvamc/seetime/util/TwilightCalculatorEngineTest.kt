package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TwilightCalculatorEngineTest {

    @Test
    fun testSolarElevation_rangesWithinBounds() {
        val cities = TwilightCalculatorEngine.getPredefinedCities()
        val testDate = LocalDate.of(2026, 6, 21) // Summer Solstice

        for (city in cities) {
            // Test at multiple hours of the day
            for (hour in 0..23) {
                val zdt = ZonedDateTime.of(testDate, LocalTime.of(hour, 0), city.zoneId)
                val elevation = TwilightCalculatorEngine.calculateSolarElevation(city.latitude, city.longitude, zdt)
                val azimuth = TwilightCalculatorEngine.calculateSolarAzimuth(city.latitude, city.longitude, zdt)

                assertTrue("Elevation $elevation for ${city.cityName} at hour $hour must be in [-90, 90]",
                    elevation >= -90.0 && elevation <= 90.0)
                assertTrue("Azimuth $azimuth for ${city.cityName} at hour $hour must be in [0, 360]",
                    azimuth >= 0.0 && azimuth <= 360.0)
            }
        }
    }

    @Test
    fun testSolarElevation_solarNoonHigherThanMidnight() {
        val lat = 51.5074 // London
        val lon = -0.1278
        val zone = ZoneId.of("Europe/London")
        val date = LocalDate.of(2026, 9, 2)

        val noonZdt = ZonedDateTime.of(date, LocalTime.of(12, 0), zone)
        val midnightZdt = ZonedDateTime.of(date, LocalTime.of(0, 0), zone)

        val noonElevation = TwilightCalculatorEngine.calculateSolarElevation(lat, lon, noonZdt)
        val midnightElevation = TwilightCalculatorEngine.calculateSolarElevation(lat, lon, midnightZdt)

        assertTrue("Solar noon elevation ($noonElevation) must be significantly higher than midnight ($midnightElevation)",
            noonElevation > midnightElevation)
        assertTrue("London solar noon in Sept should be above horizon", noonElevation > 20.0)
        assertTrue("London midnight in Sept should be well below horizon", midnightElevation < -20.0)
    }

    @Test
    fun testTwilightPhaseClassification_correctBoundaries() {
        assertEquals(TwilightPhase.DIRECT_SUNLIGHT, TwilightCalculatorEngine.getTwilightPhase(25.0))
        assertEquals(TwilightPhase.DIRECT_SUNLIGHT, TwilightCalculatorEngine.getTwilightPhase(0.1))

        assertEquals(TwilightPhase.CIVIL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(0.0))
        assertEquals(TwilightPhase.CIVIL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-3.5))
        assertEquals(TwilightPhase.CIVIL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-6.0))

        assertEquals(TwilightPhase.NAUTICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-6.1))
        assertEquals(TwilightPhase.NAUTICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-9.0))
        assertEquals(TwilightPhase.NAUTICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-12.0))

        assertEquals(TwilightPhase.ASTRONOMICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-12.1))
        assertEquals(TwilightPhase.ASTRONOMICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-15.0))
        assertEquals(TwilightPhase.ASTRONOMICAL_TWILIGHT, TwilightCalculatorEngine.getTwilightPhase(-18.0))

        assertEquals(TwilightPhase.NIGHT, TwilightCalculatorEngine.getTwilightPhase(-18.1))
        assertEquals(TwilightPhase.NIGHT, TwilightCalculatorEngine.getTwilightPhase(-45.0))
        assertEquals(TwilightPhase.NIGHT, TwilightCalculatorEngine.getTwilightPhase(-89.9))
    }

    @Test
    fun testPhotoLightingWindow_classification() {
        assertEquals(PhotoLightingWindow.STANDARD_DAY, TwilightCalculatorEngine.getPhotoWindow(15.0))
        assertEquals(PhotoLightingWindow.STANDARD_DAY, TwilightCalculatorEngine.getPhotoWindow(6.1))

        assertEquals(PhotoLightingWindow.GOLDEN_HOUR, TwilightCalculatorEngine.getPhotoWindow(5.9))
        assertEquals(PhotoLightingWindow.GOLDEN_HOUR, TwilightCalculatorEngine.getPhotoWindow(0.0))
        assertEquals(PhotoLightingWindow.GOLDEN_HOUR, TwilightCalculatorEngine.getPhotoWindow(-3.9))

        assertEquals(PhotoLightingWindow.BLUE_HOUR, TwilightCalculatorEngine.getPhotoWindow(-4.1))
        assertEquals(PhotoLightingWindow.BLUE_HOUR, TwilightCalculatorEngine.getPhotoWindow(-5.5))
        assertEquals(PhotoLightingWindow.BLUE_HOUR, TwilightCalculatorEngine.getPhotoWindow(-6.0))

        assertEquals(PhotoLightingWindow.DARK_SKY, TwilightCalculatorEngine.getPhotoWindow(-6.1))
        assertEquals(PhotoLightingWindow.DARK_SKY, TwilightCalculatorEngine.getPhotoWindow(-25.0))
    }

    @Test
    fun testApproximateLuxAndStars() {
        // High noon daylight
        val dayLux = TwilightCalculatorEngine.approximateLux(45.0)
        assertTrue("Daylight lux must be high (> 10000)", dayLux > 10000.0)
        assertEquals(0, TwilightCalculatorEngine.estimateStarVisibility(45.0))

        // Civil twilight
        val civilLux = TwilightCalculatorEngine.approximateLux(-3.0)
        assertTrue("Civil lux must be moderate (3.4..400)", civilLux in 3.4..400.0)
        assertEquals(1, TwilightCalculatorEngine.estimateStarVisibility(-3.0))

        // Nautical twilight
        val nauticalLux = TwilightCalculatorEngine.approximateLux(-9.0)
        assertTrue("Nautical lux must be low (0.008..3.4)", nauticalLux in 0.008..3.4)
        assertEquals(2, TwilightCalculatorEngine.estimateStarVisibility(-9.0))

        // True night
        val nightLux = TwilightCalculatorEngine.approximateLux(-30.0)
        assertTrue("Night lux must be minimal", nightLux < 0.001)
        assertEquals(5, TwilightCalculatorEngine.estimateStarVisibility(-30.0))
    }

    @Test
    fun testFormatCountdown() {
        assertEquals("2h 15m", TwilightCalculatorEngine.formatCountdown(135))
        assertEquals("45m", TwilightCalculatorEngine.formatCountdown(45))
        assertEquals("3h", TwilightCalculatorEngine.formatCountdown(180))
        assertEquals("0m", TwilightCalculatorEngine.formatCountdown(0))
    }

    @Test
    fun testPredefinedCitiesCatalog() {
        val cities = TwilightCalculatorEngine.getPredefinedCities()
        assertTrue("Must have at least 10 world cities", cities.size >= 10)

        val cityIds = cities.map { it.id }.toSet()
        assertTrue("Must include London", cityIds.contains("london"))
        assertTrue("Must include Tokyo", cityIds.contains("tokyo"))
        assertTrue("Must include New York", cityIds.contains("new_york"))
        assertTrue("Must include Reykjavik", cityIds.contains("reykjavik"))
        assertTrue("Must include Sydney", cityIds.contains("sydney"))
        assertTrue("Must include Singapore", cityIds.contains("singapore"))
    }

    @Test
    fun testCalculateTelemetry_returnsValidData() {
        val city = TwilightCalculatorEngine.getPredefinedCities().first { it.id == "tokyo" }
        val zdt = ZonedDateTime.of(LocalDate.of(2026, 9, 2), LocalTime.of(14, 0), city.zoneId)

        val telemetry = TwilightCalculatorEngine.calculateTelemetry(city.latitude, city.longitude, zdt)

        assertNotNull(telemetry)
        assertTrue("Elevation must be valid", telemetry.elevationDegrees in -90.0..90.0)
        assertTrue("Azimuth must be valid", telemetry.azimuthDegrees in 0.0..360.0)
        assertNotNull(telemetry.phase)
        assertNotNull(telemetry.photoWindow)
        assertTrue("Peak elevation must be positive for Tokyo summer/autumn", telemetry.solarNoonElevation > 0.0)
    }

    @Test
    fun testDailySchedule_generatesMilestones() {
        val city = TwilightCalculatorEngine.getPredefinedCities().first { it.id == "new_york" }
        val date = LocalDate.of(2026, 9, 2)

        val schedule = TwilightCalculatorEngine.calculateDailySchedule(city.latitude, city.longitude, date, city.zoneId)

        assertEquals(date, schedule.date)
        assertTrue("Must contain milestones", schedule.milestones.isNotEmpty())
        assertTrue("Peak elevation should be reasonable for NYC", schedule.peakElevationDeg in 40.0..75.0)

        val milestoneNames = schedule.milestones.map { it.name }
        assertTrue("Must have sunrise", milestoneNames.contains("Sunrise"))
        assertTrue("Must have sunset", milestoneNames.contains("Sunset (Civil Dusk)"))
        assertTrue("Must have civil dawn", milestoneNames.contains("Civil Dawn"))
    }

    @Test
    fun testNextTransition_findsUpcomingEvent() {
        val city = TwilightCalculatorEngine.getPredefinedCities().first { it.id == "london" }
        val zdt = ZonedDateTime.of(LocalDate.of(2026, 9, 2), LocalTime.of(3, 0), city.zoneId) // Pre-dawn

        val event = TwilightCalculatorEngine.findNextTransition(city.latitude, city.longitude, zdt)

        assertNotNull("Should discover upcoming transition before dawn", event)
        assertTrue("Minutes until event must be positive", event!!.minutesUntil > 0)
        assertTrue("Countdown string should not be blank", event.formattedCountdown.isNotBlank())
    }
}
