package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment // Placeholder for fire_icon (Goals)
import androidx.compose.material.icons.filled.KeyboardArrowUp // Placeholder for up_arrow_icon (Trend)
import androidx.compose.material.icons.filled.MilitaryTech // Placeholder for trophy/crown icon (Wins/Crown)
import androidx.compose.material.icons.filled.PeopleAlt // Placeholder for users_icon (Members)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import tn.esprit.dam.ui.theme.DAMTheme // Import your main application theme

// IMPORTANT: Renamed data classes to avoid conflict with the centralized models
// These models are specific to the Leaderboard (PlacmentScreen) ranking display.

/**
 * Data model for a single player entry on the Leaderboard.
 */
data class RankedPlayer(
    val rank: Int,
    val initial: String,
    val name: String,
    val wins: Int,
    val goals: Int,
    val winRate: String,
    val avatarColor: Color,
    val isTop: Boolean = false,
    val crown: Boolean = false
)

/**
 * Data model for a single team entry on the Leaderboard.
 */
data class RankedTeam(
    val rank: Int,
    val name: String,
    val wins: Int,
    val members: Int,
    val winRate: String,
    val icon: ImageVector,
    val isTop: Boolean = false,
    val crown: Boolean = false
)

// Mock Data (Colors used for rank/avatar branding are intentionally kept fixed)
private val mockPlayers = listOf(
    RankedPlayer(1, "AR", "Alex Rodriguez", 89, 145, "90%", Color(0xFFFF9800), true, true), // Orange (Rank 1/Secondary)
    RankedPlayer(2, "MS", "Marcus Silva", 85, 130, "85%", Color(0xFF9E9E9E), true), // Silver (Rank 2)
    RankedPlayer(3, "JL", "Jordan Lee", 78, 120, "78%", Color(0xFFFF5722), true), // Bronze/Red (Rank 3)
    RankedPlayer(4, "ST", "Sam Taylor", 72, 124, "77%", Color(0xFF4CAF50)), // Green (Primary)
    RankedPlayer(5, "RB", "Riley Brooks", 66, 118, "75%", Color(0xFF4CAF50)),
    RankedPlayer(6, "CJ", "Casey Jordan", 64, 112, "73%", Color(0xFF4CAF50)),
    RankedPlayer(7, "MB", "Morgan Blake", 61, 106, "71%", Color(0xFF4CAF50)),
    RankedPlayer(8, "AQ", "Avery Quinn", 58, 98, "69%", Color(0xFF4CAF50)),
)

private val mockTeams = listOf(
    RankedTeam(1, "Thunder FC", 124, 12, "92%", Icons.Default.MilitaryTech, true, true),
    RankedTeam(2, "Phoenix Rising", 118, 10, "88%", Icons.Default.MilitaryTech, true),
    RankedTeam(3, "Storm United", 112, 11, "85%", Icons.Default.MilitaryTech, true),
    RankedTeam(4, "Elite Squad", 106, 9, "79%", Icons.Default.MilitaryTech),
    RankedTeam(5, "Champions FC", 98, 10, "76%", Icons.Default.MilitaryTech),
)

// Renamed the composable function to match the requested spelling
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacmentScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // 0 for Players, 1 for Teams

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                // Use theme surface color for the Top Bar background
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // Use theme background color for the main screen
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Top performers this season",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp),
                // Use theme's onSurfaceVariant for secondary/muted text
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )

            TopThreeSection(selectedTab)

            // Tabs for Players and Teams
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TabButton(text = "Players", isSelected = selectedTab == 0) { selectedTab = 0 }
                Spacer(Modifier.width(16.dp))
                TabButton(text = "Teams", isSelected = selectedTab == 1) { selectedTab = 1 }
            }

            // List Content
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                val listToDisplay = if (selectedTab == 0) mockPlayers else mockTeams
                val startIndex = 3 // Start after the top 3
                items(listToDisplay.size - startIndex) { index ->
                    val item = listToDisplay[index + startIndex]
                    if (selectedTab == 0) {
                        PlayerListItem(player = item as RankedPlayer)
                    } else {
                        TeamListItem(team = item as RankedTeam)
                    }
                }
            }
        }
    }
}

@Composable
fun TopThreeSection(selectedTab: Int) {
    val data = if (selectedTab == 0) mockPlayers.take(3) else mockTeams.take(3)
    // The colors based on rank: Gold, Silver, Bronze. These remain fixed branding colors.
    val colors = listOf(Color(0xFFFF9800), Color(0xFF9E9E9E), Color(0xFFFF5722))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Render #2, #1, #3 in order
        if (data.size > 1) TopThreeItem(data[1], colors[1], selectedTab, crown = false) // #2
        if (data.isNotEmpty()) TopThreeItem(data[0], colors[0], selectedTab, crown = true)  // #1
        if (data.size > 2) TopThreeItem(data[2], colors[2], selectedTab, crown = false) // #3
    }
}

