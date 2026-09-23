package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.models.VideoItem
import com.example.data.repository.CuratedVideoData
import com.example.ui.components.FloatingPipPlayer
import com.example.ui.components.SafeTubeBottomBar
import com.example.ui.components.SwitchAccountDialog
import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.Screen
import com.example.ui.screens.create.CreateContentScreen
import com.example.ui.screens.history.WatchHistoryScreen
import com.example.ui.screens.later.WatchLaterScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.music.MusicScreen
import com.example.ui.screens.offline.OfflineVideosScreen
import com.example.ui.screens.parent.ParentDashboardScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shorts.SafeShortsScreen
import com.example.ui.theme.SafeTubeTheme
import com.example.ui.viewmodel.SafeTubeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SafeTubeViewModel by viewModels()
    private var isInPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            SafeTubeTheme(themeMode = uiState.themeMode) {
                SafeTubeApp(
                    viewModel = viewModel,
                    isInSystemPip = isInPipMode,
                    onEnterSystemPip = { enterSystemPip() }
                )
            }
        }
    }

    fun enterSystemPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val state = viewModel.uiState.value
        if (state.currentScreen is Screen.Player || state.isPipActive) {
            enterSystemPip()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }
}

@Composable
fun SafeTubeApp(
    viewModel: SafeTubeViewModel,
    isInSystemPip: Boolean = false,
    onEnterSystemPip: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val parentSettings by viewModel.parentSettings.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val offlineVideos by viewModel.offlineVideos.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val watchHistoryCount by viewModel.watchHistoryCount.collectAsState()
    val watchLater by viewModel.watchLater.collectAsState()
    val watchLaterCount by viewModel.watchLaterCount.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val filteredVideos = viewModel.getFilteredVideos()
    val safeShorts = viewModel.getSafeShorts()

    // When in Android OS system PiP mode, render a clean dedicated video display
    if (isInSystemPip) {
        val pipTargetVideo = uiState.activeVideo ?: uiState.pipVideo ?: (uiState.currentScreen as? Screen.Player)?.video
        if (pipTargetVideo != null) {
            FloatingPipPlayer(
                video = pipTargetVideo,
                isOffline = uiState.isOfflinePlayback || uiState.isPipOffline,
                onExpand = { viewModel.expandPipToPlayer() },
                onClose = { viewModel.closePip() },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // Handle Back Navigation: minimize player to PiP instead of closing abruptly
    BackHandler(enabled = uiState.currentScreen !is Screen.Home && uiState.currentScreen !is Screen.WelcomeIntro) {
        when (uiState.currentScreen) {
            is Screen.Player -> viewModel.enterPipMode()
            else -> viewModel.navigateTo(Screen.Home)
        }
    }

    val showBottomBar = uiState.currentScreen !is Screen.WelcomeIntro &&
            uiState.currentScreen !is Screen.Player

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                SafeTubeBottomBar(
                    selectedTab = uiState.currentTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomTab.HOME -> viewModel.navigateTo(Screen.Home)
                            BottomTab.SHORTS -> viewModel.navigateTo(Screen.SafeShorts)
                            BottomTab.MUSIC -> viewModel.navigateTo(Screen.Music)
                            BottomTab.CREATE -> viewModel.navigateTo(Screen.CreateContent)
                            BottomTab.OFFLINE -> viewModel.navigateTo(Screen.OfflineMode)
                            BottomTab.SETTINGS -> viewModel.navigateTo(Screen.Settings)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = uiState.currentScreen) {
                is Screen.WelcomeIntro, is Screen.Home -> {
                    HomeScreen(
                        videos = filteredVideos,
                        shorts = safeShorts,
                        trendingVideos = uiState.trendingVideos,
                        isTrendingLoading = uiState.isTrendingLoading,
                        offlineVideos = offlineVideos,
                        parentSettings = parentSettings,
                        activeProfile = activeProfile,
                        selectedCategory = uiState.selectedCategory,
                        selectedAgeGroup = uiState.selectedAgeGroup,
                        searchQuery = uiState.searchQuery,
                        isSearching = uiState.isSearching,
                        searchSuggestions = uiState.searchSuggestions,
                        searchHistory = searchHistory,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchSubmit = { viewModel.submitSearchQuery(it) },
                        onDeleteSearchHistoryItem = { viewModel.deleteSearchQuery(it) },
                        onClearSearchHistory = { viewModel.clearSearchHistory() },
                        onCategorySelected = { viewModel.setSelectedCategory(it) },
                        onAgeGroupSelected = { viewModel.setSelectedAgeGroup(it) },
                        onVideoClick = { video -> viewModel.startPlayingVideo(video) },
                        onToggleOffline = { video -> viewModel.toggleSaveOffline(video) },
                        onOpenParentDashboard = { viewModel.navigateTo(Screen.ParentDashboard) },
                        onOpenOfflineMode = { viewModel.navigateTo(Screen.OfflineMode) },
                        onOpenWatchHistory = { viewModel.navigateTo(Screen.WatchHistory) },
                        onRemoveWatchHistoryItem = { viewModel.removeFromWatchHistory(it) },
                        onOpenWatchLater = { viewModel.navigateTo(Screen.WatchLater) },
                        onRemoveWatchLaterItem = { viewModel.removeFromWatchLater(it) },
                        onSwitchAccount = { viewModel.openAccountSwitcher() },
                        watchHistory = watchHistory,
                        watchLater = watchLater,
                        onOpenShorts = { viewModel.navigateTo(Screen.SafeShorts) },
                        downloadProgress = downloadProgress,
                        isOnline = isOnline,
                        themeMode = uiState.themeMode,
                        onToggleTheme = { viewModel.toggleThemeMode() },
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshFeed() }
                    )
                }

                is Screen.Music -> {
                    val musicVideos = viewModel.getMusicVideos()
                    MusicScreen(
                        musicVideos = musicVideos,
                        offlineVideos = offlineVideos,
                        onVideoClick = { video -> viewModel.startPlayingVideo(video) },
                        onToggleOffline = { video -> viewModel.toggleSaveOffline(video) }
                    )
                }

                is Screen.CreateContent -> {
                    CreateContentScreen(
                        userCreatedVideos = uiState.userCreatedVideos,
                        creatorName = activeProfile?.name ?: "Creator",
                        onPublishVideo = { video -> viewModel.addCreatedVideo(video) },
                        onPlayVideo = { video -> viewModel.startPlayingVideo(video) }
                    )
                }

                is Screen.SafeShorts -> {
                    SafeShortsScreen(
                        shorts = safeShorts,
                        isShortsAllowedByParent = true,
                        isBatterySaverActive = uiState.isBatterySaverActive,
                        batteryState = uiState.batteryState,
                        onVideoClick = { video -> viewModel.startPlayingVideo(video) },
                        onOpenParentControls = { viewModel.navigateTo(Screen.ParentDashboard) }
                    )
                }

                is Screen.OfflineMode -> {
                    OfflineVideosScreen(
                        offlineVideos = offlineVideos,
                        isOfflineModeActive = uiState.isOfflineModeActive,
                        onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                        onPlayOfflineVideo = { video -> viewModel.startPlayingVideo(video, isOffline = true) },
                        onDeleteOfflineVideo = { videoId -> viewModel.deleteOfflineVideo(videoId) },
                        onBrowseSafeVideos = { viewModel.navigateTo(Screen.Home) }
                    )
                }

                is Screen.Player -> {
                    val isOfflineSaved = offlineVideos.any { it.videoId == screen.video.id }
                    val isWatchLater = watchLater.any { it.videoId == screen.video.id }
                    VideoPlayerScreen(
                        video = screen.video,
                        isOfflineMode = screen.isOfflinePlayback,
                        isSavedOffline = isOfflineSaved,
                        isWatchLater = isWatchLater,
                        isKidLock = false,
                        remainingMinutes = 999,
                        recommendedVideos = filteredVideos,
                        playbackSpeed = uiState.playbackSpeed,
                        onSetPlaybackSpeed = { speed -> viewModel.setPlaybackSpeed(speed) },
                        selectedResolution = uiState.selectedVideoResolution,
                        onSetResolution = { quality -> viewModel.setVideoResolution(quality) },
                        sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                        sleepTimerTotalMinutes = uiState.sleepTimerTotalMinutes,
                        isSleepTimerTriggered = uiState.isSleepTimerTriggered,
                        onSetSleepTimer = { minutes -> viewModel.setSleepTimer(minutes) },
                        onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                        onDismissSleepTimerTriggered = { viewModel.dismissSleepTimerTriggered() },
                        onBackClick = { viewModel.enterPipMode() },
                        onToggleKidLock = { viewModel.toggleKidLock() },
                        onToggleOffline = { video -> viewModel.toggleSaveOffline(video) },
                        onToggleWatchLater = { video -> viewModel.toggleWatchLater(video) },
                        onSelectRecommended = { video -> viewModel.startPlayingVideo(video) },
                        onRemoveUnavailable = { videoId -> viewModel.removeUnavailableVideo(videoId) },
                        onEnterPip = { viewModel.enterPipMode() },
                        onSystemPip = onEnterSystemPip,
                        trackingConfig = uiState.trackingConfig,
                        trackingState = uiState.trackingState,
                        isTrackingDialogOpen = uiState.isTrackingDialogOpen,
                        onSetTrackingDialogOpen = { isOpen -> viewModel.setTrackingDialogOpen(isOpen) },
                        onUpdateTrackingConfig = { block -> viewModel.updateTrackingConfig(block) },
                        onSimulateTrackingGesture = { gesture -> viewModel.simulateTrackingGesture(gesture) },
                        onRequestCameraPermission = { viewModel.setCameraPermissionGranted(true) },
                        trackingActionEvents = viewModel.trackingManager.actionEvents,
                        onStartCameraTracking = { owner -> viewModel.trackingManager.startCamera(owner) },
                        onStopCameraTracking = { viewModel.trackingManager.stopCamera() },
                        themeMode = uiState.themeMode,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onToggleTheme = { viewModel.toggleThemeMode() },
                        isBatterySaverActive = uiState.isBatterySaverActive,
                        batteryState = uiState.batteryState
                    )
                }

                is Screen.ParentDashboard -> {
                    ParentDashboardScreen(
                        settings = parentSettings,
                        activities = recentActivities,
                        onUpdateSettings = { viewModel.updateParentSettings(it) },
                        onResetTodayScreenTime = { viewModel.resetDailyScreenTime() },
                        onClearHistory = { viewModel.clearWatchHistory() },
                        onCloseDashboard = { viewModel.navigateTo(Screen.Home) },
                        themeMode = uiState.themeMode,
                        onSetThemeMode = { viewModel.setThemeMode(it) }
                    )
                }

                is Screen.WatchHistory -> {
                    WatchHistoryScreen(
                        watchHistory = watchHistory,
                        offlineVideos = offlineVideos,
                        onVideoClick = { video -> viewModel.startPlayingVideo(video) },
                        onRemoveFromHistory = { videoId -> viewModel.removeFromWatchHistory(videoId) },
                        onClearAllHistory = { viewModel.clearWatchHistory() },
                        onToggleOffline = { video -> viewModel.toggleSaveOffline(video) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }

                is Screen.WatchLater -> {
                    WatchLaterScreen(
                        watchLaterList = watchLater,
                        offlineVideos = offlineVideos,
                        onVideoClick = { video -> viewModel.startPlayingVideo(video) },
                        onRemoveFromWatchLater = { videoId -> viewModel.removeFromWatchLater(videoId) },
                        onClearAllWatchLater = { viewModel.clearWatchLater() },
                        onToggleOffline = { video -> viewModel.toggleSaveOffline(video) },
                        onBack = { viewModel.navigateTo(Screen.Home) }
                    )
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        settings = parentSettings,
                        activeProfile = activeProfile,
                        allProfiles = allProfiles,
                        onUpdateSettings = { viewModel.updateParentSettings(it) },
                        onSwitchProfile = { viewModel.switchProfile(it) },
                        onCreateProfile = { name, emoji, age -> viewModel.createProfile(name, emoji, age) },
                        onOpenWatchHistory = { viewModel.navigateTo(Screen.WatchHistory) },
                        onClearWatchHistory = { viewModel.clearWatchHistory() },
                        watchHistoryCount = watchHistoryCount,
                        onOpenWatchLater = { viewModel.navigateTo(Screen.WatchLater) },
                        onClearWatchLater = { viewModel.clearWatchLater() },
                        watchLaterCount = watchLaterCount,
                        themeMode = uiState.themeMode,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        batteryState = uiState.batteryState,
                        isBatterySaverEnabled = uiState.isBatterySaverEnabled,
                        isBatterySaverActive = uiState.isBatterySaverActive,
                        onToggleBatterySaver = { viewModel.toggleBatterySaver(it) },
                        onSetSimulatedBatteryLevel = { viewModel.setSimulatedBatteryLevel(it) }
                    )
                }
            }

            // Account Switcher Dialog (Accessible from header and settings)
            if (uiState.isAccountSwitchDialogOpen) {
                SwitchAccountDialog(
                    allProfiles = allProfiles,
                    activeProfile = activeProfile,
                    onDismiss = { viewModel.dismissAccountSwitcher() },
                    onSelectProfile = { profile -> viewModel.switchProfile(profile) },
                    onCreateProfile = { name, emoji, age -> viewModel.createProfile(name, emoji, age) }
                )
            }

            // Floating In-App Picture-in-Picture Mini Player
            if (uiState.isPipActive && uiState.pipVideo != null && uiState.currentScreen !is Screen.Player) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp, end = 12.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingPipPlayer(
                        video = uiState.pipVideo!!,
                        isOffline = uiState.isPipOffline,
                        onExpand = { viewModel.expandPipToPlayer() },
                        onClose = { viewModel.closePip() },
                        onSystemPip = onEnterSystemPip
                    )
                }
            }
        }
    }
}
