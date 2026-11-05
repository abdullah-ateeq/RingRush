package com.example.ringrush

import com.example.ringrush.engine.DifficultyProgression
import com.example.ringrush.model.Difficulty
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyProgressionTest {

    @Test
    fun testOpeningArcProgression() {
        for (difficulty in Difficulty.values()) {
            val initialArc = DifficultyProgression.calculateOpeningArc(0, difficulty)
            val laterArc = DifficultyProgression.calculateOpeningArc(20, difficulty)

            assertTrue(
                "Initial arc should equal base opening arc",
                initialArc == difficulty.baseOpeningArcDegrees
            )
            assertTrue(
                "Later arc should be narrower or equal to min",
                laterArc <= initialArc
            )
            assertTrue(
                "Arc must never narrow below minOpeningArcDegrees",
                laterArc >= difficulty.minOpeningArcDegrees
            )
        }
    }

    @Test
    fun testShrinkSpeedProgression() {
        val arenaRadius = 400f
        for (difficulty in Difficulty.values()) {
            val initialSpeed = DifficultyProgression.calculateShrinkSpeed(0, difficulty, arenaRadius)
            val laterSpeed = DifficultyProgression.calculateShrinkSpeed(30, difficulty, arenaRadius)

            assertTrue(
                "Speed must increase or cap at max with progression",
                laterSpeed >= initialSpeed
            )
            val maxAllowedSpeed = arenaRadius * difficulty.maxShrinkSpeed
            assertTrue(
                "Speed must not exceed maxShrinkSpeed ($laterSpeed <= $maxAllowedSpeed)",
                laterSpeed <= maxAllowedSpeed + 0.01f
            )
        }
    }

    @Test
    fun testArenaRadiusScaling() {
        val minDim = 1000f
        val easyRadius = DifficultyProgression.calculateArenaRadius(minDim, Difficulty.EASY)
        val mediumRadius = DifficultyProgression.calculateArenaRadius(minDim, Difficulty.MEDIUM)
        val hardRadius = DifficultyProgression.calculateArenaRadius(minDim, Difficulty.HARD)

        assertTrue("Easy arena should be largest", easyRadius > mediumRadius)
        assertTrue("Medium arena should be larger than hard", mediumRadius > hardRadius)
    }
}
