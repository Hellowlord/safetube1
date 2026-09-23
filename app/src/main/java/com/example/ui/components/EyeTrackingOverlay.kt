package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.tracking.TrackingConfig
import com.example.service.tracking.TrackingState
import kotlinx.coroutines.delay

@Composable
fun EyeTrackingOverlay(
    state: TrackingState,
    config: TrackingConfig,
    modifier: Modifier = Modifier
) {
    if (!config.isTrackingEnabled) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("eye_tracking_overlay")
    ) {
        // 1. Hands-Free Head Pointer Cursor
        if (config.isPointerCursorEnabled && state.isFaceDetected) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Map normalized 0.0..1.0 coordinates to offset
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    androidx.compose.foundation.layout.BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val posX = (constraints.maxWidth * state.pointerX).toInt()
                        val posY = (constraints.maxHeight * state.pointerY).toInt()

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(posX - 18, posY - 18) }
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x334285F4))
                                .border(2.dp, Color(0xFF4285F4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }

        // 2. Smart Attention Lost Alert (Screen dimmed overlay reminder)
        AnimatedVisibility(
            visible = state.isPausedByAttention,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4285F4)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(24.dp)
                    .testTag("attention_paused_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4285F4).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Attention Paused",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Smart Attention: Video Paused",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Look back at the screen to resume playing",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. Eye Posture / Distance Alert
        AnimatedVisibility(
            visible = state.isTooCloseToScreen && config.isDistanceAlertEnabled,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE65100).copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("distance_alert_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Too close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Eye Health Alert: Please move further from the screen",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 4. Temporary Gesture Feedback Pill (Shows for 1.8s upon action)
        var showActionPill by remember { mutableStateOf(false) }
        var actionText by remember { mutableStateOf("") }

        LaunchedEffect(state.lastActionTimestamp) {
            if (state.lastActionMessage != null && state.lastActionTimestamp > 0) {
                actionText = state.lastActionMessage
                showActionPill = true
                delay(1800)
                showActionPill = false
            }
        }

        AnimatedVisibility(
            visible = showActionPill,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4)),
                modifier = Modifier.testTag("gesture_feedback_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.lastGesture.icon,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actionText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 5. Live Debug HUD (If enabled)
        if (config.showDebugHUD) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .testTag("tracking_debug_hud")
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.isFaceDetected) Color.Green else Color.Red)
                        )
                        Text(
                            text = if (state.isFaceDetected) "Face Locked" else "Searching...",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Yaw: ${"%.1f".format(state.headYaw)}°  Pitch: ${"%.1f".format(state.headPitch)}°",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Roll: ${"%.1f".format(state.headRoll)}°  Eyes: L ${"%.2f".format(state.leftEyeOpenProb)} R ${"%.2f".format(state.rightEyeOpenProb)}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = "Gesture: ${state.lastGesture.displayName}",
                        color = Color(0xFF4285F4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
