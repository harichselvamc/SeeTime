package com.harichselvamc.seetime.backup

import com.harichselvamc.seetime.data.backup.DatabaseBackupManager
import com.harichselvamc.seetime.data.local.Activity
import com.harichselvamc.seetime.util.CsvTimeExporter
import com.harichselvamc.seetime.util.IcsCalendarExporter
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException

class BackupExportTest {

    @Test
    fun testEncryptionAndDecryptionRoundtrip() {
        val sampleData = "SeeTime Room Database Raw Content — Streaks, Zones, Alarms".toByteArray(Charsets.UTF_8)
        val password = "StrongMasterPassword123!".toCharArray()

        val encrypted = DatabaseBackupManager.encryptData(sampleData, password)
        assertNotNull(encrypted)
        assertTrue(encrypted.size > sampleData.size)

        val decrypted = DatabaseBackupManager.decryptData(encrypted, password)
        assertArrayEquals(sampleData, decrypted)
        assertEquals(String(sampleData, Charsets.UTF_8), String(decrypted, Charsets.UTF_8))
    }

    @Test
    fun testDecryptionFailsWithWrongPassword() {
        val sampleData = "Confidential SQLite binary block".toByteArray(Charsets.UTF_8)
        val correctPassword = "CorrectPassword2026".toCharArray()
        val wrongPassword = "WrongPassword999".toCharArray()

        val encrypted = DatabaseBackupManager.encryptData(sampleData, correctPassword)

        try {
            DatabaseBackupManager.decryptData(encrypted, wrongPassword)
            fail("Expected AEADBadTagException / GeneralSecurityException when decrypting with wrong password")
        } catch (e: Exception) {
            assertTrue(e is GeneralSecurityException || e is AEADBadTagException)
        }
    }

    @Test
    fun testDecryptionFailsWithTamperedData() {
        val sampleData = "Authentic payload".toByteArray(Charsets.UTF_8)
        val password = "Password123!".toCharArray()

        val encrypted = DatabaseBackupManager.encryptData(sampleData, password)
        // Tamper last byte (part of GCM tag)
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1] + 1).toByte()

        try {
            DatabaseBackupManager.decryptData(encrypted, password)
            fail("Expected security exception on tampered data")
        } catch (e: Exception) {
            assertTrue(e is GeneralSecurityException || e is AEADBadTagException)
        }
    }

    @Test
    fun testDecryptionFailsWithCorruptedMagicHeader() {
        val sampleData = "Data".toByteArray(Charsets.UTF_8)
        val password = "Password".toCharArray()

        val encrypted = DatabaseBackupManager.encryptData(sampleData, password)
        // Corrupt first magic byte
        encrypted[0] = 'X'.code.toByte()

        try {
            DatabaseBackupManager.decryptData(encrypted, password)
            fail("Expected IllegalArgumentException on invalid magic header")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Invalid backup format"))
        }
    }

    @Test
    fun testIcsCalendarGeneration() {
        val event1 = IcsCalendarExporter.createMeetingOverlapEvent(
            sourceZone = "America/New_York",
            targetZone = "Asia/Tokyo",
            startEpochMillis = 1700000000000L,
            endEpochMillis = 1700003600000L,
            overlapQuality = "Green Working Hours"
        )
        val event2 = IcsCalendarExporter.CalendarEvent(
            title = "Standup, Sync & Review",
            description = "Daily cross-timezone check-in; priority: high.",
            startEpochMillis = 1700010000000L,
            endEpochMillis = 1700013600000L,
            location = "London / Sydney"
        )

        val icsString = IcsCalendarExporter.generateIcs(listOf(event1, event2))

        assertTrue(icsString.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(icsString.contains("VERSION:2.0\r\n"))
        assertTrue(icsString.contains("PRODID:-//SeeTime//Timezone Overlap Engine//EN\r\n"))
        assertTrue(icsString.contains("BEGIN:VEVENT\r\n"))
        assertTrue(icsString.contains("SUMMARY:SeeTime Meeting: America/New_York / Asia/Tokyo\r\n"))
        assertTrue(icsString.contains("SUMMARY:Standup\\, Sync & Review\r\n"))
        assertTrue(icsString.contains("DESCRIPTION:Daily cross-timezone check-in\\; priority: high.\r\n"))
        assertTrue(icsString.contains("STATUS:CONFIRMED\r\n"))
        assertTrue(icsString.endsWith("END:VCALENDAR\r\n"))
    }

    @Test
    fun testCsvTimeExportAndEscaping() {
        val activities = listOf(
            Activity(
                id = 1L,
                label = "Sprint Planning, Team Discussion",
                startTimeMillis = 1700000000000L,
                endTimeMillis = 1700003600000L, // 60 mins
                category = "Work & Projects"
            ),
            Activity(
                id = 2L,
                label = "Client \"Deep Dive\" Call",
                startTimeMillis = 1700010000000L,
                endTimeMillis = 1700011800000L, // 30 mins
                category = "Client Meetings"
            )
        )

        val csv = CsvTimeExporter.exportActivitiesToCsv(activities)

        assertTrue(csv.startsWith("ID,Category,Label,Date,StartTime,EndTime,DurationMinutes\r\n"))
        assertTrue(csv.contains("\"Sprint Planning, Team Discussion\""))
        assertTrue(csv.contains("\"Client \"\"Deep Dive\"\" Call\""))
        assertTrue(csv.contains("60.0"))
        assertTrue(csv.contains("30.0"))
    }
}
