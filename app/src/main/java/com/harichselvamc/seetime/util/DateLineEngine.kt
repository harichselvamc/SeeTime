package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class IdlFlightRoute(
    val routeCode: String,
    val originCity: String,
    val originAirportCode: String,
    val originZoneId: String,
    val originLat: Double,
    val originLon: Double,
    val destCity: String,
    val destAirportCode: String,
    val destZoneId: String,
    val destLat: Double,
    val destLon: Double,
    val flightDurationHours: Double,
    val isEastboundTimeTravel: Boolean,
    val paradoxTitle: String,
    val paradoxDescription: String
)

data class TransPacificFlightStatus(
    val route: IdlFlightRoute,
    val progressFraction: Float,
    val planeLat: Double,
    val planeLon: Double,
    val hasCrossedDateLine: Boolean,
    val departureZdt: ZonedDateTime,
    val arrivalZdt: ZonedDateTime,
    val calendarDayDelta: Int, // -1 (Yesterday), 0 (Same day), +1 (Tomorrow / Future)
    val dayShiftBannerText: String,
    val timeTravelBadge: String
)

object DateLineEngine {

    // Actual geopolitical zigzag path of the International Date Line
    val IDL_ZIGZAG_POLYLINE = listOf(
        Pair(85.0, 180.0),
        Pair(65.5, 169.0),  // Bering Strait (between Russia & Alaska)
        Pair(60.0, 180.0),
        Pair(52.0, 170.0),  // West of Aleutian Islands (US stays on American side)
        Pair(48.0, 180.0),
        Pair(5.0, 180.0),
        Pair(0.0, -150.0),  // Bulge East around Kiribati / Line Islands (UTC+14)
        Pair(-12.0, -150.0),
        Pair(-15.0, -172.5), // West of Samoa / Tonga (UTC+13)
        Pair(-45.0, 180.0),
        Pair(-85.0, 180.0)
    )

    val POPULAR_IDL_ROUTES = listOf(
        IdlFlightRoute(
            routeCode = "HND ✈ HNL",
            originCity = "Tokyo",
            originAirportCode = "HND",
            originZoneId = "Asia/Tokyo",
            originLat = 35.5494,
            originLon = 139.7798,
            destCity = "Honolulu",
            destAirportCode = "HNL",
            destZoneId = "Pacific/Honolulu",
            destLat = 21.3187,
            destLon = -157.9225,
            flightDurationHours = 8.0,
            isEastboundTimeTravel = true,
            paradoxTitle = "The Yesterday Machine",
            paradoxDescription = "Depart Tokyo on Monday morning, cross the Date Line eastward, and land in Hawaii on Sunday evening of the previous day!"
        ),
        IdlFlightRoute(
            routeCode = "SYD ✈ LAX",
            originCity = "Sydney",
            originAirportCode = "SYD",
            originZoneId = "Australia/Sydney",
            originLat = -33.9399,
            originLon = 151.1753,
            destCity = "Los Angeles",
            destAirportCode = "LAX",
            destZoneId = "America/Los_Angeles",
            destLat = 33.9416,
            destLon = -118.4085,
            flightDurationHours = 14.0,
            isEastboundTimeTravel = true,
            paradoxTitle = "Double Birthday Flight",
            paradoxDescription = "Depart Sydney at 11:00 AM and arrive in Los Angeles at 07:00 AM on the SAME morning, landing 4 hours earlier than when you took off!"
        ),
        IdlFlightRoute(
            routeCode = "APW ✈ PPG",
            originCity = "Samoa (Apia)",
            originAirportCode = "APW",
            originZoneId = "Pacific/Apia", // UTC+13
            originLat = -13.8300,
            originLon = -171.7600,
            destCity = "American Samoa (Pago Pago)",
            destAirportCode = "PPG",
            destZoneId = "Pacific/Pago_Pago", // UTC-11
            destLat = -14.3317,
            destLon = -170.7106,
            flightDurationHours = 0.45, // 25 min flight
            isEastboundTimeTravel = true,
            paradoxTitle = "The 24-Hour 25-Minute Leap",
            paradoxDescription = "A 25-minute flight across just 120 km of ocean transports you 24 full hours backward in time between the two Samoas."
        ),
        IdlFlightRoute(
            routeCode = "SFO ✈ AKL",
            originCity = "San Francisco",
            originAirportCode = "SFO",
            originZoneId = "America/Los_Angeles",
            originLat = 37.6213,
            originLon = -122.3790,
            destCity = "Auckland",
            destAirportCode = "AKL",
            destZoneId = "Pacific/Auckland",
            destLat = -37.0082,
            destLon = 174.7850,
            flightDurationHours = 13.0,
            isEastboundTimeTravel = false,
            paradoxTitle = "The Disappearing Day",
            paradoxDescription = "Flying westbound into the future: Depart Monday night at 21:00, cross the Date Line, and touch down Wednesday at 05:30. Tuesday disappears completely!"
        )
    )

    /**
     * Calculates the flight progress and time warp parameters for a given route and progress fraction (0.0 to 1.0).
     */
    fun calculateFlightStatus(
        route: IdlFlightRoute,
        progressFraction: Float = 0.5f,
        departureTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of(route.originZoneId))
    ): TransPacificFlightStatus {
        val prog = progressFraction.coerceIn(0.0f, 1.0f)

        // Interpolate plane coordinates (handling longitude wrapping across Pacific 180°)
        val originLonNorm = if (route.originLon < 0) route.originLon + 360.0 else route.originLon
        val destLonNorm = if (route.destLon < 0) route.destLon + 360.0 else route.destLon

        val currentLat = route.originLat + (route.destLat - route.originLat) * prog
        var currentLonNorm = originLonNorm + (destLonNorm - originLonNorm) * prog
        val currentLon = if (currentLonNorm > 180.0) currentLonNorm - 360.0 else currentLonNorm

        // Calculate arrival time in destination timezone
        val duration = Duration.ofMinutes((route.flightDurationHours * 60).toLong())
        val arrivalUtc = departureTime.toInstant().plus(duration)
        val arrivalZdt = arrivalUtc.atZone(ZoneId.of(route.destZoneId))

        // Has crossed Date Line (approx mid-point of trans-Pacific corridor)
        val hasCrossed = prog >= 0.48f

        val depDate = departureTime.toLocalDate()
        val arrDate = arrivalZdt.toLocalDate()
        val dayDelta = ChronoUnit.DAYS.between(depDate, arrDate).toInt()

        val banner = when {
            dayDelta < 0 -> "TIME WARP: You have traveled BACK in time to ${arrivalZdt.dayOfWeek} (Yesterday!)"
            dayDelta > 0 -> "FUTURE JUMP: You skipped ahead into ${arrivalZdt.dayOfWeek} (Tomorrow!)"
            else -> "Same calendar day arrival ($dayDelta days delta)"
        }

        val badge = when {
            dayDelta < 0 -> "-1 DAY (YESTERDAY)"
            dayDelta > 0 -> "+1 DAY (TOMORROW)"
            else -> "SAME DAY"
        }

        return TransPacificFlightStatus(
            route = route,
            progressFraction = prog,
            planeLat = currentLat,
            planeLon = currentLon,
            hasCrossedDateLine = hasCrossed,
            departureZdt = departureTime,
            arrivalZdt = arrivalZdt,
            calendarDayDelta = dayDelta,
            dayShiftBannerText = banner,
            timeTravelBadge = badge
        )
    }
}
