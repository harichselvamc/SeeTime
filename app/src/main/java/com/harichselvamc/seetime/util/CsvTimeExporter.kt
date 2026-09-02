package com.harichselvamc.seetime.util

import com.harichselvamc.seetime.data.local.Activity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Standard RFC 4180 CSV Activity & Time Tracker Exporter.
 * Formats time audit records, categories, and durations for spreadsheet tools (Excel, Google Sheets, Notion).
 */
object CsvTimeExporter {

    private const val CSV_HEADER = "ID,Category,Label,Date,StartTime,EndTime,DurationMinutes"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    /**
     * Exports a list of activities to an RFC 4180 compliant CSV string.
     */
    fun exportActivitiesToCsv(activities: List<Activity>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\r\n")

        for (act in activities) {
            val durationMin = ((act.endTimeMillis - act.startTimeMillis).coerceAtLeast(0) / 60000L).toDouble()
            val dateStr = synchronized(dateFormat) { dateFormat.format(Date(act.startTimeMillis)) }
            val startStr = synchronized(timeFormat) { timeFormat.format(Date(act.startTimeMillis)) }
            val endStr = synchronized(timeFormat) { timeFormat.format(Date(act.endTimeMillis)) }

            val row = listOf(
                act.id.toString(),
                escapeCsv(act.category),
                escapeCsv(act.label),
                dateStr,
                startStr,
                endStr,
                String.format(Locale.US, "%.1f", durationMin)
            ).joinToString(",")

            sb.append(row).append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Escapes a single CSV field according to RFC 4180.
     */
    fun escapeCsv(value: String): String {
        var result = value
        val needsQuotes = result.contains(",") || result.contains("\"") || result.contains("\n") || result.contains("\r")
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"")
        }
        return if (needsQuotes) "\"$result\"" else result
    }
}
