package com.harichselvamc.seetime.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppDao {

    /* ---------- TimePair CRUD ---------- */

    @Query("SELECT * FROM timepair ORDER BY id ASC")
    suspend fun getPairs(): List<TimePair>

    @Insert
    suspend fun insert(pair: TimePair): Long

    @Delete
    suspend fun delete(pair: TimePair)

    // 🔹 used by TimeRepository.deletePairById(...)
    @Query("DELETE FROM timepair WHERE id = :id")
    suspend fun deleteById(id: Long)

    // 🔹 used by TimeRepository.updatePair(...)
    @Query("UPDATE timepair SET fromZone = :fromZone, toZone = :toZone WHERE id = :id")
    suspend fun updatePair(id: Long, fromZone: String, toZone: String)

    /* ---------- ZoneCache upsert / read ---------- */

    @Query("SELECT * FROM zonecache WHERE timeZone = :tz LIMIT 1")
    suspend fun getCache(tz: String): ZoneCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCache(cache: ZoneCache)
}
