package tn.esprit.dam.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import tn.esprit.dam.components.AnimatedCard
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.ui.theme.DAMTheme

// --- Data Structures ---

data class Player(
    val id: Int,
    val initials: String,
    val color: Color,
    val name: String,
    val number: Int,
    val position: String,
    val rating: Double,
    val isCaptain: Boolean = false
)

data class Team(
    val id: Int,
    val name: String,
    val tagline: String,
    val members: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Int,
    val establishedYear: Int,
    val isCaptain: Boolean,
    val colorStart: Color,
    val colorEnd: Color,
    val logoIcon: @Composable () -> Unit,
    val roster: List<Player> = emptyList(),
    val stats: TeamStats = TeamStats(),
    val formations: List<String> = listOf("4-3-3", "4-4-2", "3-5-2"),
    val selectedFormation: String = "4-3-3"
)

data class TeamInvite(
    val id: Int,
    val teamName: String,
    val inviter: String,
    val teamMembers: Int,
    val colorStart: Color,
    val colorEnd: Color
)

data class DiscoverTeam(
    val id: Int,
    val name: String,
    val members: Int,
    val tagline: String,
    val wins: Int,
    val winRate: Int,
    val colorStart: Color,
    val colorEnd: Color,
    val isRequested: Boolean = false
)

data class TeamStats(
    val goalsScored: Int = 142,
    val goalsConceded: Int = 58,
    val cleanSheets: Int = 18,
    val possessionAvg: Int = 64, // percentage
    val topScorerName: String = "Blake Stone",
    val topScorerGoals: Int = 24,
    val mostAssistsName: String = "Avery Quinn",
    val mostAssistsCount: Int = 18
)

// --- Chat Data Structures ---
data class ChatMessage(
    val id: Int,
    val senderId: Int,
    val message: String,
    val time: String,
    val isSelf: Boolean = false // If the message is sent by the current user
)


// --- Colors and Constants ---
val CaptainColor = Color(0xFFE5B01E) // Gold for Captain tag/crown
val MemberCountColor = Color(0xFFE0E0E0) // Light grey text on cards
val StatLabelColor = Color(0xFFB0BEC5) // Light grey text for Wins/LossES/Rate labels
val StatValueColor = Color(0xFFFFFFFF) // White text for stat values
private val TeamAcceptButtonColor = Color(0xFF4CAF50) // Green for accept button
private val TeamDeclineButtonColor = Color(0xFFF44336) // Red for decline button

// Assuming these were defined globally or in another file, keeping them here for TeamsScreen.kt
val RatingColor = Color(0xFFFFC107) // Yellow for ratings
val ChatHeaderColor = Color(0xFF66BB6A) // Green from the chat screenshot
val SelfChatBubbleColor = Color(0xFF66BB6A)
val OtherChatBubbleColor = Color(0xFFE0E0E0)
val ChatTextColor = Color(0xFF333333)

// --- Mock Data ---

// Player data used for Roster and Formation tabs
val phoenixRoster = listOf(
    Player(1, "AR", Color(0xFF66BB6A), "Alex Rodriguez", 1, "GK", 4.8, true), // Captain (AR)
    Player(2, "MS", Color(0xFF66BB6A), "Marcus Silva", 4, "CB", 4.6), // MS
    Player(3, "JL", Color(0xFF66BB6A), "Jordan Lee", 5, "CB", 4.5), // JL
    Player(4, "ST", Color(0xFF66BB6A), "Sam Taylor", 3, "LB", 4.4),
    Player(5, "RB", Color(0xFF66BB6A), "Riley Brooks", 2, "RB", 4.3),
    Player(6, "CJ", Color(0xFF66BB6A), "Casey Jordan", 6, "CDM", 4.5),
    Player(7, "AQ", Color(0xFF66BB6A), "Avery Quinn", 8, "CM", 4.7),
    Player(8, "BS", Color(0xFF66BB6A), "Blake Stone", 10, "CAM", 4.9),
    Player(9, "DB", Color(0xFF66BB6A), "David Brown", 7, "LW", 4.6),
    Player(10, "TK", Color(0xFF66BB6A), "Tony Kemp", 11, "RW", 4.4),
    Player(11, "ZB", Color(0xFF66BB6A), "Zoe Baker", 9, "ST", 4.7)
)

