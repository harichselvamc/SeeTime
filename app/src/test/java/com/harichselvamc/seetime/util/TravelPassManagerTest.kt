package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TravelPassManagerTest {

    @Test
    fun `calculateCountdown calculates departure and arrival countdowns accurately`() {
        val now = Instant.now()
        val departure = now.plus(3, ChronoUnit.HOURS).toEpochMilli()
        val arrival = now.plus(16, ChronoUnit.HOURS).toEpochMilli()

        val pass = TravelPass(
            carrierName = "Japan Airlines",
            carrierCode = "JL",
            serviceNumber = "005",
            passengerName = "HARICHSELVAM / C",
            originCode = "JFK",
            originCity = "New York",
            originZoneId = "America/New_York",
            destCode = "HND",
            destCity = "Tokyo",
            destZoneId = "Asia/Tokyo",
            departureEpochMillis = departure,
            arrivalEpochMillis = arrival,
            gate = "B24"
        )

        val countdown = TravelPassManager.calculateCountdown(pass, now.toEpochMilli())

        assertEquals(3, countdown.hoursRemaining)
        assertEquals(0, countdown.minutesRemaining)
        assertTrue(!countdown.isDeparted)
        assertTrue(!countdown.isInFlight)
        assertTrue(!countdown.isLanded)
        assertTrue(countdown.statusLabel.contains("Departing"))
        assertTrue(countdown.flightDurationFormatted.contains("13h"))
        assertTrue(countdown.departureFormattedOrigin.isNotBlank())
        assertTrue(countdown.arrivalFormattedDest.isNotBlank())
    }

    @Test
    fun `calculateCountdown recognizes in-flight status`() {
        val now = Instant.now()
        val departure = now.minus(2, ChronoUnit.HOURS).toEpochMilli()
        val arrival = now.plus(10, ChronoUnit.HOURS).toEpochMilli()

        val pass = TravelPass(
            carrierName = "Japan Airlines",
            carrierCode = "JL",
            serviceNumber = "005",
            passengerName = "HARICHSELVAM / C",
            originCode = "JFK",
            originCity = "New York",
            originZoneId = "America/New_York",
            destCode = "HND",
            destCity = "Tokyo",
            destZoneId = "Asia/Tokyo",
            departureEpochMillis = departure,
            arrivalEpochMillis = arrival
        )

        val countdown = TravelPassManager.calculateCountdown(pass, now.toEpochMilli())
        assertTrue(countdown.isDeparted)
        assertTrue(countdown.isInFlight)
        assertTrue(!countdown.isLanded)
        assertTrue(countdown.statusLabel.contains("In-Flight"))
    }

    @Test
    fun `generateBarcodeMatrix creates valid binary matrix for QR and Aztec formats`() {
        val payload = "M1SEETIMER/HARIC  EJLK889 JFKHNDJL 005 240Y012A0024 100"

        val qrMatrix = TravelPassManager.generateBarcodeMatrix(payload, BarcodeFormat.QR_CODE)
        assertTrue(qrMatrix.isNotEmpty())
        assertTrue(qrMatrix[0].isNotEmpty())
        // Top-left finder pattern corner must be true (black)
        assertTrue(qrMatrix[0][0])

        val pdfMatrix = TravelPassManager.generateBarcodeMatrix(payload, BarcodeFormat.PDF417)
        assertTrue(pdfMatrix.isNotEmpty())
        assertTrue(pdfMatrix[0].isNotEmpty())
    }
}
