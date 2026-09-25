package com.example.ui.screens.shorts

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.VideoItem
import com.example.util.YouTubeEmbedPlayer
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafePurple
import com.example.util.BatteryState
import com.example.util.ShareHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SafeShortsJsBridge(
    private val onError: (Int) -> Unit,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun notifyShortError(code: Int) {
        // Only trigger for legitimate third-party embed restriction errors
        if (code == 101 || code == 150 || code == 152 || code == 153) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onError(code)
            }
        }
    }

    @JavascriptInterface
    fun notifyShortReady() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onReady()
        }
    }
}

@Composable
fun SafeShortsScreen(
    shorts: List<VideoItem>,
    isShortsAllowedByParent: Boolean = true,
    isBatterySaverActive: Boolean = false,
    batteryState: BatteryState = BatteryState(),
    onVideoClick: (VideoItem) -> Unit,
    onOpenParentControls: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (shorts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = SafeCoral,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "No Shorts Available",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { shorts.size })
    val coroutineScope = rememberCoroutineScope()

    // Track liked and subscribed states for interactive feedback
    val likedMap = remember { mutableStateMapOf<String, Boolean>() }
    val dislikedMap = remember { mutableStateMapOf<String, Boolean>() }
    val subscribedMap = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("safe_shorts_pager")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> shorts[index].id }
        ) { pageIndex ->
            val short = shorts[pageIndex]
            val isActive = pagerState.currentPage == pageIndex
            val isLiked = likedMap[short.id] ?: false
            val isDisliked = dislikedMap[short.id] ?: false
            val isSubscribed = subscribedMap[short.channelTitle] ?: false

            AutoplayShortItemPage(
                short = short,
                isActive = isActive,
                isLiked = isLiked,
                isDisliked = isDisliked,
                isSubscribed = isSubscribed,
                isBatterySaverActive = isBatterySaverActive,
                onToggleLike = {
                    likedMap[short.id] = !isLiked
                    if (isLiked) dislikedMap[short.id] = false
                },
                onToggleDislike = {
                    dislikedMap[short.id] = !isDisliked
                    if (!isDisliked) likedMap[short.id] = false
                },
                onToggleSubscribe = {
                    subscribedMap[short.channelTitle] = !isSubscribed
                },
                onNextShort = {
                    if (pageIndex + 1 < shorts.size) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex + 1)
                        }
                    }
                },
                onOpenFullPlayer = { onVideoClick(short) }
            )
        }

        // Top Header Overlay (Clean, no YouTube branding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SafeCoral),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Shorts",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Shorts",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Battery Saver Mode Badge
                if (isBatterySaverActive) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Saver 240p (${batteryState.batteryPercent}%)",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Safe Ad-Free Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Safe & Ad-Free",
                            tint = SafeGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Safe & Ad-Free",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoplayShortItemPage(
    short: VideoItem,
    isActive: Boolean,
    isLiked: Boolean,
    isDisliked: Boolean,
    isSubscribed: Boolean,
    isBatterySaverActive: Boolean = false,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onNextShort: () -> Unit,
    onOpenFullPlayer: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember(short.id) { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoUnavailable by remember(short.id) { mutableStateOf(false) }
    // Error 153 = YouTube's embed Referer/identity validation failed. Retry once on the
    // privacy-enhanced domain before showing the restricted banner.
    var useFallbackEmbed by remember(short.id) { mutableStateOf(false) }
    var showPlayPauseFeedback by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Manage auto-play when active card enters viewport, and pause when scrolling away
    LaunchedEffect(isActive) {
        if (isActive) {
            isPlaying = true
            webViewRef?.evaluateJavascript("if (typeof playShort === 'function') { playShort(); }", null)
        } else {
            isPlaying = false
            webViewRef?.evaluateJavascript("if (typeof pauseShort === 'function') { pauseShort(); }", null)
        }
    }

    DisposableEffect(isActive) {
        onDispose {
            try {
                webViewRef?.evaluateJavascript("if (typeof pauseShort === 'function') { pauseShort(); }", null)
            } catch (_: Exception) {
                // WebView may already have been released during teardown.
            }
        }
    }

    // Auto-dismiss play/pause feedback icon
    LaunchedEffect(showPlayPauseFeedback) {
        if (showPlayPauseFeedback) {
            delay(800)
            showPlayPauseFeedback = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("short_card_${short.id}")
    ) {
        // High quality static thumbnail as baseline background (prevents black flash)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://img.youtube.com/vi/${short.id}/hqdefault.jpg")
                .crossfade(true)
                .build(),
            contentDescription = short.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isActive) {
            // Live Autoplaying Video Player
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        val cookieManager = android.webkit.CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false // REQUIRED FOR AUTOPLAY WHEN ENTERING VIEWPORT!
                            allowFileAccess = true
                            allowContentAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            javaScriptCanOpenWindowsAutomatically = false
                            setSupportMultipleWindows(false)
                            loadsImagesAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = YouTubeEmbedPlayer.MOBILE_USER_AGENT
                        }
                        addJavascriptInterface(
                            SafeShortsJsBridge(
                                onError = { errorCode ->
                                    if (errorCode == 153 && !useFallbackEmbed) {
                                        // Retry this short once on the fallback embed domain.
                                        useFallbackEmbed = true
                                        isVideoUnavailable = false
                                    } else {
                                        isVideoUnavailable = true
                                    }
                                },
                                onReady = {
                                    isVideoUnavailable = false
                                }
                            ),
                            "SafeTubeShortsBridge"
                        )
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            // YouTube requires an HTTP Referer on the /embed/ player document.
                            // WebView wrapper pages don't always send it — inject it explicitly.
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                // NOTE: shouldInterceptRequest() runs on a WebView background
                                // thread. Calling ANY WebView method here (e.g. view.settings)
                                // throws via WebView.checkThread() and crashes the app — this
                                // was the crash that fired every time a Short opened.
                                val url = request?.url?.toString() ?: return null
                                return try {
                                    YouTubeEmbedPlayer.interceptEmbedRequest(
                                        url,
                                        YouTubeEmbedPlayer.MOBILE_USER_AGENT,
                                        useFallbackEmbed
                                    )
                                } catch (_: Throwable) {
                                    // Never let request interception kill the app.
                                    null
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    // Never show the raw "Webpage not available" system page —
                                    // surface the in-app recovery banner instead.
                                    isVideoUnavailable = true
                                }
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                // A WebView renderer crash must never take down the whole app.
                                return true
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                if (request?.isForMainFrame == false) {
                                    return false // DO NOT BLOCK SUBFRAMES, SCRIPTS, OR MEDIA ASSETS
                                }
                                val url = request?.url?.toString().orEmpty()
                                if (url.contains("youtube.com") || url.contains("googlevideo.com") || 
                                    url.contains("youtube-nocookie.com") || url.contains("ytimg.com") || 
                                    url.contains("gstatic.com") || url.contains("google.com") || 
                                    url.contains("googleapis.com") || url.contains("ggpht.com") || 
                                    url.contains("doubleclick.net")) {
                                    return false
                                }
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    view?.context?.startActivity(intent)
                                } catch (e: Exception) {}
                                return true
                            }
                        }

                        val html = buildShortPlayerHtml(short.id, isBatterySaverActive, useFallbackEmbed)
                        loadDataWithBaseURL(YouTubeEmbedPlayer.host(useFallbackEmbed), html, "text/html", "UTF-8", null)
                        tag = "${short.id}_${isBatterySaverActive}_${useFallbackEmbed}"
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                    val currentTag = "${short.id}_${isBatterySaverActive}_${useFallbackEmbed}"
                    if (webView.tag != currentTag) {
                        webView.tag = currentTag
                        val html = buildShortPlayerHtml(short.id, isBatterySaverActive, useFallbackEmbed)
                        webView.loadDataWithBaseURL(YouTubeEmbedPlayer.host(useFallbackEmbed), html, "text/html", "UTF-8", null)
                    }
                    if (isBatterySaverActive) {
                        webView.evaluateJavascript("if (typeof applyPlaybackQuality === 'function') { applyPlaybackQuality('small'); }", null)
                    }
                },
                onRelease = { webView ->
                    if (webViewRef === webView) {
                        webViewRef = null
                    }
                    try {
                        (webView.parent as? ViewGroup)?.removeView(webView)
                        webView.stopLoading()
                        webView.destroy()
                    } catch (_: Exception) {
                        // Teardown must never crash the app.
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Tap on screen to toggle Play / Pause
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isVideoUnavailable) return@clickable
                    isPlaying = !isPlaying
                    showPlayPauseFeedback = true
                    if (isPlaying) {
                        webViewRef?.evaluateJavascript("if (typeof playShort === 'function') { playShort(); }", null)
                    } else {
                        webViewRef?.evaluateJavascript("if (typeof pauseShort === 'function') { pauseShort(); }", null)
                    }
                }
        )

        // Center Play / Pause Animated Icon Feedback
        AnimatedVisibility(
            visible = showPlayPauseFeedback || (!isPlaying && !isVideoUnavailable),
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Viewport In-Card Recovery Banner if YouTube Creator has restricted embedding (Never shows raw 152-4 error!)
        if (isVideoUnavailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111116).copy(alpha = 0.94f))
                    .padding(start = 24.dp, end = 76.dp, top = 60.dp, bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1E26),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SafeCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Restricted Short",
                                tint = SafeCoral,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Short Restricted by Creator",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "The creator of this video has disabled embedded playback on third-party mobile webviews (Error 150/152).",
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNextShort,
                                colors = ButtonDefaults.buttonColors(containerColor = SafeCoral),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("short_next_button_${short.id}")
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Next Short", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/shorts/${short.id}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("short_watch_youtube_${short.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Watch on YouTube", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Gradient Vignette overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // Right Action Rail (Vertical buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sound Mute/Unmute Toggle
            ShortActionButton(
                icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                label = if (isMuted) "Muted" else "Sound",
                tint = if (isMuted) Color(0xFFEF4444) else Color.White,
                onClick = {
                    isMuted = !isMuted
                    webViewRef?.evaluateJavascript("if (typeof muteShort === 'function') { muteShort($isMuted); }", null)
                },
                testTag = "short_sound_button_${short.id}"
            )

            // Like Button
            ShortActionButton(
                icon = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                label = if (isLiked) "Liked" else "Like",
                tint = if (isLiked) SafeCoral else Color.White,
                onClick = onToggleLike,
                testTag = "short_like_button_${short.id}"
            )

            // Dislike Button
            ShortActionButton(
                icon = if (isDisliked) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                label = "Dislike",
                tint = if (isDisliked) Color(0xFF94A3B8) else Color.White,
                onClick = onToggleDislike,
                testTag = "short_dislike_button_${short.id}"
            )

            // Share Button
            ShortActionButton(
                icon = Icons.Filled.Share,
                label = "Share",
                tint = Color.White,
                onClick = {
                    ShareHelper.shareVideo(context, short.id, short.title)
                },
                testTag = "short_share_button_${short.id}"
            )

            // Full Player Button
            ShortActionButton(
                icon = Icons.Default.OpenInNew,
                label = "Player",
                tint = Color.White,
                onClick = onOpenFullPlayer,
                testTag = "short_open_player_${short.id}"
            )

            // Spinning Sound Disc
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF272727))
                    .rotate(if (isPlaying) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF181818)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Sound",
                        tint = SafeBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Left Info: Channel, Title, Subscribe
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Channel row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SafePurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = short.channelTitle.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Text(
                    text = "@${short.channelTitle.replace(" ", "")}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subscribe Button
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSubscribed) Color.White.copy(alpha = 0.25f) else Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onToggleSubscribe)
                        .testTag("subscribe_button_${short.id}")
                ) {
                    Text(
                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                        color = if (isSubscribed) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Title
            Text(
                text = short.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Audio track ticker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Original Audio • ${short.channelTitle}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShortActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun buildShortPlayerHtml(
    shortId: String,
    isBatterySaverActive: Boolean,
    useFallbackHost: Boolean = false
): String {
    val qualityParam = if (isBatterySaverActive) "vq=small" else ""
    val embedSrc = YouTubeEmbedPlayer.buildEmbedUrl(
        videoId = shortId,
        useFallbackHost = useFallbackHost,
        autoplay = true,
        controls = false,
        enableJsApi = true,
        extraParams = qualityParam
    )
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <meta name="referrer" content="strict-origin-when-cross-origin">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body { background: #000; overflow: hidden; width: 100%; height: 100%; }
                iframe { width: 100%; height: 100%; border: 0; display: block; object-fit: cover; }
                .ytp-youtube-button, .ytp-watermark, .ytp-impression-link, 
                .ytp-title-link, .ytp-ce-element, .ytp-pause-overlay, 
                .ytp-share-button, .ytp-watch-later-button,
                a[href*="youtube.com"], a[href*="youtu.be"] {
                    display: none !important;
                    opacity: 0 !important;
                    visibility: hidden !important;
                    pointer-events: none !important;
                }
            </style>
            <script>
                function sendCmd(func, args) {
                    try {
                        var iframe = document.querySelector('iframe');
                        if (iframe && iframe.contentWindow) {
                            iframe.contentWindow.postMessage(JSON.stringify({
                                event: 'command',
                                func: func,
                                args: args || []
                            }), '*');
                        }
                    } catch(e) {}
                }
                function playShort() { sendCmd('playVideo'); }
                function pauseShort() { sendCmd('pauseVideo'); }
                function muteShort(muted) { sendCmd(muted ? 'mute' : 'unMute'); }
                function applyPlaybackQuality(quality) {
                    sendCmd('setPlaybackQuality', [quality]);
                    sendCmd('setPlaybackQualityRange', [quality, quality]);
                }

                window.addEventListener('message', function(event) {
                    try {
                        var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                        if (data) {
                            if (data.event === 'onError') {
                                var errCode = (typeof data.info === 'number') ? data.info : 150;
                                if (errCode === 101 || errCode === 150 || errCode === 152 || errCode === 153) {
                                    if (window.SafeTubeShortsBridge) {
                                        window.SafeTubeShortsBridge.notifyShortError(errCode);
                                    }
                                }
                            }
                            // Cleanly loop short when ended
                            if (data.event === 'onStateChange' && data.info === 0) {
                                sendCmd('seekTo', [0, true]);
                                sendCmd('playVideo');
                            }
                            if (data.event === 'onReady' || data.info === 1) {
                                if (window.SafeTubeShortsBridge) {
                                    window.SafeTubeShortsBridge.notifyShortReady();
                                }
                                applyPlaybackQuality('${if (isBatterySaverActive) "small" else "default"}');
                                playShort();
                            }
                        }
                    } catch(e) {}
                });

                setInterval(function() {
                    try {
                        var iframe = document.querySelector('iframe');
                        if (iframe && iframe.contentWindow) {
                            iframe.contentWindow.postMessage(JSON.stringify({
                                event: 'listening'
                            }), '*');
                        }
                    } catch(e) {}
                }, 500);

                window.addEventListener('load', function() {
                    setTimeout(playShort, 300);
                    setTimeout(playShort, 900);
                    setTimeout(playShort, 1800);
                });
            </script>
        </head>
        <body>
            <iframe 
                id="player"
                width="360"
                height="640"
                src="$embedSrc" 
                title="YouTube video player"
                frameborder="0"
                referrerpolicy="strict-origin-when-cross-origin"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen" 
                allowfullscreen>
            </iframe>
        </body>
        </html>
    """.trimIndent()
}

