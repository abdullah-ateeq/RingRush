package com.example.ringrush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.model.Difficulty
import com.example.ringrush.ui.theme.ElectricCyan

@Composable
fun SegmentedDifficultySelector(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { diff ->
            val isSelected = diff == selectedDifficulty
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ElectricCyan else Color.Transparent,
                animationSpec = tween(durationMillis = 180),
                label = "diffBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF080A0F) else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 180),
                label = "diffText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bgColor)
                    .clickable { onDifficultySelected(diff) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = diff.title.uppercase(),
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
