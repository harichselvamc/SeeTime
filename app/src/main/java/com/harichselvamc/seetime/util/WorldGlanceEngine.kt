package com.harichselvamc.seetime.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

enum class MarketStatus(val label: String) {
    OPEN("MARKET OPEN"),
    CLOSING_SOON("CLOSING SOON"),
    CLOSED("CLOSED"),
    WEEKEND("WEEKEND CLOSED")
}

data class MarketInfo(
    val exchangeName: String,
    val exchangeCode: String,
    val openLocalTime: LocalTime,
    val closeLocalTime: LocalTime
)

data class WorldGlanceCity(
    val id: String,
    val cityName: String,
    val countryName: String,
    val countryCode: String,
    val flagEmoji: String,
    val timezoneId: String,
    val marketInfo: MarketInfo?,
    val region: String,
    val isPinned: Boolean = false
)

data class CityLiveState(
    val city: WorldGlanceCity,
    val zonedDateTime: ZonedDateTime,
    val formattedTime12: String,
    val formattedTime24: String,
    val formattedSeconds: String,
    val amPm: String,
    val formattedDate: String,
    val offsetMinutesFromUser: Int,
    val formattedOffset: String,
    val isDaytime: Boolean,
    val marketStatus: MarketStatus,
    val marketStatusDescription: String,
    val hourAngle: Float,
    val minuteAngle: Float,
    val secondAngle: Float
)

