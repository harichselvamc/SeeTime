package com.harichselvamc.seetime.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class ChimeMelody(
    val id: String,
    val title: String,
    val origin: String,
    val description: String
) {
    WESTMINSTER(
        id = "westminster",
        title = "Westminster Quarters (Big Ben)",
        origin = "Palace of Westminster, London (1859)",
        description = "Iconic 4-note E-major progression with deep Big Ben Great Bell hour gong"
    ),
    WHITTINGTON(
        id = "whittington",
        title = "Whittington Chimes",
        origin = "St. Mary-le-Bow, London (14th Century)",
        description = "Traditional 8-bell ringing progression celebrated in English folklore"
    ),
    ST_MICHAEL(
        id = "st_michael",
        title = "St. Michael Chimes",
        origin = "St. Michael's Church, Charleston (1764)",
        description = "Resonant colonial church chimes cast in Whitechapel Foundry"
    ),
    TEMPLE_RIN(
        id = "temple_rin",
        title = "Japanese Temple Rin Bell (432 Hz)",
        origin = "Kyoto Zen Temple",
        description = "Singing bowl harmonic chime tuned to 432 Hz with extended acoustic decay"
    )
}

enum class ChimeQuarter(val displayName: String, val minutes: Int) {
    FIRST_QUARTER("15 Min (Quarter 1)", 15),
    HALF_HOUR("30 Min (Half Hour)", 30),
    THIRD_QUARTER("45 Min (Quarter 3)", 45),
    FULL_HOUR("00 Min (Full Hour)", 0)
}

