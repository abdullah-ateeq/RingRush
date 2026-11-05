package com.example.ringrush.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

sealed interface VisualEffect

data class PulseRipple(
    val id: Long = Random.nextLong(),
    val center: Offset,
    val initialRadius: Float,
    val targetRadius: Float,
    val currentRadius: Float = initialRadius,
    val alpha: Float = 1.0f,
    val color: Color = Color(0xFF00E5FF),
    val strokeWidth: Float = 4f
) : VisualEffect

data class FloatingScore(
    val id: Long = Random.nextLong(),
    val text: String,
    val position: Offset,
    val alpha: Float = 1.0f,
    val scale: Float = 1.0f,
    val color: Color = Color(0xFF38BDF8)
) : VisualEffect

data class ImpactParticle(
    val position: Offset,
    val velocity: Offset,
    val size: Float,
    val alpha: Float = 1.0f,
    val color: Color = Color(0xFFFF3366)
)

data class CollisionBurst(
    val id: Long = Random.nextLong(),
    val particles: List<ImpactParticle>,
    val elapsedMillis: Long = 0,
    val maxDurationMillis: Long = 650
) : VisualEffect
