package com.example.ringrush.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.data.UserPreferences
import com.example.ringrush.model.Difficulty
import com.example.ringrush.ui.components.ArcadePrimaryButton
import com.example.ringrush.ui.components.SegmentedDifficultySelector
import com.example.ringrush.ui.components.StatCard
import com.example.ringrush.ui.theme.ElectricCyan

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    onStartGame: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Action Bar: Settings & About buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title & Tagline
            Text(
                text = "RINGRUSH",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = ElectricCyan
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Beat the closing ring.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Best Score & Streak Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Best Score",
                    value = String.format("%05d", preferences.bestScore),
                    modifier = Modifier.weight(1f),
                    valueColor = ElectricCyan
                )

                StatCard(
                    label = "Best Streak",
                    value = "${preferences.highestStreak}",
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty Label & Selector
            Text(
                text = "SELECT DIFFICULTY",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SegmentedDifficultySelector(
                selectedDifficulty = preferences.difficulty,
                onDifficultySelected = onDifficultySelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preferences.difficulty.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            )
        }

        // Bottom Play Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            ArcadePrimaryButton(
                text = "Play",
                icon = Icons.Default.PlayArrow,
                onClick = onStartGame
            )
        }
    }
}