class ClockChimeEngine(
    context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("seetime_clock_chimes", Context.MODE_PRIVATE)

    companion object {
        const val SAMPLE_RATE = 44100

        // Big Ben Note Frequencies (Hz)
        const val NOTE_GS4 = 415.30 // G#4
        const val NOTE_FS4 = 369.99 // F#4
        const val NOTE_E4 = 329.63  // E4
        const val NOTE_B3 = 246.94  // B3
        const val NOTE_E3 = 164.81  // Big Ben Hour Strike (E3 deep gong)

        @Volatile
        private var INSTANCE: ClockChimeEngine? = null

        fun getInstance(context: Context): ClockChimeEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClockChimeEngine(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        /**
         * Pure algorithmic generator for authentic acoustic bell timbre.
         * Combines strike transient, fundamental, strike tone overtones, and exponential decay envelope.
         */
        fun generateBellTone(
            fundamentalHz: Double,
            durationSec: Double,
            decayConstant: Double = 3.5,
            isDeepGong: Boolean = false,
            sampleRate: Int = SAMPLE_RATE
        ): ShortArray {
            val totalSamples = (durationSec * sampleRate).toInt()
            val buffer = ShortArray(totalSamples)

            val f0 = fundamentalHz
            val fTierce = fundamentalHz * 1.20 // Minor third overtone
            val fQuint = fundamentalHz * 1.50   // Perfect fifth overtone
            val fOctave = fundamentalHz * 2.00  // Nominal octave overtone

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-decayConstant * t)

                // Attack transient: 8ms fast ramp-up
                val attack = (t / 0.008).coerceIn(0.0, 1.0)

                var sample = 0.0
                if (isDeepGong) {
                    // Deep hour gong: heavy fundamental + sub-harmonics
                    sample += sin(2.0 * PI * f0 * t) * 0.65
                    sample += sin(2.0 * PI * (f0 * 0.5) * t) * 0.30
                    sample += sin(2.0 * PI * fTierce * t) * 0.20
                    sample += sin(2.0 * PI * fQuint * t) * 0.15
                    sample += sin(2.0 * PI * fOctave * t) * 0.10
                } else {
                    // Chime bell: clear ringing fundamental + chime harmonics
                    sample += sin(2.0 * PI * f0 * t) * 0.55
                    sample += sin(2.0 * PI * fTierce * t) * 0.25
                    sample += sin(2.0 * PI * fQuint * t) * 0.20
                    sample += sin(2.0 * PI * fOctave * t) * 0.15
                }

                val finalSample = (sample * attack * decay * 0.85 * Short.MAX_VALUE).toInt()
                buffer[i] = finalSample.coerceIn(-32768, 32767).toShort()
            }
            return buffer
        }
    }

    private val _isHourlyChimeEnabled = MutableStateFlow(true)
    val isHourlyChimeEnabled: StateFlow<Boolean> = _isHourlyChimeEnabled.asStateFlow()

    private val _isQuarterChimeEnabled = MutableStateFlow(false)
    val isQuarterChimeEnabled: StateFlow<Boolean> = _isQuarterChimeEnabled.asStateFlow()

    private val _selectedMelody = MutableStateFlow(ChimeMelody.WESTMINSTER)
    val selectedMelody: StateFlow<ChimeMelody> = _selectedMelody.asStateFlow()

    private val _chimeVolume = MutableStateFlow(0.85f)
    val chimeVolume: StateFlow<Float> = _chimeVolume.asStateFlow()

    private val _quietHoursStart = MutableStateFlow(22) // 10 PM
    val quietHoursStart: StateFlow<Int> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow(7) // 7 AM
    val quietHoursEnd: StateFlow<Int> = _quietHoursEnd.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    init {
        loadSettings()
    }

    fun setHourlyChimeEnabled(enabled: Boolean) {
        _isHourlyChimeEnabled.value = enabled
        saveSettings()
    }

    fun setQuarterChimeEnabled(enabled: Boolean) {
        _isQuarterChimeEnabled.value = enabled
        saveSettings()
    }

    fun setSelectedMelody(melody: ChimeMelody) {
        _selectedMelody.value = melody
        saveSettings()
    }

    fun setChimeVolume(vol: Float) {
        _chimeVolume.value = vol.coerceIn(0f, 1f)
        saveSettings()
    }

    fun setQuietHours(startHour: Int, endHour: Int) {
        _quietHoursStart.value = startHour.coerceIn(0, 23)
        _quietHoursEnd.value = endHour.coerceIn(0, 23)
        saveSettings()
    }

    /**
     * Determines whether chimes should ring at [time] respecting quiet hours and quarter settings.
     */
    fun shouldChimeAtTime(
        time: LocalTime,
        isEnabled: Boolean = _isHourlyChimeEnabled.value,
        quietStart: Int = _quietHoursStart.value,
        quietEnd: Int = _quietHoursEnd.value,
        isQuarterEnabled: Boolean = _isQuarterChimeEnabled.value
    ): Boolean {
        if (!isEnabled) return false

        val hour = time.hour
        val minute = time.minute

        // Check Quiet Hours
        val isQuiet = if (quietStart > quietEnd) {
            hour >= quietStart || hour < quietEnd
        } else {
            hour in quietStart until quietEnd
        }

        if (isQuiet) return false

        // Quarter vs Full Hour check
        return if (isQuarterEnabled) {
            minute == 0 || minute == 15 || minute == 30 || minute == 45
        } else {
            minute == 0
        }
    }

    /**
     * Synthesizes and plays a chime progression for [quarter].
     */
    fun playChime(
        melody: ChimeMelody = _selectedMelody.value,
        quarter: ChimeQuarter = ChimeQuarter.FULL_HOUR,
        hourToStrike: Int = 12
    ) {
        if (_isPlayingPreview.value) return

        scope.launch {
            _isPlayingPreview.value = true
            try {
                when (melody) {
                    ChimeMelody.WESTMINSTER -> playWestminster(quarter, hourToStrike)
                    ChimeMelody.WHITTINGTON -> playWhittington(quarter, hourToStrike)
                    ChimeMelody.ST_MICHAEL -> playStMichael(quarter, hourToStrike)
                    ChimeMelody.TEMPLE_RIN -> playTempleRin(hourToStrike)
                }
            } catch (_: Exception) {
            } finally {
                _isPlayingPreview.value = false
            }
        }
    }

    private fun playWestminster(quarter: ChimeQuarter, hourToStrike: Int) {
        val notes = when (quarter) {
            ChimeQuarter.FIRST_QUARTER -> listOf(NOTE_GS4, NOTE_FS4, NOTE_E4, NOTE_B3)
            ChimeQuarter.HALF_HOUR -> listOf(
                NOTE_E4, NOTE_GS4, NOTE_FS4, NOTE_B3,
                NOTE_E4, NOTE_FS4, NOTE_GS4, NOTE_E4
            )
            ChimeQuarter.THIRD_QUARTER -> listOf(
                NOTE_GS4, NOTE_E4, NOTE_FS4, NOTE_B3,
                NOTE_B3, NOTE_FS4, NOTE_GS4, NOTE_E4,
                NOTE_GS4, NOTE_FS4, NOTE_E4, NOTE_B3
            )
            ChimeQuarter.FULL_HOUR -> listOf(
                NOTE_E4, NOTE_GS4, NOTE_FS4, NOTE_B3,
                NOTE_E4, NOTE_FS4, NOTE_GS4, NOTE_E4,
                NOTE_GS4, NOTE_E4, NOTE_FS4, NOTE_B3,
                NOTE_B3, NOTE_FS4, NOTE_GS4, NOTE_E4
            )
        }

        // Play Quarter Chime Notes
        for (freq in notes) {
            playPcmTone(generateBellTone(freq, 0.75, decayConstant = 3.0, isDeepGong = false))
        }

        // Play Hour Strikes if Full Hour
        if (quarter == ChimeQuarter.FULL_HOUR) {
            Thread.sleep(700L) // Pause before hour strikes
            val strikes = ((hourToStrike - 1) % 12) + 1
            for (i in 0 until strikes) {
                playPcmTone(generateBellTone(NOTE_E3, 2.0, decayConstant = 1.8, isDeepGong = true))
            }
        }
    }

    private fun playWhittington(quarter: ChimeQuarter, hourToStrike: Int) {
        val scale = listOf(587.33, 523.25, 493.88, 440.0, 392.0, 349.23, 329.63, 293.66)
        val noteCount = when (quarter) {
            ChimeQuarter.FIRST_QUARTER -> 4
            ChimeQuarter.HALF_HOUR -> 8
            ChimeQuarter.THIRD_QUARTER -> 12
            ChimeQuarter.FULL_HOUR -> 16
        }

        for (i in 0 until noteCount) {
            val freq = scale[i % scale.size]
            playPcmTone(generateBellTone(freq, 0.55, decayConstant = 3.8, isDeepGong = false))
        }

        if (quarter == ChimeQuarter.FULL_HOUR) {
            Thread.sleep(600L)
            val strikes = ((hourToStrike - 1) % 12) + 1
            for (i in 0 until strikes) {
                playPcmTone(generateBellTone(196.0, 1.8, decayConstant = 2.0, isDeepGong = true))
            }
        }
    }

    private fun playStMichael(quarter: ChimeQuarter, hourToStrike: Int) {
        val notes = listOf(440.0, 392.0, 349.23, 329.63, 293.66, 261.63, 293.66, 329.63)
        val count = when (quarter) {
            ChimeQuarter.FIRST_QUARTER -> 4
            ChimeQuarter.HALF_HOUR -> 8
            ChimeQuarter.THIRD_QUARTER -> 12
            ChimeQuarter.FULL_HOUR -> 16
        }

        for (i in 0 until count) {
            val freq = notes[i % notes.size]
            playPcmTone(generateBellTone(freq, 0.60, decayConstant = 3.2, isDeepGong = false))
        }

        if (quarter == ChimeQuarter.FULL_HOUR) {
            Thread.sleep(600L)
            val strikes = ((hourToStrike - 1) % 12) + 1
            for (i in 0 until strikes) {
                playPcmTone(generateBellTone(220.0, 1.9, decayConstant = 1.9, isDeepGong = true))
            }
        }
    }

    private fun playTempleRin(hourToStrike: Int) {
        val strikes = ((hourToStrike - 1) % 12) + 1
        for (i in 0 until strikes.coerceAtMost(3)) {
            // 432 Hz fundamental with extended 3.5s decay
            playPcmTone(generateBellTone(432.0, 3.5, decayConstant = 1.0, isDeepGong = false))
        }
    }

    private fun playPcmTone(pcm: ShortArray) {
        try {
            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    pcm.size * 2,
                    AudioTrack.MODE_STATIC
                )
            }

            val vol = _chimeVolume.value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                track.setVolume(vol)
            } else {
                @Suppress("DEPRECATION")
                track.setStereoVolume(vol, vol)
            }

            track.write(pcm, 0, pcm.size)
            track.play()

            val sleepDuration = (pcm.size.toDouble() / SAMPLE_RATE * 1000).toLong()
            Thread.sleep(sleepDuration)

            track.stop()
            track.release()
        } catch (_: Exception) {}
    }

    private fun saveSettings() {
        prefs?.edit()
            ?.putBoolean("hourly_enabled", _isHourlyChimeEnabled.value)
            ?.putBoolean("quarter_enabled", _isQuarterChimeEnabled.value)
            ?.putString("melody", _selectedMelody.value.id)
            ?.putFloat("volume", _chimeVolume.value)
            ?.putInt("quiet_start", _quietHoursStart.value)
            ?.putInt("quiet_end", _quietHoursEnd.value)
            ?.apply()
    }

    private fun loadSettings() {
        val sp = prefs ?: return
        _isHourlyChimeEnabled.value = sp.getBoolean("hourly_enabled", true)
        _isQuarterChimeEnabled.value = sp.getBoolean("quarter_enabled", false)
        val melodyId = sp.getString("melody", "westminster")
        _selectedMelody.value = ChimeMelody.values().find { it.id == melodyId } ?: ChimeMelody.WESTMINSTER
        _chimeVolume.value = sp.getFloat("volume", 0.85f)
        _quietHoursStart.value = sp.getInt("quiet_start", 22)
        _quietHoursEnd.value = sp.getInt("quiet_end", 7)
    }
}
