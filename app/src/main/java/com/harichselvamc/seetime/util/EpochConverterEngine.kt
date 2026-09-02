package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.util.Locale

data class ConvertedEpochFormats(
    val gregorianUtc: String,
    val gregorianLocal: String,
    val unixSeconds: Long,
    val unixMilliseconds: Long,
    val unixHexadecimal: String,
    val unixBinary: String,
    val julianDate: Double,
    val modifiedJulianDate: Double,
    val gpsWeek: Int,
    val gpsSecondOfWeek: Long,
    val excelSerial: Double,
    val isoOrdinalDate: String,
    val isoWeekDate: String
)

data class MilestoneCountdown(
    val title: String,
    val targetUtc: String,
    val targetTimestampSeconds: Long,
    val daysRemaining: Long,
    val secondsRemaining: Long,
    val description: String
)

object EpochConverterEngine {

    const val UNIX_TO_JULIAN_OFFSET = 2440587.5
    const val UNIX_TO_MJD_OFFSET = 40587.0
    const val UNIX_TO_EXCEL_OFFSET = 25569.0
    const val GPS_EPOCH_UNIX_SECONDS = 315964800L // 1980-01-06T00:00:00Z
    const val YEAR_2038_TIMESTAMP = 2147483647L // 2038-01-19T03:14:07Z
    const val TWO_BILLION_UNIX_TIMESTAMP = 2000000000L // 2033-05-18T03:33:20Z

    /**
     * Converts epoch milliseconds into comprehensive multi-format representations.
     */
    fun convertEpochMillis(
        epochMillis: Long,
        localZoneId: ZoneId = ZoneId.systemDefault()
    ): ConvertedEpochFormats {
        val instant = Instant.ofEpochMilli(epochMillis)
        val utcZdt = instant.atZone(ZoneId.of("UTC"))
        val localZdt = instant.atZone(localZoneId)

        val unixSec = epochMillis / 1000L
        val daysFraction = epochMillis / 86400000.0

        // Julian & Modified Julian Date
        val julianDate = daysFraction + UNIX_TO_JULIAN_OFFSET
        val mjd = daysFraction + UNIX_TO_MJD_OFFSET

        // GPS Epoch (GPS Week & Second of week)
        val gpsTotalSec = (unixSec - GPS_EPOCH_UNIX_SECONDS).coerceAtLeast(0L)
        val gpsWeek = (gpsTotalSec / 604800L).toInt()
        val gpsSecOfWeek = gpsTotalSec % 604800L

        // Excel serial date (1900 date system)
        val excelSerial = daysFraction + UNIX_TO_EXCEL_OFFSET

        // Hexadecimal & Binary 32/64-bit representations
        val hexStr = "0x" + java.lang.Long.toHexString(unixSec).uppercase()
        val binStr = "0b" + java.lang.Long.toBinaryString(unixSec).padStart(32, '0')

        // ISO 8601 Ordinal & Week Date
        val ordinalDay = utcZdt.dayOfYear
        val isoOrdinal = "${utcZdt.year}-${String.format(Locale.getDefault(), "%03d", ordinalDay)}"
        val weekNumber = utcZdt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val dayOfWeekNumber = utcZdt.dayOfWeek.value
        val isoWeek = "${utcZdt.year}-W${String.format(Locale.getDefault(), "%02d", weekNumber)}-$dayOfWeekNumber"

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

        return ConvertedEpochFormats(
            gregorianUtc = "${utcZdt.format(formatter)} UTC",
            gregorianLocal = "${localZdt.format(formatter)} (${localZdt.zone.id.substringAfterLast('/').replace('_', ' ')})",
            unixSeconds = unixSec,
            unixMilliseconds = epochMillis,
            unixHexadecimal = hexStr,
            unixBinary = binStr,
            julianDate = julianDate,
            modifiedJulianDate = mjd,
            gpsWeek = gpsWeek,
            gpsSecondOfWeek = gpsSecOfWeek,
            excelSerial = excelSerial,
            isoOrdinalDate = isoOrdinal,
            isoWeekDate = isoWeek
        )
    }

    /**
     * Smart parser that evaluates a user raw string input into epoch milliseconds.
     */
    fun parseInputToEpochMillis(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // 1. Hexadecimal format (0x...)
        if (trimmed.startsWith("0x", ignoreCase = true)) {
            return try {
                val hexVal = java.lang.Long.parseLong(trimmed.removePrefix("0x").removePrefix("0X"), 16)
                hexVal * 1000L
            } catch (_: Exception) { null }
        }

        // 2. Binary format (0b...)
        if (trimmed.startsWith("0b", ignoreCase = true)) {
            return try {
                val binVal = java.lang.Long.parseLong(trimmed.removePrefix("0b").removePrefix("0B"), 2)
                binVal * 1000L
            } catch (_: Exception) { null }
        }

        // 3. Numeric timestamp (Seconds vs Milliseconds auto-detection)
        val numeric = trimmed.toLongOrNull()
        if (numeric != null) {
            return if (numeric > 100_000_000_000L) {
                // Milliseconds
                numeric
            } else {
                // Seconds
                numeric * 1000L
            }
        }

        // 4. ISO Date / DateTime string (yyyy-MM-dd or yyyy-MM-dd HH:mm:ss)
        return try {
            if (trimmed.length == 10) {
                val date = LocalDate.parse(trimmed)
                date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } else {
                val zdt = ZonedDateTime.parse(trimmed)
                zdt.toInstant().toEpochMilli()
            }
        } catch (_: Exception) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val zdt = java.time.LocalDateTime.parse(trimmed, formatter).atZone(ZoneId.of("UTC"))
                zdt.toInstant().toEpochMilli()
            } catch (_: Exception) { null }
        }
    }

    /**
     * Returns milestone countdown details for the Year 2038 problem and 2-Billion Unix Seconds.
     */
    fun calculateMilestoneCountdowns(currentEpochSeconds: Long = System.currentTimeMillis() / 1000L): List<MilestoneCountdown> {
        val secTo2038 = (YEAR_2038_TIMESTAMP - currentEpochSeconds).coerceAtLeast(0L)
        val daysTo2038 = secTo2038 / 86400L

        val secTo2Bil = (TWO_BILLION_UNIX_TIMESTAMP - currentEpochSeconds).coerceAtLeast(0L)
        val daysTo2Bil = secTo2Bil / 86400L

        return listOf(
            MilestoneCountdown(
                title = "Year 2038 Problem (Y2K38)",
                targetUtc = "2038-01-19 03:14:07 UTC",
                targetTimestampSeconds = YEAR_2038_TIMESTAMP,
                daysRemaining = daysTo2038,
                secondsRemaining = secTo2038,
                description = "Signed 32-bit integer overflow (0x7FFFFFFF) will wrap to Dec 13, 1901 unless systems use 64-bit time."
            ),
            MilestoneCountdown(
                title = "2 Billion Unix Seconds Milestone",
                targetUtc = "2033-05-18 03:33:20 UTC",
                targetTimestampSeconds = TWO_BILLION_UNIX_TIMESTAMP,
                daysRemaining = daysTo2Bil,
                secondsRemaining = secTo2Bil,
                description = "Next major millennial decimal timestamp epoch celebration."
            )
        )
    }
}
