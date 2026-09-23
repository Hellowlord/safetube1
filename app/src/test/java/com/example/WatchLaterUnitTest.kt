package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.SafeTubeDao
import com.example.data.db.SafeTubeDatabase
import com.example.data.db.WatchLaterEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.SafeTubeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WatchLaterUnitTest {

    private lateinit var db: SafeTubeDatabase
    private lateinit var dao: SafeTubeDao

    private val testVideo = VideoItem(
        id = "later_test_123",
        title = "Science Experiments for Kids",
        channelTitle = "Fun Science",
        description = "Cool hands-on experiments",
        category = VideoCategory.SCIENCE,
        ageGroup = AgeGroup.TEEN,
        durationText = "5:30"
    )

    private val testVideo2 = VideoItem(
        id = "later_test_456",
        title = "Drawing Cartoons",
        channelTitle = "Art Studio",
        description = "Step by step drawing",
        category = VideoCategory.ART,
        ageGroup = AgeGroup.EARLY,
        durationText = "8:15"
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SafeTubeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.safeTubeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `test insert and query watch later item`() = runBlocking {
        val entity = WatchLaterEntity(
            videoId = testVideo.id,
            title = testVideo.title,
            channelTitle = testVideo.channelTitle,
            thumbnailUrl = "https://img.youtube.com/vi/${testVideo.id}/hqdefault.jpg",
            durationText = testVideo.durationText,
            category = testVideo.category.name,
            ageGroup = testVideo.ageGroup.name,
            description = testVideo.description,
            isShort = false
        )

        dao.insertWatchLater(entity)

        val isSaved = dao.isWatchLater(testVideo.id)
        assertTrue(isSaved)

        val items = dao.getAllWatchLaterFlow().first()
        assertEquals(1, items.size)
        assertEquals(testVideo.id, items[0].videoId)
        assertEquals(testVideo.title, items[0].title)

        val count = dao.getWatchLaterCountFlow().first()
        assertEquals(1, count)
    }

    @Test
    fun `test delete watch later item`() = runBlocking {
        val entity1 = WatchLaterEntity(
            videoId = testVideo.id,
            title = testVideo.title,
            channelTitle = testVideo.channelTitle,
            thumbnailUrl = "",
            durationText = testVideo.durationText,
            category = testVideo.category.name,
            ageGroup = testVideo.ageGroup.name,
            description = testVideo.description,
            isShort = false
        )
        val entity2 = WatchLaterEntity(
            videoId = testVideo2.id,
            title = testVideo2.title,
            channelTitle = testVideo2.channelTitle,
            thumbnailUrl = "",
            durationText = testVideo2.durationText,
            category = testVideo2.category.name,
            ageGroup = testVideo2.ageGroup.name,
            description = testVideo2.description,
            isShort = false
        )

        dao.insertWatchLater(entity1)
        dao.insertWatchLater(entity2)

        var items = dao.getAllWatchLaterFlow().first()
        assertEquals(2, items.size)

        dao.deleteWatchLaterItem(testVideo.id)

        items = dao.getAllWatchLaterFlow().first()
        assertEquals(1, items.size)
        assertEquals(testVideo2.id, items[0].videoId)
        assertFalse(dao.isWatchLater(testVideo.id))
        assertTrue(dao.isWatchLater(testVideo2.id))
    }

    @Test
    fun `test clear all watch later items`() = runBlocking {
        val entity = WatchLaterEntity(
            videoId = testVideo.id,
            title = testVideo.title,
            channelTitle = testVideo.channelTitle,
            thumbnailUrl = "",
            durationText = testVideo.durationText,
            category = testVideo.category.name,
            ageGroup = testVideo.ageGroup.name,
            description = testVideo.description,
            isShort = false
        )
        dao.insertWatchLater(entity)
        dao.clearAllWatchLater()

        val items = dao.getAllWatchLaterFlow().first()
        assertTrue(items.isEmpty())
        assertEquals(0, dao.getWatchLaterCountFlow().first())
    }

    @Test
    fun `test WatchLaterEntity to VideoItem conversion`() {
        val entity = WatchLaterEntity(
            videoId = testVideo.id,
            title = testVideo.title,
            channelTitle = testVideo.channelTitle,
            thumbnailUrl = "",
            durationText = testVideo.durationText,
            category = testVideo.category.name,
            ageGroup = testVideo.ageGroup.name,
            description = testVideo.description,
            isShort = false
        )
        val converted = entity.toVideoItem()
        assertEquals(testVideo.id, converted.id)
        assertEquals(testVideo.title, converted.title)
        assertEquals(testVideo.channelTitle, converted.channelTitle)
        assertEquals("https://img.youtube.com/vi/${testVideo.id}/hqdefault.jpg", converted.thumbnailUrl)
    }

    @Test
    fun `test navigation to WatchLater screen`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SafeTubeViewModel(app)
        viewModel.navigateTo(Screen.WatchLater)
        assertEquals(Screen.WatchLater, viewModel.uiState.value.currentScreen)
    }
}
