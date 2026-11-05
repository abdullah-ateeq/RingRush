package com.example.ringrush.model

import androidx.compose.ui.geometry.Offset

data class TrailPoint(
    val position: Offset,
    val alpha: Float
)

data class Player(
    val position: Offset = Offset.Zero,
    val radius: Float = 14f,
    val glowRadius: Float = 28f,
    val trail: List<TrailPoint> = emptyList()
) {
    fun withUpdatedTrail(newPos: Offset, maxTrailLength: Int = 8): Player {
        val updated = mutableListOf<TrailPoint>()
        updated.add(TrailPoint(newPos, 1f))
        for (tp in trail) {
            val nextAlpha = tp.alpha * 0.72f
            if (nextAlpha > 0.05f && updated.size < maxTrailLength) {
                updated.add(tp.copy(alpha = nextAlpha))
            }
        }
        return copy(position = newPos, trail = updated)
    }
}
