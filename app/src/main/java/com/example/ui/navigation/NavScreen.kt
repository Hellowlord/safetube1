package com.example.ui.navigation

import com.example.data.models.VideoItem

sealed class Screen {
    object WelcomeIntro : Screen()
    object Home : Screen()
    object Music : Screen()
    object CreateContent : Screen()
    object SafeShorts : Screen()
    object OfflineMode : Screen()
    data class Player(val video: VideoItem, val isOfflinePlayback: Boolean = false) : Screen()
    object ParentDashboard : Screen()
    object WatchHistory : Screen()
    object WatchLater : Screen()
    object Settings : Screen()
}

enum class BottomTab(val label: String, val iconEmoji: String) {
    HOME("Home", "🏠"),
    SHORTS("Shorts", "⚡"),
    MUSIC("Music", "🎵"),
    CREATE("Create", "➕"),
    OFFLINE("Offline", "📥"),
    SETTINGS("Settings", "⚙️")
}
