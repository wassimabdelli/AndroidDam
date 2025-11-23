package tn.esprit.dam.screens

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import tn.esprit.dam.components.AnimatedCard
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController // Import NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.esprit.dam.api.TournamentApiService
import tn.esprit.dam.data.AuthRepository
import tn.esprit.dam.data.RetrofitClient
import tn.esprit.dam.ui.theme.DAMTheme
import tn.esprit.dam.ui.theme.WinrateProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import tn.esprit.dam.components.HomeBottomNavigationBar // <-- New Import
import tn.esprit.dam.components.Screen

// --- 1. Data Structures ---
data class StatItem(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

data class TabItem(
    val label: String,
    val count: Int,
    val isSelected: Boolean
)

data class RegisteredTeam(
    val rank: Int,
    val name: String,
    val rating: Float,
    val wins: Int
)

data class Event(
    val id: String = "", // Added ID for keying
    val title: String,
    val host: String,
    val type: String,
    val typeIcon: ImageVector,
    val headerColor: Color,
    val location: String,
    val date: String,
    val time: String,
    val playersJoined: Int,
    val playersMax: Int,
    val entryFee: Int,
    val prizePool: Int,
    val registeredTeams: List<String>, // Changed to List<String> to match your API usage
    val rules: List<String>,
    val matches: List<Any>, // Added matches
    // NEW FIELD: To support "My Events" filtering
    val isOrganizer: Boolean = false,
    val isBracketGenerated: Boolean = false
)

// --- 2. Static Data (remove all static eventData and sample events) ---

val CardBlue = Color(0xFF64B5F6)
val CardOrange = Color(0xFFFFB74D)
val CardPurple = Color(0xFF9575CD)
val PriceGreen = Color(0xFF85E4A0)

private val statData = listOf(
    StatItem("12", "Wins", Icons.Rounded.EmojiEvents, Color(0xFF60B17A), Color(0xFFE8F5E9)),
    StatItem("8", "Active", Icons.Filled.Place, Color(0xFF03A9F4), Color(0xFFE1F5FE)),
    StatItem("75%", "Win Rate", Icons.Rounded.FlashOn, Color(0xFFFF9800), Color(0xFFFFF8E1))
)


// --- 3. Main Screen Composables (omitted for brevity, assume content remains the same) ---
@Composable
fun StatCard(item: StatItem) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val isDarkTheme = isSystemInDarkTheme()

    val cardBg = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else surfaceColor
    val iconBg = if (isDarkTheme) item.iconColor.copy(alpha = 0.2f) else item.bgColor

    Card(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.iconColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                Text(
                    text = item.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Search tournaments or stadiums...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        trailingIcon = {
            IconButton(onClick = { /* Handle filter click */ }) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabChip(item: TabItem, onSelected: (String) -> Unit) {
    val isSelected = item.isSelected
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline

    FilterChip(
        selected = isSelected,
        onClick = { onSelected(item.label) },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.label, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                Spacer(Modifier.width(4.dp))
                Text(
                    "(${item.count})",
                    fontWeight = FontWeight.Light,
                    color = if (isSelected) primaryColor else outlineColor
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) primaryColor else outlineColor.copy(alpha = 0.5f)),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = onSurfaceColor,
            selectedLabelColor = primaryColor
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else Color.Black
    val cardElevation = if (isSystemInDarkTheme()) 1.dp else 4.dp
    val playerRatio = event.playersJoined.toFloat() / event.playersMax.toFloat()

    AnimatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface, // Card body always uses surface color
        defaultElevation = cardElevation,
        pressedElevation = if (isSystemInDarkTheme()) 4.dp else 8.dp
    ) {
        Column {
            // --- Card Header (Colored Box) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(event.headerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title and Host
                    Column {
                        Text(
                            text = event.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = event.typeIcon,
                                contentDescription = event.type,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = event.host, // Show organizer name under the title
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            // --- Card Body (Surface Color) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DetailRow(
                    icon = Icons.Filled.LocationOn,
                    text = event.location,
                    textColor = textColor
                )
                Spacer(Modifier.height(8.dp))
                DetailRow(
                    icon = Icons.Filled.CalendarToday,
                    text = "${event.date} • ${event.time}",
                    textColor = textColor
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailRow(
                        icon = Icons.Filled.People,
                        text = "${event.playersJoined}/${event.playersMax} Players",
                        textColor = textColor,
                        iconTint = outlineColor
                    )
                    // Use prizePool for the price tag
                    if (event.prizePool > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriceGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$${event.prizePool}",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = playerRatio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = WinrateProgress,
                    trackColor = outlineColor.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${(playerRatio * 100).toInt()}% Full",
                        fontSize = 10.sp,
                        color = outlineColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, text: String, textColor: Color, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

// --- 4. Main Screen Container (Updated for Navigation Bar) ---

@Composable
fun EventsScreen(navController: NavHostController) { // <-- Changed type to NavHostController
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showMatchCoupeScreen by remember { mutableStateOf(false) }

    Crossfade(targetState = selectedEvent, label = "ScreenCrossfade") { event ->
        when {
            showMatchCoupeScreen && event != null -> {
                MatchCoupeScreen(coupeId = event.id, matches = event.matches, onBackClick = {
                    showMatchCoupeScreen = false
                })
            }
            event == null -> {
                // Event List View (Show Navigation Bar)
                EventsScreenContent(
                    navController = navController,
                    onEventClick = { clickedEvent ->
                        selectedEvent = clickedEvent
                    }
                )
            }
            else -> {
                // Event Detail View (Hide Navigation Bar)
                EventDetailScreen(
                    event = event,
                    onBackClick = {
                        selectedEvent = null
                    },
                    onNavigateToMatches = {
                        showMatchCoupeScreen = true
                    }
                )
            }
        }
    }
}

// --- 5. Events List Content (Updated for Search and Bottom Nav) ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreenContent(navController: NavHostController, onEventClick: (Event) -> Unit) {
    val scrollState = rememberScrollState()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    var selectedType by remember { mutableStateOf("All Events") }
    var searchQuery by remember { mutableStateOf("") }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch events from API on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val repo = AuthRepository(context.applicationContext as android.app.Application)
                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                // Get current user ID to verify ownership
                val currentUser = withContext(Dispatchers.IO) { repo.getUser() }
                val currentUserId = currentUser?._id

                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                val response = api.getCoupesWithAuth("Bearer $jwt")

                if (response.isSuccessful) {
                    val coupes = response.body() ?: emptyList()
                    events = coupes.map { coupe ->
                        val participantsCount = coupe.participants.size
                        val cardColor = when (coupe.type) {
                            "Tournament" -> CardPurple
                            "League" -> CardOrange
                            else -> CardPurple
                        }
                        val isOrganizer = coupe.id_organisateur?._id == currentUserId
                        Event(
                            id = coupe._id, // Map the ID
                            title = coupe.tournamentName,
                            host = coupe.nom,
                            type = coupe.type,
                            typeIcon = Icons.Filled.EmojiEvents,
                            headerColor = cardColor,
                            location = coupe.stadium,
                            date = coupe.date.take(10),
                            time = coupe.time,
                            playersJoined = participantsCount,
                            playersMax = coupe.maxParticipants,
                            entryFee = coupe.entryFee ?: 0,
                            prizePool = coupe.prizePool ?: 0,
                            registeredTeams = emptyList(), // Or map participant IDs if available immediately
                            rules = emptyList(), // Add rules if your API supports it
                            matches = coupe.matches, // Pass matches
                            isOrganizer = isOrganizer, // Set the organizer flag
                            isBracketGenerated = coupe.isBracketGenerated
                        )
                    }
                } else {
                    errorMessage = "Failed to load events: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- FILTERING LOGIC START ---
    // 1. Define the fixed categories + "My Events"
    val categories = listOf("All Events", "My Events", "Tournament", "League")

    // 2. Update Filter Logic
    val tabFilteredEvents = when (selectedType) {
        "All Events" -> events
        "My Events" -> events.filter { it.isOrganizer }
        else -> events.filter { it.type == selectedType }
    }

    // 3. Apply Search Filter
    val filteredEvents = if (searchQuery.isBlank()) {
        tabFilteredEvents
    } else {
        val lowerCaseQuery = searchQuery.lowercase(Locale.getDefault())
        tabFilteredEvents.filter { event ->
            event.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    event.location.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    event.host.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }
    }

    // 4. Recalculate Counts for Tabs
    val calculatedTabData = categories.map { category ->
        val count = when (category) {
            "All Events" -> events.size
            "My Events" -> events.count { it.isOrganizer }
            else -> events.count { it.type == category }
        }
        TabItem(label = category, count = count, isSelected = category == selectedType)
    }
    // --- FILTERING LOGIC END ---

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Apply content padding provided by the Scaffold
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Welcome Back! ⚽",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Text(
                text = "Find your next match",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statData.forEach { StatCard(it) }
            }
            Spacer(Modifier.height(24.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    // Don't reset the selected tab when searching, just filter within it
                }
            )
            Spacer(Modifier.height(16.dp))

            // Category Tabs
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calculatedTabData) { item ->
                    TabChip(item) { type ->
                        selectedType = type
                        searchQuery = ""
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "$selectedType (${filteredEvents.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceColor
            )
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (filteredEvents.isEmpty()) {
                        Text(
                            text = "No events found.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(20.dp)
                        )
                    } else {
                        filteredEvents.forEachIndexed { index, event ->
                            EventCard(event, onClick = { onEventClick(event) })
                            if (index < filteredEvents.lastIndex) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// --- 6. Event Detail Screen (Navigation Bar is omitted here, as requested) ---

@Composable
fun EventDetailScreen(event: Event, onBackClick: () -> Unit, onNavigateToMatches: () -> Unit) {
    val scrollState = rememberScrollState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var participantNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingParticipants by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var isGeneratingBracket by remember { mutableStateOf(false) }
    var isBracketGenerated by remember { mutableStateOf(event.isBracketGenerated) }
    var isUserAnArbitre by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val repo = AuthRepository(context.applicationContext as android.app.Application)
        val user = withContext(Dispatchers.IO) { repo.getUser() }
        currentUserId = user?._id
    }

    var coupeId by remember { mutableStateOf<String?>(null) }
    var coupeOwnerId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(event, currentUserId) {
        coroutineScope.launch {
            isLoadingParticipants = true
            try {
                val repo = AuthRepository(context.applicationContext as android.app.Application)
                val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                val coupesResponse = api.getCoupesWithAuth("Bearer $jwt")
                val coupe = coupesResponse.body()?.find {
                    it.tournamentName == event.title && it.nom == event.host
                }
                coupeId = coupe?._id
                coupeOwnerId = coupe?.id_organisateur?._id
                isBracketGenerated = coupe?.isBracketGenerated ?: false
                if (currentUserId != null && coupeOwnerId != null) {
                    val arbitreResponse = api.isArbitreInAcademie(coupeOwnerId!!, currentUserId!!, "Bearer $jwt")
                    if (arbitreResponse.isSuccessful) {
                        isUserAnArbitre = arbitreResponse.body()?.exists ?: false
                    }
                }

                val participantIds = coupe?.participants?.mapNotNull {
                    when (it) {
                        is String -> it
                        is Map<*, *> -> it["\$oid"] as? String // Use string literal for key
                        else -> null
                    }
                } ?: emptyList()
                val names = participantIds.mapNotNull { id ->
                    val response = api.getUserByIdWithAuth(id, "Bearer $jwt")
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) listOfNotNull(user.prenom, user.nom).joinToString(" ") else null
                    } else null
                }
                participantNames = names
            } catch (_: Exception) {
                participantNames = emptyList()
            } finally {
                isLoadingParticipants = false
            }
        }
    }

    val formattedDate = remember(event.date) {
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            displayFormat.timeZone = TimeZone.getDefault()
            val date = isoFormat.parse(event.date)
            if (date != null) displayFormat.format(date) else event.date
        } catch (e: Exception) {
            event.date
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(event.headerColor)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    IconButton(
                        onClick = { /* Handle Share */ },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = event.type,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.host,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(surfaceColor)
                    .padding(20.dp)
            ) {
                Text(
                    text = event.location,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Spacer(Modifier.height(24.dp))

                DetailInfoRow(icon = Icons.Filled.CalendarToday, title = "Date", value = formattedDate)
                DetailInfoRow(icon = Icons.Filled.Schedule, title = "Time", value = event.time)
                DetailInfoRow(icon = Icons.Filled.People, title = "Participants", value = "${event.playersJoined}/${event.playersMax} Teams")
                DetailInfoRow(icon = Icons.Filled.AttachMoney, title = "Entry Fee", value = "$${event.entryFee}")
                DetailInfoRow(icon = Icons.Filled.EmojiEvents, title = "Prize Pool", value = "$${event.prizePool}", isLast = true)

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Registered Teams (${event.playersJoined})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Spacer(Modifier.height(16.dp))
                if (isLoadingParticipants) {
                    CircularProgressIndicator()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        participantNames.forEachIndexed { idx, name ->
                            TeamRowSimple(name = name, index = idx)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                RulesCard(rules = event.rules)
                Spacer(Modifier.height(16.dp))
                var hasJoined by remember { mutableStateOf(false) }
                LaunchedEffect(currentUserId, coupeId, participantNames) {
                    if (currentUserId != null && coupeId != null) {
                        val repo = AuthRepository(context.applicationContext as android.app.Application)
                        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                        val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                        val coupesResponse = api.getCoupesWithAuth("Bearer $jwt")
                        val coupe = coupesResponse.body()?.find { it._id == coupeId }
                        val participantIds = coupe?.participants?.mapNotNull {
                            when (it) {
                                is String -> it
                                is Map<*, *> -> it["\$oid"] as? String // FIX: Use string literal for key
                                else -> null
                            }
                        } ?: emptyList()
                        hasJoined = participantIds.contains(currentUserId)
                    }
                }
                if (currentUserId != null && coupeId != null) {
                    val isOwner = currentUserId == coupeOwnerId
                    val buttonColor = when {
                        isOwner -> MaterialTheme.colorScheme.error
                        hasJoined -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val buttonText = when {
                        isOwner -> "Edit"
                        hasJoined -> "Joined"
                        else -> "Join"
                    }
                    val buttonEnabled = !isOwner && !hasJoined
                    Button(
                        onClick = {
                            if (!isOwner && !hasJoined) {
                                coroutineScope.launch {
                                    val repo = AuthRepository(context.applicationContext as android.app.Application)
                                    val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                    val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                    val response = api.addParticipantWithAuth(
                                        coupeId!!,
                                        tn.esprit.dam.api.AddParticipantRequest(userId = currentUserId!!),
                                        "Bearer $jwt"
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "You have joined the tournament!", Toast.LENGTH_LONG).show()
                                        hasJoined = true
                                    } else if (response.code() == 409) {
                                        Toast.makeText(context, "You are already a participant.", Toast.LENGTH_LONG).show()
                                        hasJoined = true
                                    } else {
                                        Toast.makeText(context, "Failed to join tournament: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(buttonText, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (isBracketGenerated) {
                    Button(
                        onClick = onNavigateToMatches,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Voir matchs", fontWeight = FontWeight.Bold)
                    }
                } else if (isUserAnArbitre) {
                    Button(
                        onClick = {
                            if (coupeId != null) {
                                coroutineScope.launch {
                                    isGeneratingBracket = true
                                    try {
                                        val repo = AuthRepository(context.applicationContext as android.app.Application)
                                        val jwt = withContext(Dispatchers.IO) { repo.getToken() }
                                        val api = RetrofitClient.getRetrofit(context).create(TournamentApiService::class.java)
                                        val response = api.generateBracket(coupeId!!, "Bearer $jwt")

                                        if (response.isSuccessful) {
                                            isBracketGenerated = true
                                            Toast.makeText(context, "Calendrier généré avec succès!", Toast.LENGTH_LONG).show()
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                            Toast.makeText(context, "Failed to generate bracket: $errorBody", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingBracket = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isBracketGenerated && !isGeneratingBracket
                    ) {
                        if (isGeneratingBracket) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Générer la calendrier", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// Helper to get the coupe ID for the event (by matching title and host)
fun coupeIdForEvent(event: Event): String? {
    // This should be improved to use a more reliable mapping if available
    // For now, use a static mapping or pass the ID in the Event if possible
    return null // TODO: Implement actual mapping
}

// --- 7. Detail Screen Helpers (omitted for brevity, assume content remains the same) ---

@Composable
fun DetailInfoRow(icon: ImageVector, title: String, value: String, isLast: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Column
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Text Column (takes remaining space)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        // Divider
        if (!isLast) {
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
        }
    }
}


@Composable
fun TeamRow(team: RegisteredTeam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${team.rank}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${team.rating} • ${team.wins} wins",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun TeamRowSimple(name: String, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RulesCard(rules: List<String>) {
    val cardBg = if (isSystemInDarkTheme()) CardBlue.copy(alpha = 0.1f) else Color(0xFFE3F2FD)
    val iconBg = if (isSystemInDarkTheme()) CardBlue.copy(alpha = 0.3f) else Color.White
    val iconTint = Color(0xFF1E88E5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, CardBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Rules",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Tournament Rules",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rules.forEach { rule ->
                    Row {
                        Text("• ", color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = rule,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}


// --- 8. Previews ---

@Preview(showBackground = true, name = "Events List (Light)")
@Composable
fun EventsScreenPreviewLight() {
    DAMTheme(darkTheme = false) {
        // Use rememberNavController() for previews
        EventsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Events List (Dark)")
@Composable
fun EventsScreenPreviewDark() {
    DAMTheme(darkTheme = true) {
        EventsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Event Detail (Light)")
@Composable
fun EventDetailPreviewLight() {
    DAMTheme(darkTheme = false) {
        EventDetailScreen(event = Event(
            title = "Preview Event",
            host = "Preview Host",
            type = "PROGRAMME",
            typeIcon = Icons.Filled.EmojiEvents,
            headerColor = CardPurple,
            location = "Preview Stadium",
            date = "2025-01-01",
            time = "14:00",
            playersJoined = 0,
            playersMax = 32,
            entryFee = 0,
            prizePool = 0,
            registeredTeams = emptyList(),
            rules = emptyList(),
            matches = emptyList() // Added for preview
        ), onBackClick = {}, onNavigateToMatches = {})
    }
}

@Preview(showBackground = true, name = "Event Detail (Dark)")
@Composable
fun EventDetailPreviewDark() {
    DAMTheme(darkTheme = true) {
        EventDetailScreen(event = Event(
            title = "Preview Event",
            host = "Preview Host",
            type = "PROGRAMME",
            typeIcon = Icons.Filled.EmojiEvents,
            headerColor = CardPurple,
            location = "Preview Stadium",
            date = "2025-01-01",
            time = "14:00",
            playersJoined = 0,
            playersMax = 32,
            entryFee = 0,
            prizePool = 0,
            registeredTeams = emptyList(),
            rules = emptyList(),
            matches = emptyList() // Added for preview
        ), onBackClick = {}, onNavigateToMatches = {})
    }
}
