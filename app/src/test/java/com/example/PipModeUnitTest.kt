package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.SafeTubeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PipModeUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    private val testVideo = VideoItem(
        id = "test_vid_123",
        title = "Learn Alphabet ABCs",
        channelTitle = "Fun Learning",
        description = "Educational video for kids",
        category = VideoCategory.EDUCATION,
        ageGroup = AgeGroup.TEEN,
        durationText = "3:45"
    )

    private val secondVideo = VideoItem(
        id = "second_vid_456",
        title = "Nursery Rhymes",
        channelTitle = "Kids Music",
        description = "Sing along songs",
        category = VideoCategory.MUSIC,
        ageGroup = AgeGroup.TEEN,
        durationText = "2:15"
    )

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test initial pip state is inactive`() {
        val state = viewModel.uiState.value
        assertFalse(state.isPipActive)
        assertNull(state.pipVideo)
    }

    @Test
    fun `test enterPipMode activates pip and changes screen`() {
        // Start playing video
        viewModel.startPlayingVideo(testVideo)
        assertTrue(viewModel.uiState.value.currentScreen is Screen.Player)

        // Enter PiP mode
        viewModel.enterPipMode()

        val state = viewModel.uiState.value
        assertTrue(state.isPipActive)
        assertEquals(testVideo.id, state.pipVideo?.id)
        // User should now be on Home/active tab screen to browse other sections
        assertTrue(state.currentScreen !is Screen.Player)
    }

    @Test
    fun `test navigating away from Player automatically enters PiP`() {
        viewModel.startPlayingVideo(testVideo)
        assertTrue(viewModel.uiState.value.currentScreen is Screen.Player)

        // Navigate to Music section
        viewModel.navigateTo(Screen.Music)

        val state = viewModel.uiState.value
        assertEquals(Screen.Music, state.currentScreen)
        assertEquals(BottomTab.MUSIC, state.currentTab)
        assertTrue(state.isPipActive)
        assertEquals(testVideo.id, state.pipVideo?.id)
    }

    @Test
    fun `test expandPipToPlayer restores full player screen`() {
        viewModel.startPlayingVideo(testVideo)
        viewModel.enterPipMode()

        assertTrue(viewModel.uiState.value.isPipActive)

        // Expand PiP back to full player
        viewModel.expandPipToPlayer()

        val state = viewModel.uiState.value
        assertFalse(state.isPipActive)
        assertNull(state.pipVideo)
        assertTrue(state.currentScreen is Screen.Player)
        assertEquals(testVideo.id, (state.currentScreen as Screen.Player).video.id)
    }

    @Test
    fun `test closePip deactivates pip`() {
        viewModel.startPlayingVideo(testVideo)
        viewModel.enterPipMode()
        assertTrue(viewModel.uiState.value.isPipActive)

        // Close PiP
        viewModel.closePip()

        val state = viewModel.uiState.value
        assertFalse(state.isPipActive)
        assertNull(state.pipVideo)
    }

    @Test
    fun `test starting new video clears existing PiP`() {
        viewModel.startPlayingVideo(testVideo)
        viewModel.enterPipMode()
        assertTrue(viewModel.uiState.value.isPipActive)

        // Start another video
        viewModel.startPlayingVideo(secondVideo)

        val state = viewModel.uiState.value
        assertFalse(state.isPipActive)
        assertNull(state.pipVideo)
        assertTrue(state.currentScreen is Screen.Player)
        assertEquals(secondVideo.id, (state.currentScreen as Screen.Player).video.id)
    }
}
