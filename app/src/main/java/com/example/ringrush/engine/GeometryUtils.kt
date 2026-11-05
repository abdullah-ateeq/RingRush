package com.example.ringrush.engine

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeometryUtils {

    /**
     * Euclidean distance between two points.
     */
    fun distance(p1: Offset, p2: Offset): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Normalizes an angle in degrees into [0.0, 360.0).
     */
    fun normalizeAngle(degrees: Float): Float {
        var angle = degrees % 360f
        if (angle < 0f) {
            angle += 360f
        }
        return angle
    }

    /**
     * Returns the angle in degrees in [0.0, 360.0) from center to point.
     * 0 degrees is along positive X-axis (to the right), increasing clockwise.
     */
    fun angleFromCenter(center: Offset, point: Offset): Float {
        val dx = point.x - center.x
        val dy = point.y - center.y
        val radians = atan2(dy, dx)
        val degrees = Math.toDegrees(radians.toDouble()).toFloat()
        return normalizeAngle(degrees)
    }

    /**
     * Calculates the minimal absolute angular difference between two angles in degrees.
     * Returns a value in [0.0, 180.0].
     */
    fun angularDifference(angle1: Float, angle2: Float): Float {
        val diff = Math.abs(normalizeAngle(angle1) - normalizeAngle(angle2))
        return if (diff > 180f) 360f - diff else diff
    }

    /**
     * Checks if a point at [pointAngle] falls inside an arc centered at [arcCenterAngle]
     * with an angular width of [arcSpanDegrees].
     */
    fun isAngleInsideArc(pointAngle: Float, arcCenterAngle: Float, arcSpanDegrees: Float): Boolean {
        val halfSpan = arcSpanDegrees / 2f
        val diff = angularDifference(pointAngle, arcCenterAngle)
        return diff <= halfSpan
    }

    /**
     * Determines whether the shrinking ring and the player overlap radially.
     * Overlap occurs if the distance between ring's radius and the player's radial distance
     * is smaller than the combined player radius and half the stroke width.
     */
    fun isRadialOverlap(
        ringRadius: Float,
        strokeWidth: Float,
        playerCenter: Offset,
        ringCenter: Offset,
        playerRadius: Float
    ): Boolean {
        val distToCenter = distance(playerCenter, ringCenter)
        val radialDistance = Math.abs(ringRadius - distToCenter)
        val contactThreshold = playerRadius + (strokeWidth / 2f)
        return radialDistance <= contactThreshold
    }

    /**
     * Determines if the player has passed the ring safely through its opening.
     *
     * A pass is safe if:
     * 1. The player's angular span is safely contained inside the opening arc.
     * 2. The player is not colliding with the solid ring edges.
     */
    fun isPlayerInsideOpening(
        playerCenter: Offset,
        ringCenter: Offset,
        openingCenterAngle: Float,
        openingArcDegrees: Float,
        playerRadius: Float,
        ringRadius: Float
    ): Boolean {
        val distToCenter = distance(playerCenter, ringCenter)
        if (distToCenter < 1f) {
            // Player is right at center; cannot determine angle safely
            return false
        }

        val playerAngle = angleFromCenter(ringCenter, playerCenter)
        val angularDiff = angularDifference(playerAngle, openingCenterAngle)

        // Calculate angular half-width of the player at this radial distance
        // For d >> r, angular radius is approximately (r / d) in radians
        val ratio = (playerRadius / distToCenter).coerceIn(0f, 1f)
        val angularMarginDegrees = Math.toDegrees(kotlin.math.asin(ratio.toDouble())).toFloat()

        // Effective safe half-angle
        val safeHalfAngle = (openingArcDegrees / 2f) - angularMarginDegrees

        // Player is safely inside if diff <= safeHalfAngle
        // If opening is smaller than player's angular footprint, safeHalfAngle < 0
        return safeHalfAngle > 0f && angularDiff <= safeHalfAngle
    }

    /**
     * Checks whether the ring has completely shrunk past the player's orbit.
     * This happens when the outer edge of the ring is strictly closer to center
     * than the inner edge of the player.
     */
    fun hasRingShrunkPastPlayer(
        ringRadius: Float,
        strokeWidth: Float,
        playerCenter: Offset,
        ringCenter: Offset,
        playerRadius: Float
    ): Boolean {
        val distToCenter = distance(playerCenter, ringCenter)
        val ringOuterEdge = ringRadius + (strokeWidth / 2f)
        val playerInnerEdge = distToCenter - playerRadius
        return ringOuterEdge < playerInnerEdge
    }

    /**
     * Clamps the player position to stay strictly inside the playable arena circle.
     */
    fun clampPlayerToArena(
        position: Offset,
        arenaCenter: Offset,
        arenaRadius: Float,
        playerRadius: Float
    ): Offset {
        val maxAllowedDist = (arenaRadius - playerRadius).coerceAtLeast(0f)
        val dist = distance(position, arenaCenter)
        if (dist <= maxAllowedDist || dist == 0f) {
            return position
        }
        val ratio = maxAllowedDist / dist
        val dx = (position.x - arenaCenter.x) * ratio
        val dy = (position.y - arenaCenter.y) * ratio
        return Offset(arenaCenter.x + dx, arenaCenter.y + dy)
    }

    /**
     * Calculates precision ratio [0.0, 1.0] based on how close the player is
     * to the exact center of the opening.
     */
    fun calculatePrecisionRatio(
        playerCenter: Offset,
        ringCenter: Offset,
        openingCenterAngle: Float,
        maxWindowDegrees: Float
    ): Float {
        val dist = distance(playerCenter, ringCenter)
        if (dist < 1f || maxWindowDegrees <= 0f) return 0f
        val playerAngle = angleFromCenter(ringCenter, playerCenter)
        val diff = angularDifference(playerAngle, openingCenterAngle)
        if (diff >= maxWindowDegrees) return 0f
        return (1f - (diff / maxWindowDegrees)).coerceIn(0f, 1f)
    }

    /**
     * Generates a fair opening angle for the next ring based on current player position.
     * To ensure fairness and playability, the next opening is reachable:
     * it produces varied angles while avoiding unplayable instantaneous jumps.
     */
    fun generateFairOpeningAngle(
        playerCenter: Offset,
        arenaCenter: Offset,
        previousOpeningAngle: Float?,
        randomFloat: Float // [0.0, 1.0]
    ): Float {
        val dist = distance(playerCenter, arenaCenter)
        val playerAngle = if (dist > 5f) {
            angleFromCenter(arenaCenter, playerCenter)
        } else {
            previousOpeningAngle ?: (randomFloat * 360f)
        }

        // Shift angle by varied delta between 45 and 180 degrees
        val direction = if (randomFloat > 0.5f) 1f else -1f
        val delta = 60f + (randomFloat * 120f)
        return normalizeAngle(playerAngle + (direction * delta))
    }
}
