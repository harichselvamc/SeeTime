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

        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
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
}
