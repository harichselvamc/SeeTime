package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class DstShiftType(val displayName: String, val badgeColor: Long) {
    SPRING_FORWARD_MINUS_ONE_HOUR_SLEEP("Spring Forward (-1h Sleep)", 0xFFEA580C),
    FALL_BACK_PLUS_ONE_HOUR_SLEEP("Fall Back (+1h Sleep)", 0xFF22C55E),
    NO_DST("Permanent Standard (No DST)", 0xFF64748B)
}

data class ZoneDstStatus(
    val zoneId: String,
    val regionName: String,
    val countryEmoji: String,
    val isDstActiveNow: Boolean,
    val currentOffsetHours: Double,
    val standardOffsetHours: Double,
    val hasDst: Boolean,
    val nextTransitionZdt: ZonedDateTime?,
    val nextTransitionFormatted: String,
    val shiftType: DstShiftType,
    val daysUntilTransition: Int,
    val sleepImpactDescription: String
)

data class DstMeetingAnomaly(
    val zone1Id: String,
    val zone2Id: String,
    val standardDiffHours: Double,
    val temporaryDiffHours: Double,
    val anomalyStart: String,
    val anomalyEnd: String,
    val warningHeadline: String,
    val explanation: String
)

object DstPredictorEngine {

    val MONITORED_REGIONS = listOf(
        Triple("Europe/London", "London (UK / BST)", "🇬🇧"),
        Triple("America/New_York", "New York (US Eastern)", "🇺🇸"),
        Triple("Europe/Paris", "Paris / Berlin (Central Europe)", "🇪🇺"),
        Triple("Australia/Sydney", "Sydney (Australia Eastern)", "🇦🇺"),
        Triple("Asia/Tokyo", "Tokyo (Japan)", "🇯🇵"),
        Triple("Asia/Kolkata", "Kolkata (India / IST)", "🇮🇳"),
        Triple("Asia/Singapore", "Singapore (SGT)", "🇸🇬"),
        Triple("America/Los_Angeles", "Los Angeles (US Pacific)", "🇺🇸")
    )

    /**
     * Calculates current and upcoming DST status for [zoneIdStr].
     */
    fun calculateZoneDstStatus(
        zoneIdStr: String,
        regionName: String? = null,
        countryEmoji: String? = null,
        referenceInstant: Instant = Instant.now()
    ): ZoneDstStatus {
        val zone = try { ZoneId.of(zoneIdStr) } catch (_: Exception) { ZoneId.systemDefault() }
        val rules = zone.rules

        val currentZdt = referenceInstant.atZone(zone)
        val isDstNow = rules.isDaylightSavings(referenceInstant)
        val currentOffsetSec = rules.getOffset(referenceInstant).totalSeconds
        val standardOffsetSec = rules.getStandardOffset(referenceInstant).totalSeconds

        val currentOffsetHours = currentOffsetSec / 3600.0
        val standardOffsetHours = standardOffsetSec / 3600.0

        val nextTransition = rules.nextTransition(referenceInstant)

        val regName = regionName ?: zoneIdStr.substringAfterLast('/').replace('_', ' ')
        val emoji = countryEmoji ?: "🌐"

        if (nextTransition == null || nextTransition.duration.isZero) {
            return ZoneDstStatus(
                zoneId = zoneIdStr,
                regionName = regName,
                countryEmoji = emoji,
                isDstActiveNow = false,
                currentOffsetHours = currentOffsetHours,
                standardOffsetHours = standardOffsetHours,
                hasDst = false,
                nextTransitionZdt = null,
                nextTransitionFormatted = "No DST Shifts (Permanent UTC${if (currentOffsetHours >= 0) "+$currentOffsetHours" else "$currentOffsetHours"})",
                shiftType = DstShiftType.NO_DST,
                daysUntilTransition = 9999,
                sleepImpactDescription = "Consistent year-round biological schedule with zero clock shift disruption."
            )
        }

        val transitionInstant = nextTransition.instant
        val transitionZdt = transitionInstant.atZone(zone)

        val offsetBefore = nextTransition.offsetBefore.totalSeconds
        val offsetAfter = nextTransition.offsetAfter.totalSeconds
        val shiftSeconds = offsetAfter - offsetBefore

        val isSpringForward = shiftSeconds > 0
        val shiftType = if (isSpringForward) DstShiftType.SPRING_FORWARD_MINUS_ONE_HOUR_SLEEP else DstShiftType.FALL_BACK_PLUS_ONE_HOUR_SLEEP

        val daysUntil = ChronoUnit.DAYS.between(currentZdt.toLocalDate(), transitionZdt.toLocalDate()).toInt().coerceAtLeast(0)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH)

