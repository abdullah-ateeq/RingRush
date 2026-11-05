package com.example.ringrush.model

import androidx.compose.ui.geometry.Offset

data class Ring(
    val id: Long = System.currentTimeMillis(),
    val center: Offset = Offset.Zero,
    val initialRadius: Float = 0f,
    val currentRadius: Float = 0f,
    val openingCenterAngle: Float = 0f, // in degrees, [0, 360)
    val openingArcDegrees: Float = 60f,  // total arc angle of the opening in degrees
    val strokeWidth: Float = 8f,
    val shrinkSpeed: Float = 100f,      // pixels per second
    val minRadius: Float = 16f,         // radius at which ring collapses
    val passed: Boolean = false
) {
    /**
     * Start angle of the opening in degrees [0, 360).
     */
    val openingStartAngle: Float
        get() = (openingCenterAngle - (openingArcDegrees / 2f) + 360f) % 360f

    /**
     * End angle of the opening in degrees [0, 360).
     */
    val openingEndAngle: Float
        get() = (openingCenterAngle + (openingArcDegrees / 2f)) % 360f

    /**
     * The solid arc degrees: remaining 360 - openingArcDegrees.
     */
    val solidArcDegrees: Float
        get() = 360f - openingArcDegrees

    /**
     * Start angle of the solid portion (starts at end of opening).
     */
    val solidStartAngle: Float
        get() = openingEndAngle
}
