package com.harichselvamc.seetime.data.local

import androidx.room.*

@Dao
interface TimeDao {

    @Query("SELECT * FROM time_pairs")
    suspend fun getPairs(): List<TimePair>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pair: TimePair): Long

    @Delete
    suspend fun delete(pair: TimePair)

    @Query("SELECT * FROM timezone_cache WHERE timeZone = :tz")
    suspend fun getCache(tz: String): ZoneCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCache(cache: ZoneCache)
}
