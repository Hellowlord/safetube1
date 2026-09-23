package com.example.ui.screens.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.UserProfileEntity
import com.example.data.models.AgeGroup
import com.example.data.models.VideoItem
import com.example.data.repository.CuratedVideoData
import com.example.ui.theme.SafeBlue
import com.example.ui.theme.SafeCoral
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeOrange
import com.example.ui.theme.SafePurple

@Composable
fun WelcomeIntroScreen(
    profiles: List<UserProfileEntity> = emptyList(),
    activeProfile: UserProfileEntity? = null,
    onSelectProfile: (UserProfileEntity) -> Unit = {},
    onStartExploring: () -> Unit,
    onWatchIntroVideo: (VideoItem) -> Unit,
    onSaveProfileAndEnter: (name: String, emoji: String, ageGroup: AgeGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Sign In / Switch Account, 1: Create Profile

    // Sign In form state
    var signInEmail by remember { mutableStateOf("") }
    var signInPin by remember { mutableStateOf("1234") }

    // Sign Up form state
    var childName by remember { mutableStateOf("Alex") }
    var selectedEmoji by remember { mutableStateOf("🦊") }
    var selectedAgeGroup by remember { mutableStateOf(AgeGroup.EARLY) }

    val emojis = listOf("🦊", "🦁", "🐼", "🚀", "🦄", "⭐", "🦕", "🎨", "🍳", "🎵")

    // Pulsing animation for mascot star
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFEFF6FF),
                        Color(0xFFF8FAFC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .testTag("welcome_intro_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Animation & Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SafeBlue, Color(0xFF4F46E5), SafePurple)
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.safetube_welcome_hero),
                    contentDescription = "SafePlay Welcome Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.35f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(starScale)
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎬", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SafePlay Video & Music",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "Curated Music, Cooking, Science & Originals • Ad-Free",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Feature Highlights Pills
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeaturePill(emoji = "🎵", title = "Music Lounge", subtitle = "Curated Tracks", modifier = Modifier.weight(1f))
                FeaturePill(emoji = "🍳", title = "Cooking & Food", subtitle = "Tasty Recipes", modifier = Modifier.weight(1f))
                FeaturePill(emoji = "📥", title = "Offline Mode", subtitle = "Watch Anywhere", modifier = Modifier.weight(1f))
            }
        }

        // Who is Watching / Switch Accounts Section
        if (profiles.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("who_is_watching_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = "Switch Account",
                                tint = SafeBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Who is Watching?",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "Tap a profile to start watching instantly or switch accounts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        // Profiles Grid / Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(profiles) { profile ->
                                val isActive = activeProfile?.id == profile.id
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) SafeBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, SafeBlue) else null,
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clickable { onSelectProfile(profile) }
                                        .testTag("profile_card_${profile.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(if (isActive) SafeBlue else MaterialTheme.colorScheme.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = profile.avatarEmoji, fontSize = 28.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = profile.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )

                                        Text(
                                            text = profile.ageGroup,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isActive) SafeBlue else SafeCoral
                                        ) {
                                            Text(
                                                text = if (isActive) "Active" else "Play",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Add New Profile item
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { selectedAuthTab = 1 }
                                        .testTag("add_profile_shortcut")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(SafePurple.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Profile",
                                                tint = SafePurple,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "+ New",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SafePurple
                                        )
                                        Text(
                                            text = "Profile",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Welcome Intro Video Preview Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("welcome_intro_video_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SafeOrange.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🎬 Video Guide",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "How SafePlay works",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Watch How SafePlay Works!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Experience safe, ad-free streaming, quick account switching, and offline saving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Video preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { onWatchIntroVideo(CuratedVideoData.INTRO_WELCOME_VIDEO) }
                            .testTag("play_intro_video_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.safetube_welcome_hero),
                            contentDescription = "Welcome Intro Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(SafeOrange)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Intro Video",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Play Video Guide",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Sign In / Sign Up Tabs Card
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("auth_container_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tab row
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                                color = SafeBlue,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = {
                                Text(
                                    text = "🔑 Sign In",
                                    fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAuthTab == 0) SafeBlue else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = {
                                Text(
                                    text = "✨ New Profile",
                                    fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAuthTab == 1) SafeBlue else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_up")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (selectedAuthTab == 0) {
                        // SIGN IN
                        Text(
                            text = "Account Sign In",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Sign in to access your playlist favorites, uploads, and switch accounts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = signInEmail,
                            onValueChange = { signInEmail = it },
                            label = { Text("Email or Username") },
                            placeholder = { Text("user@example.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = signInPin,
                            onValueChange = { signInPin = it },
                            label = { Text("Password or PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onStartExploring,
                            colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("signin_submit_button")
                        ) {
                            Text(
                                text = "Sign In & Enter SafePlay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                    } else {
                        // CREATE CHILD PROFILE
                        Text(
                            text = "Create Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Set up your avatar and profile identity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Child Name field
                        OutlinedTextField(
                            value = childName,
                            onValueChange = { childName = it },
                            label = { Text("Profile Name") },
                            placeholder = { Text("e.g. Alex, Sam, Jordan") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_child_name_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avatar Emoji Picker
                        Text(
                            text = "Choose Mascot Avatar:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(emojis) { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedEmoji == emoji) SafeBlue.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                            color = if (selectedEmoji == emoji) SafeBlue else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Age Bracket Picker
                        Text(
                            text = "Select Age Group:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AgeGroup.values().forEach { age ->
                                val isSelected = selectedAgeGroup == age
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SafeBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedAgeGroup = age }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = age.iconEmoji, fontSize = 18.sp)
                                        Text(
                                            text = age.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = age.ageRange,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val name = if (childName.isNotBlank()) childName else "Explorer"
                                onSaveProfileAndEnter(name, selectedEmoji, selectedAgeGroup)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("signup_submit_button")
                        ) {
                            Text(
                                text = "Create Profile & Start Watching",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Guest / Explore Button
                    Button(
                        onClick = onStartExploring,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_guest_explore_button")
                    ) {
                        Text(
                            text = "Quick Watch Without Sign In",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun FeaturePill(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
