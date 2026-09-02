package com.harichselvamc.seetime.widget

import com.harichselvamc.seetime.data.local.StreakEntity
import com.harichselvamc.seetime.data.local.TimePair
import com.harichselvamc.seetime.util.TimeMath
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

enum class WidgetStreakTier(val displayName: String, val colorHex: Long) {
    SPARK("Spark", 0xFFFF9800),
    BLAZE("Blaze", 0xFFFF5722),
    INFERNO("Inferno", 0xFFE91E63),
    SUPERNOVA("Supernova", 0xFF9C27B0)
}

data class StreakWidgetData(
    val currentStreak: Int,
    val tier: WidgetStreakTier,
    val freezeCount: Int,
    val isCompletedToday: Boolean,
    val isExpiringSoon: Boolean,
    val badgeLabel: String,
    val overlapHeadline: String,
    val overlapSubtext: String,
    val isOverlapActive: Boolean
)

data class QuickLogWidgetData(
    val currentStreak: Int,
    val isCompletedToday: Boolean,
    val streakText: String,
    val actionText: String
)

data class CommandCenterWidgetData(
    val localCity: String,
    val localTimeFormatted: String,
    val localIsDaytime: Boolean,
    val remoteCity: String,
    val remoteTimeFormatted: String,
    val remoteIsDaytime: Boolean,
    val timeDiffFormatted: String,
    val currentStreak: Int,
    val overlapHeadline: String,
    val isOverlapActive: Boolean
)

object WidgetDataEngine {

    private val TIME_FORMATTER_24H = DateTimeFormatter.ofPattern("HH:mm")

    fun getStreakTier(streak: Int): WidgetStreakTier {
        return when {
            streak >= 30 -> WidgetStreakTier.SUPERNOVA
            streak >= 14 -> WidgetStreakTier.INFERNO
            streak >= 7 -> WidgetStreakTier.BLAZE
            else -> WidgetStreakTier.SPARK
        }
    }

    fun isActivityDoneToday(streak: StreakEntity, nowEpochDay: Long = LocalDate.now().toEpochDay()): Boolean {
        return streak.lastActiveEpochDay == nowEpochDay && streak.currentStreak > 0
    }

    fun formatCityName(timeZoneId: String): String {
        return timeZoneId.substringAfterLast('/').replace('_', ' ')
    }

    fun getZoneOffsetMinutes(zoneIdStr: String, nowInstant: Instant = Instant.now()): Int {
        val zone = runCatching { ZoneId.of(zoneIdStr) }.getOrElse { ZoneId.systemDefault() }
        return zone.rules.getOffset(nowInstant).totalSeconds / 60
    }

    fun computeStreakWidgetData(
        streak: StreakEntity,
        pairs: List<TimePair>,
        now: ZonedDateTime = ZonedDateTime.now()
    ): StreakWidgetData {
        val todayEpochDay = now.toLocalDate().toEpochDay()
        val isDoneToday = isActivityDoneToday(streak, todayEpochDay)
        val hoursUntilMidnight = 24 - now.hour
        val isExpiringSoon = !isDoneToday && hoursUntilMidnight <= 4 && streak.currentStreak > 0

        val badgeLabel = when {
            isDoneToday -> "Completed"
            isExpiringSoon -> "Expiring Soon!"
            streak.streakFreezeCount > 0 -> "${streak.streakFreezeCount} Freeze"
            else -> "Active"
        }

        val overlapInfo = calculateOverlapInfo(pairs, now)

        return StreakWidgetData(
            currentStreak = streak.currentStreak,
            tier = getStreakTier(streak.currentStreak),
            freezeCount = streak.streakFreezeCount,
            isCompletedToday = isDoneToday,
            isExpiringSoon = isExpiringSoon,
            badgeLabel = badgeLabel,
            overlapHeadline = overlapInfo.first,
            overlapSubtext = overlapInfo.second,
            isOverlapActive = overlapInfo.third
        )
    }

    fun computeQuickLogWidgetData(
        streak: StreakEntity,
        now: ZonedDateTime = ZonedDateTime.now()
    ): QuickLogWidgetData {
        val todayEpochDay = now.toLocalDate().toEpochDay()
        val isDoneToday = isActivityDoneToday(streak, todayEpochDay)

        return QuickLogWidgetData(
            currentStreak = streak.currentStreak,
            isCompletedToday = isDoneToday,
            streakText = "${streak.currentStreak}d",
            actionText = if (isDoneToday) "Done" else "+ Log"
        )
    }

