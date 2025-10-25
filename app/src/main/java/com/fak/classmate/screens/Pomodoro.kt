package com.fak.classmate.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

enum class TimerState {
    IDLE, RUNNING, PAUSED
}

enum class SessionType(val displayName: String, val duration: Long, val color: Color) {
    WORK("Focus Time", 25 * 60 * 1000L, Color(0xFF4DB6AC)),      // 25 minutes - Teal
    SHORT_BREAK("Short Break", 5 * 60 * 1000L, Color(0xFF81C784)), // 5 minutes - Green
    LONG_BREAK("Long Break", 15 * 60 * 1000L, Color(0xFF64B5F6))  // 15 minutes - Blue
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pomodoro(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var currentSession by remember { mutableStateOf(SessionType.WORK) }
    var timeLeftMillis by remember { mutableStateOf(currentSession.duration) }
    var completedSessions by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Timer logic
    LaunchedEffect(timerState, timeLeftMillis) {
        if (timerState == TimerState.RUNNING && timeLeftMillis > 0) {
            delay(1000L)
            timeLeftMillis -= 1000L

            // Timer completed
            if (timeLeftMillis <= 0) {
                timerState = TimerState.IDLE

                // Increment completed sessions
                if (currentSession == SessionType.WORK) {
                    completedSessions++

                    // Switch to break
                    currentSession = if (completedSessions % 4 == 0) {
                        SessionType.LONG_BREAK
                    } else {
                        SessionType.SHORT_BREAK
                    }
                } else {
                    // Switch back to work
                    currentSession = SessionType.WORK
                }

                timeLeftMillis = currentSession.duration
            }
        }
    }

    // Format time display
    val minutes = (timeLeftMillis / 1000) / 60
    val seconds = (timeLeftMillis / 1000) % 60
    val timeDisplay = String.format("%02d:%02d", minutes, seconds)

    // Calculate progress for animation
    val progress = 1f - (timeLeftMillis.toFloat() / currentSession.duration.toFloat())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pomodoro Timer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Session info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = currentSession.color.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentSession.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = currentSession.color
                    )
                    Text(
                        text = "Sessions completed: $completedSessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Circular Timer Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(320.dp)
            ) {
                // Animated progress circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val strokeWidth = 12.dp.toPx()
                    val radius = (canvasSize - strokeWidth) / 2

                    // Background circle
                    drawCircle(
                        color = currentSession.color.copy(alpha = 0.2f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress arc
                    if (progress > 0f) {
                        drawArc(
                            color = currentSession.color,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(
                                (canvasSize - radius * 2) / 2,
                                (canvasSize - radius * 2) / 2
                            ),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Timer circle with time display
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(currentSession.color),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timeDisplay,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Play/Pause icon
                        Icon(
                            imageVector = if (timerState == TimerState.RUNNING) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Start/Pause Button
                Button(
                    onClick = {
                        timerState = when (timerState) {
                            TimerState.IDLE, TimerState.PAUSED -> TimerState.RUNNING
                            TimerState.RUNNING -> TimerState.PAUSED
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = when (timerState) {
                            TimerState.IDLE -> "Start"
                            TimerState.RUNNING -> "Pause"
                            TimerState.PAUSED -> "Resume"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Reset Button
                OutlinedButton(
                    onClick = {
                        timerState = TimerState.IDLE
                        timeLeftMillis = currentSession.duration
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Timer info
            Text(
                text = "💡 Tip: Take regular breaks to maintain focus and productivity!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}