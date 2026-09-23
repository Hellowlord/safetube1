package com.example.ui.screens.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ParentSettingsEntity
import com.example.data.db.WatchActivityEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.ui.components.ThemeModeSelectorCard
import com.example.ui.theme.SafeAmber
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeBlueLight
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeOrange
import com.example.ui.theme.SafePurple
import com.example.ui.theme.SafeTeal
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentDashboardScreen(
    settings: ParentSettingsEntity?,
    activities: List<WatchActivityEntity>,
    onUpdateSettings: (ParentSettingsEntity) -> Unit,
    onResetTodayScreenTime: () -> Unit,
    onClearHistory: () -> Unit,
    onCloseDashboard: () -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (settings == null) return

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Screen Time, 1: Content Filtering, 2: Activity Reports

    // Screen Time Slider state
    var limitMinutes by remember(settings.dailyScreenTimeMinutes) {
        mutableFloatStateOf(settings.dailyScreenTimeMinutes.toFloat())
    }

    // New keyword input state
    var newKeywordInput by remember { mutableStateOf("") }

    val blockedKeywordsList = remember(settings.blockedKeywords) {
        settings.blockedKeywords.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("parent_dashboard_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SafeBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Parent Dashboard",
                                    tint = SafeBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Parental Dashboard",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Parental Controls are Active by Default",
                                    fontSize = 12.sp,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onCloseDashboard,
                            modifier = Modifier.testTag("close_parent_dashboard_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dashboard")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Tabs: Screen Time, Content Filters, Activity Reports
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("⏱️ Screen Time", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("tab_screen_time")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("🎯 Filtering", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("tab_content_filter")
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("📊 Reports", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("tab_activity_reports")
                        )
                    }
                }
            }
        }

        // ==================== TAB 0: SCREEN TIME LIMITS ====================
        if (selectedTab == 0) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Today's Usage Gauge Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Screen Time",
                                        tint = SafeBlue
                                    )
                                    Text(
                                        text = "Today's Screen Time",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Text(
                                    text = "${settings.screenTimeUsedSecondsToday / 60}m / ${settings.dailyScreenTimeMinutes}m",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (settings.screenTimeUsedSecondsToday >= settings.dailyScreenTimeMinutes * 60) SafeCoral else SafeBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val progress = if (settings.dailyScreenTimeMinutes > 0) {
                                (settings.screenTimeUsedSecondsToday.toFloat() / (settings.dailyScreenTimeMinutes * 60f)).coerceIn(0f, 1f)
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = if (progress >= 1f) SafeCoral else SafeBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onResetTodayScreenTime,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reset_screen_time_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Today", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val extended = (settings.dailyScreenTimeMinutes + 15).coerceAtMost(180)
                                        onUpdateSettings(settings.copy(dailyScreenTimeMinutes = extended))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bonus_15m_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+15 Min Bonus", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Daily Screen Time Limit Slider
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Daily Screen Limit: ${limitMinutes.toInt()} Minutes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "SafeTube will pause videos and show a healthy stretch/outside reminder when time expires.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Slider(
                                value = limitMinutes,
                                onValueChange = { limitMinutes = it },
                                onValueChangeFinished = {
                                    onUpdateSettings(settings.copy(dailyScreenTimeMinutes = limitMinutes.toInt()))
                                },
                                valueRange = 15f..120f,
                                steps = 6, // 15, 30, 45, 60, 75, 90, 105, 120
                                colors = SliderDefaults.colors(
                                    thumbColor = SafeBlue,
                                    activeTrackColor = SafeBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("screen_time_limit_slider")
                            )

                            // Preset Quick Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(30, 45, 60, 90).forEach { preset ->
                                    val isSelected = settings.dailyScreenTimeMinutes == preset
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clickable {
                                                limitMinutes = preset.toFloat()
                                                onUpdateSettings(settings.copy(dailyScreenTimeMinutes = preset))
                                            }
                                            .testTag("preset_${preset}m")
                                    ) {
                                        Text(
                                            text = "${preset}m",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bedtime Lockout Hours
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = "Bedtime Lock",
                                        tint = SafePurple
                                    )
                                    Column {
                                        Text(
                                            text = "Bedtime Lockout",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Locks app automatically during sleep hours",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }
                                }

                                Switch(
                                    checked = settings.bedtimeLockEnabled,
                                    onCheckedChange = {
                                        onUpdateSettings(settings.copy(bedtimeLockEnabled = it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SafePurple
                                    ),
                                    modifier = Modifier.testTag("bedtime_lock_switch")
                                )
                            }

                            if (settings.bedtimeLockEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "🌙 Starts at", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(text = "${settings.bedtimeStartHour}:00 PM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text(text = "→", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "☀️ Unlocks at", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            Text(text = "${settings.bedtimeEndHour}:00 AM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Appearance & Theme Controls for Kids
                    ThemeModeSelectorCard(
                        selectedMode = themeMode,
                        onSelectMode = onSetThemeMode
                    )
                }
            }
        }

        // ==================== TAB 1: CONTENT FILTERING SYSTEM ====================
        if (selectedTab == 1) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Age Bracket Setting Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Age-Appropriate Content Filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Only videos approved for the selected bracket will be shown to your child.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AgeGroup.values().forEach { age ->
                                val isSelected = settings.selectedAgeFilter == age.name
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SafeBlue.copy(alpha = 0.12f) else Color.Transparent,
                                    border = if (isSelected) BorderStroke(1.5.dp, SafeBlue) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onUpdateSettings(settings.copy(selectedAgeFilter = age.name))
                                        }
                                        .testTag("filter_age_${age.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(text = age.iconEmoji, fontSize = 20.sp)
                                            Column {
                                                Text(text = age.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = age.ageRange, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = SafeBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Safe Shorts Toggle
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SafeOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "⚡", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        text = "Allow Safe Shorts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Kept OFF by default to eliminate doomscrolling habits",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Switch(
                                checked = settings.allowShorts,
                                onCheckedChange = {
                                    onUpdateSettings(settings.copy(allowShorts = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SafeOrange
                                ),
                                modifier = Modifier.testTag("allow_shorts_parent_switch")
                            )
                        }
                    }

                    // Custom Keyword Blacklist Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Blocked Keywords Filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Videos containing these words in titles or descriptions will be hidden automatically.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Add new word row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKeywordInput,
                                    onValueChange = { newKeywordInput = it },
                                    placeholder = { Text("Add blocked word...", fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_keyword_input")
                                )
                                Button(
                                    onClick = {
                                        if (newKeywordInput.isNotBlank()) {
                                            blockedKeywordsList.add(newKeywordInput.trim().lowercase())
                                            onUpdateSettings(settings.copy(blockedKeywords = blockedKeywordsList.joinToString(",")))
                                            newKeywordInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                                    modifier = Modifier.testTag("add_keyword_button")
                                ) {
                                    Text("Add")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Chips for blocked words
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                blockedKeywordsList.forEach { word ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SafeCoral.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, SafeCoral.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = word,
                                                fontSize = 12.sp,
                                                color = SafeCoral,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $word",
                                                tint = SafeCoral,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        blockedKeywordsList.remove(word)
                                                        onUpdateSettings(settings.copy(blockedKeywords = blockedKeywordsList.joinToString(",")))
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== TAB 2: ACTIVITY REPORTS & ENGAGEMENT ====================
        if (selectedTab == 2) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Weekly Screen Time Bar Chart Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Weekly Watch Engagement",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Daily viewing minutes tracking",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "Healthy Pace",
                                        color = SafeGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Custom Bar Chart representation for the 7 days of the week
                            val days = listOf("Mon" to 32, "Tue" to 28, "Wed" to 40, "Thu" to 22, "Fri" to 35, "Sat" to 45, "Today" to (settings.screenTimeUsedSecondsToday / 60))
                            val maxMinutes = 60

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                days.forEach { (day, minutes) ->
                                    val heightFraction = (minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
                                    val isToday = day == "Today"

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${minutes}m",
                                            fontSize = 10.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(18.dp)
                                                .height((100 * heightFraction).dp)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(if (isToday) SafeBlue else SafeBlueLight.copy(alpha = 0.4f))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = day,
                                            fontSize = 11.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Breakdown Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Learning Category Distribution",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Where your child is spending their time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            CategoryDistributionBar(label = "🔬 Science & Space", percentage = 42, color = SafeBlue)
                            CategoryDistributionBar(label = "🦁 Nature & Animals", percentage = 28, color = SafeTeal)
                            CategoryDistributionBar(label = "✂️ Arts & Crafts", percentage = 18, color = SafeOrange)
                            CategoryDistributionBar(label = "🌙 Bedtime & Calm", percentage = 12, color = SafePurple)
                        }
                    }

                    // Chronological Activity Log
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = SafeBlue)
                                    Text(
                                        text = "Watched Videos Log",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (activities.isNotEmpty()) {
                                    IconButton(onClick = onClearHistory) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Clear History",
                                            tint = SafeCoral.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (activities.isEmpty()) {
                                Text(
                                    text = "No watched videos recorded yet today. Videos watched by your child will appear here with exact durations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                activities.take(15).forEach { act ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = act.videoTitle,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${act.channelTitle} • ${act.category}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "${act.durationWatchedSeconds / 60}m ${act.durationWatchedSeconds % 60}s",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SafeBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDistributionBar(label: String, percentage: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$percentage%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
