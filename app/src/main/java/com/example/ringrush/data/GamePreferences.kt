package com.example.ringrush.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ringrush.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val bestScore: Int = 0,
    val highestStreak: Int = 0,
    val totalRingsPassed: Int = 0,
    val totalGamesPlayed: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val themeMode: ThemeMode = ThemeMode.DARK
)

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferencesFlow = MutableStateFlow(loadPreferences())
    val preferencesFlow: StateFlow<UserPreferences> = _preferencesFlow.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            highestStreak = prefs.getInt(KEY_HIGHEST_STREAK, 0),
            totalRingsPassed = prefs.getInt(KEY_TOTAL_RINGS, 0),
            totalGamesPlayed = prefs.getInt(KEY_TOTAL_GAMES, 0),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            difficulty = Difficulty.fromName(prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name),
            themeMode = ThemeMode.fromName(prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        )
    }

    fun updateScoreAndStreak(score: Int, streak: Int, ringsPassed: Int): Pair<Boolean, Boolean> {
        val currentBest = _preferencesFlow.value.bestScore
        val currentStreak = _preferencesFlow.value.highestStreak
        val isNewBestScore = score > currentBest
        val isNewBestStreak = streak > currentStreak

        val newBestScore = if (isNewBestScore) score else currentBest
        val newHighestStreak = if (isNewBestStreak) streak else currentStreak
        val newTotalRings = _preferencesFlow.value.totalRingsPassed + ringsPassed
        val newTotalGames = _preferencesFlow.value.totalGamesPlayed + 1

        prefs.edit()
            .putInt(KEY_BEST_SCORE, newBestScore)
            .putInt(KEY_HIGHEST_STREAK, newHighestStreak)
            .putInt(KEY_TOTAL_RINGS, newTotalRings)
            .putInt(KEY_TOTAL_GAMES, newTotalGames)
            .apply()

        _preferencesFlow.value = _preferencesFlow.value.copy(
            bestScore = newBestScore,
            highestStreak = newHighestStreak,
            totalRingsPassed = newTotalRings,
            totalGamesPlayed = newTotalGames
        )

        return Pair(isNewBestScore, isNewBestStreak)
    }

    fun setDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _preferencesFlow.value = _preferencesFlow.value.copy(difficulty = difficulty)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _preferencesFlow.value = _preferencesFlow.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _preferencesFlow.value = _preferencesFlow.value.copy(hapticsEnabled = enabled)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _preferencesFlow.value = _preferencesFlow.value.copy(themeMode = mode)
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_HIGHEST_STREAK, 0)
            .putInt(KEY_TOTAL_RINGS, 0)
            .putInt(KEY_TOTAL_GAMES, 0)
            .apply()

        _preferencesFlow.value = _preferencesFlow.value.copy(
            bestScore = 0,
            highestStreak = 0,
            totalRingsPassed = 0,
            totalGamesPlayed = 0
        )
    }

    companion object {
        private const val PREFS_NAME = "ring_rush_prefs"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_HIGHEST_STREAK = "key_highest_streak"
        private const val KEY_TOTAL_RINGS = "key_total_rings"
        private const val KEY_TOTAL_GAMES = "key_total_games"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_DIFFICULTY = "key_difficulty"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
