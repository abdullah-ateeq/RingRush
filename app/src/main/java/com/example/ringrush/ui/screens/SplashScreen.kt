package com.example.ringrush.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringrush.ui.theme.ElectricCyan
import com.example.ringrush.ui.theme.SkyBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ringScale = remember { Animatable(0.2f) }
    val ringAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        ringAlpha.animateTo(1f, animationSpec = tween(400))
        ringScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(750, easing = FastOutSlowInEasing)
        )
        textAlpha.animateTo(1f, animationSpec = tween(450))
        delay(900)
        onNavigateToHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onNavigateToHome()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Glowing Ring Icon
            Canvas(modifier = Modifier.size(140.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.width * 0.42f) * ringScale.value
                val strokeWidth = 5.dp.toPx()

                // Outer faint guidance ring
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.2f * ringAlpha.value),
                    radius = baseRadius * 1.25f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Main Ring with open gate
                drawArc(
                    color = ElectricCyan.copy(alpha = ringAlpha.value),
                    startAngle = 55f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                    size = Size(baseRadius * 2f, baseRadius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Core glowing player dot
                drawCircle(
                    color = ElectricCyan.copy(alpha = 0.35f * ringAlpha.value),
                    radius = 24.dp.toPx() * ringScale.value,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = ringAlpha.value),
                    radius = 8.dp.toPx() * ringScale.value,
                    center = center
                )
                drawCircle(
                    color = SkyBlue.copy(alpha = ringAlpha.value),
                    radius = 4.dp.toPx() * ringScale.value,
                    center = center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Game Title
            Text(
                text = "RINGRUSH",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = ElectricCyan.copy(alpha = textAlpha.value)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "BEAT THE CLOSING RING.",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = textAlpha.value)
                )
            )
        }
    }
}
