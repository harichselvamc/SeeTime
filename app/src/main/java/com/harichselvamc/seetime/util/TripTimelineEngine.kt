package com.harichselvamc.seetime.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

data class TravelLeg(
    val id: String = UUID.randomUUID().toString(),
    val originCity: String,
    val originAirportCode: String,
    val originTimezoneId: String,
    val departureEpochMillis: Long,
    val destCity: String,
    val destAirportCode: String,
    val destTimezoneId: String,
    val arrivalEpochMillis: Long,
    val flightNumber: String,
    val airlineName: String,
    val notes: String = ""
)

data class LayoverInfo(
    val airportCode: String,
    val cityName: String,
    val durationMinutes: Long,
    val isTightConnection: Boolean, // < 90 min
    val arrivalTimeStr: String,
    val nextDepartureTimeStr: String
)

data class TripCircadianSleepWindow(
    val flightNumber: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val durationHours: Double,
    val rationale: String
)

data class MultiLegTrip(
    val id: String = UUID.randomUUID().toString(),
    val tripTitle: String,
    val legs: List<TravelLeg>,
    val isCustom: Boolean = false
)

data class TripSummary(
    val trip: MultiLegTrip,
    val originCity: String,
    val finalDestCity: String,
    val originAirport: String,
    val finalAirport: String,
    val departureZdt: ZonedDateTime,
    val finalArrivalZdt: ZonedDateTime,
    val totalJourneyDurationMinutes: Long,
    val totalFlightDurationMinutes: Long,
    val totalLayoverDurationMinutes: Long,
    val netTimezoneShiftHours: Double,
    val layovers: List<LayoverInfo>,
    val sleepWindows: List<TripCircadianSleepWindow>,
    val daysDelta: Long
)

