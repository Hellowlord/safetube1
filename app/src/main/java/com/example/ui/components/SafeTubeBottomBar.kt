package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomTab
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafePurple
import com.example.ui.theme.YouTubeRed

@Composable
fun SafeTubeBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == BottomTab.HOME,
                activeColor = SafeBlue,
                testTag = "nav_tab_home",
                onClick = { onTabSelected(BottomTab.HOME) }
            )

            BottomNavItem(
                icon = Icons.Default.FlashOn,
                label = "Shorts",
                isSelected = selectedTab == BottomTab.SHORTS,
                activeColor = YouTubeRed,
                testTag = "nav_tab_shorts",
                onClick = { onTabSelected(BottomTab.SHORTS) }
            )

            BottomNavItem(
                icon = Icons.Default.MusicNote,
                label = "Music",
                isSelected = selectedTab == BottomTab.MUSIC,
                activeColor = SafePurple,
                testTag = "nav_tab_music",
                onClick = { onTabSelected(BottomTab.MUSIC) }
            )

            BottomNavItem(
                icon = Icons.Default.AddCircle,
                label = "Create",
                isSelected = selectedTab == BottomTab.CREATE,
                activeColor = SafeCoral,
                testTag = "nav_tab_create",
                onClick = { onTabSelected(BottomTab.CREATE) }
            )

            BottomNavItem(
                icon = Icons.Default.WifiOff,
                label = "Offline",
                isSelected = selectedTab == BottomTab.OFFLINE,
                activeColor = SafeGreen,
                testTag = "nav_tab_offline",
                onClick = { onTabSelected(BottomTab.OFFLINE) }
            )

            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedTab == BottomTab.SETTINGS,
                activeColor = SafeBlue,
                testTag = "nav_tab_settings",
                onClick = { onTabSelected(BottomTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
