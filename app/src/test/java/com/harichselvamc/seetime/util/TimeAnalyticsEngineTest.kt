package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class TimeAnalyticsEngineTest {

    @Test
    fun `sample activities are generated with productive categories`() {
        val samples = TimeAnalyticsEngine.createSampleActivities()
        assertTrue(samples.isNotEmpty())
        assertTrue(samples.any { it.category == "Coding & Dev" })
        assertTrue(samples.any { it.category == "Deep Work" })
    }

    @Test
    fun `generateReport aggregates durations percentages and scores accurately`() {
        val now = Instant.parse("2026-09-02T12:00:00Z")
        val base = now.truncatedTo(ChronoUnit.DAYS)

        val activities = listOf(
            Activity(
                label = "Kotlin Compose Architecture",
                startTimeMillis = base.plus(9, ChronoUnit.HOURS).toEpochMilli(),
                endTimeMillis = base.plus(11, ChronoUnit.HOURS).toEpochMilli(), // 120 min
                category = "Coding & Dev"
            ),
            Activity(
                label = "Sprint Planning Sync",
                startTimeMillis = base.plus(11, ChronoUnit.HOURS).toEpochMilli(),
                endTimeMillis = base.plus(12, ChronoUnit.HOURS).toEpochMilli(), // 60 min
                category = "Meetings"
            ),
            Activity(
                label = "Lunch & Rest Break",
                startTimeMillis = base.plus(12, ChronoUnit.HOURS).toEpochMilli(),
                endTimeMillis = base.plus(13, ChronoUnit.HOURS).toEpochMilli(), // 60 min
                category = "Rest & Sleep"
            )
        )

        val report = TimeAnalyticsEngine.generateReport(
            activities = activities,
            period = AnalyticsPeriod.TODAY,
            referenceInstant = now,
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(240L, report.totalTrackedMinutes) // 4 hours
        assertEquals(4.0, report.totalTrackedHours, 0.001)
        assertEquals(3, report.categories.size)

        val codingCat = report.categories.find { it.categoryName == "Coding & Dev" }
        assertNotNull(codingCat)
        assertEquals(120L, codingCat!!.durationMinutes)
        assertEquals(2.0, codingCat.durationHours, 0.001)
        assertEquals(0.50f, codingCat.percentage, 0.01f) // 50%

        // Productivity score: Productive (Coding 120 + Meetings 60 = 180 / 240 = 75%)
        assertEquals(75, report.productivityScorePercent)
    }

    @Test
    fun `inferCategoryFromLabel infers appropriate category from keywords`() {
        assertEquals("Coding & Dev", TimeAnalyticsEngine.inferCategoryFromLabel("Fixing Compose bug in Navigation"))
        assertEquals("Meetings", TimeAnalyticsEngine.inferCategoryFromLabel("1-on-1 Sync with Hari"))
        assertEquals("Study & Research", TimeAnalyticsEngine.inferCategoryFromLabel("Researching Kepler orbits"))
        assertEquals("Exercise & Health", TimeAnalyticsEngine.inferCategoryFromLabel("Morning gym workout"))
        assertEquals("Rest & Sleep", TimeAnalyticsEngine.inferCategoryFromLabel("Power nap 20m"))
    }

    @Test
    fun `findPeakProductiveHour identifies highest activity frequency window`() {
        val now = Instant.parse("2026-09-02T12:00:00Z")
        val base = now.truncatedTo(ChronoUnit.DAYS)

        val activities = listOf(
            Activity(label = "A", startTimeMillis = base.plus(10, ChronoUnit.HOURS).toEpochMilli(), endTimeMillis = base.plus(11, ChronoUnit.HOURS).toEpochMilli(), category = "Work"),
            Activity(label = "B", startTimeMillis = base.plus(10, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES).toEpochMilli(), endTimeMillis = base.plus(11, ChronoUnit.HOURS).toEpochMilli(), category = "Work"),
            Activity(label = "C", startTimeMillis = base.plus(14, ChronoUnit.HOURS).toEpochMilli(), endTimeMillis = base.plus(15, ChronoUnit.HOURS).toEpochMilli(), category = "Work")
        )

        val peak = TimeAnalyticsEngine.findPeakProductiveHour(activities, ZoneId.of("UTC"))
        assertTrue(peak.contains("10:00"))
    }
}
