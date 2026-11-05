package com.example.ringrush.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result")
    object Settings : Screen("settings")
    object About : Screen("about")
}
