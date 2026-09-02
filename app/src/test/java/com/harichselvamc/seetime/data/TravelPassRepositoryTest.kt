package com.harichselvamc.seetime.data

import com.harichselvamc.seetime.util.BarcodeFormat
import com.harichselvamc.seetime.util.PassType
import com.harichselvamc.seetime.util.TravelPass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TravelPassRepositoryTest {

    @Test
    fun `sample passes are loaded properly`() {
        val repo = TravelPassRepository(null)
        val passes = repo.passes.value

        assertEquals(3, passes.size)
        val flightPass = passes.find { it.passType == PassType.FLIGHT }
        assertNotNull(flightPass)
        assertEquals("Japan Airlines", flightPass!!.carrierName)
        assertEquals("JFK", flightPass.originCode)
        assertEquals("HND", flightPass.destCode)
    }

    @Test
    fun `addPass and deletePass manipulate vault collection`() {
        val repo = TravelPassRepository(null)
        val initialSize = repo.passes.value.size

        val customPass = TravelPass(
            id = "custom-test-pass",
            passType = PassType.EVENT,
            carrierName = "Android Dev Summit",
            carrierCode = "ADS",
            serviceNumber = "2026",
            passengerName = "HARICHSELVAM / C",
            originCode = "SFO",
            originCity = "San Francisco",
            originZoneId = "America/Los_Angeles",
            destCode = "SFO",
            destCity = "San Francisco",
            destZoneId = "America/Los_Angeles",
            departureEpochMillis = Instant.now().toEpochMilli(),
            arrivalEpochMillis = Instant.now().toEpochMilli(),
            bookingReference = "ADS-998822"
        )

        repo.addPass(customPass)
        assertEquals(initialSize + 1, repo.passes.value.size)
        assertEquals(customPass.id, repo.passes.value.first().id)

        repo.deletePass("custom-test-pass")
        assertEquals(initialSize, repo.passes.value.size)
    }

    @Test
    fun `togglePin toggles pinned status`() {
        val repo = TravelPassRepository(null)
        val firstId = repo.passes.value.first().id
        val initialPin = repo.passes.value.first().isPinned

        repo.togglePin(firstId)
        assertEquals(!initialPin, repo.passes.value.first { it.id == firstId }.isPinned)
    }
}
