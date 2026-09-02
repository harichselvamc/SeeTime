package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class VoiceAnnouncerManagerTest {

    @Test
    fun `generateAnnouncementScript generates clear English spoken announcements`() {
        val refTime = ZonedDateTime.of(2026, 9, 2, 9, 30, 0, 0, ZoneId.of("Asia/Tokyo"))
        val script = VoiceAnnouncerManager.generateAnnouncementScript(
            cityName = "Tokyo",
            targetZoneId = "Asia/Tokyo",
            language = SupportedVoiceLanguage.ENGLISH,
            localZoneId = "Europe/London", // Tokyo is +8h ahead of London
            referenceTime = refTime
        )

        assertNotNull(script)
        assertTrue(script.contains("Tokyo"))
        assertTrue(script.contains("9:30 AM") || script.contains("9 30 AM"))
        assertTrue(script.contains("ahead of your local time"))
    }

    @Test
    fun `generateAnnouncementScript generates accurate Japanese spoken text`() {
        val refTime = ZonedDateTime.of(2026, 9, 2, 14, 15, 0, 0, ZoneId.of("Asia/Tokyo"))
        val script = VoiceAnnouncerManager.generateAnnouncementScript(
            cityName = "東京",
            targetZoneId = "Asia/Tokyo",
            language = SupportedVoiceLanguage.JAPANESE,
            localZoneId = "Asia/Kolkata",
            referenceTime = refTime
        )

        assertNotNull(script)
        assertTrue(script.contains("東京"))
        assertTrue(script.contains("午後") || script.contains("時"))
    }

    @Test
    fun `generateAnnouncementScript generates French, Spanish and German announcements`() {
        val refTime = ZonedDateTime.of(2026, 9, 2, 10, 0, 0, 0, ZoneId.of("Europe/Paris"))

        val frScript = VoiceAnnouncerManager.generateAnnouncementScript("Paris", "Europe/Paris", SupportedVoiceLanguage.FRENCH, "UTC", refTime)
        assertTrue(frScript.contains("Paris"))
        assertTrue(frScript.contains("heures"))

        val esScript = VoiceAnnouncerManager.generateAnnouncementScript("Madrid", "Europe/Madrid", SupportedVoiceLanguage.SPANISH, "UTC", refTime)
        assertTrue(esScript.contains("Madrid"))
        assertTrue(esScript.contains("son las") || esScript.contains("horas"))

        val deScript = VoiceAnnouncerManager.generateAnnouncementScript("Berlin", "Europe/Berlin", SupportedVoiceLanguage.GERMAN, "UTC", refTime)
        assertTrue(deScript.contains("Berlin"))
        assertTrue(deScript.contains("Uhr"))
    }

    @Test
    fun `all supported voice languages have valid locales and emojis`() {
        assertEquals(5, SupportedVoiceLanguage.values().size)
        SupportedVoiceLanguage.values().forEach { lang ->
            assertNotNull(lang.code)
            assertNotNull(lang.locale)
            assertNotNull(lang.displayName)
            assertNotNull(lang.flagEmoji)
        }
    }
}
