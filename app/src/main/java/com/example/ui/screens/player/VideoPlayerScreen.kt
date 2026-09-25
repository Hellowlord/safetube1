package com.example.ui.screens.player

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import java.io.ByteArrayInputStream
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.service.tracking.TrackingAction
import com.example.service.tracking.TrackingConfig
import com.example.service.tracking.TrackingGesture
import com.example.service.tracking.TrackingState
import com.example.ui.components.EyeHeadTrackingDialog
import com.example.ui.components.EyeTrackingOverlay
import com.example.ui.components.ThemeToggleIconButton
import com.example.ui.theme.ThemeMode
import com.example.util.BatteryState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.models.VideoItem
import com.example.util.YouTubeEmbedPlayer
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeOrange
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.util.ShareHelper

private val YouTubeRed = Color(0xFFFF0000)

fun parseDurationTextToSeconds(durationText: String): Float {
    if (durationText.isBlank()) return 300f
    val parts = durationText.trim().split(":")
    return try {
        when (parts.size) {
            1 -> parts[0].toFloatOrNull() ?: 300f
            2 -> (parts[0].toLong() * 60 + parts[1].toLong()).toFloat()
            3 -> (parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()).toFloat()
            else -> 300f
        }
    } catch (e: Exception) {
        300f
    }
}

fun formatPlaybackTime(seconds: Float): String {
    val totalSec = seconds.toLong().coerceAtLeast(0)
    val hrs = totalSec / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60
    return if (hrs > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }
}

