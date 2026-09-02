package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey val currencyCode: String,
    val rateToBaseUSD: Double,
    val currencyName: String,
    val symbol: String,
    val countryOrZoneHint: String,
    val lastUpdatedEpochMillis: Long = System.currentTimeMillis()
)
