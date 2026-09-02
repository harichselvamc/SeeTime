package com.harichselvamc.seetime.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastActiveEpochDay: Long = -1L,
    val streakFreezeCount: Int = 2,
    val totalDaysActive: Int = 0,
    val dailyActionsCount: Int = 0,
    val dailyTargetActions: Int = 3,
    val lastUpdatedEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val category: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val unlockedAtEpochMillis: Long? = null
)