val initialMyTeams = listOf(
    Team(
        id = 1,
        name = "Thunder FC",
        tagline = "The Speed of the Storm",
        members = 11,
        wins = 24,
        losses = 8,
        winRate = 75,
        establishedYear = 2024,
        isCaptain = true,
        colorStart = Color(0xFF42A5F5),
        colorEnd = Color(0xFF1E88E5),
        logoIcon = { Icon(Icons.Filled.FlashOn, contentDescription = "Flash", tint = Color.White, modifier = Modifier.size(32.dp)) }
    ),
    Team(
        id = 2,
        name = "Phoenix Rising",
        tagline = "Born from the Ashes",
        members = 11, // Updated member count to 11 to match chat screenshot
        wins = 18,
        losses = 10,
        winRate = 64,
        establishedYear = 2023,
        isCaptain = false, // Current user is NOT the captain for this team
        colorStart = Color(0xFFFF9800), // Orange
        colorEnd = Color(0xFFF44336), // Red
        logoIcon = { Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Fire", tint = Color.White, modifier = Modifier.size(32.dp)) },
        roster = phoenixRoster,
        stats = TeamStats(),
    ),
)

val initialInvites = listOf(
    TeamInvite(3, "The Titans", "Invited by @john.d", 15, Color(0xFF66BB6A), Color(0xFF4CAF50)),
    TeamInvite(4, "Storm Breakers", "Invited by @team.lead", 8, Color(0xFF9C27B0), Color(0xFF8E24AA))
)

val initialDiscoverTeams = listOf(
    DiscoverTeam(5, "The Dragons", 10, "Seeking powerful strikers", 30, 85, Color(0xFFFDD835), Color(0xFFFFB300)),
)

val mockChatMessages = listOf(
    ChatMessage(1, 2, "Team meeting at 6 PM today!", "9:00 AM", false), // MS (Marcus Silva)
    ChatMessage(2, 0, "I'll be there!", "9:05 AM", true), // Self
    ChatMessage(3, 3, "Can we discuss the new formation?", "9:10 AM", false), // JL (Jordan Lee)
    ChatMessage(4, 7, "Great idea! I have some suggestions", "9:15 AM", false), // AQ (Avery Quinn - using 7 for a different user)
)

// --- State Management ---

class TeamState(
    initialMyTeams: List<Team>,
    initialInvites: List<TeamInvite>,
    initialDiscoverTeams: List<DiscoverTeam>
) {
    var selectedTab by mutableStateOf("My Teams")
    var currentScreen by mutableStateOf<Screen>(Screen.TeamsList)
    var selectedTeamId by mutableStateOf<Int?>(null)

    val myTeams = mutableStateListOf(*initialMyTeams.toTypedArray())
    val invites = mutableStateListOf(*initialInvites.toTypedArray())
    val discoverTeams = mutableStateListOf(*initialDiscoverTeams.toTypedArray())

    fun navigateToDetail(teamId: Int) {
        selectedTeamId = teamId
        currentScreen = Screen.TeamDetail
    }

    // NEW: Navigation to Chat Screen
    fun navigateToChat(teamId: Int) {
        selectedTeamId = teamId
        currentScreen = Screen.TeamChat
    }

    fun navigateBack() {
        currentScreen = Screen.TeamsList
        selectedTeamId = null
    }

    // Existing team logic
    fun acceptInvite(invite: TeamInvite) {
        invites.remove(invite)
        val newTeam = Team(
            id = invite.id, name = invite.teamName, tagline = "New Member", members = invite.teamMembers + 1,
            wins = 0, losses = 0, winRate = 0, establishedYear = 2025, isCaptain = false,
            colorStart = invite.colorStart, colorEnd = invite.colorEnd,
            logoIcon = { Icon(Icons.Filled.People, contentDescription = "People", tint = Color.White, modifier = Modifier.size(32.dp)) }
        )
        myTeams.add(newTeam)
    }
    fun declineInvite(invite: TeamInvite) { invites.remove(invite) }
    fun sendRequestToJoin(team: DiscoverTeam) {
        val index = discoverTeams.indexOfFirst { it.id == team.id }
        if (index != -1) { discoverTeams[index] = team.copy(isRequested = true) }
    }
}

// UPDATED: Added TeamChat screen state
sealed class Screen {
    data object TeamsList : Screen()
    data object TeamDetail : Screen()
    data object TeamChat : Screen() // NEW
}


// --- Main Screen Composable (Entry Point) ---

@Composable
fun TeamsScreen(navController: NavHostController) {
    val teamState = remember { TeamState(initialMyTeams, initialInvites, initialDiscoverTeams) }

    // Logic to switch between the list view and the detail view
    when (teamState.currentScreen) {
        Screen.TeamsList -> TeamsListScreen(navController, teamState)
        Screen.TeamDetail -> {
            val team = teamState.myTeams.find { it.id == teamState.selectedTeamId }
            if (team != null) {
                // Pass navigateToChat function to TeamDetailScreen
                TeamDetailScreen(
                    team = team,
                    onBack = teamState::navigateBack,
                    onTeamChat = { teamState.navigateToChat(team.id) }
                )
            } else {
                TeamsListScreen(navController, teamState)
            }
        }
        // NEW: Team Chat Screen Entry
        Screen.TeamChat -> {
            val team = teamState.myTeams.find { it.id == teamState.selectedTeamId }
            if (team != null) {
                TeamChatScreen(team = team, onBack = { teamState.currentScreen = Screen.TeamDetail })
            } else {
                TeamsListScreen(navController, teamState)
            }
        }
    }
}


// --- Team Detail Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(team: Team, onBack: () -> Unit, onTeamChat: () -> Unit) { // Added onTeamChat
    var selectedDetailTab by remember { mutableStateOf("Formation") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            item {
                TeamDetailHeader(team = team, onBack = onBack)
            }

            item {
                // Pass the new action handler to the navigation
                TeamDetailNavigation(
                    selectedTab = selectedDetailTab,
                    onTabSelected = { selectedDetailTab = it },
                    onTeamChat = onTeamChat // Passed the action here
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    when (selectedDetailTab) {
                        "Formation" -> TeamFormationContent(team)
                        "Roster" -> TeamRosterContent(team.roster)
                        "Stats" -> TeamStatsContent(team.stats)
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// --- Team Detail Navigation (Action Buttons) ---

@Composable
fun TeamDetailNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onTeamChat: () -> Unit // NEW: Action for Team Chat
) {
    val detailTabs = listOf("Formation", "Roster", "Stats")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Pulls this card up over the header
            .offset(y = (-30).dp)
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        // Action Buttons (Team Chat & Invite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButton(
                icon = Icons.Filled.Chat,
                text = "Team Chat",
                modifier = Modifier.weight(1f),
                onClick = onTeamChat // Hooked up the click handler
            )
            ActionButton(Icons.Filled.PersonAdd, "Invite", Modifier.weight(1f))
        }

        // Main Detail Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            detailTabs.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(60.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
    }
}

// --- Action Button Helper (Updated to take onClick) ---
@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Added onClick parameter
) {
    AnimatedCard(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        defaultElevation = 2.dp,
        pressedElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}


// --- NEW: Team Chat Screen Implementation ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamChatScreen(team: Team, onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ChatTopBar(team = team, onBack = onBack)
        },
        bottomBar = {
            ChatInputBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Chat messages section
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) // Light background for the chat area
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            reverseLayout = true // Start from the bottom
        ) {
            // Display messages in reverse order for chat feel
            items(mockChatMessages.reversed(), key = { it.id }) { message ->
                ChatMessageBubble(message = message, team = team)
            }
        }
    }
}

