package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.OfflineVideoEntity
import com.example.data.db.ParentSettingsEntity
import com.example.data.db.UserProfileEntity
import com.example.data.db.WatchHistoryEntity
import com.example.data.db.WatchLaterEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.components.TrendingVideoCard
import com.example.ui.components.VideoCard
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeOrange
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.YouTubeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    shorts: List<VideoItem>,
    trendingVideos: List<VideoItem> = emptyList(),
    isTrendingLoading: Boolean = false,
    offlineVideos: List<OfflineVideoEntity>,
    watchHistory: List<WatchHistoryEntity> = emptyList(),
    watchLater: List<WatchLaterEntity> = emptyList(),
    parentSettings: ParentSettingsEntity?,
    activeProfile: UserProfileEntity?,
    selectedCategory: VideoCategory,
    selectedAgeGroup: AgeGroup,
    searchQuery: String = "",
    isSearching: Boolean = false,
    searchSuggestions: List<String> = emptyList(),
    searchHistory: List<String> = emptyList(),
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onDeleteSearchHistoryItem: (String) -> Unit = {},
    onClearSearchHistory: () -> Unit = {},
    onCategorySelected: (VideoCategory) -> Unit,
    onAgeGroupSelected: (AgeGroup) -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onToggleOffline: (VideoItem) -> Unit,
    onOpenParentDashboard: () -> Unit,
    onOpenOfflineMode: () -> Unit,
    onOpenWatchHistory: () -> Unit = {},
    onRemoveWatchHistoryItem: (String) -> Unit = {},
    onOpenWatchLater: () -> Unit = {},
    onRemoveWatchLaterItem: (String) -> Unit = {},
    onSwitchAccount: () -> Unit = {},
    onOpenShorts: () -> Unit = {},
    downloadProgress: Map<String, Float> = emptyMap(),
    isOnline: Boolean = true,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleTheme: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val offlineIds = offlineVideos.map { it.videoId }.toSet()
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var isDropdownManuallyOpened by remember { mutableStateOf(false) }

    val filteredHistory = remember(searchHistory, searchQuery) {
        if (searchQuery.isBlank()) {
            searchHistory
        } else {
            searchHistory.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val showDropdown = isSearchFocused || isDropdownManuallyOpened

    val creatorVideos = remember(videos) {
        videos.filter {
            it.channelTitle.contains("Technoblade", ignoreCase = true) ||
            it.channelTitle.contains("MrBeast", ignoreCase = true) ||
            it.channelTitle.contains("Mark Rober", ignoreCase = true) ||
            it.category == VideoCategory.CREATORS ||
            (it.category == VideoCategory.GAMING && !it.channelTitle.contains("Music", ignoreCase = true))
        }.distinctBy { it.id }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_feed_pull_to_refresh")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("home_screen"),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        // Compact Unified App Header (Reduced spacing, explicit "Powered by AdBlock", search & categories)
        item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Row 1: Brand Logo, "Powered by AdBlock" badge, Profile & Downloads
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand SafeTube
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(listOf(YouTubeRed, SafeOrange))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "SafeTube Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "SafeTube",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(5.dp),
                                        color = YouTubeRed.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "AD-FREE",
                                            color = YouTubeRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                // Explicit "Powered by AdBlock" line
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "AdBlock Shield",
                                        tint = SafeGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "Powered by AdBlock • Zero Ads Guaranteed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen
                                    )
                                }
                            }
                        }

                        // Right actions: AdBlock Status Pill, Downloads & Switch Account
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Prominent "Powered by AdBlock" badge with green pulse indicator
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SafeGreen.copy(alpha = 0.14f),
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(SafeGreen)
                                    )
                                    Text(
                                        text = "AdBlock ON",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SafeGreen
                                    )
                                }
                            }

                            // Offline Downloads button
                            IconButton(
                                onClick = onOpenOfflineMode,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Saved Offline Videos",
                                    tint = SafeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Watch History button
                            IconButton(
                                onClick = onOpenWatchHistory,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("watch_history_nav_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Watch History",
                                    tint = SafeBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Watch Later button
                            IconButton(
                                onClick = onOpenWatchLater,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("home_watch_later_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Watch Later",
                                    tint = SafeOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Theme Toggle Button (System Auto / Light / Dark)
                            ThemeToggleIconButton(
                                currentMode = themeMode,
                                onToggle = onToggleTheme
                            )

                            // Switch Account Button & Avatar
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SafeBlue.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(onClick = onSwitchAccount)
                                    .testTag("switch_account_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = activeProfile?.avatarEmoji ?: "🚀",
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = activeProfile?.name ?: "Account",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeBlue
                                    )
                                    Icon(
                                        imageVector = Icons.Default.SwitchAccount,
                                        contentDescription = "Switch Account",
                                        tint = SafeBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Compact Search Bar with Search History Dropdown
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            onSearchQueryChange(it)
                            if (!isDropdownManuallyOpened) isDropdownManuallyOpened = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isSearchFocused = focusState.isFocused
                            }
                            .testTag("youtube_search_bar"),
                        placeholder = {
                            Text(
                                text = "Search YouTube videos, creators, music, cooking...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SafeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = SafeBlue
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { isDropdownManuallyOpened = !isDropdownManuallyOpened },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("toggle_search_history_dropdown")
                                ) {
                                    Icon(
                                        imageVector = if (showDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Toggle Search History Dropdown",
                                        tint = if (showDropdown) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafeBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                isSearchFocused = false
                                isDropdownManuallyOpened = false
                                if (searchQuery.isNotBlank()) {
                                    onSearchSubmit(searchQuery)
                                }
                            }
                        )
                    )

                    // Dropdown Below the Search Bar
                    AnimatedVisibility(
                        visible = showDropdown,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                                .testTag("search_history_dropdown")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                // Search History Section
                                if (filteredHistory.isNotEmpty() || (searchQuery.isBlank() && searchHistory.isNotEmpty())) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "History",
                                                tint = SafeBlue,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "Recent Searches",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SafeBlue.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "Room DB",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SafeBlue,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            TextButton(
                                                onClick = onClearSearchHistory,
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("clear_search_history_button")
                                            ) {
                                                Text(
                                                    text = "Clear All",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    isDropdownManuallyOpened = false
                                                    isSearchFocused = false
                                                    focusManager.clearFocus()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    // List of recent queries
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        filteredHistory.take(7).forEach { queryItem ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onSearchQueryChange(queryItem)
                                                        onSearchSubmit(queryItem)
                                                        focusManager.clearFocus()
                                                        isSearchFocused = false
                                                        isDropdownManuallyOpened = false
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                                    .testTag("search_history_item_$queryItem"),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.History,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = queryItem,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            onSearchQueryChange(queryItem)
                                                        },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.NorthWest,
                                                            contentDescription = "Put in search bar",
                                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            onDeleteSearchHistoryItem(queryItem)
                                                        },
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .testTag("delete_search_history_$queryItem")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete search item",
                                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (searchHistory.isEmpty()) {
                                    // Empty state with quick navigation
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "No recent searches saved in Room DB yet",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = "Quick Navigation Suggestions:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SafeBlue
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val quickTopics = listOf("Technoblade", "Amr Diab", "Arctic Monkeys", "MrBeast", "Mark Rober", "Minecraft", "Gaming", "Music")
                                            items(quickTopics) { topic ->
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                                    modifier = Modifier
                                                        .clickable {
                                                            onSearchQueryChange(topic)
                                                            onSearchSubmit(topic)
                                                            focusManager.clearFocus()
                                                            isSearchFocused = false
                                                            isDropdownManuallyOpened = false
                                                        }
                                                        .testTag("quick_topic_$topic")
                                                ) {
                                                    Text(
                                                        text = topic,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Suggested searches section when typing
                                if (searchQuery.isNotBlank() && searchSuggestions.isNotEmpty()) {
                                    if (filteredHistory.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )
                                    }

                                    Text(
                                        text = "YouTube Suggestions",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        searchSuggestions.take(5).forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onSearchQueryChange(suggestion)
                                                        onSearchSubmit(suggestion)
                                                        focusManager.clearFocus()
                                                        isSearchFocused = false
                                                        isDropdownManuallyOpened = false
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                                    .testTag("search_suggestion_item_$suggestion"),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null,
                                                        tint = SafeBlue,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Text(
                                                        text = suggestion,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        onSearchQueryChange(suggestion)
                                                    },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.NorthWest,
                                                        contentDescription = "Put in search bar",
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Category Pills (Featuring "For You (FYP)", "Trending", "All", etc.)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(VideoCategory.values()) { category ->
                            val isSelected = selectedCategory == category && searchQuery.isBlank()
                            val pillColor = when {
                                isSelected && category == VideoCategory.FYP -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
                                isSelected && category == VideoCategory.TRENDING -> Brush.horizontalGradient(listOf(YouTubeRed, SafeOrange))
                                isSelected && category == VideoCategory.SHORTS -> Brush.horizontalGradient(listOf(YouTubeRed, Color(0xFFDC2626)))
                                isSelected && category == VideoCategory.CREATORS -> Brush.horizontalGradient(listOf(SafeCoral, Color(0xFFEA580C)))
                                isSelected -> Brush.horizontalGradient(listOf(SafeBlue, Color(0xFF2563EB)))
                                else -> null
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(
                                        if (pillColor != null) {
                                            Modifier.background(pillColor)
                                        } else {
                                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        }
                                    )
                                    .clickable {
                                        if (searchQuery.isNotBlank()) {
                                            onSearchQueryChange("")
                                        }
                                        if (category == VideoCategory.SHORTS) {
                                            onOpenShorts()
                                        } else {
                                            onCategorySelected(category)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("category_pill_${category.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(text = category.emoji, fontSize = 13.sp)
                                    Text(
                                        text = category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Personalized FYP Banner when "For You" is selected
        if (selectedCategory == VideoCategory.FYP && searchQuery.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3E8FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✨", fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Personalized FYP (For You Page)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF581C87)
                            )
                            Text(
                                text = "Curated for ${activeProfile?.name ?: "You"} based on your favorite creators & watch history • 100% Ad-Free",
                                fontSize = 11.sp,
                                color = Color(0xFF7E22CE)
                            )
                        }
                    }
                }
            }
        }

        // Jump Back In / Watch History Shelf
        if (watchHistory.isNotEmpty() && searchQuery.isBlank() && selectedCategory != VideoCategory.COOKING && selectedCategory != VideoCategory.MUSIC) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("home_watch_history_shelf")
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = SafeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Jump Back In",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SafeBlue.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Watch History",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeBlue
                                )
                            }
                        }

                        TextButton(
                            onClick = onOpenWatchHistory,
                            modifier = Modifier.testTag("see_all_history_button")
                        ) {
                            Text(
                                text = "See All (${watchHistory.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeBlue
                            )
                        }
                    }

                    // Horizontal shelf of watched videos
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = watchHistory.take(10),
                            key = { "shelf_${it.videoId}" }
                        ) { item ->
                            HomeWatchHistoryCard(
                                item = item,
                                onClick = { onVideoClick(item.toVideoItem()) },
                                onRemove = { onRemoveWatchHistoryItem(item.videoId) }
                            )
                        }
                    }
                }
            }
        }

        // Watch Later Shelf
        if (watchLater.isNotEmpty() && searchQuery.isBlank() && selectedCategory != VideoCategory.COOKING && selectedCategory != VideoCategory.MUSIC) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("home_watch_later_shelf")
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = SafeOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Watch Later",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SafeOrange.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${watchLater.size} saved",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeOrange
                                )
                            }
                        }

                        TextButton(
                            onClick = onOpenWatchLater,
                            modifier = Modifier.testTag("see_all_watch_later_button")
                        ) {
                            Text(
                                text = "See All (${watchLater.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrange
                            )
                        }
                    }

                    // Horizontal shelf of saved videos
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = watchLater.take(10),
                            key = { "later_${it.videoId}" }
                        ) { item ->
                            HomeWatchLaterCard(
                                item = item,
                                onClick = { onVideoClick(item.toVideoItem()) },
                                onRemove = { onRemoveWatchLaterItem(item.videoId) }
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Scrollable Trending Section at top of the app
        if (searchQuery.isBlank() && selectedCategory != VideoCategory.COOKING && selectedCategory != VideoCategory.MUSIC) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    // Trending Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🔥 Trending on YouTube",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = YouTubeRed.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(YouTubeRed)
                                    )
                                    Text(
                                        text = "LIVE",
                                        color = YouTubeRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        if (isTrendingLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = YouTubeRed
                            )
                        } else {
                            Text(
                                text = "YouTube Data API v3",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Horizontal Scrollable Trending Row
                    val trendingList = if (trendingVideos.isNotEmpty()) trendingVideos else videos.take(10)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(trendingList.size) { index ->
                            val video = trendingList[index]
                            TrendingVideoCard(
                                video = video,
                                rank = index + 1,
                                onClick = { onVideoClick(video) },
                                isSavedOffline = offlineIds.contains(video.id),
                                onToggleOffline = { onToggleOffline(video) }
                            )
                        }
                    }
                }
            }
        }

        // 🌟 Top Creators & Gaming Legends (Technoblade, MrBeast, Mark Rober) Shelf
        if (searchQuery.isBlank() && creatorVideos.isNotEmpty() && selectedCategory != VideoCategory.COOKING && selectedCategory != VideoCategory.MUSIC) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("home_creators_shelf")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🌟 Top Creators & Gaming",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeCoral.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Technoblade & Legends",
                                    color = SafeCoral,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { onCategorySelected(VideoCategory.CREATORS) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "View All ›",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeCoral
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(creatorVideos.size) { index ->
                            val video = creatorVideos[index]
                            TrendingVideoCard(
                                video = video,
                                rank = index + 1,
                                onClick = { onVideoClick(video) },
                                isSavedOffline = offlineIds.contains(video.id),
                                onToggleOffline = { onToggleOffline(video) }
                            )
                        }
                    }
                }
            }
        }

        // ⚡ YouTube Shorts Shelf on Home Screen
        if (searchQuery.isBlank() && shorts.isNotEmpty() && selectedCategory != VideoCategory.COOKING && selectedCategory != VideoCategory.MUSIC) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("home_shorts_shelf")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "⚡ YouTube Shorts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = YouTubeRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${shorts.size} Clips",
                                    color = YouTubeRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = onOpenShorts,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Explore All ›",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = YouTubeRed
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(shorts.size) { index ->
                            val shortItem = shorts[index]
                            HomeShortCard(
                                short = shortItem,
                                onClick = onOpenShorts
                            )
                        }
                    }
                }
            }
        }

        // Offline Mode Banner (Shown if device is offline or downloaded videos exist)
        if (!isOnline || offlineVideos.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isOnline) Color(0xFFFEF3C7) else Color(0xFFF0FDF4)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clickable(onClick = onOpenOfflineMode)
                        .testTag("offline_shelf_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.CheckCircle,
                                contentDescription = "Offline Viewing",
                                tint = if (!isOnline) Color(0xFFD97706) else SafeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (!isOnline) "Offline Mode • No Internet Connection" else "${offlineVideos.size} Videos Downloaded Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOnline) Color(0xFF92400E) else Color(0xFF166534)
                                )
                                Text(
                                    text = if (!isOnline) "Playback downloaded videos locally with no internet" else "Ready to play anywhere without Wi-Fi or cellular data",
                                    fontSize = 11.sp,
                                    color = if (!isOnline) Color(0xFFB45309) else Color(0xFF15803D)
                                )
                            }
                        }
                        Text(
                            text = "View",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isOnline) Color(0xFF92400E) else SafeGreen,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Section Title for Videos Grid
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        searchQuery.isNotBlank() -> "Search Results for \"$searchQuery\""
                        selectedCategory == VideoCategory.FYP -> "✨ Recommended For You"
                        selectedCategory == VideoCategory.TRENDING -> "🔥 Trending Highlights"
                        selectedCategory == VideoCategory.ALL -> "All Safe Videos"
                        else -> selectedCategory.displayName
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "${videos.size} videos",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Empty state
        if (videos.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎬", fontSize = 40.sp)
                        Text(
                            text = if (isSearching) "Searching YouTube..." else "No videos found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isSearching) "Fetching video results from YouTube..." else "Try searching for another topic or choose a category above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Video Cards in Adaptive Grid
            items(videos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    isSavedOffline = offlineIds.contains(video.id),
                    onVideoClick = { onVideoClick(video) },
                    onToggleOffline = { onToggleOffline(video) },
                    downloadProgress = downloadProgress[video.id],
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}
}

@Composable
private fun HomeWatchHistoryCard(
    item: WatchHistoryEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(170.dp)
            .clickable(onClick = onClick)
            .testTag("home_history_card_${item.videoId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Quick remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("remove_shelf_${item.videoId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Duration badge
                if (item.durationText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.durationText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Red watched progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(YouTubeRed)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Text(
                    text = item.channelTitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun HomeWatchLaterCard(
    item: WatchLaterEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(170.dp)
            .clickable(onClick = onClick)
            .testTag("home_watch_later_card_${item.videoId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl.ifBlank { "https://img.youtube.com/vi/${item.videoId}/hqdefault.jpg" },
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Quick remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("remove_later_shelf_${item.videoId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Bookmark badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SafeOrange.copy(alpha = 0.85f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }

                // Duration badge
                if (item.durationText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.durationText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Text(
                    text = item.channelTitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeShortCard(
    short: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(135.dp)
            .height(210.dp)
            .clickable(onClick = onClick)
            .testTag("home_short_card_${short.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://img.youtube.com/vi/${short.id}/hqdefault.jpg")
                    .crossfade(true)
                    .build(),
                contentDescription = short.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Red Shorts badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(YouTubeRed)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "⚡", fontSize = 8.sp)
                    Text(
                        text = "Shorts",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Title & Channel at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = short.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Text(
                    text = short.channelTitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