@Composable
fun TopThreeItem(item: Any, cardColor: Color, selectedTab: Int, crown: Boolean) {
    val rank: Int
    val primaryText: String
    val secondaryText: String
    val bottomText: String
    val icon: ImageVector?

    if (selectedTab == 0) {
        val player = item as RankedPlayer
        rank = player.rank
        primaryText = player.initial
        secondaryText = player.name
        bottomText = "${player.wins} wins"
        icon = null
    } else {
        val team = item as RankedTeam
        rank = team.rank
        primaryText = team.name
        secondaryText = team.name
        bottomText = "${team.wins} wins"
        icon = team.icon
    }

    // Fixed colors are used for avatar background and rank card to maintain brand identity
    val avatarShape = if (selectedTab == 0) CircleShape else RoundedCornerShape(12.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(IntrinsicSize.Min)
    ) {
        if (crown) {
            Icon(
                Icons.Default.MilitaryTech, // Crown Icon
                contentDescription = "Crown",
                tint = Color(0xFFFFC107), // Gold tint (fixed)
                modifier = Modifier.size(32.dp).offset(y = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (crown) 0.dp else 16.dp))

        // Avatar/Icon
        if (selectedTab == 0) {
            Box(
                modifier = Modifier
                    .size(if (rank == 1) 72.dp else 64.dp)
                    .clip(CircleShape)
                    .background(if (rank == 1) cardColor.copy(alpha = 0.8f) else cardColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = primaryText,
                    color = Color.White, // Text on fixed dark color should be white
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Team Icon
            Icon(
                imageVector = icon!!,
                contentDescription = "Team Icon",
                modifier = Modifier
                    .size(if (rank == 1) 72.dp else 64.dp)
                    .clip(avatarShape)
                    .background(cardColor) // Background remains specific ranking color
                    .padding(8.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = secondaryText,
            fontSize = 14.sp,
            maxLines = 1,
            // Use theme color for secondary text
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = bottomText,
            fontSize = 12.sp,
            // Use theme color for secondary text
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (rank == 1) 100.dp else 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor), // Background remains specific ranking color
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#$rank",
                    color = Color.White, // Text on fixed dark color should be white
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        // Use theme primary color for selected tab, onSurfaceVariant for unselected
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .run {
                this.clip(RoundedCornerShape(4.dp))
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        // Use theme primary color for the border
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(bottom = 2.dp)
            }
    )
}

@Composable
fun PlayerListItem(player: RankedPlayer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Use theme surface color for card background
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${player.rank}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(32.dp),
                // Use theme onSurface color for rank number
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(player.avatarColor), // Fixed avatar color
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.initial, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface // Main text color
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = "Wins", modifier = Modifier.size(12.dp), tint = Color(0xFFFF9800)) // Fixed orange icon
                    Text(text = "${player.wins} wins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Muted text color
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Goals", modifier = Modifier.size(12.dp), tint = Color(0xFFFF5722)) // Fixed red icon
                    Text(text = "${player.goals} goals", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Muted text color
                }
            }

            Text(
                text = player.winRate,
                fontWeight = FontWeight.Bold,
                // Use theme primary color for the key metric
                color = MaterialTheme.colorScheme.primary
            )
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Trend", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun TeamListItem(team: RankedTeam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Use theme surface color for card background
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${team.rank}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.onSurface // Main text color
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = team.icon,
                contentDescription = "Team Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF03A9F4)) // Fixed blue background color
                    .padding(8.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface // Main text color
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = "Wins", modifier = Modifier.size(12.dp), tint = Color(0xFFFF9800)) // Fixed orange icon
                    Text(text = "${team.wins} wins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Muted text color
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PeopleAlt, contentDescription = "Members", modifier = Modifier.size(12.dp), tint = Color(0xFF03A9F4)) // Fixed blue icon
                    Text(text = "${team.members} members", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Muted text color
                }
            }

            Text(
                text = team.winRate,
                fontWeight = FontWeight.Bold,
                // Use theme primary color for the key metric
                color = MaterialTheme.colorScheme.primary
            )
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Trend", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
    }
}

// Previews to showcase Dark and Light modes using your DAMTheme
@Preview(showBackground = true)
@Composable
fun PlacmentScreenLightPreview() {
    DAMTheme(darkTheme = false) {
        PlacmentScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun PlacmentScreenDarkPreview() {
    DAMTheme(darkTheme = true) {
        PlacmentScreen(navController = rememberNavController())
    }
}