@Composable
fun ChatTopBar(team: Team, onBack: () -> Unit) {
    // Mimics the green header from the screenshot
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatHeaderColor)
            .statusBarsPadding()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Team Icon (Flash) and Name
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Icon/Logo
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.FlashOn, contentDescription = "Flash", tint = Color.White, modifier = Modifier.size(20.dp)) }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(team.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("${team.members} members", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        // Video Call and Options Buttons
        IconButton(onClick = { /* Video Call Action */ }) {
            Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = Color.White)
        }
        IconButton(onClick = { /* Options Menu */ }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = Color.White)
        }
    }
}

@Composable
fun ChatInputBar() {
    // Simple input field at the bottom
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var messageInput by remember { mutableStateOf("") }

        OutlinedTextField(
            value = messageInput,
            onValueChange = { messageInput = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message team...") },
            trailingIcon = {
                // Placeholder for sending attachments/media
                IconButton(onClick = { /* Attachments */ }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
            )
        )

        // Send Button
        IconButton(
            onClick = {
                if (messageInput.isNotBlank()) {
                    // Logic to send message (Not implemented here, just resets input)
                    println("Sending message: $messageInput")
                    messageInput = ""
                }
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SelfChatBubbleColor)
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage, team: Team) {
    val sender = team.roster.find { it.id == message.senderId }
    val isSelf = message.isSelf

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        // If not self, display avatar and message
        if (!isSelf) {
            val user = sender ?: Player(0, "AC", Color.Gray, "Unknown User", 0, "", 0.0) // Fallback for AC (Alex Chen)

            // Avatar (using initials for simplicity)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(user.color),
                contentAlignment = Alignment.Center
            ) {
                Text(user.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.width(8.dp))
        } else {
            // Spacer to push self message bubble to the end
            Spacer(Modifier.weight(0.1f))
        }

        // Message Bubble
        Column(
            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(0.9f, fill = false) // Allow content to dictate width
        ) {
            // Sender Name (Only for non-self messages)
            if (!isSelf && sender != null) {
                Text(
                    text = sender.name,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp, start = 8.dp)
                )
            }

            Card(
                shape = if (isSelf) {
                    RoundedCornerShape(20.dp, 20.dp, 8.dp, 20.dp)
                } else {
                    RoundedCornerShape(20.dp, 20.dp, 20.dp, 8.dp)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelf) SelfChatBubbleColor else OtherChatBubbleColor,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isSelf) Color.White else ChatTextColor,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            // Time Stamp
            Text(
                text = message.time,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, end = if (isSelf) 8.dp else 0.dp, start = if (!isSelf) 8.dp else 0.dp)
            )
        }
    }
}


