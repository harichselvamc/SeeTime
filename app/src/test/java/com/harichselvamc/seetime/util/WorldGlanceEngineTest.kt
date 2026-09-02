package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class WorldGlanceEngineTest {

    private lateinit var engine: WorldGlanceEngine

    @Before
    fun setup() {
        engine = WorldGlanceEngine(context = null)
    }

    @Test
    fun `database contains key world financial hubs`() {
        val cities = WorldGlanceEngine.WORLD_CITIES_DATABASE
        assertTrue(cities.size >= 16)
        assertTrue(cities.any { it.cityName == "New York" && it.marketInfo?.exchangeCode == "NYSE" })
        assertTrue(cities.any { it.cityName == "Tokyo" && it.marketInfo?.exchangeCode == "TSE" })
        assertTrue(cities.any { it.cityName == "London" && it.marketInfo?.exchangeCode == "LSE" })
        assertTrue(cities.any { it.cityName == "Singapore" && it.marketInfo?.exchangeCode == "SGX" })
        assertTrue(cities.any { it.cityName == "Hong Kong" && it.marketInfo?.exchangeCode == "HKEX" })
    }

    @Test
    fun `evaluateMarketStatus detects open trading hours`() {
        val nyse = MarketInfo("New York Stock Exchange", "NYSE", LocalTime.of(9, 30), LocalTime.of(16, 0))

        // Wednesday at 11:00 AM -> Market Open
        val openDate = LocalDate.of(2026, 9, 2) // Wednesday
        val openTime = LocalTime.of(11, 0)
        val (statusOpen, _) = engine.evaluateMarketStatus(nyse, openDate, openTime)
        assertEquals(MarketStatus.OPEN, statusOpen)

        // Wednesday at 15:45 PM -> Closing Soon (< 30 min left)
        val closingTime = LocalTime.of(15, 45)
        val (statusClosing, _) = engine.evaluateMarketStatus(nyse, openDate, closingTime)
        assertEquals(MarketStatus.CLOSING_SOON, statusClosing)

        // Wednesday at 17:00 PM -> Closed
        val closedTime = LocalTime.of(17, 0)
        val (statusClosed, _) = engine.evaluateMarketStatus(nyse, openDate, closedTime)
        assertEquals(MarketStatus.CLOSED, statusClosed)

        // Saturday -> Weekend Closed
        val weekendDate = LocalDate.of(2026, 9, 5) // Saturday
        val (statusWeekend, _) = engine.evaluateMarketStatus(nyse, weekendDate, openTime)
        assertEquals(MarketStatus.WEEKEND, statusWeekend)
    }

    @Test
    fun `computeCityLiveState calculates timezone offset and analog angles`() {
        val tokyo = WorldGlanceEngine.WORLD_CITIES_DATABASE.first { it.id == "tyo" }

        // Fixed instant: 2026-09-02T12:00:00Z
        val instant = Instant.parse("2026-09-02T12:00:00Z")
        val state = engine.computeCityLiveState(tokyo, instant, ZoneId.of("UTC"))

        // Tokyo is UTC+9 -> 21:00:00
        assertEquals("21:00", state.formattedTime24)
        assertEquals("+9h ahead", state.formattedOffset)
        assertEquals(540, state.offsetMinutesFromUser)
        assertFalse(state.isDaytime) // 21:00 is night

        // Analog angles for 21:00:00 (9:00 PM) -> Hour hand at 270 degrees (9 * 30), Minute at 0
        assertEquals(270f, state.hourAngle, 0.01f)
        assertEquals(0f, state.minuteAngle, 0.01f)
    }

    @Test
    fun `filterCities by region and search query`() {
        val instant = Instant.parse("2026-09-02T12:00:00Z")

        // Filter Asia-Pacific
        val apac = engine.filterCities(regionFilter = "Asia-Pacific", instant = instant)
        assertTrue(apac.isNotEmpty())
        assertTrue(apac.all { it.city.region == "Asia-Pacific" })

        // Search query "NYSE"
        val nyseSearch = engine.filterCities(query = "NYSE", instant = instant)
        assertEquals(1, nyseSearch.size)
        assertEquals("New York", nyseSearch.first().city.cityName)
    }

    @Test
    fun `toggle pin updates city pinned state`() {
        val cityId = "cai"
        val initiallyPinned = engine.isCityPinned(cityId)

        engine.togglePin(cityId)
        assertEquals(!initiallyPinned, engine.isCityPinned(cityId))

        engine.togglePin(cityId)
        assertEquals(initiallyPinned, engine.isCityPinned(cityId))
    }
}
