package com.harichselvamc.seetime.data.local

import androidx.room.*

@Dao
interface TimeDao {

    @Query("SELECT * FROM time_pairs ORDER BY sortOrder ASC, id ASC")
    suspend fun getPairs(): List<TimePair>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pair: TimePair): Long

    @Delete
    suspend fun delete(pair: TimePair)

    @Query("DELETE FROM time_pairs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE time_pairs SET fromZone = :fromZone, toZone = :toZone, label = :label WHERE id = :id")
    suspend fun updateZones(id: Long, fromZone: String, toZone: String, label: String)

    @Query("UPDATE time_pairs SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM time_pairs")
    suspend fun getMaxSortOrder(): Long

    @Query("SELECT * FROM timezone_cache WHERE timeZone = :tz")
    suspend fun getCache(tz: String): ZoneCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCache(cache: ZoneCache)
}
