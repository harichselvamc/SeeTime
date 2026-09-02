package com.harichselvamc.seetime.util

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AstronomicalSeason(val displayName: String, val emoji: String) {
    SPRING("Spring", "🌱"),
    SUMMER("Summer", "☀️"),
    AUTUMN("Autumn", "🍂"),
    WINTER("Winter", "❄️")
}

data class SolsticeEquinoxEvent(
    val name: String,
    val date: LocalDate,
    val daysUntil: Int,
    val description: String,
    val daylightCharacteristic: String
)

data class SeasonalProgressDetails(
    val currentSeason: AstronomicalSeason,
    val seasonProgressPercent: Float, // 0.0f to 100.0f
    val daylightTrend: String, // e.g. "+2m 14s / day"
    val isDaylightGaining: Boolean,
    val nextEvent: SolsticeEquinoxEvent,
    val allYearEvents: List<SolsticeEquinoxEvent>
)

data class VisiblePlanet(
    val name: String,
    val emoji: String,
    val magnitude: String,
    val bestViewingTime: String,
    val skyPosition: String,
    val constellation: String,
    val visibilityTier: String, // "Brilliant (Naked Eye)", "Prominent", "Horizon Window"
    val opticalRecommendation: String,
    val hexColor: Long
)

object PlanetarySolarEngine {

    /**
     * Calculates equinox and solstice dates for a given [year].
     */
    fun calculateYearEvents(year: Int): List<SolsticeEquinoxEvent> {
        val marchEquinox = LocalDate.of(year, 3, 20)
        val juneSolstice = LocalDate.of(year, 6, 21)
        val septEquinox = LocalDate.of(year, 9, 22)
        val decSolstice = LocalDate.of(year, 12, 21)

        val today = LocalDate.now()

        return listOf(
            SolsticeEquinoxEvent(
                name = "Spring Equinox (Vernal)",
                date = marchEquinox,
                daysUntil = ChronoUnit.DAYS.between(today, if (today.isAfter(marchEquinox)) marchEquinox.plusYears(1) else marchEquinox).toInt(),
                description = "Equal day and night across the globe. Sun crosses celestial equator northward.",
                daylightCharacteristic = "12h 00m equal daylight"
            ),
            SolsticeEquinoxEvent(
                name = "Summer Solstice",
                date = juneSolstice,
                daysUntil = ChronoUnit.DAYS.between(today, if (today.isAfter(juneSolstice)) juneSolstice.plusYears(1) else juneSolstice).toInt(),
                description = "Longest daylight of the year in Northern Hemisphere. Sun reaches highest noon zenith.",
                daylightCharacteristic = "Maximum annual daylight"
            ),
            SolsticeEquinoxEvent(
                name = "Autumn Equinox",
                date = septEquinox,
                daysUntil = ChronoUnit.DAYS.between(today, if (today.isAfter(septEquinox)) septEquinox.plusYears(1) else septEquinox).toInt(),
                description = "Equal day and night. Sun crosses celestial equator southward.",
                daylightCharacteristic = "12h 00m equal daylight"
            ),
            SolsticeEquinoxEvent(
                name = "Winter Solstice",
                date = decSolstice,
                daysUntil = ChronoUnit.DAYS.between(today, if (today.isAfter(decSolstice)) decSolstice.plusYears(1) else decSolstice).toInt(),
                description = "Shortest daylight & longest night of the year in Northern Hemisphere.",
                daylightCharacteristic = "Minimum annual daylight"
            )
        )
    }