class SafeTubeJsBridge(
    private val onUnavailable: (Int) -> Unit,
    private val onProgress: (Float, Float) -> Unit = { _, _ -> },
    private val onPlaybackState: (Int) -> Unit = {}
) {
    @android.webkit.JavascriptInterface
    fun notifyVideoError(code: Int) {
        // Only trigger on genuine embed restriction error codes (101, 150, 152, 153)
        if (code == 101 || code == 150 || code == 152 || code == 153) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onUnavailable(code)
            }
        }
    }

    @android.webkit.JavascriptInterface
    fun updateProgress(currentSec: Float, durationSec: Float) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onProgress(currentSec, durationSec)
        }
    }

    @android.webkit.JavascriptInterface
    fun onPlaybackStateChange(state: Int) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onPlaybackState(state)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerScreen(
    video: VideoItem,
    isOfflineMode: Boolean,
    isSavedOffline: Boolean,
    isWatchLater: Boolean = false,
    isKidLock: Boolean,
    remainingMinutes: Int,
    recommendedVideos: List<VideoItem>,
    playbackSpeed: Float = 1.0f,
    onSetPlaybackSpeed: (Float) -> Unit = {},
    selectedResolution: String = "auto",
    onSetResolution: (String) -> Unit = {},
    sleepTimerRemainingSeconds: Int? = null,
    sleepTimerTotalMinutes: Int? = null,
    isSleepTimerTriggered: Boolean = false,
    onSetSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onDismissSleepTimerTriggered: () -> Unit = {},
    onBackClick: () -> Unit,
    onToggleKidLock: () -> Unit,
    onToggleOffline: (VideoItem) -> Unit,
    onToggleWatchLater: (VideoItem) -> Unit = {},
    onSelectRecommended: (VideoItem) -> Unit,
    onRemoveUnavailable: ((String) -> Unit)? = null,
    onEnterPip: (() -> Unit)? = null,
    onSystemPip: (() -> Unit)? = null,
    trackingConfig: TrackingConfig = TrackingConfig(),
    trackingState: TrackingState = TrackingState(),
    isTrackingDialogOpen: Boolean = false,
    onSetTrackingDialogOpen: (Boolean) -> Unit = {},
    onUpdateTrackingConfig: ((TrackingConfig) -> TrackingConfig) -> Unit = {},
    onSimulateTrackingGesture: (TrackingGesture) -> Unit = {},
    onRequestCameraPermission: () -> Unit = {},
    trackingActionEvents: kotlinx.coroutines.flow.SharedFlow<TrackingAction>? = null,
    onStartCameraTracking: (androidx.lifecycle.LifecycleOwner) -> Unit = {},
    onStopCameraTracking: () -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isBatterySaverActive: Boolean = false,
    batteryState: BatteryState = BatteryState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLiked by remember(video.id) { mutableStateOf(false) }
    var isVideoReportedUnavailable by remember(video.id) { mutableStateOf(false) }
    var isIframeGrantedMode by remember(video.id) { mutableStateOf(true) }
    // YouTube rejects embeds whose Referer identity fails validation with error 153.
    // Retry once on the privacy-enhanced domain before showing the restricted overlay.
    var useFallbackEmbed by remember(video.id) { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showQualityMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var customSleepMinutesText by remember { mutableStateOf("") }
    val speedOptions = remember { listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f) }
    
    data class VideoResolution(
        val id: String,
        val label: String,
        val subtitle: String,
        val badge: String? = null
    )
    val resolutionOptions = remember {
        listOf(
            VideoResolution("auto", "Auto", "Optimal quality based on network", "Recommended"),
            VideoResolution("hd1080", "1080p Full HD", "Best clarity • High data (~30 MB/min)", "FHD"),
            VideoResolution("hd720", "720p HD", "High quality • Balanced (~15 MB/min)", "HD"),
            VideoResolution("large", "480p SD", "Data Saver • Standard (~10 MB/min)", "Data Saver"),
            VideoResolution("medium", "360p Low", "Minimal data usage (~5 MB/min)", null),
            VideoResolution("small", "240p Data Saver", "Extreme data saver (~2 MB/min)", null)
        )
    }
    var selectedQuality by remember(video.id, selectedResolution) { mutableStateOf(selectedResolution) }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    val defaultDurationSeconds = remember(video.id, video.durationText) {
        parseDurationTextToSeconds(video.durationText)
    }
    var currentPlaybackSeconds by remember(video.id) { mutableStateOf(0f) }
    var totalDurationSeconds by remember(video.id, defaultDurationSeconds) { mutableStateOf(defaultDurationSeconds) }
    var isScrubbing by remember(video.id) { mutableStateOf(false) }
    var scrubPreviewSeconds by remember(video.id) { mutableStateOf(0f) }
    var isPlayingState by remember(video.id) { mutableStateOf(true) }

    fun seekToSeconds(seconds: Float) {
        val target = seconds.coerceIn(0f, totalDurationSeconds.coerceAtLeast(1f))
        currentPlaybackSeconds = target
        if (isOfflineMode) {
            mediaPlayerRef?.seekTo((target * 1000).toInt())
        } else {
            val script = """
                (function() {
                    try {
                        if (window.ytPlayer && typeof window.ytPlayer.seekTo === 'function') {
                            window.ytPlayer.seekTo($target, true);
                        }
                        var iframe = document.querySelector('iframe');
                        if (iframe && iframe.contentWindow) {
                            iframe.contentWindow.postMessage(JSON.stringify({
                                event: 'command',
                                func: 'seekTo',
                                args: [$target, true]
                            }), '*');
                        }
                        var vids = document.querySelectorAll('video');
                        for (var i = 0; i < vids.length; i++) {
                            vids[i].currentTime = $target;
                        }
                    } catch(e) {}
                })();
            """.trimIndent()
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    fun togglePlayPauseVideo() {
        if (isOfflineMode) {
            mediaPlayerRef?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    isPlayingState = false
                } else {
                    mp.start()
                    isPlayingState = true
                }
            }
        } else {
            val willPlay = !isPlayingState
            isPlayingState = willPlay
            val script = if (willPlay) {
                """
                (function() {
                    try {
                        if (window.ytPlayer && typeof window.ytPlayer.playVideo === 'function') {
                            window.ytPlayer.playVideo();
                        }
                        var iframe = document.querySelector('iframe');
                        if (iframe && iframe.contentWindow) {
                            iframe.contentWindow.postMessage(JSON.stringify({
                                event: 'command',
                                func: 'playVideo',
                                args: []
                            }), '*');
                        }
                        var vids = document.querySelectorAll('video');
                        for (var i = 0; i < vids.length; i++) {
                            vids[i].play();
                        }
                    } catch(e) {}
                })();
                """.trimIndent()
            } else {
                """
                (function() {
                    try {
                        if (window.ytPlayer && typeof window.ytPlayer.pauseVideo === 'function') {
                            window.ytPlayer.pauseVideo();
                        }
                        var iframe = document.querySelector('iframe');
                        if (iframe && iframe.contentWindow) {
                            iframe.contentWindow.postMessage(JSON.stringify({
                                event: 'command',
                                func: 'pauseVideo',
                                args: []
                            }), '*');
                        }
                        var vids = document.querySelectorAll('video');
                        for (var i = 0; i < vids.length; i++) {
                            vids[i].pause();
                        }
                    } catch(e) {}
                })();
                """.trimIndent()
            }
            webViewRef?.evaluateJavascript(script, null)
        }
    }

    LaunchedEffect(isOfflineMode, mediaPlayerRef) {
        if (isOfflineMode) {
            while (true) {
                mediaPlayerRef?.let { mp ->
                    try {
                        if (mp.isPlaying && !isScrubbing) {
                            currentPlaybackSeconds = mp.currentPosition / 1000f
                            val dur = mp.duration / 1000f
                            if (dur > 0f) {
                                totalDurationSeconds = dur
                            }
                            isPlayingState = true
                        }
                    } catch (e: Exception) {}
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun applyQuality(qualityId: String) {
        selectedQuality = qualityId
        onSetResolution(qualityId)
        val targetQuality = if (isBatterySaverActive) "small" else if (qualityId == "auto") "default" else qualityId
        val script = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'setPlaybackQuality',
                            args: ['$targetQuality']
                        }), '*');
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'setPlaybackQualityRange',
                            args: ['$targetQuality', '$targetQuality']
                        }), '*');
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(script, null)
    }

    LaunchedEffect(isBatterySaverActive) {
        if (isBatterySaverActive) {
            applyQuality("small")
        }
    }

    fun formatSpeedLabel(speed: Float): String = if (speed == 1.0f) "1.0x" else "${speed}x"
    fun formatSpeedName(speed: Float): String = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x"

    fun formatSleepTimerRemaining(totalSecs: Int?): String {
        if (totalSecs == null) return ""
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return if (mins > 0) {
            "${mins}m ${String.format("%02d", secs)}s"
        } else {
            "${secs}s"
        }
    }

    var isPlaying by remember(video.id) { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bind front camera tracking when enabled
    LaunchedEffect(trackingConfig.isTrackingEnabled, lifecycleOwner) {
        if (trackingConfig.isTrackingEnabled) {
            onStartCameraTracking(lifecycleOwner)
        } else {
            onStopCameraTracking()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onStopCameraTracking()
        }
    }

    fun pausePlayback() {
        isPlaying = false
        val pauseScript = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'pauseVideo',
                            args: []
                        }), '*');
                    }
                    var vids = document.querySelectorAll('video');
                    for (var i = 0; i < vids.length; i++) {
                        vids[i].pause();
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(pauseScript, null)
        try {
            mediaPlayerRef?.pause()
        } catch (e: Exception) {}
    }

    fun resumePlayback() {
        isPlaying = true
        val playScript = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'playVideo',
                            args: []
                        }), '*');
                    }
                    var vids = document.querySelectorAll('video');
                    for (var i = 0; i < vids.length; i++) {
                        vids[i].play();
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(playScript, null)
        try {
            mediaPlayerRef?.start()
        } catch (e: Exception) {}
    }

    fun togglePlayPause() {
        if (isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    fun seekBy(deltaSeconds: Int) {
        val script = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'seekBy',
                            args: [$deltaSeconds]
                        }), '*');
                    }
                    var vids = document.querySelectorAll('video');
                    for (var i = 0; i < vids.length; i++) {
                        vids[i].currentTime += $deltaSeconds;
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(script, null)
        try {
            val current = mediaPlayerRef?.currentPosition ?: 0
            val target = (current + deltaSeconds * 1000).coerceAtLeast(0)
            mediaPlayerRef?.seekTo(target)
        } catch (e: Exception) {}
    }

    // Handle hands-free tracking actions (Attention Pause/Resume, Blinks, Nods, Tilts)
    LaunchedEffect(trackingActionEvents) {
        trackingActionEvents?.collect { action ->
            when (action) {
                TrackingAction.TOGGLE_PLAY_PAUSE -> {
                    togglePlayPause()
                }
                TrackingAction.PAUSE_ATTENTION -> {
                    pausePlayback()
                }
                TrackingAction.RESUME_ATTENTION -> {
                    resumePlayback()
                }
                TrackingAction.SEEK_FORWARD_10S -> {
                    seekBy(10)
                }
                TrackingAction.SEEK_BACKWARD_10S -> {
                    seekBy(-10)
                }
                TrackingAction.DISTANCE_WARNING, TrackingAction.NONE -> {}
            }
        }
    }

    // Automatically pause playback when the sleep timer triggers
    LaunchedEffect(isSleepTimerTriggered) {
        if (isSleepTimerTriggered) {
            pausePlayback()
        }
    }

    // Dynamically apply playback speed to WebView YouTube iframe and HTML5 video
    LaunchedEffect(playbackSpeed, webViewRef) {
        val script = """
            (function() {
                try {
                    var iframe = document.querySelector('iframe');
                    if (iframe && iframe.contentWindow) {
                        iframe.contentWindow.postMessage(JSON.stringify({
                            event: 'command',
                            func: 'setPlaybackRate',
                            args: [$playbackSpeed]
                        }), '*');
                    }
                    var vids = document.querySelectorAll('video');
                    for (var i = 0; i < vids.length; i++) {
                        vids[i].playbackRate = $playbackSpeed;
                    }
                } catch(e) {}
            })();
        """.trimIndent()
        webViewRef?.evaluateJavascript(script, null)
    }

    // Dynamically apply playback speed to offline MediaPlayer
    LaunchedEffect(playbackSpeed, mediaPlayerRef) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayerRef?.let { mp ->
                try {
                    val params = mp.playbackParams
                    params.speed = playbackSpeed
                    mp.playbackParams = params
                } catch (e: Exception) {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("video_player_screen")
    ) {
        // Player Top Navigation Bar (Clean & open, no parental gate or kid locks)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // SafePlay Video Player badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeBlue)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "SafePlay",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Playing",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Quick action buttons (Tracking, Playback Speed, Sleep Timer, Bookmark, PiP, Save, Share, Remove)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Eye & Head Tracking Button
                    Surface(
                        onClick = { onSetTrackingDialogOpen(true) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (trackingConfig.isTrackingEnabled) {
                            if (trackingState.isFaceDetected) SafeBlue.copy(alpha = 0.18f) else SafeOrange.copy(alpha = 0.18f)
                        } else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("player_top_eye_tracking_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (trackingConfig.isTrackingEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Eye & Head Tracking",
                                tint = if (trackingConfig.isTrackingEnabled) {
                                    if (trackingState.isFaceDetected) SafeBlue else SafeOrange
                                } else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (trackingConfig.isTrackingEnabled) {
                                    if (trackingState.isFaceDetected) "Tracking" else "Searching"
                                } else "Tracking",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (trackingConfig.isTrackingEnabled) {
                                    if (trackingState.isFaceDetected) SafeBlue else SafeOrange
                                } else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Quick Playback Speed Indicator & Opener
                    Surface(
                        onClick = { showSpeedMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (playbackSpeed != 1.0f) SafeBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("player_top_speed_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed: ${formatSpeedLabel(playbackSpeed)}",
                                tint = if (playbackSpeed != 1.0f) SafeBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatSpeedLabel(playbackSpeed),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (playbackSpeed != 1.0f) SafeBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Sleep Timer Quick Button in Top Bar
                    Surface(
                        onClick = { showSleepTimerDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (sleepTimerRemainingSeconds != null) SafeOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("player_top_sleep_timer_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimerRemainingSeconds != null) SafeOrange else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            if (sleepTimerRemainingSeconds != null) {
                                Text(
                                    text = formatSleepTimerRemaining(sleepTimerRemainingSeconds),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeOrange
                                )
                            }
                        }
                    }

                    // Quick Watch Later Bookmark Button
                    IconButton(
                        onClick = { onToggleWatchLater(video) },
                        modifier = Modifier.testTag("player_top_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isWatchLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isWatchLater) "Remove from Watch Later" else "Save to Watch Later",
                            tint = if (isWatchLater) SafeOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Picture-in-Picture Button
                    if (onEnterPip != null) {
                        IconButton(
                            onClick = onEnterPip,
                            modifier = Modifier.testTag("player_pip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture-in-Picture Mode",
                                tint = SafeBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Remove if unavailable button
                    IconButton(
                        onClick = {
                            onRemoveUnavailable?.invoke(video.id)
                            onBackClick()
                        },
                        modifier = Modifier.testTag("remove_unavailable_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Video",
                            tint = SafeCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            ShareHelper.shareVideo(context, video.id, video.title)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // IFrame Player Mode Toggle (Granted for unavailable or restricted videos)
                    IconButton(
                        onClick = {
                            isIframeGrantedMode = !isIframeGrantedMode
                            isVideoReportedUnavailable = false
                        },
                        modifier = Modifier.testTag("iframe_player_toggle")
                    ) {
                        Icon(
                            imageVector = if (isIframeGrantedMode) Icons.Default.Shield else Icons.Default.Code,
                            contentDescription = "IFrame Mode (Granted)",
                            tint = if (isIframeGrantedMode) SafeGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Theme Toggle Button
                    ThemeToggleIconButton(
                        currentMode = themeMode,
                        onToggle = onToggleTheme
                    )

                    // Player Settings Dialog Button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("player_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Player Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Clean sanitized metadata values
        val displayTitle = remember(video.title) {
            video.title
                .replace("YouTube Video", "Video", ignoreCase = true)
        }

        val displayChannel = remember(video.channelTitle) {
            if (video.channelTitle.equals("YouTube", ignoreCase = true)) {
                "SafeTube Creator"
            } else {
                video.channelTitle
            }
        }

        val displayViews = remember(video.viewCountText) {
            if (video.viewCountText.isBlank()) {
                "Featured Video"
            } else {
                video.viewCountText
            }
        }

        val displayDescription = remember(video.description) {
            if (video.description.isBlank()) {
                "Enjoy watching this video in high definition on SafeTube."
            } else {
                video.description
                    .replace(Regex("https?:\\/\\/(www\\.)?(youtube\\.com|youtu\\.be)[^\\s]*", RegexOption.IGNORE_CASE), "")
                    .trim()
            }
        }

        // Player HTML template with comprehensive ad-blocking CSS, script stripping and source-hiding
        val effectiveQuality = if (isBatterySaverActive) "small" else selectedQuality
        val qualityParam = if (effectiveQuality == "auto") "" else "vq=$effectiveQuality"

        // Unified reload tag so the WebView only reloads when something actually changed
        // (video, granted mode, quality, battery saver, or embed domain retry).
        fun playerTag(): String =
            "${video.id}_${isIframeGrantedMode}_${selectedQuality}_${isBatterySaverActive}_${useFallbackEmbed}"

        val playerHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <meta name="referrer" content="strict-origin-when-cross-origin">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { background: #000; overflow: hidden; width: 100%; height: 100%; }
                    iframe { width: 100%; height: 100%; border: 0; display: block; }
                    /* Comprehensive Ad-Block & Source Hiding Rules */
                    .ytp-youtube-button, .ytp-watermark, .ytp-impression-link, 
                    .ytp-title-link, .ytp-ce-element, .ytp-pause-overlay, 
                    .ytp-share-button, .ytp-watch-later-button, .ytp-show-cards-title,
                    .video-ads, .ytp-ad-module, .ytp-ad-overlay-container,
                    .ytp-ad-text, .ytp-ad-skip-button, .ytp-ad-preview-container,
                    .ytp-ad-image, .ytp-ad-player-overlay,
                    a[href*="youtube.com"], a[href*="youtu.be"], a[href*="doubleclick.net"] {
                        display: none !important;
                        opacity: 0 !important;
                        visibility: hidden !important;
                        pointer-events: none !important;
                        width: 0 !important;
                        height: 0 !important;
                    }
                </style>
                <script>
                    function applyPlaybackSpeed(rate) {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'setPlaybackRate',
                                    args: [rate]
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].playbackRate = rate;
                            }
                        } catch(e) {}
                    }

                    function applyPlaybackQuality(quality) {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'setPlaybackQuality',
                                    args: [quality]
                                }), '*');
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'setPlaybackQualityRange',
                                    args: [quality, quality]
                                }), '*');
                            }
                        } catch(e) {}
                    }

                    function seekToSeconds(sec) {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'seekTo',
                                    args: [sec, true]
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].currentTime = sec;
                            }
                        } catch(e) {}
                    }

                    function playVideo() {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'playVideo',
                                    args: []
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].play();
                            }
                        } catch(e) {}
                    }

                    function pauseVideo() {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'pauseVideo',
                                    args: []
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].pause();
                            }
                        } catch(e) {}
                    }

                    window.addEventListener('message', function(event) {
                        try {
                            var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                            if (data) {
                                if (data.event === 'onError') {
                                    var errCode = (typeof data.info === 'number') ? data.info : 150;
                                    if (errCode === 101 || errCode === 150 || errCode === 152 || errCode === 153) {
                                        if (window.SafeTubeAndroidBridge) {
                                            window.SafeTubeAndroidBridge.notifyVideoError(errCode);
                                        }
                                    }
                                }
                                if (data.event === 'infoDelivery' && data.info) {
                                    var cur = data.info.currentTime;
                                    var dur = data.info.duration;
                                    if (cur !== undefined || dur !== undefined) {
                                        var c = cur !== undefined ? cur : -1;
                                        var d = dur !== undefined ? dur : -1;
                                        if (window.SafeTubeAndroidBridge) {
                                            window.SafeTubeAndroidBridge.updateProgress(c, d);
                                        }
                                    }
                                }
                                if (data.event === 'onStateChange' && data.info !== undefined) {
                                    if (window.SafeTubeAndroidBridge) {
                                        window.SafeTubeAndroidBridge.onPlaybackStateChange(data.info);
                                    }
                                }
                                if (data.event === 'onReady' || data.info === 1) {
                                    applyPlaybackSpeed(${playbackSpeed});
                                    applyPlaybackQuality('${if (effectiveQuality == "auto") "default" else effectiveQuality}');
                                }
                            }
                        } catch(e) {}
                    });

                    // Auto-skip or fast-forward any embedded ads if detected
                    setInterval(function() {
                        var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button');
                        if (skipBtn) { skipBtn.click(); }
                        var adOverlays = document.querySelectorAll('.ytp-ad-overlay-close-button');
                        adOverlays.forEach(function(btn) { btn.click(); });
                    }, 500);

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
                        setTimeout(function() { 
                            applyPlaybackSpeed(${playbackSpeed});
                            applyPlaybackQuality('${if (effectiveQuality == "auto") "default" else effectiveQuality}');
                        }, 1000);
                        setTimeout(function() { 
                            applyPlaybackSpeed(${playbackSpeed}); 
                        }, 2200);
                    });
                </script>
            </head>
            <body>
                <iframe 
                    id="player"
                    src="${YouTubeEmbedPlayer.buildEmbedUrl(video.id, useFallbackEmbed, extraParams = qualityParam)}" 
                    referrerpolicy="strict-origin-when-cross-origin"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                    allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()

        // Granted IFrame player template for unavailable or restricted videos
        val grantedIframeHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <meta name="referrer" content="strict-origin-when-cross-origin">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { background: #000; overflow: hidden; width: 100%; height: 100%; }
                    iframe { width: 100%; height: 100%; border: 0; display: block; }
                    .ytp-youtube-button, .ytp-watermark, .ytp-impression-link, 
                    .ytp-title-link, .ytp-ce-element, .ytp-pause-overlay, 
                    .ytp-share-button, .ytp-watch-later-button,
                    .video-ads, .ytp-ad-module {
                        display: none !important;
                    }
                </style>
                <script>
                    function applyPlaybackSpeed(rate) {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'setPlaybackRate',
                                    args: [rate]
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].playbackRate = rate;
                            }
                        } catch(e) {}
                    }
                    function seekToSeconds(sec) {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'seekTo',
                                    args: [sec, true]
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].currentTime = sec;
                            }
                        } catch(e) {}
                    }
                    function playVideo() {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'playVideo',
                                    args: []
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].play();
                            }
                        } catch(e) {}
                    }
                    function pauseVideo() {
                        try {
                            var iframe = document.querySelector('iframe');
                            if (iframe && iframe.contentWindow) {
                                iframe.contentWindow.postMessage(JSON.stringify({
                                    event: 'command',
                                    func: 'pauseVideo',
                                    args: []
                                }), '*');
                            }
                            var vids = document.querySelectorAll('video');
                            for (var i = 0; i < vids.length; i++) {
                                vids[i].pause();
                            }
                        } catch(e) {}
                    }
                    window.addEventListener('message', function(event) {
                        try {
                            var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                            if (data) {
                                if (data.event === 'infoDelivery' && data.info) {
                                    var cur = data.info.currentTime;
                                    var dur = data.info.duration;
                                    if (cur !== undefined || dur !== undefined) {
                                        var c = cur !== undefined ? cur : -1;
                                        var d = dur !== undefined ? dur : -1;
                                        if (window.SafeTubeAndroidBridge) {
                                            window.SafeTubeAndroidBridge.updateProgress(c, d);
                                        }
                                    }
                                }
                                if (data.event === 'onStateChange' && data.info !== undefined) {
                                    if (window.SafeTubeAndroidBridge) {
                                        window.SafeTubeAndroidBridge.onPlaybackStateChange(data.info);
                                    }
                                }
                                if (data.event === 'onReady' || data.info === 1) {
                                    applyPlaybackSpeed(${playbackSpeed});
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
                        setTimeout(function() { applyPlaybackSpeed(${playbackSpeed}); }, 1000);
                    });
                </script>
            </head>
            <body>
                <iframe 
                    id="player"
                    src="${YouTubeEmbedPlayer.buildEmbedUrl(video.id, useFallbackEmbed, extraParams = qualityParam)}" 
                    referrerpolicy="strict-origin-when-cross-origin"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen" 
                    allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()

        val activePlayerHtml = if (isIframeGrantedMode) grantedIframeHtml else playerHtml
        val activeBaseUrl = YouTubeEmbedPlayer.host(useFallbackEmbed)

        // VIDEO VIEWPORT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isOfflineMode) {
                // Real local MP4 video player for offline playback!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("offline_player_viewport"),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                val mediaController = android.widget.MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)

                                val localPath = video.localFilePath
                                val uri = if (localPath != null && java.io.File(localPath).exists()) {
                                    Uri.fromFile(java.io.File(localPath))
                                } else {
                                    Uri.parse("android.resource://" + ctx.packageName + "/" + com.example.R.raw.safetube_offline_sample)
                                }
                                setVideoURI(uri)
                                setOnPreparedListener { mp ->
                                    mediaPlayerRef = mp
                                    mp.isLooping = true
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            val params = mp.playbackParams
                                            params.speed = playbackSpeed
                                            mp.playbackParams = params
                                        } catch (e: Exception) {}
                                    }
                                    start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Offline Mode Badge Overlay
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Playback",
                                tint = SafeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Offline Playback",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Full High Definition Player Embed
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            tag = playerTag()
                            webViewRef = this
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
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                javaScriptCanOpenWindowsAutomatically = false
                                setSupportMultipleWindows(false)
                                loadsImagesAutomatically = true
                                userAgentString = YouTubeEmbedPlayer.MOBILE_USER_AGENT
                            }
                            addJavascriptInterface(
                                SafeTubeJsBridge(
                                    onUnavailable = { errorCode ->
                                        if (errorCode == 153 && !useFallbackEmbed) {
                                            // Error 153 = YouTube embed Referer/identity check failed.
                                            // Retry once on the fallback domain before giving up.
                                            useFallbackEmbed = true
                                            isVideoReportedUnavailable = false
                                        } else {
                                            isVideoReportedUnavailable = true
                                        }
                                    },
                                    onProgress = { cur, dur ->
                                        if (!isScrubbing && cur >= 0f) {
                                            currentPlaybackSeconds = cur
                                        }
                                        if (dur > 0f) {
                                            totalDurationSeconds = dur
                                        }
                                    },
                                    onPlaybackState = { state ->
                                        if (state == 1) isPlayingState = true
                                        else if (state == 2) isPlayingState = false
                                    }
                                ),
                                "SafeTubeAndroidBridge"
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
                                    // was the crash that fired every time a video or Short opened.
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

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    if (request?.isForMainFrame == false) {
                                        return false
                                    }
                                    val url = request?.url?.toString().orEmpty()
                                    if (url.contains("youtube.com") || url.contains("googlevideo.com") || 
                                        url.contains("youtube-nocookie.com") || url.contains("ytimg.com") || 
                                        url.contains("gstatic.com") || url.contains("google.com") || 
                                        url.contains("googleapis.com") || url.contains("ggpht.com") ||
                                        url.contains("doubleclick.net")) {
                                        return false
                                    }
                                    if (request?.isForMainFrame == true) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            view?.context?.startActivity(intent)
                                        } catch (e: Exception) {}
                                        return true
                                    }
                                    return false
                                }
                            }

                            loadDataWithBaseURL(activeBaseUrl, activePlayerHtml, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                        if (webView.tag != playerTag()) {
                            webView.tag = playerTag()
                            webView.loadDataWithBaseURL(activeBaseUrl, activePlayerHtml, "text/html", "UTF-8", null)
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

            // Viewport Recovery Overlay for Restricted or Unavailable Videos (Never show raw YouTube 152-4 error!)
            if (isVideoReportedUnavailable && !isOfflineMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141419).copy(alpha = 0.98f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Video Unavailable",
                            tint = SafeCoral,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Video Restricted by YouTube Creator",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Playback in third-party mobile apps has been disabled by the video owner (Error 152/150). You can play it directly in the YouTube app or try Granted IFrame mode.",
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.id}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafeCoral),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Watch on YouTube", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (!isIframeGrantedMode) {
                                OutlinedButton(
                                    onClick = {
                                        isIframeGrantedMode = true
                                        isVideoReportedUnavailable = false
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Try Granted IFrame", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Granted IFrame Mode Badge Indicator
            if (isIframeGrantedMode && !isOfflineMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .testTag("iframe_granted_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "IFrame Mode Active",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Granted IFrame Player",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Eye & Head Tracking Overlay (Hands-Free HUD, Pointer Cursor, Attention & Gesture Alerts)
            EyeTrackingOverlay(
                state = trackingState,
                config = trackingConfig,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Interactive Scrub Seek Bar Component
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("video_player_seek_bar_container"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Time & Scrubbing Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayTime = if (isScrubbing) scrubPreviewSeconds else currentPlaybackSeconds
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatPlaybackTime(displayTime),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isScrubbing) SafeCoral else MaterialTheme.colorScheme.onSurface
                        )
                        if (isScrubbing) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeCoral.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Scrubbing",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SafeCoral,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = formatPlaybackTime(totalDurationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Scrub Slider
                Slider(
                    value = (if (isScrubbing) scrubPreviewSeconds else currentPlaybackSeconds).coerceIn(0f, totalDurationSeconds.coerceAtLeast(1f)),
                    onValueChange = { newSec ->
                        isScrubbing = true
                        scrubPreviewSeconds = newSec
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        seekToSeconds(scrubPreviewSeconds)
                    },
                    valueRange = 0f..totalDurationSeconds.coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = SafeCoral,
                        activeTrackColor = SafeCoral,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .testTag("video_seek_bar_slider")
                )

                // Quick Navigation Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s Button
                    TextButton(
                        onClick = {
                            seekToSeconds((currentPlaybackSeconds - 10f).coerceAtLeast(0f))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("video_seek_rewind_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10s",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "-10s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Play / Pause Toggle Button
                    Surface(
                        shape = CircleShape,
                        color = SafeCoral.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { togglePlayPauseVideo() }
                            .testTag("video_seek_play_pause_button"),
                        contentColor = SafeCoral
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlayingState) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingState) "Pause Video" else "Play Video",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Forward 10s Button
                    TextButton(
                        onClick = {
                            seekToSeconds((currentPlaybackSeconds + 10f).coerceAtMost(totalDurationSeconds))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("video_seek_forward_10")
                    ) {
                        Text(
                            text = "+10s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10s",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Video Info & Details Scrollable Area
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                if (isBatterySaverActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("battery_saver_video_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = "Battery Saver Active",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Battery Saver Mode Active (${batteryState.percentage}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Playback resolution reduced to 240p to extend battery life.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Channel Info & Views
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayChannel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SafeBlue
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = displayViews,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "${video.category.emoji} ${video.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Explicit "Powered by AdBlock" badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SafeGreen.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "AdBlock Active",
                            tint = SafeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Powered by AdBlock • Zero Ads & Intermissions Guaranteed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeGreen
                        )
                    }
                }

                if (isVideoReportedUnavailable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unavailable_recovery_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Video Unavailable",
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Video Embed Restricted • IFrame Access Granted",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = YouTubeRed
                                )
                            }
                            Text(
                                text = "The creator of this video has restricted third-party embedded playback, but parent permission has granted IFrame bypass. Tap 'Play with Granted IFrame' to stream smoothly.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Button(
                                    onClick = {
                                        isIframeGrantedMode = true
                                        isVideoReportedUnavailable = false
                                        webViewRef?.tag = playerTag()
                                        webViewRef?.loadDataWithBaseURL(activeBaseUrl, grantedIframeHtml, "text/html", "UTF-8", null)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("play_iframe_granted_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play with Granted IFrame", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        isVideoReportedUnavailable = false
                                        useFallbackEmbed = false
                                        webViewRef?.tag = playerTag()
                                        webViewRef?.loadDataWithBaseURL(activeBaseUrl, playerHtml, "text/html", "UTF-8", null)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.id}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Watch on YouTube", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (recommendedVideos.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { onSelectRecommended(recommendedVideos.first()) },
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Play Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        onRemoveUnavailable?.invoke(video.id)
                                        onBackClick()
                                    },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Remove", fontSize = 11.sp, color = SafeCoral, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons row: Resolution, Speed, Sleep, Like, Watch Later, Save Offline, Share, PiP, Remove
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Video Resolution Toggle Action Button with DropdownMenu
                    Box {
                        val currentRes = resolutionOptions.firstOrNull { it.id == selectedQuality } ?: resolutionOptions.first()
                        Button(
                            onClick = { showQualityMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedQuality != "auto") SafeBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedQuality != "auto") SafeBlue else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("player_quality_action_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = "Video Resolution Menu",
                                tint = if (selectedQuality != "auto") SafeBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quality: ${currentRes.label}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedQuality != "auto") SafeBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false },
                            modifier = Modifier.testTag("video_quality_menu")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = "Video Resolution",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SafeBlue
                                )
                                Text(
                                    text = if (isBatterySaverActive) "Battery Saver is active (240p enforced)" else "Manage data usage & playback stability",
                                    fontSize = 11.sp,
                                    color = if (isBatterySaverActive) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontWeight = if (isBatterySaverActive) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                            resolutionOptions.forEach { res ->
                                val isSelected = (res.id == selectedQuality)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = res.label,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (res.badge != null) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (isSelected) SafeBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                                        ) {
                                                            Text(
                                                                text = res.badge,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = res.subtitle,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active Resolution",
                                                    tint = SafeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        applyQuality(res.id)
                                        showQualityMenu = false
                                        android.widget.Toast.makeText(context, "Resolution set to ${res.label}", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("video_quality_option_${res.id}")
                                )
                            }
                        }
                    }

                    // Playback Speed Adjustment Button with DropdownMenu
                    Box {
                        Button(
                            onClick = { showSpeedMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (playbackSpeed != 1.0f) SafeBlue.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (playbackSpeed != 1.0f) SafeBlue else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("player_speed_action_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Playback Speed Menu",
                                tint = if (playbackSpeed != 1.0f) SafeBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speed: ${formatSpeedLabel(playbackSpeed)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (playbackSpeed != 1.0f) SafeBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier.testTag("playback_speed_menu")
                        ) {
                            Text(
                                text = "Playback Speed",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SafeBlue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            speedOptions.forEach { speed ->
                                val isSelected = (speed == playbackSpeed)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatSpeedName(speed),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active Speed",
                                                    tint = SafeBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSetPlaybackSpeed(speed)
                                        showSpeedMenu = false
                                    },
                                    modifier = Modifier.testTag("playback_speed_option_${speed.toString().replace('.', '_')}")
                                )
                            }
                        }
                    }

                    // Sleep Timer Action Button
                    Button(
                        onClick = { showSleepTimerDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sleepTimerRemainingSeconds != null) SafeOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sleepTimerRemainingSeconds != null) SafeOrange else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("player_sleep_timer_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerRemainingSeconds != null) SafeOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (sleepTimerRemainingSeconds != null) {
                                "Sleep: ${formatSleepTimerRemaining(sleepTimerRemainingSeconds)}"
                            } else {
                                "Sleep Timer"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sleepTimerRemainingSeconds != null) SafeOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Like button
                    Button(
                        onClick = { isLiked = !isLiked },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiked) SafeCoral.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isLiked) SafeCoral else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiked) "Liked" else "Like",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Watch Later Bookmark Button
                    Button(
                        onClick = { onToggleWatchLater(video) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWatchLater) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isWatchLater) SafeOrange else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("player_watch_later_button")
                    ) {
                        Icon(
                            imageVector = if (isWatchLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isWatchLater) "Remove from Watch Later" else "Save to Watch Later",
                            tint = if (isWatchLater) SafeOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isWatchLater) "Saved" else "Watch Later",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save Offline Button
                    Button(
                        onClick = { onToggleOffline(video) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedOffline) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSavedOffline) SafeGreen else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("player_save_offline_button")
                    ) {
                        Icon(
                            imageVector = if (isSavedOffline) Icons.Default.FileDownloadDone else Icons.Default.Download,
                            contentDescription = "Save Offline",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSavedOffline) "Saved" else "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Open in YouTube Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.id}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("player_open_youtube_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in YouTube",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YouTube",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share Button
                    OutlinedButton(
                        onClick = {
                            ShareHelper.shareVideo(context, video.id, displayTitle)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Picture-in-Picture / Mini-Player Button
                    if (onEnterPip != null) {
                        OutlinedButton(
                            onClick = onEnterPip,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("player_pip_action_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture-in-Picture",
                                tint = SafeBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PiP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeBlue
                            )
                        }
                    }

                    // Remove Unavailable Button
                    OutlinedButton(
                        onClick = {
                            onRemoveUnavailable?.invoke(video.id)
                            onBackClick()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeCoral),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove if Unavailable",
                            modifier = Modifier.size(16.dp),
                            tint = SafeCoral
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remove",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeCoral
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description Box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Video Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displayDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "More Recommended Videos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Recommendations
            items(recommendedVideos.filter { it.id != video.id }) { recVideo ->
                val recTitle = recVideo.title
                    .replace("YouTube Video", "Video", ignoreCase = true)
                    .replace("YouTube", "SafePlay", ignoreCase = true)
                val recChannel = if (recVideo.channelTitle.contains("YouTube", ignoreCase = true)) {
                    "SafePlay Creator"
                } else {
                    recVideo.channelTitle
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectRecommended(recVideo) }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp, 56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = recVideo.thumbnailUrl,
                                contentDescription = recTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$recChannel • ${recVideo.durationText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = SafeCoral,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Sleep Timer",
                    tint = SafeOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sleep_timer_dialog"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Automatically pauses playback so you can fall asleep peacefully without worrying about screen time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (sleepTimerRemainingSeconds != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SafeOrange.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Active Timer Running",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SafeOrange
                                    )
                                    Text(
                                        text = "${formatSleepTimerRemaining(sleepTimerRemainingSeconds)} remaining",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Button(
                                    onClick = {
                                        onCancelSleepTimer()
                                        showSleepTimerDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafeCoral),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("turn_off_sleep_timer_button")
                                ) {
                                    Text("Turn Off", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Text(
                        text = "Choose Timer Duration:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val timerPresets = listOf(15, 30, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timerPresets.forEach { minutes ->
                            val isSelected = (sleepTimerTotalMinutes == minutes && sleepTimerRemainingSeconds != null)
                            OutlinedButton(
                                onClick = {
                                    onSetSleepTimer(minutes)
                                    showSleepTimerDialog = false
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) SafeOrange.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (isSelected) SafeOrange else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SafeOrange else MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sleep_timer_preset_${minutes}m")
                            ) {
                                Text("${minutes}m", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Custom minutes input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customSleepMinutesText,
                            onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) customSleepMinutesText = it },
                            label = { Text("Custom (mins)") },
                            placeholder = { Text("e.g. 20") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_sleep_timer_input")
                        )
                        Button(
                            onClick = {
                                val mins = customSleepMinutesText.toIntOrNull()
                                if (mins != null && mins > 0) {
                                    onSetSleepTimer(mins)
                                    customSleepMinutesText = ""
                                    showSleepTimerDialog = false
                                }
                            },
                            enabled = (customSleepMinutesText.toIntOrNull() ?: 0) > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = SafeOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("apply_custom_sleep_timer_button")
                        ) {
                            Text("Set")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSleepTimerDialog = false },
                    modifier = Modifier.testTag("close_sleep_timer_dialog_button")
                ) {
                    Text("Close")
                }
            },
            dismissButton = if (sleepTimerRemainingSeconds != null) {
                {
                    TextButton(
                        onClick = {
                            onCancelSleepTimer()
                            showSleepTimerDialog = false
                        }
                    ) {
                        Text("Turn Off Timer", color = SafeCoral)
                    }
                }
            } else null
        )
    }

    // Dialog shown when Sleep Timer triggers and pauses playback
    if (isSleepTimerTriggered) {
        AlertDialog(
            onDismissRequest = {
                onDismissSleepTimerTriggered()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Bedtime Sleep Timer",
                    tint = SafeOrange,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Sleep Timer Ended",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Video playback has been automatically paused for bedtime and sleep. Goodnight! 🌙",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetSleepTimer(15)
                        onDismissSleepTimerTriggered()
                        resumePlayback()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeOrange),
                    modifier = Modifier.testTag("sleep_timer_add_15_button")
                ) {
                    Text("+15 Minutes")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = {
                            onDismissSleepTimerTriggered()
                            resumePlayback()
                        },
                        modifier = Modifier.testTag("sleep_timer_resume_button")
                    ) {
                        Text("Resume")
                    }
                    TextButton(
                        onClick = {
                            onDismissSleepTimerTriggered()
                            onBackClick()
                        },
                        modifier = Modifier.testTag("sleep_timer_done_button")
                    ) {
                        Text("Close Video")
                    }
                }
            }
        )
    }

    // Comprehensive Player Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SafeBlue,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Player Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Resolution section
                    Text(
                        text = "Video Resolution & Data Usage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SafeBlue
                    )
                    Text(
                        text = "Select your preferred resolution to manage data usage and ensure smooth playback:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    resolutionOptions.forEach { res ->
                        val isSelected = res.id == selectedQuality
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SafeBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.5.dp, SafeBlue) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    applyQuality(res.id)
                                    android.widget.Toast.makeText(context, "Resolution set to ${res.label}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .testTag("settings_resolution_${res.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = res.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (res.badge != null) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = res.badge,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = res.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = SafeBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Playback Speed quick link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Playback Speed", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Current: ${formatSpeedLabel(playbackSpeed)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = {
                                showSettingsDialog = false
                                showSpeedMenu = true
                            },
                            modifier = Modifier.testTag("settings_speed_button")
                        ) {
                            Text(formatSpeedName(playbackSpeed), color = SafeBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Sleep Timer quick link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sleep Timer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (sleepTimerRemainingSeconds != null) "${formatSleepTimerRemaining(sleepTimerRemainingSeconds)} left" else "Off",
                                fontSize = 11.sp,
                                color = if (sleepTimerRemainingSeconds != null) SafeOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                showSettingsDialog = false
                                showSleepTimerDialog = true
                            },
                            modifier = Modifier.testTag("settings_sleep_timer_button")
                        ) {
                            Text(
                                text = if (sleepTimerRemainingSeconds != null) "Adjust" else "Set Timer",
                                color = SafeOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Eye & Head Tracking quick link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Eye & Head Tracking", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (trackingConfig.isTrackingEnabled) "Enabled • Hands-Free Active" else "Disabled",
                                fontSize = 11.sp,
                                color = if (trackingConfig.isTrackingEnabled) SafeBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                showSettingsDialog = false
                                onSetTrackingDialogOpen(true)
                            },
                            modifier = Modifier.testTag("settings_tracking_button")
                        ) {
                            Text(
                                text = if (trackingConfig.isTrackingEnabled) "Configure" else "Enable",
                                color = SafeBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Theme selector in Player Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Display Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = when (themeMode) {
                                    ThemeMode.SYSTEM -> "System Default (Auto)"
                                    ThemeMode.LIGHT -> "Light Mode"
                                    ThemeMode.DARK -> "Dark Mode"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ThemeMode.values().forEach { mode ->
                                val isSel = themeMode == mode
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) SafeBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { onSetThemeMode(mode) }
                                        .testTag("player_theme_${mode.name.lowercase()}")
                                ) {
                                    Text(
                                        text = "${mode.iconEmoji} ${mode.name.take(1) + mode.name.drop(1).lowercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                    modifier = Modifier.testTag("close_settings_dialog_button")
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Eye & Head Tracking Configuration & Sandbox Dialog
    if (isTrackingDialogOpen) {
        EyeHeadTrackingDialog(
            config = trackingConfig,
            state = trackingState,
            onDismiss = { onSetTrackingDialogOpen(false) },
            onUpdateConfig = onUpdateTrackingConfig,
            onSimulateGesture = onSimulateTrackingGesture,
            onRequestCameraPermission = onRequestCameraPermission
        )
    }
}
