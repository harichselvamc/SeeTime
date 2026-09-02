package com.harichselvamc.seetime.util

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

data class MetricDecimalTime(
    val decimalHours: Int, // 0..9
    val decimalMinutes: Int, // 0..99
    val decimalSeconds: Int, // 0..99
    val totalDecimalFraction: Double, // 0.0 to 1.0
    val formatted: String // e.g. "5:41:66"
)

data class SwatchBeatDetails(
    val beats: Double, // 0.0 to 999.99
    val integerBeats: Int, // 0..999
    val centibeats: Int, // 0..99
    val formattedString: String, // "@542.85"
    val bmtTimeFormatted: String, // "13:01:42 BMT"
    val dayProgressPercent: Float // 0..100%
)

data class InternetMeetingPreset(
    val title: String,
    val beats: Int,
    val bmtUtcTime: String,
    val description: String
)

object DecimalTimeEngine {

    const val SECONDS_PER_BEAT = 86.4 // 1 .beat = 86.4 seconds

    val POPULAR_BEAT_PRESETS = listOf(
        InternetMeetingPreset("Global Sync / Standup", 375, "08:00 UTC / 09:00 BMT", "Morning Europe/Africa, Afternoon Asia, Evening Pacific"),
        InternetMeetingPreset("Global Gaming / Stream Peak", 750, "17:00 UTC / 18:00 BMT", "Evening Europe, Midday Americas, Dawn Asia"),
        InternetMeetingPreset("Cyber Midnight Epoch", 0, "23:00 UTC / 00:00 BMT", "Biel Mean Time Midnight rollover"),
        InternetMeetingPreset("Biel Solar Noon", 500, "11:00 UTC / 12:00 BMT", "Midway point of the global internet day")
    )

    /**
     * Calculates Swatch Internet Time (.beats) from any [ZonedDateTime].
     * Synchronized globally to Biel Mean Time (BMT = UTC+1).
     */
    fun calculateSwatchBeats(dateTime: ZonedDateTime): SwatchBeatDetails {
        // Convert to UTC+1 (BMT)
        val bmtZdt = dateTime.withZoneSameInstant(ZoneOffset.ofHours(1))

        val hour = bmtZdt.hour
        val min = bmtZdt.minute
        val sec = bmtZdt.second
        val nano = bmtZdt.nano

        val totalBmtSeconds = (hour * 3600.0) + (min * 60.0) + sec + (nano / 1_000_000_000.0)
        val totalCentibeats = Math.round(totalBmtSeconds / 0.864).coerceIn(0L, 99999L)
        val intBeats = (totalCentibeats / 100L).toInt().coerceIn(0, 999)
        val centibeats = (totalCentibeats % 100L).toInt().coerceIn(0, 99)
        val beatsExact = totalCentibeats / 100.0

        val formatted = String.format(Locale.US, "@%03d.%02d", intBeats, centibeats)
        val bmtFormatted = String.format(Locale.US, "%02d:%02d:%02d BMT", hour, min, sec)

        return SwatchBeatDetails(
            beats = beatsExact,
            integerBeats = intBeats,
            centibeats = centibeats,
            formattedString = formatted,
            bmtTimeFormatted = bmtFormatted,
            dayProgressPercent = (beatsExact / 10.0).toFloat()
        )
    }

    /**
     * Calculates French Revolutionary Metric Decimal Time (10 hours/day, 100 min/hr, 100 sec/min).
     */
    fun calculateMetricDecimalTime(dateTime: ZonedDateTime): MetricDecimalTime {
        val hour = dateTime.hour
        val min = dateTime.minute
        val sec = dateTime.second
        val nano = dateTime.nano

        val totalStandardSeconds = (hour * 3600.0) + (min * 60.0) + sec + (nano / 1_000_000_000.0)
        val dayFraction = totalStandardSeconds / 86400.0

        val totalDecimalSeconds = (dayFraction * 100000.0).coerceIn(0.0, 99999.99)

        val decHours = (totalDecimalSeconds / 10000.0).toInt().coerceIn(0, 9)
        val decMins = ((totalDecimalSeconds % 10000.0) / 100.0).toInt().coerceIn(0, 99)
        val decSecs = (totalDecimalSeconds % 100.0).toInt().coerceIn(0, 99)

        val formatted = String.format(Locale.US, "%d:%02d:%02d", decHours, decMins, decSecs)

        return MetricDecimalTime(
            decimalHours = decHours,
            decimalMinutes = decMins,
            decimalSeconds = decSecs,
            totalDecimalFraction = dayFraction,
            formatted = formatted
        )
    }

    /**
     * Converts Swatch .beats value back into standard UTC time.
     */
    fun beatsToUtcTime(beats: Double): LocalTime {
        val clampedBeats = beats.coerceIn(0.0, 999.999)
        val bmtSeconds = clampedBeats * SECONDS_PER_BEAT
        val utcSeconds = ((bmtSeconds - 3600.0) + 86400.0).mod(86400.0)

        val h = (utcSeconds / 3600.0).toInt().coerceIn(0, 23)
        val m = ((utcSeconds % 3600.0) / 60.0).toInt().coerceIn(0, 59)
        val s = (utcSeconds % 60.0).toInt().coerceIn(0, 59)

        return LocalTime.of(h, m, s)
    }

    /**
     * Converts standard local time to Swatch .beats.
     */
    fun localTimeToBeat(hour: Int, minute: Int, second: Int, zoneId: ZoneId = ZoneId.systemDefault()): Double {
        val today = java.time.LocalDate.now()
        val zdt = ZonedDateTime.of(today, LocalTime.of(hour, minute, second), zoneId)
        return calculateSwatchBeats(zdt).beats
    }
}
