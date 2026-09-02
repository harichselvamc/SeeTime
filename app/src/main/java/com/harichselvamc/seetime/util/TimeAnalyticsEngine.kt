package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.Activity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class AnalyticsPeriod(val displayName: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class CategoryAnalytics(
    val categoryName: String,
    val durationMinutes: Long,
    val durationHours: Double,
    val percentage: Float,
    val colorHex: Long,
    val isProductive: Boolean,
    val sessionCount: Int
)

data class TimeAnalyticsReport(
    val period: AnalyticsPeriod,
    val totalTrackedMinutes: Long,
    val totalTrackedHours: Double,
    val categories: List<CategoryAnalytics>,
    val productivityScorePercent: Int,
    val peakProductiveHour: String,
    val dailyAverageHours: Double,
    val activitiesCount: Int
)

object TimeAnalyticsEngine {

    val CATEGORY_COLORS = mapOf(
        "Deep Work" to 0xFF3B82F6,      // Cobalt Blue
        "Meetings" to 0xFF6366F1,       // Indigo
        "Study & Research" to 0xFF06B6D4, // Cyan
        "Coding & Dev" to 0xFF10B981,   // Success Green
        "Exercise & Health" to 0xFFF59E0B,// Amber
        "Rest & Sleep" to 0xFF8B5CF6,   // Purple
        "Travel & Transit" to 0xFFEC4899,// Pink
        "Admin & Email" to 0xFF64748B,  // Slate
        "General" to 0xFF94A3B8         // Light Slate
    )

    val PRODUCTIVE_CATEGORIES = setOf(
        "Deep Work",
        "Study & Research",
        "Coding & Dev",
        "Exercise & Health",
        "Meetings",
        "Work",
        "Focus"
    )

    fun getCategoryColorHex(category: String): Long {
        return CATEGORY_COLORS[category]
            ?: CATEGORY_COLORS.entries.find { category.contains(it.key, ignoreCase = true) }?.value
            ?: 0xFF3B82F6
    }

    fun isCategoryProductive(category: String): Boolean {
        return PRODUCTIVE_CATEGORIES.any { category.contains(it, ignoreCase = true) }
    }

    /**
     * Generates a comprehensive time analytics report for [activities] filtered by [period].
     */
    fun generateReport(
        activities: List<Activity>,
        period: AnalyticsPeriod,
        referenceInstant: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): TimeAnalyticsReport {
        val effectiveActivities = if (activities.isEmpty()) createSampleActivities(referenceInstant) else activities

        val filtered = filterActivitiesByPeriod(effectiveActivities, period, referenceInstant, zoneId)

        var totalMinutes = 0L
        val categoryGroups = mutableMapOf<String, MutableList<Activity>>()

        for (act in filtered) {
            val durMin = ((act.endTimeMillis - act.startTimeMillis) / (1000 * 60)).coerceAtLeast(1L)
            totalMinutes += durMin

            val cat = if (act.category.isBlank() || act.category.equals("Uncategorized", ignoreCase = true)) {
                inferCategoryFromLabel(act.label)
            } else {
                act.category
            }
            categoryGroups.getOrPut(cat) { mutableListOf() }.add(act)
        }

        val totalHours = totalMinutes / 60.0

        val categoryAnalyticsList = categoryGroups.map { (catName, acts) ->
            val catMin = acts.sumOf { ((it.endTimeMillis - it.startTimeMillis) / (1000 * 60)).coerceAtLeast(1L) }
            val catHours = catMin / 60.0
            val percent = if (totalMinutes > 0) (catMin.toFloat() / totalMinutes) else 0f

            CategoryAnalytics(
                categoryName = catName,
                durationMinutes = catMin,
                durationHours = catHours,
                percentage = percent,
                colorHex = getCategoryColorHex(catName),
                isProductive = isCategoryProductive(catName),
                sessionCount = acts.size
            )
        }.sortedByDescending { it.durationMinutes }

        val productivityScore = calculateProductivityScore(categoryAnalyticsList)
        val peakHour = findPeakProductiveHour(filtered, zoneId)

        val daysInPeriod = when (period) {
            AnalyticsPeriod.TODAY -> 1.0
            AnalyticsPeriod.THIS_WEEK -> 7.0
            AnalyticsPeriod.THIS_MONTH -> 30.0
            AnalyticsPeriod.ALL_TIME -> maxOf(1.0, (ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(filtered.minOfOrNull { it.startTimeMillis } ?: referenceInstant.toEpochMilli()).atZone(zoneId).toLocalDate(),
                referenceInstant.atZone(zoneId).toLocalDate()
            ) + 1).toDouble())
        }
        val dailyAvg = totalHours / daysInPeriod

        return TimeAnalyticsReport(
            period = period,
            totalTrackedMinutes = totalMinutes,
            totalTrackedHours = totalHours,
            categories = categoryAnalyticsList,
            productivityScorePercent = productivityScore,
            peakProductiveHour = peakHour,
            dailyAverageHours = dailyAvg,
            activitiesCount = filtered.size
        )
    }

    fun filterActivitiesByPeriod(
        activities: List<Activity>,
        period: AnalyticsPeriod,
        referenceInstant: Instant,
        zoneId: ZoneId
    ): List<Activity> {
        val refDate = referenceInstant.atZone(zoneId).toLocalDate()

        return when (period) {
            AnalyticsPeriod.TODAY -> {
                val startOfDay = refDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endOfDay = refDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                activities.filter { it.startTimeMillis in startOfDay until endOfDay }
            }
            AnalyticsPeriod.THIS_WEEK -> {
                val startOfWeek = refDate.minusDays(refDate.dayOfWeek.value.toLong() - 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endOfWeek = refDate.plusDays(8 - refDate.dayOfWeek.value.toLong()).atStartOfDay(zoneId).toInstant().toEpochMilli()
                activities.filter { it.startTimeMillis in startOfWeek until endOfWeek }
            }
            AnalyticsPeriod.THIS_MONTH -> {
                val startOfMonth = refDate.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endOfMonth = refDate.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                activities.filter { it.startTimeMillis in startOfMonth until endOfMonth }
            }
            AnalyticsPeriod.ALL_TIME -> activities
        }
    }

    fun calculateProductivityScore(categories: List<CategoryAnalytics>): Int {
        if (categories.isEmpty()) return 75
        val productiveMinutes = categories.filter { it.isProductive }.sumOf { it.durationMinutes }
        val totalMinutes = categories.sumOf { it.durationMinutes }
        if (totalMinutes == 0L) return 75
        val ratio = (productiveMinutes.toDouble() / totalMinutes) * 100.0
        return ratio.toInt().coerceIn(0, 100)
    }

    fun findPeakProductiveHour(activities: List<Activity>, zoneId: ZoneId): String {
        if (activities.isEmpty()) return "10:00 AM – 11:00 AM"

        val hourlyCount = IntArray(24)
        for (act in activities) {
            val hour = Instant.ofEpochMilli(act.startTimeMillis).atZone(zoneId).hour
            hourlyCount[hour]++
        }

        var peakHour = 10
        var maxCount = -1
        for (h in 0 until 24) {
            if (hourlyCount[h] > maxCount) {
                maxCount = hourlyCount[h]
                peakHour = h
            }
        }

        val nextHour = (peakHour + 1) % 24
        val startFormatted = LocalTime.of(peakHour, 0).format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
        val endFormatted = LocalTime.of(nextHour, 0).format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))

        return "$startFormatted – $endFormatted"
    }

    fun inferCategoryFromLabel(label: String): String {
        val l = label.lowercase()
        return when {
            l.contains("code") || l.contains("dev") || l.contains("program") || l.contains("bug") || l.contains("git") -> "Coding & Dev"
            l.contains("deep") || l.contains("focus") || l.contains("pomodoro") || l.contains("write") -> "Deep Work"
            l.contains("meet") || l.contains("sync") || l.contains("call") || l.contains("zoom") || l.contains("standup") -> "Meetings"
            l.contains("study") || l.contains("learn") || l.contains("read") || l.contains("research") -> "Study & Research"
            l.contains("gym") || l.contains("run") || l.contains("walk") || l.contains("workout") || l.contains("exercise") -> "Exercise & Health"
            l.contains("rest") || l.contains("sleep") || l.contains("nap") || l.contains("relax") -> "Rest & Sleep"
            l.contains("flight") || l.contains("transit") || l.contains("travel") || l.contains("train") -> "Travel & Transit"
            l.contains("email") || l.contains("admin") || l.contains("slack") || l.contains("message") -> "Admin & Email"
            else -> "Deep Work"
        }
    }

    fun createSampleActivities(referenceInstant: Instant = Instant.now()): List<Activity> {
        val base = referenceInstant.truncatedTo(ChronoUnit.DAYS)
        return listOf(
            Activity(
                label = "Core Architecture & Jetpack Compose",
                startTimeMillis = base.plus(9, ChronoUnit.HOURS).toEpochMilli(),
                endTimeMillis = base.plus(11, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli(),
                category = "Coding & Dev"
            ),
            Activity(
                label = "Global Team Cross-Timezone Sync",
                startTimeMillis = base.plus(11, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES).toEpochMilli(),
                endTimeMillis = base.plus(12, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli(),
                category = "Meetings"
            ),
            Activity(
                label = "Autonomous Hive Agent Research",
                startTimeMillis = base.plus(13, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli(),
                endTimeMillis = base.plus(15, ChronoUnit.HOURS).toEpochMilli(),
                category = "Study & Research"
            ),
            Activity(
                label = "Deep Work Pomodoro Sprint",
                startTimeMillis = base.plus(15, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES).toEpochMilli(),
                endTimeMillis = base.plus(17, ChronoUnit.HOURS).toEpochMilli(),
                category = "Deep Work"
            ),
            Activity(
                label = "Evening Cardio & Walk",
                startTimeMillis = base.plus(17, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES).toEpochMilli(),
                endTimeMillis = base.plus(18, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES).toEpochMilli(),
                category = "Exercise & Health"
            )
        )
    }
}
