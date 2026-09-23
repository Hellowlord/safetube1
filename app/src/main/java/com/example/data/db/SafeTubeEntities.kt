package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem

@Entity(tableName = "parent_settings")
data class ParentSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val parentPin: String = "1234",
    val parentalControlsEnabled: Boolean = true,
    val noAdsEnforced: Boolean = true,
    val allowShorts: Boolean = false,
    val offlineViewingEnabled: Boolean = true,
    val dailyScreenTimeMinutes: Int = 45,
    val screenTimeUsedSecondsToday: Int = 0,
    val lastActiveDate: String = "",
    val bedtimeLockEnabled: Boolean = true,
    val bedtimeStartHour: Int = 20, // 8:00 PM
    val bedtimeEndHour: Int = 7,   // 7:00 AM
    val selectedAgeFilter: String = "EARLY",
    val blockedKeywords: String = "scary,horror,violent,gun,fight,swear,prank",
    val allowedCategories: String = "ALL,SCIENCE,NATURE,CARTOONS,MUSIC,ART,BEDTIME,MATH,SHORTS"
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val avatarEmoji: String = "🦊",
    val ageGroup: String = "EARLY",
    val isActive: Boolean = true
)

@Entity(tableName = "watch_activity")
data class WatchActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val videoId: String,
    val videoTitle: String,
    val channelTitle: String,
    val category: String,
    val durationWatchedSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String
)

@Entity(tableName = "offline_videos")
data class OfflineVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val category: String = "ALL",
    val ageGroup: String = "TEEN",
    val durationText: String = "",
    val description: String = "",
    val downloadedTimestamp: Long = System.currentTimeMillis(),
    val fileSizeMb: Double = 14.5,
    val localFilePath: String = "", // Mobile storage path to downloaded .mp4 file
    val localThumbnailPath: String = "", // Mobile storage path to cached thumbnail image
    val mimeType: String = "video/mp4", // Explicit MIME type for mobile MP4 video playback
    val fileSizeBytes: Long = 0L,
    val durationSeconds: Int = 0,
    val videoQuality: String = "720p",
    val lastPlaybackPositionMs: Long = 0L, // Resume point for offline playback
    val isDownloadComplete: Boolean = true
) {
    fun toVideoItem(): VideoItem {
        val cat = try {
            VideoCategory.valueOf(category)
        } catch (_: Exception) {
            VideoCategory.ALL
        }
        val age = try {
            AgeGroup.valueOf(ageGroup)
        } catch (_: Exception) {
            AgeGroup.TEEN
        }
        return VideoItem(
            id = videoId,
            title = title,
            channelTitle = channelTitle,
            description = description,
            category = cat,
            ageGroup = age,
            durationText = durationText,
            thumbnailUrl = if (localThumbnailPath.isNotBlank()) "file://$localThumbnailPath" else "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
            localFilePath = localFilePath.ifBlank { null },
            localThumbnailPath = localThumbnailPath.ifBlank { null },
            offlineAvailable = true
        )
    }
}

@Entity(tableName = "search_history")
data class SearchQueryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
    val durationText: String = "",
    val category: String = "ALL",
    val ageGroup: String = "TEEN",
    val description: String = "",
    val isShort: Boolean = false,
    val watchedAt: Long = System.currentTimeMillis(),
    val durationWatchedSeconds: Int = 0
) {
    fun toVideoItem(): VideoItem {
        val cat = try {
            VideoCategory.valueOf(category)
        } catch (_: Exception) {
            VideoCategory.ALL
        }
        val age = try {
            AgeGroup.valueOf(ageGroup)
        } catch (_: Exception) {
            AgeGroup.TEEN
        }
        return VideoItem(
            id = videoId,
            title = title,
            channelTitle = channelTitle,
            description = description,
            category = cat,
            ageGroup = age,
            durationText = durationText,
            isShort = isShort,
            thumbnailUrl = thumbnailUrl.ifBlank { "https://img.youtube.com/vi/$videoId/hqdefault.jpg" }
        )
    }
}

@Entity(tableName = "watch_later")
data class WatchLaterEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val durationText: String,
    val category: String,
    val ageGroup: String,
    val description: String,
    val isShort: Boolean,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toVideoItem(): VideoItem {
        val cat = try {
            VideoCategory.valueOf(category)
        } catch (_: Exception) {
            VideoCategory.ALL
        }
        val age = try {
            AgeGroup.valueOf(ageGroup)
        } catch (_: Exception) {
            AgeGroup.TEEN
        }
        return VideoItem(
            id = videoId,
            title = title,
            channelTitle = channelTitle,
            description = description,
            category = cat,
            ageGroup = age,
            durationText = durationText,
            isShort = isShort,
            thumbnailUrl = thumbnailUrl.ifBlank { "https://img.youtube.com/vi/$videoId/hqdefault.jpg" }
        )
    }
}
