package com.example.ringrush

import com.example.ringrush.engine.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun testStreakMultiplierProgression() {
        // Streak 0 and 1 give 1.0x
        assertEquals(1.0f, ScoreCalculator.calculateStreakMultiplier(0), 0.001f)
        assertEquals(1.0f, ScoreCalculator.calculateStreakMultiplier(1), 0.001f)

        // Streak 2 gives 1.2x
        assertEquals(1.2f, ScoreCalculator.calculateStreakMultiplier(2), 0.001f)

        // Streak 3 gives 1.4x
        assertEquals(1.4f, ScoreCalculator.calculateStreakMultiplier(3), 0.001f)

        // Streak 6 gives 2.0x
        assertEquals(2.0f, ScoreCalculator.calculateStreakMultiplier(6), 0.001f)

        // Streak cap at 3.0x
        assertEquals(3.0f, ScoreCalculator.calculateStreakMultiplier(20), 0.001f)
    }

    @Test
    fun testCalculatePassScoreFirstRing() {
        val result = ScoreCalculator.calculatePassScore(
            currentScore = 0,
            currentStreak = 0,
            precisionRatio = 0.0f
        )

        assertEquals(100, result.pointsAwarded)
        assertEquals(100, result.newTotalScore)
        assertEquals(1, result.newStreak)
        assertEquals(1.0f, result.streakMultiplier, 0.001f)
        assertEquals(0, result.precisionBonus)
    }

    @Test
    fun testCalculatePassScoreWithPrecisionAndStreak() {
        val result = ScoreCalculator.calculatePassScore(
            currentScore = 100,
            currentStreak = 1,
            precisionRatio = 1.0f // Perfect center pass
        )

        // Streak will become 2 -> multiplier 1.2x -> 120 base points
        // Perfect precision -> 50 bonus points
        // Total awarded = 170
        assertEquals(120, result.basePoints)
        assertEquals(50, result.precisionBonus)
        assertTrue(result.isPerfect)
        assertEquals(170, result.pointsAwarded)
        assertEquals(270, result.newTotalScore)
        assertEquals(2, result.newStreak)
    }
}
