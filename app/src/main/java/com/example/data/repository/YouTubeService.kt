package com.example.data.repository

import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object YouTubeService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Regex to extract 11-character YouTube video ID
    private val YOUTUBE_URL_PATTERN = Pattern.compile(
        "(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=|shorts\\/))([\\w-]{11})",
        Pattern.CASE_INSENSITIVE
    )

    private val DIRECT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{11}$")

    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (DIRECT_ID_PATTERN.matcher(trimmed).matches()) {
            return trimmed
        }
        val matcher = YOUTUBE_URL_PATTERN.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    suspend fun fetchVideoDetails(videoId: String): VideoItem = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val title = json.optString("title", "Featured Video")
                    val authorName = json.optString("author_name", "Featured Creator")
                    val thumbnail = json.optString("thumbnail_url", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")

                    return@withContext VideoItem(
                        id = videoId,
                        title = title,
                        channelTitle = authorName,
                        description = "Watch this video in high definition on SafePlay.",
                        category = VideoCategory.ALL,
                        ageGroup = AgeGroup.TEEN,
                        durationText = "Video",
                        thumbnailUrl = thumbnail,
                        viewCountText = "Featured Video"
                    )
                }
            }
        } catch (_: Exception) {
        }

        // Fallback item with standard thumbnail
        VideoItem(
            id = videoId,
            title = "Featured Video ($videoId)",
            channelTitle = "Featured Creator",
            description = "Playing in high definition on SafePlay.",
            category = VideoCategory.ALL,
            ageGroup = AgeGroup.TEEN,
            durationText = "Play",
            thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
            viewCountText = "Featured Video"
        )
    }

    suspend fun searchYouTube(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<VideoItem>()
        val seenIds = mutableSetOf<String>()

        // 1. If query is a direct video ID or URL, resolve it first
        val directId = extractVideoId(query)
        if (directId != null) {
            val directVideo = fetchVideoDetails(directId)
            results.add(directVideo)
            seenIds.add(directId)
        }

        // 2. Query live YouTube search results
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.youtube.com/results?search_query=$encodedQuery"
            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                val parsed = parseYouTubeSearchHtml(html)
                for (item in parsed) {
                    if (!seenIds.contains(item.id)) {
                        seenIds.add(item.id)
                        results.add(item)
                    }
                }
            }
        } catch (_: Exception) {
        }

        // 3. Match from local rich catalog as well
        val localMatches = CuratedVideoData.CURATED_VIDEOS.filter { video ->
            val q = query.lowercase().trim()
            video.title.lowercase().contains(q) ||
                    video.channelTitle.lowercase().contains(q) ||
                    video.tags.any { it.lowercase().contains(q) } ||
                    video.category.name.lowercase().contains(q)
        }
        for (item in localMatches) {
            if (!seenIds.contains(item.id)) {
                seenIds.add(item.id)
                results.add(item)
            }
        }

        results
    }

    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val suggestions = mutableListOf<String>()
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=youtube&ds=yt&q=$encoded"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                // Suggest response format is: window.google.ac.h(["query",[["suggest1",0,[...]],["suggest2",0]]])
                val jsonStart = body.indexOf("(")
                val jsonEnd = body.lastIndexOf(")")
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    val jsonStr = body.substring(jsonStart + 1, jsonEnd)
                    val array = JSONArray(jsonStr)
                    if (array.length() > 1) {
                        val itemsArray = array.getJSONArray(1)
                        for (i in 0 until itemsArray.length()) {
                            val item = itemsArray.getJSONArray(i)
                            if (item.length() > 0) {
                                suggestions.add(item.getString(0))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        suggestions
    }

    private fun parseYouTubeSearchHtml(html: String): List<VideoItem> {
        val items = mutableListOf<VideoItem>()
        try {
            // Locate videoId occurrences and associated metadata
            // YouTube search results contain "videoId":"xyz12345678"
            val videoIdPattern = Pattern.compile(
                "\"videoId\":\"([a-zA-Z0-9_-]{11})\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"\\}\\].*?\"ownerText\":\\{\"runs\":\\[\\{\"text\":\"(.*?)\"\\}",
                Pattern.DOTALL
            )
            val matcher = videoIdPattern.matcher(html)
            var count = 0
            while (matcher.find() && count < 25) {
                val id = matcher.group(1) ?: continue
                val rawTitle = matcher.group(2) ?: "Featured Video"
                val rawChannel = matcher.group(3) ?: "SafePlay Creator"

                // Unescape unicode and html entities
                val title = unescapeJavaString(rawTitle)
                val channel = unescapeJavaString(rawChannel)

                items.add(
                    VideoItem(
                        id = id,
                        title = title,
                        channelTitle = channel,
                        description = "SafePlay featured video",
                        category = VideoCategory.ALL,
                        ageGroup = AgeGroup.TEEN,
                        durationText = "HD",
                        thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
                        viewCountText = "Featured Video"
                    )
                )
                count++
            }

            // Fallback simpler regex if complex regex didn't match
            if (items.isEmpty()) {
                val simplePattern = Pattern.compile("\"videoId\":\"([a-zA-Z0-9_-]{11})\"")
                val simpleMatcher = simplePattern.matcher(html)
                val addedIds = mutableSetOf<String>()
                while (simpleMatcher.find() && items.size < 15) {
                    val id = simpleMatcher.group(1) ?: continue
                    if (!addedIds.contains(id)) {
                        addedIds.add(id)
                        items.add(
                            VideoItem(
                                id = id,
                                title = "Featured Video ($id)",
                                channelTitle = "SafePlay Creator",
                                description = "Watch now on SafePlay",
                                category = VideoCategory.ALL,
                                ageGroup = AgeGroup.TEEN,
                                durationText = "HD",
                                thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
                                viewCountText = "Featured Video"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
        return items
    }

    private fun unescapeJavaString(st: String): String {
        return st
            .replace("\\u0026", "&")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }
}
