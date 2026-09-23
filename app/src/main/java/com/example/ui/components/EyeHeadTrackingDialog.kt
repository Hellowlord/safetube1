package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.tracking.TrackingConfig
import com.example.service.tracking.TrackingGesture
import com.example.service.tracking.TrackingSensitivity
import com.example.service.tracking.TrackingState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EyeHeadTrackingDialog(
    config: TrackingConfig,
    state: TrackingState,
    onDismiss: () -> Unit,
    onUpdateConfig: ((TrackingConfig) -> TrackingConfig) -> Unit,
    onSimulateGesture: (TrackingGesture) -> Unit,
    onRequestCameraPermission: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            onRequestCameraPermission()
        }
    }

    val primaryColor = Color(0xFF1A73E8)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Eye & Head Tracking Service",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "AI-Powered Hands-Free Accessibility Controls",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Camera Permission Banner if not granted
                if (!hasCameraPermission) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("tracking_camera_permission_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Camera Permission Required",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Real-time face & gaze tracking runs 100% locally on-device.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_camera_permission_button")
                            ) {
                                Text("Grant", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Master Toggle Switch
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (config.isTrackingEnabled) primaryColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.5.dp, if (config.isTrackingEnabled) primaryColor else Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (config.isTrackingEnabled) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = if (config.isTrackingEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Tracking Service",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (config.isTrackingEnabled) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (config.isTrackingEnabled) "Active • Tracking Eyes & Head" else "Disabled",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = config.isTrackingEnabled,
                            onCheckedChange = { isEnabled ->
                                if (isEnabled && !hasCameraPermission) {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                                onUpdateConfig { it.copy(isTrackingEnabled = isEnabled) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor),
                            modifier = Modifier.testTag("master_tracking_switch")
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Feature Items
                Text(
                    text = "Tracking Features",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = primaryColor
                )

                TrackingToggleItem(
                    title = "Smart Attention Pause",
                    subtitle = "Auto-pause video when looking away, resume when looking back",
                    icon = Icons.Default.Visibility,
                    checked = config.isSmartAttentionEnabled,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_smart_attention",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(isSmartAttentionEnabled = it) } }
                )

                TrackingToggleItem(
                    title = "Head Tilt Seeking",
                    subtitle = "Tilt head left/right to rewind or forward 10 seconds",
                    icon = Icons.Default.FastForward,
                    checked = config.isHeadTiltSeekEnabled,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_head_tilt_seek",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(isHeadTiltSeekEnabled = it) } }
                )

                TrackingToggleItem(
                    title = "Eye Blink & Wink Controls",
                    subtitle = "Double blink to play/pause, wink left/right to seek",
                    icon = Icons.Default.CenterFocusStrong,
                    checked = config.isBlinkControlEnabled,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_blink_control",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(isBlinkControlEnabled = it) } }
                )

                TrackingToggleItem(
                    title = "Eye Safety Distance Alert",
                    subtitle = "Notifies when face is too close to the screen (< 30cm)",
                    icon = Icons.Default.Warning,
                    checked = config.isDistanceAlertEnabled,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_distance_alert",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(isDistanceAlertEnabled = it) } }
                )

                TrackingToggleItem(
                    title = "Hands-Free Head Pointer",
                    subtitle = "Steer a floating cursor across the screen with your head",
                    icon = Icons.Default.TouchApp,
                    checked = config.isPointerCursorEnabled,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_head_pointer",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(isPointerCursorEnabled = it) } }
                )

                TrackingToggleItem(
                    title = "Live Metrics HUD",
                    subtitle = "Show real-time pitch, yaw, roll & eye status overlay",
                    icon = Icons.Default.Info,
                    checked = config.showDebugHUD,
                    enabled = config.isTrackingEnabled,
                    testTag = "toggle_debug_hud",
                    onCheckedChange = { onUpdateConfig { cfg -> cfg.copy(showDebugHUD = it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Sensitivity Selector
                Text(
                    text = "Motion Sensitivity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = primaryColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrackingSensitivity.values().forEach { sens ->
                        val isSelected = config.sensitivity == sens
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = config.isTrackingEnabled) {
                                    onUpdateConfig { it.copy(sensitivity = sens) }
                                }
                                .testTag("sensitivity_${sens.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = sens.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (sens) {
                                        TrackingSensitivity.LOW -> "Gentle"
                                        TrackingSensitivity.MEDIUM -> "Balanced"
                                        TrackingSensitivity.HIGH -> "Fast"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Interactive Simulation Sandbox
                Text(
                    text = "Test Tracking Gestures",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = primaryColor
                )
                Text(
                    text = "Tap to simulate any gesture on this device:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SimulationChip(label = "👀 Look Away", testTag = "sim_look_away") {
                        onSimulateGesture(TrackingGesture.LOOKING_AWAY)
                    }
                    SimulationChip(label = "👁️ Look Back", testTag = "sim_look_back") {
                        onSimulateGesture(TrackingGesture.ATTENTION_RESUMED)
                    }
                    SimulationChip(label = "⏩ Tilt Right (+10s)", testTag = "sim_tilt_right") {
                        onSimulateGesture(TrackingGesture.HEAD_TILT_RIGHT)
                    }
                    SimulationChip(label = "⏪ Tilt Left (-10s)", testTag = "sim_tilt_left") {
                        onSimulateGesture(TrackingGesture.HEAD_TILT_LEFT)
                    }
                    SimulationChip(label = "😉 Blink (Play/Pause)", testTag = "sim_blink") {
                        onSimulateGesture(TrackingGesture.DOUBLE_BLINK)
                    }
                    SimulationChip(label = "⚠️ Too Close Alert", testTag = "sim_too_close") {
                        onSimulateGesture(TrackingGesture.TOO_CLOSE)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("close_tracking_dialog_button")
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun TrackingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SimulationChip(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
