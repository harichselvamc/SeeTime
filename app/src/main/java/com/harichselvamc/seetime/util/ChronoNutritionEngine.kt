package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class MealWindow(
    val name: String,
    val recommendedTime: LocalTime,
    val nutritionalFocus: String,
    val circadianBenefit: String
)

data class HydrationSlot(
    val time: LocalTime,
    val targetVolumeMl: Int,
    val milestoneName: String,
    val isElectrolyteBoost: Boolean = false
)

data class JetLagChronoPlan(
    val dayNumber: Int,
    val dayTitle: String,
    val originZoneId: String,
    val destZoneId: String,
    val offsetHoursDiff: Int,
    val isEastward: Boolean,
    val meals: List<MealWindow>,
    val fastingWindow: String,
    val hydrationSlots: List<HydrationSlot>,
    val totalDailyWaterMl: Int,
    val caffeineCutoff: LocalTime,
    val melatoninWindow: String,
    val lightExposureWindow: String,
    val lightAvoidanceWindow: String,
    val targetBedtime: LocalTime,
    val targetWakeTime: LocalTime,
    val chronoTip: String
)

object ChronoNutritionEngine {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a")

    /**
     * Calculates the time difference in whole hours between two timezone IDs.
     */
    fun calculateZoneOffsetDiffHours(originZone: String, destZone: String, instant: Instant = Instant.now()): Int {
        val originOffsetSecs = try {
            ZoneId.of(originZone).rules.getOffset(instant).totalSeconds
        } catch (_: Exception) { 0 }

        val destOffsetSecs = try {
            ZoneId.of(destZone).rules.getOffset(instant).totalSeconds
        } catch (_: Exception) { 0 }

        return (destOffsetSecs - originOffsetSecs) / 3600
    }

