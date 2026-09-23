package com.example.data.models

enum class AgeGroup(val displayName: String, val ageRange: String, val iconEmoji: String) {
    TODDLER("All Ages", "Universal", "🌐"),
    EARLY("General", "Everyone", "✨"),
    TWEEN("Tweens", "Ages 9-12", "🚀"),
    TEEN("Teens & Adults", "All", "⚡")
}

enum class VideoCategory(val displayName: String, val emoji: String) {
    ALL("All", "⚡"),
    FYP("For You", "✨"),
    TRENDING("Trending", "🔥"),
    COOKING("Cooking & Food", "🍳"),
    MUSIC("Music", "🎵"),
    SCIENCE("Science & Facts", "🔬"),
    GAMING("Gaming", "🎮"),
    SPORTS("Sports & Stunts", "🏆"),
    CREATORS("Top Creators", "🌟"),
    ENTERTAINMENT("Movies & Fun", "🎬"),
    EDUCATION("Learning & Code", "💡"),
    CARTOONS("Animation", "🎨"),
    ART("Art & DIY", "✂️"),
    BEDTIME("Lo-Fi & Chill", "🌙"),
    NATURE("Nature & Animals", "🌿"),
    TECH("Tech & Gadgets", "💻"),
    SHORTS("Shorts", "⚡")
}

data class VideoItem(
    val id: String, // YouTube Video ID
    val title: String,
    val channelTitle: String,
    val description: String = "",
    val category: VideoCategory = VideoCategory.ALL,
    val ageGroup: AgeGroup = AgeGroup.TEEN,
    val durationText: String = "",
    val isShort: Boolean = false,
    val thumbnailUrl: String = "https://img.youtube.com/vi/$id/hqdefault.jpg",
    val tags: List<String> = emptyList(),
    val offlineAvailable: Boolean = false,
    val viewCountText: String = "",
    val localFilePath: String? = null,
    val localThumbnailPath: String? = null
)

