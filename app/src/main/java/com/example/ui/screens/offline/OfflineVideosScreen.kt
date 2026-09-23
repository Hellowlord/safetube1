package com.example.ui.screens.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.OfflineVideoEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoCategory
import com.example.data.models.VideoItem
import com.example.data.repository.CuratedVideoData
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeOrange
import com.example.util.ShareHelper

@Composable
fun OfflineVideosScreen(
    offlineVideos: List<OfflineVideoEntity>,
    isOfflineModeActive: Boolean,
    onToggleOfflineMode: () -> Unit,
    onPlayOfflineVideo: (VideoItem) -> Unit,
    onDeleteOfflineVideo: (String) -> Unit,
    onBrowseSafeVideos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalSizeMb = offlineVideos.sumOf { it.fileSizeMb }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("offline_videos_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SafeGreen.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Mode",
                                tint = SafeGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Offline Viewing Mode",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "Watch without internet • Travel & Car rides",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Airplane / Offline mode toggle card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOfflineModeActive) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flight,
                                    contentDescription = "Travel Mode",
                                    tint = if (isOfflineModeActive) SafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        text = if (isOfflineModeActive) "Travel Mode Active" else "Enable Travel Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Only show offline downloaded videos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Switch(
                                checked = isOfflineModeActive,
                                onCheckedChange = { onToggleOfflineMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SafeGreen
                                ),
                                modifier = Modifier.testTag("offline_travel_mode_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Storage summary bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = "Storage",
                                tint = SafeBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${offlineVideos.size} Videos Saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = String.format("%.1f MB used", totalSizeMb),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (totalSizeMb.toFloat() / 500f).coerceIn(0.05f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SafeGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Empty State or List
        if (offlineVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "📥", fontSize = 52.sp)
                        Text(
                            text = "No Videos Saved for Offline Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap the download icon 📥 on any safe video from the Home screen so your child can watch it anytime without internet!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onBrowseSafeVideos,
                            colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("browse_to_download_button")
                        ) {
                            Text("Browse Safe Videos", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Downloaded Videos Ready to Play",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(offlineVideos) { item ->
                val thumbModel = item.localThumbnailPath?.takeIf { it.isNotBlank() }
                    ?: "https://img.youtube.com/vi/${item.videoId}/hqdefault.jpg"

                val videoItem = VideoItem(
                    id = item.videoId,
                    title = item.title,
                    channelTitle = item.channelTitle,
                    description = item.description,
                    category = try { VideoCategory.valueOf(item.category) } catch (e: Exception) { VideoCategory.ALL },
                    ageGroup = try { AgeGroup.valueOf(item.ageGroup) } catch (e: Exception) { AgeGroup.EARLY },
                    durationText = item.durationText,
                    thumbnailUrl = thumbModel,
                    offlineAvailable = true,
                    localFilePath = item.localFilePath,
                    localThumbnailPath = item.localThumbnailPath
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onPlayOfflineVideo(videoItem) }
                        .testTag("offline_item_${item.videoId}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play preview / thumbnail
                        Box(
                            modifier = Modifier
                                .size(76.dp, 52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SafeBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = thumbModel,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Offline",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.channelTitle} • ${item.durationText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "✓ Offline",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SafeBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "MP4",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafeBlue,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = String.format("%.1f MB", item.fileSizeMb),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Share button
                            IconButton(
                                onClick = {
                                    ShareHelper.shareVideo(context, item.videoId, item.title)
                                },
                                modifier = Modifier.testTag("share_offline_${item.videoId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Video Link",
                                    tint = SafeBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete button
                            IconButton(
                                onClick = { onDeleteOfflineVideo(item.videoId) },
                                modifier = Modifier.testTag("delete_offline_${item.videoId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Download",
                                    tint = SafeCoral.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
