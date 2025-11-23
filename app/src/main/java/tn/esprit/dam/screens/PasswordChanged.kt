package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R // Ensure you have access to your resources
import tn.esprit.dam.ui.theme.DAMTheme // Import your theme

/**
 * Main Composable for the Password Changed Screen.
 * This screen confirms successful password reset and provides a button to continue to login.
 */
@Composable
fun PasswordChangedScreen(
    navController: NavController,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    PasswordChangedContent(
        navController = navController,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangedContent(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // 1. Dynamic Colors using MaterialTheme.colorScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline

    Scaffold(
        topBar = {
            // Theme Toggle Button in the top right corner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryTextColor
                    )
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // 2. Header Text
            Text(
                text = "Password Changed!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor, // Using primary color for the main title
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "No hassle anymore.",
                fontSize = 16.sp,
                color = secondaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp)
            )

            // 3. Illustration and Checkmark (FIXED LAYOUT)
            // Using a placeholder for the illustration image (R.drawable.ic_launcher_foreground)
            val illustration: Painter = painterResource(id = R.drawable.ic_launcher_foreground) // Placeholder

            Box(
                contentAlignment = Alignment.Center, // The Box itself is centered in the Column
                modifier = Modifier.size(200.dp)
            ) {
                // Background surface for the illustration
                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center), // Align Surface inside Box
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDarkTheme) Color(0xFF324A38) else Color(0xFFEFF8F1), // Dk_Tertiary / Lt_Surface
                    shadowElevation = 4.dp
                ) {
                    // Placeholder for the main illustration figure
                    Image(
                        painter = illustration,
                        contentDescription = "Success Illustration",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center) // Center Image inside Surface
                            .offset(y = 20.dp)
                    )
                }

                // Large Checkmark circle
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter) // Align Checkmark inside Box
                        .offset(y = (-30).dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = Color.White, // White circle background
                    shadowElevation = 8.dp,
                ) {
                    // Icon must be inside a Composable context, like this one
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder for Checkmark V icon
                        contentDescription = "Success Checkmark",
                        tint = primaryColor,
                        modifier = Modifier.padding(15.dp) // Add padding to size the icon within the surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // 4. Success Text
            val annotatedSuccessText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 18.sp, fontWeight = FontWeight.Normal)) {
                    append("Your password has been reset\n")
                }
                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)) {
                    append("Successfully!")
                }
            }
            Text(
                text = annotatedSuccessText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp)
            )

            // 5. Continue Button
            Button(
                onClick = {
                    // Navigate to LoginScreen and clear the entire reset flow from the back stack
                    navController.navigate("LoginScreen") {
                        // Pop up to the starting destination (graph ID) to clear this flow
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text(
                    text = "Continue",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Spacer to push content up
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PasswordChangedScreenLightPreview() {
    MaterialTheme {
        PasswordChangedScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PasswordChangedScreenDarkPreview() {
    MaterialTheme {
        PasswordChangedScreen(navController = rememberNavController())
    }
}