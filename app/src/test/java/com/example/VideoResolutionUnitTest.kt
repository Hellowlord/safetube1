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
class VideoResolutionUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test initial video resolution is auto`() {
        val state = viewModel.uiState.value
        assertEquals("auto", state.selectedVideoResolution)
    }

    @Test
    fun `test toggling between video resolutions updates state`() {
        viewModel.setVideoResolution("hd1080")
        assertEquals("hd1080", viewModel.uiState.value.selectedVideoResolution)

        viewModel.setVideoResolution("hd720")
        assertEquals("hd720", viewModel.uiState.value.selectedVideoResolution)

        viewModel.setVideoResolution("large") // 480p
        assertEquals("large", viewModel.uiState.value.selectedVideoResolution)

        viewModel.setVideoResolution("medium") // 360p
        assertEquals("medium", viewModel.uiState.value.selectedVideoResolution)

        viewModel.setVideoResolution("small") // 240p
        assertEquals("small", viewModel.uiState.value.selectedVideoResolution)

        viewModel.setVideoResolution("auto")
        assertEquals("auto", viewModel.uiState.value.selectedVideoResolution)
    }
}
