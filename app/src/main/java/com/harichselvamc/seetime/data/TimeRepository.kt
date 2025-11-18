package com.harichselvamc.seetime.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.harichselvamc.seetime.data.local.AppDatabase
import com.harichselvamc.seetime.data.local.TimePair
import com.harichselvamc.seetime.data.local.ZoneCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

class TimeRepository private constructor(context: Context) {

    companion object {
        private const val TAG = "TimeRepository"

        @Volatile
        private var INSTANCE: TimeRepository? = null

        fun getInstance(context: Context): TimeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimeRepository(context).also { INSTANCE = it }
            }
        }
    }

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "see_time.db"
    ).build()

    private val dao = db.dao()

    /* --------- Pairs --------- */

    suspend fun getPairs(): List<TimePair> {
        val list = dao.getPairs()
        Log.d(TAG, "getPairs() -> count=${list.size}")
        return list
    }

    suspend fun addPair(fromZone: String, toZone: String): Long {
        Log.d(TAG, "addPair() from=$fromZone to=$toZone")
        val pair = TimePair(fromZone = fromZone, toZone = toZone)
        val id = dao.insert(pair)
        Log.d(TAG, "addPair() inserted id=$id")
        return id
    }

    // Delete by entity (if you still call this anywhere)
    suspend fun deletePair(pair: TimePair) {
        Log.d(TAG, "deletePair() id=${pair.id}")
        dao.delete(pair)
    }

    // 🔹 New: delete by ID (used by swipe-to-delete)
    // Uses getPairs() + delete(), so it doesn't depend on extra DAO methods.
    suspend fun deletePairById(id: Long) {
        Log.d(TAG, "deletePairById() id=$id")
        val existing = dao.getPairs().find { it.id == id }
        if (existing != null) {
            dao.delete(existing)
            Log.d(TAG, "deletePairById() deleted id=$id")
        } else {
            Log.d(TAG, "deletePairById() no row for id=$id")
        }
    }

    // 🔹 New: update an existing pair's zones (used by edit dialog)
    // Implementation: find existing -> delete -> insert updated copy.
    // (ID may change, but UI reloads fresh list, so it's fine.)
    suspend fun updatePair(id: Long, fromZone: String, toZone: String) {
        Log.d(TAG, "updatePair() id=$id from=$fromZone to=$toZone")
        val existing = dao.getPairs().find { it.id == id }
        if (existing == null) {
            Log.d(TAG, "updatePair() no existing row for id=$id, inserting new")
            addPair(fromZone, toZone)
            return
        }

        // Delete old row
        dao.delete(existing)

        // Insert updated row
        val newId = dao.insert(
            existing.copy(
                fromZone = fromZone,
                toZone = toZone
            )
        )
        Log.d(TAG, "updatePair() replaced id=$id with newId=$newId")
    }

    /* --------- Timezone cache & refresh (local only) --------- */

    suspend fun refreshAllZones() = withContext(Dispatchers.IO) {
        val pairs = dao.getPairs()
        Log.d(TAG, "refreshAllZones() pairs count=${pairs.size}")
        if (pairs.isEmpty()) {
            Log.d(TAG, "refreshAllZones() -> no pairs, skipping")
            return@withContext
        }

        val uniqueZones = pairs
            .flatMap { listOf(it.fromZone, it.toZone) }
            .toSet()

        Log.d(TAG, "refreshAllZones() uniqueZones=${uniqueZones.joinToString()}")

        val nowInstant = Instant.now()
        val nowUtcMillis = System.currentTimeMillis()

        for (tz in uniqueZones) {
            try {
                Log.d(TAG, "refreshAllZones() computing locally for tz=$tz")

                val zoneId = ZoneId.of(tz)
                val zoned = nowInstant.atZone(zoneId)

                val offsetMinutes = zoned.offset.totalSeconds / 60
                val dstActive = zoneId.rules.isDaylightSavings(nowInstant)

                Log.d(
                    TAG,
                    "refreshAllZones() tz=$tz offsetMinutes=$offsetMinutes dstActive=$dstActive"
                )

                val cache = ZoneCache(
                    timeZone = tz,
                    offsetMinutes = offsetMinutes,
                    dstActive = dstActive,
                    lastUpdated = nowUtcMillis
                )
                dao.upsertCache(cache)
                Log.d(TAG, "refreshAllZones() upserted cache for tz=$tz")
            } catch (e: Exception) {
                Log.e(TAG, "refreshAllZones() error for tz=$tz -> ${e.message}", e)
            }
        }
    }

    suspend fun getZoneCache(tz: String): ZoneCache? {
        val cache = dao.getCache(tz)
        Log.d(
            TAG,
            "getZoneCache($tz) -> ${
                if (cache == null) "null"
                else "offset=${cache.offsetMinutes} dst=${cache.dstActive} updated=${cache.lastUpdated}"
            }"
        )
        return cache
    }
}
