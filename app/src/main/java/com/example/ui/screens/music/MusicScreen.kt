package com.example.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.OfflineVideoEntity
import com.example.data.models.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafePink
import com.example.ui.theme.SafePurple

data class MusicPlaylist(
    val title: String,
    val description: String,
    val emoji: String,
    val color: Color,
    val filterTags: List<String>
)

@Composable
fun MusicScreen(
    musicVideos: List<VideoItem>,
    offlineVideos: List<OfflineVideoEntity>,
    onVideoClick: (VideoItem) -> Unit,
    onToggleOffline: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val offlineIds = remember(offlineVideos) { offlineVideos.map { it.videoId }.toSet() }

    val playlists = remember {
        listOf(
            MusicPlaylist("All Music", "Complete track library", "🎵", SafeBlue, listOf("all")),
            MusicPlaylist("Amr Diab & Arabic Hits", "Tamally Maak, Nour El Ein, Inta El Haz", "🌙", Color(0xFFD97706), listOf("amr diab", "arabic", "tamally maak", "nour el ein", "inta el haz", "wayah", "amarain")),
            MusicPlaylist("Arctic Monkeys & Rock", "Do I Wanna Know?, 505, R U Mine?", "🎸", Color(0xFF475569), listOf("arctic monkeys", "alex turner", "do i wanna know", "505", "r u mine", "fluorescent")),
            MusicPlaylist("KATSEYE & K-Pop", "Touch, Debut, BTS, ILLIT", "💖", SafePink, listOf("katseye", "kpop", "hybe", "illit", "bts")),
            MusicPlaylist("Maroon 5 & Pop Rock", "Sugar, Memories, Jagger, OneRepublic", "🎸", SafeCoral, listOf("maroon 5", "maroon", "onerepublic", "imagine dragons", "rock")),
            MusicPlaylist("Pop & Dance Party", "Sabrina Carpenter, Taylor Swift, Trolls", "🕺", Color(0xFFE11D48), listOf("pop", "dance", "espresso", "shake it off", "trolls", "party", "sabrina", "taylor swift")),
            MusicPlaylist("Chill & Lofi Beats", "Calming study & relax beats", "☕", SafePurple, listOf("lofi", "chill", "relax", "beats")),
            MusicPlaylist("Piano & Instrumental", "Piano Guys, Ghibli, golden hour", "🎹", Color(0xFF0D9488), listOf("piano", "cello", "ghibli", "instrumental", "jvke", "golden hour")),
            MusicPlaylist("Disney Hits & Sing-Alongs", "Encanto, Frozen, Moana", "✨", Color(0xFFF59E0B), listOf("disney", "frozen", "encanto", "moana", "sing along"))
        )
    }

    val artists = remember {
        listOf(
            "All Artists",
            "Amr Diab",
            "Arctic Monkeys",
            "KATSEYE",
            "Maroon 5",
            "Sabrina Carpenter",
            "BTS",
            "ILLIT",
            "Taylor Swift",
            "OneRepublic",
            "Imagine Dragons",
            "JVKE",
            "Coldplay",
            "Disney"
        )
    }

    var selectedPlaylist by remember { mutableStateOf(playlists.first()) }
    var selectedArtist by remember { mutableStateOf("All Artists") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(selectedPlaylist, selectedArtist, searchQuery, musicVideos) {
        var list = musicVideos

        // Apply playlist filter
        if (!selectedPlaylist.filterTags.contains("all")) {
            list = list.filter { video ->
                selectedPlaylist.filterTags.any { tag ->
                    video.tags.any { it.contains(tag, ignoreCase = true) } ||
                    video.title.contains(tag, ignoreCase = true) ||
                    video.channelTitle.contains(tag, ignoreCase = true) ||
                    video.description.contains(tag, ignoreCase = true)
                }
            }
        }

        // Apply artist quick filter
        if (selectedArtist != "All Artists") {
            list = list.filter { video ->
                video.title.contains(selectedArtist, ignoreCase = true) ||
                video.channelTitle.contains(selectedArtist, ignoreCase = true) ||
                video.tags.any { it.contains(selectedArtist, ignoreCase = true) }
            }
        }

        // Apply text search query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            list = list.filter { video ->
                video.title.contains(q, ignoreCase = true) ||
                video.channelTitle.contains(q, ignoreCase = true) ||
                video.tags.any { it.contains(q, ignoreCase = true) } ||
                video.description.contains(q, ignoreCase = true)
            }
        }

        list.ifEmpty {
            if (searchQuery.isNotBlank() || selectedArtist != "All Artists") emptyList() else musicVideos
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("music_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Music Header Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF312E81), Color(0xFF4C1D95), SafePurple)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Music",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Music Lounge",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "KATSEYE • Maroon 5 • Pop Hits • Playlists",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Ad-Free Shield Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SafeGreen.copy(alpha = 0.22f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "AdBlock",
                                    tint = Color(0xFF86EFAC),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Ad-Free",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // In-Music Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("music_search_input"),
                        placeholder = {
                            Text(
                                text = "Search tracks, KATSEYE, Maroon 5...",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Play all / Shuffle bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clickable {
                                    if (filteredList.isNotEmpty()) {
                                        onVideoClick(filteredList.first())
                                    }
                                }
                                .testTag("music_play_all_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Play All (${filteredList.size})",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .clickable {
                                    if (filteredList.isNotEmpty()) {
                                        onVideoClick(filteredList.shuffled().first())
                                    }
                                }
                                .testTag("music_shuffle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Shuffle",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Playlist Cards Row
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Curated Playlists",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(playlists) { playlist ->
                    val isSelected = selectedPlaylist == playlist
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) playlist.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, playlist.color) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .width(165.dp)
                            .clickable {
                                selectedPlaylist = playlist
                                selectedArtist = "All Artists"
                            }
                            .testTag("playlist_card_${playlist.title.replace(" ", "_")}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(playlist.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = playlist.emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = playlist.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = playlist.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Quick Artist Chips Filter
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Featured Artists",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(artists) { artist ->
                    val isSelected = selectedArtist == artist
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedArtist = if (isSelected && artist != "All Artists") "All Artists" else artist
                        },
                        label = {
                            Text(
                                text = artist,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafePurple.copy(alpha = 0.2f),
                            selectedLabelColor = SafePurple
                        ),
                        modifier = Modifier.testTag("artist_chip_${artist.replace(" ", "_")}")
                    )
                }
            }
        }

        // Selected Playlist Header
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedArtist != "All Artists") "🎤 $selectedArtist Tracks" else "${selectedPlaylist.emoji} ${selectedPlaylist.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${filteredList.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Empty state when search or artist filter has no results
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎵", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No songs found for this filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing the search or choosing another artist",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            // Music Videos List
            items(filteredList) { video ->
                VideoCard(
                    video = video,
                    isSavedOffline = offlineIds.contains(video.id),
                    onVideoClick = { onVideoClick(video) },
                    onToggleOffline = { onToggleOffline(video) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
