package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.ZoneCache
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

/**
 * Pure, side-effect-free time formatting/diff/DST math used by the home
 * screen. Kept separate from TimeViewModel (and free of any Android
 * framework or Room runtime dependency beyond the plain ZoneCache data
 * class) so it can be unit tested on the JVM.
 */
object TimeMath {

    private val monthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    /**
     * Format **date + time**, in either 12-hour or 24-hour format depending
     * on [use24Hour], optionally including seconds.
     * Example (12h): "18 Nov 2025, 06:01:32 PM"
     * Example (24h): "18 Nov 2025, 18:01:32"
     */
    fun formatDateTime(
        nowUtc: Long,
        cache: ZoneCache?,
        use24Hour: Boolean = false,
        showSeconds: Boolean = true
    ): String {
        if (cache == null) return "--"

        val millis = nowUtc + cache.offsetMinutes * 60_000L
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-11
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)


        val monthName = monthNames[month.coerceIn(0, 11)]
        val datePart = "%02d %s %04d".format(day, monthName, year)

        if (use24Hour) {
            val timePart = if (showSeconds) {
                "%02d:%02d:%02d".format(hour24, minute, second)
            } else {
                "%02d:%02d".format(hour24, minute)
            }
            return "$datePart, $timePart"
        }

        val (hour12, amPm) = when {
            hour24 == 0 -> 12 to "AM"          // 00:xx -> 12 AM
            hour24 < 12 -> hour24 to "AM"      // 01-11 -> AM
            hour24 == 12 -> 12 to "PM"         // 12:xx -> 12 PM
            else -> (hour24 - 12) to "PM"      // 13-23 -> 1-11 PM
        }
        val timePart = if (showSeconds) {
            "%02d:%02d:%02d %s".format(hour12, minute, second, amPm)
        } else {
            "%02d:%02d %s".format(hour12, minute, amPm)
        }
        return "$datePart, $timePart"
    }

    fun getMillisForTimeToday(hour: Int, minute: Int, currentMillis: Long): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = currentMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Show time difference including DST as "+H:MM hrs" or "-H:MM hrs".
     * Example: "Time difference: +1:30 hrs"
     */
    fun buildDiffText(from: ZoneCache?, to: ZoneCache?): String {
        if (from == null || to == null) return "Time difference: ?"

        // offsetMinutes already includes DST if active
        val diff = to.offsetMinutes - from.offsetMinutes
        val sign = if (diff >= 0) "+" else "-"
        val absMin = abs(diff)
        val h = absMin / 60
        val m = absMin % 60
        return "Time difference: $sign$h:${m.toString().padStart(2, '0')} hrs"
    }

    /**
     * Show DST status for each side, with the *actual* DST delta for that
     * zone (offsetMinutes - standardOffsetMinutes) rather than assuming a
     * fixed +1:00 shift — a handful of zones observe a non-1-hour DST
     * shift.
     *
     * Example:
     *   "From DST: active (+1:00 hrs) | To DST: inactive"
     */
    fun buildDstText(from: ZoneCache?, to: ZoneCache?): String {
        fun formatSide(label: String, cache: ZoneCache?): String {
            if (cache == null) return "$label DST: ?"
            if (!cache.dstActive) return "$label DST: inactive"

            val deltaMin = cache.offsetMinutes - cache.standardOffsetMinutes
            val sign = if (deltaMin >= 0) "+" else "-"
            val absMin = abs(deltaMin)
            val h = absMin / 60
            val m = absMin % 60
            return "$label DST: active ($sign$h:${m.toString().padStart(2, '0')} hrs)"
        }

        return "${formatSide("From", from)} | ${formatSide("To", to)}"
    }

    /**
     * Format date only (dd Mon yyyy).
     */
    fun formatDateOnly(
        nowUtc: Long,
        cache: ZoneCache?
    ): String {
        if (cache == null) return "--"

        val millis = nowUtc + cache.offsetMinutes * 60_000L
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = millis

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-11
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val monthName = monthNames[month.coerceIn(0, 11)]
        return "%02d %s %04d".format(day, monthName, year)
    }

    enum class OverlapCategory {
        FULL_WORKING,      // Both in 9 AM - 5 PM (hours 9..16)
        EXTENDED_WORKING,  // One or both in 7 AM - 9 AM or 5 PM - 8 PM
        OFF_HOURS          // Outside 7 AM - 8 PM
    }

    data class HourlyOverlapSlot(
        val localHour: Int,
        val targetHour: Int,
        val targetMinute: Int,
        val category: OverlapCategory,
        val localDisplay: String,
        val targetDisplay: String
    )

    /**
     * Helper to format hour and minute into a 12-hour string (e.g., "9 AM" or "10:30 PM").
     */
    private fun formatH(h: Int, m: Int): String {
        val ampm = if (h < 12) "AM" else "PM"
        val h12 = when {
            h == 0 -> 12
            h <= 12 -> h
            else -> h - 12
        }
        return if (m == 0) "%d %s".format(h12, ampm) else "%d:%02d %s".format(h12, m, ampm)
    }

    /**
     * Compute 24-hour overlap grid for a given offset difference in minutes.
     */
    fun compute24HourOverlapMatrix(offsetDiffMinutes: Int): List<HourlyOverlapSlot> {
        val slots = mutableListOf<HourlyOverlapSlot>()
        for (localH in 0 until 24) {
            val totalTargetMinutes = (localH * 60 + offsetDiffMinutes)
            val normalizedTargetMin = ((totalTargetMinutes % 1440) + 1440) % 1440
            val targetH = normalizedTargetMin / 60
            val targetM = normalizedTargetMin % 60

            val isLocalWorking = localH in 9..16
            val isLocalExtended = localH in 7..8 || localH in 17..19

            val isTargetWorking = targetH in 9..16
            val isTargetExtended = targetH in 7..8 || targetH in 17..19

            val category = when {
                isLocalWorking && isTargetWorking -> OverlapCategory.FULL_WORKING
                (isLocalWorking || isLocalExtended) && (isTargetWorking || isTargetExtended) -> OverlapCategory.EXTENDED_WORKING
                else -> OverlapCategory.OFF_HOURS
            }

            slots.add(
                HourlyOverlapSlot(
                    localHour = localH,
                    targetHour = targetH,
                    targetMinute = targetM,
                    category = category,
                    localDisplay = formatH(localH, 0),
                    targetDisplay = formatH(targetH, targetM)
                )
            )
        }
        return slots
    }

    val systemZoneId: String = TimeZone.getDefault().id
}
