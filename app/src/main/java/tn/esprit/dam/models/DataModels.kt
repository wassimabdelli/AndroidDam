package tn.esprit.dam.models

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing a single Player in a team.
 * This class has been centralized here to prevent redeclaration errors in multiple screen files.
 */
data class Player(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val number: Int,
    val position: String, // e.g., "Striker", "Midfielder", "Defender"
    val rating: Double,
    val isCaptain: Boolean,
    val initials: String, // e.g., "LM"
    val color: Color,
) {
    // Utility function to generate initials if they are not passed directly
    companion object {
        fun generateInitials(name: String): String {
            return name.split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
        }
    }
}

/**
 * Data class representing a Team in the competition.
 * This class has been centralized here to prevent redeclaration errors in multiple screen files.
 */
data class Team(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val tagline: String,
    val rank: Int,
    val wins: Int,
    val losses: Int,
    val establishedYear: Int,
    val icon: ImageVector, // Small decorative icon for lists
    val colorStart: Color, // Gradient start color
    val colorEnd: Color,   // Gradient end color
    val logoIcon: ImageVector, // Large logo icon for detail screen
    val roster: List<Player>, // List of players in the team
    val formations: List<String>, // List of available formations, like ["4-4-2", "4-3-3"]
)

// --- User and Profile Data Models ---
data class UserProfile(
    val initials: String,
    val name: String,
    val handle: String,
    val level: Int,
    val wins: Int,
    val losses: Int,
    val winRate: String,
    val goalsScored: Int,
    val assists: Int,
    val cleanSheets: Int,
    val totalMatches: Int,
    val favoriteStadium: String,
    val memberSince: String
)

data class Achievement(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String,
    val unlocked: Boolean = true
)

data class MatchResult(
    val status: String, // "W", "L", "D"
    val title: String,
    val date: String,
    val score: String
)
