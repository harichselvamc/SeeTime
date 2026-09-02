package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TripTimelineEngineTest {

    private lateinit var engine: TripTimelineEngine

    @Before
    fun setup() {
        engine = TripTimelineEngine(context = null)
    }

    @Test
    fun `sample trips are configured with multi-leg routes`() {
        val trips = engine.getAllTrips()
        assertTrue(trips.isNotEmpty())

        val tokyoTrip = trips.find { it.tripTitle.contains("Tokyo") }
        assertNotNull(tokyoTrip)
        assertEquals(2, tokyoTrip!!.legs.size)
        assertEquals("JFK", tokyoTrip.legs[0].originAirportCode)
        assertEquals("SFO", tokyoTrip.legs[0].destAirportCode)
        assertEquals("HND", tokyoTrip.legs[1].destAirportCode)
    }

    @Test
    fun `calculateTripSummary computes transit duration layovers and timezone shift`() {
        val now = Instant.now()
        val leg1Dep = now.toEpochMilli()
        val leg1Arr = now.plus(6, ChronoUnit.HOURS).toEpochMilli() // 6h flight
        val leg2Dep = now.plus(8, ChronoUnit.HOURS).toEpochMilli() // 2h layover
        val leg2Arr = now.plus(19, ChronoUnit.HOURS).toEpochMilli() // 11h flight

        val trip = MultiLegTrip(
            tripTitle = "Transpacific Test",
            legs = listOf(
                TravelLeg(
                    originCity = "New York",
                    originAirportCode = "JFK",
                    originTimezoneId = "America/New_York",
                    departureEpochMillis = leg1Dep,
                    destCity = "San Francisco",
                    destAirportCode = "SFO",
                    destTimezoneId = "America/Los_Angeles",
                    arrivalEpochMillis = leg1Arr,
                    flightNumber = "UA 100",
                    airlineName = "United"
                ),
                TravelLeg(
                    originCity = "San Francisco",
                    originAirportCode = "SFO",
                    originTimezoneId = "America/Los_Angeles",
                    departureEpochMillis = leg2Dep,
                    destCity = "Tokyo",
                    destAirportCode = "HND",
                    destTimezoneId = "Asia/Tokyo",
                    arrivalEpochMillis = leg2Arr,
                    flightNumber = "NH 200",
                    airlineName = "ANA"
                )
            )
        )

        val summary = engine.calculateTripSummary(trip)
        assertEquals("New York", summary.originCity)
        assertEquals("Tokyo", summary.finalDestCity)
        assertEquals(19 * 60L, summary.totalJourneyDurationMinutes)
        assertEquals(17 * 60L, summary.totalFlightDurationMinutes)
        assertEquals(2 * 60L, summary.totalLayoverDurationMinutes)

        // Timezone shift from America/New_York (-4/5 UTC) to Asia/Tokyo (+9 UTC) -> +13/14 hours
        assertTrue(summary.netTimezoneShiftHours > 12.0)

        // Layover check
        assertEquals(1, summary.layovers.size)
        assertEquals("SFO", summary.layovers[0].airportCode)
        assertEquals(120L, summary.layovers[0].durationMinutes)
        assertFalse(summary.layovers[0].isTightConnection) // 120m is not tight (<90m is tight)
    }

    @Test
    fun `calculateLayovers detects tight connection under 90 minutes`() {
        val now = Instant.now()
        val leg1Arr = now.plus(4, ChronoUnit.HOURS).toEpochMilli()
        val leg2Dep = now.plus(4, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES).toEpochMilli() // 45m layover

        val legs = listOf(
            TravelLeg(
                originCity = "London",
                originAirportCode = "LHR",
                originTimezoneId = "Europe/London",
                departureEpochMillis = now.toEpochMilli(),
                destCity = "Frankfurt",
                destAirportCode = "FRA",
                destTimezoneId = "Europe/Berlin",
                arrivalEpochMillis = leg1Arr,
                flightNumber = "LH 901",
                airlineName = "Lufthansa"
            ),
            TravelLeg(
                originCity = "Frankfurt",
                originAirportCode = "FRA",
                originTimezoneId = "Europe/Berlin",
                departureEpochMillis = leg2Dep,
                destCity = "Singapore",
                destAirportCode = "SIN",
                destTimezoneId = "Asia/Singapore",
                arrivalEpochMillis = now.plus(16, ChronoUnit.HOURS).toEpochMilli(),
                flightNumber = "SQ 25",
                airlineName = "Singapore Airlines"
            )
        )

        val layovers = engine.calculateLayovers(legs)
        assertEquals(1, layovers.size)
        assertEquals(45L, layovers[0].durationMinutes)
        assertTrue(layovers[0].isTightConnection)
    }

    @Test
    fun `generateMarkdownTripBrief exports rich brief`() {
        val trip = engine.getAllTrips().first()
        val brief = engine.generateMarkdownTripBrief(trip)
        assertTrue(brief.contains(trip.tripTitle))
        assertTrue(brief.contains("Total Transit Duration"))
        assertTrue(brief.contains("Flight Legs"))
    }
}
