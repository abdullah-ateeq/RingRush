package com.example.ringrush.engine

import com.example.ringrush.model.Difficulty

object DifficultyProgression {

    /**
     * Computes the ring shrink speed in pixels per second given arena radius,
     * base difficulty, and rings successfully passed.
     */
    fun calculateShrinkSpeed(
        ringsPassed: Int,
        difficulty: Difficulty,
        arenaRadius: Float
    ): Float {
        // Progression ramps up speed slightly every 3 rings, capped at max
        val progressionSteps = ringsPassed / 3
        val speedIncrement = progressionSteps * 0.02f
        val effectiveSpeedRatio = (difficulty.baseShrinkSpeed + speedIncrement)
            .coerceAtMost(difficulty.maxShrinkSpeed)

        // Arena radius * speed ratio gives pixel speed per second
        return arenaRadius * effectiveSpeedRatio
    }

    /**
     * Computes the opening arc angle in degrees, narrowing gradually
     * without dropping below minOpeningArcDegrees.
     */
    fun calculateOpeningArc(
        ringsPassed: Int,
        difficulty: Difficulty
    ): Float {
        // Narrows by 1 degree every 2 rings passed
        val narrowAmount = (ringsPassed / 2) * 1.0f
        return (difficulty.baseOpeningArcDegrees - narrowAmount)
            .coerceAtLeast(difficulty.minOpeningArcDegrees)
    }

    /**
     * Computes the playable arena radius relative to available viewport radius.
     * Easy provides slightly larger arena, Hard is slightly tighter for challenge.
     */
    fun calculateArenaRadius(
        viewportMinDimension: Float,
        difficulty: Difficulty
    ): Float {
        val factor = when (difficulty) {
            Difficulty.EASY -> 0.44f
            Difficulty.MEDIUM -> 0.42f
            Difficulty.HARD -> 0.40f
        }
        return viewportMinDimension * factor
    }
}
