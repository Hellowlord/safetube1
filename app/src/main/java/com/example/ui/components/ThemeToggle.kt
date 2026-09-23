package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeOrange
import com.example.ui.theme.SafeTeal
import com.example.ui.theme.ThemeMode

/**
 * Compact icon button for TopBars and Headers that cycles between System, Light, and Dark modes.
 */
@Composable
fun ThemeToggleIconButton(
    currentMode: ThemeMode,
    onToggle: () -> Unit,
    onOpenDialog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDarkEffective = currentMode.isDark(isSystemDark)

    val icon: ImageVector = when (currentMode) {
        ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
    }

    val iconTint: Color = when (currentMode) {
        ThemeMode.SYSTEM -> SafeTeal
        ThemeMode.LIGHT -> SafeOrange
        ThemeMode.DARK -> SafeBlue
    }

    val tooltipText = when (currentMode) {
        ThemeMode.SYSTEM -> "System Auto (${if (isSystemDark) "Dark" else "Light"})"
        ThemeMode.LIGHT -> "Light Mode"
        ThemeMode.DARK -> "Dark Mode"
    }

    val animatedBg by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "theme_btn_bg"
    )
    val animatedTint by animateColorAsState(
        targetValue = iconTint,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "theme_btn_tint"
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(animatedBg)
            .testTag("theme_toggle_button")
    ) {
        AnimatedContent(
            targetState = currentMode,
            transitionSpec = {
                (fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) + 
                        scaleIn(initialScale = 0.8f, animationSpec = tween(350, easing = FastOutSlowInEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(350, easing = FastOutSlowInEasing)) + 
                        scaleOut(targetScale = 0.8f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                    )
            },
            label = "ThemeIconCrossFade"
        ) { mode ->
            Icon(
                imageVector = when (mode) {
                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                },
                contentDescription = tooltipText,
                tint = animatedTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Complete Theme Selector Card embedded in Settings and Parent Dashboard.
 */
@Composable
fun ThemeModeSelectorCard(
    selectedMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDarkEffective = selectedMode.isDark(isSystemDark)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_selector_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SafeBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Palette",
                        tint = SafeBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Appearance & Color Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Customize the look and feel across all screens",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                // Active status pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDarkEffective) SafeBlue.copy(alpha = 0.15f) else SafeOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (selectedMode) {
                            ThemeMode.SYSTEM -> "Auto (${if (isSystemDark) "Dark" else "Light"})"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        },
                        color = if (isDarkEffective) SafeBlue else SafeOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Three Segmented Choice Tiles: System, Light, Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionTile(
                    mode = ThemeMode.SYSTEM,
                    isSelected = selectedMode == ThemeMode.SYSTEM,
                    currentActive = if (isSystemDark) "Dark" else "Light",
                    onSelect = { onSelectMode(ThemeMode.SYSTEM) },
                    testTag = "theme_mode_system_button",
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionTile(
                    mode = ThemeMode.LIGHT,
                    isSelected = selectedMode == ThemeMode.LIGHT,
                    currentActive = null,
                    onSelect = { onSelectMode(ThemeMode.LIGHT) },
                    testTag = "theme_mode_light_button",
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionTile(
                    mode = ThemeMode.DARK,
                    isSelected = selectedMode == ThemeMode.DARK,
                    currentActive = null,
                    onSelect = { onSelectMode(ThemeMode.DARK) },
                    testTag = "theme_mode_dark_button",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explanatory note
            Text(
                text = when (selectedMode) {
                    ThemeMode.SYSTEM -> "SafeTube automatically mirrors your Android system theme (${if (isSystemDark) "currently Dark Mode" else "currently Light Mode"})."
                    ThemeMode.LIGHT -> "Always use the vibrant daylight color scheme with bright backgrounds and high readability."
                    ThemeMode.DARK -> "Always use the gentle midnight color scheme designed for bedtime viewing and reduced glare."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ThemeOptionTile(
    mode: ThemeMode,
    isSelected: Boolean,
    currentActive: String?,
    onSelect: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) SafeBlue else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) {
        SafeBlue.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .clickable(onClick = onSelect)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) SafeBlue else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.iconEmoji,
                    fontSize = 18.sp
                )
            }

            Text(
                text = when (mode) {
                    ThemeMode.SYSTEM -> "System"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface
            )

            if (currentActive != null) {
                Text(
                    text = "($currentActive)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = if (isSelected) "Active" else "Select",
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Compact Dialog for selecting theme mode anywhere (e.g. video player or top bar).
 */
@Composable
fun ThemeQuickSelectionDialog(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = SafeBlue
                )
                Text(
                    text = "Display Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Choose how you'd like SafeTube to display:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ThemeMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SafeBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) BorderStroke(1.5.dp, SafeBlue) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMode(mode)
                                onDismiss()
                            }
                            .testTag("quick_theme_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = mode.iconEmoji, fontSize = 22.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.subtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = SafeBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SafeBlue)
            ) {
                Text("Done")
            }
        }
    )
}