    /**
     * Generates a 5-day circadian chrono-nutrition and hydration recovery plan.
     * Day 1: 2 Days Before Flight (Initial Sync)
     * Day 2: 1 Day Before Flight (Fasting Priming)
     * Day 3: Travel / Flight Day (In-flight Circadian Gate)
     * Day 4: Destination Arrival Day 1 (Anchor Breakfast)
     * Day 5: Destination Arrival Day 2 (Full Synchronization)
     */
    fun generateRecoveryPlan(
        originZone: String,
        destZone: String,
        baseBreakfast: LocalTime = LocalTime.of(8, 0),
        baseLunch: LocalTime = LocalTime.of(13, 0),
        baseDinner: LocalTime = LocalTime.of(19, 30),
        baseBedtime: LocalTime = LocalTime.of(23, 0),
        baseWakeTime: LocalTime = LocalTime.of(7, 0)
    ): List<JetLagChronoPlan> {
        val diffHours = calculateZoneOffsetDiffHours(originZone, destZone)
        val isEastward = diffHours > 0
        val totalShiftHours = abs(diffHours).coerceAtMost(12)

        val plans = mutableListOf<JetLagChronoPlan>()

        val dayTitles = listOf(
            "Pre-Flight 48h (Initial Shift)",
            "Pre-Flight 24h (Metabolic Fast)",
            "Flight Day (Chrono-Gate)",
            "Arrival Day 1 (Anchor Breakfast)",
            "Arrival Day 2 (Circadian Lock)"
        )

        for (dayIndex in 0 until 5) {
            val progressFraction = (dayIndex.toDouble() / 4.0) // 0.0 to 1.0
            val currentShiftMinutes = (totalShiftHours * 60 * progressFraction).toInt()

            val shiftedBreakfast = if (isEastward) baseBreakfast.minusMinutes(currentShiftMinutes.toLong()) else baseBreakfast.plusMinutes(currentShiftMinutes.toLong())
            val shiftedLunch = if (isEastward) baseLunch.minusMinutes(currentShiftMinutes.toLong()) else baseLunch.plusMinutes(currentShiftMinutes.toLong())
            val shiftedDinner = if (isEastward) baseDinner.minusMinutes(currentShiftMinutes.toLong()) else baseDinner.plusMinutes(currentShiftMinutes.toLong())
            val shiftedBedtime = if (isEastward) baseBedtime.minusMinutes(currentShiftMinutes.toLong()) else baseBedtime.plusMinutes(currentShiftMinutes.toLong())
            val shiftedWakeTime = if (isEastward) baseWakeTime.minusMinutes(currentShiftMinutes.toLong()) else baseWakeTime.plusMinutes(currentShiftMinutes.toLong())

            val caffeineCutoff = shiftedBedtime.minusHours(9)

            val meals = listOf(
                MealWindow(
                    name = "Anchor Breakfast",
                    recommendedTime = shiftedBreakfast,
                    nutritionalFocus = "High Protein (30g+), Low Glycemic Index",
                    circadianBenefit = "Triggers cortisol awakening surge & resets hepatic clock"
                ),
                MealWindow(
                    name = "Circadian Lunch",
                    recommendedTime = shiftedLunch,
                    nutritionalFocus = "Complex Carbs, Healthy Fats & Leafy Greens",
                    circadianBenefit = "Prevents mid-day glucose crash and stabilizes core body temp"
                ),
                MealWindow(
                    name = "Restorative Dinner",
                    recommendedTime = shiftedDinner,
                    nutritionalFocus = "Light meal with Tryptophan & Magnesium (e.g. Salmon/Quinoa)",
                    circadianBenefit = "Facilitates natural melatonin synthesis without digestive strain"
                )
            )

            val fastingWindow = "${shiftedDinner.format(TIME_FORMATTER)} until ${shiftedBreakfast.format(TIME_FORMATTER)} (12-14h Fast)"

            val hydrationSlots = listOf(
                HydrationSlot(shiftedWakeTime.plusMinutes(15), 400, "Morning Rehydration (with electrolytes)", isElectrolyteBoost = true),
                HydrationSlot(shiftedBreakfast.plusHours(2), 350, "Mid-Morning Cellular Hydration"),
                HydrationSlot(shiftedLunch.minusMinutes(30), 300, "Pre-Lunch Digestive Water"),
                HydrationSlot(shiftedLunch.plusHours(2), 350, "Afternoon Alertness Intake"),
                HydrationSlot(shiftedDinner.minusMinutes(30), 300, "Pre-Dinner Fluid"),
                HydrationSlot(shiftedBedtime.minusHours(2), 250, "Evening Light Hydration")
            )

            val totalDailyWaterMl = hydrationSlots.sumOf { it.targetVolumeMl } + if (dayIndex == 2) 800 else 0 // Extra 800ml on flight day

            val melatoninWindow = "${shiftedBedtime.minusMinutes(45).format(TIME_FORMATTER)} - ${shiftedBedtime.minusMinutes(15).format(TIME_FORMATTER)} (0.5mg - 3mg)"

            val lightExposure = if (isEastward) {
                "Bright Light / Sunlight: ${shiftedWakeTime.format(TIME_FORMATTER)} to ${shiftedWakeTime.plusHours(2).format(TIME_FORMATTER)}"
            } else {
                "Late Afternoon Sun: ${shiftedBedtime.minusHours(4).format(TIME_FORMATTER)} to ${shiftedBedtime.minusHours(2).format(TIME_FORMATTER)}"
            }

            val lightAvoidance = if (isEastward) {
                "Dim Lights / Blue Blockers: After ${shiftedBedtime.minusHours(2).format(TIME_FORMATTER)}"
            } else {
                "Wear Sunglasses: Before ${shiftedWakeTime.plusHours(2).format(TIME_FORMATTER)}"
            }

            val chronoTip = when (dayIndex) {
                0 -> "Start gently nudging your meal times by 30-45m toward your destination timezone."
                1 -> "Observe a 12-hour metabolic fast before destination breakfast to reset liver circadian clocks."
                2 -> "Drink 250ml water every flight hour. Fast on the plane if it is nighttime at your destination."
                3 -> "Eat a hearty protein breakfast upon arrival in destination morning light. Avoid daytime naps >20m."
                4 -> "Circadian entrainment complete! Maintain your new bedtime and hydration baseline."
                else -> "Stay aligned with destination natural solar cycles."
            }

            plans.add(
                JetLagChronoPlan(
                    dayNumber = dayIndex + 1,
                    dayTitle = dayTitles[dayIndex],
                    originZoneId = originZone,
                    destZoneId = destZone,
                    offsetHoursDiff = diffHours,
                    isEastward = isEastward,
                    meals = meals,
                    fastingWindow = fastingWindow,
                    hydrationSlots = hydrationSlots,
                    totalDailyWaterMl = totalDailyWaterMl,
                    caffeineCutoff = caffeineCutoff,
                    melatoninWindow = melatoninWindow,
                    lightExposureWindow = lightExposure,
                    lightAvoidanceWindow = lightAvoidance,
                    targetBedtime = shiftedBedtime,
                    targetWakeTime = shiftedWakeTime,
                    chronoTip = chronoTip
                )
            )
        }

        return plans
    }
}
