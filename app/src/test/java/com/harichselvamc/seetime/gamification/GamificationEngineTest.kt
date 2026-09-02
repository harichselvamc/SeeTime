package com.harichselvamc.seetime.gamification

import com.harichselvamc.seetime.data.GamificationAction
import com.harichselvamc.seetime.data.GamificationRepository
import com.harichselvamc.seetime.data.local.BadgeEntity
import com.harichselvamc.seetime.data.local.GamificationDao
import com.harichselvamc.seetime.data.local.StreakEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FakeGamificationDao : GamificationDao {
    var streak: StreakEntity? = null
    val badges = mutableMapOf<String, BadgeEntity>()

    override suspend fun getStreak(): StreakEntity? = streak

    override fun getStreakFlow(): Flow<StreakEntity?> = flowOf(streak)

    override suspend fun upsertStreak(streak: StreakEntity) {
        this.streak = streak
    }

    override suspend fun getAllBadges(): List<BadgeEntity> = badges.values.toList()

    override fun getAllBadgesFlow(): Flow<List<BadgeEntity>> = flowOf(badges.values.toList())

    override suspend fun getBadge(id: String): BadgeEntity? = badges[id]

    override suspend fun upsertBadge(badge: BadgeEntity) {
        badges[badge.id] = badge
    }

    override suspend fun upsertBadges(badges: List<BadgeEntity>) {
        badges.forEach { this.badges[it.id] = it }
    }

    override suspend fun getBadgeCount(): Int = badges.size
}

class GamificationEngineTest {

    private lateinit var fakeDao: FakeGamificationDao
    private lateinit var repository: GamificationRepository

    private val zoneUtc = ZoneId.of("UTC")

    @Before
    fun setup() {
        fakeDao = FakeGamificationDao()
        repository = GamificationRepository.createForTesting(fakeDao)
    }

    @Test
    fun `first time activity initializes streak to 1`() = runBlocking {
        val date = LocalDate.of(2026, 9, 1)
        val epochMillis = date.atStartOfDay(zoneUtc).toInstant().toEpochMilli()

        val result = repository.evaluateDailyActivity(zoneUtc, epochMillis)

        assertEquals(0, result.previousStreak)
        assertEquals(1, result.newStreak)
        assertTrue(result.isIncremented)
        assertFalse(result.isFreezeUsed)
        assertFalse(result.isBroken)

        val stored = fakeDao.getStreak()
        assertNotNull(stored)
        assertEquals(1, stored!!.currentStreak)
        assertEquals(1, stored.maxStreak)
        assertEquals(date.toEpochDay(), stored.lastActiveEpochDay)
        assertEquals(1, stored.totalDaysActive)
    }

