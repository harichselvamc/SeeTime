package com.harichselvamc.seetime.util

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class BiologicalChronotype(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val populationPercentage: String,
    val idealWakeTime: String,
    val idealBedTime: String,
    val summary: String,
    val themeColorHex: Long
) {
    LION(
        title = "Lion",
        emoji = "🦁",
        subtitle = "Early Morning Hunter",
        populationPercentage = "~15-20% of population",
        idealWakeTime = "05:30 AM",
        idealBedTime = "09:30 PM",
        summary = "Early riser with immense morning willpower, sharp analytical focus before noon, and early evening wind-down.",
        themeColorHex = 0xFFF59E0B // Warm Amber Gold
    ),
    BEAR(
        title = "Bear",
        emoji = "🐻",
        subtitle = "Solar Rhythm Synchronized",
        populationPercentage = "~50-55% of population",
        idealWakeTime = "07:00 AM",
        idealBedTime = "11:00 PM",
        summary = "Rises and sets with the sun. Peak cognitive energy in mid-morning (10 AM - 2 PM), post-lunch dip, and steady evening stamina.",
        themeColorHex = 0xFF0284C7 // Sky Cobalt
    ),
    WOLF(
        title = "Wolf",
        emoji = "🐺",
        subtitle = "Night Owl & Late Creative",
        populationPercentage = "~15-20% of population",
        idealWakeTime = "08:30 AM",
        idealBedTime = "00:30 AM",
        summary = "Groggy mornings, sharp afternoon acceleration, and explosive creative burst during evening hours (5 PM - 9 PM).",
        themeColorHex = 0xFF7C3AED // Electric Violet
    ),
    DOLPHIN(
        title = "Dolphin",
        emoji = "🐬",
        subtitle = "Light Sleeper & Strategist",
        populationPercentage = "~10% of population",
        idealWakeTime = "06:30 AM",
        idealBedTime = "11:30 PM",
        summary = "Intelligent, highly analytical light sleeper with periodic bursts of intense focus in late afternoon.",
        themeColorHex = 0xFF059669 // Emerald Green
    )
}

data class ChronoScheduleBlock(
    val title: String,
    val timeSpan: String,
    val startHour: Int,
    val endHour: Int,
    val emoji: String,
    val recommendation: String,
    val isPeakFocus: Boolean
)

data class ChronoQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<Pair<String, Int>> // Label to score (1..5)
)

object ChronotypeOptimizerEngine {

    val CHRONO_QUIZ_QUESTIONS = listOf(
        ChronoQuizQuestion(
            id = 1,
            question = "If you had complete freedom with no alarms, what time would you naturally wake up?",
            options = listOf(
                Pair("Before 06:00 AM (Energized)", 5),
                Pair("06:30 AM - 07:30 AM (Refreshed)", 4),
                Pair("07:30 AM - 08:30 AM (Normal)", 3),
                Pair("08:30 AM - 10:00 AM (Need time to wake)", 2),
                Pair("Irregular / Wake up multiple times", 1)
            )
        ),
        ChronoQuizQuestion(
            id = 2,
            question = "When do you feel your sharpest mental focus for difficult deep work?",
            options = listOf(
                Pair("Early Morning (07:00 - 11:00 AM)", 5),
                Pair("Midday / Late Morning (10:00 AM - 02:00 PM)", 4),
                Pair("Late Afternoon (03:00 - 06:00 PM)", 3),
                Pair("Evening / Night (06:00 - 10:00 PM)", 2),
                Pair("Short unpredictable bursts throughout the day", 1)
            )
        ),
        ChronoQuizQuestion(
            id = 3,
            question = "How do you feel in the first 30 minutes after waking up?",
            options = listOf(
                Pair("Wide awake and ready to conquer the day immediately", 5),
                Pair("Awake after a light stretch or glass of water", 4),
                Pair("Somewhat foggy, need 30-45 minutes to get going", 3),
                Pair("Exhausted, desperately need caffeine to function", 2),
                Pair("Tired and lightheaded, sleep feels unrefreshing", 1)
            )
        ),
        ChronoQuizQuestion(
            id = 4,
            question = "When is your preferred time to exercise or do intense physical workouts?",
            options = listOf(
                Pair("Early morning before breakfast (06:00 - 08:00 AM)", 5),
                Pair("Lunchtime or early afternoon (12:00 - 02:00 PM)", 4),
                Pair("Late afternoon (04:00 - 06:00 PM)", 3),
                Pair("Evening (07:00 - 09:00 PM)", 2),
                Pair("Inconsistent / Hard to find physical energy", 1)
            )
        ),
        ChronoQuizQuestion(
            id = 5,
            question = "What time do you naturally begin feeling sleepy in the evening?",
            options = listOf(
                Pair("Around 09:00 - 09:30 PM", 5),
                Pair("Around 10:30 - 11:00 PM", 4),
                Pair("Around 11:30 PM - 00:30 AM", 3),
                Pair("After 01:00 AM (True night owl)", 2),
                Pair("Tired all day, but brain races at bedtime", 1)
            )
        )
    )

