package com.harichselvamc.seetime.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SoundscapeMixerEngineTest {

    private lateinit var engine: SoundscapeMixerEngine

    @Before
    fun setup() {
        engine = SoundscapeMixerEngine(context = null, scope = CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `default presets are complete and properly configured`() {
        val presets = SoundscapeMixerEngine.createDefaultPresets()
        assertEquals(5, presets.size)

        val deepSleep = presets.find { it.name == "Deep Sleep" }
        assertNotNull(deepSleep)
        assertEquals(0.75f, deepSleep!!.volumes[SoundLayer.GENTLE_RAIN] ?: 0f, 0.001f)
        assertEquals(0.50f, deepSleep.volumes[SoundLayer.OCEAN_SURF] ?: 0f, 0.001f)

        val focusStudy = presets.find { it.name == "Focus Study" }
        assertNotNull(focusStudy)
        assertEquals(0.80f, focusStudy!!.volumes[SoundLayer.BINAURAL_ALPHA] ?: 0f, 0.001f)
    }

    @Test
    fun `setTrackVolume updates track volume and clears active preset name`() {
        engine.setTrackVolume(SoundLayer.OCEAN_SURF, 0.95f)
        assertEquals(0.95f, engine.trackVolumes.value[SoundLayer.OCEAN_SURF] ?: 0f, 0.001f)
        assertEquals(null, engine.activePresetName.value)
    }

    @Test
    fun `applyPreset updates all 6 channel volumes and active preset name`() {
        val focusPreset = SoundscapeMixerEngine.createDefaultPresets().first { it.name == "Focus Study" }
        engine.applyPreset(focusPreset)

        assertEquals("Focus Study", engine.activePresetName.value)
        assertEquals(0.80f, engine.trackVolumes.value[SoundLayer.BINAURAL_ALPHA] ?: 0f, 0.001f)
        assertEquals(0.60f, engine.trackVolumes.value[SoundLayer.PINK_NOISE] ?: 0f, 0.001f)
        assertEquals(0.0f, engine.trackVolumes.value[SoundLayer.OCEAN_SURF] ?: 0f, 0.001f)
    }

    @Test
    fun `master volume clamp and mute toggle`() {
        engine.setMasterVolume(1.5f)
        assertEquals(1.0f, engine.masterVolume.value, 0.001f)

        engine.setMasterVolume(-0.2f)
        assertEquals(0.0f, engine.masterVolume.value, 0.001f)

        engine.setMasterVolume(0.75f)
        assertFalse(engine.isMuted.value)

        engine.toggleMute()
        assertTrue(engine.isMuted.value)

        engine.toggleMute()
        assertFalse(engine.isMuted.value)
    }

    @Test
    fun `save and delete custom presets`() {
        engine.setTrackVolume(SoundLayer.CAMPFIRE, 0.88f)
        engine.setTrackVolume(SoundLayer.NIGHT_WIND, 0.77f)

        engine.saveCustomPreset("Midnight Cabin")
        val customPresets = engine.customPresets.value
        assertTrue(customPresets.any { it.name == "Midnight Cabin" })

        val saved = customPresets.first { it.name == "Midnight Cabin" }
        assertEquals(0.88f, saved.volumes[SoundLayer.CAMPFIRE] ?: 0f, 0.001f)
        assertEquals(0.77f, saved.volumes[SoundLayer.NIGHT_WIND] ?: 0f, 0.001f)
        assertTrue(saved.isCustom)

        engine.deleteCustomPreset("Midnight Cabin")
        assertFalse(engine.customPresets.value.any { it.name == "Midnight Cabin" })
    }

    @Test
    fun `sleep timer start, duration calculation and cancel`() = runBlocking {
        engine.startSleepTimer(15) // 15 minutes = 900,000 ms
        assertEquals(15 * 60 * 1000L, engine.sleepTimerRemainingMillis.value)
        assertEquals(15 * 60 * 1000L, engine.sleepTimerTotalMillis.value)

        engine.cancelSleepTimer()
        assertEquals(0L, engine.sleepTimerRemainingMillis.value)
        assertEquals(0L, engine.sleepTimerTotalMillis.value)
    }
}