    @Test
    fun `same day activity keeps streak unchanged and increments actions`() = runBlocking {
        val date = LocalDate.of(2026, 9, 1)
        val morningMillis = date.atTime(9, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val eveningMillis = date.atTime(18, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repository.evaluateDailyActivity(zoneUtc, morningMillis)
        val result = repository.evaluateDailyActivity(zoneUtc, eveningMillis)

        assertEquals(1, result.previousStreak)
        assertEquals(1, result.newStreak)
        assertFalse(result.isIncremented)
        assertFalse(result.isFreezeUsed)
        assertFalse(result.isBroken)

        val stored = fakeDao.getStreak()
        assertEquals(1, stored!!.currentStreak)
        assertEquals(2, stored.dailyActionsCount)
    }

    @Test
    fun `consecutive day activity increments streak`() = runBlocking {
        val day1 = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val day2 = LocalDate.of(2026, 9, 2).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val day3 = LocalDate.of(2026, 9, 3).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repository.evaluateDailyActivity(zoneUtc, day1)
        val res2 = repository.evaluateDailyActivity(zoneUtc, day2)
        val res3 = repository.evaluateDailyActivity(zoneUtc, day3)

        assertEquals(3, res3.newStreak)
        assertTrue(res3.isIncremented)
        assertFalse(res3.isFreezeUsed)

        val stored = fakeDao.getStreak()
        assertEquals(3, stored!!.currentStreak)
        assertEquals(3, stored.maxStreak)
        assertEquals(3, stored.totalDaysActive)
    }

    @Test
    fun `missed single day uses streak freeze when available`() = runBlocking {
        val day1 = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        // Day 2 (Sep 2) is missed!
        // Day 3 (Sep 3) activity:
        val day3 = LocalDate.of(2026, 9, 3).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repository.evaluateDailyActivity(zoneUtc, day1)
        val result = repository.evaluateDailyActivity(zoneUtc, day3)

        assertTrue(result.isFreezeUsed)
        assertTrue(result.isIncremented)
        assertFalse(result.isBroken)
        assertEquals(2, result.newStreak)

        val stored = fakeDao.getStreak()
        assertEquals(2, stored!!.currentStreak)
        assertEquals(1, stored.streakFreezeCount) // 2 -> 1
    }

    @Test
    fun `missed single day without streak freeze resets streak`() = runBlocking {
        // Set up streak with 0 freezes
        fakeDao.upsertStreak(
            StreakEntity(
                id = 1,
                currentStreak = 5,
                maxStreak = 5,
                lastActiveEpochDay = LocalDate.of(2026, 9, 1).toEpochDay(),
                streakFreezeCount = 0
            )
        )

        // Day 3 activity (Day 2 missed, 0 freezes left)
        val day3 = LocalDate.of(2026, 9, 3).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val result = repository.evaluateDailyActivity(zoneUtc, day3)

        assertTrue(result.isBroken)
        assertEquals(1, result.newStreak)

        val stored = fakeDao.getStreak()
        assertEquals(1, stored!!.currentStreak)
        assertEquals(5, stored.maxStreak) // max streak preserved!
    }

    @Test
    fun `missed two or more days resets streak even if freeze is available`() = runBlocking {
        val day1 = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        // Day 2 & 3 missed, active on Day 4
        val day4 = LocalDate.of(2026, 9, 4).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repository.evaluateDailyActivity(zoneUtc, day1)
        val result = repository.evaluateDailyActivity(zoneUtc, day4)

        assertTrue(result.isBroken)
        assertFalse(result.isFreezeUsed)
        assertEquals(1, result.newStreak)
        assertEquals(2, fakeDao.getStreak()!!.streakFreezeCount) // freeze was not wasted
    }

    @Test
    fun `badge unlocks on first timezone added action`() = runBlocking {
        val epochMillis = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        val unlocked = repository.recordAction(GamificationAction.ADD_TIME_PAIR, zoneUtc, epochMillis)

        assertTrue(unlocked.any { it.id == "first_timezone_added" })
        val badge = fakeDao.getBadge("first_timezone_added")
        assertNotNull(badge)
        assertTrue(badge!!.isUnlocked)
        assertEquals(1, badge.progress)
    }

    @Test
    fun `streak 3 days badge unlocks on 3rd consecutive day`() = runBlocking {
        val day1 = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val day2 = LocalDate.of(2026, 9, 2).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()
        val day3 = LocalDate.of(2026, 9, 3).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repository.evaluateDailyActivity(zoneUtc, day1)
        repository.evaluateDailyActivity(zoneUtc, day2)
        val result = repository.evaluateDailyActivity(zoneUtc, day3)

        assertTrue(result.unlockedBadges.any { it.id == "streak_3_days" })
        val badge = fakeDao.getBadge("streak_3_days")
        assertTrue(badge!!.isUnlocked)
    }

    @Test
    fun `time traveler badge increments and unlocks at 5 actions`() = runBlocking {
        val day = LocalDate.of(2026, 9, 1).atTime(12, 0).atZone(zoneUtc).toInstant().toEpochMilli()

        repeat(4) {
            repository.recordAction(GamificationAction.TIME_TRAVEL, zoneUtc, day)
        }
        val badgeBefore = fakeDao.getBadge("time_traveler")
        assertNotNull(badgeBefore)
        assertFalse(badgeBefore!!.isUnlocked)
        assertEquals(4, badgeBefore.progress)

        // 5th action unlocks
        val unlocked = repository.recordAction(GamificationAction.TIME_TRAVEL, zoneUtc, day)
        assertTrue(unlocked.any { it.id == "time_traveler" })
        val badgeAfter = fakeDao.getBadge("time_traveler")
        assertTrue(badgeAfter!!.isUnlocked)
        assertEquals(5, badgeAfter.progress)
    }

    @Test
    fun `night owl badge unlocks when active between 22 and 4`() = runBlocking {
        val nightTime = LocalDate.of(2026, 9, 1).atTime(23, 30).atZone(zoneUtc).toInstant().toEpochMilli()

        val result = repository.evaluateDailyActivity(zoneUtc, nightTime)
        assertTrue(result.unlockedBadges.any { it.id == "night_owl" })
        val badge = fakeDao.getBadge("night_owl")
        assertTrue(badge!!.isUnlocked)
    }

    @Test
    fun `early bird badge unlocks when active between 5 and 8`() = runBlocking {
        val earlyMorning = LocalDate.of(2026, 9, 1).atTime(6, 15).atZone(zoneUtc).toInstant().toEpochMilli()

        val result = repository.evaluateDailyActivity(zoneUtc, earlyMorning)
        assertTrue(result.unlockedBadges.any { it.id == "early_bird" })
        val badge = fakeDao.getBadge("early_bird")
        assertTrue(badge!!.isUnlocked)
    }

    @Test
    fun `timezone boundary evaluates local day accurately`() = runBlocking {
        // 2026-09-01 23:30 UTC is:
        // - 2026-09-01 in UTC
        // - 2026-09-02 in Tokyo (UTC+9 -> 08:30 AM next day!)
        val timeUtc = ZonedDateTime.of(2026, 9, 1, 23, 30, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        val tokyoZone = ZoneId.of("Asia/Tokyo")

        repository.evaluateDailyActivity(tokyoZone, timeUtc)
        val stored = fakeDao.getStreak()

        assertEquals(LocalDate.of(2026, 9, 2).toEpochDay(), stored!!.lastActiveEpochDay)
    }
}
