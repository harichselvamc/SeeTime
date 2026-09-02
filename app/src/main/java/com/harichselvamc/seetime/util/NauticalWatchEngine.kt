package com.harichselvamc.seetime.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class NauticalWatchType(
    val title: String,
    val timeSpan: String,
    val startHour: Int,
    val endHour: Int,
    val description: String
) {
    MIDDLE_WATCH("Middle Watch (Graveyard)", "00:00 - 04:00", 0, 4, "Dead of night watch, darkest hours at sea"),
    MORNING_WATCH("Morning Watch (Dawn)", "04:00 - 08:00", 4, 8, "Dawn twilight watch, ship wakes & morning washdown"),
    FORENOON_WATCH("Forenoon Watch", "08:00 - 12:00", 8, 12, "Primary morning duty watch & solar noon meridian sight"),
    AFTERNOON_WATCH("Afternoon Watch", "12:00 - 16:00", 12, 16, "Post-noon watch, regular maintenance & navigation"),
    FIRST_DOG_WATCH("First Dog Watch", "16:00 - 18:00", 16, 18, "Split 2h dog watch to rotate daily watch schedule"),
    LAST_DOG_WATCH("Last Dog Watch", "18:00 - 20:00", 18, 20, "Evening 2h split dog watch prior to nightfall"),
    FIRST_WATCH("First Watch", "20:00 - 00:00", 20, 24, "Night watch following evening dog watches")
}

data class NauticalWatchStatus(
    val watchType: NauticalWatchType,
    val watchName: String,
    val bellCount: Int, // 1..8
    val bellStrikesNotation: String,
    val bellPhoneticText: String,
    val minutesIntoWatch: Int,
    val minutesRemainingInWatch: Int,
    val nextBellTimeFormatted: String,
    val timeFormatted: String
)

data class WatchScheduleRow(
    val timeFormatted: String,
    val watchName: String,
    val bellCount: Int,
    val bellNotation: String
)

object NauticalWatchEngine {

    /**
     * Calculates the traditional nautical bell count (1..8) for any given [hour] (0..23) and [minute] (0..59).
     */
    fun calculateBellCount(hour: Int, minute: Int): Int {
        val isHalfPast = minute >= 30

        return when (hour) {
            0 -> if (isHalfPast) 1 else 8
            1 -> if (isHalfPast) 3 else 2
            2 -> if (isHalfPast) 5 else 4
            3 -> if (isHalfPast) 7 else 6
            4 -> if (isHalfPast) 1 else 8
            5 -> if (isHalfPast) 3 else 2
            6 -> if (isHalfPast) 5 else 4
            7 -> if (isHalfPast) 7 else 6
            8 -> if (isHalfPast) 1 else 8
            9 -> if (isHalfPast) 3 else 2
            10 -> if (isHalfPast) 5 else 4
            11 -> if (isHalfPast) 7 else 6
            12 -> if (isHalfPast) 1 else 8
            13 -> if (isHalfPast) 3 else 2
            14 -> if (isHalfPast) 5 else 4
            15 -> if (isHalfPast) 7 else 6
            // Dog Watches (16:00 to 20:00)
            16 -> if (isHalfPast) 1 else 8
            17 -> if (isHalfPast) 3 else 2
            18 -> if (isHalfPast) 1 else 4
            19 -> if (isHalfPast) 3 else 2
            20 -> if (isHalfPast) 1 else 8
            21 -> if (isHalfPast) 3 else 2
            22 -> if (isHalfPast) 5 else 4
            23 -> if (isHalfPast) 7 else 6
            else -> 8
        }
    }

    /**
     * Identifies the current maritime watch for a given [hour].
     */
    fun getWatchType(hour: Int): NauticalWatchType {
        return when (hour) {
            in 0..3 -> NauticalWatchType.MIDDLE_WATCH
            in 4..7 -> NauticalWatchType.MORNING_WATCH
            in 8..11 -> NauticalWatchType.FORENOON_WATCH
            in 12..15 -> NauticalWatchType.AFTERNOON_WATCH
            in 16..17 -> NauticalWatchType.FIRST_DOG_WATCH
            in 18..19 -> NauticalWatchType.LAST_DOG_WATCH
            else -> NauticalWatchType.FIRST_WATCH
        }
    }

    /**
     * Converts a bell count (1..8) into paired maritime bell symbols (e.g. 🔔🔔  🔔🔔  🔔).
     */
    fun bellsToNotation(bellCount: Int): String {
        val pairs = bellCount / 2
        val single = bellCount % 2

        val sb = StringBuilder()
        for (i in 0 until pairs) {
            sb.append("🔔🔔")
            if (i < pairs - 1 || single > 0) sb.append("  ")
        }
        if (single > 0) {
            sb.append("🔔")
        }
        return sb.toString()
    }

