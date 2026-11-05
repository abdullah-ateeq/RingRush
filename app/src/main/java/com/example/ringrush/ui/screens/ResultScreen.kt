package com.example.ringrush.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.model.GameState
import com.example.ringrush.ui.components.ArcadePrimaryButton
import com.example.ringrush.ui.components.ArcadeSecondaryButton
import com.example.ringrush.ui.components.StatCard
import com.example.ringrush.ui.theme.AccentRed
import com.example.ringrush.ui.theme.ElectricCyan
import com.example.ringrush.ui.theme.SkyBlue

@Composable
fun ResultScreen(
    gameState: GameState,
    onPlayAgain: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "GAME OVER",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = AccentRed
                )
            )

            // Game over reason description
            Text(
                text = gameState.gameOverReason?.message ?: "Game ended.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // New Best Indicator Badge
            AnimatedVisibility(
                visible = gameState.isNewBestScore || gameState.isNewBestStreak,
                enter = fadeIn() + scaleIn()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (gameState.isNewBestScore && gameState.isNewBestStreak) {
                            "★ NEW BEST SCORE & STREAK! ★"
                        } else if (gameState.isNewBestScore) {
                            "★ NEW BEST SCORE! ★"
                        } else {
                            "★ NEW HIGHEST STREAK! ★"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Final Score Showcase
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FINAL SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%05d", gameState.score),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (gameState.isNewBestScore) ElectricCyan else Color.White,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Rings Passed",
                    value = "${gameState.ringsPassed}",
                    modifier = Modifier.weight(1f),
                    valueColor = SkyBlue
                )
                StatCard(
                    label = "Highest Streak",
                    value = "${gameState.streak}",
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Best Score",
                    value = String.format("%05d", gameState.bestScore),
                    modifier = Modifier.weight(1f),
                    valueColor = ElectricCyan
                )
                StatCard(
                    label = "Difficulty",
                    value = gameState.difficulty.title,
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            ArcadePrimaryButton(
                text = "Play Again",
                icon = Icons.Default.Refresh,
                onClick = onPlayAgain
            )

            Spacer(modifier = Modifier.height(10.dp))

            ArcadeSecondaryButton(
                text = "Home",
                icon = Icons.Default.Home,
                onClick = onNavigateToHome
            )
        }
    }
}
