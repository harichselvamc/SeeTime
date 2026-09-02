package com.harichselvamc.seetime.util

import java.util.Locale

enum class StopwatchState {
    STOPPED,
    RUNNING,
    PAUSED
}

data class LapTelemetry(
    val lapNumber: Int,
    val lapDurationMs: Long,
    val splitTimestampMs: Long,
    val deltaFromPreviousLapMs: Long,
    val isFastest: Boolean,
    val isSlowest: Boolean,
    val formattedLapTime: String,
    val formattedSplitTime: String,
    val deltaBadgeText: String
)

data class StopwatchTelemetrySummary(
    val totalElapsedTimeMs: Long,
    val lapsCount: Int,
    val fastestLapMs: Long?,
    val slowestLapMs: Long?,
    val averageLapMs: Long,
    val formattedTotalTime: String,
    val laps: List<LapTelemetry>
)

object PrecisionStopwatchEngine {

    /**
     * Formats milliseconds into high-precision stopwatch time string (MM:SS.mmm or HH:MM:SS.mmm).
     */
    fun formatElapsed(millis: Long): String {
        val safeMs = millis.coerceAtLeast(0L)
        val hrs = safeMs / 3600000L
        val mins = (safeMs % 3600000L) / 60000L
        val secs = (safeMs % 60000L) / 1000L
        val ms = safeMs % 1000L

        return if (hrs > 0) {
            String.format(Locale.US, "%02d:%02d:%02d.%03d", hrs, mins, secs, ms)
        } else {
            String.format(Locale.US, "%02d:%02d.%03d", mins, secs, ms)
        }
    }

    /**
     * Evaluates a sequence of recorded raw lap split timestamps into full telemetry analysis.
     * [rawLapSplitsMs]: List of cumulative split timestamps (e.g. [12000, 25500, 37000]).
     */
    fun evaluateLapTelemetry(rawLapSplitsMs: List<Long>): StopwatchTelemetrySummary {
        if (rawLapSplitsMs.isEmpty()) {
            return StopwatchTelemetrySummary(
                totalElapsedTimeMs = 0L,
                lapsCount = 0,
                fastestLapMs = null,
                slowestLapMs = null,
                averageLapMs = 0L,
                formattedTotalTime = "00:00.000",
                laps = emptyList()
            )
        }

        val lapDurations = mutableListOf<Long>()
        var prevSplit = 0L
        rawLapSplitsMs.forEach { split ->
            val duration = (split - prevSplit).coerceAtLeast(0L)
            lapDurations.add(duration)
            prevSplit = split
        }

        val fastest = if (lapDurations.size >= 2) lapDurations.minOrNull() else null
        val slowest = if (lapDurations.size >= 2) lapDurations.maxOrNull() else null
        val avgLap = if (lapDurations.isNotEmpty()) lapDurations.average().toLong() else 0L

        val lapsList = mutableListOf<LapTelemetry>()
        var runningPrevLapDuration = 0L

        lapDurations.forEachIndexed { index, duration ->
            val lapNum = index + 1
            val splitTime = rawLapSplitsMs[index]
            val deltaFromPrev = if (index > 0) duration - runningPrevLapDuration else 0L

            val isFast = (fastest != null && duration == fastest && fastest != slowest)
            val isSlow = (slowest != null && duration == slowest && fastest != slowest)

            val deltaText = when {
                index == 0 -> "Baseline"
                deltaFromPrev < 0 -> String.format(Locale.US, "-%.3fs", kotlin.math.abs(deltaFromPrev) / 1000.0)
                deltaFromPrev > 0 -> String.format(Locale.US, "+%.3fs", deltaFromPrev / 1000.0)
                else -> "+0.000s"
            }

            lapsList.add(
                LapTelemetry(
                    lapNumber = lapNum,
                    lapDurationMs = duration,
                    splitTimestampMs = splitTime,
                    deltaFromPreviousLapMs = deltaFromPrev,
                    isFastest = isFast,
                    isSlowest = isSlow,
                    formattedLapTime = formatElapsed(duration),
                    formattedSplitTime = formatElapsed(splitTime),
                    deltaBadgeText = deltaText
                )
            )

            runningPrevLapDuration = duration
        }

        val totalElapsed = rawLapSplitsMs.lastOrNull() ?: 0L

        return StopwatchTelemetrySummary(
            totalElapsedTimeMs = totalElapsed,
            lapsCount = lapsList.size,
            fastestLapMs = fastest,
            slowestLapMs = slowest,
            averageLapMs = avgLap,
            formattedTotalTime = formatElapsed(totalElapsed),
            laps = lapsList.reversed() // Reverse so newest lap appears first
        )
    }

    /**
     * Generates CSV content from lap telemetry for exporting.
     */
    fun generateLapCsv(summary: StopwatchTelemetrySummary): String {
        val sb = StringBuilder()
        sb.appendLine("Lap,Lap Duration (ms),Lap Time,Split Time (ms),Split Time,Delta (ms),Status")

        summary.laps.reversed().forEach { lap ->
            val status = when {
                lap.isFastest -> "FASTEST"
                lap.isSlowest -> "SLOWEST"
                else -> "NORMAL"
            }
            sb.appendLine("${lap.lapNumber},${lap.lapDurationMs},${lap.formattedLapTime},${lap.splitTimestampMs},${lap.formattedSplitTime},${lap.deltaFromPreviousLapMs},$status")
        }

        sb.appendLine()
        sb.appendLine("Total Laps,${summary.lapsCount}")
        sb.appendLine("Total Time (ms),${summary.totalElapsedTimeMs}")
        sb.appendLine("Average Lap (ms),${summary.averageLapMs}")

        return sb.toString()
    }
}
