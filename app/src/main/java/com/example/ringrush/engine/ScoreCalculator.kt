package com.example.ringrush.engine

data class ScoreResult(
    val pointsAwarded: Int,
    val basePoints: Int,
    val streakMultiplier: Float,
    val precisionBonus: Int,
    val isPerfect: Boolean,
    val newTotalScore: Int,
    val newStreak: Int
)

object ScoreCalculator {
    const val BASE_POINTS_PER_RING = 100
    const val MAX_PRECISION_BONUS = 50

    /**
     * Calculates the streak multiplier based on current consecutive successful rings.
     * Starts at 1.0x and increases by 0.2x per consecutive ring, up to 3.0x.
     */
    fun calculateStreakMultiplier(streak: Int): Float {
        if (streak <= 1) return 1.0f
        val mult = 1.0f + ((streak - 1) * 0.2f)
        return kotlin.math.min(mult, 3.0f)
    }

    /**
     * Computes the score for passing a ring.
     *
     * @param currentScore Current game score
     * @param currentStreak Current streak count
     * @param precisionRatio [0.0, 1.0] indicating closeness to center of opening
     */
    fun calculatePassScore(
        currentScore: Int,
        currentStreak: Int,
        precisionRatio: Float
    ): ScoreResult {
        val newStreak = currentStreak + 1
        val multiplier = calculateStreakMultiplier(newStreak)

        val baseAward = (BASE_POINTS_PER_RING * multiplier).toInt()
        val precisionBonus = (precisionRatio.coerceIn(0f, 1f) * MAX_PRECISION_BONUS).toInt()
        val isPerfect = precisionRatio >= 0.85f

        val totalPoints = baseAward + precisionBonus
        val newTotalScore = currentScore + totalPoints

        return ScoreResult(
            pointsAwarded = totalPoints,
            basePoints = baseAward,
            streakMultiplier = multiplier,
            precisionBonus = precisionBonus,
            isPerfect = isPerfect,
            newTotalScore = newTotalScore,
            newStreak = newStreak
        )
    }
}
