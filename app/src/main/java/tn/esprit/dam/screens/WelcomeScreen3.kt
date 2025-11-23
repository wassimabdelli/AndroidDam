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
import tn.esprit.dam.R

@Composable
fun WelcomeScreen3(navController: NavController) {
    // Define the primary color needed for the Dot composable and overall theme
    val primaryGreen = Color(0xFF00C853)

    // A Box is the best choice for layering and specific alignment of elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {

        // --- 1. SKIP BUTTON (Aligned to Top-End) ---
        Text(
            text = "skip",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    // Navigate to LoginScreen when 'skip' is clicked
                    navController.navigate("LoginScreen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
        )

        // 2. Image at the top
        Image(
            painter = painterResource(id = R.drawable.third_page_illustration),
            contentDescription = "Soccer Illustration for page 3",
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
                text = "See the final",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = "score",
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find and Win the trophy",
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
                Dot(isSelected = false, primaryGreen = primaryGreen)
                Dot(isSelected = true, primaryGreen = primaryGreen)
            }
        }

        // 4. Next/Arrow Button (Aligned to Bottom-End)
        IconButton(
            onClick = {
                // Navigate to LoginScreen, as this is the final onboarding screen
                navController.navigate("LoginScreen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_icon),
                contentDescription = "Go to Login Screen",
                tint = Color.White
            )
        }
    }
}

// Small Composable for the pagination dots (Included for full file compilation)


@Preview(showBackground = true)
@Composable
fun WelcomeScreen3Preview() {
    WelcomeScreen3(navController = rememberNavController())
}