// --- Remaining Helper Components (TeamsListScreen, TeamCard, etc.) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsListScreen(navController: NavHostController, teamState: TeamState) {
    val tabs = listOf(
        "My Teams (${teamState.myTeams.size})",
        "Invites (${teamState.invites.size})",
        "Discover"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings/Filter */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigate to Create Team Screen */ },
                containerColor = Color(0xFF4CAF50),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Team", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Teams",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Manage your squads",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEach { tab ->
                    val label = tab.substringBefore(" (")
                    CustomTeamTab(
                        label = tab,
                        isSelected = teamState.selectedTab == label,
                        onClick = { teamState.selectedTab = label }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when (teamState.selectedTab) {
                "My Teams" -> MyTeamsContent(teamState.myTeams, teamState::navigateToDetail)
                "Invites" -> TeamInvitesContent(
                    invites = teamState.invites,
                    onAccept = teamState::acceptInvite,
                    onDecline = teamState::declineInvite
                )
                "Discover" -> DiscoverTeamsContent(
                    teams = teamState.discoverTeams,
                    onJoinRequest = teamState::sendRequestToJoin
                )
            }
        }
    }
}

@Composable
fun MyTeamsContent(teams: List<Team>, onTeamClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (teams.isEmpty()) {
            item { Text("You are not part of any teams yet.", color = MaterialTheme.colorScheme.outline) }
        } else {
            items(teams, key = { it.id }) { team ->
                TeamCard(team = team, onClick = { onTeamClick(team.id) })
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun TeamCard(team: Team, onClick: () -> Unit) {
    AnimatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.Transparent,
        defaultElevation = 4.dp,
        pressedElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(team.colorStart, team.colorEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { team.logoIcon() }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = team.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (team.isCaptain) {
                                TeamTag(text = "Captain", color = CaptainColor, icon = Icons.Filled.VerifiedUser)
                                Spacer(Modifier.width(8.dp))
                            }
                            TeamTag(text = "${team.members} Members", color = MemberCountColor.copy(alpha = 0.8f), icon = Icons.Filled.People)
                        }
                    }

                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "Wins", value = team.wins.toString())
                    StatItem(label = "Losses", value = team.losses.toString())
                    StatItem(label = "Win Rate", value = "${team.winRate}%")
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Est. ${team.establishedYear}",
                    fontSize = 12.sp,
                    color = StatLabelColor
                )
            }
        }
    }
}

