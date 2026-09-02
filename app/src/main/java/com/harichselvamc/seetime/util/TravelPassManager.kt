package com.harichselvamc.seetime.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class PassType(val displayName: String, val iconName: String) {
    FLIGHT("Flight Boarding Pass", "flight"),
    TRAIN("High-Speed Rail / Train", "train"),
    HOTEL("Hotel & Accommodation", "hotel"),
    EVENT("Event & Conference Pass", "confirmation_number")
}

enum class BarcodeFormat {
    QR_CODE,
    PDF417,
    AZTEC,
    CODE128
}

data class TravelPass(
    val id: String = UUID.randomUUID().toString(),
    val passType: PassType = PassType.FLIGHT,
    val carrierName: String,
    val carrierCode: String,
    val serviceNumber: String, // e.g. "005" for JL005
    val passengerName: String,
    val originCode: String, // e.g. "JFK"
    val originCity: String, // e.g. "New York"
    val originZoneId: String, // e.g. "America/New_York"
    val destCode: String, // e.g. "HND"
    val destCity: String, // e.g. "Tokyo"
    val destZoneId: String, // e.g. "Asia/Tokyo"
    val departureEpochMillis: Long,
    val arrivalEpochMillis: Long,
    val gate: String = "B24",
    val terminal: String = "4",
    val seat: String = "12A",
    val boardingGroup: String = "Group 1",
    val bookingReference: String = "7XQ9LM",
    val barcodeFormat: BarcodeFormat = BarcodeFormat.QR_CODE,
    val barcodePayload: String = "M1SEETIMER/HARIC  EJLK889 JFKHNDJL 005 240Y012A0024 100",
    val isPinned: Boolean = false,
    val notes: String = ""
)

data class FlightCountdownStatus(
    val hoursRemaining: Long,
    val minutesRemaining: Long,
    val secondsRemaining: Long,
    val isDeparted: Boolean,
    val isLanded: Boolean,
    val isInFlight: Boolean,
    val statusLabel: String,
    val departureFormattedOrigin: String,
    val departureFormattedDest: String,
    val arrivalFormattedDest: String,
    val arrivalFormattedOrigin: String,
    val flightDurationFormatted: String,
    val timeDifferenceHours: Int
)

object TravelPassManager {

    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a (MMM d)")

    /**
     * Calculates the countdown and dual-timezone flight schedule HUD.
     */
    fun calculateCountdown(
        pass: TravelPass,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): FlightCountdownStatus {
        val depInstant = Instant.ofEpochMilli(pass.departureEpochMillis)
        val arrInstant = Instant.ofEpochMilli(pass.arrivalEpochMillis)
        val nowInstant = Instant.ofEpochMilli(nowEpochMillis)

        val originZone = try { ZoneId.of(pass.originZoneId) } catch (_: Exception) { ZoneId.systemDefault() }
        val destZone = try { ZoneId.of(pass.destZoneId) } catch (_: Exception) { ZoneId.systemDefault() }

        val depZdtOrigin = ZonedDateTime.ofInstant(depInstant, originZone)
        val depZdtDest = ZonedDateTime.ofInstant(depInstant, destZone)
        val arrZdtDest = ZonedDateTime.ofInstant(arrInstant, destZone)
        val arrZdtOrigin = ZonedDateTime.ofInstant(arrInstant, originZone)

        val duration = Duration.between(depInstant, arrInstant)
        val durationHours = duration.toHours()
        val durationMins = duration.toMinutes() % 60
        val durationStr = "${durationHours}h ${durationMins}m"

        val originOffset = originZone.rules.getOffset(depInstant).totalSeconds / 3600
        val destOffset = destZone.rules.getOffset(depInstant).totalSeconds / 3600
        val timeDiff = destOffset - originOffset

        val isDeparted = nowInstant.isAfter(depInstant)
        val isLanded = nowInstant.isAfter(arrInstant)
        val isInFlight = isDeparted && !isLanded

        val (hoursRem, minsRem, secsRem, statusLabel) = when {
            isLanded -> {
                val past = Duration.between(arrInstant, nowInstant)
                val h = past.toHours()
                val m = past.toMinutes() % 60
                val s = past.seconds % 60
                listOf(h, m, s, "Landed ${h}h ${m}m ago in ${pass.destCity}")
            }
            isInFlight -> {
                val flightLeft = Duration.between(nowInstant, arrInstant)
                val h = flightLeft.toHours()
                val m = flightLeft.toMinutes() % 60
                val s = flightLeft.seconds % 60
                listOf(h, m, s, "In-Flight: ${h}h ${m}m to Arrival")
            }
            else -> {
                val untilDep = Duration.between(nowInstant, depInstant)
                val h = untilDep.toHours()
                val m = untilDep.toMinutes() % 60
                val s = untilDep.seconds % 60
                val label = if (h < 2) "Boarding Soon: Gate ${pass.gate}" else "Departing in ${h}h ${m}m"
                listOf(h, m, s, label)
            }
        }

        return FlightCountdownStatus(
            hoursRemaining = hoursRem as Long,
            minutesRemaining = minsRem as Long,
            secondsRemaining = secsRem as Long,
            isDeparted = isDeparted,
            isLanded = isLanded,
            isInFlight = isInFlight,
            statusLabel = statusLabel as String,
            departureFormattedOrigin = depZdtOrigin.format(TIME_FORMATTER),
            departureFormattedDest = depZdtDest.format(TIME_FORMATTER),
            arrivalFormattedDest = arrZdtDest.format(TIME_FORMATTER),
            arrivalFormattedOrigin = arrZdtOrigin.format(TIME_FORMATTER),
            flightDurationFormatted = durationStr,
            timeDifferenceHours = timeDiff
        )
    }

