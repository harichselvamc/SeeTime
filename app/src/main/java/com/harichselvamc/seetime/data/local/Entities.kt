package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_pairs")
data class TimePair(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromZone: String,
    val toZone: String,
    val sortOrder: Long = 0
)

@Entity(tableName = "timezone_cache")
data class ZoneCache(
    @PrimaryKey val timeZone: String,
    val offsetMinutes: Int,
    // Non-DST offset for this zone, used to compute the actual DST delta
    // rather than assuming a fixed +1:00 shift.
    val standardOffsetMinutes: Int,
    val dstActive: Boolean,
    val lastUpdated: Long
)
