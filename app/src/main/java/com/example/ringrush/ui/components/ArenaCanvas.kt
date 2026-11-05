package com.example.ringrush.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.model.CollisionBurst
import com.example.ringrush.model.FloatingScore
import com.example.ringrush.model.GameState
import com.example.ringrush.model.GameStatus
import com.example.ringrush.model.PulseRipple
import com.example.ringrush.ui.theme.ElectricCyan
import com.example.ringrush.ui.theme.SkyBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArenaCanvas(
    gameState: GameState,
    onDragDelta: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arenaPulse")
    val gatePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gatePulse"
    )

    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gameState.status) {
                    if (gameState.status == GameStatus.RUNNING) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta(dragAmount)
                            }
                        )
                    }
                }
        ) {
            val center = gameState.arenaCenter
            val arenaRadius = gameState.arenaRadius

            // 1. Draw arena boundary and subtle circular grid lines
            drawCircle(
                color = surfaceVariantColor.copy(alpha = 0.35f),
                radius = arenaRadius,
                center = center
            )
            drawCircle(
                color = outlineColor.copy(alpha = 0.5f),
                radius = arenaRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Midline concentric radar rings
            drawCircle(
                color = outlineColor.copy(alpha = 0.2f),
                radius = arenaRadius * 0.66f,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            drawCircle(
                color = outlineColor.copy(alpha = 0.15f),
                radius = arenaRadius * 0.33f,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
            // Center crosshair
            val crosshairLen = 10.dp.toPx()
            drawLine(
                color = outlineColor.copy(alpha = 0.35f),
                start = Offset(center.x - crosshairLen, center.y),
                end = Offset(center.x + crosshairLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = outlineColor.copy(alpha = 0.35f),
                start = Offset(center.x, center.y - crosshairLen),
                end = Offset(center.x, center.y + crosshairLen),
                strokeWidth = 1.dp.toPx()
            )

            // 2. Draw Active Shrinking Ring
            val ring = gameState.currentRing
            if (ring != null && ring.currentRadius > 0f) {
                val radius = ring.currentRadius
                val strokeWidth = ring.strokeWidth
                val topLeft = Offset(center.x - radius, center.y - radius)
                val size = Size(radius * 2f, radius * 2f)

                // Faint safe opening arc indicator (guidance)
                drawArc(
                    color = ElectricCyan.copy(alpha = gatePulse * 0.35f),
                    startAngle = ring.openingStartAngle,
                    sweepAngle = ring.openingArcDegrees,
                    useCenter = false,
                    topLeft = topLeft,
                    size = size,
                    style = Stroke(
                        width = (strokeWidth * 0.6f).coerceAtLeast(2f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )

                // Solid Ring Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.95f),
                            SkyBlue,
                            ElectricCyan.copy(alpha = 0.95f)
                        ),
                        center = center
                    ),
                    startAngle = ring.solidStartAngle,
                    sweepAngle = ring.solidArcDegrees,
                    useCenter = false,
                    topLeft = topLeft,
                    size = size,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                // Gate Tips Nodes (crisp glowing markers at the opening boundary)
                val tipRadius = strokeWidth * 0.75f
                val startRad = Math.toRadians(ring.openingStartAngle.toDouble())
                val endRad = Math.toRadians(ring.openingEndAngle.toDouble())

                val tip1 = Offset(
                    (center.x + radius * cos(startRad)).toFloat(),
                    (center.y + radius * sin(startRad)).toFloat()
                )
                val tip2 = Offset(
                    (center.x + radius * cos(endRad)).toFloat(),
                    (center.y + radius * sin(endRad)).toFloat()
                )

                drawCircle(color = ElectricCyan, radius = tipRadius, center = tip1)
                drawCircle(color = ElectricCyan, radius = tipRadius, center = tip2)
                drawCircle(color = Color.White, radius = tipRadius * 0.45f, center = tip1)
                drawCircle(color = Color.White, radius = tipRadius * 0.45f, center = tip2)
            }

            // 3. Draw Visual Effects (Ripples & Collision Sparks)
            for (effect in gameState.effects) {
                when (effect) {
                    is PulseRipple -> {
                        drawCircle(
                            color = effect.color.copy(alpha = effect.alpha),
                            radius = effect.currentRadius,
                            center = effect.center,
                            style = Stroke(width = effect.strokeWidth)
                        )
                    }
                    is CollisionBurst -> {
                        for (p in effect.particles) {
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha),
                                radius = p.size,
                                center = p.position
                            )
                        }
                    }
                    is FloatingScore -> {
                        // Handled in Compose overlay below for crisp typography
                    }
                }
            }

            // 4. Draw Player Dot
            val player = gameState.player

            // Motion trail
            for (trailPoint in player.trail) {
                drawCircle(
                    color = ElectricCyan.copy(alpha = trailPoint.alpha * 0.25f),
                    radius = player.radius * 0.7f,
                    center = trailPoint.position
                )
            }

            // Outer soft glow
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.22f),
                radius = player.glowRadius,
                center = player.position
            )
            // Mid glow halo
            drawCircle(
                color = ElectricCyan.copy(alpha = 0.55f),
                radius = player.radius * 1.35f,
                center = player.position
            )
            // Core player circle
            drawCircle(
                color = Color.White,
                radius = player.radius,
                center = player.position
            )
            // Inner core dot
            drawCircle(
                color = ElectricCyan,
                radius = player.radius * 0.5f,
                center = player.position
            )
        }

        // 5. Floating Score Typography Overlay
        val density = LocalDensity.current
        for (effect in gameState.effects) {
            if (effect is FloatingScore) {
                val xOffsetDp = with(density) { effect.position.x.toDp() }
                val yOffsetDp = with(density) { effect.position.y.toDp() }

                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (effect.position.x - 120).toInt(),
                            y = effect.position.y.toInt()
                        )
                    }
                ) {
                    Text(
                        text = effect.text,
                        color = effect.color.copy(alpha = effect.alpha),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}
