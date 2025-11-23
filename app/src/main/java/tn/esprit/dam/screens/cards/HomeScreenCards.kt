package tn.esprit.dam.screens.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.R
// Removed explicit import of legacy theme colors (e.g., tn.esprit.dam.ui.theme.*)
// as we will rely only on MaterialTheme.colorScheme now.

data class GameEntry(
    val name: String,
    val timeDate: String,
    val score: String,
    val prize: String,
    val imageRes: Int
)

@Composable
fun MainGreenCard() {
    // MAPPING FIXED COLORS to MaterialTheme roles:
    // MediumGreen (Dark Forest Green, Card Background) -> MaterialTheme.colorScheme.primary
    // WinrateProgress (Bright Green, Icon BG) -> MaterialTheme.colorScheme.secondary
    // PrizeBlue (Blue Accent, Prize Text) -> MaterialTheme.colorScheme.tertiary
    // TextWhite (Contrast Text) -> MaterialTheme.colorScheme.onPrimary
    // LightGreen (Status Text) -> MaterialTheme.colorScheme.onPrimaryContainer

    val cardAccentColor = MaterialTheme.colorScheme.primary
    val iconBgColor = MaterialTheme.colorScheme.secondary
    val prizeTextColor = MaterialTheme.colorScheme.tertiary
    val contrastTextColor = MaterialTheme.colorScheme.onPrimary
    val statusTextColor = MaterialTheme.colorScheme.onPrimaryContainer // A distinct light color for status

    Card(
        shape = RoundedCornerShape(20.dp),
        // Uses the primary color from the scheme
        colors = CardDefaults.cardColors(containerColor = cardAccentColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(130.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // --- Left Section: Tournament Progression ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Tournament",
                    // Use onPrimary for contrast
                    tint = contrastTextColor,
                    modifier = Modifier
                        .size(50.dp)
                        .background(iconBgColor, CircleShape) // Use Secondary for the accent background
                        .padding(8.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("Tournament", color = contrastTextColor, fontSize = 12.sp)
                Text(
                    "Progression",
                    color = contrastTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // --- Separator Line ---
            Divider(
                // Use a subtle shade of the contrast color
                color = contrastTextColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .height(80.dp)
                    .width(1.dp)
            )

            // --- Right Section: Next Match & Prize Pool ---
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(IntrinsicSize.Max).padding(start = 16.dp)
            ) {
                // Next Match
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Match Date",
                        tint = contrastTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Next Match", color = contrastTextColor, fontSize = 12.sp)
                }
                Text(
                    "Tommorow",
                    // Use the distinct status color
                    color = statusTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(12.dp))

                // Prize Pool
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Prize Pool",
                        tint = contrastTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Prize Pool", color = contrastTextColor, fontSize = 12.sp)
                }
                Text(
                    "$ 500,00",
                    // Use Tertiary for the fixed prize blue look
                    color = prizeTextColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * Card for a single game entry in the list.
 */
@Composable
fun GameListItem(game: GameEntry) {

    // ADAPTIVE COLORS: These elements rely on the MaterialTheme to respect Dark/Light mode.
    // MAPPING:
    // primaryTextColor -> MaterialTheme.colorScheme.onSurface (since this list is on Surface/DarkBackground)
    // subtleDividerColor -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val subtleDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    // FIXED ACCENTS: Using Material Theme roles for consistency.
    // MAPPING:
    // timeDateColor (LightGreen) -> MaterialTheme.colorScheme.secondary
    // prizeColor (PrizeBlue Accent) -> MaterialTheme.colorScheme.tertiary
    val timeDateColor = MaterialTheme.colorScheme.secondary
    val prizeColor = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. Left Section: Image and Name/Time ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = game.imageRes),
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(55.dp)
                    .clip(RoundedCornerShape(10.dp))
                    // Use surfaceVariant as a background for the image placeholder
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    game.name,
                    color = primaryTextColor, // Adaptive
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    game.timeDate,
                    color = timeDateColor, // Adaptive Secondary Accent
                    fontSize = 14.sp
                )
            }
        }

        // --- 2. Score/Progression & Divider ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            // Score Text
            Text(
                game.score,
                color = timeDateColor, // Adaptive Secondary Accent
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )

            // Vertical Divider 1
            Divider(
                color = subtleDividerColor, // Adaptive Divider Color
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .padding(horizontal = 8.dp)
            )
        }


        // --- 3. Right Section: Prize & Divider ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            // Vertical Divider 2
            Divider(
                color = subtleDividerColor, // Adaptive Divider Color
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .padding(horizontal = 8.dp)
            )

            // Prize Text
            Text(
                game.prize,
                color = prizeColor, // Adaptive Tertiary Accent
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }
    }
}