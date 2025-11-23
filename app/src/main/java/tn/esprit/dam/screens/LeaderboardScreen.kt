package tn.esprit.dam.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R
// Removed unused theme imports, keeping only the package structure
import tn.esprit.dam.components.HomeBottomNavigationBar

// --- Data Structures (Unchanged) ---
data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: String,
    val imageRes: Int,
    val isYou: Boolean = false
)

// Dummy Data (Unchanged)
val topWinners = listOf(
    LeaderboardEntry(2, "Tony Stark", "6500 Pts", R.drawable.trophy), // Changed to a better dummy image
    LeaderboardEntry(1, "Peter Parker", "7400 Pts", R.drawable.trophy), // Changed to a better dummy image
    LeaderboardEntry(3, "John Carter", "5800 Pts", R.drawable.trophy) // Changed to a better dummy image
)

val rankedList = listOf(
    LeaderboardEntry(14, "Satwik Pachino", "3684 Pts", R.drawable.trophy, isYou = true), // Changed to a better dummy image
    LeaderboardEntry(1, "Peter Parker", "7400 Pts", R.drawable.trophy), // Changed to a better dummy image
    LeaderboardEntry(2, "Tony Stark", "6500 Pts", R.drawable.trophy), // Changed to a better dummy image
    LeaderboardEntry(3, "John Carter", "5800 Pts", R.drawable.trophy), // Changed to a better dummy image
    LeaderboardEntry(4, "Reeta Chainani", "5400 Pts", R.drawable.trophy) // Changed to a better dummy image
)

// --- LeaderboardContent (Theme Adaptive) ---
@Composable
fun LeaderboardContent(paddingValues: PaddingValues = PaddingValues(0.dp)) {
    // Dynamic Theme Colors
    val topSectionColor = MaterialTheme.colorScheme.surface
    val listSectionColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(listSectionColor)
            .padding(paddingValues)
    ) {
        // --- Top Section (Adaptive Background) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(
                    color = topSectionColor, // Adaptive Top background
                    shape = RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LeaderboardHeader()
                TabBarLeaderboard()
                Spacer(Modifier.height(16.dp))
                TopWinnersSection()
            }
        }

        // --- List Section ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(listSectionColor)
                .padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(rankedList) { entry ->
                RankedListItem(entry = entry)
            }
        }
    }
}

// --- LeaderboardScreen (Main Function using NavController) ---
@Composable
fun LeaderboardScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { HomeBottomNavigationBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LeaderboardContent(paddingValues)
    }
}

// --- Component Composables (Theme Adaptive - Colors Fixed) ---

@Composable
fun LeaderboardHeader() {
    val headerTextColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Winners",
            color = headerTextColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        // Friends Dropdown
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Friends",
                color = headerTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = headerTextColor,
                modifier = Modifier.size(24.dp)
            )
        }
        // Profile Icon
        Icon(
            Icons.Default.PersonAdd,
            contentDescription = "Profile",
            tint = headerTextColor,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun TabBarLeaderboard() {
    val tabs = listOf("Today", "Month", "All time")
    // NOTE: This should probably be a state variable in a real app
    val selectedIndex = 0
    val unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            TextButton(
                onClick = { /* Handle tab click */ },
                colors = ButtonDefaults.textButtonColors(
                    // Tab Selected uses Primary color from the scheme
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    // Selected text is onPrimary, unselected text is adaptive subtle color
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else unselectedTextColor
                ),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun TopWinnersSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        // Winner 2 (Tony Stark) - Aligned CenterVertically, smaller
        TopWinnerItem(topWinners[0], Modifier.align(Alignment.CenterVertically))
        // Winner 1 (Peter Parker) - Aligned Top, taller
        TopWinnerItem(topWinners[1], Modifier.align(Alignment.Top))
        // Winner 3 (John Carter) - Aligned CenterVertically, smaller
        TopWinnerItem(topWinners[2], Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun TopWinnerItem(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    val topSectionColor = MaterialTheme.colorScheme.surface // Adaptive background for the primary badge circle

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(IntrinsicSize.Max)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Profile Image
            Image(
                painter = painterResource(id = entry.imageRes),
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Adaptive subtle background
            )
            // Rank Badge
            val badgeColor = when (entry.rank) {
                1 -> MaterialTheme.colorScheme.primary // Rank 1: Primary Green
                2 -> MaterialTheme.colorScheme.secondary // Rank 2: Secondary Color
                3 -> MaterialTheme.colorScheme.tertiary // Rank 3: Tertiary Color
                else -> Color.Transparent
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = 5.dp, y = 5.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor) // Adaptive Rank Color
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(topSectionColor) // Adaptive inner circle background (same as top section)
            ) {
                Text(
                    text = entry.rank.toString(),
                    // Rank text color: uses the color that contrasts with the inner circle
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Text is always displayed on the top section's adaptive surface
        Text(entry.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(entry.points, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 14.sp)
    }
}

@Composable
fun RankedListItem(entry: LeaderboardEntry) {
    // Dynamic Colors
    val cardBackgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val rankColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    // Highlight for 'You' row
    // Use a lighter variant of primary color (e.g., primaryContainer) for the highlight background
    val rowColor = if (entry.isYou) MaterialTheme.colorScheme.primaryContainer else cardBackgroundColor
    // Text color should be strong/contrasting for the 'You' row, especially in dark mode
    val rowTextColor = if (entry.isYou) MaterialTheme.colorScheme.onPrimaryContainer else textColor

    Card(
        shape = RoundedCornerShape(12.dp),
        // Use the adaptive/highlight color for the container
        colors = CardDefaults.cardColors(containerColor = rowColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = entry.rank.toString(),
                color = rankColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.width(30.dp)
            )

            // Profile Image
            Image(
                painter = painterResource(id = entry.imageRes),
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Adaptive subtle background
            )
            Spacer(Modifier.width(16.dp))

            // Name and (You) Label
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.name,
                        color = rowTextColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    if (entry.isYou) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "(You)",
                            // Fixed Accent Color replaced with Material Theme Primary
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Points
            Text(
                text = entry.points,
                color = rowTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// --- PREVIEW FUNCTION (Theme Adaptive) ---
@Preview(showBackground = true, widthDp = 360, heightDp = 720, name = "Leaderboard Screen Final")
@Composable
fun LeaderboardScreenPreview() {
    // Wrap in your theme for proper preview
    tn.esprit.dam.ui.theme.DAMTheme {
        // Use the adaptive background color for the Surface
        Surface(color = MaterialTheme.colorScheme.background) {
            LeaderboardContent()
        }
    }
}