package com.harichselvamc.seetime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimePair::class, ZoneCache::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TimeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE time_pairs ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE timezone_cache ADD COLUMN standardOffsetMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE time_pairs ADD COLUMN label TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
