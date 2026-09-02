package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.PI

enum class LunarPhaseType(val displayName: String, val emoji: String) {
    NEW_MOON("New Moon", "🌑"),
    WAXING_CRESCENT("Waxing Crescent", "🌒"),
    FIRST_QUARTER("First Quarter", "🌓"),
    WAXING_GIBBOUS("Waxing Gibbous", "🌔"),
    FULL_MOON("Full Moon", "🌕"),
    WANING_GIBBOUS("Waning Gibbous", "🌖"),
    LAST_QUARTER("Last Quarter", "🌗"),
    WANING_CRESCENT("Waning Crescent", "🌘")
}

data class MoonPhaseDetails(
    val phaseType: LunarPhaseType,
    val illuminationPercentage: Double, // 0.0 to 100.0
    val ageDays: Double, // 0.0 to 29.53
    val isWaxing: Boolean,
    val nextFullMoonDate: LocalDate,
    val daysUntilFullMoon: Int,
    val nextNewMoonDate: LocalDate,
    val daysUntilNewMoon: Int,
    val stargazingRating: String,
    val stargazingScore: Int, // 1 to 10
    val stargazingRecommendation: String,
    val optimalDarkSkyWindow: String
)

data class DayMoonSummary(
    val date: LocalDate,
    val phaseType: LunarPhaseType,
    val illuminationPercentage: Int,
    val isMajorPhase: Boolean
)

data class MeteorShowerEvent(
    val name: String,
    val peakDate: String,
    val zhrRate: String,
    val constellation: String,
    val viewingAdvice: String
)

object LunarCelestialEngine {

    const val SYNODIC_MONTH_DAYS = 29.53058867
    private const val REFERENCE_NEW_MOON_EPOCH_MILLIS = 947182440000L // 2000-01-06T18:14:00Z

    val MAJOR_METEOR_SHOWERS = listOf(
        MeteorShowerEvent("Perseids", "August 12-13", "100 meteors/hr", "Perseus", "Best after midnight, fast bright meteors with glowing trails"),
        MeteorShowerEvent("Geminids", "December 13-14", "120 meteors/hr", "Gemini", "King of meteor showers, bright multi-colored fireballs"),
        MeteorShowerEvent("Orionids", "October 21-22", "20 meteors/hr", "Orion", "Debris from Halley's Comet, fast streaks with fine trains"),
        MeteorShowerEvent("Quadrantids", "January 3-4", "110 meteors/hr", "Boötes", "Short intense peak window, bright fireball potential"),
        MeteorShowerEvent("Lyrids", "April 22-23", "18 meteors/hr", "Lyra", "Spring meteor display, occasional glowing dust trains")
    )

