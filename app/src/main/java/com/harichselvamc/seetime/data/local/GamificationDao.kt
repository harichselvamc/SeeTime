package com.harichselvamc.seetime.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {

    @Query("SELECT * FROM streaks WHERE id = 1")
    suspend fun getStreak(): StreakEntity?

    @Query("SELECT * FROM streaks WHERE id = 1")
    fun getStreakFlow(): Flow<StreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)

    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, id ASC")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Query("SELECT * FROM badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadgesFlow(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE id = :id")
    suspend fun getBadge(id: String): BadgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadge(badge: BadgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadges(badges: List<BadgeEntity>)

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun getBadgeCount(): Int
}
