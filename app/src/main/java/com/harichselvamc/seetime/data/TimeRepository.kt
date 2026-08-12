package com.harichselvamc.seetime.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.harichselvamc.seetime.BuildConfig
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

        private fun logd(tag: String, msg: String) {
            if (BuildConfig.DEBUG) Log.d(tag, msg)
        }
    }

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "see_time.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()

    private val dao = db.dao()

    /* --------- Pairs --------- */

    suspend fun getPairs(): List<TimePair> {
        val list = dao.getPairs()
        logd(TAG, "getPairs() -> count=${list.size}")
        return list
    }

    suspend fun addPair(fromZone: String, toZone: String, label: String = ""): Long {
        logd(TAG, "addPair() from=$fromZone to=$toZone label=$label")
        val nextOrder = dao.getMaxSortOrder() + 1
        val pair = TimePair(fromZone = fromZone, toZone = toZone, sortOrder = nextOrder, label = label)
        val id = dao.insert(pair)
        logd(TAG, "addPair() inserted id=$id sortOrder=$nextOrder")
        return id
    }

    // Delete by entity (if you still call this anywhere)
    suspend fun deletePair(pair: TimePair) {
        logd(TAG, "deletePair() id=${pair.id}")
        dao.delete(pair)
    }

    // Delete by ID.
    suspend fun deletePairById(id: Long) {
        logd(TAG, "deletePairById() id=$id")
        dao.deleteById(id)
    }

    // Update an existing pair's zones (used by edit dialog). Keeps the same
    // id and sortOrder rather than deleting + reinserting.
    suspend fun updatePair(id: Long, fromZone: String, toZone: String, label: String) {
        logd(TAG, "updatePair() id=$id from=$fromZone to=$toZone label=$label")
        dao.updateZones(id, fromZone, toZone, label)
    }

    // Persist a new ordering after a drag-to-reorder gesture.
    // `orderedIds` is the list of pair ids in their new top-to-bottom order.
    suspend fun reorderPairs(orderedIds: List<Long>) {
        logd(TAG, "reorderPairs() ids=$orderedIds")
        orderedIds.forEachIndexed { index, id ->
            dao.updateSortOrder(id, index.toLong())
        }
    }

    /* --------- Timezone cache & refresh (local only) --------- */

    suspend fun refreshAllZones() = withContext(Dispatchers.IO) {
        val pairs = dao.getPairs()
        logd(TAG, "refreshAllZones() pairs count=${pairs.size}")
        if (pairs.isEmpty()) {
            logd(TAG, "refreshAllZones() -> no pairs, skipping")
            return@withContext
        }

        val uniqueZones = pairs
            .flatMap { listOf(it.fromZone, it.toZone) }
            .toSet()

        logd(TAG, "refreshAllZones() uniqueZones=${uniqueZones.joinToString()}")

        val nowInstant = Instant.now()
        val nowUtcMillis = System.currentTimeMillis()

        for (tz in uniqueZones) {
            try {
                logd(TAG, "refreshAllZones() computing locally for tz=$tz")

                val zoneId = ZoneId.of(tz)
                val zoned = nowInstant.atZone(zoneId)

                val offsetMinutes = zoned.offset.totalSeconds / 60
                val standardOffsetMinutes =
                    zoneId.rules.getStandardOffset(nowInstant).totalSeconds / 60
                val dstActive = zoneId.rules.isDaylightSavings(nowInstant)

                logd(
                    TAG,
                    "refreshAllZones() tz=$tz offsetMinutes=$offsetMinutes " +
                        "standardOffsetMinutes=$standardOffsetMinutes dstActive=$dstActive"
                )

                val cache = ZoneCache(
                    timeZone = tz,
                    offsetMinutes = offsetMinutes,
                    standardOffsetMinutes = standardOffsetMinutes,
                    dstActive = dstActive,
                    lastUpdated = nowUtcMillis
                )
                dao.upsertCache(cache)
                logd(TAG, "refreshAllZones() upserted cache for tz=$tz")
            } catch (e: Exception) {
                Log.e(TAG, "refreshAllZones() error for tz=$tz -> ${e.message}", e)
            }
        }
    }

    suspend fun getZoneCache(tz: String): ZoneCache? {
        val cache = dao.getCache(tz)
        logd(
            TAG,
            "getZoneCache($tz) -> ${
                if (cache == null) "null"
                else "offset=${cache.offsetMinutes} dst=${cache.dstActive} updated=${cache.lastUpdated}"
            }"
        )
        return cache
    }
}
