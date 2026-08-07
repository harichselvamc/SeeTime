package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.ZoneCache
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit tests for the pure time math used to render the home screen.
 * These are the calculations most visible to users, so they're covered
 * directly rather than only exercised indirectly through the UI.
 */
class TimeMathTest {

    private fun utcMillis(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int, second: Int
    ): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(year, month - 1, day, hour, minute, second)
        return cal.timeInMillis
    }

    private fun cache(
        tz: String = "Test/Zone",
        offsetMinutes: Int,
        standardOffsetMinutes: Int = offsetMinutes,
        dstActive: Boolean = false
    ) = ZoneCache(
        timeZone = tz,
        offsetMinutes = offsetMinutes,
        standardOffsetMinutes = standardOffsetMinutes,
        dstActive = dstActive,
        lastUpdated = 0L
    )

    // ---------- formatDateTime ----------

    @Test
    fun `formatDateTime returns placeholder when cache is null`() {
        val now = utcMillis(2025, 11, 18, 12, 0, 0)
        assertEquals("--", TimeMath.formatDateTime(now, null))
    }

    @Test
    fun `formatDateTime applies positive offset and formats 12-hour PM`() {
        // 12:31:32 UTC + 5:30 (IST) = 18:01:32 local -> 06:01:32 PM
        val now = utcMillis(2025, 11, 18, 12, 31, 32)
        val result = TimeMath.formatDateTime(now, cache(offsetMinutes = 330))
        assertEquals("18 Nov 2025, 06:01:32 PM", result)
    }

    @Test
    fun `formatDateTime handles midnight as 12 AM`() {
        val now = utcMillis(2025, 1, 1, 0, 0, 0)
        val result = TimeMath.formatDateTime(now, cache(offsetMinutes = 0))
        assertEquals("01 Jan 2025, 12:00:00 AM", result)
    }

    @Test
    fun `formatDateTime handles noon as 12 PM`() {
        val now = utcMillis(2025, 1, 1, 12, 0, 0)
        val result = TimeMath.formatDateTime(now, cache(offsetMinutes = 0))
        assertEquals("01 Jan 2025, 12:00:00 PM", result)
    }

    @Test
    fun `formatDateTime uses 24-hour clock when requested`() {
        val now = utcMillis(2025, 11, 18, 12, 31, 32)
        val result = TimeMath.formatDateTime(now, cache(offsetMinutes = 330), use24Hour = true)
        assertEquals("18 Nov 2025, 18:01:32", result)
    }

    @Test
    fun `formatDateTime rolls over to previous day for negative offset`() {
        // 00:30 UTC - 2:00 hrs = previous day 22:30
        val now = utcMillis(2025, 3, 10, 0, 30, 0)
        val result = TimeMath.formatDateTime(now, cache(offsetMinutes = -120))
        assertEquals("09 Mar 2025, 10:30:00 PM", result)
    }

    // ---------- buildDiffText ----------

    @Test
    fun `buildDiffText shows question mark when either cache missing`() {
        assertEquals("Time difference: ?", TimeMath.buildDiffText(null, cache(offsetMinutes = 0)))
        assertEquals("Time difference: ?", TimeMath.buildDiffText(cache(offsetMinutes = 0), null))
    }

    @Test
    fun `buildDiffText is zero for same offset`() {
        val from = cache(offsetMinutes = 330)
        val to = cache(offsetMinutes = 330)
        assertEquals("Time difference: +0:00 hrs", TimeMath.buildDiffText(from, to))
    }

    @Test
    fun `buildDiffText shows positive difference with minutes`() {
        // Asia/Kolkata (+5:30) -> Asia/Kathmandu (+5:45) = +0:15
        val from = cache(offsetMinutes = 330)
        val to = cache(offsetMinutes = 345)
        assertEquals("Time difference: +0:15 hrs", TimeMath.buildDiffText(from, to))
    }

    @Test
    fun `buildDiffText shows negative difference`() {
        // Asia/Kolkata (+5:30) -> America/New_York (-5:00 standard) = -10:30
        val from = cache(offsetMinutes = 330)
        val to = cache(offsetMinutes = -300)
        assertEquals("Time difference: -10:30 hrs", TimeMath.buildDiffText(from, to))
    }

    // ---------- buildDstText ----------

    @Test
    fun `buildDstText shows question marks when caches missing`() {
        assertEquals("From DST: ? | To DST: ?", TimeMath.buildDstText(null, null))
    }

    @Test
    fun `buildDstText shows inactive when dst not active`() {
        val from = cache(offsetMinutes = 330, dstActive = false)
        val to = cache(offsetMinutes = -300, dstActive = false)
        assertEquals(
            "From DST: inactive | To DST: inactive",
            TimeMath.buildDstText(from, to)
        )
    }

    @Test
    fun `buildDstText computes actual 1 hour delta, not a hardcoded guess`() {
        // America/New_York in summer: standard -300 (EST), currently -240 (EDT) -> +1:00
        val from = cache(offsetMinutes = -240, standardOffsetMinutes = -300, dstActive = true)
        val to = cache(offsetMinutes = 0, dstActive = false)
        assertEquals(
            "From DST: active (+1:00 hrs) | To DST: inactive",
            TimeMath.buildDstText(from, to)
        )
    }

    @Test
    fun `buildDstText computes non-1-hour delta correctly`() {
        // Lord Howe Island observes a 30-minute DST shift.
        val from = cache(offsetMinutes = 630, standardOffsetMinutes = 600, dstActive = true)
        val to = cache(offsetMinutes = 0, dstActive = false)
        assertEquals(
            "From DST: active (+0:30 hrs) | To DST: inactive",
            TimeMath.buildDstText(from, to)
        )
    }
}
