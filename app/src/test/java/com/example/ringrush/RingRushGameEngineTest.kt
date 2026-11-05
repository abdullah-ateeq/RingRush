package com.example.ringrush

import androidx.compose.ui.geometry.Offset
import com.example.ringrush.engine.GameAudioHapticsCallback
import com.example.ringrush.engine.RingRushGameEngine
import com.example.ringrush.model.Difficulty
import com.example.ringrush.model.GameOverReason
import com.example.ringrush.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RingRushGameEngineTest {

    private lateinit var engine: RingRushGameEngine
    private var passSoundPlayed = false
    private var collisionSoundPlayed = false

    private val callback = object : GameAudioHapticsCallback {
        override fun playPassSound(streak: Int) {
            passSoundPlayed = true
        }

        override fun playCollisionSound() {
            collisionSoundPlayed = true
        }

        override fun playStreakBonusSound() {}
        override fun triggerPassHaptic() {}
        override fun triggerCollisionHaptic() {}
    }

    @Before
    fun setup() {
        passSoundPlayed = false
        collisionSoundPlayed = false
        engine = RingRushGameEngine(difficulty = Difficulty.EASY, callback = callback)
        engine.initArena(width = 400f, height = 800f, bestScore = 500, highestStreak = 5)
    }

    @Test
    fun testInitArena() {
        val state = engine.getState()
        assertEquals(GameStatus.READY, state.status)
        assertEquals(0, state.score)
        assertEquals(500, state.bestScore)
        assertEquals(5, state.highestStreak)
        assertEquals(Offset(200f, 400f), state.arenaCenter)
        assertTrue(state.arenaRadius > 0f)
    }

    @Test
    fun testStartGame() {
        engine.startGame()
        val state = engine.getState()
        assertEquals(GameStatus.RUNNING, state.status)
        assertNotNull(state.currentRing)
        assertTrue(state.currentRing!!.currentRadius > 0f)
    }

    @Test
    fun testPlayerDragClamp() {
        engine.startGame()
        // Drag far outside
        engine.onPlayerDrag(Offset(1000f, 0f))
        val state = engine.getState()
        val dist = com.example.ringrush.engine.GeometryUtils.distance(state.player.position, state.arenaCenter)
        assertTrue("Player must be clamped inside arena radius", dist <= state.arenaRadius)
    }

    @Test
    fun testRingCollisionTriggersGameOver() {
        engine.startGame()
        val state = engine.getState()
        val ring = state.currentRing!!

        // Move player to solid portion of ring (opposite opening)
        val oppositeAngle = (ring.openingCenterAngle + 180f) % 360f
        val rad = Math.toRadians(oppositeAngle.toDouble())
        val playerPos = Offset(
            (state.arenaCenter.x + ring.currentRadius * Math.cos(rad)).toFloat(),
            (state.arenaCenter.y + ring.currentRadius * Math.sin(rad)).toFloat()
        )
        engine.onPlayerMoveTo(playerPos)

        // Small update step to evaluate collision
        engine.update(0.016f)

        val updated = engine.getState()
        assertEquals(GameStatus.GAME_OVER, updated.status)
        assertEquals(GameOverReason.COLLISION_RING, updated.gameOverReason)
        assertTrue(collisionSoundPlayed)
    }

    @Test
    fun testSuccessfulPassAwardsScore() {
        engine.startGame()
        val state = engine.getState()
        val ring = state.currentRing!!

        // Position player in the exact center of the opening
        val openAngleRad = Math.toRadians(ring.openingCenterAngle.toDouble())
        val playerPos = Offset(
            (state.arenaCenter.x + ring.currentRadius * 0.95f * Math.cos(openAngleRad)).toFloat(),
            (state.arenaCenter.y + ring.currentRadius * 0.95f * Math.sin(openAngleRad)).toFloat()
        )
        engine.onPlayerMoveTo(playerPos)

        // Shrink ring until it has passed the player
        val dtToPass = (ring.currentRadius - 10f) / ring.shrinkSpeed
        var elapsed = 0f
        while (elapsed < dtToPass && engine.getState().status == GameStatus.RUNNING && engine.getState().ringsPassed == 0) {
            engine.update(0.016f)
            elapsed += 0.016f
        }

        val updated = engine.getState()
        assertTrue("Rings passed should be incremented", updated.ringsPassed >= 1)
        assertTrue("Score should be awarded", updated.score >= 100)
        assertEquals(1, updated.streak)
        assertTrue(passSoundPlayed)
    }

    @Test
    fun testRestartGameResetsState() {
        engine.startGame()
        engine.restartGame()

        val state = engine.getState()
        assertEquals(GameStatus.RUNNING, state.status)
        assertEquals(0, state.score)
        assertEquals(0, state.streak)
        assertEquals(0, state.ringsPassed)
        assertNotNull(state.currentRing)
    }
}
