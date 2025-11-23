package tn.esprit.dam.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.ui.theme.DAMTheme

// --- Data Structures ---

data class Friend(
    val initials: String,
    val color: Color,
    val name: String,
    val username: String,
    val wins: Int,
    val winRate: Int,
    val isOnline: Boolean,
    val statusDotColor: Color
)

data class FriendRequest(
    val id: Int,
    val initials: String,
    val color: Color,
    val name: String,
    val username: String,
    val mutualFriends: Int
)

data class SuggestedUser(
    val id: Int,
    val initials: String,
    val color: Color,
    val name: String,
    val username: String,
    val mutualFriends: Int,
    val isSent: Boolean = false
)

// --- Colors and Constants ---
val WinrateColor = Color(0xFF85E4A0) // Greenish accent
val StatIconColor = Color(0xFFFFC107) // Yellow for wins/stats
val AcceptButtonColor = Color(0xFF4CAF50) // Green
val DeclineButtonColor = Color(0xFFBDBDBD) // Grey
val MutualFriendColor = Color(0xFF9E9E9E) // Darker grey for helper text

// --- Mock Data (Using mutable state lists for dynamic updates) ---
val initialFriends = listOf(
    Friend("MS", Color(0xFF66BB6A), "Marcus Silva", "@marcus", 45, 78, true, Color(0xFF4CAF50)),
    Friend("JL", Color(0xFF66BB6A), "Jordan Lee", "@jordan", 38, 72, true, Color(0xFF4CAF50)),
    Friend("AC", Color(0xFF66BB6A), "Alex Chen", "@alex", 52, 81, false, Color(0xFFBDBDBD)),
    Friend("ST", Color(0xFF66BB6A), "Sam Taylor", "@samtay", 41, 75, true, Color(0xFFFF9800)),
    Friend("RB", Color(0xFF66BB6A), "Riley Brooks", "@riley", 36, 69, true, Color(0xFF4CAF50)),
)

val initialRequests = listOf(
    FriendRequest(1, "CM", Color(0xFF9C27B0), "Chris Martin", "@chris", 3), // Purple
    FriendRequest(2, "TS", Color(0xFF673AB7), "Taylor Swift", "@tswift", 5)  // Indigo
)

val initialSuggested = listOf(
    SuggestedUser(3, "MB", Color(0xFF2196F3), "Morgan Blake", "@morgan", 8), // Blue
    SuggestedUser(4, "CJ", Color(0xFF2196F3), "Casey Jordan", "@casey", 6),
    SuggestedUser(5, "AQ", Color(0xFF2196F3), "Avery Quinn", "@avery", 4),
    SuggestedUser(6, "TK", Color(0xFFF44336), "Tony Kemp", "@tkemp", 2),
)

// --- State Management ---

class FriendState(
    initialFriends: List<Friend>,
    initialRequests: List<FriendRequest>,
    initialSuggested: List<SuggestedUser>
) {
    // The currently active tab
    var selectedTab by mutableStateOf("Friends")

    // Lists are mutable and observable, so UI updates automatically
    val friends = mutableStateListOf(*initialFriends.toTypedArray())
    val requests = mutableStateListOf(*initialRequests.toTypedArray())
    val suggested = mutableStateListOf(*initialSuggested.toTypedArray())

    fun acceptRequest(request: FriendRequest) {
        requests.remove(request)
        // Convert request to a friend object (using mock stats)
        val newFriend = Friend(
            initials = request.initials,
            color = Color(0xFF66BB6A), // Default green color for new friends
            name = request.name,
            username = request.username,
            wins = 50,
            winRate = 70,
            isOnline = true,
            statusDotColor = Color(0xFF4CAF50)
        )
        friends.add(newFriend)
    }

    fun declineRequest(request: FriendRequest) {
        requests.remove(request)
    }

    fun sendRequest(suggestedUser: SuggestedUser) {
        val index = suggested.indexOfFirst { it.id == suggestedUser.id }
        if (index != -1) {
            // Update the user's status to 'Sent' and replace in the list
            suggested[index] = suggestedUser.copy(isSent = true)
        }
    }
}

