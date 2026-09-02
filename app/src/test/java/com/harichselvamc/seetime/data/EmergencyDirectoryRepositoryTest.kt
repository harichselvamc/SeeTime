package com.harichselvamc.seetime.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyDirectoryRepositoryTest {

    @Test
    fun `database contains comprehensive world countries with valid emergency numbers`() {
        val repo = EmergencyDirectoryRepository(null)
        val countries = repo.countries.value

        assertTrue("Should contain at least 100+ countries, found ${countries.size}", countries.size >= 100)

        for (country in countries) {
            assertTrue("Country name should not be blank", country.countryName.isNotBlank())
            assertTrue("Country ISO should be 2 characters: ${country.countryIsoCode}", country.countryIsoCode.length == 2)
            assertTrue("Calling code must start with +: ${country.callingCode}", country.callingCode.startsWith("+"))
            assertTrue("Police number required: ${country.countryName}", country.police.isNotBlank())
            assertTrue("Ambulance number required: ${country.countryName}", country.ambulance.isNotBlank())
            assertTrue("Fire number required: ${country.countryName}", country.fire.isNotBlank())
            assertTrue("Region required: ${country.countryName}", country.region.isNotBlank())
            assertTrue("Timezone hints required: ${country.countryName}", country.timezoneHints.isNotEmpty())
        }
    }

    @Test
    fun `database spans all major geographic world regions`() {
        val repo = EmergencyDirectoryRepository(null)
        val countries = repo.countries.value

        val regions = countries.map { it.region }.distinct()
        assertTrue(regions.contains("Americas"))
        assertTrue(regions.contains("Europe"))
        assertTrue(regions.contains("Asia"))
        assertTrue(regions.contains("Africa"))
        assertTrue(regions.contains("Oceania"))
        assertTrue(regions.contains("Middle East"))

        assertTrue("Americas countries count", countries.count { it.region == "Americas" } >= 20)
        assertTrue("Europe countries count", countries.count { it.region == "Europe" } >= 25)
        assertTrue("Asia countries count", countries.count { it.region == "Asia" } >= 20)
        assertTrue("Africa countries count", countries.count { it.region == "Africa" } >= 25)
        assertTrue("Oceania countries count", countries.count { it.region == "Oceania" } >= 10)
        assertTrue("Middle East countries count", countries.count { it.region == "Middle East" } >= 8)
    }

    @Test
    fun `findCountryForTimezone resolves correct country from diverse timezone strings`() {
        // Asia
        val japan = EmergencyDirectoryRepository.findCountryForTimezone("Asia/Tokyo")
        assertNotNull(japan)
        assertEquals("Japan", japan!!.countryName)
        assertEquals("110", japan.police)
        assertEquals("119", japan.ambulance)
        assertEquals("+81", japan.callingCode)

        val singapore = EmergencyDirectoryRepository.findCountryForTimezone("Asia/Singapore")
        assertNotNull(singapore)
        assertEquals("Singapore", singapore!!.countryName)
        assertEquals("999", singapore.police)
        assertEquals("995", singapore.ambulance)

        // Europe
        val uk = EmergencyDirectoryRepository.findCountryForTimezone("Europe/London")
        assertNotNull(uk)
        assertEquals("United Kingdom", uk!!.countryName)
        assertEquals("999", uk.police)
        assertEquals("+44", uk.callingCode)

        val france = EmergencyDirectoryRepository.findCountryForTimezone("Europe/Paris")
        assertNotNull(france)
        assertEquals("France", france!!.countryName)
        assertEquals("15 (SAMU)", france.ambulance)

        // Americas
        val usa = EmergencyDirectoryRepository.findCountryForTimezone("America/New_York")
        assertNotNull(usa)
        assertEquals("United States", usa!!.countryName)
        assertEquals("911", usa.universalEmergency)

        val brazil = EmergencyDirectoryRepository.findCountryForTimezone("America/Sao_Paulo")
        assertNotNull(brazil)
        assertEquals("Brazil", brazil!!.countryName)
        assertEquals("190", brazil.police)
        assertEquals("192 (SAMU)", brazil.ambulance)

        // Africa
        val egypt = EmergencyDirectoryRepository.findCountryForTimezone("Africa/Cairo")
        assertNotNull(egypt)
        assertEquals("Egypt", egypt!!.countryName)
        assertEquals("122", egypt.police)

        val southAfrica = EmergencyDirectoryRepository.findCountryForTimezone("Africa/Johannesburg")
        assertNotNull(southAfrica)
        assertEquals("South Africa", southAfrica!!.countryName)
        assertEquals("10111", southAfrica.police)

        // Oceania
        val australia = EmergencyDirectoryRepository.findCountryForTimezone("Australia/Sydney")
        assertNotNull(australia)
        assertEquals("Australia", australia!!.countryName)
        assertEquals("000", australia.universalEmergency)

        val nz = EmergencyDirectoryRepository.findCountryForTimezone("Pacific/Auckland")
        assertNotNull(nz)
        assertEquals("New Zealand", nz!!.countryName)
        assertEquals("111", nz.universalEmergency)
    }

    @Test
    fun `findCountryByIso and findCountryByCallingCode locate valid entries`() {
        val us = EmergencyDirectoryRepository.findCountryByIso("US")
        assertNotNull(us)
        assertEquals("United States", us!!.countryName)

        val jp = EmergencyDirectoryRepository.findCountryByIso("jp")
        assertNotNull(jp)
        assertEquals("Japan", jp!!.countryName)

        val uae = EmergencyDirectoryRepository.findCountryByCallingCode("+971")
        assertNotNull(uae)
        assertEquals("United Arab Emirates", uae!!.countryName)

        val de = EmergencyDirectoryRepository.findCountryByCallingCode("49")
        assertNotNull(de)
        assertEquals("Germany", de!!.countryName)
    }

    @Test
    fun `searchCountries filters by query and region`() {
        val repo = EmergencyDirectoryRepository(null)

        val franceSearch = repo.searchCountries("France")
        assertEquals(1, franceSearch.size)
        assertEquals("France", franceSearch.first().countryName)

        val callingCodeSearch = repo.searchCountries("+81")
        assertEquals(1, callingCodeSearch.size)
        assertEquals("Japan", callingCodeSearch.first().countryName)

        val emergency911Search = repo.searchCountries("911")
        assertTrue("Should match multiple 911 countries", emergency911Search.size >= 10)

        val europeFilter = repo.searchCountries("", "Europe")
        assertTrue(europeFilter.size >= 25)
        assertTrue(europeFilter.all { it.region == "Europe" })
    }

    @Test
    fun `formatInternationalNumber formats correctly with dialing code`() {
        val formattedJp = EmergencyDirectoryRepository.formatInternationalNumber("+81", "090-1234-5678")
        assertEquals("+81 9012345678", formattedJp)

        val formattedUk = EmergencyDirectoryRepository.formatInternationalNumber("+44", "020 7946 0991")
        assertEquals("+44 2079460991", formattedUk)
    }

    @Test
    fun `composeEmergencySmsBeacon generates structured SOS broadcast payload`() {
        val japan = EmergencyDirectoryRepository.findCountryByIso("JP")!!
        val beacon = EmergencyDirectoryRepository.composeEmergencySmsBeacon(
            country = japan,
            timezoneId = "Asia/Tokyo",
            customNote = "Need mountain rescue assistance"
        )

        assertTrue(beacon.contains("EMERGENCY SOS BEACON"))
        assertTrue(beacon.contains("Asia/Tokyo"))
        assertTrue(beacon.contains("Japan"))
        assertTrue(beacon.contains("Police 110"))
        assertTrue(beacon.contains("Ambulance 119"))
        assertTrue(beacon.contains("+81"))
        assertTrue(beacon.contains("Need mountain rescue assistance"))
    }
}
