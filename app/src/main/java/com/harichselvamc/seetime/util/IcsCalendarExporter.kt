package com.harichselvamc.seetime.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Standard RFC 5545 iCalendar (.ics) Generator.
 * Exports meeting overlaps and smart alarms into calendar files compatible with
 * Google Calendar, Outlook, Apple Calendar, and native Android calendar providers.
 */
object IcsCalendarExporter {

    data class CalendarEvent(
        val uid: String = UUID.randomUUID().toString() + "@seetime.app",
        val title: String,
        val description: String = "",
        val startEpochMillis: Long,
        val endEpochMillis: Long = startEpochMillis + (60 * 60 * 1000), // Default 1 hour
        val location: String = "",
        val timeZoneId: String = "UTC"
    )

    private val utcDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Generates a complete RFC 5545 compliant VCALENDAR document.
     */
    fun generateIcs(
        events: List<CalendarEvent>,
        prodId: String = "-//SeeTime//Timezone Overlap Engine//EN"
    ): String {
        val nowFormatted = formatUtc(System.currentTimeMillis())
        val builder = StringBuilder()

        builder.append("BEGIN:VCALENDAR\r\n")
        builder.append("VERSION:2.0\r\n")
        builder.append("PRODID:$prodId\r\n")
        builder.append("CALSCALE:GREGORIAN\r\n")
        builder.append("METHOD:PUBLISH\r\n")

        for (event in events) {
            builder.append("BEGIN:VEVENT\r\n")
            builder.append("UID:${escapeField(event.uid)}\r\n")
            builder.append("DTSTAMP:$nowFormatted\r\n")
            builder.append("DTSTART:${formatUtc(event.startEpochMillis)}\r\n")
            builder.append("DTEND:${formatUtc(event.endEpochMillis)}\r\n")
            builder.append("SUMMARY:${escapeField(event.title)}\r\n")
            if (event.description.isNotBlank()) {
                builder.append("DESCRIPTION:${escapeField(event.description)}\r\n")
            }
            if (event.location.isNotBlank()) {
                builder.append("LOCATION:${escapeField(event.location)}\r\n")
            }
            builder.append("STATUS:CONFIRMED\r\n")
            builder.append("SEQUENCE:0\r\n")
            builder.append("END:VEVENT\r\n")
        }

        builder.append("END:VCALENDAR\r\n")
        return builder.toString()
    }

    /**
     * Creates an overlap meeting calendar event between two timezones.
     */
    fun createMeetingOverlapEvent(
        sourceZone: String,
        targetZone: String,
        startEpochMillis: Long,
        endEpochMillis: Long,
        overlapQuality: String = "Optimal Overlap"
    ): CalendarEvent {
        return CalendarEvent(
            title = "SeeTime Meeting: $sourceZone / $targetZone",
            description = "Cross-timezone meeting scheduled via SeeTime. Overlap: $overlapQuality.",
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis,
            location = "$sourceZone & $targetZone"
        )
    }

    private fun formatUtc(epochMillis: Long): String {
        return synchronized(utcDateFormat) {
            utcDateFormat.format(Date(epochMillis))
        }
    }

    private fun escapeField(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\r\n", "\\n")
            .replace("\n", "\\n")
    }
}