// --- Main Screen Composable (Entry Point) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavHostController) {
    // Initialize all friend-related state
    val friendState = remember { FriendState(initialFriends, initialRequests, initialSuggested) }

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
                    IconButton(onClick = { /* Notifications */ }) {
                        BadgedBox(badge = {
                            Badge(containerColor = Color.Red) {
                                Text(
                                    text = friendState.requests.size.toString(), // Updated to use dynamic count
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        FriendsScreenContent(
            modifier = Modifier.padding(paddingValues),
            friendState = friendState
        )
    }
}

// --- Main Content Composable (Handles Tab Switching) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreenContent(modifier: Modifier = Modifier, friendState: FriendState) {
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf(
        "Friends (${friendState.friends.size})",
        "Requests (${friendState.requests.size})",
        "Find"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // --- Header Text ---
        Text(
            text = "Friends",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Connect with players",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(24.dp))

        // --- Tabs ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEach { tab ->
                val label = tab.substringBefore(" (").substringBefore("s (") // Handle pluralization
                CustomTab(
                    label = tab,
                    isSelected = friendState.selectedTab == label,
                    onClick = { friendState.selectedTab = label }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // --- Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text("Search friends...", fontSize = 14.sp) },
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

        // --- Dynamic Content based on Tab ---
        when (friendState.selectedTab) {
            "Friends" -> FriendsListContent(friendState.friends, searchQuery)
            "Requests" -> FriendRequestsContent(
                requests = friendState.requests,
                onAccept = friendState::acceptRequest,
                onDecline = friendState::declineRequest
            )
            "Find" -> FindFriendsContent(
                suggestedUsers = friendState.suggested,
                onAddFriend = friendState::sendRequest,
                searchQuery = searchQuery // Use search query for finding users
            )
        }
    }
}

// --- Tab 1: Friends List ---

@Composable
fun FriendsListContent(friends: List<Friend>, searchQuery: String) {
    // Simple filter for demo
    val filteredFriends = friends.filter { it.name.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(filteredFriends, key = { it.username }) { friend ->
            FriendRow(friend = friend)
        }

        item {
            Spacer(Modifier.height(20.dp)) // Extra padding at the bottom of the list
        }
    }
}

// --- Tab 2: Friend Requests ---

@Composable
fun FriendRequestsContent(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit
) {
    Column {
        Text(
            text = "Pending Requests",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (requests.isEmpty()) {
                item {
                    Text("No pending friend requests.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                items(requests, key = { it.id }) { request ->
                    RequestRow(
                        request = request,
                        onAccept = { onAccept(request) },
                        onDecline = { onDecline(request) }
                    )
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun RequestRow(request: FriendRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
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
            // Profile Initials Circle
            AvatarCircle(initials = request.initials, color = request.color)

            Spacer(Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = request.username,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${request.mutualFriends} mutual friends",
                    fontSize = 14.sp,
                    color = MutualFriendColor
                )
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Decline Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DeclineButtonColor.copy(alpha = 0.1f))
                        .clickable(onClick = onDecline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Decline",
                        tint = DeclineButtonColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Accept Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AcceptButtonColor.copy(alpha = 0.1f))
                        .clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Accept",
                        tint = AcceptButtonColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// --- Tab 3: Find Friends ---

@Composable
fun FindFriendsContent(
    suggestedUsers: List<SuggestedUser>,
    onAddFriend: (SuggestedUser) -> Unit,
    searchQuery: String
) {
    Column {
        Text(
            text = "Suggested Friends",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple filter for demo: only show users who match the search query OR have not sent a request yet
            val filteredSuggested = suggestedUsers.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
            }

            if (filteredSuggested.isEmpty()) {
                item {
                    Text("No suggested users found.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                items(filteredSuggested, key = { it.id }) { user ->
                    SuggestedUserRow(
                        user = user,
                        onAddFriend = { onAddFriend(user) }
                    )
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SuggestedUserRow(user: SuggestedUser, onAddFriend: () -> Unit) {
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
            // Profile Initials Circle
            AvatarCircle(initials = user.initials, color = user.color)

            Spacer(Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.username,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${user.mutualFriends} mutual friends",
                    fontSize = 14.sp,
                    color = MutualFriendColor
                )
            }

            // Action Button
            val buttonColor = if (user.isSent) AcceptButtonColor.copy(alpha = 0.5f) else AcceptButtonColor
            val icon = if (user.isSent) Icons.Filled.Check else Icons.Filled.PersonAdd

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.1f))
                    .clickable(enabled = !user.isSent, onClick = onAddFriend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = if (user.isSent) "Request Sent" else "Add Friend",
                    tint = buttonColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- Helper Components (Used across all rows) ---

@Composable
fun AvatarCircle(initials: String, color: Color, showStatus: Boolean = false, statusDotColor: Color = Color.Transparent) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (showStatus) {
            // Status Dot (optional)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 20.dp, y = 20.dp) // Positioning the dot
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface) // Border color
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(statusDotColor)
            )
        }
    }
}

@Composable
fun FriendRow(friend: Friend) {
    val chatIconColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
    val chatBgColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
    val rowBgColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* View profile */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rowBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Initials Circle (with Status Dot)
            AvatarCircle(
                initials = friend.initials,
                color = friend.color,
                showStatus = true,
                statusDotColor = friend.statusDotColor
            )

            Spacer(Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = friend.username,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = "Wins",
                        tint = StatIconColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${friend.wins} wins",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "\u2022", // Dot separator
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.FlashOn,
                        contentDescription = "Win Rate",
                        tint = WinrateColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${friend.winRate}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Chat Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(chatBgColor)
                    .clickable { /* Start chat */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ChatBubble,
                    contentDescription = "Chat",
                    tint = chatIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
fun CustomTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = color,
                fontSize = 16.sp
            )
            if (count != null) {
                Spacer(Modifier.width(4.dp))
                // Red dot for Requests tab
                if (text == "Requests") {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        text = count,
                        fontWeight = FontWeight.Normal,
                        color = color,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, name = "Friends Screen")
@Composable
fun FriendsScreenPreview() {
    DAMTheme {
        FriendsScreen(rememberNavController())
    }
}