@Composable
fun TeamDetailHeader(team: Team, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(team.colorStart, team.colorEnd.copy(alpha = 0.8f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            IconButton(onClick = { /* Team Settings */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { team.logoIcon() }



            Text(
                text = team.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.People, contentDescription = "Members", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${team.members} Members",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderStatBubble(label = "Wins", value = team.wins.toString())
            HeaderStatBubble(label = "Losses", value = team.losses.toString())
            HeaderStatBubble(label = "Win Rate", value = "${team.winRate}%")
        }
    }
}

@Composable
fun HeaderStatBubble(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TeamFormationContent(team: Team) {
    var currentFormation by remember { mutableStateOf(team.selectedFormation) }

    Column {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(team.formations) { formation ->
                val isSelected = formation == currentFormation
                Text(
                    text = formation,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) TeamAcceptButtonColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface)
                        .clickable { currentFormation = formation }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.6f))
                    .align(Alignment.Center))
                Box(modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .align(Alignment.Center))

                // Player Positions (Absolute positioning simulation for 4-3-3)
                PlayerDot(team.roster.firstOrNull { it.number == 1 }, Alignment.Bottom, 20.dp, 0.5f)

                // Defenders (Back 4)
                PlayerDot(team.roster.firstOrNull { it.number == 2 }, Alignment.Bottom, 70.dp, 0.2f)
                PlayerDot(team.roster.firstOrNull { it.number == 3 }, Alignment.Bottom, 70.dp, 0.8f)
                PlayerDot(team.roster.firstOrNull { it.number == 4 }, Alignment.Bottom, 70.dp, 0.4f)
                PlayerDot(team.roster.firstOrNull { it.number == 5 }, Alignment.Bottom, 70.dp, 0.6f)

                // Midfielders (Mid 3)
                PlayerDot(team.roster.firstOrNull { it.number == 6 }, Alignment.CenterVertically, 0.dp, 0.5f)
                PlayerDot(team.roster.firstOrNull { it.number == 8 }, Alignment.CenterVertically, (-60).dp, 0.3f)
                PlayerDot(team.roster.firstOrNull { it.number == 10 }, Alignment.CenterVertically, (-60).dp, 0.7f)

                // Forwards (Front 3)
                PlayerDot(team.roster.firstOrNull { it.number == 7 }, Alignment.Top, 80.dp, 0.2f)
                PlayerDot(team.roster.firstOrNull { it.number == 9 }, Alignment.Top, 80.dp, 0.8f)
                PlayerDot(team.roster.firstOrNull { it.number == 11 }, Alignment.Top, 80.dp, 0.5f)
            }
        }
    }
}

@Composable
fun BoxScope.PlayerDot(player: Player?, verticalAlignment: Alignment.Vertical, verticalOffsetDp: Dp, horizontalPercent: Float) {
    if (player == null) return

    val fieldHeight = 400.dp

    val yOffset = when (verticalAlignment) {
        Alignment.Top -> verticalOffsetDp
        Alignment.Bottom -> fieldHeight - verticalOffsetDp - 60.dp
        Alignment.CenterVertically -> (fieldHeight / 2) + verticalOffsetDp - 30.dp
        else -> 0.dp
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp - 40.dp
    val xOffset = (screenWidth * horizontalPercent) - 20.dp

    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = xOffset,
                y = yOffset
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF66BB6A), CircleShape)
                    .shadow(2.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.number.toString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (player.isCaptain) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = "Captain",
                        tint = CaptainColor,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = player.position,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun TeamRosterContent(roster: List<Player>) {
    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(roster.sortedBy { it.number }, key = { it.id }) { player ->
                PlayerRosterRow(player)
            }
        }
    }
}