    /**
     * Converts a bell count into phonetic strike rhythm (e.g. "ding-ding, ding-ding, ding").
     */
    fun bellsToPhonetic(bellCount: Int): String {
        val pairs = bellCount / 2
        val single = bellCount % 2

        val parts = mutableListOf<String>()
        for (i in 0 until pairs) {
            parts.add("ding-ding")
        }
        if (single > 0) {
            parts.add("ding")
        }
        return parts.joinToString(", ")
    }

    /**
     * Calculates comprehensive nautical watch and bell status for [localTime].
     */
    fun calculateWatchStatus(localTime: LocalTime = LocalTime.now()): NauticalWatchStatus {
        val h = localTime.hour
        val m = localTime.minute

        val watch = getWatchType(h)
        val bells = calculateBellCount(h, m)
        val notation = bellsToNotation(bells)
        val phonetic = bellsToPhonetic(bells)

        // Calculate progress into watch
        val watchStartMinutes = watch.startHour * 60
        val currentMinutes = (h * 60) + m
        val minutesInto = currentMinutes - watchStartMinutes
        val totalWatchMinutes = (watch.endHour - watch.startHour) * 60
        val minutesRemaining = (totalWatchMinutes - minutesInto).coerceAtLeast(0)

        // Next bell time (:00 or :30)
        val nextBellM = if (m < 30) 30 else 0
        val nextBellH = if (m < 30) h else (h + 1) % 24
        val nextBellStr = String.format(Locale.US, "%02d:%02d", nextBellH, nextBellM)

        val timeStr = String.format(Locale.US, "%02d:%02d", h, m)

        return NauticalWatchStatus(
            watchType = watch,
            watchName = watch.title,
            bellCount = bells,
            bellStrikesNotation = notation,
            bellPhoneticText = phonetic,
            minutesIntoWatch = minutesInto,
            minutesRemainingInWatch = minutesRemaining,
            nextBellTimeFormatted = nextBellStr,
            timeFormatted = timeStr
        )
    }

    /**
     * Generates a 24-hour master timetable of all 48 half-hour nautical bell strikes.
     */
    fun generate24HourTimetable(): List<WatchScheduleRow> {
        val rows = mutableListOf<WatchScheduleRow>()
        for (h in 0 until 24) {
            for (m in listOf(0, 30)) {
                val timeStr = String.format(Locale.US, "%02d:%02d", h, m)
                val watch = getWatchType(h)
                val bells = calculateBellCount(h, m)
                val notation = bellsToNotation(bells)
                rows.add(WatchScheduleRow(timeStr, watch.title, bells, notation))
            }
        }
        return rows
    }

    /**
     * Synthesizes authentic brass ship's bell harmonic chime audio via PCM AudioTrack.
     */
    fun playShipBellAcoustic(bellCount: Int) {
        Thread {
            try {
                val sampleRate = 44100
                val pairs = bellCount / 2
                val single = bellCount % 2

                val strikeDuration = 0.35 // 350ms per bell strike
                val pairGap = 0.15 // 150ms between dings in a pair
                val groupGap = 0.50 // 500ms between pairs

                val totalDuration = (pairs * (2 * strikeDuration + pairGap + groupGap)) + (if (single > 0) strikeDuration + groupGap else 0.0)
                val numSamples = (totalDuration * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                var sampleIndex = 0

                fun renderSingleBellStrike(startIndex: Int) {
                    val strikeSamples = (strikeDuration * sampleRate).toInt()
                    val f1 = 1046.50 // C6 fundamental brass bell
                    val f2 = 1318.51 // E6 major third harmonic
                    val f3 = 1567.98 // G6 fifth overtone

                    for (i in 0 until strikeSamples) {
                        if (startIndex + i >= buffer.size) break
                        val t = i.toDouble() / sampleRate
                        // Exponential decay envelope
                        val envelope = exp(-8.0 * t)
                        val sampleVal = envelope * (
                                0.60 * sin(2 * PI * f1 * t) +
                                0.25 * sin(2 * PI * f2 * t) +
                                0.15 * sin(2 * PI * f3 * t)
                        )
                        buffer[startIndex + i] = (sampleVal * Short.MAX_VALUE * 0.85).toInt().toShort()
                    }
                }

                for (p in 0 until pairs) {
                    // Strike 1 of pair
                    renderSingleBellStrike(sampleIndex)
                    sampleIndex += ((strikeDuration + pairGap) * sampleRate).toInt()

                    // Strike 2 of pair
                    renderSingleBellStrike(sampleIndex)
                    sampleIndex += ((strikeDuration + groupGap) * sampleRate).toInt()
                }

                if (single > 0) {
                    renderSingleBellStrike(sampleIndex)
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
            } catch (_: Exception) {}
        }.start()
    }
}
