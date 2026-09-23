package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.service.tracking.TrackingAction
import com.example.service.tracking.TrackingGesture
import com.example.service.tracking.TrackingSensitivity
import com.example.ui.viewmodel.SafeTubeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EyeHeadTrackingUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test initial tracking service defaults`() {
        val config = viewModel.uiState.value.trackingConfig
        val state = viewModel.uiState.value.trackingState

        // Tracking should be configured with smart attention, tilts, blinks, and distance alerts enabled
        assertTrue(config.isSmartAttentionEnabled)
        assertTrue(config.isHeadTiltSeekEnabled)
        assertTrue(config.isBlinkControlEnabled)
        assertTrue(config.isDistanceAlertEnabled)
        assertEquals(TrackingSensitivity.MEDIUM, config.sensitivity)
        assertFalse(state.isFaceDetected)
        assertFalse(state.isPausedByAttention)
    }

    @Test
    fun `test updating tracking configuration`() {
        viewModel.updateTrackingConfig {
            it.copy(
                isTrackingEnabled = true,
                sensitivity = TrackingSensitivity.HIGH,
                isPointerCursorEnabled = true
            )
        }

        val updated = viewModel.uiState.value.trackingConfig
        assertTrue(updated.isTrackingEnabled)
        assertEquals(TrackingSensitivity.HIGH, updated.sensitivity)
        assertTrue(updated.isPointerCursorEnabled)
    }

    @Test
    fun `test tracking dialog visibility state`() {
        assertFalse(viewModel.uiState.value.isTrackingDialogOpen)
        viewModel.setTrackingDialogOpen(true)
        assertTrue(viewModel.uiState.value.isTrackingDialogOpen)
        viewModel.setTrackingDialogOpen(false)
        assertFalse(viewModel.uiState.value.isTrackingDialogOpen)
    }

    @Test
    fun `test simulated head tilt right triggers seek forward action`() = runTest {
        var receivedAction: TrackingAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.trackingManager.actionEvents.collect { action ->
                receivedAction = action
            }
        }

        viewModel.simulateTrackingGesture(TrackingGesture.HEAD_TILT_RIGHT)
        assertEquals(TrackingAction.SEEK_FORWARD_10S, receivedAction)
        assertEquals(TrackingGesture.HEAD_TILT_RIGHT, viewModel.uiState.value.trackingState.lastGesture)
        assertEquals(TrackingAction.SEEK_FORWARD_10S, viewModel.uiState.value.trackingState.lastAction)
    }

    @Test
    fun `test simulated head tilt left triggers seek backward action`() = runTest {
        var receivedAction: TrackingAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.trackingManager.actionEvents.collect { action ->
                receivedAction = action
            }
        }

        viewModel.simulateTrackingGesture(TrackingGesture.HEAD_TILT_LEFT)
        assertEquals(TrackingAction.SEEK_BACKWARD_10S, receivedAction)
        assertEquals(TrackingGesture.HEAD_TILT_LEFT, viewModel.uiState.value.trackingState.lastGesture)
        assertEquals(TrackingAction.SEEK_BACKWARD_10S, viewModel.uiState.value.trackingState.lastAction)
    }

    @Test
    fun `test simulated double blink triggers toggle play pause action`() = runTest {
        var receivedAction: TrackingAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.trackingManager.actionEvents.collect { action ->
                receivedAction = action
            }
        }

        viewModel.simulateTrackingGesture(TrackingGesture.DOUBLE_BLINK)
        assertEquals(TrackingAction.TOGGLE_PLAY_PAUSE, receivedAction)
        assertEquals(TrackingGesture.DOUBLE_BLINK, viewModel.uiState.value.trackingState.lastGesture)
        assertEquals(TrackingAction.TOGGLE_PLAY_PAUSE, viewModel.uiState.value.trackingState.lastAction)
    }

    @Test
    fun `test simulated look away triggers attention pause and resume`() = runTest {
        val actions = mutableListOf<TrackingAction>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.trackingManager.actionEvents.collect { action ->
                actions.add(action)
            }
        }

        viewModel.simulateTrackingGesture(TrackingGesture.LOOKING_AWAY)
        assertTrue(viewModel.uiState.value.trackingState.isPausedByAttention)
        assertTrue(actions.contains(TrackingAction.PAUSE_ATTENTION))

        viewModel.simulateTrackingGesture(TrackingGesture.ATTENTION_RESUMED)
        assertFalse(viewModel.uiState.value.trackingState.isPausedByAttention)
        assertTrue(actions.contains(TrackingAction.RESUME_ATTENTION))
    }

    @Test
    fun `test distance warning simulation`() = runTest {
        var receivedAction: TrackingAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.trackingManager.actionEvents.collect { action ->
                receivedAction = action
            }
        }

        viewModel.simulateTrackingGesture(TrackingGesture.TOO_CLOSE)
        assertEquals(TrackingAction.DISTANCE_WARNING, receivedAction)
        assertTrue(viewModel.uiState.value.trackingState.isTooCloseToScreen)
        assertEquals(TrackingAction.DISTANCE_WARNING, viewModel.uiState.value.trackingState.lastAction)
    }
}
