package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

data class CountdownEvent(
    val id: String,
    val title: String,
    val description: String,
    val targetZdt: ZonedDateTime,
    val category: String,
    val emoji: String,
    val accentColorHex: Long
)

data class CountdownTimeRemaining(
    val totalMillisRemaining: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val millis: Long,
    val isPastEvent: Boolean,
    val tMinusFormatted: String,
    val subSecondProgress: Float
)

object EventCountdownEngine {

    val PRELOADED_EVENTS = listOf(
        CountdownEvent(
            id = "event_ny_syd_2027",
            title = "New Year 2027 (Sydney Harbour)",
            description = "First major global metropolis fireworks over Sydney Opera House",
            targetZdt = ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneId.of("Australia/Sydney")),
            category = "NEW_YEAR",
            emoji = "🎆",
            accentColorHex = 0xFFF59E0B // Gold
        ),
        CountdownEvent(
            id = "event_ny_tyo_2027",
            title = "New Year 2027 (Tokyo Skytree)",
            description = "Midnight bell strikes and illumination in Japan",
            targetZdt = ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneId.of("Asia/Tokyo")),
            category = "NEW_YEAR",
            emoji = "🗼",
            accentColorHex = 0xFFEF4444 // Red
        ),
        CountdownEvent(
            id = "event_ny_nyc_2027",
            title = "New Year 2027 (Times Square NYC)",
            description = "Iconic crystal ball drop at midnight in New York City",
            targetZdt = ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneId.of("America/New_York")),
            category = "NEW_YEAR",
            emoji = "🗽",
            accentColorHex = 0xFF38BDF8 // Sky Blue
        ),
        CountdownEvent(
            id = "event_solar_eclipse_2026",
            title = "Total Solar Eclipse (Greenland / Spain)",
            description = "Major total solar eclipse crossing the Arctic, Iceland, and Northern Spain",
            targetZdt = ZonedDateTime.of(2026, 8, 12, 17, 47, 0, 0, ZoneOffset.UTC),
            category = "ASTRONOMICAL",
            emoji = "🌑",
            accentColorHex = 0xFF7C3AED // Electric Violet
        ),
        CountdownEvent(
            id = "event_autumn_equinox_2026",
            title = "Autumnal Equinox 2026",
            description = "Sun crosses celestial equator southward; equal day & night across Earth",
            targetZdt = ZonedDateTime.of(2026, 9, 22, 12, 45, 0, 0, ZoneOffset.UTC),
            category = "ASTRONOMICAL",
            emoji = "🍂",
            accentColorHex = 0xFFEA580C // Orange
        ),
        CountdownEvent(
            id = "event_starship_launch_2026",
            title = "SpaceX Starship Mars Window (Starbase TX)",
            description = "Interplanetary launch window alignment for deep space mission",
            targetZdt = ZonedDateTime.of(2026, 11, 15, 14, 0, 0, 0, ZoneOffset.UTC),
            category = "SPACE_LAUNCH",
            emoji = "🚀",
            accentColorHex = 0xFF22C55E // Green
        ),
        CountdownEvent(
            id = "event_silicon_valley_keynote",
            title = "Silicon Valley Developer Keynote (Cupertino CA)",
            description = "Next-generation software & hardware ecosystem announcements",
            targetZdt = ZonedDateTime.of(2026, 9, 8, 17, 0, 0, 0, ZoneId.of("America/Los_Angeles")),
            category = "TECH_KEYNOTE",
            emoji = "💻",
            accentColorHex = 0xFF0284C7 // Cobalt
        )
    )

    /**
     * Calculates the millisecond-precision time delta between [nowZdt] and [targetZdt].
     */
    fun calculateCountdown(
        targetZdt: ZonedDateTime,
        nowZdt: ZonedDateTime = ZonedDateTime.now()
    ): CountdownTimeRemaining {
        val targetMillis = targetZdt.toInstant().toEpochMilli()
        val nowMillis = nowZdt.toInstant().toEpochMilli()

        val rawDiff = targetMillis - nowMillis
        val isPast = rawDiff <= 0L
        val absDiff = kotlin.math.abs(rawDiff)

        val days = absDiff / 86400000L
        val hours = (absDiff % 86400000L) / 3600000L
        val minutes = (absDiff % 3600000L) / 60000L
        val seconds = (absDiff % 60000L) / 1000L
        val millis = absDiff % 1000L

        val prefix = if (isPast) "T + " else "T - "
        val formatted = String.format(
            Locale.US,
            "%s%03dd : %02dh : %02dm : %02ds . %03d",
            prefix, days, hours, minutes, seconds, millis
        )

        val subSecFraction = (millis / 1000.0).toFloat()

        return CountdownTimeRemaining(
            totalMillisRemaining = rawDiff,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            millis = millis,
            isPastEvent = isPast,
            tMinusFormatted = formatted,
            subSecondProgress = subSecFraction
        )
    }

    /**
     * Creates a custom user event with target [date], [time], and [zoneId].
     */
    fun createCustomEvent(
        title: String,
        description: String,
        date: LocalDate,
        time: LocalTime,
        zoneId: ZoneId
    ): CountdownEvent {
        val zdt = ZonedDateTime.of(date, time, zoneId)
        val id = "event_custom_${System.currentTimeMillis()}"
        return CountdownEvent(
            id = id,
            title = title.trim().ifEmpty { "Milestone Event" },
            description = description.trim().ifEmpty { "Personal custom target date" },
            targetZdt = zdt,
            category = "PERSONAL",
            emoji = "⭐",
            accentColorHex = 0xFFF59E0B
        )
    }
}
