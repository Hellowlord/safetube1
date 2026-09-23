package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.SafeTubeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SleepTimerUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test initial sleep timer state is inactive`() {
        val state = viewModel.uiState.value
        assertNull(state.sleepTimerRemainingSeconds)
        assertNull(state.sleepTimerTotalMinutes)
        assertFalse(state.isSleepTimerTriggered)
    }

    @Test
    fun `test setting sleep timer configures state`() {
        viewModel.setSleepTimer(15)
        val state15 = viewModel.uiState.value
        assertEquals(15, state15.sleepTimerTotalMinutes)
        assertEquals(15 * 60, state15.sleepTimerRemainingSeconds)
        assertFalse(state15.isSleepTimerTriggered)

        viewModel.setSleepTimer(30)
        val state30 = viewModel.uiState.value
        assertEquals(30, state30.sleepTimerTotalMinutes)
        assertEquals(30 * 60, state30.sleepTimerRemainingSeconds)

        viewModel.setSleepTimer(60)
        val state60 = viewModel.uiState.value
        assertEquals(60, state60.sleepTimerTotalMinutes)
        assertEquals(60 * 60, state60.sleepTimerRemainingSeconds)
    }

    @Test
    fun `test canceling sleep timer clears state`() {
        viewModel.setSleepTimer(45)
        assertEquals(45, viewModel.uiState.value.sleepTimerTotalMinutes)

        viewModel.cancelSleepTimer()
        val state = viewModel.uiState.value
        assertNull(state.sleepTimerRemainingSeconds)
        assertNull(state.sleepTimerTotalMinutes)
        assertFalse(state.isSleepTimerTriggered)
    }

    @Test
    fun `test dismissing triggered sleep timer resets trigger flag`() {
        viewModel.dismissSleepTimerTriggered()
        assertFalse(viewModel.uiState.value.isSleepTimerTriggered)
    }
}
