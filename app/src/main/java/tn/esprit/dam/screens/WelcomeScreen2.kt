package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Required for skip text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R // Make sure this R is correctly pointing to your resources

@Composable
fun WelcomeScreen2(navController: NavController) {
    // Define the primary color needed for the Dot composable
    val primaryGreen = Color(0xFF00C853) // Define the color here

    // A Box is the best choice for layering and specific alignment of elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp) // Add padding around the screen content
    ) {

        // --- 1. SKIP BUTTON (Aligned to Top-End) ---
        Text(
            text = "skip",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd) // Position at the top right
                .clickable {
                    // Navigate to LoginScreen when 'skip' is clicked
                    navController.navigate("LoginScreen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
        )

        // 2. Image at the top
        Image(
            // Use the correct resource ID for your image (e.g., image_148da7.png)
            painter = painterResource(id = R.drawable.second_page_illustration),
            contentDescription = "Soccer Players Illustration",
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the Box
                .align(Alignment.TopCenter) // Position at the top center
                // Control the height to ensure text has space at the bottom
                .heightIn(max = 450.dp)
                .padding(top = 150.dp) // Add some padding at the bottom
        )

        // 3. Text and Dots (Aligned to Bottom-Start)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart) // Aligns the whole Column to the bottom-start
                .padding(bottom = 130.dp) // Push up from the very bottom edge to make space for the button
        ) {
            Text(
                text = "Reach the",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = "unknown spot",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Master the Tournament",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder for the dots/indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FIX: Pass the primaryGreen parameter here
                Dot(isSelected = false, primaryGreen = primaryGreen)
                Dot(isSelected = true, primaryGreen = primaryGreen)
                Dot(isSelected = false, primaryGreen = primaryGreen)
            }
        }

        // 4. Next/Arrow Button (Aligned to Bottom-End)
        IconButton(
            onClick = {
                navController.navigate("welcome_screen_3") // Navigate to WelcomeScreen3
            },
            modifier = Modifier
                .size(64.dp) // Set a specific size for the circular button
                .clip(CircleShape) // Make it circular
                .background(Color.Black) // Use black background
                .align(Alignment.BottomEnd) // Position at the bottom end (right)
        ) {
            // Placeholder for the Right Arrow Icon
            Icon(
                painter = painterResource(id = R.drawable.arrow_icon), // Ensure this drawable exists
                contentDescription = "Next",
                tint = Color.White
            )
        }
    }
}

// Small Composable for the pagination dots
// NOTE: I am redefining Dot here, assuming it was missing or defined elsewhere without this parameter before.


@Preview(showBackground = true)
@Composable
fun WelcomeScreen2Preview() {
    WelcomeScreen2(navController = rememberNavController())
}