    fun computeCommandCenterWidgetData(
        streak: StreakEntity,
        pairs: List<TimePair>,
        now: ZonedDateTime = ZonedDateTime.now()
    ): CommandCenterWidgetData {
        val primaryPair = pairs.firstOrNull()

        if (primaryPair == null) {
            val localTimeStr = now.format(TIME_FORMATTER_24H)
            val isDay = now.hour in 6..19
            return CommandCenterWidgetData(
                localCity = "Local",
                localTimeFormatted = localTimeStr,
                localIsDaytime = isDay,
                remoteCity = "Select Pair",
                remoteTimeFormatted = "--:--",
                remoteIsDaytime = true,
                timeDiffFormatted = "No Zone Configured",
                currentStreak = streak.currentStreak,
                overlapHeadline = "Open SeeTime to add timezone pairs",
                isOverlapActive = false
            )
        }

        val originZone = runCatching { ZoneId.of(primaryPair.fromZone) }.getOrElse { ZoneId.systemDefault() }
        val targetZone = runCatching { ZoneId.of(primaryPair.toZone) }.getOrElse { ZoneId.systemDefault() }

        val originTime = now.withZoneSameInstant(originZone)
        val targetTime = now.withZoneSameInstant(targetZone)

        val localIsDay = originTime.hour in 6..19
        val remoteIsDay = targetTime.hour in 6..19

        val fromOffset = getZoneOffsetMinutes(primaryPair.fromZone, now.toInstant())
        val toOffset = getZoneOffsetMinutes(primaryPair.toZone, now.toInstant())
        val diffMinutes = toOffset - fromOffset
        val diffHours = diffMinutes / 60
        val diffRemainderMinutes = abs(diffMinutes % 60)

        val diffStr = buildString {
            if (diffMinutes >= 0) append("+")
            append("${diffHours}h")
            if (diffRemainderMinutes > 0) append(" ${diffRemainderMinutes}m")
            if (diffMinutes > 0) append(" Ahead") else if (diffMinutes < 0) append(" Behind") else append(" Same Time")
        }

        val overlapInfo = calculateOverlapInfo(listOf(primaryPair), now)

        val originCityLabel = formatCityName(primaryPair.fromZone)
        val targetCityLabel = formatCityName(primaryPair.toZone)

        return CommandCenterWidgetData(
            localCity = originCityLabel,
            localTimeFormatted = originTime.format(TIME_FORMATTER_24H),
            localIsDaytime = localIsDay,
            remoteCity = targetCityLabel,
            remoteTimeFormatted = targetTime.format(TIME_FORMATTER_24H),
            remoteIsDaytime = remoteIsDay,
            timeDiffFormatted = diffStr,
            currentStreak = streak.currentStreak,
            overlapHeadline = overlapInfo.first,
            isOverlapActive = overlapInfo.third
        )
    }

    private fun calculateOverlapInfo(
        pairs: List<TimePair>,
        now: ZonedDateTime
    ): Triple<String, String, Boolean> {
        val primaryPair = pairs.firstOrNull() ?: return Triple(
            "Track Timezones",
            "Open SeeTime to configure saved zones",
            false
        )

        val fromOffset = getZoneOffsetMinutes(primaryPair.fromZone, now.toInstant())
        val toOffset = getZoneOffsetMinutes(primaryPair.toZone, now.toInstant())
        val diffMinutes = toOffset - fromOffset
        val matrix = TimeMath.compute24HourOverlapMatrix(diffMinutes)

        val originZone = runCatching { ZoneId.of(primaryPair.fromZone) }.getOrElse { ZoneId.systemDefault() }
        val currentOriginHour = now.withZoneSameInstant(originZone).hour

        val currentSlot = matrix.getOrNull(currentOriginHour)
        val isCurrentlyOverlapping = currentSlot?.category == TimeMath.OverlapCategory.FULL_WORKING ||
                currentSlot?.category == TimeMath.OverlapCategory.EXTENDED_WORKING

        val originLabel = formatCityName(primaryPair.fromZone)
        val targetLabel = formatCityName(primaryPair.toZone)

        if (isCurrentlyOverlapping) {
            return Triple(
                "🟢 Overlap Active Now",
                "$originLabel ⇄ $targetLabel in working hours",
                true
            )
        }

        // Search for next working overlap window within next 24 hours
        var hoursUntilOverlap = -1
        for (i in 1..24) {
            val candidateHour = (currentOriginHour + i) % 24
            val slot = matrix.getOrNull(candidateHour)
            if (slot?.category == TimeMath.OverlapCategory.FULL_WORKING || slot?.category == TimeMath.OverlapCategory.EXTENDED_WORKING) {
                hoursUntilOverlap = i
                break
            }
        }

        return if (hoursUntilOverlap in 1..24) {
            Triple(
                "⏳ Next Overlap in ${hoursUntilOverlap}h",
                "$originLabel ⇄ $targetLabel window ahead",
                false
            )
        } else {
            Triple(
                "💤 Off-Hours",
                "$originLabel ⇄ $targetLabel no working overlap",
                false
            )
        }
    }
}
