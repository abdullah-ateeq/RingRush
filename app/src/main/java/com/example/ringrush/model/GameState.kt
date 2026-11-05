package com.example.ringrush.model

enum class GameStatus {
    READY,
    RUNNING,
    GAME_OVER
}

enum class GameOverReason(val message: String) {
    COLLISION_RING("Ring collision! Stay clear of the solid barrier."),
    RING_COLLAPSED("Ring closed! Pass through the opening before it shrinks.")
}

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val score: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val ringsPassed: Int = 0,
    val bestScore: Int = 0,
    val highestStreak: Int = 0,
    val isNewBestScore: Boolean = false,
    val isNewBestStreak: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val gameOverReason: GameOverReason? = null,
    val player: Player = Player(),
    val currentRing: Ring? = null,
    val effects: List<VisualEffect> = emptyList(),
    val arenaRadius: Float = 300f,
    val arenaCenter: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero
)
