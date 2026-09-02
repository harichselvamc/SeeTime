package com.harichselvamc.seetime.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TimePair::class, ZoneCache::class, Activity::class, StreakEntity::class, BadgeEntity::class, CurrencyRateEntity::class, TravelChecklistEntity::class],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TimeDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun checklistDao(): ChecklistDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS activities (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, label TEXT NOT NULL, startTimeMillis INTEGER NOT NULL, endTimeMillis INTEGER NOT NULL, category TEXT NOT NULL DEFAULT 'Uncategorized')")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS streaks (id INTEGER PRIMARY KEY NOT NULL, currentStreak INTEGER NOT NULL, maxStreak INTEGER NOT NULL, lastActiveEpochDay INTEGER NOT NULL, streakFreezeCount INTEGER NOT NULL, totalDaysActive INTEGER NOT NULL, dailyActionsCount INTEGER NOT NULL, dailyTargetActions INTEGER NOT NULL, lastUpdatedEpochMillis INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS badges (id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, iconName TEXT NOT NULL, category TEXT NOT NULL, isUnlocked INTEGER NOT NULL, progress INTEGER NOT NULL, maxProgress INTEGER NOT NULL, unlockedAtEpochMillis INTEGER)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS currency_rates (currencyCode TEXT PRIMARY KEY NOT NULL, rateToBaseUSD REAL NOT NULL, currencyName TEXT NOT NULL, symbol TEXT NOT NULL, countryOrZoneHint TEXT NOT NULL, lastUpdatedEpochMillis INTEGER NOT NULL)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS travel_checklists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tripTitle TEXT NOT NULL, targetTimezone TEXT NOT NULL, category TEXT NOT NULL, title TEXT NOT NULL, notes TEXT NOT NULL, isChecked INTEGER NOT NULL, isEssential INTEGER NOT NULL, sortOrder INTEGER NOT NULL)")
            }
        }
    }
}
