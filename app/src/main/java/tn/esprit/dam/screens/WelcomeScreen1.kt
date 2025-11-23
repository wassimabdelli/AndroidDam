package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // <-- NEW IMPORT
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
fun WelcomeScreen1(navController: NavController) {
    // Colors
    val primaryGreen = Color(0xFF00C853) // Use the dot color for the skip link

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
            color = Color.Gray, // A subtle color is good for secondary action
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd) // Position at the top right
                // --- ADDED TOP PADDING HERE ---
                .padding(top = 40.dp)
                .clickable {
                    // Navigate to LoginScreen when 'skip' is clicked
                    navController.navigate("LoginScreen") {
                        // Optional: Clear the back stack so the user cannot go back to onboarding
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
        )

        // 2. Image Illustration
        // NOTE: Ensure 'R.drawable.first_page_illustration' points to an image like 'Screenshot 2025-11-03 214603.png' or 'image_c6336e.png'
        Image(
            painter = painterResource(id = R.drawable.first_page_illustration),
            contentDescription = "Soccer Players Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .heightIn(max = 450.dp)
                .padding(top = 150.dp)
        )

        // 3. Text and Dots (Aligned to Bottom-Start)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 130.dp)
        ) {
            Text(
                text = "Explore the",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = "world easily",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Simplify the Game",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder for the dots/indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dot(isSelected = true, primaryGreen = primaryGreen)
                Dot(isSelected = false, primaryGreen = primaryGreen)
                Dot(isSelected = false, primaryGreen = primaryGreen)
            }
        }

        // 4. Next/Arrow Button (Aligned to Bottom-End)
        IconButton(
            onClick = {
                navController.navigate("welcome_screen_2") // Navigate to WelcomeScreen2
            },
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black) // Use the specified black background
                .align(Alignment.BottomEnd)
        ) {
            // NOTE: Ensure 'R.drawable.arrow_icon' exists and is a suitable right arrow icon
            Icon(
                painter = painterResource(id = R.drawable.arrow_icon),
                contentDescription = "Next",
                tint = Color.White
            )
        }
    }
}

// Small Composable for the pagination dots
@Composable
fun Dot(isSelected: Boolean, primaryGreen: Color) {
    Box(
        modifier = Modifier
            .width(if (isSelected) 32.dp else 10.dp)
            .height(10.dp)
            .clip(CircleShape)
            .background(if (isSelected) primaryGreen else Color.LightGray)
    )
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreen1Preview() {
    WelcomeScreen1(navController = rememberNavController())
}