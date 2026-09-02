package com.harichselvamc.seetime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency_rates ORDER BY currencyCode ASC")
    suspend fun getAllRates(): List<CurrencyRateEntity>

    @Query("SELECT * FROM currency_rates ORDER BY currencyCode ASC")
    fun getAllRatesFlow(): Flow<List<CurrencyRateEntity>>

    @Query("SELECT * FROM currency_rates WHERE currencyCode = :code LIMIT 1")
    suspend fun getRate(code: String): CurrencyRateEntity?

    @Query("SELECT COUNT(*) FROM currency_rates")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<CurrencyRateEntity>)

    @Query("SELECT * FROM currency_rates WHERE currencyCode LIKE '%' || :query || '%' OR currencyName LIKE '%' || :query || '%' OR countryOrZoneHint LIKE '%' || :query || '%'")
    suspend fun searchCurrencies(query: String): List<CurrencyRateEntity>
}
