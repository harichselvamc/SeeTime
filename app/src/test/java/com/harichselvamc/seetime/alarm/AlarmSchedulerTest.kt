package com.harichselvamc.seetime.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmSchedulerTest {

    @Test
    fun `calculateNextTriggerMillis schedules for later today if time has not passed`() {
        // Fix reference time: 2026-09-02 08:00:00 in UTC (08:00 UTC)
        val zone = "UTC"
        val refTime = ZonedDateTime.of(2026, 9, 2, 8, 0, 0, 0, ZoneId.of(zone)).toInstant().toEpochMilli()

        // Target: 10:30 UTC today
        val triggerMillis = AlarmScheduler.calculateNextTriggerMillis(
            targetHour = 10,
            targetMinute = 30,
            targetZone = zone,
            repeatDays = emptyList(),
            referenceEpochMillis = refTime
        )

        val triggerDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerMillis), ZoneId.of(zone))
        assertEquals(2026, triggerDateTime.year)
        assertEquals(9, triggerDateTime.monthValue)
        assertEquals(2, triggerDateTime.dayOfMonth)
        assertEquals(10, triggerDateTime.hour)
        assertEquals(30, triggerDateTime.minute)
    }

    @Test
    fun `calculateNextTriggerMillis rolls to tomorrow if target time has already passed today`() {
        // Fix reference time: 2026-09-02 15:00:00 in Tokyo (+09:00)
        val zone = "Asia/Tokyo"
        val refTime = ZonedDateTime.of(2026, 9, 2, 15, 0, 0, 0, ZoneId.of(zone)).toInstant().toEpochMilli()

        // Target: 09:00 Tokyo (already passed today)
        val triggerMillis = AlarmScheduler.calculateNextTriggerMillis(
            targetHour = 9,
            targetMinute = 0,
            targetZone = zone,
            repeatDays = emptyList(),
            referenceEpochMillis = refTime
        )

        val triggerDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerMillis), ZoneId.of(zone))
        assertEquals(2026, triggerDateTime.year)
        assertEquals(9, triggerDateTime.monthValue)
        assertEquals(3, triggerDateTime.dayOfMonth) // next day!
        assertEquals(9, triggerDateTime.hour)
        assertEquals(0, triggerDateTime.minute)
    }

    @Test
    fun `calculateNextTriggerMillis correctly selects next matching repeat day`() {
        // 2026-09-02 is Wednesday (dayOfWeek=3, index=2: Mon=0, Tue=1, Wed=2, Thu=3, Fri=4, Sat=5, Sun=6)
        val zone = "America/New_York"
        val refTime = ZonedDateTime.of(2026, 9, 2, 16, 0, 0, 0, ZoneId.of(zone)).toInstant().toEpochMilli()

        // Target: 09:00 on Fri (index 4) and Mon (index 0)
        val repeatDays = listOf(0, 4) // Mon, Fri
        val triggerMillis = AlarmScheduler.calculateNextTriggerMillis(
            targetHour = 9,
            targetMinute = 0,
            targetZone = zone,
            repeatDays = repeatDays,
            referenceEpochMillis = refTime
        )

        val triggerDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerMillis), ZoneId.of(zone))
        // Next Friday is 2026-09-04
        assertEquals(2026, triggerDateTime.year)
        assertEquals(9, triggerDateTime.monthValue)
        assertEquals(4, triggerDateTime.dayOfMonth)
        assertEquals(9, triggerDateTime.hour)
        assertEquals(0, triggerDateTime.minute)
    }

    @Test
    fun `calculateNextTriggerMillis wraps around to next week for repeat days`() {
        // 2026-09-05 is Saturday (index 5)
        val zone = "Europe/London"
        val refTime = ZonedDateTime.of(2026, 9, 5, 12, 0, 0, 0, ZoneId.of(zone)).toInstant().toEpochMilli()

        // Repeat only on Tuesday (index 1)
        val repeatDays = listOf(1)
        val triggerMillis = AlarmScheduler.calculateNextTriggerMillis(
            targetHour = 14,
            targetMinute = 15,
            targetZone = zone,
            repeatDays = repeatDays,
            referenceEpochMillis = refTime
        )

        val triggerDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerMillis), ZoneId.of(zone))
        // Next Tuesday is 2026-09-08
        assertEquals(2026, triggerDateTime.year)
        assertEquals(9, triggerDateTime.monthValue)
        assertEquals(8, triggerDateTime.dayOfMonth)
        assertEquals(14, triggerDateTime.hour)
        assertEquals(15, triggerDateTime.minute)
    }

    @Test
    fun `formatLocalEquivalent returns formatted time string`() {
        val result12 = AlarmScheduler.formatLocalEquivalent(
            targetHour = 14,
            targetMinute = 30,
            targetZone = "Asia/Tokyo",
            use24Hour = false
        )
        assertTrue(result12.isNotEmpty())
        assertTrue(result12.contains("AM") || result12.contains("PM"))

        val result24 = AlarmScheduler.formatLocalEquivalent(
            targetHour = 14,
            targetMinute = 30,
            targetZone = "Asia/Tokyo",
            use24Hour = true
        )
        assertTrue(result24.isNotEmpty())
        assertTrue(result24.matches(Regex("\\d{2}:\\d{2}")))
    }
}
