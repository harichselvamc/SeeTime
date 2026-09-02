package com.harichselvamc.seetime.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChronotypeOptimizerEngineTest {

    @Test
    fun `evaluateQuizScore accurately diagnoses all 4 biological chronotypes`() {
        assertEquals(BiologicalChronotype.LION, ChronotypeOptimizerEngine.evaluateQuizScore(25))
        assertEquals(BiologicalChronotype.LION, ChronotypeOptimizerEngine.evaluateQuizScore(21))

        assertEquals(BiologicalChronotype.BEAR, ChronotypeOptimizerEngine.evaluateQuizScore(20))
        assertEquals(BiologicalChronotype.BEAR, ChronotypeOptimizerEngine.evaluateQuizScore(15))

        assertEquals(BiologicalChronotype.WOLF, ChronotypeOptimizerEngine.evaluateQuizScore(14))
        assertEquals(BiologicalChronotype.WOLF, ChronotypeOptimizerEngine.evaluateQuizScore(9))

        assertEquals(BiologicalChronotype.DOLPHIN, ChronotypeOptimizerEngine.evaluateQuizScore(8))
        assertEquals(BiologicalChronotype.DOLPHIN, ChronotypeOptimizerEngine.evaluateQuizScore(5))
    }

    @Test
    fun `getDailyRoutineSchedule produces valid blocks and peak focus windows`() {
        BiologicalChronotype.values().forEach { chrono ->
            val schedule = ChronotypeOptimizerEngine.getDailyRoutineSchedule(chrono)

            assertTrue(schedule.isNotEmpty())
            assertTrue(schedule.any { it.isPeakFocus })

            schedule.forEach { block ->
                assertNotNull(block.title)
                assertNotNull(block.timeSpan)
                assertNotNull(block.recommendation)
                assertNotNull(block.emoji)
            }
        }
    }

    @Test
    fun `generate24HourEnergyCurve aligns peak cognitive hours with chronotypes`() {
        val lionCurve = ChronotypeOptimizerEngine.generate24HourEnergyCurve(BiologicalChronotype.LION)
        val wolfCurve = ChronotypeOptimizerEngine.generate24HourEnergyCurve(BiologicalChronotype.WOLF)

        assertEquals(24, lionCurve.size)
        assertEquals(24, wolfCurve.size)

        // Lion morning energy at 8 AM is higher than Wolf at 8 AM
        assertTrue(lionCurve[8] > wolfCurve[8])

        // Wolf evening energy at 8 PM is higher than Lion at 8 PM
        assertTrue(wolfCurve[20] > lionCurve[20])
    }

    @Test
    fun `quiz questions are complete with 5 options per question`() {
        assertEquals(5, ChronotypeOptimizerEngine.CHRONO_QUIZ_QUESTIONS.size)
        ChronotypeOptimizerEngine.CHRONO_QUIZ_QUESTIONS.forEach { q ->
            assertNotNull(q.question)
            assertEquals(5, q.options.size)
        }
    }
}
