package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.SafeTubeViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaybackSpeedUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test initial playback speed is normal 1_0x`() {
        val state = viewModel.uiState.value
        assertEquals(1.0f, state.playbackSpeed, 0.001f)
    }

    @Test
    fun `test setting custom playback speeds updates state`() {
        viewModel.setPlaybackSpeed(1.5f)
        assertEquals(1.5f, viewModel.uiState.value.playbackSpeed, 0.001f)

        viewModel.setPlaybackSpeed(2.0f)
        assertEquals(2.0f, viewModel.uiState.value.playbackSpeed, 0.001f)

        viewModel.setPlaybackSpeed(0.5f)
        assertEquals(0.5f, viewModel.uiState.value.playbackSpeed, 0.001f)

        viewModel.setPlaybackSpeed(1.0f)
        assertEquals(1.0f, viewModel.uiState.value.playbackSpeed, 0.001f)
    }
}