        val sleepImpact = if (isSpringForward) {
            "Clock jumps forward by 1 hour (02:00 -> 03:00). You lose 1 hour of sleep. Pre-adjust sleep 15 min earlier for 4 days."
        } else {
            "Clock falls back by 1 hour (02:00 -> 01:00). You gain 1 hour of extra sleep & morning sunlight."
        }

        return ZoneDstStatus(
            zoneId = zoneIdStr,
            regionName = regName,
            countryEmoji = emoji,
            isDstActiveNow = isDstNow,
            currentOffsetHours = currentOffsetHours,
            standardOffsetHours = standardOffsetHours,
            hasDst = true,
            nextTransitionZdt = transitionZdt,
            nextTransitionFormatted = transitionZdt.format(dateFormatter),
            shiftType = shiftType,
            daysUntilTransition = daysUntil,
            sleepImpactDescription = sleepImpact
        )
    }

    /**
     * Checks if two timezones have an asymmetric DST transition causing a temporary cross-border meeting anomaly.
     */
    fun checkMeetingDstShiftAnomaly(
        zone1Id: String = "Europe/London",
        zone2Id: String = "America/New_York",
        referenceInstant: Instant = Instant.now()
    ): DstMeetingAnomaly? {
        val status1 = calculateZoneDstStatus(zone1Id, referenceInstant = referenceInstant)
        val status2 = calculateZoneDstStatus(zone2Id, referenceInstant = referenceInstant)

        if (!status1.hasDst || !status2.hasDst) return null

        val t1 = status1.nextTransitionZdt ?: return null
        val t2 = status2.nextTransitionZdt ?: return null

        val diffDays = ChronoUnit.DAYS.between(t1.toLocalDate(), t2.toLocalDate())

        if (diffDays != 0L && kotlin.math.abs(diffDays) < 30L) {
            val earlier = if (t1.isBefore(t2)) t1 else t2
            val later = if (t1.isBefore(t2)) t2 else t1
            val earlierName = if (t1.isBefore(t2)) status1.regionName else status2.regionName
            val laterName = if (t1.isBefore(t2)) status2.regionName else status1.regionName

            val currentDiffHours = kotlin.math.abs(status1.currentOffsetHours - status2.currentOffsetHours)
            val anomalousDiffHours = if (status1.shiftType == DstShiftType.FALL_BACK_PLUS_ONE_HOUR_SLEEP) currentDiffHours - 1.0 else currentDiffHours + 1.0

            val dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

            return DstMeetingAnomaly(
                zone1Id = zone1Id,
                zone2Id = zone2Id,
                standardDiffHours = currentDiffHours,
                temporaryDiffHours = anomalousDiffHours,
                anomalyStart = earlier.format(dateFmt),
                anomalyEnd = later.format(dateFmt),
                warningHeadline = "Asymmetric DST Shift Warning: ${kotlin.math.abs(diffDays)}-Day Window ($earlierName vs $laterName)",
                explanation = "$earlierName shifts clocks on ${earlier.format(dateFmt)}, while $laterName shifts on ${later.format(dateFmt)}. During this ${kotlin.math.abs(diffDays)}-day window, the time difference will be ${anomalousDiffHours.toInt()}h instead of standard ${currentDiffHours.toInt()}h!"
            )
        }

        return null
    }
}
