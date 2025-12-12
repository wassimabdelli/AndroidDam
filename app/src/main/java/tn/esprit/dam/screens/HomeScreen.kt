package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.esprit.dam.R
import tn.esprit.dam.screens.cards.GameEntry
import tn.esprit.dam.screens.cards.GameListItem
import tn.esprit.dam.screens.cards.MainGreenCard
import tn.esprit.dam.ui.theme.* // Keeping this to resolve dependencies if needed, but not using custom Colors directly
import tn.esprit.dam.components.HomeBottomNavigationBar
import android.net.Uri


// --- Data & Main Content Extracted (Unchanged) ---

// Dummy data for GameList
private val gameList = listOf(
    GameEntry("Esprit Stade", "18:00 - April 30", "16/32", "$400,00", R.drawable.stade1),
    GameEntry("TamTam Stade", "17:00 - April 24", "08/16", "$100,00", R.drawable.stade2),
    GameEntry("FC Stadium", "8:30 - April 15", "22/32", "$674,40", R.drawable.stade3)
)

// NEW: Extracted the main content Column to avoid Scaffold/NavHostController issues in preview
@Composable
fun HomeContent(paddingValues: PaddingValues = PaddingValues(0.dp), onGamePlanClick: (String) -> Unit = {}) {
    val topBackgroundColor = MaterialTheme.colorScheme.background
    val listBackgroundColor = MaterialTheme.colorScheme.surface

    // Make the whole content scrollable
    val context = LocalContext.current
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var currentUserNom by remember { mutableStateOf<String?>(null) }
    var currentUserPrenom by remember { mutableStateOf<String?>(null) }
    var currentUserRole by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as android.app.Application
            val repo = tn.esprit.dam.data.AuthRepository(app)
            val user = repo.getUser()
            currentUserId = user?._id
            currentUserNom = user?.nom
            currentUserPrenom = user?.prenom
            currentUserRole = user?.role
        } catch (_: Exception) {
            currentUserId = null
            currentUserNom = null
            currentUserPrenom = null
            currentUserRole = null
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(topBackgroundColor)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // Top part of the screen
        TopSection()
        StatsSection()

        // --- Bottom Column with rounded top corners for cards/list ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(listBackgroundColor) // Bottom part uses Surface (for contrast)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            MainGreenCard()
            if (currentUserRole == "OWNER") {
                val encodedId = Uri.encode(currentUserId ?: "")
                val encodedNom = Uri.encode(currentUserNom ?: "")
                val encodedPrenom = Uri.encode(currentUserPrenom ?: "")
                Card(
                    onClick = {
                        if (currentUserId != null) {
                            onGamePlanClick("PlanScreen/$encodedId/$encodedNom/$encodedPrenom")
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Game Plan",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Choisissez votre composition",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            TabBar()

            LazyColumn(
                modifier = Modifier.heightIn(min = 0.dp, max = 400.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(gameList) { game ->
                    GameListItem(game = game)
                }
            }
            // --- Shared Preferences Debug Section at the bottom ---
            SharedPrefsDebugSectionWithUser()
            DataStoreUserDebugSection()
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        // Ensure Scaffold background is set to a base color, usually background
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { HomeBottomNavigationBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            HomeContent(paddingValues, onGamePlanClick = { route -> navController.navigate(route) })
        }
    }
}

// --- Helper Composables (Colors changed to MaterialTheme) ---
@Composable
fun TopSection() {
    val context = LocalContext.current
    var playerName by remember { mutableStateOf("Player Name") }
    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as android.app.Application
            val repo = tn.esprit.dam.data.AuthRepository(app)
            val user = repo.getUser()
            playerName = if (user != null) {
                listOfNotNull(user.prenom, user.nom).joinToString(" ").ifBlank { user.email ?: "Player" }
            } else {
                "Player Name"
            }
        } catch (_: Exception) {
            playerName = "Player Name"
        }
    }
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hi, Welcome Back", color = onBackgroundColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(playerName, color = onBackgroundColor, fontSize = 16.sp)
        }
        Icon(
            Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = primaryColor, // Use Primary color
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun StatsSection() {
    // MAPPING: TextBlack -> onBackground
    // MAPPING: LossText -> MaterialTheme.colorScheme.error
    // MAPPING: MediumGreen -> MaterialTheme.colorScheme.primary
    // MAPPING: TextWhite (for Divider) -> MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f)
    // MAPPING: WinrateProgress (Color) -> MaterialTheme.colorScheme.secondary
    // MAPPING: DarkerGreen -> MaterialTheme.colorScheme.primary
    // MAPPING: LightGreen -> MaterialTheme.colorScheme.secondary

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Total Losses / Total Wins Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Losses
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Square, contentDescription = "Losses", tint = errorColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Losses", color = onBackgroundColor, fontSize = 14.sp)
                    Text("21", color = errorColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Vertical Divider (Central separator)
            HorizontalDivider(
                color = onBackgroundColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
            )

            // Total Wins
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Wins", tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Wins", color = onBackgroundColor, fontSize = 14.sp)
                    Text("63", color = primaryColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Winrate Progress Bar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Winrate", color = onBackgroundColor, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                color = secondaryColor, // Use Secondary for progress color
                // Use a subtle surface variant for the track
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text("%75", color = primaryColor, fontWeight = FontWeight.Bold) // Use Primary color
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = "Checked", tint = secondaryColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("75% Of Your Games, Looks Good.", color = onBackgroundColor, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun TabBar() {
    val tabs = listOf("Today", "This Week", "This Month")
    var selectedIndex by remember { mutableStateOf(2) }

    // MAPPING: DarkerGreen (Tab background) -> MaterialTheme.colorScheme.primaryContainer
    // MAPPING: TabSelected (Selected tab background) -> MaterialTheme.colorScheme.primary
    // MAPPING: TextWhite (Selected text color) -> MaterialTheme.colorScheme.onPrimary
    // MAPPING: TabUnselectedText (Unselected text color) -> MaterialTheme.colorScheme.onPrimaryContainer

    val tabRowBackground = MaterialTheme.colorScheme.primaryContainer
    val tabSelectedBackground = MaterialTheme.colorScheme.primary
    val tabSelectedTextColor = MaterialTheme.colorScheme.onPrimary
    val tabUnselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(tabRowBackground) // Use Primary Container for the background
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) tabSelectedBackground else Color.Transparent)
                    .padding(horizontal = 8.dp)
                    .clickable { selectedIndex = index },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) tabSelectedTextColor else tabUnselectedTextColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SharedPrefsDebugSectionWithUser() {
    val context = LocalContext.current
    var prefsDump by remember { mutableStateOf("Loading...") }
    var userInfo by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as android.app.Application
            val repo = tn.esprit.dam.data.AuthRepository(app)
            val token = repo.getToken() ?: "<none>"
            val rememberMe = repo.getRememberMe().toString()
            val pendingEmail = repo.getPendingVerificationEmail() ?: "<none>"
            val (forgotEmail, forgotCode) = repo.getForgotPasswordContext()
            val user = repo.getUser()
            prefsDump = "\n--- DataStore (Shared Preferences) ---\n" +
                "JWT Token: $token\n" +
                "Remember Me: $rememberMe\n" +
                "Pending Verification Email: $pendingEmail\n" +
                "Forgot Password Email: ${forgotEmail ?: "<none>"}\n" +
                "Forgot Password Code: ${forgotCode ?: "<none>"}"
            userInfo = if (user != null) {
                "ID: ${user._id}\n" +
                "First Name: ${user.prenom}\n" +
                "Last Name: ${user.nom}\n" +
                "Email: ${user.email}\n" +
                "Birth Date: ${user.age}\n" +
                "Phone: ${user.tel}\n" +
                "Role: ${user.role}\n" +
                "Email Verified: ${user.emailVerified}\n" +
                "Is Verified: ${user.isVerified}"
            } else null
        } catch (e: Exception) {
            prefsDump = "Error reading shared preferences: ${e.message}"
            userInfo = null
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Shared Preferences Debug", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(prefsDump, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("User Info", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            if (userInfo != null) {
                Text(userInfo!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("No user info available.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DataStoreUserDebugSection() {
    val context = LocalContext.current
    var userInfo by remember { mutableStateOf("Chargement...") }
    LaunchedEffect(Unit) {
        try {
            val app = context.applicationContext as android.app.Application
            val repo = tn.esprit.dam.data.AuthRepository(app)
            val user = repo.getUser()
            userInfo = if (user != null) {
                "ID: ${user._id}\n" +
                "Prénom: ${user.prenom}\n" +
                "Nom: ${user.nom}\n" +
                "Email: ${user.email}\n" +
                "Date de naissance: ${user.age}\n" +
                "Téléphone: ${user.tel}\n" +
                "Rôle: ${user.role}\n" +
                "Email vérifié: ${user.emailVerified}\n" +
                "Compte vérifié: ${user.isVerified}"
            } else {
                "Aucune information utilisateur enregistrée."
            }
        } catch (e: Exception) {
            userInfo = "Erreur lors de la lecture du DataStore: ${e.message}"
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User DataStore Debug", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(userInfo, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


// --- PREVIEW FUNCTION ---
@Preview(showBackground = true, name = "Home Screen Final")
@Composable
fun HomeScreenPreview() {
    // Wrap the content in your theme/surface for correct color rendering
    DAMTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeContent()
        }
    }
}