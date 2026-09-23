package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf

enum class ThemeMode(
    val title: String,
    val subtitle: String,
    val iconEmoji: String
) {
    SYSTEM(
        title = "System Default",
        subtitle = "Matches your device's system appearance",
        iconEmoji = "🌓"
    ),
    LIGHT(
        title = "Light Mode",
        subtitle = "Vibrant, crisp daytime color palette",
        iconEmoji = "☀️"
    ),
    DARK(
        title = "Dark Mode",
        subtitle = "Gentle midnight tones to reduce eye strain",
        iconEmoji = "🌙"
    );

    fun isDark(systemInDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemInDark
        LIGHT -> false
        DARK -> true
    }

    fun next(): ThemeMode = when (this) {
        SYSTEM -> LIGHT
        LIGHT -> DARK
        DARK -> SYSTEM
    }
}

val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }
val LocalIsDarkTheme = compositionLocalOf { false }
