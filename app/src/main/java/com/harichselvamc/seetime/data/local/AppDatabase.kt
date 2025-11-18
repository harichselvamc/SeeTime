package com.harichselvamc.seetime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TimePair::class, ZoneCache::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TimeDao
}
