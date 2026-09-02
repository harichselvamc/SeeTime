package com.harichselvamc.seetime.data

import android.content.Context
import com.harichselvamc.seetime.data.local.CurrencyDao
import com.harichselvamc.seetime.data.local.CurrencyRateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineCurrencyRepository private constructor(
    private val dao: CurrencyDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _ratesState = MutableStateFlow<List<CurrencyRateEntity>>(emptyList())
    val ratesState: StateFlow<List<CurrencyRateEntity>> = _ratesState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: OfflineCurrencyRepository? = null

        fun getInstance(context: Context): OfflineCurrencyRepository {
            return INSTANCE ?: synchronized(this) {
                val db = TimeRepository.getInstance(context).database
                INSTANCE ?: OfflineCurrencyRepository(db.currencyDao()).also {
                    INSTANCE = it
                    it.initialize()
                }
            }
        }

        fun createForTesting(dao: CurrencyDao, scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)): OfflineCurrencyRepository {
            return OfflineCurrencyRepository(dao, scope)
        }

        // Comprehensive snapshot of world currencies with base USD = 1.0
        val BASELINE_RATES = listOf(
            CurrencyRateEntity("USD", 1.0000, "US Dollar", "$", "United States / New York"),
            CurrencyRateEntity("EUR", 0.9250, "Euro", "€", "Eurozone / Germany / France"),
            CurrencyRateEntity("GBP", 0.7900, "British Pound", "£", "United Kingdom / London"),
            CurrencyRateEntity("JPY", 154.50, "Japanese Yen", "¥", "Japan / Tokyo"),
            CurrencyRateEntity("CAD", 1.3650, "Canadian Dollar", "CA$", "Canada / Toronto"),
            CurrencyRateEntity("AUD", 1.5200, "Australian Dollar", "A$", "Australia / Sydney"),
            CurrencyRateEntity("CHF", 0.8950, "Swiss Franc", "CHF", "Switzerland / Zurich"),
            CurrencyRateEntity("CNY", 7.2400, "Chinese Yuan", "¥", "China / Shanghai"),
            CurrencyRateEntity("INR", 83.500, "Indian Rupee", "₹", "India / Kolkata / Mumbai"),
            CurrencyRateEntity("SGD", 1.3450, "Singapore Dollar", "S$", "Singapore"),
            CurrencyRateEntity("HKD", 7.8200, "Hong Kong Dollar", "HK$", "Hong Kong"),
            CurrencyRateEntity("NZD", 1.6450, "New Zealand Dollar", "NZ$", "New Zealand / Auckland"),
            CurrencyRateEntity("KRW", 1370.0, "South Korean Won", "₩", "South Korea / Seoul"),
            CurrencyRateEntity("BRL", 5.2500, "Brazilian Real", "R$", "Brazil / Sao Paulo"),
            CurrencyRateEntity("MXN", 16.850, "Mexican Peso", "MX$", "Mexico / Mexico City"),
            CurrencyRateEntity("AED", 3.6725, "UAE Dirham", "AED", "United Arab Emirates / Dubai"),
            CurrencyRateEntity("SAR", 3.7500, "Saudi Riyal", "SAR", "Saudi Arabia / Riyadh"),
            CurrencyRateEntity("SEK", 10.650, "Swedish Krona", "kr", "Sweden / Stockholm"),
            CurrencyRateEntity("NOK", 10.750, "Norwegian Krone", "kr", "Norway / Oslo"),
            CurrencyRateEntity("DKK", 6.9000, "Danish Krone", "kr", "Denmark / Copenhagen"),
            CurrencyRateEntity("PLN", 3.9800, "Polish Zloty", "zł", "Poland / Warsaw"),
            CurrencyRateEntity("THB", 36.800, "Thai Baht", "฿", "Thailand / Bangkok"),
            CurrencyRateEntity("IDR", 16250.0, "Indonesian Rupiah", "Rp", "Indonesia / Jakarta"),
            CurrencyRateEntity("MYR", 4.7200, "Malaysian Ringgit", "RM", "Malaysia / Kuala Lumpur"),
            CurrencyRateEntity("PHP", 57.800, "Philippine Peso", "₱", "Philippines / Manila"),
            CurrencyRateEntity("TRY", 32.500, "Turkish Lira", "₺", "Turkey / Istanbul"),
            CurrencyRateEntity("ZAR", 18.450, "South African Rand", "R", "South Africa / Johannesburg"),
            CurrencyRateEntity("TWD", 32.350, "New Taiwan Dollar", "NT$", "Taiwan / Taipei"),
            CurrencyRateEntity("VND", 25400.0, "Vietnamese Dong", "₫", "Vietnam / Ho Chi Minh"),
            CurrencyRateEntity("ILS", 3.7200, "Israeli Shekel", "₪", "Israel / Jerusalem")
        )
    }

    fun initialize() {
        scope.launch {
            seedRatesIfEmpty()
            refreshRates()
        }
    }

    suspend fun seedRatesIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            dao.insertRates(BASELINE_RATES)
        }
    }

    suspend fun refreshRates() = withContext(Dispatchers.IO) {
        val rates = dao.getAllRates()
        if (rates.isEmpty()) {
            dao.insertRates(BASELINE_RATES)
            _ratesState.value = BASELINE_RATES
        } else {
            _ratesState.value = rates
        }
    }

    fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>> = dao.getAllRatesFlow()

    /**
     * Converts [amount] from [fromCurrencyCode] to [toCurrencyCode] using baseline rates relative to USD.
     */
    suspend fun convert(amount: Double, fromCurrencyCode: String, toCurrencyCode: String): Double = withContext(Dispatchers.IO) {
        if (fromCurrencyCode.equals(toCurrencyCode, ignoreCase = true)) return@withContext amount

        seedRatesIfEmpty()
        val fromRateEntity = dao.getRate(fromCurrencyCode.uppercase())
            ?: BASELINE_RATES.firstOrNull { it.currencyCode.equals(fromCurrencyCode, ignoreCase = true) }
            ?: return@withContext amount

        val toRateEntity = dao.getRate(toCurrencyCode.uppercase())
            ?: BASELINE_RATES.firstOrNull { it.currencyCode.equals(toCurrencyCode, ignoreCase = true) }
            ?: return@withContext amount

        // Amount in USD = amount / fromRate (since fromRate is units per 1 USD)
        val amountInUsd = amount / fromRateEntity.rateToBaseUSD
        // Target amount = amountInUsd * toRate
        val convertedAmount = amountInUsd * toRateEntity.rateToBaseUSD
        convertedAmount
    }

    fun calculateTip(amount: Double, tipPercent: Double): Double {
        return amount * (tipPercent / 100.0)
    }

    fun calculateSplit(totalAmount: Double, peopleCount: Int): Double {
        val count = peopleCount.coerceAtLeast(1)
        return totalAmount / count
    }

    /**
     * Contextual helper: Suggests primary currency matching a timezone identifier.
     */
    fun getSuggestedCurrencyForTimezone(zoneIdStr: String): String {
        return when {
            zoneIdStr.contains("Tokyo", ignoreCase = true) || zoneIdStr.contains("Japan", ignoreCase = true) -> "JPY"
            zoneIdStr.contains("London", ignoreCase = true) || zoneIdStr.contains("GMT", ignoreCase = true) -> "GBP"
            zoneIdStr.contains("Paris", ignoreCase = true) || zoneIdStr.contains("Berlin", ignoreCase = true) || zoneIdStr.contains("Rome", ignoreCase = true) || zoneIdStr.contains("Madrid", ignoreCase = true) -> "EUR"
            zoneIdStr.contains("New_York", ignoreCase = true) || zoneIdStr.contains("Los_Angeles", ignoreCase = true) || zoneIdStr.contains("Chicago", ignoreCase = true) -> "USD"
            zoneIdStr.contains("Toronto", ignoreCase = true) || zoneIdStr.contains("Vancouver", ignoreCase = true) -> "CAD"
            zoneIdStr.contains("Sydney", ignoreCase = true) || zoneIdStr.contains("Melbourne", ignoreCase = true) -> "AUD"
            zoneIdStr.contains("Kolkata", ignoreCase = true) || zoneIdStr.contains("India", ignoreCase = true) -> "INR"
            zoneIdStr.contains("Singapore", ignoreCase = true) -> "SGD"
            zoneIdStr.contains("Hong_Kong", ignoreCase = true) -> "HKD"
            zoneIdStr.contains("Auckland", ignoreCase = true) -> "NZD"
            zoneIdStr.contains("Seoul", ignoreCase = true) -> "KRW"
            zoneIdStr.contains("Dubai", ignoreCase = true) -> "AED"
            zoneIdStr.contains("Zurich", ignoreCase = true) -> "CHF"
            else -> "USD"
        }
    }

    suspend fun searchCurrencies(query: String): List<CurrencyRateEntity> = withContext(Dispatchers.IO) {
        seedRatesIfEmpty()
        if (query.isBlank()) {
            dao.getAllRates().ifEmpty { BASELINE_RATES }
        } else {
            val results = dao.searchCurrencies(query)
            if (results.isEmpty()) {
                BASELINE_RATES.filter {
                    it.currencyCode.contains(query, ignoreCase = true) ||
                    it.currencyName.contains(query, ignoreCase = true) ||
                    it.countryOrZoneHint.contains(query, ignoreCase = true)
                }
            } else results
        }
    }
}
