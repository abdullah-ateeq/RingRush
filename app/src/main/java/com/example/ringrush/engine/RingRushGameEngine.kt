package com.example.ringrush.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ringrush.model.CollisionBurst
import com.example.ringrush.model.Difficulty
import com.example.ringrush.model.FloatingScore
import com.example.ringrush.model.GameOverReason
import com.example.ringrush.model.GameState
import com.example.ringrush.model.GameStatus
import com.example.ringrush.model.ImpactParticle
import com.example.ringrush.model.Player
import com.example.ringrush.model.PulseRipple
import com.example.ringrush.model.Ring
import com.example.ringrush.model.VisualEffect
import kotlin.random.Random

interface GameAudioHapticsCallback {
    fun playPassSound(streak: Int)
    fun playCollisionSound()
    fun playStreakBonusSound()
    fun triggerPassHaptic()
    fun triggerCollisionHaptic()
}

class RingRushGameEngine(
    private var difficulty: Difficulty = Difficulty.MEDIUM,
    private var callback: GameAudioHapticsCallback? = null
) {
    private var state: GameState = GameState(difficulty = difficulty)
    private var random: Random = Random(System.currentTimeMillis())

    fun setCallback(callback: GameAudioHapticsCallback?) {
        this.callback = callback
    }

    fun getState(): GameState = state

    fun setDifficulty(newDifficulty: Difficulty) {
        this.difficulty = newDifficulty
        if (state.status == GameStatus.READY) {
            state = state.copy(difficulty = newDifficulty)
        }
    }

    fun initArena(width: Float, height: Float, bestScore: Int, highestStreak: Int) {
        val center = Offset(width / 2f, height / 2f)
        val minDim = kotlin.math.min(width, height)
        val arenaRadius = DifficultyProgression.calculateArenaRadius(minDim, difficulty)

        val playerRadius = (minDim * 0.032f).coerceIn(12f, 20f)
        val initialPlayer = Player(
            position = center,
            radius = playerRadius,
            glowRadius = playerRadius * 2.2f
        )

        state = GameState(
            status = GameStatus.READY,
            score = 0,
            streak = 0,
            multiplier = 1.0f,
            ringsPassed = 0,
            bestScore = bestScore,
            highestStreak = highestStreak,
            difficulty = difficulty,
            player = initialPlayer,
            arenaRadius = arenaRadius,
            arenaCenter = center,
            currentRing = null,
            effects = emptyList()
        )
    }

    fun startGame() {
        if (state.status == GameStatus.RUNNING) return

        val newRing = createNewRing(ringsPassed = 0, previousAngle = null)
        state = state.copy(
            status = GameStatus.RUNNING,
            score = 0,
            streak = 0,
            multiplier = 1.0f,
            ringsPassed = 0,
            isNewBestScore = false,
            isNewBestStreak = false,
            gameOverReason = null,
            currentRing = newRing,
            effects = emptyList()
        )
    }

    fun restartGame() {
        val center = state.arenaCenter
        val resetPlayer = Player(
            position = center,
            radius = state.player.radius,
            glowRadius = state.player.glowRadius
        )
        val newRing = createNewRing(ringsPassed = 0, previousAngle = null)
        state = state.copy(
            status = GameStatus.RUNNING,
            score = 0,
            streak = 0,
            multiplier = 1.0f,
            ringsPassed = 0,
            isNewBestScore = false,
            isNewBestStreak = false,
            gameOverReason = null,
            player = resetPlayer,
            currentRing = newRing,
            effects = emptyList()
        )
    }

    fun onPlayerDrag(dragDelta: Offset) {
        if (state.status != GameStatus.RUNNING) return

        val targetPos = state.player.position + dragDelta
        val clampedPos = GeometryUtils.clampPlayerToArena(
            position = targetPos,
            arenaCenter = state.arenaCenter,
            arenaRadius = state.arenaRadius,
            playerRadius = state.player.radius
        )

        val updatedPlayer = state.player.withUpdatedTrail(clampedPos)
        state = state.copy(player = updatedPlayer)
    }

    fun onPlayerMoveTo(targetPosition: Offset) {
        if (state.status != GameStatus.RUNNING) return

        val clampedPos = GeometryUtils.clampPlayerToArena(
            position = targetPosition,
            arenaCenter = state.arenaCenter,
            arenaRadius = state.arenaRadius,
            playerRadius = state.player.radius
        )

        val updatedPlayer = state.player.withUpdatedTrail(clampedPos)
        state = state.copy(player = updatedPlayer)
    }

    fun update(deltaSeconds: Float) {
        if (state.status != GameStatus.RUNNING) {
            updateEffects(deltaSeconds)
            return
        }

        val ring = state.currentRing ?: return
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)

        // 1. Shrink ring
        val newRadius = ring.currentRadius - (ring.shrinkSpeed * dt)

        // 2. Check collision with solid portion of the ring
        val radialOverlap = GeometryUtils.isRadialOverlap(
            ringRadius = newRadius,
            strokeWidth = ring.strokeWidth,
            playerCenter = state.player.position,
            ringCenter = state.arenaCenter,
            playerRadius = state.player.radius
        )

        if (radialOverlap) {
            val safeInside = GeometryUtils.isPlayerInsideOpening(
                playerCenter = state.player.position,
                ringCenter = state.arenaCenter,
                openingCenterAngle = ring.openingCenterAngle,
                openingArcDegrees = ring.openingArcDegrees,
                playerRadius = state.player.radius,
                ringRadius = newRadius
            )

            if (!safeInside) {
                // Collision with solid ring!
                triggerGameOver(GameOverReason.COLLISION_RING, state.player.position)
                return
            }
        }

        // 3. Check if ring has safely passed player
        val ringHasPassed = GeometryUtils.hasRingShrunkPastPlayer(
            ringRadius = newRadius,
            strokeWidth = ring.strokeWidth,
            playerCenter = state.player.position,
            ringCenter = state.arenaCenter,
            playerRadius = state.player.radius
        )

        if (ringHasPassed && !ring.passed) {
            // Ring successfully passed player!
            handleSuccessfulPass(ring, newRadius)
            updateEffects(dt)
            return
        }

        // 4. Check if ring collapsed without player passing
        if (newRadius <= ring.minRadius) {
            if (!ring.passed) {
                triggerGameOver(GameOverReason.RING_COLLAPSED, state.arenaCenter)
                return
            } else {
                // Spawn next ring if previous ring collapsed
                val nextRing = createNewRing(
                    ringsPassed = state.ringsPassed,
                    previousAngle = ring.openingCenterAngle
                )
                state = state.copy(currentRing = nextRing)
            }
        } else {
            // Update ring radius
            state = state.copy(currentRing = ring.copy(currentRadius = newRadius))
        }

        updateEffects(dt)
    }

    private fun handleSuccessfulPass(ring: Ring, currentRingRadius: Float) {
        val playerPos = state.player.position
        val arenaCenter = state.arenaCenter

        // Precision bonus calculation
        val precisionRatio = GeometryUtils.calculatePrecisionRatio(
            playerCenter = playerPos,
            ringCenter = arenaCenter,
            openingCenterAngle = ring.openingCenterAngle,
            maxWindowDegrees = difficulty.precisionBonusWindowDegrees
        )

        val scoreResult = ScoreCalculator.calculatePassScore(
            currentScore = state.score,
            currentStreak = state.streak,
            precisionRatio = precisionRatio
        )

        val newRingsPassed = state.ringsPassed + 1
        val isNewBestScore = scoreResult.newTotalScore > state.bestScore
        val isNewBestStreak = scoreResult.newStreak > state.highestStreak

        // Visual effects
        val newEffects = state.effects.toMutableList()

        // Expanding pulse ripple
        newEffects.add(
            PulseRipple(
                center = playerPos,
                initialRadius = state.player.radius,
                targetRadius = state.player.radius * 4.5f,
                strokeWidth = 3.5f,
                color = if (scoreResult.isPerfect) Color(0xFF00E5FF) else Color(0xFF38BDF8)
            )
        )

        // Floating score banner
        val scoreBannerText = if (scoreResult.isPerfect) {
            "+${scoreResult.pointsAwarded} (PERFECT!)"
        } else if (scoreResult.streakMultiplier > 1.0f) {
            "+${scoreResult.pointsAwarded} (x${String.format("%.1f", scoreResult.streakMultiplier)})"
        } else {
            "+${scoreResult.pointsAwarded}"
        }

        newEffects.add(
            FloatingScore(
                text = scoreBannerText,
                position = playerPos - Offset(0f, state.player.radius * 1.8f),
                color = if (scoreResult.isPerfect) Color(0xFF00E5FF) else Color(0xFFE2E8F0)
            )
        )

        // Audio and haptics
        callback?.playPassSound(scoreResult.newStreak)
        callback?.triggerPassHaptic()
        if (scoreResult.isPerfect || scoreResult.newStreak % 5 == 0) {
            callback?.playStreakBonusSound()
        }

        // Spawn next ring immediately
        val nextRing = createNewRing(
            ringsPassed = newRingsPassed,
            previousAngle = ring.openingCenterAngle
        )

        state = state.copy(
            score = scoreResult.newTotalScore,
            streak = scoreResult.newStreak,
            multiplier = scoreResult.streakMultiplier,
            ringsPassed = newRingsPassed,
            isNewBestScore = isNewBestScore || state.isNewBestScore,
            isNewBestStreak = isNewBestStreak || state.isNewBestStreak,
            bestScore = if (isNewBestScore) scoreResult.newTotalScore else state.bestScore,
            highestStreak = if (isNewBestStreak) scoreResult.newStreak else state.highestStreak,
            currentRing = nextRing,
            effects = newEffects
        )
    }

    private fun triggerGameOver(reason: GameOverReason, impactPoint: Offset) {
        callback?.playCollisionSound()
        callback?.triggerCollisionHaptic()

        // Create impact burst particles
        val particles = (0 until 18).map {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 90f + (random.nextFloat() * 160f)
            ImpactParticle(
                position = impactPoint,
                velocity = Offset(kotlin.math.cos(angle) * speed, kotlin.math.sin(angle) * speed),
                size = 3f + (random.nextFloat() * 4f),
                color = if (reason == GameOverReason.COLLISION_RING) Color(0xFFFF3366) else Color(0xFFF59E0B)
            )
        }

        val burst = CollisionBurst(particles = particles)
        val updatedEffects = state.effects + burst

        state = state.copy(
            status = GameStatus.GAME_OVER,
            gameOverReason = reason,
            effects = updatedEffects
        )
    }

    private fun updateEffects(dt: Float) {
        if (state.effects.isEmpty()) return

        val updatedEffects = mutableListOf<VisualEffect>()
        for (effect in state.effects) {
            when (effect) {
                is PulseRipple -> {
                    val radiusStep = (effect.targetRadius - effect.initialRadius) * (dt / 0.35f)
                    val nextRadius = effect.currentRadius + radiusStep
                    val progress = ((nextRadius - effect.initialRadius) / (effect.targetRadius - effect.initialRadius)).coerceIn(0f, 1f)
                    val nextAlpha = (1f - progress).coerceIn(0f, 1f)
                    if (progress < 1f && nextAlpha > 0.02f) {
                        updatedEffects.add(effect.copy(currentRadius = nextRadius, alpha = nextAlpha))
                    }
                }
                is FloatingScore -> {
                    val nextPos = effect.position - Offset(0f, 60f * dt)
                    val nextAlpha = (effect.alpha - (dt / 0.8f)).coerceAtLeast(0f)
                    if (nextAlpha > 0.05f) {
                        updatedEffects.add(effect.copy(position = nextPos, alpha = nextAlpha))
                    }
                }
                is CollisionBurst -> {
                    val nextElapsed = effect.elapsedMillis + (dt * 1000).toLong()
                    if (nextElapsed < effect.maxDurationMillis) {
                        val progress = nextElapsed.toFloat() / effect.maxDurationMillis.toFloat()
                        val updatedParticles = effect.particles.map { p ->
                            p.copy(
                                position = p.position + (p.velocity * dt),
                                alpha = (1f - progress).coerceIn(0f, 1f)
                            )
                        }
                        updatedEffects.add(effect.copy(particles = updatedParticles, elapsedMillis = nextElapsed))
                    }
                }
            }
        }
        state = state.copy(effects = updatedEffects)
    }

    private fun createNewRing(ringsPassed: Int, previousAngle: Float?): Ring {
        val arenaRadius = state.arenaRadius
        val initialRadius = arenaRadius * 0.96f
        val strokeWidth = (arenaRadius * 0.024f).coerceIn(6f, 10f)

        val openingArc = DifficultyProgression.calculateOpeningArc(ringsPassed, difficulty)
        val shrinkSpeed = DifficultyProgression.calculateShrinkSpeed(ringsPassed, difficulty, arenaRadius)

        val angle = GeometryUtils.generateFairOpeningAngle(
            playerCenter = state.player.position,
            arenaCenter = state.arenaCenter,
            previousOpeningAngle = previousAngle,
            randomFloat = random.nextFloat()
        )

        return Ring(
            id = System.nanoTime(),
            center = state.arenaCenter,
            initialRadius = initialRadius,
            currentRadius = initialRadius,
            openingCenterAngle = angle,
            openingArcDegrees = openingArc,
            strokeWidth = strokeWidth,
            shrinkSpeed = shrinkSpeed,
            minRadius = state.player.radius * 1.1f,
            passed = false
        )
    }
}
