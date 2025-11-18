package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_pairs")
data class TimePair(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromZone: String,
    val toZone: String
)

@Entity(tableName = "timezone_cache")
data class ZoneCache(
    @PrimaryKey val timeZone: String,
    val offsetMinutes: Int,
    val dstActive: Boolean,
    val lastUpdated: Long
)
