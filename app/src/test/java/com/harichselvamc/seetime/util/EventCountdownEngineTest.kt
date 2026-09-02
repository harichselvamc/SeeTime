package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EventCountdownEngineTest {

    @Test
    fun `calculateCountdown accurately computes T-Minus for future target`() {
        val now = ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, ZoneOffset.UTC)
        val target = ZonedDateTime.of(2026, 9, 12, 18, 30, 15, 500_000_000, ZoneOffset.UTC) // +10d 6h 30m 15s 500ms

        val countdown = EventCountdownEngine.calculateCountdown(target, now)

        assertFalse(countdown.isPastEvent)
        assertEquals(10L, countdown.days)
        assertEquals(6L, countdown.hours)
        assertEquals(30L, countdown.minutes)
        assertEquals(15L, countdown.seconds)
        assertEquals(500L, countdown.millis)
        assertTrue(countdown.tMinusFormatted.startsWith("T - "))
    }

    @Test
    fun `calculateCountdown accurately computes T-Plus for past target`() {
        val now = ZonedDateTime.of(2026, 9, 7, 12, 0, 0, 0, ZoneOffset.UTC)
        val target = ZonedDateTime.of(2026, 9, 2, 10, 0, 0, 0, ZoneOffset.UTC) // 5d 2h ago

        val countdown = EventCountdownEngine.calculateCountdown(target, now)

        assertTrue(countdown.isPastEvent)
        assertEquals(5L, countdown.days)
        assertEquals(2L, countdown.hours)
        assertTrue(countdown.tMinusFormatted.startsWith("T + "))
    }

    @Test
    fun `createCustomEvent generates valid custom countdown record`() {
        val date = LocalDate.of(2027, 5, 20)
        val time = LocalTime.of(14, 30)
        val zone = ZoneId.of("Asia/Tokyo")

        val event = EventCountdownEngine.createCustomEvent(
            title = "Tokyo Product Launch",
            description = "Major offline release",
            date = date,
            time = time,
            zoneId = zone
        )

        assertNotNull(event.id)
        assertEquals("Tokyo Product Launch", event.title)
        assertEquals("Asia/Tokyo", event.targetZdt.zone.id)
        assertEquals(2027, event.targetZdt.year)
        assertEquals(5, event.targetZdt.monthValue)
    }

    @Test
    fun `preloaded events list contains verified global milestones`() {
        assertTrue(EventCountdownEngine.PRELOADED_EVENTS.isNotEmpty())
        val titles = EventCountdownEngine.PRELOADED_EVENTS.map { it.title }

        assertTrue(titles.any { it.contains("Sydney") })
        assertTrue(titles.any { it.contains("Tokyo") })
        assertTrue(titles.any { it.contains("Times Square") })
        assertTrue(titles.any { it.contains("Eclipse") })
    }
}
