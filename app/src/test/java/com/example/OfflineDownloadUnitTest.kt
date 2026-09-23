package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.OfflineVideoEntity
import com.example.data.db.SafeTubeDao
import com.example.data.db.SafeTubeDatabase
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.data.repository.SafeTubeRepository
import com.example.data.storage.OfflineVideoStorageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineDownloadUnitTest {

    private lateinit var db: SafeTubeDatabase
    private lateinit var dao: SafeTubeDao
    private lateinit var storageManager: OfflineVideoStorageManager
    private lateinit var repository: SafeTubeRepository
    private lateinit var context: Context

    private val sampleVideo = VideoItem(
        id = "offline_unit_test_101",
        title = "Learn Multiplication with Dinosaurs",
        channelTitle = "Math Adventure",
        description = "A fun math journey for kids",
        category = VideoCategory.EDUCATION,
        ageGroup = AgeGroup.TEEN,
        durationText = "10:15",
        thumbnailUrl = "https://img.youtube.com/vi/offline_unit_test_101/hqdefault.jpg"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SafeTubeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.safeTubeDao()
        storageManager = OfflineVideoStorageManager(context)
        repository = SafeTubeRepository(dao, db.offlineVideoDao(), storageManager)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        storageManager.deleteVideo(sampleVideo.id)
        db.close()
    }

    @Test
    fun testStorageManagerDownloadAndCheckOfflineVideo() = runBlocking {
        // Trigger downloadVideo
        val file = storageManager.downloadVideo(sampleVideo)

        assertTrue(file.exists())
        assertTrue(storageManager.isVideoDownloaded(sampleVideo.id))

        val retrievedFile = storageManager.getLocalVideoFile(sampleVideo.id)
        assertNotNull(retrievedFile)
        assertEquals(file.absolutePath, retrievedFile?.absolutePath)

        // Clean up
        val deleted = storageManager.deleteVideo(sampleVideo.id)
        assertTrue(deleted)
        assertFalse(storageManager.isVideoDownloaded(sampleVideo.id))
    }

    @Test
    fun testRepositoryDownloadAndSaveOfflineFlow() = runBlocking {
        assertFalse(dao.isVideoOffline(sampleVideo.id))

        // Trigger download flow
        val success = repository.downloadAndSaveOffline(sampleVideo)
        assertTrue(success)

        // Verify entity persisted in Room database
        assertTrue(dao.isVideoOffline(sampleVideo.id))
        val offlineList = dao.getOfflineVideosFlow().first()
        assertEquals(1, offlineList.size)

        val entity = offlineList.first()
        assertEquals(sampleVideo.id, entity.videoId)
        assertEquals(sampleVideo.title, entity.title)
        assertTrue(entity.localFilePath.isNotBlank())
        assertTrue(File(entity.localFilePath).exists())

        // Verify toggleSaveOffline removes it
        repository.toggleSaveOffline(sampleVideo)
        assertFalse(dao.isVideoOffline(sampleVideo.id))
        assertFalse(File(entity.localFilePath).exists())
    }

    @Test
    fun testOfflineVideoEntityRoomInsertionAndFetch() = runBlocking {
        val entity = OfflineVideoEntity(
            videoId = "room_offline_test",
            title = "Test Video",
            channelTitle = "Test Channel",
            description = "Test Desc",
            category = VideoCategory.SCIENCE.name,
            ageGroup = AgeGroup.TEEN.name,
            durationText = "3:45",
            fileSizeMb = 14.5,
            downloadedTimestamp = System.currentTimeMillis(),
            localFilePath = "/mock/path/video.mp4",
            localThumbnailPath = "/mock/path/thumb.jpg"
        )

        dao.insertOfflineVideo(entity)

        val list = dao.getOfflineVideosFlow().first()
        assertEquals(1, list.size)
        assertEquals("/mock/path/video.mp4", list[0].localFilePath)
        assertEquals("/mock/path/thumb.jpg", list[0].localThumbnailPath)

        dao.deleteOfflineVideo("room_offline_test")
        val emptyList = dao.getOfflineVideosFlow().first()
        assertTrue(emptyList.isEmpty())
    }
}
