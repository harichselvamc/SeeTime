package com.harichselvamc.seetime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimePair::class, ZoneCache::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TimeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE time_pairs ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE timezone_cache ADD COLUMN standardOffsetMinutes INTEGER NOT NULL DEFAULT 0")
                // Existing rows get sortOrder=0 (stable, order by id as tiebreaker) and
                // standardOffsetMinutes=0 (harmless: cache is recomputed on next refresh).
            }
        }
    }
}
