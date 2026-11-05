package com.example.ringrush

import androidx.compose.ui.geometry.Offset
import com.example.ringrush.engine.GeometryUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryUtilsTest {

    @Test
    fun testNormalizeAngle() {
        assertEquals(0f, GeometryUtils.normalizeAngle(0f), 0.001f)
        assertEquals(90f, GeometryUtils.normalizeAngle(90f), 0.001f)
        assertEquals(180f, GeometryUtils.normalizeAngle(180f), 0.001f)
        assertEquals(0f, GeometryUtils.normalizeAngle(360f), 0.001f)
        assertEquals(30f, GeometryUtils.normalizeAngle(390f), 0.001f)
        assertEquals(330f, GeometryUtils.normalizeAngle(-30f), 0.001f)
        assertEquals(180f, GeometryUtils.normalizeAngle(-180f), 0.001f)
    }

    @Test
    fun testAngularDifference() {
        assertEquals(0f, GeometryUtils.angularDifference(45f, 45f), 0.001f)
        assertEquals(90f, GeometryUtils.angularDifference(0f, 90f), 0.001f)
        assertEquals(180f, GeometryUtils.angularDifference(0f, 180f), 0.001f)
        // Across the 0/360 boundary
        assertEquals(20f, GeometryUtils.angularDifference(10f, 350f), 0.001f)
        assertEquals(30f, GeometryUtils.angularDifference(350f, 20f), 0.001f)
    }

    @Test
    fun testAngleFromCenter() {
        val center = Offset(100f, 100f)
        // Right
        assertEquals(0f, GeometryUtils.angleFromCenter(center, Offset(200f, 100f)), 0.01f)
        // Down
        assertEquals(90f, GeometryUtils.angleFromCenter(center, Offset(100f, 200f)), 0.01f)
        // Left
        assertEquals(180f, GeometryUtils.angleFromCenter(center, Offset(0f, 100f)), 0.01f)
        // Up
        assertEquals(270f, GeometryUtils.angleFromCenter(center, Offset(100f, 0f)), 0.01f)
    }

    @Test
    fun testPlayerBoundaryClamping() {
        val center = Offset(200f, 200f)
        val arenaRadius = 150f
        val playerRadius = 10f
        val maxAllowed = 140f // 150 - 10

        // Inside arena remains unchanged
        val insidePos = Offset(250f, 200f) // distance 50
        val clampedInside = GeometryUtils.clampPlayerToArena(insidePos, center, arenaRadius, playerRadius)
        assertEquals(insidePos.x, clampedInside.x, 0.01f)
        assertEquals(insidePos.y, clampedInside.y, 0.01f)

        // Outside arena along X axis is clamped
        val outsidePos = Offset(400f, 200f) // distance 200
        val clampedOutside = GeometryUtils.clampPlayerToArena(outsidePos, center, arenaRadius, playerRadius)
        assertEquals(200f + maxAllowed, clampedOutside.x, 0.01f)
        assertEquals(200f, clampedOutside.y, 0.01f)
        assertEquals(maxAllowed, GeometryUtils.distance(center, clampedOutside), 0.01f)
    }

    @Test
    fun testRadialOverlap() {
        val ringCenter = Offset(200f, 200f)
        val ringRadius = 100f
        val strokeWidth = 8f
        val playerRadius = 12f

        // Player at distance 100 from center -> direct overlap
        val playerAtRing = Offset(300f, 200f)
        assertTrue(GeometryUtils.isRadialOverlap(ringRadius, strokeWidth, playerAtRing, ringCenter, playerRadius))

        // Player far away inside
        val playerInside = Offset(200f, 200f)
        assertFalse(GeometryUtils.isRadialOverlap(ringRadius, strokeWidth, playerInside, ringCenter, playerRadius))

        // Player far outside
        val playerOutside = Offset(350f, 200f)
        assertFalse(GeometryUtils.isRadialOverlap(ringRadius, strokeWidth, playerOutside, ringCenter, playerRadius))
    }

    @Test
    fun testPlayerInsideOpeningDetection() {
        val ringCenter = Offset(200f, 200f)
        val ringRadius = 100f
        val openingCenterAngle = 0f // opening points right
        val openingArc = 60f
        val playerRadius = 10f

        // Player positioned in the middle of the opening
        val playerInOpening = Offset(300f, 200f) // distance 100, angle 0
        assertTrue(
            GeometryUtils.isPlayerInsideOpening(
                playerCenter = playerInOpening,
                ringCenter = ringCenter,
                openingCenterAngle = openingCenterAngle,
                openingArcDegrees = openingArc,
                playerRadius = playerRadius,
                ringRadius = ringRadius
            )
        )

        // Player positioned on the solid ring opposite the opening
        val playerOpposite = Offset(100f, 200f) // distance 100, angle 180
        assertFalse(
            GeometryUtils.isPlayerInsideOpening(
                playerCenter = playerOpposite,
                ringCenter = ringCenter,
                openingCenterAngle = openingCenterAngle,
                openingArcDegrees = openingArc,
                playerRadius = playerRadius,
                ringRadius = ringRadius
            )
        )
    }

    @Test
    fun testHasRingShrunkPastPlayer() {
        val center = Offset(200f, 200f)
        val strokeWidth = 6f
        val playerRadius = 10f
        val playerPos = Offset(300f, 200f) // dist = 100, player inner edge = 90

        // Ring outer edge = 80 + 3 = 83 < 90 -> passed!
        assertTrue(
            GeometryUtils.hasRingShrunkPastPlayer(
                ringRadius = 80f,
                strokeWidth = strokeWidth,
                playerCenter = playerPos,
                ringCenter = center,
                playerRadius = playerRadius
            )
        )

        // Ring outer edge = 95 + 3 = 98 > 90 -> not passed yet
        assertFalse(
            GeometryUtils.hasRingShrunkPastPlayer(
                ringRadius = 95f,
                strokeWidth = strokeWidth,
                playerCenter = playerPos,
                ringCenter = center,
                playerRadius = playerRadius
            )
        )
    }

    @Test
    fun testPrecisionRatio() {
        val center = Offset(200f, 200f)
        val openingCenterAngle = 90f // points down
        val maxWindow = 20f

        // Exactly at center of opening
        val playerDeadCenter = Offset(200f, 300f) // angle = 90
        val precisionFull = GeometryUtils.calculatePrecisionRatio(
            playerCenter = playerDeadCenter,
            ringCenter = center,
            openingCenterAngle = openingCenterAngle,
            maxWindowDegrees = maxWindow
        )
        assertEquals(1.0f, precisionFull, 0.01f)

        // 10 degrees off in a 20 degree window -> 0.5 ratio
        val angleRad = Math.toRadians(100.0)
        val playerOff = Offset(
            (200f + 100f * Math.cos(angleRad)).toFloat(),
            (200f + 100f * Math.sin(angleRad)).toFloat()
        )
        val precisionHalf = GeometryUtils.calculatePrecisionRatio(
            playerCenter = playerOff,
            ringCenter = center,
            openingCenterAngle = openingCenterAngle,
            maxWindowDegrees = maxWindow
        )
        assertEquals(0.5f, precisionHalf, 0.05f)
    }

    @Test
    fun testFairOpeningAngleGeneration() {
        val center = Offset(200f, 200f)
        val player = Offset(250f, 200f)

        val angle1 = GeometryUtils.generateFairOpeningAngle(player, center, null, 0.2f)
        val angle2 = GeometryUtils.generateFairOpeningAngle(player, center, angle1, 0.8f)

        assertTrue("Generated angle1 must be in [0, 360)", angle1 in 0f..360f)
        assertTrue("Generated angle2 must be in [0, 360)", angle2 in 0f..360f)
    }
}
