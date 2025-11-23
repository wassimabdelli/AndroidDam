package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // FIX: Use AutoMirrored version
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.components.HomeBottomNavigationBar

// --- 1. Custom Colors and Data Models ---

// Custom colors based on the images (STATIC COLORS - used for branding)
val PrimaryGreen = Color(0xFF4CAF50) // For the main profile card and 'W'
val PurpleAccent = Color(0xFF673AB7) // For Assists icon
val BlueAccent = Color(0xFF2196F3) // For Goals Scored icon
val OrangeAccent = Color(0xFFFF9800) // For Clean Sheets icon and Hat Trick achievement
val RedLoss = Color(0xFFF44336) // For 'L' match result
val GrayDraw = Color(0xFF9E9E9E) // For 'D' match result
// LightGrayCard will be dynamically defined based on theme using colorScheme.surfaceVariant

data class UserProfile(
    val initials: String,
    val name: String,
    val handle: String,
    val level: Int,
    val wins: Int,
    val losses: Int,
    val winRate: String,
    val goalsScored: Int,
    val assists: Int,
    val cleanSheets: Int,
    val totalMatches: Int,
    val favoriteStadium: String,
    val memberSince: String
)

data class Achievement(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String,
    val unlocked: Boolean = true // Assuming the visible ones are unlocked
)

data class MatchResult(
    val status: String, // "W", "L", "D"
    val title: String,
    val date: String,
    val score: String
)

// Mock Data for the screen (Unchanged)
val mockUserProfile = UserProfile(
    initials = "AR",
    name = "Alex Rodriguez",
    handle = "alexrod",
    level = 12,
    wins = 28,
    losses = 12,
    winRate = "62%",
    goalsScored = 84,
    assists = 32,
    cleanSheets = 15,
    totalMatches = 45,
    favoriteStadium = "Arena Sports Complex",
    memberSince = "January 2024"
)

val mockAchievements = listOf(
    Achievement(Icons.Filled.MilitaryTech, OrangeAccent, "Hat Trick Hero", "Score 3 goals in one match"),
    Achievement(Icons.Filled.Flare, OrangeAccent, "Winning Streak", "Win 5 matches in a row"),
    Achievement(Icons.Filled.EmojiEvents, OrangeAccent, "Tournament Champion", "Win a tournament"),
    Achievement(Icons.Filled.Star, PrimaryGreen, "Perfect Season", "Win all league matches", unlocked = false)
)

val mockMatches = listOf(
    MatchResult("W", "Champions Cup", "Nov 5", "3-1"),
    MatchResult("W", "Sunday League", "Nov 3", "2-0"),
    MatchResult("L", "Quick Match", "Nov 1", "1-2"),
    MatchResult("W", "Elite Knockout", "Oct 28", "4-2"),
    MatchResult("D", "Weekend Warriors", "Oct 25", "2-2")
)

// --- 2. Reusable Composable Components ---

// NOTE: ProfileTopBar function was removed as requested.

