package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MorseTimeEngineTest {

    @Test
    fun `textToMorseNotation accurately encodes SOS and time strings`() {
        val sosMorse = MorseTimeEngine.textToMorseNotation("SOS")
        assertEquals("... --- ...", sosMorse)

        val timeMorse = MorseTimeEngine.textToMorseNotation("TIME")
        assertEquals("- .. -- .", timeMorse)
    }

    @Test
    fun `encodeTextToPulses generates precise timing pulses with Farnsworth intervals`() {
        // At 20 WPM, unitMs = 1200 / 20 = 60 ms
        val pulses = MorseTimeEngine.encodeTextToPulses("SOS", 20)

        assertTrue(pulses.isNotEmpty())

        // S is 3 dits (3 high pulses of 60ms)
        // O is 3 dahs (3 high pulses of 180ms)
        // S is 3 dits (3 high pulses of 60ms)
        val highPulses = pulses.filter { it.isHigh }
        assertEquals(9, highPulses.size)

        // First 3 are dits (60ms)
        assertEquals(60L, highPulses[0].durationMs)
        assertEquals(60L, highPulses[1].durationMs)
        assertEquals(60L, highPulses[2].durationMs)

        // Next 3 are dahs (180ms)
        assertEquals(180L, highPulses[3].durationMs)
        assertEquals(180L, highPulses[4].durationMs)
        assertEquals(180L, highPulses[5].durationMs)
    }

    @Test
    fun `calculateTotalDurationMs calculates positive aggregate duration`() {
        val pulses = MorseTimeEngine.encodeTextToPulses("TIME 14:30", 15)
        val totalMs = MorseTimeEngine.calculateTotalDurationMs(pulses)

        assertTrue(totalMs > 1000L) // At least 1 second
    }

    @Test
    fun `cheat sheet entries cover alphabet, digits and punctuation`() {
        assertTrue(MorseTimeEngine.CHEAT_SHEET_ENTRIES.isNotEmpty())
        val chars = MorseTimeEngine.CHEAT_SHEET_ENTRIES.map { it.char }

        assertTrue(chars.contains('A'))
        assertTrue(chars.contains('Z'))
        assertTrue(chars.contains('0'))
        assertTrue(chars.contains('9'))
        assertTrue(chars.contains(':'))
    }
}
