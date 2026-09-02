package com.harichselvamc.seetime.util

import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

data class QuartzDriftResult(
    val temperatureCelsius: Float,
    val ppmDrift: Double, // Parts per million
    val dailyDriftSeconds: Double,
    val monthlyDriftSeconds: Double,
    val frequencyHz: Double,
    val isOptimalTemp: Boolean
)

data class NtpSyncResult(
    val serverHost: String,
    val stratumTier: Int,
    val roundTripDelayMs: Double,
    val clockOffsetMs: Double,
    val jitterMs: Double,
    val syncTimestamp: Instant,
    val statusLabel: String
)

object AtomicClockEngine {

    const val CESIUM_133_FREQUENCY_HZ = 9192631770L // Exact SI definition of 1 second
    const val QUARTZ_NOMINAL_HZ = 32768.0 // Standard 32.768 kHz watch crystal
    const val QUARTZ_TURNOVER_TEMP_C = 25.0 // Peak accuracy at 25°C room temperature
    const val QUARTZ_PARABOLIC_COEFF = 0.035 // ppm/°C²

    val NTP_SERVERS = listOf(
        Pair("time.nist.gov (NIST Atomic)", 1),
        Pair("time.google.com (Google Stratum 1)", 1),
        Pair("time.cloudflare.com (Cloudflare)", 1),
        Pair("pool.ntp.org (Global NTP Pool)", 2),
        Pair("time.apple.com (Apple NTP)", 1)
    )

    /**
     * Calculates the exact number of quantum Cesium-133 oscillations for a given second and millisecond.
     */
    fun calculateCesiumOscillations(secondOfMinute: Int, millisecond: Int): Long {
        val safeSec = secondOfMinute.coerceIn(0, 59)
        val safeMs = millisecond.coerceIn(0, 999)

        val fullSecondOscillations = safeSec * CESIUM_133_FREQUENCY_HZ
        val fractionOscillations = (safeMs / 1000.0 * CESIUM_133_FREQUENCY_HZ).toLong()

        return fullSecondOscillations + fractionOscillations
    }

    /**
     * Calculates quartz crystal thermal drift due to temperature deviations from 25°C.
     */
    fun calculateQuartzThermalDrift(temperatureC: Float): QuartzDriftResult {
        val deltaT = (temperatureC - QUARTZ_TURNOVER_TEMP_C).toDouble()
        // Parabolic curve: delta_f / f = -k * (T - T0)^2 (in PPM)
        val ppm = -QUARTZ_PARABOLIC_COEFF * deltaT.pow(2.0)

        val dailyDriftSec = (ppm / 1_000_000.0) * 86400.0
        val monthlyDriftSec = dailyDriftSec * 30.4375

        val actualFreq = QUARTZ_NOMINAL_HZ * (1.0 + (ppm / 1_000_000.0))
        val isOptimal = kotlin.math.abs(deltaT) < 3.0

        return QuartzDriftResult(
            temperatureCelsius = temperatureC,
            ppmDrift = ppm,
            dailyDriftSeconds = dailyDriftSec,
            monthlyDriftSeconds = monthlyDriftSec,
            frequencyHz = actualFreq,
            isOptimalTemp = isOptimal
        )
    }

    /**
     * Simulates a 4-timestamp RFC 5905 Network Time Protocol (NTP) round-trip synchronization packet exchange.
     */
    fun simulateNtpSync(serverHost: String = "time.nist.gov", stratum: Int = 1): NtpSyncResult {
        val nowMs = System.currentTimeMillis()

        // Simulate network round-trip packet transmission:
        // T1 = Client Send, T2 = Server Recv, T3 = Server Transmit, T4 = Client Recv
        val rtt = Random.nextDouble(12.0, 38.0) // 12-38 ms realistic RTT
        val serverProcessing = Random.nextDouble(0.2, 1.5) // Server internal latency
        val localOffset = Random.nextDouble(-3.5, 3.5) // Local clock divergence in ms
        val jitter = Random.nextDouble(0.4, 1.8)

        val t1 = nowMs.toDouble()
        val t2 = t1 + (rtt / 2.0) + localOffset
        val t3 = t2 + serverProcessing
        val t4 = t1 + rtt + serverProcessing

        // Standard NTP RFC 5905 formulas:
        val measuredDelay = (t4 - t1) - (t3 - t2)
        val measuredOffset = ((t2 - t1) + (t3 - t4)) / 2.0

        val status = if (kotlin.math.abs(measuredOffset) < 1.0) "Synchronized (Sub-Millisecond)"
                     else "Synchronized (±${String.format(Locale.US, "%.1f", kotlin.math.abs(measuredOffset))}ms)"

        return NtpSyncResult(
            serverHost = serverHost,
            stratumTier = stratum,
            roundTripDelayMs = measuredDelay,
            clockOffsetMs = measuredOffset,
            jitterMs = jitter,
            syncTimestamp = Instant.ofEpochMilli(nowMs),
            statusLabel = status
        )
    }
}
