package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen

@Composable
fun KidFriendlyTimerBadge(
    dailyLimitMinutes: Int,
    usedSeconds: Int,
    parentalControlsEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!parentalControlsEnabled || dailyLimitMinutes <= 0) {
        // Unlimited / timer off
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE0F2FE))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("timer_badge_unlimited")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🛡️", fontSize = 14.sp)
                Text(
                    text = "Safe Protected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0369A1)
                )
            }
        }
        return
    }

    val totalSeconds = dailyLimitMinutes * 60
    val remainingSeconds = (totalSeconds - usedSeconds).coerceAtLeast(0)
    val remainingMinutes = (remainingSeconds + 59) / 60

    val (bgColor, textColor, iconColor) = when {
        remainingMinutes > 15 -> Triple(Color(0xFFDCFCE7), SafeGreen, SafeGreen)
        remainingMinutes > 5 -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), SafeAmber)
        else -> Triple(Color(0xFFFFE4E6), SafeCoral, SafeCoral)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("timer_badge_active")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Screen Time Remaining",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (remainingMinutes > 0) "$remainingMinutes min left" else "Time's up!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