    /**
     * Evaluates total quiz score (5..25) to diagnose the corresponding biological chronotype.
     */
    fun evaluateQuizScore(totalScore: Int): BiologicalChronotype {
        return when {
            totalScore >= 21 -> BiologicalChronotype.LION
            totalScore in 15..20 -> BiologicalChronotype.BEAR
            totalScore in 9..14 -> BiologicalChronotype.WOLF
            else -> BiologicalChronotype.DOLPHIN
        }
    }

    /**
     * Generates a personalized daily routine schedule for [chronotype].
     */
    fun getDailyRoutineSchedule(chronotype: BiologicalChronotype): List<ChronoScheduleBlock> {
        return when (chronotype) {
            BiologicalChronotype.LION -> listOf(
                ChronoScheduleBlock("Natural Awakening & Hydration", "05:30 - 06:30", 5, 6, "🌅", "Hydrate with water and get natural sunlight immediately to reinforce circadian clock.", false),
                ChronoScheduleBlock("Peak Mental Focus & Strategy", "08:00 - 12:00", 8, 12, "🎯", "Uninterrupted analytical deep work, critical decision making, and complex coding.", true),
                ChronoScheduleBlock("Optimal Caffeine Window", "07:00 - 08:00", 7, 8, "☕", "Consume coffee 90 min after waking as natural cortisol surge plateaus.", false),
                ChronoScheduleBlock("Collaborative Meetings & Admin", "12:00 - 14:30", 12, 14, "👥", "Team syncs, emails, and conversational work as analytical focus stabilizes.", false),
                ChronoScheduleBlock("Physical Workout Peak", "15:00 - 17:00", 15, 17, "💪", "Optimal body temperature and strength for gym, running, or sports.", false),
                ChronoScheduleBlock("Caffeine Curfew", "12:30", 12, 13, "🛑", "Stop all caffeine to protect deep restorative delta sleep.", false),
                ChronoScheduleBlock("Melatonin Wind-Down & Reading", "20:00 - 21:30", 20, 21, "📖", "Dim screens, read fiction, prepare bedroom for sleep gate at 21:30.", false)
            )
            BiologicalChronotype.BEAR -> listOf(
                ChronoScheduleBlock("Solar Awakening & Morning Walk", "07:00 - 08:00", 7, 8, "☀️", "Wake with the sun, get 10-15 minutes of direct morning outdoor light.", false),
                ChronoScheduleBlock("Optimal First Coffee", "09:30 - 10:00", 9, 10, "☕", "Allow morning cortisol to peak naturally before having first caffeine.", false),
                ChronoScheduleBlock("Peak Cognitive Deep Work", "10:00 - 14:00", 10, 14, "🚀", "Maximum brainpower window for difficult analytical tasks and creative problem solving.", true),
                ChronoScheduleBlock("Post-Lunch Slump & Light Admin", "14:00 - 16:00", 14, 16, "🔋", "Natural biological dip. Ideal for a 20m power nap, light emails, or walking meeting.", false),
                ChronoScheduleBlock("Second Wind & Workout Slot", "16:30 - 18:30", 16, 18, "🏋️", "Peak physical reaction time, strength, and cardiovascular efficiency.", false),
                ChronoScheduleBlock("Caffeine Curfew", "14:00", 14, 15, "🛑", "No coffee after 2 PM to preserve evening melatonin synthesis.", false),
                ChronoScheduleBlock("Digital Sunset & Sleep Gate", "21:30 - 23:00", 21, 23, "🌙", "Dim overhead lights, activate night shift screen warmers, sleep by 23:00.", false)
            )
            BiologicalChronotype.WOLF -> listOf(
                ChronoScheduleBlock("Gradual Awakening & Bright Light", "08:30 - 09:30", 8, 9, "⏰", "Do not rush. Drink cold water and expose eyes to bright light to shut off melatonin.", false),
                ChronoScheduleBlock("Midday Ramp-Up & Planning", "11:00 - 13:00", 11, 13, "📋", "Organize tasks, review code, handle low-friction correspondence.", false),
                ChronoScheduleBlock("Optimal Coffee Window", "12:00 - 13:00", 12, 13, "☕", "First caffeine peak as natural grogginess clears.", false),
                ChronoScheduleBlock("Afternoon Focus Session", "13:30 - 16:30", 13, 16, "💻", "Steady productive workflow and team meetings.", false),
                ChronoScheduleBlock("Peak Creative Burst (Golden Zone)", "17:00 - 21:00", 17, 21, "⚡", "Maximum divergent thinking, architecture design, writing, and flow state.", true),
                ChronoScheduleBlock("Late Afternoon Workout", "18:00 - 19:30", 18, 19, "🏃", "Physical stamina peak for high-intensity training.", false),
                ChronoScheduleBlock("Caffeine Curfew", "15:30", 15, 16, "🛑", "Curfew 9 hours before planned 00:30 sleep.", false),
                ChronoScheduleBlock("Creative Wind-Down & Bedtime", "23:00 - 00:30", 23, 0, "🌌", "Calm creative hobbies, darkness, sleep gate at 00:30.", false)
            )
            BiologicalChronotype.DOLPHIN -> listOf(
                ChronoScheduleBlock("Morning Awakening & Light Jog", "06:30 - 07:30", 6, 7, "🏃", "Morning cardio or brisk walk to stimulate serotonin and core body temperature.", false),
                ChronoScheduleBlock("Morning Strategy Sprint", "08:30 - 11:00", 8, 11, "🧠", "Sharp early morning focus window for tactical planning.", false),
                ChronoScheduleBlock("Optimal Coffee Window", "09:30 - 10:30", 9, 10, "☕", "Caffeine boost before midday slump.", false),
                ChronoScheduleBlock("Restorative Quiet Time", "13:00 - 14:30", 13, 14, "🧘", "Meditation, yoga, or quiet rest (avoid long naps which disrupt night sleep).", false),
                ChronoScheduleBlock("Deep Work Flow State", "15:30 - 19:00", 15, 19, "🔥", "Primary creative and analytical focus powerhouse window.", true),
                ChronoScheduleBlock("Caffeine Curfew", "13:00", 13, 14, "🛑", "Strict early curfew for sensitive dolphin sleep architecture.", false),
                ChronoScheduleBlock("Warm Bath & Blue Light Shield", "21:30 - 23:30", 21, 23, "🛁", "Warm bath to trigger body cooling, read physical book, sleep by 23:30.", false)
            )
        }
    }

