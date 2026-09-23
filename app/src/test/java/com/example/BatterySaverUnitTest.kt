package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.SafeTubeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BatterySaverUnitTest {

    private lateinit var viewModel: SafeTubeViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SafeTubeViewModel(app)
    }

    @Test
    fun `test battery saver is enabled by default`() {
        val state = viewModel.uiState.value
        assertTrue(state.isBatterySaverEnabled)
    }

    @Test
    fun `test battery drop below 20 activates battery saver and reduces resolution to 240p`() {
        // Set battery level to 15% (<= 20%)
        viewModel.setSimulatedBatteryLevel(15)

        val state = viewModel.uiState.value
        assertTrue("Battery saver should be active when battery is <= 20%", state.isBatterySaverActive)
        assertEquals(15, state.batteryState.level)
        assertEquals("240p", state.selectedVideoResolution)
    }

    @Test
    fun `test battery above 20 deactivates battery saver and restores resolution`() {
        // First drop battery
        viewModel.setSimulatedBatteryLevel(15)
        assertTrue(viewModel.uiState.value.isBatterySaverActive)

        // Then restore battery to 85%
        viewModel.setSimulatedBatteryLevel(85)
        val state = viewModel.uiState.value
        assertFalse("Battery saver should deactivate when battery > 20%", state.isBatterySaverActive)
        assertEquals(85, state.batteryState.level)
        assertEquals("auto", state.selectedVideoResolution)
    }

    @Test
    fun `test disabling battery saver prevents auto resolution reduction`() {
        viewModel.setBatterySaverEnabled(false)
        viewModel.setSimulatedBatteryLevel(10)

        val state = viewModel.uiState.value
        assertFalse("Battery saver should not be active when disabled", state.isBatterySaverActive)
    }
}
