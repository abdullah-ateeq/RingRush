package com.example.ringrush.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ringrush.ui.screens.AboutScreen
import com.example.ringrush.ui.screens.GameScreen
import com.example.ringrush.ui.screens.HomeScreen
import com.example.ringrush.ui.screens.ResultScreen
import com.example.ringrush.ui.screens.SettingsScreen
import com.example.ringrush.ui.screens.SplashScreen
import com.example.ringrush.ui.viewmodel.GameViewModel

@Composable
fun RingRushNavGraph(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val preferences by viewModel.preferencesFlow.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                preferences = preferences,
                onStartGame = {
                    viewModel.playClick()
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route)
                },
                onDifficultySelected = { diff ->
                    viewModel.playClick()
                    viewModel.setDifficulty(diff)
                },
                onNavigateToSettings = {
                    viewModel.playClick()
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAbout = {
                    viewModel.playClick()
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                gameState = gameState,
                onArenaSizeChanged = { w, h ->
                    viewModel.onArenaSizeChanged(w, h)
                },
                onStartGame = {
                    viewModel.startGame()
                },
                onDragDelta = { delta ->
                    viewModel.onPlayerDrag(delta)
                },
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    viewModel.playClick()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                gameState = gameState,
                onPlayAgain = {
                    viewModel.playClick()
                    viewModel.restartGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    viewModel.playClick()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferences = preferences,
                onSoundToggled = { enabled ->
                    viewModel.setSoundEnabled(enabled)
                    if (enabled) viewModel.playClick()
                },
                onHapticsToggled = { enabled ->
                    viewModel.setHapticsEnabled(enabled)
                    if (enabled) viewModel.playClick()
                },
                onDifficultyChanged = { diff ->
                    viewModel.playClick()
                    viewModel.setDifficulty(diff)
                },
                onThemeModeChanged = { mode ->
                    viewModel.playClick()
                    viewModel.setThemeMode(mode)
                },
                onResetStatistics = {
                    viewModel.playClick()
                    viewModel.resetStatistics()
                },
                onNavigateBack = {
                    viewModel.playClick()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    viewModel.playClick()
                    navController.popBackStack()
                }
            )
        }
    }
}
