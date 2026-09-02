package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class ClockChimeEngineTest {

    private lateinit var engine: ClockChimeEngine

    @Before
    fun setup() {
        engine = ClockChimeEngine(context = null)
    }

    @Test
    fun `all 4 authentic chime melodies are configured`() {
        val melodies = ChimeMelody.values()
        assertEquals(4, melodies.size)

        val westminster = melodies.find { it.id == "westminster" }
        assertNotNull(westminster)
        assertTrue(westminster!!.title.contains("Big Ben"))

        val templeRin = melodies.find { it.id == "temple_rin" }
        assertNotNull(templeRin)
        assertTrue(templeRin!!.description.contains("432 Hz"))
    }

    @Test
    fun `generateBellTone produces valid non-empty PCM samples`() {
        val tonePcm = ClockChimeEngine.generateBellTone(
            fundamentalHz = 440.0,
            durationSec = 0.5,
            decayConstant = 3.0,
            isDeepGong = false,
            sampleRate = 44100
        )

        assertEquals(22050, tonePcm.size) // 0.5 * 44100
        assertTrue(tonePcm.any { it != 0.toShort() })

        // Check deep gong generation
        val gongPcm = ClockChimeEngine.generateBellTone(
            fundamentalHz = ClockChimeEngine.NOTE_E3,
            durationSec = 1.0,
            decayConstant = 1.8,
            isDeepGong = true,
            sampleRate = 44100
        )
        assertEquals(44100, gongPcm.size)
        assertTrue(gongPcm.any { it != 0.toShort() })
    }

    @Test
    fun `shouldChimeAtTime respects quiet hours and hourly timing`() {
        // Quiet hours: 22:00 (10 PM) to 07:00 (7 AM)
        val afternoon = LocalTime.of(14, 0) // 2:00 PM (Hourly)
        assertTrue(engine.shouldChimeAtTime(afternoon, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))

        val afternoonQuarter = LocalTime.of(14, 15) // 2:15 PM (Quarter)
        assertFalse(engine.shouldChimeAtTime(afternoonQuarter, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))
        assertTrue(engine.shouldChimeAtTime(afternoonQuarter, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = true))

        // Nighttime (Quiet hours active)
        val midnight = LocalTime.of(0, 0) // 12:00 AM (Quiet)
        assertFalse(engine.shouldChimeAtTime(midnight, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))

        val lateNight = LocalTime.of(23, 0) // 11:00 PM (Quiet)
        assertFalse(engine.shouldChimeAtTime(lateNight, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))

        val earlyMorning = LocalTime.of(6, 0) // 6:00 AM (Quiet)
        assertFalse(engine.shouldChimeAtTime(earlyMorning, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))

        val morningActive = LocalTime.of(8, 0) // 8:00 AM (Active)
        assertTrue(engine.shouldChimeAtTime(morningActive, isEnabled = true, quietStart = 22, quietEnd = 7, isQuarterEnabled = false))
    }

    @Test
    fun `settings updates volume melody and quiet hours`() {
        engine.setChimeVolume(0.92f)
        assertEquals(0.92f, engine.chimeVolume.value, 0.001f)

        engine.setSelectedMelody(ChimeMelody.TEMPLE_RIN)
        assertEquals(ChimeMelody.TEMPLE_RIN, engine.selectedMelody.value)

        engine.setQuietHours(23, 6)
        assertEquals(23, engine.quietHoursStart.value)
        assertEquals(6, engine.quietHoursEnd.value)
    }
}
