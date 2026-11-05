package com.example.ringrush.model

enum class Difficulty(
    val title: String,
    val description: String,
    val baseOpeningArcDegrees: Float,
    val baseShrinkSpeed: Float, // radius units per second relative to arena size
    val minOpeningArcDegrees: Float,
    val maxShrinkSpeed: Float,
    val precisionBonusWindowDegrees: Float
) {
    EASY(
        title = "Easy",
        description = "Large opening, gentle shrink, relaxed timing",
        baseOpeningArcDegrees = 75f,
        baseShrinkSpeed = 0.28f,
        minOpeningArcDegrees = 55f,
        maxShrinkSpeed = 0.42f,
        precisionBonusWindowDegrees = 14f
    ),
    MEDIUM(
        title = "Medium",
        description = "Balanced opening and speed, varied angles",
        baseOpeningArcDegrees = 52f,
        baseShrinkSpeed = 0.40f,
        minOpeningArcDegrees = 38f,
        maxShrinkSpeed = 0.60f,
        precisionBonusWindowDegrees = 10f
    ),
    HARD(
        title = "Hard",
        description = "Narrow opening, rapid shrink, reflex required",
        baseOpeningArcDegrees = 36f,
        baseShrinkSpeed = 0.54f,
        minOpeningArcDegrees = 26f,
        maxShrinkSpeed = 0.82f,
        precisionBonusWindowDegrees = 7f
    );

    companion object {
        fun fromOrdinal(ordinal: Int): Difficulty =
            values().getOrElse(ordinal) { MEDIUM }

        fun fromName(name: String): Difficulty =
            try {
                valueOf(name.uppercase())
            } catch (e: Exception) {
                MEDIUM
            }
    }
}
