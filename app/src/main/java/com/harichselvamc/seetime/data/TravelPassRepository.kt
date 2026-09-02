package com.harichselvamc.seetime.data

import android.content.Context
import android.content.SharedPreferences
import com.harichselvamc.seetime.util.BarcodeFormat
import com.harichselvamc.seetime.util.PassType
import com.harichselvamc.seetime.util.TravelPass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

class TravelPassRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("seetime_travel_vault", Context.MODE_PRIVATE)

    private val _passes = MutableStateFlow<List<TravelPass>>(emptyList())
    val passes: StateFlow<List<TravelPass>> = _passes.asStateFlow()

    companion object {
        fun createSamplePasses(): List<TravelPass> {
            val now = Instant.now()
            val flightDep = now.plus(4, ChronoUnit.HOURS).toEpochMilli()
            val flightArr = now.plus(18, ChronoUnit.HOURS).toEpochMilli()

            val trainDep = now.plus(2, ChronoUnit.DAYS).toEpochMilli()
            val trainArr = now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS).plus(20, ChronoUnit.MINUTES).toEpochMilli()

            val hotelCheckIn = now.plus(19, ChronoUnit.HOURS).toEpochMilli()
            val hotelCheckOut = now.plus(5, ChronoUnit.DAYS).toEpochMilli()

            return listOf(
                TravelPass(
                    id = "sample-pass-1",
                    passType = PassType.FLIGHT,
                    carrierName = "Japan Airlines",
                    carrierCode = "JL",
                    serviceNumber = "005",
                    passengerName = "HARICHSELVAM / C",
                    originCode = "JFK",
                    originCity = "New York",
                    originZoneId = "America/New_York",
                    destCode = "HND",
                    destCity = "Tokyo",
                    destZoneId = "Asia/Tokyo",
                    departureEpochMillis = flightDep,
                    arrivalEpochMillis = flightArr,
                    gate = "B24",
                    terminal = "1",
                    seat = "12A",
                    boardingGroup = "Group 1",
                    bookingReference = "7XQ9LM",
                    barcodeFormat = BarcodeFormat.PDF417,
                    barcodePayload = "M1SEETIMER/HARIC  EJLK889 JFKHNDJL 005 240Y012A0024 100",
                    isPinned = true,
                    notes = "Priority Boarding · Cabin Luggage 10kg · In-flight Wi-Fi Included"
                ),
                TravelPass(
                    id = "sample-pass-2",
                    passType = PassType.TRAIN,
                    carrierName = "Eurostar Express",
                    carrierCode = "EST",
                    serviceNumber = "9014",
                    passengerName = "HARICHSELVAM / C",
                    originCode = "STP",
                    originCity = "London St Pancras",
                    originZoneId = "Europe/London",
                    destCode = "XPG",
                    destCity = "Paris Gare du Nord",
                    destZoneId = "Europe/Paris",
                    departureEpochMillis = trainDep,
                    arrivalEpochMillis = trainArr,
                    gate = "Platform 7",
                    terminal = "Intl",
                    seat = "Coach 05 / 42",
                    boardingGroup = "Standard Premier",
                    bookingReference = "EUR-48821",
                    barcodeFormat = BarcodeFormat.QR_CODE,
                    barcodePayload = "EUROSTAR-TICKET:EST9014:STP-XPG:COACH05:SEAT42:REF-EUR48821",
                    isPinned = false,
                    notes = "Board 30m prior for passport control · Power outlets at seat"
                ),
                TravelPass(
                    id = "sample-pass-3",
                    passType = PassType.HOTEL,
                    carrierName = "Park Hyatt Tokyo",
                    carrierCode = "PHT",
                    serviceNumber = "HTL",
                    passengerName = "HARICHSELVAM / C",
                    originCode = "TYO",
                    originCity = "Shinjuku, Tokyo",
                    originZoneId = "Asia/Tokyo",
                    destCode = "TYO",
                    destCity = "Tokyo",
                    destZoneId = "Asia/Tokyo",
                    departureEpochMillis = hotelCheckIn,
                    arrivalEpochMillis = hotelCheckOut,
                    gate = "Front Desk",
                    terminal = "Tower 41F",
                    seat = "Deluxe King (City View)",
                    boardingGroup = "Check-in 15:00 JST",
                    bookingReference = "HTL-88219",
                    barcodeFormat = BarcodeFormat.AZTEC,
                    barcodePayload = "HOTEL-CONFIRMATION:PARK-HYATT-TOKYO:RES-HTL-88219:HARIC",
                    isPinned = false,
                    notes = "Late checkout requested (14:00) · 24h Concierge · Airport Limousine Bus Stop"
                )
            )
        }
    }

    init {
        loadFromStorage()
    }

    fun addPass(pass: TravelPass) {
        _passes.value = listOf(pass) + _passes.value.filterNot { it.id == pass.id }
        saveToStorage()
    }

    fun deletePass(passId: String) {
        _passes.value = _passes.value.filterNot { it.id == passId }
        saveToStorage()
    }

    fun togglePin(passId: String) {
        _passes.value = _passes.value.map {
            if (it.id == passId) it.copy(isPinned = !it.isPinned) else it
        }
        saveToStorage()
    }

    fun resetToDefault() {
        _passes.value = createSamplePasses()
        saveToStorage()
    }

    private fun saveToStorage() {
        val sp = prefs ?: return
        try {
            val arr = JSONArray()
            for (p in _passes.value) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("passType", p.passType.name)
                    put("carrierName", p.carrierName)
                    put("carrierCode", p.carrierCode)
                    put("serviceNumber", p.serviceNumber)
                    put("passengerName", p.passengerName)
                    put("originCode", p.originCode)
                    put("originCity", p.originCity)
                    put("originZoneId", p.originZoneId)
                    put("destCode", p.destCode)
                    put("destCity", p.destCity)
                    put("destZoneId", p.destZoneId)
                    put("departureEpochMillis", p.departureEpochMillis)
                    put("arrivalEpochMillis", p.arrivalEpochMillis)
                    put("gate", p.gate)
                    put("terminal", p.terminal)
                    put("seat", p.seat)
                    put("boardingGroup", p.boardingGroup)
                    put("bookingReference", p.bookingReference)
                    put("barcodeFormat", p.barcodeFormat.name)
                    put("barcodePayload", p.barcodePayload)
                    put("isPinned", p.isPinned)
                    put("notes", p.notes)
                }
                arr.put(obj)
            }
            sp.edit().putString("saved_passes", arr.toString()).apply()
        } catch (_: Exception) {
            // Storage safety
        }
    }

    private fun loadFromStorage() {
        val sp = prefs
        if (sp == null || !sp.contains("saved_passes")) {
            _passes.value = createSamplePasses()
            return
        }

        try {
            val raw = sp.getString("saved_passes", "[]") ?: "[]"
            val arr = JSONArray(raw)
            val list = mutableListOf<TravelPass>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TravelPass(
                        id = obj.getString("id"),
                        passType = try { PassType.valueOf(obj.getString("passType")) } catch (_: Exception) { PassType.FLIGHT },
                        carrierName = obj.getString("carrierName"),
                        carrierCode = obj.getString("carrierCode"),
                        serviceNumber = obj.getString("serviceNumber"),
                        passengerName = obj.getString("passengerName"),
                        originCode = obj.getString("originCode"),
                        originCity = obj.getString("originCity"),
                        originZoneId = obj.getString("originZoneId"),
                        destCode = obj.getString("destCode"),
                        destCity = obj.getString("destCity"),
                        destZoneId = obj.getString("destZoneId"),
                        departureEpochMillis = obj.getLong("departureEpochMillis"),
                        arrivalEpochMillis = obj.getLong("arrivalEpochMillis"),
                        gate = obj.optString("gate", "B24"),
                        terminal = obj.optString("terminal", "1"),
                        seat = obj.optString("seat", "12A"),
                        boardingGroup = obj.optString("boardingGroup", "Group 1"),
                        bookingReference = obj.optString("bookingReference", "7XQ9LM"),
                        barcodeFormat = try { BarcodeFormat.valueOf(obj.getString("barcodeFormat")) } catch (_: Exception) { BarcodeFormat.QR_CODE },
                        barcodePayload = obj.optString("barcodePayload", ""),
                        isPinned = obj.optBoolean("isPinned", false),
                        notes = obj.optString("notes", "")
                    )
                )
            }
            if (list.isNotEmpty()) {
                _passes.value = list
            } else {
                _passes.value = createSamplePasses()
            }
        } catch (_: Exception) {
            _passes.value = createSamplePasses()
        }
    }
}
