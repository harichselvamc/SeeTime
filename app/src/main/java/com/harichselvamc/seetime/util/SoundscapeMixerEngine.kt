package com.harichselvamc.seetime.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class SoundLayer(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String
) {
    BINAURAL_ALPHA("binaural", "Binaural Alpha (10 Hz)", "200 Hz carrier with 10 Hz frequency offset for relaxation", "Psychology"),
    PINK_NOISE("pink_noise", "Pink Noise", "Calibrated 1/f acoustic noise for deep mental focus", "GraphicEq"),
    OCEAN_SURF("ocean_surf", "Ocean Surf", "Rhythmic ocean tidal surge and breaking surf waves", "Water"),
    GENTLE_RAIN("gentle_rain", "Gentle Rain", "Acoustic raindrop patter and soothing storm wash", "Cloud"),
    CAMPFIRE("campfire", "Campfire Crackle", "Warm crackling wood logs with gentle ember pops", "LocalFireDepartment"),
    NIGHT_WIND("night_wind", "Night Wind", "Gentle sweeping resonance breeze and tree rustle", "Air")
}

data class SoundscapePreset(
    val name: String,
    val description: String,
    val volumes: Map<SoundLayer, Float>,
    val isCustom: Boolean = false
)

class SoundscapeMixerEngine(
    context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("seetime_soundscape_mixer", Context.MODE_PRIVATE)

    companion object {
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE_SAMPLES = 4096

        @Volatile
        private var INSTANCE: SoundscapeMixerEngine? = null

        fun getInstance(context: Context): SoundscapeMixerEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundscapeMixerEngine(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        fun createDefaultPresets(): List<SoundscapePreset> {
            return listOf(
                SoundscapePreset(
                    name = "Deep Sleep",
                    description = "Soothing rain, rolling ocean surf, and warm embers for restful sleep",
                    volumes = mapOf(
                        SoundLayer.GENTLE_RAIN to 0.75f,
                        SoundLayer.OCEAN_SURF to 0.50f,
                        SoundLayer.PINK_NOISE to 0.25f,
                        SoundLayer.BINAURAL_ALPHA to 0.0f,
                        SoundLayer.CAMPFIRE to 0.20f,
                        SoundLayer.NIGHT_WIND to 0.25f
                    )
                ),
                SoundscapePreset(
                    name = "Focus Study",
                    description = "Binaural 10 Hz alpha brainwaves and calibrated 1/f pink noise for deep cognitive work",
                    volumes = mapOf(
                        SoundLayer.BINAURAL_ALPHA to 0.80f,
                        SoundLayer.PINK_NOISE to 0.60f,
                        SoundLayer.GENTLE_RAIN to 0.30f,
                        SoundLayer.OCEAN_SURF to 0.0f,
                        SoundLayer.CAMPFIRE to 0.0f,
                        SoundLayer.NIGHT_WIND to 0.20f
                    )
                ),
                SoundscapePreset(
                    name = "Ocean Breeze",
                    description = "Immersive breaking coastal waves and gentle sea wind",
                    volumes = mapOf(
                        SoundLayer.OCEAN_SURF to 0.85f,
                        SoundLayer.NIGHT_WIND to 0.55f,
                        SoundLayer.GENTLE_RAIN to 0.15f,
                        SoundLayer.PINK_NOISE to 0.20f,
                        SoundLayer.BINAURAL_ALPHA to 0.0f,
                        SoundLayer.CAMPFIRE to 0.0f
                    )
                ),
                SoundscapePreset(
                    name = "Campfire Serenade",
                    description = "Cozy glowing campfire crackles, distant mountain wind, and soft rain",
                    volumes = mapOf(
                        SoundLayer.CAMPFIRE to 0.85f,
                        SoundLayer.NIGHT_WIND to 0.40f,
                        SoundLayer.GENTLE_RAIN to 0.30f,
                        SoundLayer.PINK_NOISE to 0.0f,
                        SoundLayer.OCEAN_SURF to 0.0f,
                        SoundLayer.BINAURAL_ALPHA to 0.20f
                    )
                ),
                SoundscapePreset(
                    name = "Zen Temple",
                    description = "Meditative harmonic binaural resonance with misty rain and night wind",
                    volumes = mapOf(
                        SoundLayer.BINAURAL_ALPHA to 0.70f,
                        SoundLayer.GENTLE_RAIN to 0.60f,
                        SoundLayer.NIGHT_WIND to 0.35f,
                        SoundLayer.PINK_NOISE to 0.20f,
                        SoundLayer.OCEAN_SURF to 0.20f,
                        SoundLayer.CAMPFIRE to 0.15f
                    )
                )
            )
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.80f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _trackVolumes = MutableStateFlow(
        mapOf(
            SoundLayer.BINAURAL_ALPHA to 0.50f,
            SoundLayer.PINK_NOISE to 0.40f,
            SoundLayer.OCEAN_SURF to 0.60f,
            SoundLayer.GENTLE_RAIN to 0.50f,
            SoundLayer.CAMPFIRE to 0.30f,
            SoundLayer.NIGHT_WIND to 0.30f
        )
    )
    val trackVolumes: StateFlow<Map<SoundLayer, Float>> = _trackVolumes.asStateFlow()

    private val _activePresetName = MutableStateFlow<String?>("Deep Sleep")
    val activePresetName: StateFlow<String?> = _activePresetName.asStateFlow()

    private val _customPresets = MutableStateFlow<List<SoundscapePreset>>(emptyList())
    val customPresets: StateFlow<List<SoundscapePreset>> = _customPresets.asStateFlow()

    private val _sleepTimerRemainingMillis = MutableStateFlow(0L)
    val sleepTimerRemainingMillis: StateFlow<Long> = _sleepTimerRemainingMillis.asStateFlow()

    private val _sleepTimerTotalMillis = MutableStateFlow(0L)
    val sleepTimerTotalMillis: StateFlow<Long> = _sleepTimerTotalMillis.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var synthThread: Thread? = null
    private var sleepTimerJob: Job? = null

    init {
        loadCustomPresets()
    }

    fun setTrackVolume(layer: SoundLayer, vol: Float) {
        val updated = _trackVolumes.value.toMutableMap()
        updated[layer] = vol.coerceIn(0f, 1f)
        _trackVolumes.value = updated
        _activePresetName.value = null // Custom modification
    }

    fun setMasterVolume(vol: Float) {
        _masterVolume.value = vol.coerceIn(0f, 1f)
        updateAudioTrackVolume()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        updateAudioTrackVolume()
    }

    fun applyPreset(preset: SoundscapePreset) {
        _trackVolumes.value = preset.volumes
        _activePresetName.value = preset.name
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        startAudioStream()
    }

    fun pause() {
        _isPlaying.value = false
        stopAudioStream()
    }

    fun stop() {
        _isPlaying.value = false
        cancelSleepTimer()
        stopAudioStream()
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalMillis = minutes * 60 * 1000L
        _sleepTimerTotalMillis.value = totalMillis
        _sleepTimerRemainingMillis.value = totalMillis

        if (!_isPlaying.value) {
            play()
        }

        sleepTimerJob = scope.launch {
            val startTime = System.currentTimeMillis()
            val initialMaster = _masterVolume.value

            while (isActive && _sleepTimerRemainingMillis.value > 0) {
                delay(1000L)
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = max(0L, totalMillis - elapsed)
                _sleepTimerRemainingMillis.value = remaining

                // Exponential fade out during the final 3 minutes (or final 25% of total time)
                val fadeWindow = min(3 * 60 * 1000L, (totalMillis * 0.25).toLong())
                if (remaining <= fadeWindow && fadeWindow > 0) {
                    val fadeRatio = remaining.toFloat() / fadeWindow
                    val exponentialGain = (exp(fadeRatio) - 1f) / (exp(1f) - 1f)
                    val fadedVolume = initialMaster * exponentialGain
                    setMasterVolume(fadedVolume)
                }
            }

            if (isActive && _sleepTimerRemainingMillis.value == 0L) {
                stop()
                _masterVolume.value = initialMaster // Reset volume for next session
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMillis.value = 0L
        _sleepTimerTotalMillis.value = 0L
    }

    fun saveCustomPreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val newPreset = SoundscapePreset(
            name = trimmed,
            description = "Custom saved user mix",
            volumes = _trackVolumes.value,
            isCustom = true
        )

        _customPresets.value = listOf(newPreset) + _customPresets.value.filterNot { it.name == trimmed }
        _activePresetName.value = trimmed
        persistCustomPresets()
    }

    fun deleteCustomPreset(name: String) {
        _customPresets.value = _customPresets.value.filterNot { it.name == name }
        persistCustomPresets()
    }

    private fun updateAudioTrackVolume() {
        val actualVol = if (_isMuted.value) 0f else _masterVolume.value
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack?.setVolume(actualVol)
            } else {
                @Suppress("DEPRECATION")
                audioTrack?.setStereoVolume(actualVol, actualVol)
            }
        } catch (_: Exception) {}
    }

    private fun startAudioStream() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, BUFFER_SIZE_SAMPLES * 4)

            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioTrack(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build(),
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            updateAudioTrackVolume()
            audioTrack?.play()
        } catch (_: Exception) {
            // Stream init safety
            return
        }

        synthThread = Thread {
            val random = Random()
            val shortBuffer = ShortArray(BUFFER_SIZE_SAMPLES * 2)

            // Oscillator phases
            var binauralPhaseL = 0.0
            var binauralPhaseR = 0.0
            var oceanPhase = 0.0
            var rainPhase = 0.0
            var windPhase = 0.0

            // Pink noise filter state
            var b0 = 0.0
            var b1 = 0.0
            var b2 = 0.0
            var b3 = 0.0
            var b4 = 0.0
            var b5 = 0.0
            var b6 = 0.0

            // Low-pass states for Ocean, Rain, Campfire, Wind
            var oceanLp = 0.0
            var rainLp = 0.0
            var campfireLp = 0.0
            var windLp = 0.0

            while (_isPlaying.value) {
                val volumes = _trackVolumes.value
                val volBinaural = volumes[SoundLayer.BINAURAL_ALPHA] ?: 0f
                val volPink = volumes[SoundLayer.PINK_NOISE] ?: 0f
                val volOcean = volumes[SoundLayer.OCEAN_SURF] ?: 0f
                val volRain = volumes[SoundLayer.GENTLE_RAIN] ?: 0f
                val volCampfire = volumes[SoundLayer.CAMPFIRE] ?: 0f
                val volWind = volumes[SoundLayer.NIGHT_WIND] ?: 0f

                for (i in 0 until BUFFER_SIZE_SAMPLES) {
                    var sampleL = 0.0
                    var sampleR = 0.0

                    // 1. Binaural Alpha (200 Hz Left, 210 Hz Right -> 10 Hz Beat)
                    if (volBinaural > 0.001f) {
                        val sL = sin(binauralPhaseL) * volBinaural * 0.35
                        val sR = sin(binauralPhaseR) * volBinaural * 0.35
                        sampleL += sL
                        sampleR += sR

                        binauralPhaseL += 2.0 * PI * 200.0 / SAMPLE_RATE
                        binauralPhaseR += 2.0 * PI * 210.0 / SAMPLE_RATE
                        if (binauralPhaseL >= 2.0 * PI) binauralPhaseL -= 2.0 * PI
                        if (binauralPhaseR >= 2.0 * PI) binauralPhaseR -= 2.0 * PI
                    }

                    // 2. Pink Noise (Paul Kellet filter)
                    if (volPink > 0.001f) {
                        val white = (random.nextFloat() * 2f - 1f)
                        b0 = 0.99886 * b0 + white * 0.0555179
                        b1 = 0.99332 * b1 + white * 0.0750759
                        b2 = 0.96900 * b2 + white * 0.1538520
                        b3 = 0.86650 * b3 + white * 0.3104856
                        b4 = 0.55000 * b4 + white * 0.5329522
                        b5 = -0.7616 * b5 - white * 0.0168980
                        val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11 * volPink
                        b6 = white * 0.115926

                        sampleL += pink
                        sampleR += pink
                    }

                    // 3. Ocean Surf (0.12 Hz sinusoidal wave surge envelope on filtered noise)
                    if (volOcean > 0.001f) {
                        val white = (random.nextFloat() * 2f - 1f)
                        oceanLp = oceanLp * 0.96 + white * 0.04
                        val surgeEnvelope = (0.5 + 0.5 * sin(oceanPhase))
                        val oceanSample = oceanLp * (0.2 + 0.8 * surgeEnvelope * surgeEnvelope) * volOcean * 0.60
                        sampleL += oceanSample * 0.9
                        sampleR += oceanSample * 1.1

                        oceanPhase += 2.0 * PI * 0.12 / SAMPLE_RATE // 12-second wave period
                        if (oceanPhase >= 2.0 * PI) oceanPhase -= 2.0 * PI
                    }

                    // 4. Gentle Rain (Poisson droplet clicks + high-frequency rain wash)
                    if (volRain > 0.001f) {
                        val white = (random.nextFloat() * 2f - 1f)
                        rainLp = rainLp * 0.92 + white * 0.08
                        val rainWash = rainLp * 0.25

                        // Random raindrop impact
                        var droplet = 0.0
                        if (random.nextFloat() < 0.008f) { // ~350 droplets/sec
                            droplet = (random.nextFloat() * 2f - 1f) * 0.40
                        }

                        val rainTotal = (rainWash + droplet) * volRain
                        sampleL += rainTotal
                        sampleR += rainTotal
                    }

                    // 5. Campfire Crackle (Low ember rumble + sporadic high-frequency burst pops)
                    if (volCampfire > 0.001f) {
                        val white = (random.nextFloat() * 2f - 1f)
                        campfireLp = campfireLp * 0.98 + white * 0.02
                        val rumble = campfireLp * 0.20

                        var cracklePop = 0.0
                        if (random.nextFloat() < 0.003f) { // Sporadic pops
                            cracklePop = (random.nextFloat() * 2f - 1f) * 0.70
                        }

                        val fireTotal = (rumble + cracklePop) * volCampfire
                        sampleL += fireTotal
                        sampleR += fireTotal
                    }

                    // 6. Night Wind (0.05 Hz sweeping resonance breeze)
                    if (volWind > 0.001f) {
                        val white = (random.nextFloat() * 2f - 1f)
                        val windCutoff = 0.90 + 0.08 * sin(windPhase)
                        windLp = windLp * windCutoff + white * (1.0 - windCutoff)
                        val windSample = windLp * 0.45 * volWind

                        sampleL += windSample * 0.8
                        sampleR += windSample * 1.2

                        windPhase += 2.0 * PI * 0.05 / SAMPLE_RATE // 20-second breeze cycle
                        if (windPhase >= 2.0 * PI) windPhase -= 2.0 * PI
                    }

                    // Convert to 16-bit PCM Stereo with soft saturation clipping
                    val finalL = (sampleL.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                    val finalR = (sampleR.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()

                    shortBuffer[i * 2] = finalL
                    shortBuffer[i * 2 + 1] = finalR
                }

                try {
                    audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                } catch (_: Exception) {
                    break
                }
            }
        }.apply {
            name = "SeeTimeSoundscapeMixerThread"
            priority = Thread.NORM_PRIORITY
            start()
        }
    }

    private fun stopAudioStream() {
        synthThread?.interrupt()
        synthThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun persistCustomPresets() {
        val sp = prefs ?: return
        try {
            val arr = JSONArray()
            for (p in _customPresets.value) {
                val obj = JSONObject().apply {
                    put("name", p.name)
                    put("description", p.description)
                    val volObj = JSONObject()
                    for ((layer, vol) in p.volumes) {
                        volObj.put(layer.name, vol.toDouble())
                    }
                    put("volumes", volObj)
                }
                arr.put(obj)
            }
            sp.edit().putString("custom_soundscapes", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadCustomPresets() {
        val sp = prefs ?: return
        try {
            val raw = sp.getString("custom_soundscapes", "[]") ?: "[]"
            val arr = JSONArray(raw)
            val list = mutableListOf<SoundscapePreset>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val volObj = obj.getJSONObject("volumes")
                val volMap = mutableMapOf<SoundLayer, Float>()
                for (layer in SoundLayer.values()) {
                    if (volObj.has(layer.name)) {
                        volMap[layer] = volObj.getDouble(layer.name).toFloat()
                    }
                }
                list.add(
                    SoundscapePreset(
                        name = obj.getString("name"),
                        description = obj.optString("description", "Custom user mix"),
                        volumes = volMap,
                        isCustom = true
                    )
                )
            }
            _customPresets.value = list
        } catch (_: Exception) {}
    }
}