@Composable
fun PlayerRosterRow(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(player.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (player.isCaptain) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = "Captain",
                        tint = CaptainColor,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${player.number}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = player.position,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Rating", tint = RatingColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = player.rating.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Rating",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun TeamStatsContent(stats: TeamStats) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Team Statistics",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        StatProgressBar(label = "Goals Scored", value = stats.goalsScored, max = 200, color = TeamAcceptButtonColor)
        StatProgressBar(label = "Goals Conceded", value = stats.goalsConceded, max = 100, color = Color.Red)
        StatProgressBar(label = "Clean Sheets", value = stats.cleanSheets, max = 30, color = Color.Blue)
        StatProgressBar(label = "Possession Avg", value = stats.possessionAvg, isPercentage = true, max = 100, color = Color(0xFF9C27B0))

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        Text(
            text = "Top Performers",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TopPerformerRow(
            icon = Icons.Filled.EmojiEvents,
            title = "Top Scorer",
            value = "${stats.topScorerName} (${stats.topScorerGoals} goals)",
            color = RatingColor
        )
        Spacer(Modifier.height(16.dp))
        TopPerformerRow(
            icon = Icons.Filled.GpsFixed,
            title = "Most Assists",
            value = "${stats.mostAssistsName} (${stats.mostAssistsCount} assists)",
            color = Color.Blue
        )
    }
}

@Composable
fun StatProgressBar(label: String, value: Int, max: Int, color: Color, isPercentage: Boolean = false) {
    val progress = value.toFloat() / max.toFloat()
    val valueText = if (isPercentage) "$value%" else value.toString()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(valueText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun TopPerformerRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun TeamInvitesContent(
    invites: List<TeamInvite>,
    onAccept: (TeamInvite) -> Unit,
    onDecline: (TeamInvite) -> Unit
) {
    Column {
        Text(
            text = "Pending Invitations",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (invites.isEmpty()) {
                item { Text("No pending team invitations.", color = MaterialTheme.colorScheme.outline) }
            } else {
                items(invites, key = { it.id }) { invite ->
                    InviteRow(
                        invite = invite,
                        onAccept = { onAccept(invite) },
                        onDecline = { onDecline(invite) }
                    )
                }
            }
        }
    }
}

@Composable
fun InviteRow(invite: TeamInvite, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(invite.colorStart, invite.colorEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Group, contentDescription = "Team", tint = Color.White, modifier = Modifier.size(30.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(invite.teamName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(invite.inviter, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                Text("${invite.teamMembers} members", fontSize = 14.sp, color = StatLabelColor.copy(alpha = 0.8f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDecline, modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TeamDeclineButtonColor.copy(alpha = 0.2f))) {
                    Icon(Icons.Filled.Close, contentDescription = "Decline", tint = TeamDeclineButtonColor, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onAccept, modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TeamAcceptButtonColor.copy(alpha = 0.2f))) {
                    Icon(Icons.Filled.Check, contentDescription = "Accept", tint = TeamAcceptButtonColor, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun DiscoverTeamsContent(
    teams: List<DiscoverTeam>,
    onJoinRequest: (DiscoverTeam) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search teams...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Teams Open for Recruitment",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val filteredTeams = teams.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.tagline.contains(searchQuery, ignoreCase = true)
            }
            if (filteredTeams.isEmpty()) {
                item { Text("No teams found matching your search.", color = MaterialTheme.colorScheme.outline) }
            } else {
                items(filteredTeams, key = { it.id }) { team ->
                    DiscoverTeamRow(team = team, onJoinRequest = { onJoinRequest(team) })
                }
            }
        }
    }
}

@Composable
fun DiscoverTeamRow(team: DiscoverTeam, onJoinRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(team.colorStart, team.colorEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.GroupAdd, contentDescription = "Team", tint = Color.White, modifier = Modifier.size(30.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(team.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(team.tagline, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${team.wins} wins • ", fontSize = 14.sp, color = StatLabelColor.copy(alpha = 0.8f))
                    Text("${team.winRate}% win rate", fontSize = 14.sp, color = StatLabelColor.copy(alpha = 0.8f))
                }
            }

            val buttonColor = if (team.isRequested) TeamAcceptButtonColor.copy(alpha = 0.5f) else TeamAcceptButtonColor
            val icon = if (team.isRequested) Icons.Filled.Check else Icons.Filled.GroupAdd

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.15f))
                    .clickable(enabled = !team.isRequested, onClick = onJoinRequest),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = if (team.isRequested) "Request Sent" else "Request to Join", tint = buttonColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun TeamTag(text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StatValueColor)
        Text(label, fontSize = 12.sp, color = StatLabelColor)
    }
}

@Composable
fun CustomTeamTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val content = label.split(" ")
    val text = content.getOrNull(0) ?: ""
    val count = content.getOrNull(1)?.replace("(", "")?.replace(")", "")

    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val background = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = color, fontSize = 16.sp)
            if (count != null) {
                Spacer(Modifier.width(4.dp))
                if (text == "Invites") {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.CenterVertically)
                    )
                } else {
                    Text(count, fontWeight = FontWeight.Normal, color = color, fontSize = 16.sp)
                }
            }
        }
    }
}


// --- Preview ---

@Preview(showBackground = true, name = "Teams Screen List")
@Composable
fun TeamsScreenListPreview() {
    DAMTheme {
        TeamsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Teams Detail Screen")
@Composable
fun TeamsDetailScreenPreview() {
    DAMTheme {
        TeamDetailScreen(
            team = initialMyTeams.first { it.id == 2 }, // Phoenix Rising
            onBack = {},
            onTeamChat = {}
        )
    }
}

@Preview(showBackground = true, name = "Team Chat Screen Preview")
@Composable
fun TeamChatScreenPreview() {
    DAMTheme {
        TeamChatScreen(
            team = initialMyTeams.first { it.id == 2 }, // Phoenix Rising
            onBack = {}
        )
    }
}