@Composable
fun MainProfileCard(user: UserProfile) {
    Card(
        shape = RoundedCornerShape(20.dp),
        // Use the fixed PrimaryGreen for the dominant color of the card
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Initials Circle (Avatar)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "@${user.handle}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    // Level Badge
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                    ) {
                        Text(
                            text = "Level ${user.level}",
                            // Use PrimaryGreen for contrast on the light badge
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats Row (Wins, Losses, Win Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatCircle(value = user.wins.toString(), label = "Wins", color = Color.White.copy(alpha = 0.1f))
                ProfileStatCircle(value = user.losses.toString(), label = "Losses", color = Color.White.copy(alpha = 0.1f))
                ProfileStatCircle(value = user.winRate, label = "Win Rate", color = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun ProfileStatCircle(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun QuickStatsGrid(user: UserProfile) {
    // Use surfaceVariant for secondary card backgrounds
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val stats = listOf(
        Triple(Icons.Outlined.SportsSoccer, BlueAccent, user.goalsScored.toString() to "Goals Scored"),
        Triple(Icons.Outlined.Timeline, PurpleAccent, user.assists.toString() to "Assists"),
        Triple(Icons.Filled.Scoreboard, OrangeAccent, user.cleanSheets.toString() to "Clean Sheets"),
        Triple(Icons.Filled.People, PrimaryGreen, user.totalMatches.toString() to "Total Matches"),
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // 2x2 Grid Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickStatCard(stats[0].first, stats[0].second, stats[0].third.first, stats[0].third.second, cardBackground, textPrimary, textSecondary, Modifier.weight(1f))
            Spacer(Modifier.width(16.dp))
            QuickStatCard(stats[1].first, stats[1].second, stats[1].third.first, stats[1].third.second, cardBackground, textPrimary, textSecondary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickStatCard(stats[2].first, stats[2].second, stats[2].third.first, stats[2].third.second, cardBackground, textPrimary, textSecondary, Modifier.weight(1f))
            Spacer(Modifier.width(16.dp))
            QuickStatCard(stats[3].first, stats[3].second, stats[3].third.first, stats[3].third.second, cardBackground, textPrimary, textSecondary, Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    cardColor: Color,
    textPrimaryColor: Color,
    textSecondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = modifier.height(110.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = textPrimaryColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textSecondaryColor
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, showViewAll: Boolean = true) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = textPrimary
        )
        if (showViewAll) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset(x = 8.dp) // Offset to visually align the right edge
            ) {
                Text(
                    text = "View All",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* Handle view all click */ }
                )
                Icon(
                    // FIX: Replaced deprecated Icons.Filled.KeyboardArrowRight with AutoMirrored version
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View All",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        modifier = Modifier
            .width(150.dp)
            .height(120.dp)
            .padding(end = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(achievement.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    achievement.icon,
                    contentDescription = achievement.title,
                    tint = achievement.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textPrimary,
                lineHeight = 16.sp
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = textSecondary
            )
        }
    }
}

@Composable
fun MatchRow(match: MatchResult) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val (statusColor, statusText) = when (match.status) {
        "W" -> PrimaryGreen to "W"
        "L" -> RedLoss to "L"
        "D" -> GrayDraw to "D"
        else -> Color.Black to "?"
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    color = Color.White, // Always white text on W/L/D boxes
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            // Match Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = textPrimary
                )
                Text(
                    text = match.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }

            // Score
            Text(
                text = match.score,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
        }
        // FIX: Replace deprecated Divider with HorizontalDivider
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun FooterDetailRow(icon: ImageVector, title: String, value: String) {
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = PrimaryGreen,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = textPrimary
            )
        }
    }
}

// --- 3. Main Screen Composable ---

@Composable
fun ProfileScreen(
    navController: NavHostController,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    user: UserProfile = mockUserProfile,
    achievements: List<Achievement> = mockAchievements,
    matches: List<MatchResult> = mockMatches
) {
    // Navigation action to settings screen
    val onSettingsClick: () -> Unit = {
        // NAVIGATE TO PROFILE SETTINGS SCREEN
        navController.navigate("ProfileScreenSettings")
    }

    // WRAPPED IN SCAFFOLD WITH BOTTOM BAR
    Scaffold(
        // The topBar slot is explicitly removed as requested.
        bottomBar = { HomeBottomNavigationBar(navController = navController) },
        // Use MaterialTheme.colorScheme.background for screen container
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Apply bottom padding from Scaffold, handle status bar manually
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Custom Header Area (Title and Settings Button) ---
            // Manually creating the header row and handling status bar padding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Ensures content clears the device status bar
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp) // Internal vertical padding
            ) {
                // Title (Aligned Start)
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Settings Button (Aligned End)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Main Profile Card
            MainProfileCard(user = user)

            Spacer(Modifier.height(16.dp))

            // Quick Stats Grid
            QuickStatsGrid(user = user)

            Spacer(Modifier.height(8.dp))

            // Achievements Section
            SectionHeader(title = "Achievements")

            // LazyRow for horizontal scroll of achievements
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(achievements) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Recent Matches Section
            SectionHeader(title = "Recent Matches")

            // List of Recent Matches
            matches.forEach { match ->
                MatchRow(match = match)
            }

            Spacer(Modifier.height(16.dp))

            // Footer Details
            FooterDetailRow(
                icon = Icons.Filled.LocationOn,
                title = "Favorite Stadium",
                value = user.favoriteStadium
            )
            FooterDetailRow(
                icon = Icons.Filled.CalendarMonth,
                title = "Member Since",
                value = user.memberSince
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- 4. Preview (Optional, for development environment) ---

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    // Use a placeholder theme for preview since AppTheme isn't imported here
    MaterialTheme {
        ProfileScreen(
            navController = rememberNavController(),
            darkTheme = false,
            onThemeToggle = {}
        )
    }
}