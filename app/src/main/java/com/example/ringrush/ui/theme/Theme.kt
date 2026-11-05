package com.example.ringrush.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.ringrush.data.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = BackgroundDark,
    primaryContainer = DarkNavyContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = SkyBlue,
    onSecondary = BackgroundDark,
    tertiary = AccentAmber,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMuted,
    outline = OutlineDark,
    error = AccentRed,
    onError = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDim,
    onPrimary = TextWhite,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = CyanDim,
    secondary = SkyBlue,
    onSecondary = TextWhite,
    tertiary = AccentAmber,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMutedLight,
    outline = OutlineLight,
    error = AccentRed,
    onError = TextWhite
)

@Composable
fun RingRushTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