    /**
     * Computes full lunar phase details for a given [dateTime].
     */
    fun calculateMoonPhase(dateTime: ZonedDateTime): MoonPhaseDetails {
        val epochMillis = dateTime.toInstant().toEpochMilli()
        val daysSinceEpoch = (epochMillis - REFERENCE_NEW_MOON_EPOCH_MILLIS) / 86400000.0

        val cyclePosDays = ((daysSinceEpoch % SYNODIC_MONTH_DAYS) + SYNODIC_MONTH_DAYS) % SYNODIC_MONTH_DAYS
        val phaseFrac = cyclePosDays / SYNODIC_MONTH_DAYS // 0.0 to 1.0

        // Illumination: 0% at New Moon (0.0), 100% at Full Moon (0.50)
        val illuminationPercent = ((1.0 - cos(phaseFrac * 2 * PI)) / 2.0) * 100.0
        val isWaxing = phaseFrac < 0.50

        val phaseType = when {
            phaseFrac < 0.03 || phaseFrac >= 0.97 -> LunarPhaseType.NEW_MOON
            phaseFrac in 0.03..0.22 -> LunarPhaseType.WAXING_CRESCENT
            phaseFrac in 0.22..0.28 -> LunarPhaseType.FIRST_QUARTER
            phaseFrac in 0.28..0.47 -> LunarPhaseType.WAXING_GIBBOUS
            phaseFrac in 0.47..0.53 -> LunarPhaseType.FULL_MOON
            phaseFrac in 0.53..0.72 -> LunarPhaseType.WANING_GIBBOUS
            phaseFrac in 0.72..0.78 -> LunarPhaseType.LAST_QUARTER
            else -> LunarPhaseType.WANING_CRESCENT
        }

        // Calculate days to Next Full Moon (phaseFrac = 0.50)
        val daysToFull = if (phaseFrac <= 0.50) {
            ((0.50 - phaseFrac) * SYNODIC_MONTH_DAYS).toInt().coerceAtLeast(0)
        } else {
            ((1.50 - phaseFrac) * SYNODIC_MONTH_DAYS).toInt().coerceAtLeast(1)
        }
        val nextFullMoonDate = dateTime.toLocalDate().plusDays(daysToFull.toLong())

        // Calculate days to Next New Moon (phaseFrac = 1.0 / 0.0)
        val daysToNew = ((1.0 - phaseFrac) * SYNODIC_MONTH_DAYS).toInt().coerceAtLeast(0)
        val nextNewMoonDate = dateTime.toLocalDate().plusDays(daysToNew.toLong())

        // Stargazing rating based on illumination
        val (rating, score, recommendation, darkWindow) = when {
            illuminationPercent < 15.0 -> Quadruple(
                "Pristine Dark Sky",
                10,
                "Outstanding for deep-sky astrophotography, Milky Way core & faint nebulae.",
                "All night (Full astronomical darkness)"
            )
            illuminationPercent in 15.0..35.0 -> Quadruple(
                "Great Stargazing",
                8,
                "Excellent viewing of constellations, planets & open star clusters.",
                "Late night / early morning before moonrise"
            )
            illuminationPercent in 35.0..70.0 -> Quadruple(
                "Moderate Moonlight",
                5,
                "Good for Lunar surface crater observation, bright planets & double stars.",
                "Window after moonset or before moonrise"
            )
            else -> Quadruple(
                "Bright Moonlit Sky",
                2,
                "Moonlight washes out faint deep-sky targets. Focus on lunar geology & bright planets.",
                "Limited dark window (High lunar glare)"
            )
        }

        return MoonPhaseDetails(
            phaseType = phaseType,
            illuminationPercentage = illuminationPercent.coerceIn(0.0, 100.0),
            ageDays = cyclePosDays,
            isWaxing = isWaxing,
            nextFullMoonDate = nextFullMoonDate,
            daysUntilFullMoon = daysToFull,
            nextNewMoonDate = nextNewMoonDate,
            daysUntilNewMoon = daysToNew,
            stargazingRating = rating,
            stargazingScore = score,
            stargazingRecommendation = recommendation,
            optimalDarkSkyWindow = darkWindow
        )
    }

    /**
     * Generates a multi-day lunar forecast (e.g. 7-day or 14-day).
     */
    fun generateMultiDayForecast(
        startDate: LocalDate = LocalDate.now(),
        daysCount: Int = 7,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<DayMoonSummary> {
        val list = mutableListOf<DayMoonSummary>()
        for (i in 0 until daysCount) {
            val date = startDate.plusDays(i.toLong())
            val zdt = date.atTime(21, 0).atZone(zoneId) // Assess at night (9 PM)
            val details = calculateMoonPhase(zdt)
            val isMajor = details.phaseType == LunarPhaseType.NEW_MOON ||
                    details.phaseType == LunarPhaseType.FIRST_QUARTER ||
                    details.phaseType == LunarPhaseType.FULL_MOON ||
                    details.phaseType == LunarPhaseType.LAST_QUARTER
            list.add(
                DayMoonSummary(
                    date = date,
                    phaseType = details.phaseType,
                    illuminationPercentage = details.illuminationPercentage.toInt(),
                    isMajorPhase = isMajor
                )
            )
        }
        return list
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