class WorldGlanceEngine(context: Context? = null) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("seetime_world_glance_wall", Context.MODE_PRIVATE)

    companion object {
        val WORLD_CITIES_DATABASE = listOf(
            WorldGlanceCity(
                id = "nyc",
                cityName = "New York",
                countryName = "United States",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                timezoneId = "America/New_York",
                marketInfo = MarketInfo("New York Stock Exchange", "NYSE", LocalTime.of(9, 30), LocalTime.of(16, 0)),
                region = "Americas",
                isPinned = true
            ),
            WorldGlanceCity(
                id = "lon",
                cityName = "London",
                countryName = "United Kingdom",
                countryCode = "GB",
                flagEmoji = "🇬🇧",
                timezoneId = "Europe/London",
                marketInfo = MarketInfo("London Stock Exchange", "LSE", LocalTime.of(8, 0), LocalTime.of(16, 30)),
                region = "Europe",
                isPinned = true
            ),
            WorldGlanceCity(
                id = "tyo",
                cityName = "Tokyo",
                countryName = "Japan",
                countryCode = "JP",
                flagEmoji = "🇯🇵",
                timezoneId = "Asia/Tokyo",
                marketInfo = MarketInfo("Tokyo Stock Exchange", "TSE", LocalTime.of(9, 0), LocalTime.of(15, 30)),
                region = "Asia-Pacific",
                isPinned = true
            ),
            WorldGlanceCity(
                id = "sfo",
                cityName = "San Francisco",
                countryName = "United States",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                timezoneId = "America/Los_Angeles",
                marketInfo = MarketInfo("Silicon Valley Tech Hub", "NASDAQ", LocalTime.of(9, 30), LocalTime.of(16, 0)),
                region = "Americas",
                isPinned = true
            ),
            WorldGlanceCity(
                id = "par",
                cityName = "Paris",
                countryName = "France",
                countryCode = "FR",
                flagEmoji = "🇫🇷",
                timezoneId = "Europe/Paris",
                marketInfo = MarketInfo("Euronext Paris", "EPA", LocalTime.of(9, 0), LocalTime.of(17, 30)),
                region = "Europe",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "sin",
                cityName = "Singapore",
                countryName = "Singapore",
                countryCode = "SG",
                flagEmoji = "🇸🇬",
                timezoneId = "Asia/Singapore",
                marketInfo = MarketInfo("Singapore Exchange", "SGX", LocalTime.of(9, 0), LocalTime.of(17, 0)),
                region = "Asia-Pacific",
                isPinned = true
            ),
            WorldGlanceCity(
                id = "syd",
                cityName = "Sydney",
                countryName = "Australia",
                countryCode = "AU",
                flagEmoji = "🇦🇺",
                timezoneId = "Australia/Sydney",
                marketInfo = MarketInfo("Australian Securities Exchange", "ASX", LocalTime.of(10, 0), LocalTime.of(16, 0)),
                region = "Asia-Pacific",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "dxb",
                cityName = "Dubai",
                countryName = "United Arab Emirates",
                countryCode = "AE",
                flagEmoji = "🇦🇪",
                timezoneId = "Asia/Dubai",
                marketInfo = MarketInfo("Dubai Financial Market", "DFM", LocalTime.of(10, 0), LocalTime.of(15, 0)),
                region = "Middle East & Africa",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "hkg",
                cityName = "Hong Kong",
                countryName = "Hong Kong",
                countryCode = "HK",
                flagEmoji = "🇭🇰",
                timezoneId = "Asia/Hong_Kong",
                marketInfo = MarketInfo("Hong Kong Exchanges", "HKEX", LocalTime.of(9, 30), LocalTime.of(16, 0)),
                region = "Asia-Pacific",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "fra",
                cityName = "Frankfurt",
                countryName = "Germany",
                countryCode = "DE",
                flagEmoji = "🇩🇪",
                timezoneId = "Europe/Berlin",
                marketInfo = MarketInfo("Frankfurt Stock Exchange", "XETRA", LocalTime.of(9, 0), LocalTime.of(17, 30)),
                region = "Europe",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "sao",
                cityName = "São Paulo",
                countryName = "Brazil",
                countryCode = "BR",
                flagEmoji = "🇧🇷",
                timezoneId = "America/Sao_Paulo",
                marketInfo = MarketInfo("Brasil Bolsa Balcão", "B3", LocalTime.of(10, 0), LocalTime.of(17, 0)),
                region = "Americas",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "tor",
                cityName = "Toronto",
                countryName = "Canada",
                countryCode = "CA",
                flagEmoji = "🇨🇦",
                timezoneId = "America/Toronto",
                marketInfo = MarketInfo("Toronto Stock Exchange", "TSX", LocalTime.of(9, 30), LocalTime.of(16, 0)),
                region = "Americas",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "zrh",
                cityName = "Zurich",
                countryName = "Switzerland",
                countryCode = "CH",
                flagEmoji = "🇨🇭",
                timezoneId = "Europe/Zurich",
                marketInfo = MarketInfo("SIX Swiss Exchange", "SIX", LocalTime.of(9, 0), LocalTime.of(17, 30)),
                region = "Europe",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "bom",
                cityName = "Mumbai",
                countryName = "India",
                countryCode = "IN",
                flagEmoji = "🇮🇳",
                timezoneId = "Asia/Kolkata",
                marketInfo = MarketInfo("National Stock Exchange of India", "NSE", LocalTime.of(9, 15), LocalTime.of(15, 30)),
                region = "Asia-Pacific",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "icn",
                cityName = "Seoul",
                countryName = "South Korea",
                countryCode = "KR",
                flagEmoji = "🇰🇷",
                timezoneId = "Asia/Seoul",
                marketInfo = MarketInfo("Korea Exchange", "KRX", LocalTime.of(9, 0), LocalTime.of(15, 30)),
                region = "Asia-Pacific",
                isPinned = false
            ),
            WorldGlanceCity(
                id = "cai",
                cityName = "Cairo",
                countryName = "Egypt",
                countryCode = "EG",
                flagEmoji = "🇪🇬",
                timezoneId = "Africa/Cairo",
                marketInfo = MarketInfo("The Egyptian Exchange", "EGX", LocalTime.of(10, 0), LocalTime.of(14, 30)),
                region = "Middle East & Africa",
                isPinned = false
            )
        )

        @Volatile
        private var INSTANCE: WorldGlanceEngine? = null

        fun getInstance(context: Context): WorldGlanceEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorldGlanceEngine(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val _pinnedCityIds = MutableStateFlow<Set<String>>(setOf("nyc", "lon", "tyo", "sfo", "sin"))
    val pinnedCityIds: StateFlow<Set<String>> = _pinnedCityIds.asStateFlow()

    init {
        loadPinnedState()
    }

    fun togglePin(cityId: String) {
        val current = _pinnedCityIds.value.toMutableSet()
        if (current.contains(cityId)) {
            current.remove(cityId)
        } else {
            current.add(cityId)
        }
        _pinnedCityIds.value = current
        savePinnedState()
    }

    fun isCityPinned(cityId: String): Boolean = _pinnedCityIds.value.contains(cityId)

    /**
     * Computes the real-time live clock, market status, and daylight state for [city].
     */
    fun computeCityLiveState(
        city: WorldGlanceCity,
        instant: Instant = Instant.now(),
        userZoneId: ZoneId = ZoneId.systemDefault()
    ): CityLiveState {
        val zone = try { ZoneId.of(city.timezoneId) } catch (_: Exception) { ZoneId.of("UTC") }
        val cityZdt = instant.atZone(zone)
        val userZdt = instant.atZone(userZoneId)

        val localTime = cityZdt.toLocalTime()
        val localDate = cityZdt.toLocalDate()

        val time12Formatter = DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault())
        val time24Formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        val secondsFormatter = DateTimeFormatter.ofPattern("ss", Locale.getDefault())
        val amPmFormatter = DateTimeFormatter.ofPattern("a", Locale.getDefault())
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault())

        val formatted12 = cityZdt.format(time12Formatter)
        val formatted24 = cityZdt.format(time24Formatter)
        val formattedSecs = cityZdt.format(secondsFormatter)
        val amPm = cityZdt.format(amPmFormatter)
        val formattedDate = cityZdt.format(dateFormatter)

        // Offset relative to user's local timezone
        val userOffsetSecs = userZdt.offset.totalSeconds
        val cityOffsetSecs = cityZdt.offset.totalSeconds
        val diffMinutes = (cityOffsetSecs - userOffsetSecs) / 60

        val formattedOffset = when {
            diffMinutes > 0 -> {
                val h = diffMinutes / 60
                val m = abs(diffMinutes % 60)
                if (m > 0) "+${h}h ${m}m ahead" else "+${h}h ahead"
            }
            diffMinutes < 0 -> {
                val absMin = abs(diffMinutes)
                val h = absMin / 60
                val m = absMin % 60
                if (m > 0) "-${h}h ${m}m behind" else "-${h}h behind"
            }
            else -> "Same Time"
        }

        // Daytime heuristic: 6:00 AM to 6:30 PM (18:30)
        val hour = localTime.hour
        val minute = localTime.minute
        val timeInMin = hour * 60 + minute
        val isDaytime = timeInMin in (6 * 60)..(18 * 60 + 30)

        // Market Trading Status calculation
        val (marketStatus, marketDesc) = evaluateMarketStatus(city.marketInfo, localDate, localTime)

        // Analog Clock Angles (360 degrees)
        val second = localTime.second
        val nano = localTime.nano
        val secFraction = second + nano / 1_000_000_000f
        val secondAngle = secFraction * 6f // 360 / 60
        val minuteAngle = (minute + secFraction / 60f) * 6f
        val hourAngle = ((hour % 12) + minute / 60f + secFraction / 3600f) * 30f // 360 / 12

        return CityLiveState(
            city = city.copy(isPinned = isCityPinned(city.id)),
            zonedDateTime = cityZdt,
            formattedTime12 = formatted12,
            formattedTime24 = formatted24,
            formattedSeconds = formattedSecs,
            amPm = amPm,
            formattedDate = formattedDate,
            offsetMinutesFromUser = diffMinutes,
            formattedOffset = formattedOffset,
            isDaytime = isDaytime,
            marketStatus = marketStatus,
            marketStatusDescription = marketDesc,
            hourAngle = hourAngle,
            minuteAngle = minuteAngle,
            secondAngle = secondAngle
        )
    }

    /**
     * Evaluates financial market trading hours for [marketInfo].
     */
    fun evaluateMarketStatus(
        marketInfo: MarketInfo?,
        localDate: LocalDate,
        localTime: LocalTime
    ): Pair<MarketStatus, String> {
        if (marketInfo == null) {
            return Pair(MarketStatus.CLOSED, "Non-Financial Hub")
        }

        val dayOfWeek = localDate.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
        if (isWeekend) {
            return Pair(MarketStatus.WEEKEND, "${marketInfo.exchangeCode} Closed for Weekend")
        }

        val nowMin = localTime.hour * 60 + localTime.minute
        val openMin = marketInfo.openLocalTime.hour * 60 + marketInfo.openLocalTime.minute
        val closeMin = marketInfo.closeLocalTime.hour * 60 + marketInfo.closeLocalTime.minute

        return when {
            nowMin in openMin..closeMin -> {
                val minsUntilClose = closeMin - nowMin
                if (minsUntilClose <= 30) {
                    Pair(MarketStatus.CLOSING_SOON, "${marketInfo.exchangeCode} Closes in ${minsUntilClose}m")
                } else {
                    Pair(MarketStatus.OPEN, "${marketInfo.exchangeCode} Trading Active (${marketInfo.openLocalTime}-${marketInfo.closeLocalTime})")
                }
            }
            nowMin < openMin -> {
                val minsUntilOpen = openMin - nowMin
                val hoursUntilOpen = minsUntilOpen / 60
                val remainingMins = minsUntilOpen % 60
                val timeStr = if (hoursUntilOpen > 0) "${hoursUntilOpen}h ${remainingMins}m" else "${remainingMins}m"
                Pair(MarketStatus.CLOSED, "${marketInfo.exchangeCode} Opens in $timeStr")
            }
            else -> {
                Pair(MarketStatus.CLOSED, "${marketInfo.exchangeCode} Closed for Day")
            }
        }
    }

    /**
     * Filters and sorts cities by region, search query, and pinned priority.
     */
    fun filterCities(
        query: String = "",
        regionFilter: String = "All",
        pinnedOnly: Boolean = false,
        instant: Instant = Instant.now(),
        userZoneId: ZoneId = ZoneId.systemDefault()
    ): List<CityLiveState> {
        val q = query.trim().lowercase()

        return WORLD_CITIES_DATABASE
            .filter { city ->
                val matchRegion = regionFilter == "All" || city.region.equals(regionFilter, ignoreCase = true)
                val matchQuery = q.isEmpty() ||
                    city.cityName.lowercase().contains(q) ||
                    city.countryName.lowercase().contains(q) ||
                    city.countryCode.lowercase().contains(q) ||
                    (city.marketInfo?.exchangeCode?.lowercase()?.contains(q) == true)
                val matchPinned = !pinnedOnly || isCityPinned(city.id)

                matchRegion && matchQuery && matchPinned
            }
            .map { computeCityLiveState(it, instant, userZoneId) }
            .sortedWith(
                compareByDescending<CityLiveState> { it.city.isPinned }
                    .thenBy { it.city.cityName }
            )
    }

    private fun savePinnedState() {
        val sp = prefs ?: return
        try {
            val arr = JSONArray()
            for (id in _pinnedCityIds.value) {
                arr.put(id)
            }
            sp.edit().putString("pinned_city_ids", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadPinnedState() {
        val sp = prefs ?: return
        try {
            val raw = sp.getString("pinned_city_ids", null)
            if (raw != null) {
                val arr = JSONArray(raw)
                val set = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    set.add(arr.getString(i))
                }
                _pinnedCityIds.value = set
            }
        } catch (_: Exception) {}
    }
}
