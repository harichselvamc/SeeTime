package com.harichselvamc.seetime.focus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FocusSessionManagerTest {

    private lateinit var manager: FocusSessionManager

    @Before
    fun setup() {
        manager = FocusSessionManager.createForTesting()
    }

    @Test
    fun `initial state starts in WORK mode with 25 minutes`() {
        val state = manager.state.value
        assertEquals(FocusMode.WORK, state.mode)
        assertEquals(FocusStatus.STOPPED, state.status)
        assertEquals(25 * 60, state.secondsRemaining)
        assertEquals(25 * 60, state.totalSeconds)
        assertEquals(0, state.completedSessions)
        assertEquals("25:00", state.formattedTime)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `setMode updates duration and resets timer`() {
        manager.setMode(FocusMode.SHORT_BREAK)
        var state = manager.state.value
        assertEquals(FocusMode.SHORT_BREAK, state.mode)
        assertEquals(5 * 60, state.secondsRemaining)
        assertEquals("05:00", state.formattedTime)

        manager.setMode(FocusMode.LONG_BREAK)
        state = manager.state.value
        assertEquals(FocusMode.LONG_BREAK, state.mode)
        assertEquals(15 * 60, state.secondsRemaining)
        assertEquals("15:00", state.formattedTime)
    }

    @Test
    fun `tick decrements secondsRemaining and advances progress`() {
        manager.setMode(FocusMode.SHORT_BREAK, minutes = 1) // 60 seconds
        assertEquals(60, manager.state.value.secondsRemaining)

        manager.tick()
        assertEquals(59, manager.state.value.secondsRemaining)
        assertEquals("00:59", manager.state.value.formattedTime)
        assertEquals(1f / 60f, manager.state.value.progress, 0.01f)
    }

    @Test
    fun `session completion transitions from WORK to SHORT_BREAK`() {
        manager.setMode(FocusMode.WORK, minutes = 1) // 1 minute work
        repeat(59) { manager.tick() }
        assertEquals(1, manager.state.value.secondsRemaining)

        // Last tick completes the session
        manager.tick()

        val state = manager.state.value
        assertEquals(1, state.completedSessions)
        assertEquals(FocusMode.SHORT_BREAK, state.mode)
        assertEquals(FocusStatus.COMPLETED, state.status)
        assertEquals(5 * 60, state.secondsRemaining)
    }

    @Test
    fun `4th completed session transitions to LONG_BREAK`() {
        // Complete 3 work sessions
        repeat(3) {
            manager.setMode(FocusMode.WORK, minutes = 1)
            repeat(60) { manager.tick() }
            // Complete short break
            manager.setMode(FocusMode.SHORT_BREAK, minutes = 1)
            repeat(60) { manager.tick() }
        }

        assertEquals(3, manager.state.value.completedSessions)

        // 4th work session
        manager.setMode(FocusMode.WORK, minutes = 1)
        repeat(60) { manager.tick() }

        val state = manager.state.value
        assertEquals(4, state.completedSessions)
        assertEquals(FocusMode.LONG_BREAK, state.mode) // 4th triggers Long Break!
        assertEquals(15 * 60, state.secondsRemaining)
    }

    @Test
    fun `skip immediately completes current session and moves to next`() {
        manager.setMode(FocusMode.WORK)
        manager.skip()

        val state = manager.state.value
        assertEquals(1, state.completedSessions)
        assertEquals(FocusMode.SHORT_BREAK, state.mode)
    }
}
