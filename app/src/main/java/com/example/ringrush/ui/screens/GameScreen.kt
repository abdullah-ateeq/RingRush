package com.example.ringrush.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.model.GameState
import com.example.ringrush.model.GameStatus
import com.example.ringrush.ui.components.ArenaCanvas
import com.example.ringrush.ui.components.GameplayHud
import com.example.ringrush.ui.theme.ElectricCyan
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    gameState: GameState,
    onArenaSizeChanged: (Float, Float) -> Unit,
    onStartGame: () -> Unit,
    onDragDelta: (androidx.compose.ui.geometry.Offset) -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // When Game Over happens, allow player to see the collision explosion before navigating
    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GAME_OVER) {
            delay(700)
            onNavigateToResult()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        LaunchedEffect(widthPx, heightPx) {
            onArenaSizeChanged(widthPx, heightPx)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Main Interactive Gameplay Canvas
            ArenaCanvas(
                gameState = gameState,
                onDragDelta = onDragDelta,
                modifier = Modifier.fillMaxSize()
            )

            // Top HUD Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            ) {
                // Exit / Back icon button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Game",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Score, Streak & Multiplier HUD
                GameplayHud(
                    score = gameState.score,
                    streak = gameState.streak,
                    multiplier = gameState.multiplier,
                    ringsPassed = gameState.ringsPassed
                )
            }

            // Start Prompt Overlay if Status == READY
            AnimatedVisibility(
                visible = gameState.status == GameStatus.READY,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onStartGame()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {
                        Text(
                            text = "TOUCH TO START",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = ElectricCyan
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Drag the glowing dot through the opening",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
