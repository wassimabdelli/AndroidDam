package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import tn.esprit.dam.components.AnimatedCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import tn.esprit.dam.ui.theme.DAMTheme


// Define Data structure for navigation items
data class SocialNavItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val route: String
)

private val socialNavItems = listOf(
    SocialNavItem(
        icon = Icons.Filled.Group,
        iconColor = CardBlue,
        title = "Friends",
        subtitle = "Manage your connections",
        route = "FriendsScreen" // <-- Route updated to match the composable name
    ),
    SocialNavItem(
        icon = Icons.Filled.WorkspacePremium,
        iconColor = CardPurple,
        title = "Teams",
        subtitle = "View and manage teams",
        route = "TeamsScreen" // <-- Route updated to match the composable name
    ),
    SocialNavItem(
        icon = Icons.Filled.EmojiEvents,
        iconColor = CardOrange,
        title = "Leaderboard",
        subtitle = "Top players & teams",
        route = "PlacmentsScreen" // <-- Route updated to match the composable name
    )
)

// Define Data structure for statistics boxes
data class SocialStat(
    val value: String,
    val label: String
)

private val socialStats = listOf(
    SocialStat("12", "Friends"),
    SocialStat("2", "Teams"),
    SocialStat("#24", "Rank")
)


@Composable
fun SocialScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Include the HomeBottomNavigationBar as requested
            HomeBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        SocialScreenContent(
            navController = navController, // Pass NavController down
            modifier = Modifier.padding(paddingValues),
            onCloseClick = {
                // Navigate back or to home
                println("Close/Navigate back from Social Hub")
            }
        )
    }
}

@Composable
fun SocialScreenContent(
    navController: NavHostController, // Receive NavController
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Header (Title and Close Button) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Social Hub",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = "Connect and compete",
                    fontSize = 16.sp,
                    color = outlineColor
                )
            }
            // Using a simple IconButton for the 'X' close visual
            IconButton(onClick = onCloseClick) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = outlineColor
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- 2. Navigation Cards (Friends, Teams, Leaderboard) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            socialNavItems.forEach { item ->
                SocialNavCard(item = item) { route ->
                    // Navigation logic: Navigate to the corresponding screen
                    navController.navigate(route)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- 3. Statistics Boxes ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            socialStats.forEach { stat ->
                SocialStatBox(stat = stat)
            }
        }

        Spacer(Modifier.height(50.dp)) // Extra space for scrolling margin
    }
}

@Composable
fun SocialNavCard(item: SocialNavItem, onClick: (String) -> Unit) {
    AnimatedCard(
        onClick = { onClick(item.route) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        defaultElevation = 2.dp,
        pressedElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Arrow Icon
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Go to ${item.title}",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SocialStatBox(stat: SocialStat) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stat.label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, name = "Social Hub Screen")
@Composable
fun SocialScreenPreview() {
    DAMTheme {
        SocialScreen(rememberNavController())
    }
}