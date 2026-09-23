package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for storing and managing video metadata for the offline playback feature.
 * Handles persistence for downloaded MP4 files stored locally on the mobile device.
 */
@Dao
interface OfflineVideoDao {

    /**
     * Get all offline video metadata records ordered by newest download first.
     */
    @Query("SELECT * FROM offline_videos ORDER BY downloadedTimestamp DESC")
    fun getAllOfflineVideosFlow(): Flow<List<OfflineVideoEntity>>

    /**
     * Observe metadata for a specific offline video by video ID.
     */
    @Query("SELECT * FROM offline_videos WHERE videoId = :videoId LIMIT 1")
    fun getOfflineVideoFlow(videoId: String): Flow<OfflineVideoEntity?>

    /**
     * Fetch a specific offline video's metadata by video ID.
     */
    @Query("SELECT * FROM offline_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getOfflineVideoById(videoId: String): OfflineVideoEntity?

    /**
     * Check if a video is stored offline (as Flow).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM offline_videos WHERE videoId = :videoId)")
    fun isVideoOfflineFlow(videoId: String): Flow<Boolean>

    /**
     * Check if a video is stored offline (one-shot).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM offline_videos WHERE videoId = :videoId)")
    suspend fun isVideoOffline(videoId: String): Boolean

    /**
     * Query all offline videos that have a valid downloaded MP4 file path on device storage.
     */
    @Query("SELECT * FROM offline_videos WHERE localFilePath != '' AND isDownloadComplete = 1 ORDER BY downloadedTimestamp DESC")
    fun getPlayableOfflineVideosFlow(): Flow<List<OfflineVideoEntity>>

    /**
     * Filter offline videos by category.
     */
    @Query("SELECT * FROM offline_videos WHERE category = :category ORDER BY downloadedTimestamp DESC")
    fun getOfflineVideosByCategoryFlow(category: String): Flow<List<OfflineVideoEntity>>

    /**
     * Search downloaded offline videos by title or channel name.
     */
    @Query("SELECT * FROM offline_videos WHERE title LIKE '%' || :query || '%' OR channelTitle LIKE '%' || :query || '%' ORDER BY downloadedTimestamp DESC")
    fun searchOfflineVideosFlow(query: String): Flow<List<OfflineVideoEntity>>

    /**
     * Total number of offline videos stored.
     */
    @Query("SELECT COUNT(*) FROM offline_videos")
    fun getOfflineVideoCountFlow(): Flow<Int>

    /**
     * Total storage size in megabytes consumed by offline MP4 videos.
     */
    @Query("SELECT SUM(fileSizeMb) FROM offline_videos")
    fun getTotalOfflineStorageMbFlow(): Flow<Double?>

    /**
     * Insert or update offline video metadata.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineVideo(video: OfflineVideoEntity)

    /**
     * Insert multiple offline video metadata records.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<OfflineVideoEntity>)

    /**
     * Update an offline video metadata record.
     */
    @Update
    suspend fun updateOfflineVideo(video: OfflineVideoEntity)

    /**
     * Update playback progress position (in milliseconds) for resuming MP4 playback offline.
     */
    @Query("UPDATE offline_videos SET lastPlaybackPositionMs = :positionMs WHERE videoId = :videoId")
    suspend fun updatePlaybackPosition(videoId: String, positionMs: Long)

    /**
     * Update the local MP4 storage path, file size, and completion status.
     */
    @Query("UPDATE offline_videos SET localFilePath = :filePath, fileSizeBytes = :fileSizeBytes, fileSizeMb = :fileSizeMb, isDownloadComplete = 1 WHERE videoId = :videoId")
    suspend fun updateDownloadSuccess(videoId: String, filePath: String, fileSizeBytes: Long, fileSizeMb: Double)

    /**
     * Delete an offline video by video ID.
     */
    @Query("DELETE FROM offline_videos WHERE videoId = :videoId")
    suspend fun deleteOfflineVideoById(videoId: String)

    /**
     * Delete an offline video entity.
     */
    @Delete
    suspend fun deleteOfflineVideo(video: OfflineVideoEntity)

    /**
     * Remove all offline videos and their metadata.
     */
    @Query("DELETE FROM offline_videos")
    suspend fun deleteAllOfflineVideos()
}
