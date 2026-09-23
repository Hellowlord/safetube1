package com.example.data.repository

import com.example.data.api.YouTubeService
import com.example.data.db.OfflineVideoDao
import com.example.data.db.OfflineVideoEntity
import com.example.data.db.ParentSettingsEntity
import com.example.data.db.SafeTubeDao
import com.example.data.db.SearchQueryEntity
import com.example.data.db.UserProfileEntity
import com.example.data.db.WatchActivityEntity
import com.example.data.db.WatchHistoryEntity
import com.example.data.db.WatchLaterEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.data.storage.OfflineVideoStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SafeTubeRepository(
    private val dao: SafeTubeDao,
    private val offlineDao: OfflineVideoDao? = null,
    val storageManager: OfflineVideoStorageManager? = null
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayString(): String = dateFormat.format(Date())

    val parentSettingsFlow: Flow<ParentSettingsEntity?> = dao.getParentSettingsFlow()
    val activeProfileFlow: Flow<UserProfileEntity?> = dao.getActiveProfileFlow()
    val allProfilesFlow: Flow<List<UserProfileEntity>> = dao.getAllProfilesFlow()
    val offlineVideosFlow: Flow<List<OfflineVideoEntity>> = offlineDao?.getAllOfflineVideosFlow() ?: dao.getOfflineVideosFlow()
    val playableOfflineVideosFlow: Flow<List<OfflineVideoEntity>>? = offlineDao?.getPlayableOfflineVideosFlow()
    val totalOfflineStorageMbFlow: Flow<Double?>? = offlineDao?.getTotalOfflineStorageMbFlow()
    val recentActivitiesFlow: Flow<List<WatchActivityEntity>> = dao.getRecentActivitiesFlow()
    val recentSearchQueriesFlow: Flow<List<String>> = dao.getRecentQueriesFlow()
    val watchHistoryFlow: Flow<List<WatchHistoryEntity>> = dao.getAllWatchHistoryFlow()
    val recentWatchHistoryFlow: Flow<List<WatchHistoryEntity>> = dao.getRecentWatchHistoryFlow(15)
    val watchHistoryCountFlow: Flow<Int> = dao.getWatchHistoryCountFlow()
    val watchLaterFlow: Flow<List<WatchLaterEntity>> = dao.getAllWatchLaterFlow()
    val watchLaterCountFlow: Flow<Int> = dao.getWatchLaterCountFlow()

    suspend fun initializeDefaultsIfNeeded() {
        val existingSettings = dao.getParentSettings()
        val today = getTodayString()
        if (existingSettings == null) {
            val defaultSettings = ParentSettingsEntity(
                id = 1,
                parentPin = "1234",
                parentalControlsEnabled = false, // Parental controls removed
                noAdsEnforced = true,
                allowShorts = true, // Shorts fully allowed
                offlineViewingEnabled = true,
                dailyScreenTimeMinutes = 0, // Unlimited screen time
                screenTimeUsedSecondsToday = 0,
                lastActiveDate = today,
                bedtimeLockEnabled = false, // No bedtime lock
                bedtimeStartHour = 22,
                bedtimeEndHour = 6,
                selectedAgeFilter = "ALL",
                blockedKeywords = "",
                allowedCategories = "ALL,TRENDING,MUSIC,GAMING,TECH,ENTERTAINMENT,SPORTS,SCIENCE,NATURE,EDUCATION,CARTOONS,ART,BEDTIME,MATH,SHORTS"
            )
            dao.insertOrUpdateParentSettings(defaultSettings)
        } else {
            // Ensure parental controls are turned off as requested
            if (existingSettings.parentalControlsEnabled || !existingSettings.allowShorts || existingSettings.bedtimeLockEnabled) {
                dao.insertOrUpdateParentSettings(
                    existingSettings.copy(
                        parentalControlsEnabled = false,
                        allowShorts = true,
                        bedtimeLockEnabled = false,
                        blockedKeywords = "",
                        dailyScreenTimeMinutes = 0
                    )
                )
            }
        }

        val existingProfiles = dao.getAllProfilesFlow().firstOrNull()
        if (existingProfiles.isNullOrEmpty()) {
            val defaultProfile = UserProfileEntity(
                name = "Junior Explorer",
                avatarEmoji = "🚀",
                ageGroup = "ALL",
                isActive = true
            )
            dao.insertProfile(defaultProfile)
        }

        // Initialize default recent search history in Room DB if empty
        val existingQueries = dao.getRecentQueriesFlow(1).firstOrNull()
        if (existingQueries.isNullOrEmpty()) {
            val seedQueries = listOf(
                "Mark Rober Experiments",
                "KATSEYE",
                "Minecraft Creations",
                "Maroon 5 Hits",
                "Science & Space",
                "Cooking Tutorials",
                "Lofi Chill Beats"
            )
            seedQueries.forEachIndexed { index, query ->
                dao.insertSearchQuery(
                    SearchQueryEntity(
                        query = query,
                        timestamp = System.currentTimeMillis() - (index * 60_000L)
                    )
                )
            }
        }
    }

    suspend fun updateParentSettings(settings: ParentSettingsEntity) {
        dao.insertOrUpdateParentSettings(settings)
    }

    suspend fun addScreenTimeSeconds(seconds: Int) {
        val current = dao.getParentSettings() ?: return
        val today = getTodayString()
        val currentUsed = if (current.lastActiveDate == today) current.screenTimeUsedSecondsToday else 0
        dao.updateScreenTimeUsed(currentUsed + seconds, today)
    }

    suspend fun resetScreenTimeToday() {
        val today = getTodayString()
        dao.updateScreenTimeUsed(0, today)
    }

    suspend fun recordWatchActivity(video: VideoItem, durationWatchedSec: Int) {
        if (durationWatchedSec <= 2) return
        val today = getTodayString()
        val activity = WatchActivityEntity(
            videoId = video.id,
            videoTitle = video.title,
            channelTitle = video.channelTitle,
            category = video.category.name,
            durationWatchedSeconds = durationWatchedSec,
            dateString = today
        )
        dao.insertWatchActivity(activity)
        addScreenTimeSeconds(durationWatchedSec)
    }

    suspend fun isVideoOffline(videoId: String): Boolean {
        return dao.isVideoOffline(videoId)
    }

    fun isVideoOfflineFlow(videoId: String): Flow<Boolean> {
        return dao.isVideoOfflineFlow(videoId)
    }

    suspend fun toggleSaveOffline(video: VideoItem): Boolean {
        val isOffline = dao.isVideoOffline(video.id)
        if (isOffline) {
            deleteOfflineVideo(video.id)
            return false
        } else {
            return downloadAndSaveOffline(video)
        }
    }

    suspend fun downloadAndSaveOffline(
        video: VideoItem,
        onProgress: (Float) -> Unit = {}
    ): Boolean {
        val localFile = storageManager?.downloadVideo(video, onProgress)
        val path = localFile?.absolutePath ?: ""
        val thumbPath = storageManager?.getLocalThumbnailFile(video.id)?.absolutePath ?: ""
        val fileSizeBytes = if (localFile != null && localFile.exists()) localFile.length() else 0L
        val sizeMb = if (fileSizeBytes > 0L) {
            (fileSizeBytes / (1024.0 * 1024.0))
        } else {
            12.4
        }
        val formattedSize = String.format(Locale.US, "%.1f", sizeMb).toDoubleOrNull() ?: 12.4
        val offlineEntity = OfflineVideoEntity(
            videoId = video.id,
            title = video.title,
            channelTitle = video.channelTitle,
            category = video.category.name,
            ageGroup = video.ageGroup.name,
            durationText = video.durationText,
            description = video.description,
            fileSizeMb = formattedSize,
            fileSizeBytes = fileSizeBytes,
            localFilePath = path,
            localThumbnailPath = thumbPath,
            mimeType = "video/mp4",
            videoQuality = "720p",
            isDownloadComplete = path.isNotBlank()
        )
        if (offlineDao != null) {
            offlineDao.insertOfflineVideo(offlineEntity)
        } else {
            dao.insertOfflineVideo(offlineEntity)
        }
        return true
    }

    suspend fun updateOfflinePlaybackPosition(videoId: String, positionMs: Long) {
        offlineDao?.updatePlaybackPosition(videoId, positionMs)
    }

    suspend fun getOfflineVideo(videoId: String): OfflineVideoEntity? {
        return offlineDao?.getOfflineVideoById(videoId) ?: dao.getOfflineVideo(videoId)
    }

    fun getLocalVideoFile(videoId: String): File? {
        return storageManager?.getLocalVideoFile(videoId)
    }

    suspend fun deleteOfflineVideo(videoId: String) {
        storageManager?.deleteVideo(videoId)
        if (offlineDao != null) {
            offlineDao.deleteOfflineVideoById(videoId)
        } else {
            dao.deleteOfflineVideo(videoId)
        }
    }

    suspend fun createProfile(name: String, emoji: String, ageGroup: AgeGroup): Long {
        val profile = UserProfileEntity(
            name = name,
            avatarEmoji = emoji,
            ageGroup = ageGroup.name,
            isActive = true
        )
        val id = dao.insertProfile(profile)
        dao.setActiveProfile(id)
        return id
    }

    suspend fun switchProfile(profileId: Long) {
        dao.setActiveProfile(profileId)
    }

    suspend fun clearHistory() {
        dao.clearWatchActivities()
        dao.clearAllWatchHistory()
    }

    suspend fun recordWatchHistory(video: VideoItem, positionSeconds: Int = 0) {
        val entity = WatchHistoryEntity(
            videoId = video.id,
            title = video.title,
            channelTitle = video.channelTitle,
            thumbnailUrl = video.thumbnailUrl.ifBlank { "https://img.youtube.com/vi/${video.id}/hqdefault.jpg" },
            durationText = video.durationText,
            category = video.category.name,
            ageGroup = video.ageGroup.name,
            description = video.description,
            isShort = video.isShort,
            watchedAt = System.currentTimeMillis(),
            durationWatchedSeconds = positionSeconds
        )
        dao.insertOrUpdateWatchHistory(entity)
    }

    suspend fun updateWatchHistoryProgress(videoId: String, seconds: Int) {
        dao.updateWatchProgress(videoId, seconds, System.currentTimeMillis())
    }

    suspend fun removeFromWatchHistory(videoId: String) {
        dao.deleteWatchHistoryItem(videoId)
    }

    suspend fun clearWatchHistory() {
        dao.clearAllWatchHistory()
        dao.clearWatchActivities()
    }

    fun isWatchLaterFlow(videoId: String): Flow<Boolean> = dao.isWatchLaterFlow(videoId)

    suspend fun isWatchLater(videoId: String): Boolean = dao.isWatchLater(videoId)

    suspend fun toggleWatchLater(video: VideoItem): Boolean {
        val exists = dao.isWatchLater(video.id)
        if (exists) {
            dao.deleteWatchLaterItem(video.id)
            return false
        } else {
            val entity = WatchLaterEntity(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl.ifBlank { "https://img.youtube.com/vi/${video.id}/hqdefault.jpg" },
                durationText = video.durationText,
                category = video.category.name,
                ageGroup = video.ageGroup.name,
                description = video.description,
                isShort = video.isShort,
                savedAt = System.currentTimeMillis()
            )
            dao.insertWatchLater(entity)
            return true
        }
    }

    suspend fun removeFromWatchLater(videoId: String) {
        dao.deleteWatchLaterItem(videoId)
    }

    suspend fun clearWatchLater() {
        dao.clearAllWatchLater()
    }

    fun filterVideos(
        query: String,
        selectedCategory: VideoCategory,
        selectedAgeGroup: AgeGroup? = null,
        allowShorts: Boolean = true,
        blockedKeywordsList: List<String> = emptyList()
    ): List<VideoItem> {
        return CuratedVideoData.CURATED_VIDEOS.filter { video ->
            // Category filter
            if (selectedCategory != VideoCategory.ALL && video.category != selectedCategory) {
                return@filter false
            }

            // Query search
            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                val matchesTitle = video.title.lowercase().contains(q)
                val matchesChannel = video.channelTitle.lowercase().contains(q)
                val matchesTags = video.tags.any { it.lowercase().contains(q) }
                if (!matchesTitle && !matchesChannel && !matchesTags) {
                    return@filter false
                }
            }

            true
        }
    }

    suspend fun searchAllYouTube(query: String): List<VideoItem> {
        return YouTubeService.searchYouTube(query)
    }

    suspend fun resolveVideoFromInput(input: String): VideoItem? {
        val videoId = YouTubeService.extractVideoId(input) ?: return null
        return YouTubeService.fetchVideoDetails(videoId)
    }

    suspend fun getSearchSuggestions(query: String): List<String> {
        return YouTubeService.getSearchSuggestions(query)
    }

    suspend fun getTrendingVideos(regionCode: String = "US"): List<VideoItem> {
        return YouTubeService.fetchTrendingVideos(regionCode = regionCode)
    }

    suspend fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            dao.insertSearchQuery(SearchQueryEntity(query = trimmed, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        dao.deleteSearchQuery(query)
    }

    suspend fun clearSearchHistory() {
        dao.clearSearchHistory()
    }
}
