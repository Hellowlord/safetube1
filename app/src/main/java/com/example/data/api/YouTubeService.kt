package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object YouTubeService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Read API key safely from BuildConfig or system property
    private fun getApiKey(): String {
        return try {
            val buildConfigClass = Class.forName("com.example.BuildConfig")
            val field = buildConfigClass.getField("YOUTUBE_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun searchYouTube(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            val apiResults = searchViaDataApi(query, apiKey)
            if (apiResults.isNotEmpty()) {
                return@withContext apiResults
            }
        }
        // Fallback or public YouTube suggest/search endpoint without requiring API key
        return@withContext searchViaPublicEndpoint(query)
    }

    /**
     * Fetch real-time trending videos from YouTube Data API v3 (chart=mostPopular).
     * Falls back seamlessly to curated viral trending videos if the API key is unconfigured or rate-limited.
     */
    suspend fun fetchTrendingVideos(regionCode: String = "US", maxResults: Int = 20): List<VideoItem> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            val trendingFromApi = fetchTrendingViaDataApi(apiKey, regionCode, maxResults)
            if (trendingFromApi.isNotEmpty()) {
                return@withContext trendingFromApi
            }
        }
        return@withContext getFallbackTrendingVideos()
    }

    private fun fetchTrendingViaDataApi(apiKey: String, regionCode: String, maxResults: Int): List<VideoItem> {
        try {
            val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails,statistics&chart=mostPopular&regionCode=$regionCode&maxResults=$maxResults&key=$apiKey"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("YouTubeService", "Trending API response code: ${response.code}")
                return emptyList()
            }

            val bodyString = response.body?.string() ?: return emptyList()
            val json = JSONObject(bodyString)
            val items = json.optJSONArray("items") ?: return emptyList()
            val result = mutableListOf<VideoItem>()

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val videoId = item.optString("id")
                if (videoId.isNullOrBlank()) continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = cleanHtmlEntities(snippet.optString("title", "Trending Video"))
                val channelTitle = snippet.optString("channelTitle", "YouTube Creator")
                val description = snippet.optString("description", "")

                val contentDetails = item.optJSONObject("contentDetails")
                val durationIso = contentDetails?.optString("duration") ?: "PT10M"
                val durationText = parseIsoDuration(durationIso)

                val statistics = item.optJSONObject("statistics")
                val viewCountStr = statistics?.optString("viewCount") ?: ""
                val viewCountText = formatViewCount(viewCountStr)

                val thumbnails = snippet.optJSONObject("thumbnails")
                val highThumb = thumbnails?.optJSONObject("high")?.optString("url")
                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                val isShort = durationIso.startsWith("PT") &&
                        !durationIso.contains("H") &&
                        (!durationIso.contains("M") || durationIso.startsWith("PT0M"))

                result.add(
                    VideoItem(
                        id = videoId,
                        title = title,
                        channelTitle = channelTitle,
                        description = description,
                        category = deduceCategory(title, description),
                        ageGroup = AgeGroup.TEEN,
                        durationText = durationText,
                        thumbnailUrl = highThumb,
                        tags = listOf("trending", "viral", "popular"),
                        viewCountText = viewCountText,
                        isShort = isShort
                    )
                )
            }
            return result
        } catch (e: Exception) {
            Log.e("YouTubeService", "Error fetching trending videos: ${e.message}")
            return emptyList()
        }
    }

    private fun searchViaDataApi(query: String, apiKey: String): List<VideoItem> {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&q=$encodedQuery&type=video&key=$apiKey"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val bodyString = response.body?.string() ?: return emptyList()
            val json = JSONObject(bodyString)
            val items = json.optJSONArray("items") ?: return emptyList()
            val result = mutableListOf<VideoItem>()

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val idObj = item.optJSONObject("id")
                val videoId = idObj?.optString("videoId") ?: continue
                val snippet = item.optJSONObject("snippet") ?: continue

                val title = snippet.optString("title", "Video")
                val channelTitle = snippet.optString("channelTitle", "Creator")
                val description = snippet.optString("description", "")
                val publishedAt = snippet.optString("publishedAt", "")

                result.add(
                    VideoItem(
                        id = videoId,
                        title = cleanHtmlEntities(title),
                        channelTitle = channelTitle,
                        description = description,
                        category = deduceCategory(title, description),
                        ageGroup = AgeGroup.TEEN,
                        durationText = "Video",
                        tags = listOf("search", "youtube", query.lowercase()),
                        viewCountText = "Trending"
                    )
                )
            }
            return result
        } catch (e: Exception) {
            Log.e("YouTubeService", "YouTube Data API error: ${e.message}")
            return emptyList()
        }
    }

    private fun searchViaPublicEndpoint(query: String): List<VideoItem> {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            // Rapid YouTube scrape search fallback for zero-quota errors
            val url = "https://www.youtube.com/results?search_query=$encoded"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val initialDataIndex = body.indexOf("var ytInitialData = ")
            if (initialDataIndex != -1) {
                val jsonStart = initialDataIndex + "var ytInitialData = ".length
                val jsonEnd = body.indexOf(";</script>", jsonStart)
                if (jsonEnd != -1) {
                    val jsonStr = body.substring(jsonStart, jsonEnd)
                    val jsonObj = JSONObject(jsonStr)
                    return parseYtInitialData(jsonObj, query)
                }
            }
        } catch (e: Exception) {
            Log.e("YouTubeService", "Search public endpoint error: ${e.message}")
        }
        return emptyList()
    }

    private fun parseYtInitialData(root: JSONObject, originalQuery: String): List<VideoItem> {
        val list = mutableListOf<VideoItem>()
        try {
            val contents = root.optJSONObject("contents")
                ?.optJSONObject("twoColumnSearchResultsRenderer")
                ?.optJSONObject("primaryContents")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: return emptyList()

            for (i in 0 until contents.length()) {
                val itemSection = contents.optJSONObject(i)
                    ?.optJSONObject("itemSectionRenderer")
                    ?.optJSONArray("contents") ?: continue

                for (j in 0 until itemSection.length()) {
                    val videoRenderer = itemSection.optJSONObject(j)?.optJSONObject("videoRenderer") ?: continue
                    val videoId = videoRenderer.optString("videoId")
                    if (videoId.isNullOrBlank()) continue

                    val titleRuns = videoRenderer.optJSONObject("title")?.optJSONArray("runs")
                    val title = titleRuns?.optJSONObject(0)?.optString("text") ?: "Video"

                    val ownerRuns = videoRenderer.optJSONObject("ownerText")?.optJSONArray("runs")
                    val channel = ownerRuns?.optJSONObject(0)?.optString("text") ?: "YouTube Creator"

                    val lengthText = videoRenderer.optJSONObject("lengthText")?.optString("simpleText") ?: "04:20"
                    val viewCount = videoRenderer.optJSONObject("viewCountText")?.optString("simpleText") ?: "Popular"

                    list.add(
                        VideoItem(
                            id = videoId,
                            title = cleanHtmlEntities(title),
                            channelTitle = channel,
                            description = "Search result for $originalQuery",
                            category = deduceCategory(title, channel),
                            ageGroup = AgeGroup.TEEN,
                            durationText = lengthText,
                            tags = listOf(originalQuery.lowercase()),
                            viewCountText = viewCount
                        )
                    )
                    if (list.size >= 30) break
                }
            }
        } catch (e: Exception) {
            Log.e("YouTubeService", "Error parsing initial data: ${e.message}")
        }
        return list
    }

    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=youtube&ds=yt&q=$encoded"
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            // Format is window.google.ac.h(["query",[["suggestion1",0,[...]], ...]])
            val start = body.indexOf("(") + 1
            val end = body.lastIndexOf(")")
            if (start in 1 until end) {
                val jsonArray = JSONArray(body.substring(start, end))
                val suggestionsArray = jsonArray.optJSONArray(1)
                val list = mutableListOf<String>()
                if (suggestionsArray != null) {
                    for (i in 0 until minOf(suggestionsArray.length(), 6)) {
                        val suggestionItem = suggestionsArray.optJSONArray(i)
                        val text = suggestionItem?.optString(0)
                        if (!text.isNullOrBlank()) {
                            list.add(text)
                        }
                    }
                }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e("YouTubeService", "Suggestions error: ${e.message}")
        }
        return@withContext emptyList()
    }

    suspend fun fetchVideoDetails(videoId: String): VideoItem? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails,statistics&id=$videoId&key=$apiKey"
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val items = json.optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val item = items.getJSONObject(0)
                        val snippet = item.getJSONObject("snippet")
                        val title = snippet.optString("title", "Video")
                        val channelTitle = snippet.optString("channelTitle", "Creator")
                        val description = snippet.optString("description", "")
                        return@withContext VideoItem(
                            id = videoId,
                            title = cleanHtmlEntities(title),
                            channelTitle = channelTitle,
                            description = description,
                            category = deduceCategory(title, description),
                            ageGroup = AgeGroup.TEEN,
                            durationText = "HD Video",
                            tags = listOf("direct", "video"),
                            viewCountText = "Popular"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("YouTubeService", "Fetch details error: ${e.message}")
            }
        }

        // Fallback video item with real ID
        return@withContext VideoItem(
            id = videoId,
            title = "YouTube Video ($videoId)",
            channelTitle = "Creator",
            description = "Playing direct video link",
            category = VideoCategory.CREATORS,
            ageGroup = AgeGroup.TEEN,
            durationText = "Video",
            tags = listOf("direct", "link"),
            viewCountText = "Popular"
        )
    }

    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        return when {
            trimmed.contains("v=") -> trimmed.substringAfter("v=").substringBefore("&").take(11)
            trimmed.contains("youtu.be/") -> trimmed.substringAfter("youtu.be/").substringBefore("?").take(11)
            trimmed.contains("embed/") -> trimmed.substringAfter("embed/").substringBefore("?").take(11)
            trimmed.contains("shorts/") -> trimmed.substringAfter("shorts/").substringBefore("?").take(11)
            trimmed.length == 11 && !trimmed.contains(" ") -> trimmed
            else -> null
        }
    }

    private fun deduceCategory(title: String, desc: String): VideoCategory {
        val text = "$title $desc".lowercase()
        return when {
            text.contains("cook") || text.contains("recipe") || text.contains("bake") || text.contains("food") -> VideoCategory.COOKING
            text.contains("music") || text.contains("song") || text.contains("audio") || text.contains("album") -> VideoCategory.MUSIC
            text.contains("game") || text.contains("minecraft") || text.contains("roblox") || text.contains("gameplay") -> VideoCategory.GAMING
            text.contains("science") || text.contains("physics") || text.contains("experiment") || text.contains("space") -> VideoCategory.SCIENCE
            text.contains("sport") || text.contains("football") || text.contains("basketball") || text.contains("trick shot") -> VideoCategory.SPORTS
            else -> VideoCategory.CREATORS
        }
    }

    fun parseIsoDuration(isoDuration: String): String {
        try {
            if (!isoDuration.startsWith("PT")) return "10:00"
            var d = isoDuration.removePrefix("PT")
            var hours = 0
            var minutes = 0
            var seconds = 0
            if (d.contains("H")) {
                hours = d.substringBefore("H").toIntOrNull() ?: 0
                d = d.substringAfter("H")
            }
            if (d.contains("M")) {
                minutes = d.substringBefore("M").toIntOrNull() ?: 0
                d = d.substringAfter("M")
            }
            if (d.contains("S")) {
                seconds = d.substringBefore("S").toIntOrNull() ?: 0
            }
            return if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
            }
        } catch (_: Exception) {
            return "10:00"
        }
    }

    fun formatViewCount(viewCountStr: String): String {
        val count = viewCountStr.toLongOrNull() ?: return "Trending"
        return when {
            count >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.1fB views", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM views", count / 1_000_000.0)
            count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK views", count / 1_000.0)
            else -> "$count views"
        }
    }

    private fun getFallbackTrendingVideos(): List<VideoItem> {
        return listOf(
            VideoItem(
                id = "0e3GPea1Tyg",
                title = "$456,000 Squid Game In Real Life!",
                channelTitle = "MrBeast",
                description = "456 people compete for $456,000 in the biggest real-life games challenge ever created!",
                category = VideoCategory.CREATORS,
                ageGroup = AgeGroup.TEEN,
                durationText = "25:41",
                thumbnailUrl = "https://img.youtube.com/vi/0e3GPea1Tyg/hqdefault.jpg",
                tags = listOf("trending", "mrbeast", "squid game", "challenge"),
                viewCountText = "620M views"
            ),
            VideoItem(
                id = "h0EGCnBjTVk",
                title = "I Outsmarted Pro Car Thieves With Science!",
                channelTitle = "Mark Rober",
                description = "Former NASA engineer Mark Rober creates the ultimate high-tech bait car using science and custom electronics.",
                category = VideoCategory.SCIENCE,
                ageGroup = AgeGroup.TEEN,
                durationText = "20:30",
                thumbnailUrl = "https://img.youtube.com/vi/h0EGCnBjTVk/hqdefault.jpg",
                tags = listOf("trending", "mark rober", "science", "engineering"),
                viewCountText = "48M views"
            ),
            VideoItem(
                id = "wTcNtgA6gHs",
                title = "World Record Model Rocket Battle!",
                channelTitle = "Dude Perfect",
                description = "The guys battle it out with custom high-powered model rockets in epic stunt challenges.",
                category = VideoCategory.SPORTS,
                ageGroup = AgeGroup.TEEN,
                durationText = "14:52",
                thumbnailUrl = "https://img.youtube.com/vi/wTcNtgA6gHs/hqdefault.jpg",
                tags = listOf("trending", "dude perfect", "rockets", "stunts"),
                viewCountText = "32M views"
            ),
            VideoItem(
                id = "mAMgKwT2u1Y",
                title = "The Simplest Math Problem No One Can Solve",
                channelTitle = "Veritasium",
                description = "An exploration into the 3x+1 Collatz Conjecture, the most deceptively simple math question.",
                category = VideoCategory.SCIENCE,
                ageGroup = AgeGroup.TEEN,
                durationText = "22:08",
                thumbnailUrl = "https://img.youtube.com/vi/mAMgKwT2u1Y/hqdefault.jpg",
                tags = listOf("trending", "veritasium", "science", "math"),
                viewCountText = "41M views"
            ),
            VideoItem(
                id = "4uK_epT_i9A",
                title = "I Survived On 1 Penny For 30 Days Across America",
                channelTitle = "Ryan Trahan",
                description = "Starting with a single cent, trading and working up to deliver a penny to MrBeast!",
                category = VideoCategory.CREATORS,
                ageGroup = AgeGroup.TEEN,
                durationText = "28:15",
                thumbnailUrl = "https://img.youtube.com/vi/4uK_epT_i9A/hqdefault.jpg",
                tags = listOf("trending", "ryan trahan", "penny challenge", "travel"),
                viewCountText = "55M views"
            ),
            VideoItem(
                id = "fNlEfnrLqB0",
                title = "5 Easy 3-Ingredient Snacks Anyone Can Make!",
                channelTitle = "Tasty",
                description = "Delicious, quick, and satisfying snacks you can prepare in 5 minutes with just 3 pantry staples.",
                category = VideoCategory.COOKING,
                ageGroup = AgeGroup.EARLY,
                durationText = "10:15",
                thumbnailUrl = "https://img.youtube.com/vi/fNlEfnrLqB0/hqdefault.jpg",
                tags = listOf("trending", "cooking", "recipes", "food"),
                viewCountText = "18M views"
            ),
            VideoItem(
                id = "JyECrGp-Sw8",
                title = "What If We Detonated All Nuclear Bombs at Once?",
                channelTitle = "Kurzgesagt – In a Nutshell",
                description = "A scientific and visual deep-dive examining what happens if every weapon detonated in one spot.",
                category = VideoCategory.SCIENCE,
                ageGroup = AgeGroup.TEEN,
                durationText = "11:04",
                thumbnailUrl = "https://img.youtube.com/vi/JyECrGp-Sw8/hqdefault.jpg",
                tags = listOf("trending", "kurzgesagt", "animation", "science"),
                viewCountText = "37M views"
            ),
            VideoItem(
                id = "bvWRMAU6V-c",
                title = "We Don't Talk About Bruno (From Encanto)",
                channelTitle = "DisneyMusicVEVO",
                description = "The billboard number one viral hit soundtrack sensation from Walt Disney Animation Studios.",
                category = VideoCategory.MUSIC,
                ageGroup = AgeGroup.EARLY,
                durationText = "3:42",
                thumbnailUrl = "https://img.youtube.com/vi/bvWRMAU6V-c/hqdefault.jpg",
                tags = listOf("trending", "music", "disney", "viral"),
                viewCountText = "540M views"
            )
        )
    }

    private fun cleanHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}
