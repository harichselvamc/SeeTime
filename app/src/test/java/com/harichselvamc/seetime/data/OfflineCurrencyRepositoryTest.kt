package com.harichselvamc.seetime.data

import com.harichselvamc.seetime.data.local.CurrencyDao
import com.harichselvamc.seetime.data.local.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeCurrencyDao : CurrencyDao {
    val rates = mutableMapOf<String, CurrencyRateEntity>()

    override suspend fun getAllRates(): List<CurrencyRateEntity> = rates.values.toList()

    override fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>> = flowOf(rates.values.toList())

    override suspend fun getRate(code: String): CurrencyRateEntity? = rates[code]

    override suspend fun getCount(): Int = rates.size

    override suspend fun insertRates(rates: List<CurrencyRateEntity>) {
        rates.forEach { this.rates[it.currencyCode] = it }
    }

    override suspend fun searchCurrencies(query: String): List<CurrencyRateEntity> {
        return rates.values.filter {
            it.currencyCode.contains(query, ignoreCase = true) ||
            it.currencyName.contains(query, ignoreCase = true) ||
            it.countryOrZoneHint.contains(query, ignoreCase = true)
        }
    }
}

class OfflineCurrencyRepositoryTest {

    private lateinit var fakeDao: FakeCurrencyDao
    private lateinit var repository: OfflineCurrencyRepository

    @Before
    fun setup() {
        fakeDao = FakeCurrencyDao()
        repository = OfflineCurrencyRepository.createForTesting(fakeDao)
    }

    @Test
    fun `convert from USD to JPY calculates accurately`() = runBlocking {
        // 1 USD = 154.50 JPY
        val converted = repository.convert(100.0, "USD", "JPY")
        assertEquals(15450.0, converted, 0.01)
    }

    @Test
    fun `convert cross-currency EUR to GBP calculates accurately`() = runBlocking {
        // 100 EUR in USD = 100 / 0.9250 = 108.108 USD
        // 108.108 USD in GBP = 108.108 * 0.7900 = 85.405 GBP
        val converted = repository.convert(100.0, "EUR", "GBP")
        val expected = (100.0 / 0.9250) * 0.7900
        assertEquals(expected, converted, 0.01)
    }

    @Test
    fun `convert same currency returns original amount`() = runBlocking {
        val converted = repository.convert(50.0, "EUR", "EUR")
        assertEquals(50.0, converted, 0.001)
    }

    @Test
    fun `calculateTip returns correct percentage`() {
        val tip15 = repository.calculateTip(80.0, 15.0)
        assertEquals(12.0, tip15, 0.001)

        val tip20 = repository.calculateTip(50.0, 20.0)
        assertEquals(10.0, tip20, 0.001)
    }

    @Test
    fun `calculateSplit divides evenly across party size`() {
        val split3 = repository.calculateSplit(90.0, 3)
        assertEquals(30.0, split3, 0.001)

        val split1 = repository.calculateSplit(45.0, 0) // zero fallback
        assertEquals(45.0, split1, 0.001)
    }

    @Test
    fun `getSuggestedCurrencyForTimezone maps correctly`() {
        assertEquals("JPY", repository.getSuggestedCurrencyForTimezone("Asia/Tokyo"))
        assertEquals("GBP", repository.getSuggestedCurrencyForTimezone("Europe/London"))
        assertEquals("EUR", repository.getSuggestedCurrencyForTimezone("Europe/Paris"))
        assertEquals("USD", repository.getSuggestedCurrencyForTimezone("America/New_York"))
        assertEquals("INR", repository.getSuggestedCurrencyForTimezone("Asia/Kolkata"))
    }

    @Test
    fun `searchCurrencies matches code name or country`() = runBlocking {
        val results = repository.searchCurrencies("Yen")
        assertTrue(results.any { it.currencyCode == "JPY" })

        val euroResults = repository.searchCurrencies("Germany")
        assertTrue(euroResults.any { it.currencyCode == "EUR" })
    }
}