    /**
     * Pure algorithmic 2D binary matrix generator for Barcode/QR visualization.
     * Generates a deterministic, high-contrast bitmatrix (true = black dot, false = white).
     */
    fun generateBarcodeMatrix(
        payload: String,
        format: BarcodeFormat,
        gridSize: Int = 29
    ): Array<BooleanArray> {
        val size = when (format) {
            BarcodeFormat.QR_CODE -> 29
            BarcodeFormat.AZTEC -> 27
            BarcodeFormat.PDF417 -> 35
            BarcodeFormat.CODE128 -> 45
        }

        val matrix = Array(size) { BooleanArray(size) { false } }
        val hash = payload.hashCode()

        // 1. Finder pattern locators (for QR Code corners)
        if (format == BarcodeFormat.QR_CODE || format == BarcodeFormat.AZTEC) {
            drawFinderPattern(matrix, 0, 0, 7)
            drawFinderPattern(matrix, size - 7, 0, 7)
            drawFinderPattern(matrix, 0, size - 7, 7)

            // Timing patterns
            for (i in 7 until size - 7) {
                matrix[6][i] = (i % 2 == 0)
                matrix[i][6] = (i % 2 == 0)
            }
        } else if (format == BarcodeFormat.PDF417 || format == BarcodeFormat.CODE128) {
            // Stacked / 1D bar stripes
            val bytes = payload.toByteArray()
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val b = if (bytes.isNotEmpty()) bytes[(col + row * 2) % bytes.size].toInt() else col
                    matrix[row][col] = ((b xor (col * 7)) % 3 == 0) || (col % 4 == 0)
                }
            }
            return matrix
        }

        // 2. Deterministic pseudo-payload encoding
        val bytes = payload.toByteArray()
        var bitIndex = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (isReservedArea(r, c, size)) continue

                val charByte = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() else 0
                val seed = (r * 31 + c * 17 + hash + charByte)
                matrix[r][c] = (seed % 2 == 0) || ((r + c) % 3 == 0)
                bitIndex++
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int, size: Int) {
        for (r in 0 until size) {
            for (c in 0 until size) {
                val row = startR + r
                val col = startC + c
                if (row in matrix.indices && col in matrix[0].indices) {
                    val isOuter = (r == 0 || r == size - 1 || c == 0 || c == size - 1)
                    val isInner = (r in 2 until size - 2 && c in 2 until size - 2)
                    matrix[row][col] = isOuter || isInner
                }
            }
        }
    }

    private fun isReservedArea(r: Int, c: Int, size: Int): Boolean {
        // Top-left finder
        if (r < 8 && c < 8) return true
        // Top-right finder
        if (r >= size - 8 && c < 8) return true
        // Bottom-left finder
        if (r < 8 && c >= size - 8) return true
        // Timing lines
        if (r == 6 || c == 6) return true
        return false
    }
}
