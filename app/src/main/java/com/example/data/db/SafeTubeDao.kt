package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeTubeDao {

    // Parent Settings
    @Query("SELECT * FROM parent_settings WHERE id = 1 LIMIT 1")
    fun getParentSettingsFlow(): Flow<ParentSettingsEntity?>

    @Query("SELECT * FROM parent_settings WHERE id = 1 LIMIT 1")
    suspend fun getParentSettings(): ParentSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateParentSettings(settings: ParentSettingsEntity)

    @Query("UPDATE parent_settings SET screenTimeUsedSecondsToday = :usedSeconds, lastActiveDate = :date WHERE id = 1")
    suspend fun updateScreenTimeUsed(usedSeconds: Int, date: String)

    // User Profiles
    @Query("SELECT * FROM user_profiles ORDER BY id ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isActive = CASE WHEN id = :profileId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(profileId: Long)

    // Offline Videos
    @Query("SELECT * FROM offline_videos ORDER BY downloadedTimestamp DESC")
    fun getOfflineVideosFlow(): Flow<List<OfflineVideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_videos WHERE videoId = :videoId)")
    fun isVideoOfflineFlow(videoId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_videos WHERE videoId = :videoId)")
    suspend fun isVideoOffline(videoId: String): Boolean

    @Query("SELECT * FROM offline_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getOfflineVideo(videoId: String): OfflineVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineVideo(video: OfflineVideoEntity)

    @Query("DELETE FROM offline_videos WHERE videoId = :videoId")
    suspend fun deleteOfflineVideo(videoId: String)

    // Watch Activity & Screen Time Tracking
    @Query("SELECT * FROM watch_activity ORDER BY timestamp DESC LIMIT 100")
    fun getRecentActivitiesFlow(): Flow<List<WatchActivityEntity>>

    @Query("SELECT * FROM watch_activity WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getActivitiesForDateFlow(dateString: String): Flow<List<WatchActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchActivity(activity: WatchActivityEntity)

    @Query("DELETE FROM watch_activity")
    suspend fun clearWatchActivities()

    // Search History
    @Query("SELECT query FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentQueriesFlow(limit: Int = 15): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchQueryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllWatchHistoryFlow(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentWatchHistoryFlow(limit: Int = 20): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWatchHistory(history: WatchHistoryEntity)

    @Query("UPDATE watch_history SET durationWatchedSeconds = :seconds, watchedAt = :timestamp WHERE videoId = :videoId")
    suspend fun updateWatchProgress(videoId: String, seconds: Int, timestamp: Long)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteWatchHistoryItem(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllWatchHistory()

    @Query("SELECT COUNT(*) FROM watch_history")
    fun getWatchHistoryCountFlow(): Flow<Int>

    // Watch Later
    @Query("SELECT * FROM watch_later ORDER BY savedAt DESC")
    fun getAllWatchLaterFlow(): Flow<List<WatchLaterEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE videoId = :videoId)")
    fun isWatchLaterFlow(videoId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE videoId = :videoId)")
    suspend fun isWatchLater(videoId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchLater(item: WatchLaterEntity)

    @Query("DELETE FROM watch_later WHERE videoId = :videoId")
    suspend fun deleteWatchLaterItem(videoId: String)

    @Query("DELETE FROM watch_later")
    suspend fun clearAllWatchLater()

    @Query("SELECT COUNT(*) FROM watch_later")
    fun getWatchLaterCountFlow(): Flow<Int>
}