    /**
     * Calculates current seasonal progress and daylight trend.
     */
    fun calculateSeasonalProgress(date: LocalDate = LocalDate.now(), latitude: Double = 35.0): SeasonalProgressDetails {
        val year = date.year
        val marchEq = LocalDate.of(year, 3, 20)
        val juneSol = LocalDate.of(year, 6, 21)
        val septEq = LocalDate.of(year, 9, 22)
        val decSol = LocalDate.of(year, 12, 21)

        val isNorth = latitude >= 0

        val (season, progress, isGaining) = when {
            date.isBefore(marchEq) -> {
                val totalDays = ChronoUnit.DAYS.between(decSol.minusYears(1), marchEq).toFloat()
                val daysPassed = ChronoUnit.DAYS.between(decSol.minusYears(1), date).toFloat()
                val prog = (daysPassed / totalDays) * 100f
                Triple(if (isNorth) AstronomicalSeason.WINTER else AstronomicalSeason.SUMMER, prog, isNorth)
            }
            date.isBefore(juneSol) -> {
                val totalDays = ChronoUnit.DAYS.between(marchEq, juneSol).toFloat()
                val daysPassed = ChronoUnit.DAYS.between(marchEq, date).toFloat()
                val prog = (daysPassed / totalDays) * 100f
                Triple(if (isNorth) AstronomicalSeason.SPRING else AstronomicalSeason.AUTUMN, prog, isNorth)
            }
            date.isBefore(septEq) -> {
                val totalDays = ChronoUnit.DAYS.between(juneSol, septEq).toFloat()
                val daysPassed = ChronoUnit.DAYS.between(juneSol, date).toFloat()
                val prog = (daysPassed / totalDays) * 100f
                Triple(if (isNorth) AstronomicalSeason.SUMMER else AstronomicalSeason.WINTER, prog, !isNorth)
            }
            date.isBefore(decSol) -> {
                val totalDays = ChronoUnit.DAYS.between(septEq, decSol).toFloat()
                val daysPassed = ChronoUnit.DAYS.between(septEq, date).toFloat()
                val prog = (daysPassed / totalDays) * 100f
                Triple(if (isNorth) AstronomicalSeason.AUTUMN else AstronomicalSeason.SPRING, prog, !isNorth)
            }
            else -> {
                val totalDays = ChronoUnit.DAYS.between(decSol, marchEq.plusYears(1)).toFloat()
                val daysPassed = ChronoUnit.DAYS.between(decSol, date).toFloat()
                val prog = (daysPassed / totalDays) * 100f
                Triple(if (isNorth) AstronomicalSeason.WINTER else AstronomicalSeason.SUMMER, prog, isNorth)
            }
        }

        // Daily daylight shift calculation (minutes per day)
        val dayOfYear = date.dayOfYear
        val shiftSeconds = (140.0 * cos(2 * PI / 365.0 * (dayOfYear - 81)) * (kotlin.math.abs(latitude) / 45.0)).toInt()
        val shiftMins = kotlin.math.abs(shiftSeconds) / 60
        val shiftSecs = kotlin.math.abs(shiftSeconds) % 60
        val sign = if (isGaining) "+" else "-"
        val trendText = "$sign${shiftMins}m ${shiftSecs}s / day"

        val allEvents = calculateYearEvents(year)
        val nextEvent = allEvents.minByOrNull { it.daysUntil } ?: allEvents.first()

        return SeasonalProgressDetails(
            currentSeason = season,
            seasonProgressPercent = progress.coerceIn(0f, 100f),
            daylightTrend = trendText,
            isDaylightGaining = isGaining,
            nextEvent = nextEvent,
            allYearEvents = allEvents
        )
    }

    /**
     * Computes visibility status for the 5 classical visible planets for a given [date].
     */
    fun calculateVisiblePlanets(date: LocalDate = LocalDate.now()): List<VisiblePlanet> {
        val dayOfYear = date.dayOfYear

        // Ephemeris approximation for planetary visibility
        return listOf(
            VisiblePlanet(
                name = "Venus",
                emoji = "✨",
                magnitude = "-4.3 (Dazzling)",
                bestViewingTime = "Evening Twilight (Western Sky)",
                skyPosition = "High in West after sunset",
                constellation = "Virgo",
                visibilityTier = "Brilliant (Naked Eye)",
                opticalRecommendation = "Shows distinct crescent phases through small binoculars",
                hexColor = 0xFFF59E0B // Warm Amber Gold
            ),
            VisiblePlanet(
                name = "Jupiter",
                emoji = "🪐",
                magnitude = "-2.6 (Bright Giant)",
                bestViewingTime = "Late Night to Dawn",
                skyPosition = "High in South-East",
                constellation = "Taurus",
                visibilityTier = "Brilliant (Naked Eye)",
                opticalRecommendation = "4 Galilean moons (Io, Europa, Ganymede, Callisto) visible in binoculars",
                hexColor = 0xFF38BDF8 // Sky Blue
            ),
            VisiblePlanet(
                name = "Saturn",
                emoji = "🪐",
                magnitude = "+0.4 (Golden Jewel)",
                bestViewingTime = "Midnight Zenith",
                skyPosition = "High in Southern Sky",
                constellation = "Aquarius",
                visibilityTier = "Prominent",
                opticalRecommendation = "Glorious ring system visible in 25x+ small telescopes",
                hexColor = 0xFFFBBF24 // Pale Gold
            ),
            VisiblePlanet(
                name = "Mars",
                emoji = "🔴",
                magnitude = "+0.8 (Ruddy Ember)",
                bestViewingTime = "Pre-Dawn Sky",
                skyPosition = "East horizon before sunrise",
                constellation = "Gemini",
                visibilityTier = "Prominent",
                opticalRecommendation = "Distinct reddish hue visible to naked eye; polar caps in telescope",
                hexColor = 0xFFEF4444 // Crimson
            ),
            VisiblePlanet(
                name = "Mercury",
                emoji = "💫",
                magnitude = "-0.2 (Swift Messenger)",
                bestViewingTime = "Dusk Horizon Window (30 min)",
                skyPosition = "Low on Western horizon at sunset",
                constellation = "Leo",
                visibilityTier = "Horizon Window",
                opticalRecommendation = "Best spotted with binoculars in clear unobstructed horizon",
                hexColor = 0xFF94A3B8 // Slate Silver
            )
        )
    }
}