    /**
     * Generates 24-point hourly circadian energy curve (0..100) for [chronotype].
     */
    fun generate24HourEnergyCurve(chronotype: BiologicalChronotype): List<Float> {
        return when (chronotype) {
            BiologicalChronotype.LION -> listOf(
                10f, 10f, 10f, 15f, 30f, 65f, 85f, 92f, 98f, 95f, 90f, 80f,
                70f, 60f, 50f, 65f, 75f, 60f, 45f, 30f, 20f, 12f, 10f, 10f
            )
            BiologicalChronotype.BEAR -> listOf(
                10f, 10f, 10f, 10f, 15f, 30f, 60f, 75f, 85f, 95f, 98f, 95f,
                88f, 70f, 50f, 55f, 75f, 85f, 75f, 60f, 40f, 25f, 15f, 10f
            )
            BiologicalChronotype.WOLF -> listOf(
                15f, 10f, 10f, 10f, 10f, 12f, 20f, 35f, 50f, 65f, 75f, 80f,
                82f, 85f, 80f, 82f, 90f, 98f, 98f, 92f, 80f, 60f, 40f, 25f
            )
            BiologicalChronotype.DOLPHIN -> listOf(
                10f, 10f, 10f, 10f, 15f, 35f, 65f, 80f, 85f, 75f, 65f, 55f,
                45f, 40f, 55f, 75f, 90f, 92f, 85f, 70f, 50f, 30f, 18f, 12f
            )
        }
    }
}
