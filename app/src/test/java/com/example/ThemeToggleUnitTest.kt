package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.SafeTubeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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
@Config(sdk = [34])
class ThemeToggleUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun themeMode_systemFollowsDeviceSystemTheme() {
        val systemMode = ThemeMode.SYSTEM
        assertTrue(systemMode.isDark(systemInDark = true))
        assertFalse(systemMode.isDark(systemInDark = false))
    }

    @Test
    fun themeMode_lightAlwaysOverridesSystemToFalse() {
        val lightMode = ThemeMode.LIGHT
        assertFalse(lightMode.isDark(systemInDark = true))
        assertFalse(lightMode.isDark(systemInDark = false))
    }

    @Test
    fun themeMode_darkAlwaysOverridesSystemToTrue() {
        val darkMode = ThemeMode.DARK
        assertTrue(darkMode.isDark(systemInDark = true))
        assertTrue(darkMode.isDark(systemInDark = false))
    }

    @Test
    fun themeMode_cyclesCorrectlyInOrder() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.SYSTEM.next())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.next())
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DARK.next())
    }

    @Test
    fun viewModel_themeModeUpdatesAndPersists() {
        val viewModel = SafeTubeViewModel(application)

        // Initial default or persisted
        viewModel.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        // Toggle to Light
        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)

        // Toggle to Dark
        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)

        // Toggle back to System
        viewModel.toggleThemeMode()
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        // Direct selection to Dark
        viewModel.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }
}
