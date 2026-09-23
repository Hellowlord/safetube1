package com.example.ui.components

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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeOrange
import com.example.ui.theme.SafePurple

@Composable
fun TimeLockScreen(
    isBedtimeLock: Boolean,
    onUnlockWithPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = if (isBedtimeLock) {
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFFBBF24))
                    )
                }
            )
            .padding(24.dp)
            .testTag("time_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Big friendly icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBedtimeLock) Color(0xFF4338CA).copy(alpha = 0.5f)
                        else Color.White.copy(alpha = 0.85f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBedtimeLock) "🌙" else "⏰",
                    fontSize = 52.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isBedtimeLock) "Bedtime Lock Active" else "Screen Time is Up for Today!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isBedtimeLock) Color.White else Color(0xFF78350F)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isBedtimeLock) {
                    "It is time to rest your eyes and dream wonderful dreams! SafeTube will be ready again in the morning."
                } else {
                    "Great watching today! Now it's time to play outside, draw, build with Lego, or read a fun book."
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isBedtimeLock) Color(0xFFCBD5E1) else Color(0xFF92400E)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Suggestions Card
            Surface(
                color = if (isBedtimeLock) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBedtimeLock) "✨ Bedtime Routine:" else "🎨 Fun things to do right now:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isBedtimeLock) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (isBedtimeLock) "• Brush teeth and put on cozy pajamas"
                        else "• Draw your favorite character from today's videos",
                        fontSize = 13.sp,
                        color = if (isBedtimeLock) Color(0xFF94A3B8) else Color(0xFF475569)
                    )
                    Text(
                        text = if (isBedtimeLock) "• Read a gentle bedtime story with parents"
                        else "• Play tag outside or do 10 jumping jacks",
                        fontSize = 13.sp,
                        color = if (isBedtimeLock) Color(0xFF94A3B8) else Color(0xFF475569)
                    )
                    Text(
                        text = if (isBedtimeLock) "• Listen to soft music or rain sounds"
                        else "• Help make a delicious healthy snack",
                        fontSize = 13.sp,
                        color = if (isBedtimeLock) Color(0xFF94A3B8) else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Parent Unlock Button
            Button(
                onClick = onUnlockWithPin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBedtimeLock) SafeBlue else SafeOrange
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("parent_unlock_screen_time_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Parent Unlock",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Parent Access & Extend Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