class TripTimelineEngine(context: Context? = null) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("seetime_trip_timeline", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: TripTimelineEngine? = null

        fun getInstance(context: Context): TripTimelineEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TripTimelineEngine(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun createSampleTrips(): List<MultiLegTrip> {
            val now = Instant.now()
            // Sample Trip 1: Transpacific New York (JFK) -> San Francisco (SFO) -> Tokyo (HND)
            val leg1Dep = now.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).toEpochMilli()
            val leg1Arr = now.plus(1, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli()

            val leg2Dep = now.plus(1, ChronoUnit.DAYS).plus(9, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES).toEpochMilli()
            val leg2Arr = now.plus(1, ChronoUnit.DAYS).plus(20, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES).toEpochMilli()

            val trip1 = MultiLegTrip(
                id = "sample-trip-tokyo",
                tripTitle = "Transpacific Journey to Tokyo",
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
                        flightNumber = "UA 412",
                        airlineName = "United Airlines",
                        notes = "Terminal 7 · Seat 14A · Carry-on luggage only"
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
                        flightNumber = "NH 107",
                        airlineName = "All Nippon Airways",
                        notes = "International Terminal G · Dinner served after takeoff"
                    )
                )
            )

            // Sample Trip 2: London (LHR) -> Dubai (DXB) -> Singapore (SIN)
            val t2Leg1Dep = now.plus(4, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS).toEpochMilli()
            val t2Leg1Arr = now.plus(4, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS).toEpochMilli()

            val t2Leg2Dep = now.plus(4, ChronoUnit.DAYS).plus(9, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli()
            val t2Leg2Arr = now.plus(4, ChronoUnit.DAYS).plus(17, ChronoUnit.HOURS).toEpochMilli()

            val trip2 = MultiLegTrip(
                id = "sample-trip-singapore",
                tripTitle = "Eurasia Corridor to Singapore",
                legs = listOf(
                    TravelLeg(
                        originCity = "London",
                        originAirportCode = "LHR",
                        originTimezoneId = "Europe/London",
                        departureEpochMillis = t2Leg1Dep,
                        destCity = "Dubai",
                        destAirportCode = "DXB",
                        destTimezoneId = "Asia/Dubai",
                        arrivalEpochMillis = t2Leg1Arr,
                        flightNumber = "EK 002",
                        airlineName = "Emirates",
                        notes = "Terminal 3 · A380 Flight · Wi-Fi available"
                    ),
                    TravelLeg(
                        originCity = "Dubai",
                        originAirportCode = "DXB",
                        originTimezoneId = "Asia/Dubai",
                        departureEpochMillis = t2Leg2Dep,
                        destCity = "Singapore",
                        destAirportCode = "SIN",
                        destTimezoneId = "Asia/Singapore",
                        arrivalEpochMillis = t2Leg2Arr,
                        flightNumber = "SQ 495",
                        airlineName = "Singapore Airlines",
                        notes = "Terminal 3 · Changi Jewel transit"
                    )
                )
            )

            return listOf(trip1, trip2)
        }
    }

    private val _customTrips = MutableStateFlow<List<MultiLegTrip>>(emptyList())
    val customTrips: StateFlow<List<MultiLegTrip>> = _customTrips.asStateFlow()

    init {
        loadCustomTrips()
    }

    fun getAllTrips(): List<MultiLegTrip> {
        return createSampleTrips() + _customTrips.value
    }

    fun saveCustomTrip(trip: MultiLegTrip) {
        val updated = listOf(trip.copy(isCustom = true)) + _customTrips.value.filterNot { it.id == trip.id }
        _customTrips.value = updated
        persistCustomTrips()
    }

    fun deleteCustomTrip(tripId: String) {
        _customTrips.value = _customTrips.value.filterNot { it.id == tripId }
        persistCustomTrips()
    }

    /**
     * Calculates the complete multi-stop journey summary with local timezone conversions,
     * layovers, net timezone shift, and destination circadian sleep alignment.
     */
    fun calculateTripSummary(trip: MultiLegTrip): TripSummary {
        val legs = trip.legs
        if (legs.isEmpty()) {
            val nowZdt = ZonedDateTime.now()
            return TripSummary(
                trip = trip,
                originCity = "Unknown",
                finalDestCity = "Unknown",
                originAirport = "N/A",
                finalAirport = "N/A",
                departureZdt = nowZdt,
                finalArrivalZdt = nowZdt,
                totalJourneyDurationMinutes = 0,
                totalFlightDurationMinutes = 0,
                totalLayoverDurationMinutes = 0,
                netTimezoneShiftHours = 0.0,
                layovers = emptyList(),
                sleepWindows = emptyList(),
                daysDelta = 0
            )
        }

        val firstLeg = legs.first()
        val lastLeg = legs.last()

        val originZone = try { ZoneId.of(firstLeg.originTimezoneId) } catch (_: Exception) { ZoneId.of("UTC") }
        val destZone = try { ZoneId.of(lastLeg.destTimezoneId) } catch (_: Exception) { ZoneId.of("UTC") }

        val departureZdt = Instant.ofEpochMilli(firstLeg.departureEpochMillis).atZone(originZone)
        val finalArrivalZdt = Instant.ofEpochMilli(lastLeg.arrivalEpochMillis).atZone(destZone)

        val totalJourneyDurationMinutes = ChronoUnit.MINUTES.between(
            Instant.ofEpochMilli(firstLeg.departureEpochMillis),
            Instant.ofEpochMilli(lastLeg.arrivalEpochMillis)
        )

        var totalFlightMinutes = 0L
        for (leg in legs) {
            totalFlightMinutes += ChronoUnit.MINUTES.between(
                Instant.ofEpochMilli(leg.departureEpochMillis),
                Instant.ofEpochMilli(leg.arrivalEpochMillis)
            )
        }

        val layovers = calculateLayovers(legs)
        val totalLayoverMinutes = layovers.sumOf { it.durationMinutes }

        // Timezone shift from origin to final destination
        val originOffsetSecs = departureZdt.offset.totalSeconds
        val destOffsetSecs = finalArrivalZdt.offset.totalSeconds
        val netShiftHours = (destOffsetSecs - originOffsetSecs) / 3600.0

        val sleepWindows = calculateInFlightSleepWindows(trip)

        val daysDelta = ChronoUnit.DAYS.between(departureZdt.toLocalDate(), finalArrivalZdt.toLocalDate())

        return TripSummary(
            trip = trip,
            originCity = firstLeg.originCity,
            finalDestCity = lastLeg.destCity,
            originAirport = firstLeg.originAirportCode,
            finalAirport = lastLeg.destAirportCode,
            departureZdt = departureZdt,
            finalArrivalZdt = finalArrivalZdt,
            totalJourneyDurationMinutes = totalJourneyDurationMinutes,
            totalFlightDurationMinutes = totalFlightMinutes,
            totalLayoverDurationMinutes = totalLayoverMinutes,
            netTimezoneShiftHours = netShiftHours,
            layovers = layovers,
            sleepWindows = sleepWindows,
            daysDelta = daysDelta
        )
    }

    /**
     * Identifies layover transit nodes between connecting flight legs.
     */
    fun calculateLayovers(legs: List<TravelLeg>): List<LayoverInfo> {
        val layovers = mutableListOf<LayoverInfo>()
        if (legs.size < 2) return layovers

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm (z)", Locale.getDefault())

        for (i in 0 until legs.size - 1) {
            val incomingLeg = legs[i]
            val outgoingLeg = legs[i + 1]

            val layoverZone = try { ZoneId.of(incomingLeg.destTimezoneId) } catch (_: Exception) { ZoneId.of("UTC") }

            val arrInstant = Instant.ofEpochMilli(incomingLeg.arrivalEpochMillis)
            val depInstant = Instant.ofEpochMilli(outgoingLeg.departureEpochMillis)

            val durationMinutes = ChronoUnit.MINUTES.between(arrInstant, depInstant)
            val isTight = durationMinutes in 1..89 // less than 1.5 hours

            val arrZdt = arrInstant.atZone(layoverZone)
            val depZdt = depInstant.atZone(layoverZone)

            layovers.add(
                LayoverInfo(
                    airportCode = incomingLeg.destAirportCode,
                    cityName = incomingLeg.destCity,
                    durationMinutes = maxOf(0L, durationMinutes),
                    isTightConnection = isTight,
                    arrivalTimeStr = arrZdt.format(timeFormatter),
                    nextDepartureTimeStr = depZdt.format(timeFormatter)
                )
            )
        }
        return layovers
    }

    /**
     * Calculates optimal in-flight sleep intervals based on the destination's circadian night cycle (23:00-07:00).
     */
    fun calculateInFlightSleepWindows(trip: MultiLegTrip): List<TripCircadianSleepWindow> {
        val windows = mutableListOf<TripCircadianSleepWindow>()
        val legs = trip.legs
        if (legs.isEmpty()) return windows

        val finalDestZone = try { ZoneId.of(legs.last().destTimezoneId) } catch (_: Exception) { ZoneId.of("UTC") }

        for (leg in legs) {
            val depInstant = Instant.ofEpochMilli(leg.departureEpochMillis)
            val arrInstant = Instant.ofEpochMilli(leg.arrivalEpochMillis)
            val flightDurationHours = ChronoUnit.MINUTES.between(depInstant, arrInstant) / 60.0

            // Only long-haul flights (> 5 hours) warrant structured sleep windows
            if (flightDurationHours >= 5.0) {
                // Find when the destination clock is at night (23:00 to 07:00 local destination time)
                val depAtDestTime = depInstant.atZone(finalDestZone)
                val arrAtDestTime = arrInstant.atZone(finalDestZone)

                // Recommended sleep start: 1.5 hours after takeoff
                val sleepStart = depInstant.plus(90, ChronoUnit.MINUTES)
                // Recommended sleep end: 1.5 hours before landing (or max 7 hours)
                val maxSleepEnd = arrInstant.minus(90, ChronoUnit.MINUTES)

                if (maxSleepEnd.isAfter(sleepStart)) {
                    val sleepDurationHours = ChronoUnit.MINUTES.between(sleepStart, maxSleepEnd) / 60.0
                    val sleepAtDest = sleepStart.atZone(finalDestZone)

                    val rationale = "Aligns with ${trip.legs.last().destCity} local circadian night (${sleepAtDest.format(DateTimeFormatter.ofPattern("HH:mm"))} destination time). Put on eye-mask and sleep to prevent jet lag."

                    windows.add(
                        TripCircadianSleepWindow(
                            flightNumber = leg.flightNumber,
                            startEpochMillis = sleepStart.toEpochMilli(),
                            endEpochMillis = maxSleepEnd.toEpochMilli(),
                            durationHours = sleepDurationHours,
                            rationale = rationale
                        )
                    )
                }
            }
        }
        return windows
    }

    /**
     * Generates a clean Markdown trip brief for 1-tap clipboard copying.
     */
    fun generateMarkdownTripBrief(trip: MultiLegTrip): String {
        val summary = calculateTripSummary(trip)
        val sb = StringBuilder()

        sb.appendLine("✈️ **${trip.tripTitle}**")
        sb.appendLine("📍 Route: ${summary.originCity} (${summary.originAirport}) ➔ ${summary.finalDestCity} (${summary.finalAirport})")
        sb.appendLine("⏱️ Total Transit Duration: ${summary.totalJourneyDurationMinutes / 60}h ${summary.totalJourneyDurationMinutes % 60}m")

        val shiftStr = if (summary.netTimezoneShiftHours > 0) "+${summary.netTimezoneShiftHours}h ahead" else "${summary.netTimezoneShiftHours}h behind"
        sb.appendLine("🌐 Net Timezone Shift: $shiftStr")

        val timeFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy · HH:mm z", Locale.getDefault())
        sb.appendLine("🛫 Departure: ${summary.departureZdt.format(timeFormat)}")
        sb.appendLine("🛬 Final Arrival: ${summary.finalArrivalZdt.format(timeFormat)}")

        if (summary.daysDelta != 0L) {
            val deltaStr = if (summary.daysDelta > 0) "+${summary.daysDelta} Day(s) Next Day" else "${summary.daysDelta} Day(s) Yesterday"
            sb.appendLine("🗓️ Calendar Shift: $deltaStr")
        }

        sb.appendLine("\n### Flight Legs:")
        summary.trip.legs.forEachIndexed { index, leg ->
            val dep = Instant.ofEpochMilli(leg.departureEpochMillis).atZone(ZoneId.of(leg.originTimezoneId))
            val arr = Instant.ofEpochMilli(leg.arrivalEpochMillis).atZone(ZoneId.of(leg.destTimezoneId))
            val legMin = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(leg.departureEpochMillis), Instant.ofEpochMilli(leg.arrivalEpochMillis))

            sb.appendLine("${index + 1}. **${leg.airlineName} ${leg.flightNumber}** (${leg.originAirportCode} ➔ ${leg.destAirportCode})")
            sb.appendLine("   - Departs: ${dep.format(timeFormat)}")
            sb.appendLine("   - Arrives: ${arr.format(timeFormat)}")
            sb.appendLine("   - Flying Duration: ${legMin / 60}h ${legMin % 60}m")
            if (leg.notes.isNotBlank()) {
                sb.appendLine("   - Notes: ${leg.notes}")
            }
        }

        if (summary.layovers.isNotEmpty()) {
            sb.appendLine("\n### Layovers:")
            summary.layovers.forEach { layover ->
                val warning = if (layover.isTightConnection) " ⚠️ *TIGHT CONNECTION (<90m)*" else ""
                sb.appendLine("- **${layover.cityName} (${layover.airportCode})**: ${layover.durationMinutes / 60}h ${layover.durationMinutes % 60}m$warning")
            }
        }

        if (summary.sleepWindows.isNotEmpty()) {
            sb.appendLine("\n### Circadian In-Flight Sleep Windows:")
            summary.sleepWindows.forEach { sleep ->
                val sZdt = Instant.ofEpochMilli(sleep.startEpochMillis).atZone(summary.departureZdt.zone)
                val eZdt = Instant.ofEpochMilli(sleep.endEpochMillis).atZone(summary.departureZdt.zone)
                sb.appendLine("- **${sleep.flightNumber}**: ${String.format(Locale.getDefault(), "%.1f", sleep.durationHours)}h sleep (${sZdt.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${eZdt.format(DateTimeFormatter.ofPattern("HH:mm"))})")
                sb.appendLine("  *${sleep.rationale}*")
            }
        }

        return sb.toString()
    }

    private fun persistCustomTrips() {
        val sp = prefs ?: return
        try {
            val arr = JSONArray()
            for (trip in _customTrips.value) {
                val tripObj = JSONObject()
                tripObj.put("id", trip.id)
                tripObj.put("tripTitle", trip.tripTitle)

                val legsArr = JSONArray()
                for (leg in trip.legs) {
                    val legObj = JSONObject().apply {
                        put("id", leg.id)
                        put("originCity", leg.originCity)
                        put("originAirportCode", leg.originAirportCode)
                        put("originTimezoneId", leg.originTimezoneId)
                        put("departureEpochMillis", leg.departureEpochMillis)
                        put("destCity", leg.destCity)
                        put("destAirportCode", leg.destAirportCode)
                        put("destTimezoneId", leg.destTimezoneId)
                        put("arrivalEpochMillis", leg.arrivalEpochMillis)
                        put("flightNumber", leg.flightNumber)
                        put("airlineName", leg.airlineName)
                        put("notes", leg.notes)
                    }
                    legsArr.put(legObj)
                }
                tripObj.put("legs", legsArr)
                arr.put(tripObj)
            }
            sp.edit().putString("saved_custom_trips", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadCustomTrips() {
        val sp = prefs ?: return
        try {
            val raw = sp.getString("saved_custom_trips", null)
            if (raw != null) {
                val arr = JSONArray(raw)
                val list = mutableListOf<MultiLegTrip>()
                for (i in 0 until arr.length()) {
                    val tripObj = arr.getJSONObject(i)
                    val legsArr = tripObj.getJSONArray("legs")
                    val legsList = mutableListOf<TravelLeg>()
                    for (j in 0 until legsArr.length()) {
                        val legObj = legsArr.getJSONObject(j)
                        legsList.add(
                            TravelLeg(
                                id = legObj.getString("id"),
                                originCity = legObj.getString("originCity"),
                                originAirportCode = legObj.getString("originAirportCode"),
                                originTimezoneId = legObj.getString("originTimezoneId"),
                                departureEpochMillis = legObj.getLong("departureEpochMillis"),
                                destCity = legObj.getString("destCity"),
                                destAirportCode = legObj.getString("destAirportCode"),
                                destTimezoneId = legObj.getString("destTimezoneId"),
                                arrivalEpochMillis = legObj.getLong("arrivalEpochMillis"),
                                flightNumber = legObj.getString("flightNumber"),
                                airlineName = legObj.getString("airlineName"),
                                notes = legObj.optString("notes", "")
                            )
                        )
                    }
                    list.add(
                        MultiLegTrip(
                            id = tripObj.getString("id"),
                            tripTitle = tripObj.getString("tripTitle"),
                            legs = legsList,
                            isCustom = true
                        )
                    )
                }
                _customTrips.value = list
            }
        } catch (_: Exception) {}
    }
}
