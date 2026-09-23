package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.OfflineVideoEntity
import com.example.data.db.ParentSettingsEntity
import com.example.data.db.SafeTubeDatabase
import com.example.data.db.UserProfileEntity
import com.example.data.db.WatchActivityEntity
import com.example.data.db.WatchHistoryEntity
import com.example.data.db.WatchLaterEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.data.repository.CuratedVideoData
import com.example.data.repository.SafeTubeRepository
import com.example.data.storage.OfflineVideoStorageManager
import com.example.service.tracking.EyeHeadTrackingManager
import com.example.service.tracking.TrackingAction
import com.example.service.tracking.TrackingConfig
import com.example.service.tracking.TrackingGesture
import com.example.service.tracking.TrackingState
import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.Screen
import com.example.ui.theme.ThemeMode
import com.example.util.BatteryMonitor
import com.example.util.BatteryState
import com.example.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SafeTubeUiState(
    val currentScreen: Screen = Screen.Home,
    val currentTab: BottomTab = BottomTab.HOME,
    val hasCompletedWelcome: Boolean = true,
    val isAccountSwitchDialogOpen: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<VideoItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchSuggestions: List<String> = emptyList(),
    val selectedCategory: VideoCategory = VideoCategory.ALL,
    val selectedAgeGroup: AgeGroup = AgeGroup.TEEN,
    val isOfflineModeActive: Boolean = false,
    val isKidLockActive: Boolean = false,
    val isPinDialogOpen: Boolean = false,
    val pinDialogTargetScreen: Screen? = null,
    val pinError: String? = null,
    val activeVideo: VideoItem? = null,
    val isOfflinePlayback: Boolean = false,
    val currentWatchSeconds: Int = 0,
    val isBedtimeLocked: Boolean = false,
    val isScreenTimeLocked: Boolean = false,
    val directUrlInput: String = "",
    val isLoadingDirectVideo: Boolean = false,
    val userCreatedVideos: List<VideoItem> = emptyList(),
    val trendingVideos: List<VideoItem> = emptyList(),
    val isTrendingLoading: Boolean = false,
    val isPipActive: Boolean = false,
    val pipVideo: VideoItem? = null,
    val isPipOffline: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val selectedVideoResolution: String = "auto",
    val userPreferredResolution: String = "auto",
    val batteryState: BatteryState = BatteryState(),
    val isBatterySaverEnabled: Boolean = true,
    val isBatterySaverActive: Boolean = false,
    val sleepTimerRemainingSeconds: Int? = null,
    val sleepTimerTotalMinutes: Int? = null,
    val isSleepTimerTriggered: Boolean = false,
    val trackingConfig: TrackingConfig = TrackingConfig(),
    val trackingState: TrackingState = TrackingState(),
    val isTrackingDialogOpen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isIframeGrantedForUnavailable: Boolean = true,
    val isRefreshing: Boolean = false
)

class SafeTubeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SafeTubeRepository
    val storageManager: OfflineVideoStorageManager
    val networkMonitor: NetworkMonitor
    val batteryMonitor: BatteryMonitor = BatteryMonitor(application)
    val trackingManager: EyeHeadTrackingManager = EyeHeadTrackingManager(application)
    private val sharedPreferences = application.getSharedPreferences("safetube_user_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SafeTubeUiState())
    val uiState: StateFlow<SafeTubeUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean>
    val downloadProgress: StateFlow<Map<String, Float>>

    val parentSettings: StateFlow<ParentSettingsEntity?>
    val activeProfile: StateFlow<UserProfileEntity?>
    val allProfiles: StateFlow<List<UserProfileEntity>>
    val offlineVideos: StateFlow<List<OfflineVideoEntity>>
    val recentActivities: StateFlow<List<WatchActivityEntity>>
    val searchHistory: StateFlow<List<String>>
    val watchHistory: StateFlow<List<WatchHistoryEntity>>
    val recentWatchHistory: StateFlow<List<WatchHistoryEntity>>
    val watchHistoryCount: StateFlow<Int>
    val watchLater: StateFlow<List<WatchLaterEntity>>
    val watchLaterCount: StateFlow<Int>

    private val _unavailableVideoIds = MutableStateFlow<Set<String>>(emptySet())
    val unavailableVideoIds: StateFlow<Set<String>> = _unavailableVideoIds.asStateFlow()

    private var playerWatchTimerJob: Job? = null
    private var searchJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        val db = SafeTubeDatabase.getInstance(application)
        storageManager = OfflineVideoStorageManager(application)
        networkMonitor = NetworkMonitor(application)
        repository = SafeTubeRepository(db.safeTubeDao(), db.offlineVideoDao(), storageManager)

        isOnline = networkMonitor.isOnlineFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkMonitor.isCurrentlyConnected())

        downloadProgress = storageManager.downloadProgress

        parentSettings = repository.parentSettingsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        activeProfile = repository.activeProfileFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allProfiles = repository.allProfilesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        offlineVideos = repository.offlineVideosFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recentActivities = repository.recentActivitiesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        searchHistory = repository.recentSearchQueriesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        watchHistory = repository.watchHistoryFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recentWatchHistory = repository.recentWatchHistoryFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        watchHistoryCount = repository.watchHistoryCountFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        watchLater = repository.watchLaterFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        watchLaterCount = repository.watchLaterCountFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        // Keep locks permanently disabled as parental controls are removed
        _uiState.value = _uiState.value.copy(
            isScreenTimeLocked = false,
            isBedtimeLocked = false
        )

        viewModelScope.launch {
            trackingManager.config.collect { cfg ->
                _uiState.value = _uiState.value.copy(trackingConfig = cfg)
            }
        }

        viewModelScope.launch {
            trackingManager.state.collect { st ->
                _uiState.value = _uiState.value.copy(trackingState = st)
            }
        }

        // Restore saved theme mode (System Default by default)
        val savedThemeName = sharedPreferences.getString("safetube_theme_mode", ThemeMode.SYSTEM.name)
        val initialTheme = try {
            ThemeMode.valueOf(savedThemeName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        _uiState.value = _uiState.value.copy(themeMode = initialTheme)

        // Monitor battery level and automatically activate Battery Saver when <= 20%
        viewModelScope.launch {
            batteryMonitor.batteryStateFlow.collect { bState ->
                val isSaverActive = _uiState.value.isBatterySaverEnabled && bState.isLowBattery
                val resolution = if (isSaverActive) {
                    "240p"
                } else {
                    _uiState.value.userPreferredResolution
                }
                _uiState.value = _uiState.value.copy(
                    batteryState = bState,
                    isBatterySaverActive = isSaverActive,
                    selectedVideoResolution = resolution
                )
            }
        }

        loadTrendingVideos()
    }

    fun loadTrendingVideos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTrendingLoading = true)
            val trending = repository.getTrendingVideos()
            _uiState.value = _uiState.value.copy(
                trendingVideos = trending,
                isTrendingLoading = false
            )
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, isTrendingLoading = true)
            // Clear any cached unavailable video flags to allow fresh retries
            _unavailableVideoIds.value = emptySet()
            // Re-fetch trending content from YouTubeService / Repository
            val freshTrending = repository.getTrendingVideos()
            delay(400) // Brief debounce for natural pull feel
            _uiState.value = _uiState.value.copy(
                trendingVideos = freshTrending,
                isTrendingLoading = false,
                isRefreshing = false
            )
        }
    }

    fun navigateTo(screen: Screen) {
        val tab = when (screen) {
            is Screen.Home -> BottomTab.HOME
            is Screen.SafeShorts -> BottomTab.SHORTS
            is Screen.Music -> BottomTab.MUSIC
            is Screen.CreateContent -> BottomTab.CREATE
            is Screen.OfflineMode -> BottomTab.OFFLINE
            is Screen.Settings -> BottomTab.SETTINGS
            else -> _uiState.value.currentTab
        }

        // If navigating away from Player to another screen and we have an active video, minimize into PiP
        val shouldEnterPip = _uiState.value.currentScreen is Screen.Player &&
                screen !is Screen.Player &&
                _uiState.value.activeVideo != null

        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            currentTab = tab,
            isPipActive = if (shouldEnterPip) true else _uiState.value.isPipActive,
            pipVideo = if (shouldEnterPip) _uiState.value.activeVideo else _uiState.value.pipVideo,
            isPipOffline = if (shouldEnterPip) _uiState.value.isOfflinePlayback else _uiState.value.isPipOffline,
            pinError = null,
            isPinDialogOpen = false
        )
    }

    fun requestParentScreen(target: Screen) {
        // Parental controls removed: navigate directly without PIN prompt
        navigateTo(target)
    }

    fun verifyPin(enteredPin: String): Boolean {
        // No PIN requirement: navigate directly
        val target = _uiState.value.pinDialogTargetScreen ?: Screen.ParentDashboard
        _uiState.value = _uiState.value.copy(
            isPinDialogOpen = false,
            pinDialogTargetScreen = null,
            pinError = null
        )
        navigateTo(target)
        return true
    }

    fun dismissPinDialog() {
        _uiState.value = _uiState.value.copy(
            isPinDialogOpen = false,
            pinDialogTargetScreen = null,
            pinError = null
        )
    }

    fun completeWelcomeIntro() {
        _uiState.value = _uiState.value.copy(
            hasCompletedWelcome = true,
            currentScreen = Screen.Home,
            currentTab = BottomTab.HOME
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchSuggestions = emptyList(),
                isSearching = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            // Debounce slightly to prevent rapid network spam
            delay(350)
            val suggestions = repository.getSearchSuggestions(query)
            val liveResults = repository.searchAllYouTube(query)
            _uiState.value = _uiState.value.copy(
                searchResults = liveResults,
                searchSuggestions = suggestions,
                isSearching = false
            )
        }
    }

    fun submitSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                repository.saveSearchQuery(trimmed)
            }
        }
        setSearchQuery(query)
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun playAnyVideoFromInput(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDirectVideo = true)
            val video = repository.resolveVideoFromInput(input)
            _uiState.value = _uiState.value.copy(isLoadingDirectVideo = false)
            if (video != null) {
                startPlayingVideo(video)
            } else {
                // If not a direct ID or URL, perform search on it
                setSearchQuery(input)
            }
        }
    }

    fun setSelectedCategory(category: VideoCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun setSelectedAgeGroup(ageGroup: AgeGroup) {
        _uiState.value = _uiState.value.copy(selectedAgeGroup = ageGroup)
    }

    fun toggleOfflineMode() {
        val newMode = !_uiState.value.isOfflineModeActive
        _uiState.value = _uiState.value.copy(isOfflineModeActive = newMode)
        if (newMode) {
            navigateTo(Screen.OfflineMode)
        }
    }

    fun toggleKidLock() {
        // Kid lock removed or harmless no-op
        _uiState.value = _uiState.value.copy(isKidLockActive = false)
    }

    fun startPlayingVideo(video: VideoItem, isOffline: Boolean = false) {
        stopWatchingTimer()

        val isLocallySaved = storageManager.isVideoDownloaded(video.id) ||
                offlineVideos.value.any { it.videoId == video.id }
        val shouldPlayOffline = isOffline || !isOnline.value || _uiState.value.isOfflineModeActive || (isLocallySaved && !isOnline.value)

        val localFile = storageManager.getLocalVideoFile(video.id)
        val resolvedLocalPath = video.localFilePath ?: localFile?.absolutePath
        val resolvedVideo = if (resolvedLocalPath != null) {
            video.copy(localFilePath = resolvedLocalPath, offlineAvailable = true)
        } else {
            video
        }

        _uiState.value = _uiState.value.copy(
            activeVideo = resolvedVideo,
            isOfflinePlayback = shouldPlayOffline,
            currentWatchSeconds = 0,
            isPipActive = false,
            pipVideo = null,
            isPipOffline = false,
            currentScreen = Screen.Player(resolvedVideo, shouldPlayOffline)
        )

        // Store video in Room Watch History immediately
        viewModelScope.launch {
            repository.recordWatchHistory(resolvedVideo)
        }

        // Count watch seconds for watch activity and history progress
        playerWatchTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSeconds = _uiState.value.currentWatchSeconds + 1
                _uiState.value = _uiState.value.copy(currentWatchSeconds = currentSeconds)

                if (currentSeconds % 5 == 0) {
                    repository.updateWatchHistoryProgress(resolvedVideo.id, currentSeconds)
                }

                if (currentSeconds % 10 == 0) {
                    repository.addScreenTimeSeconds(10)
                }
            }
        }
    }

    fun enterPipMode(video: VideoItem? = null, isOffline: Boolean? = null) {
        val targetVideo = video ?: _uiState.value.activeVideo ?: _uiState.value.pipVideo
        if (targetVideo == null) return
        val offline = isOffline ?: _uiState.value.isOfflinePlayback

        val destinationScreen = when (_uiState.value.currentTab) {
            BottomTab.HOME -> Screen.Home
            BottomTab.SHORTS -> Screen.SafeShorts
            BottomTab.MUSIC -> Screen.Music
            BottomTab.CREATE -> Screen.CreateContent
            BottomTab.OFFLINE -> Screen.OfflineMode
            BottomTab.SETTINGS -> Screen.Settings
        }

        _uiState.value = _uiState.value.copy(
            isPipActive = true,
            pipVideo = targetVideo,
            isPipOffline = offline,
            activeVideo = targetVideo,
            isOfflinePlayback = offline,
            currentScreen = destinationScreen
        )
    }

    fun expandPipToPlayer() {
        val video = _uiState.value.pipVideo ?: _uiState.value.activeVideo ?: return
        val isOffline = _uiState.value.isPipOffline
        _uiState.value = _uiState.value.copy(
            isPipActive = false,
            pipVideo = null,
            activeVideo = video,
            isOfflinePlayback = isOffline,
            currentScreen = Screen.Player(video, isOffline)
        )
    }

    fun closePip() {
        val video = _uiState.value.pipVideo ?: _uiState.value.activeVideo
        val watched = _uiState.value.currentWatchSeconds
        stopWatchingTimer()

        if (video != null && watched > 2) {
            viewModelScope.launch {
                repository.recordWatchActivity(video, watched)
            }
        }

        _uiState.value = _uiState.value.copy(
            isPipActive = false,
            pipVideo = null,
            isPipOffline = false,
            activeVideo = null,
            currentWatchSeconds = 0
        )
    }

    fun stopPlayingVideo() {
        val video = _uiState.value.activeVideo ?: _uiState.value.pipVideo
        val watched = _uiState.value.currentWatchSeconds
        stopWatchingTimer()

        if (video != null && watched > 2) {
            viewModelScope.launch {
                repository.recordWatchActivity(video, watched)
            }
        }

        _uiState.value = _uiState.value.copy(
            activeVideo = null,
            pipVideo = null,
            isPipActive = false,
            isPipOffline = false,
            currentWatchSeconds = 0,
            isKidLockActive = false,
            currentScreen = Screen.Home
        )
    }

    private fun stopWatchingTimer() {
        playerWatchTimerJob?.cancel()
        playerWatchTimerJob = null
    }

    fun toggleSaveOffline(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleSaveOffline(video)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setVideoResolution(quality: String) {
        if (_uiState.value.isBatterySaverActive) {
            _uiState.value = _uiState.value.copy(
                userPreferredResolution = quality,
                selectedVideoResolution = "240p"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                userPreferredResolution = quality,
                selectedVideoResolution = quality
            )
        }
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        val bState = _uiState.value.batteryState
        val isSaverActive = enabled && bState.isLowBattery
        val resolution = if (isSaverActive) "240p" else _uiState.value.userPreferredResolution
        _uiState.value = _uiState.value.copy(
            isBatterySaverEnabled = enabled,
            isBatterySaverActive = isSaverActive,
            selectedVideoResolution = resolution
        )
    }

    fun toggleBatterySaver(enabled: Boolean) = setBatterySaverEnabled(enabled)

    fun setSimulatedBatteryLevel(level: Int?) {
        batteryMonitor.setSimulatedBatteryLevel(level)
        val bState = batteryMonitor.getCurrentBatteryState()
        val isSaverActive = _uiState.value.isBatterySaverEnabled && bState.isLowBattery
        val resolution = if (isSaverActive) "240p" else _uiState.value.userPreferredResolution
        _uiState.value = _uiState.value.copy(
            batteryState = bState,
            isBatterySaverActive = isSaverActive,
            selectedVideoResolution = resolution
        )
    }

    fun setTrackingDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isTrackingDialogOpen = isOpen)
    }

    fun updateTrackingConfig(block: (TrackingConfig) -> TrackingConfig) {
        trackingManager.updateConfig(block)
    }

    fun simulateTrackingGesture(gesture: TrackingGesture) {
        trackingManager.simulateGesture(gesture)
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        trackingManager.setCameraPermissionGranted(granted)
    }

    fun resetAttentionPause() {
        trackingManager.resetAttentionPause()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _uiState.value = _uiState.value.copy(
                sleepTimerRemainingSeconds = null,
                sleepTimerTotalMinutes = null,
                isSleepTimerTriggered = false
            )
            return
        }
        val totalSeconds = minutes * 60
        _uiState.value = _uiState.value.copy(
            sleepTimerRemainingSeconds = totalSeconds,
            sleepTimerTotalMinutes = minutes,
            isSleepTimerTriggered = false
        )
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(sleepTimerRemainingSeconds = remaining)
            }
            // Sleep timer reached zero: trigger pause state
            _uiState.value = _uiState.value.copy(
                sleepTimerRemainingSeconds = null,
                sleepTimerTotalMinutes = null,
                isSleepTimerTriggered = true
            )
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.value = _uiState.value.copy(
            sleepTimerRemainingSeconds = null,
            sleepTimerTotalMinutes = null
        )
    }

    fun dismissSleepTimerTriggered() {
        _uiState.value = _uiState.value.copy(isSleepTimerTriggered = false)
    }

    fun downloadVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.downloadAndSaveOffline(video)
        }
    }

    fun deleteOfflineVideo(videoId: String) {
        viewModelScope.launch {
            repository.deleteOfflineVideo(videoId)
        }
    }

    fun updateParentSettings(newSettings: ParentSettingsEntity) {
        viewModelScope.launch {
            // Keep parental controls disabled
            val unrestricted = newSettings.copy(
                parentalControlsEnabled = false,
                allowShorts = true,
                bedtimeLockEnabled = false,
                dailyScreenTimeMinutes = 0
            )
            repository.updateParentSettings(unrestricted)
        }
    }

    fun resetDailyScreenTime() {
        viewModelScope.launch {
            repository.resetScreenTimeToday()
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    fun removeFromWatchHistory(videoId: String) {
        viewModelScope.launch {
            repository.removeFromWatchHistory(videoId)
        }
    }

    fun toggleWatchLater(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleWatchLater(video)
        }
    }

    fun removeFromWatchLater(videoId: String) {
        viewModelScope.launch {
            repository.removeFromWatchLater(videoId)
        }
    }

    fun clearWatchLater() {
        viewModelScope.launch {
            repository.clearWatchLater()
        }
    }

    fun openAccountSwitcher() {
        _uiState.value = _uiState.value.copy(
            isAccountSwitchDialogOpen = true
        )
    }

    fun dismissAccountSwitcher() {
        _uiState.value = _uiState.value.copy(
            isAccountSwitchDialogOpen = false
        )
    }

    fun switchProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.switchProfile(profile.id)
            val age = try {
                AgeGroup.valueOf(profile.ageGroup)
            } catch (_: Exception) {
                AgeGroup.TEEN
            }
            _uiState.value = _uiState.value.copy(
                selectedAgeGroup = age,
                isAccountSwitchDialogOpen = false
            )
        }
    }

    fun completeSignInAndEnter(profile: UserProfileEntity? = null) {
        viewModelScope.launch {
            if (profile != null) {
                repository.switchProfile(profile.id)
                val age = try {
                    AgeGroup.valueOf(profile.ageGroup)
                } catch (_: Exception) {
                    AgeGroup.TEEN
                }
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.Home,
                    currentTab = BottomTab.HOME,
                    hasCompletedWelcome = true,
                    selectedAgeGroup = age
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    currentScreen = Screen.Home,
                    currentTab = BottomTab.HOME,
                    hasCompletedWelcome = true
                )
            }
        }
    }

    fun createProfile(name: String, emoji: String, ageGroup: AgeGroup) {
        viewModelScope.launch {
            repository.createProfile(name, emoji, ageGroup)
            _uiState.value = _uiState.value.copy(
                selectedAgeGroup = ageGroup,
                currentScreen = Screen.Home,
                currentTab = BottomTab.HOME,
                hasCompletedWelcome = true,
                isAccountSwitchDialogOpen = false
            )
        }
    }

    fun removeUnavailableVideo(videoId: String) {
        val updated = _unavailableVideoIds.value + videoId
        _unavailableVideoIds.value = updated
        if (_uiState.value.activeVideo?.id == videoId) {
            stopPlayingVideo()
        }
        _uiState.value = _uiState.value.copy(
            searchResults = _uiState.value.searchResults.filter { it.id != videoId }
        )
    }

    fun unmarkUnavailableVideo(videoId: String) {
        _unavailableVideoIds.value = _unavailableVideoIds.value - videoId
    }

    fun isVideoUnavailable(videoId: String): Boolean {
        return _unavailableVideoIds.value.contains(videoId)
    }

    fun setIframeGrantedForUnavailable(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isIframeGrantedForUnavailable = granted)
    }

    fun addCreatedVideo(video: VideoItem) {
        val currentCreated = _uiState.value.userCreatedVideos
        _uiState.value = _uiState.value.copy(
            userCreatedVideos = listOf(video) + currentCreated
        )
    }

    fun getFilteredVideos(): List<VideoItem> {
        val unavailable = _unavailableVideoIds.value
        val query = _uiState.value.searchQuery.trim()
        val userVids = _uiState.value.userCreatedVideos.filter { !unavailable.contains(it.id) }

        if (query.isNotEmpty()) {
            val live = _uiState.value.searchResults.filter { !unavailable.contains(it.id) }
            val matchedUser = userVids.filter { 
                it.title.contains(query, ignoreCase = true) || it.channelTitle.contains(query, ignoreCase = true) 
            }
            val matchedCurated = repository.filterVideos(
                query = query,
                selectedCategory = _uiState.value.selectedCategory
            ).filter { !unavailable.contains(it.id) }

            // Deduplicate across user, curated, and live YouTube API results
            val combined = (matchedUser + matchedCurated + live).distinctBy { it.id }
            return combined
        }

        if (_uiState.value.selectedCategory == VideoCategory.FYP) {
            return getFypVideos()
        }

        if (_uiState.value.selectedCategory == VideoCategory.TRENDING) {
            val trending = _uiState.value.trendingVideos.filter { !unavailable.contains(it.id) }
            if (trending.isNotEmpty()) return trending
            return CuratedVideoData.CURATED_VIDEOS.filter { !unavailable.contains(it.id) }
        }

        val filteredCurated = repository.filterVideos(
            query = query,
            selectedCategory = _uiState.value.selectedCategory
        ).filter { !unavailable.contains(it.id) }

        val categoryUserVids = if (_uiState.value.selectedCategory == VideoCategory.ALL) {
            userVids
        } else {
            userVids.filter { it.category == _uiState.value.selectedCategory }
        }

        return (categoryUserVids + filteredCurated).distinctBy { it.id }
    }

    /**
     * Personalized For You Page (FYP) algorithm:
     * Combines user-created uploads, videos from genres the user recently engaged with,
     * high-engagement trending videos, and top creator picks.
     */
    fun getFypVideos(): List<VideoItem> {
        val unavailable = _unavailableVideoIds.value
        val history = recentActivities.value
        val watchedCategories = history.map { it.category.uppercase() }.toSet()
        val allCurated = CuratedVideoData.CURATED_VIDEOS.filter { !unavailable.contains(it.id) }
        val trending = _uiState.value.trendingVideos.filter { !unavailable.contains(it.id) }
        val userCreated = _uiState.value.userCreatedVideos.filter { !unavailable.contains(it.id) }

        val fypList = mutableListOf<VideoItem>()
        // 1. Personal uploads first
        fypList.addAll(userCreated)

        // 2. Videos matching watched categories
        if (watchedCategories.isNotEmpty()) {
            val historyMatches = allCurated.filter { video ->
                watchedCategories.contains(video.category.name.uppercase()) ||
                        watchedCategories.contains(video.category.displayName.uppercase())
            }
            fypList.addAll(historyMatches)
        }

        // 3. Trending & viral hits
        fypList.addAll(trending)

        // 4. Balanced mix of high quality categories
        val creators = allCurated.filter { it.category == VideoCategory.CREATORS }
        val science = allCurated.filter { it.category == VideoCategory.SCIENCE }
        val cooking = allCurated.filter { it.category == VideoCategory.COOKING }
        val music = allCurated.filter { it.category == VideoCategory.MUSIC }
        fypList.addAll(creators)
        fypList.addAll(science)
        fypList.addAll(cooking)
        fypList.addAll(music)

        if (fypList.size < 12) {
            fypList.addAll(allCurated)
        }

        return fypList.distinctBy { it.id }
    }

    fun getMusicVideos(): List<VideoItem> {
        val unavailable = _unavailableVideoIds.value
        val userMusic = _uiState.value.userCreatedVideos.filter { it.category == VideoCategory.MUSIC && !unavailable.contains(it.id) }
        val curatedMusic = CuratedVideoData.CURATED_VIDEOS.filter { it.category == VideoCategory.MUSIC && !unavailable.contains(it.id) }
        return userMusic + curatedMusic
    }

    fun getCookingVideos(): List<VideoItem> {
        val unavailable = _unavailableVideoIds.value
        val userCooking = _uiState.value.userCreatedVideos.filter { it.category == VideoCategory.COOKING && !unavailable.contains(it.id) }
        val curatedCooking = CuratedVideoData.CURATED_VIDEOS.filter { it.category == VideoCategory.COOKING && !unavailable.contains(it.id) }
        return userCooking + curatedCooking
    }

    fun getSafeShorts(): List<VideoItem> {
        val unavailable = _unavailableVideoIds.value
        return CuratedVideoData.CURATED_VIDEOS.filter { it.isShort && !unavailable.contains(it.id) }
    }

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString("safetube_theme_mode", mode.name).apply()
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun toggleThemeMode() {
        val nextMode = _uiState.value.themeMode.next()
        setThemeMode(nextMode)
    }

    override fun onCleared() {
        super.onCleared()
        stopWatchingTimer()
        searchJob?.cancel()
        sleepTimerJob?.cancel()
    }
}
