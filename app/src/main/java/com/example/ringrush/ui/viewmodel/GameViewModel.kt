package com.example.ringrush.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringrush.audio.HapticsPlayer
import com.example.ringrush.audio.SoundPlayer
import com.example.ringrush.data.GamePreferences
import com.example.ringrush.data.ThemeMode
import com.example.ringrush.data.UserPreferences
import com.example.ringrush.engine.GameAudioHapticsCallback
import com.example.ringrush.engine.RingRushGameEngine
import com.example.ringrush.model.Difficulty
import com.example.ringrush.model.GameState
import com.example.ringrush.model.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application), GameAudioHapticsCallback {

    private val preferences = GamePreferences(application)
    val preferencesFlow: StateFlow<UserPreferences> = preferences.preferencesFlow

    private val soundPlayer = SoundPlayer()
    private val hapticsPlayer = HapticsPlayer(application)

    private val engine = RingRushGameEngine(
        difficulty = preferences.preferencesFlow.value.difficulty,
        callback = this
    )

    private val _gameState = MutableStateFlow(engine.getState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastFrameTimeNanos: Long = 0L

    init {
        // Sync preferences with audio/haptics players
        viewModelScope.launch {
            preferences.preferencesFlow.collect { prefs ->
                soundPlayer.setEnabled(prefs.soundEnabled)
                hapticsPlayer.setEnabled(prefs.hapticsEnabled)
                engine.setDifficulty(prefs.difficulty)
            }
        }
    }

    fun onArenaSizeChanged(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val prefs = preferences.preferencesFlow.value
        engine.initArena(
            width = width,
            height = height,
            bestScore = prefs.bestScore,
            highestStreak = prefs.highestStreak
        )
        _gameState.value = engine.getState()
    }

    fun startGame() {
        engine.startGame()
        _gameState.value = engine.getState()
        startGameLoop()
    }

    fun restartGame() {
        engine.restartGame()
        _gameState.value = engine.getState()
        startGameLoop()
    }

    fun onPlayerDrag(dragDelta: androidx.compose.ui.geometry.Offset) {
        engine.onPlayerDrag(dragDelta)
        _gameState.value = engine.getState()
    }

    fun onPlayerMoveTo(target: androidx.compose.ui.geometry.Offset) {
        engine.onPlayerMoveTo(target)
        _gameState.value = engine.getState()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastFrameTimeNanos = System.nanoTime()

        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val currentTime = System.nanoTime()
                val deltaSeconds = ((currentTime - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastFrameTimeNanos = currentTime

                val prevStatus = engine.getState().status
                engine.update(deltaSeconds)
                val current = engine.getState()
                _gameState.value = current

                if (prevStatus == GameStatus.RUNNING && current.status == GameStatus.GAME_OVER) {
                    onGameOver(current)
                }

                delay(16L) // ~60 FPS
            }
        }
    }

    private fun onGameOver(state: GameState) {
        val (isNewBestScore, isNewBestStreak) = preferences.updateScoreAndStreak(
            score = state.score,
            streak = state.streak,
            ringsPassed = state.ringsPassed
        )

        _gameState.value = _gameState.value.copy(
            isNewBestScore = isNewBestScore,
            isNewBestStreak = isNewBestStreak,
            bestScore = preferences.preferencesFlow.value.bestScore,
            highestStreak = preferences.preferencesFlow.value.highestStreak
        )
    }

    fun setDifficulty(difficulty: Difficulty) {
        preferences.setDifficulty(difficulty)
        engine.setDifficulty(difficulty)
        _gameState.value = engine.getState().copy(difficulty = difficulty)
    }

    fun setSoundEnabled(enabled: Boolean) {
        preferences.setSoundEnabled(enabled)
        soundPlayer.setEnabled(enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        preferences.setHapticsEnabled(enabled)
        hapticsPlayer.setEnabled(enabled)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun resetStatistics() {
        preferences.resetStatistics()
        _gameState.value = _gameState.value.copy(
            bestScore = 0,
            highestStreak = 0,
            isNewBestScore = false,
            isNewBestStreak = false
        )
    }

    fun playClick() {
        soundPlayer.playClickSound()
        hapticsPlayer.triggerClickHaptic()
    }

    // Audio / Haptics Callback implementation
    override fun playPassSound(streak: Int) {
        soundPlayer.playPassSound(streak)
    }

    override fun playCollisionSound() {
        soundPlayer.playCollisionSound()
    }

    override fun playStreakBonusSound() {
        soundPlayer.playStreakBonusSound()
    }

    override fun triggerPassHaptic() {
        hapticsPlayer.triggerPassHaptic()
    }

    override fun triggerCollisionHaptic() {
        hapticsPlayer.triggerCollisionHaptic()
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
