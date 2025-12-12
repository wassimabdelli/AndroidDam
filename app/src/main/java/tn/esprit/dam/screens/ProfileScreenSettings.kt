package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// FIX: Ensure all AutoMirrored icons used (ExitToApp, KeyboardArrowRight, Help, and ArrowBack) are explicitly imported.
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.dam.R
import tn.esprit.dam.ui.theme.DAMTheme
import tn.esprit.dam.models.AuthViewModel

// Helper data structure for settings items (Unchanged)
private data class SettingItemData(
    val icon: ImageVector,
    val title: String,
    val iconBgColor: Color, // This is the color for the icon and its subtle background
    val isToggle: Boolean = false,
    val isAction: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenSettings(
    navController: NavController,
    darkTheme: Boolean, // Explicitly pass the current theme state
    onThemeToggle: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // State to control the visibility of the dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    // Dynamic Theme Colors
    val colorScheme = MaterialTheme.colorScheme
    val cardSurfaceColor = colorScheme.surface
    val screenBackgroundColor = colorScheme.background
    val textColor = colorScheme.onSurface
    val context = LocalContext.current
    var currentUserRole by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as android.app.Application
            val repo = tn.esprit.dam.data.AuthRepository(app)
            val user = repo.getUser()
            currentUserRole = user?.role
        } catch (_: Exception) {
            currentUserRole = null
        }
    }


