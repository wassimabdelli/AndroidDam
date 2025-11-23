package tn.esprit.dam.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
// Import AutoMirrored icons for RTL compatibility
import androidx.compose.material.icons.automirrored.filled.Send
// Import standard filled icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import tn.esprit.dam.ui.theme.MediumGreen // Keep MediumGreen as it's a fixed accent color

// Define screen routes using strings that match the composable keys in MainActivity
sealed class Screen(val route: String, val icon: ImageVector) {
    object Home : Screen("HomeScreen", Icons.Default.Home)
    object Tournament : Screen("CreateTournamentScreen", Icons.Default.Leaderboard)
    object Events : Screen("EventsScreen", Icons.Default.EmojiEvents)
    object Share : Screen("SocialScreen", Icons.Default.Groups)
    object Profile : Screen("ProfileScreen", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Tournament,
    Screen.Events,
    Screen.Share,
    Screen.Profile
)

@Composable
fun HomeBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Use MaterialTheme.colorScheme.surface for the NavigationBar background
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(70.dp)
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            val routeExists = navController.graph.find { it.route == screen.route } != null

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route && routeExists) {
                        navController.navigate(screen.route) {
                            // This ensures that hitting the back button from a destination
                            // in the bottom nav doesn't take you back to the app start
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoids recreating the same screen multiple times
                            launchSingleTop = true
                            // Restores the previous state (e.g., scroll position)
                            restoreState = true
                        }
                    } else if (currentRoute != screen.route) {
                        // Handle navigation to a route that might not be the start destination
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.route,
                        modifier = Modifier.size(30.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    // MediumGreen remains fixed as the strong accent color
                    selectedIconColor = MediumGreen,
                    // Unselected icon color adapts to the theme's text color (onSurface)
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = if (isSelected) MediumGreen.copy(alpha = 0.3f) else Color.Transparent
                )
            )
        }
    }
}