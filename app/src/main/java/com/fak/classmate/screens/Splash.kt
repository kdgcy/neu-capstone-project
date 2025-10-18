package com.fak.classmate.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

@Composable
fun Splash(modifier: Modifier = Modifier, navController: NavController) {
    // Animation states
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val progressAlpha = remember { Animatable(0f) }

    // Floating circles
    val circle1Offset = remember { Animatable(0f) }
    val circle2Offset = remember { Animatable(0f) }
    val circle3Offset = remember { Animatable(0f) }

    // Check authentication and navigate
    LaunchedEffect(Unit) {
        // Start floating animations
        circle1Offset.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(50)
        circle2Offset.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(100)
        circle3Offset.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        // Logo entrance - scale and rotate
        logoScale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )

        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )

        // Slight bounce back
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(200)
        )

        // App name
        delay(100)
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )

        // Tagline
        delay(100)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )

        // Progress bar
        delay(100)
        progressAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )

        // Wait a bit more for effect
        delay(800)

        // Navigate
        val isLoggedIn = Firebase.auth.currentUser != null
        val destination = if (isLoggedIn) "home" else "auth"

        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    // Gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating decorative circles
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = (-250 + circle1Offset.value * 20).dp)
                .size(80.dp)
                .alpha(0.15f)
                .clip(CircleShape)
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-150 + circle2Offset.value * 25).dp)
                .size(100.dp)
                .alpha(0.1f)
                .clip(CircleShape)
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .offset(x = 280.dp, y = (100 + circle3Offset.value * 15).dp)
                .size(70.dp)
                .alpha(0.2f)
                .clip(CircleShape)
                .background(Color.White)
        )

        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (250 + circle1Offset.value * 30).dp)
                .size(90.dp)
                .alpha(0.12f)
                .clip(CircleShape)
                .background(Color.White)
        )

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo with glow effect
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // White circle background
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "ClassMate Logo",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with fade in
            Text(
                text = "ClassMate",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha.value),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline with fade in
            Text(
                text = "Your academic sidekick for tackling\ntasks, deadlines and everything in between!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Animated progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(progressAlpha.value)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.padding(horizontal = 64.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Loading your workspace...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Version at bottom
        Text(
            text = "v1.0.0",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(progressAlpha.value)
                .padding(bottom = 32.dp)
        )
    }
}