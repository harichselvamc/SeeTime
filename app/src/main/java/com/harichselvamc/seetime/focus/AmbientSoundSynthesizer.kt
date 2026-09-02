package com.harichselvamc.seetime.focus

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class SoundPreset(val title: String, val description: String) {
    OFF("Silent Focus", "No ambient sound"),
    PINK_NOISE("Pink Noise", "Calibrated 1/f noise for deep cognitive focus"),
    BINAURAL_ALPHA_BEATS("Binaural Alpha (10 Hz)", "200 Hz carrier with 10 Hz frequency offset for relaxed focus"),
    RAIN_WAVES("Gentle Rain", "Acoustic rhythmic rain wave simulation"),
    WHITE_NOISE("White Noise", "Steady broadband sound masking background chatter")
}

class AmbientSoundSynthesizer {

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var activePreset = SoundPreset.OFF
    private var synthThread: Thread? = null
    private var volume = 0.6f

    companion object {
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE_SAMPLES = 4096
    }

    fun getActivePreset(): SoundPreset = activePreset

    fun isPlaying(): Boolean = isPlaying

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioTrack?.setVolume(volume)
            } else {
                @Suppress("DEPRECATION")
                audioTrack?.setStereoVolume(volume, volume)
            }
        } catch (_: Exception) {}
    }

    fun play(preset: SoundPreset) {
        if (preset == SoundPreset.OFF) {
            stop()
            return
        }

        stop()
        activePreset = preset
        isPlaying = true

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

        setVolume(volume)
        audioTrack?.play()

        synthThread = Thread {
            val random = Random()
            val shortBuffer = ShortArray(BUFFER_SIZE_SAMPLES * 2) // Stereo: Left & Right
            var phaseLeft = 0.0
            var phaseRight = 0.0
            var rainPhase = 0.0

            // Pink noise filter state
            var b0 = 0.0
            var b1 = 0.0
            var b2 = 0.0
            var b3 = 0.0
            var b4 = 0.0
            var b5 = 0.0
            var b6 = 0.0

            while (isPlaying) {
                for (i in 0 until BUFFER_SIZE_SAMPLES) {
                    when (activePreset) {
                        SoundPreset.WHITE_NOISE -> {
                            val sample = ((random.nextFloat() * 2f - 1f) * 0.3f * Short.MAX_VALUE).toInt().toShort()
                            shortBuffer[i * 2] = sample
                            shortBuffer[i * 2 + 1] = sample
                        }
                        SoundPreset.PINK_NOISE -> {
                            // Paul Kellet's filtered pink noise algorithm
                            val white = (random.nextFloat() * 2f - 1f)
                            b0 = 0.99886 * b0 + white * 0.0555179
                            b1 = 0.99332 * b1 + white * 0.0750759
                            b2 = 0.96900 * b2 + white * 0.1538520
                            b3 = 0.86650 * b3 + white * 0.3104856
                            b4 = 0.55000 * b4 + white * 0.5329522
                            b5 = -0.7616 * b5 - white * 0.0168980
                            val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.12
                            b6 = white * 0.115926
                            val sample = (pink * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
                            shortBuffer[i * 2] = sample
                            shortBuffer[i * 2 + 1] = sample
                        }
                        SoundPreset.BINAURAL_ALPHA_BEATS -> {
                            // 200 Hz carrier on left, 210 Hz on right -> 10 Hz perceived alpha frequency
                            val freqLeft = 200.0
                            val freqRight = 210.0
                            val sampleLeft = (sin(phaseLeft) * 0.35 * Short.MAX_VALUE).toInt().toShort()
                            val sampleRight = (sin(phaseRight) * 0.35 * Short.MAX_VALUE).toInt().toShort()

                            phaseLeft += 2.0 * PI * freqLeft / SAMPLE_RATE
                            phaseRight += 2.0 * PI * freqRight / SAMPLE_RATE

                            if (phaseLeft >= 2.0 * PI) phaseLeft -= 2.0 * PI
                            if (phaseRight >= 2.0 * PI) phaseRight -= 2.0 * PI

                            shortBuffer[i * 2] = sampleLeft
                            shortBuffer[i * 2 + 1] = sampleRight
                        }
                        SoundPreset.RAIN_WAVES -> {
                            // Modulated pink/brown noise
                            val white = (random.nextFloat() * 2f - 1f)
                            b0 = 0.995 * b0 + white * 0.05
                            val rainEnvelope = 0.6 + 0.4 * sin(rainPhase)
                            rainPhase += 2.0 * PI * 0.25 / SAMPLE_RATE // 0.25 Hz wave surge
                            if (rainPhase >= 2.0 * PI) rainPhase -= 2.0 * PI

                            val rainSample = (b0 * rainEnvelope * 0.35 * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
                            shortBuffer[i * 2] = rainSample
                            shortBuffer[i * 2 + 1] = rainSample
                        }
                        SoundPreset.OFF -> {
                            shortBuffer[i * 2] = 0
                            shortBuffer[i * 2 + 1] = 0
                        }
                    }
                }

                try {
                    audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                } catch (_: Exception) {
                    break
                }
            }
        }.apply {
            name = "SeeTimeAmbientSynthThread"
            priority = Thread.NORM_PRIORITY
            start()
        }
    }

    fun stop() {
        isPlaying = false
        activePreset = SoundPreset.OFF
        synthThread?.interrupt()
        synthThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun release() {
        stop()
    }
}
