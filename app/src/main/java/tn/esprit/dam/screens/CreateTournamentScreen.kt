package tn.esprit.dam.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.components.HomeBottomNavigationBar // Import the shared component

// --- 1. Custom Colors and Data Models ---

// Primary Green used for selection borders, progress bar, and continue button

val YellowAccent = Color(0xFFFFC107)
// Disabled/Gray color for the button
val DisabledGray = Color(0xFFE0E0E0)
val DisabledText = Color(0xFF9E9E9E)

data class TournamentType(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val capacity: String
)

val mockTournamentTypes = listOf(
    TournamentType(
        id = "knockout",
        title = "Knockout Tournament",
        description = "Single elimination bracket",
        icon = Icons.Filled.EmojiEvents,
        iconColor = PurpleAccent,
        capacity = "8-16 teams"
    ),
    TournamentType(
        id = "league",
        title = "League",
        description = "Round-robin format",
        icon = Icons.Filled.SportsScore,
        iconColor = BlueAccent,
        capacity = "6-12 teams"
    ),
    TournamentType(
        id = "quick_match",
        title = "Quick Match",
        description = "Fast casual game",
        icon = Icons.Filled.FlashOn,
        iconColor = YellowAccent,
        capacity = "2-10 teams"
    )
)


// --- 2. Composable Components ---

@Composable
fun ProgressHeader(
    title: String,
    step: String,
    progress: Float,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        // Removed .statusBarsPadding() to improve preview reliability
    ) {
        // Back Button and Title/Step
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Progress Bar
        // FIX: Use the lambda overload for 'progress' to resolve deprecation
        LinearProgressIndicator(
            progress = { progress }, // Changed to lambda
            color = PrimaryGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun TournamentTypeCard(
    type: TournamentType,
    isSelected: Boolean,
    onSelect: (TournamentType) -> Unit
) {
    // Animate the border color and thickness
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 200)
    )
    val borderWidth = if (isSelected) 2.dp else 1.dp

    // The inner container background is a very subtle color change when selected
    val cardBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect(type) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(type.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    type.icon,
                    contentDescription = type.title,
                    tint = type.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Details (Title, Description, Capacity)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.People,
                        contentDescription = "Teams",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = type.capacity,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Checkmark (Only visible when selected)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    navController: NavHostController = rememberNavController()
) {
    // State to track the currently selected tournament type ID.
    var selectedType by remember { mutableStateOf("") }

    // Check if the continue button should be enabled
    val isSelectionMade = selectedType.isNotEmpty()

    // Determine button colors based on state
    val buttonColor by animateColorAsState(
        targetValue = if (isSelectionMade) PrimaryGreen else DisabledGray,
        animationSpec = tween(durationMillis = 200)
    )
    val buttonContentColor by animateColorAsState(
        targetValue = if (isSelectionMade) Color.White else DisabledText,
        animationSpec = tween(durationMillis = 200)
    )

    // Tournament details state
    var details by remember { mutableStateOf(TournamentDetails()) }

    // Use Scaffold to structure the screen and reserve space for the bottom bar
    Scaffold(
        bottomBar = {
            // Using the shared component
            HomeBottomNavigationBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            // 1. Header and Progress Bar
            ProgressHeader(
                title = "Create Tournament",
                step = "Step 1 of 2",
                progress = 0.5f,
                onBackClick = { navController.popBackStack() }
            )

            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // 2. Section Title
                Text(
                    text = "Choose Tournament Type",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Select the format that suits your event",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 3. Tournament Type Cards
                mockTournamentTypes.forEach { type ->
                    TournamentTypeCard(
                        type = type,
                        isSelected = type.id == selectedType,
                        onSelect = { selectedType = it.id }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // 4. Continue Button (fixed at the bottom above the NavigationBar area)
            Button(
                onClick = {
                    navController.navigate("TournamentCreateForumScreen")
                },
                enabled = isSelectionMade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = DisabledGray,
                    disabledContentColor = DisabledText
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true)
@Composable
fun PreviewCreateTournamentScreen() {
    MaterialTheme {
        CreateTournamentScreen()
    }
}