// --- Data Definitions ---
    val accountSettings = remember(colorScheme, currentUserRole) {
        // Liste de base avec Edit Profile et les autres
        val listItems = mutableListOf(
            SettingItemData(Icons.Default.Person, "Edit Profile", colorScheme.primary),
            SettingItemData(Icons.Default.Security, "Security", colorScheme.secondary),
            SettingItemData(Icons.Default.Notifications, "Notifications", colorScheme.tertiary),
            SettingItemData(Icons.Default.Lock, "Privacy", colorScheme.error)
        )

        // Ajouter "Recruter arbitre" seulement si OWNER
        if (currentUserRole == "OWNER") {
            listItems.add(
                SettingItemData(Icons.Default.PersonAdd, "Recruter arbitre", colorScheme.primary)
            )
            listItems.add(
                SettingItemData(Icons.Default.GroupAdd, "Recruter joueurs", colorScheme.primary)
            )
        }

        listItems
    }

    val supportAbout = remember(colorScheme) {
        listOf(
            SettingItemData(Icons.Default.CreditCard, "My Subscription", colorScheme.primary),
            // FIX: Icons.AutoMirrored.Filled.Help is correctly used here
            SettingItemData(Icons.AutoMirrored.Filled.Help, "Help & Support", colorScheme.secondary),
            SettingItemData(Icons.Default.Info, "Terms and Policies", colorScheme.tertiary)
        )
    }

    val cacheCellular = remember(colorScheme) {
        listOf(
            SettingItemData(Icons.Default.Delete, "Free up space", colorScheme.error),
            SettingItemData(Icons.Default.ColorLens, "Theme Selection", colorScheme.primary, isToggle = true),
            SettingItemData(Icons.Default.NetworkCell, "Data Saver", colorScheme.secondary, isToggle = true)
        )
    }

    val actions = remember(colorScheme) {
        listOf(
            SettingItemData(Icons.Default.Flag, "Report a problem", colorScheme.error, isAction = true),
            SettingItemData(Icons.Default.GroupAdd, "Add account", colorScheme.primary, isAction = true),
            SettingItemData(Icons.AutoMirrored.Filled.ExitToApp, "Log out", colorScheme.error, isAction = true)
        )
    }


    // --- End Data Definitions ---


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Profile", color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // FIX: Corrected capitalization from Icons.automirrored to Icons.AutoMirrored to resolve 'Unresolved reference'
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle notification click */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardSurfaceColor
                )
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBackgroundColor)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Profile Card ---
                item {
                    ProfileCard(
                        profileName = "Jhon Steward",
                        phoneNumber = "+88 01685007600",
                        department = "HR Department (Dhaka Office)",
                        avatarResId = R.drawable.trophy,
                        cardBackgroundColor = cardSurfaceColor,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 2. Account Settings (General) ---
                item {
                    SettingsSection(
                        title = "Account Settings",
                        items = accountSettings,
                        navController = navController,
                        darkTheme = darkTheme, // Required parameter
                        onLogoutClick = { /* N/A - Not an action item */ }, // Required parameter
                        onThemeToggle = { /* N/A - Not a toggle item */ } // Required parameter
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 3. Support & About ---
                item {
                    SettingsSection(
                        title = "Support & About",
                        items = supportAbout,
                        navController = navController,
                        darkTheme = darkTheme, // Required parameter
                        onLogoutClick = { /* N/A - Not an action item */ }, // Required parameter
                        onThemeToggle = { /* N/A - Not a toggle item */ } // Required parameter
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 4. Cache & Cellular ---
                item {
                    SettingsSection(
                        title = "Cache & cellular",
                        items = cacheCellular,
                        navController = navController,
                        darkTheme = darkTheme, // Required parameter
                        onLogoutClick = { /* N/A - Not an action item */ }, // Required parameter
                        onThemeToggle = onThemeToggle // Pass the actual handler
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- 5. Actions ---
                item {
                    SettingsSection(
                        title = "Actions",
                        items = actions,
                        navController = navController,
                        darkTheme = darkTheme, // Required parameter
                        onLogoutClick = { showLogoutDialog = true }, // Pass the actual handler
                        onThemeToggle = { /* N/A - Not a toggle item */ } // Required parameter
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    )

    // --- Logout Confirmation Dialog ---
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = {
                // Call logout to clear token and remember me preference
                viewModel.logout()
                // Navigate to LoginScreen
                navController.navigate("LoginScreen") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                showLogoutDialog = false
            }
        )
    }
}

// --- LogoutConfirmationDialog (Unchanged) ---

@Composable
fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirmLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "End Session",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out?",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Yes, End Session", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// --- SettingsSection (Updated signature) ---

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItemData>,
    navController: NavController,
    darkTheme: Boolean, // Added darkTheme
    onLogoutClick: () -> Unit, // Added onLogoutClick
    onThemeToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Settings Card Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEachIndexed { index, item ->
                SettingsListItem(
                    item = item,
                    navController = navController,
                    showDivider = index < items.lastIndex,
                    darkTheme = darkTheme, // Passed darkTheme
                    onLogoutClick = onLogoutClick,
                    onThemeToggle = onThemeToggle
                )
            }
        }
    }
}


// --- SettingsListItem (Updated logic) ---

@Composable
private fun SettingsListItem(
    item: SettingItemData,
    navController: NavController,
    showDivider: Boolean,
    darkTheme: Boolean, // Explicitly used for Theme Selection
    onLogoutClick: () -> Unit,
    onThemeToggle: () -> Unit
) {
    // State for local toggles (Data Saver, etc.)
    var isToggled by remember {
        mutableStateOf(
            when (item.title) {
                "Data Saver" -> false // Default state for Data Saver
                else -> false
            }
        )
    }

    // Determine the checked state for the switch item
    val switchCheckedState = if (item.title == "Theme Selection") darkTheme else isToggled

    val clickAction: () -> Unit = {
        if (item.title == "Theme Selection") {
            // Use the external handler for theme toggle
            onThemeToggle()
        } else if (item.isToggle) {
            // Local state management for non-theme toggles (like Data Saver)
            isToggled = !isToggled
        } else if (item.title == "Log out") {
            onLogoutClick()
        } else if (item.title == "Recruter arbitre") {
            navController.navigate("RecruteScreen")
        } else if (item.title == "Recruter joueurs") {
            navController.navigate("Negotiation")
        } else if (item.title == "Negotiation") {
            navController.navigate("Negotiation")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = clickAction)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(item.iconBgColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = item.title, tint = item.iconBgColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Trailing Content (Toggle or Arrow)
            when {
                item.isToggle -> {
                    Switch(
                        checked = switchCheckedState,
                        onCheckedChange = { clickAction() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                item.isAction -> {
                    // Action items like "Log out" don't get an arrow or toggle, but use the click action
                    Spacer(Modifier.width(20.dp))
                }
                else -> {
                    // Default for navigation items
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go to",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Divider
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp
            )
        }
    }
}


// --- ProfileCard (Unchanged) ---

@Composable
fun ProfileCard(
    profileName: String,
    phoneNumber: String,
    department: String,
    avatarResId: Int,
    cardBackgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = "Hello, $profileName",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Phone Number
            Text(
                text = phoneNumber,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Department (Chip)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = "Department Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = department,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// --- Preview (Unchanged) ---

@Preview(showBackground = true)
@Composable
fun ProfileScreenSettingsPreview() {
    DAMTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreenSettings(
                navController = rememberNavController(),
                darkTheme = false,
                onThemeToggle = {} // Provide empty lambda for preview
            )
        }
    }
}
