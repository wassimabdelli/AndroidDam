package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import android.util.Log
import tn.esprit.dam.R
import tn.esprit.dam.models.AuthViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // --- Animation States ---
    val infiniteTransition = rememberInfiniteTransition()
    val uiState = viewModel.uiState

    // 1. Horizontal movement from left (-screen width) to center, then right (screen width)
    val horizontalOffset by infiniteTransition.animateFloat(
        initialValue = -200f, // Start off-screen left
        targetValue = 200f,  // End off-screen right
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000, // Time to cross the screen
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    // 2. Vertical movement (realistic bounce)
    val totalDuration = 3000
    val bounceAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = totalDuration

                // Note: The KeyframesSpec.KeyframeEntity<Float> needs the Float value first,
                // then .at(Int timestamp)
                0f.at(0) with FastOutSlowInEasing // Start at "ground"
                // First high bounce
                (-100f).at((totalDuration * 0.2f).toInt()) with FastOutSlowInEasing // Peak of first bounce
                0f.at((totalDuration * 0.35f).toInt()) with FastOutSlowInEasing // Land after first bounce

                // Second smaller bounce
                (-50f).at((totalDuration * 0.45f).toInt()) with FastOutSlowInEasing // Peak of second bounce
                0f.at((totalDuration * 0.60f).toInt()) with FastOutSlowInEasing // Land after second bounce

                // Third even smaller bounce
                (-20f).at((totalDuration * 0.70f).toInt()) with FastOutSlowInEasing // Peak of third bounce
                0f.at((totalDuration * 0.80f).toInt()) with FastOutSlowInEasing // Land after third bounce

                // Settle
                0f.at(totalDuration)
            },
            repeatMode = RepeatMode.Restart
        )
    )

    // Self-writing text state
    var textDisplayed by remember { mutableStateOf("") }
    val fullText = "Welcome to DAM!"

    LaunchedEffect(Unit) {
        for (i in 1..fullText.length) {
            delay(150)
            textDisplayed = fullText.substring(0, i)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.splash_page),
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // The animated ball (splash logo) in the middle of the screen
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = "Splash Logo",
            modifier = Modifier
                .offset(
                    x = horizontalOffset.dp, // Apply horizontal movement
                    y = -50.dp + bounceAnimation.dp // Adjust base Y, then apply bounce (negative for "up")
                )
                .size(150.dp)
        )

        // Self-writing text just below the ball
        Text(
            text = textDisplayed,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp)
        )
    }

    // Track navigation to prevent multiple navigations
    var hasNavigated by remember { mutableStateOf(false) }
    var authCheckStarted by remember { mutableStateOf(false) }
    
    // Check authentication state on startup
    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "=== SPLASH SCREEN STARTED ===")
        viewModel.checkAuthState()
        // Small delay to ensure state update propagates
        delay(50)
        authCheckStarted = true
    }
    
    // Navigate when auth state is determined
    LaunchedEffect(uiState.isLoading, uiState.isAuthenticated, authCheckStarted) {
        // Only navigate once, when auth check has started, and when loading is complete
        if (authCheckStarted && !uiState.isLoading && !hasNavigated) {
            hasNavigated = true
            
            if (uiState.isAuthenticated) {
                // User is remembered - navigate immediately to HomeScreen
                Log.d("SplashScreen", "✅ User is remembered, navigating to HomeScreen immediately")
                navController.navigate("HomeScreen") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                // User not remembered - wait for splash animation then navigate to welcome screen
                Log.d("SplashScreen", "❌ User not remembered, waiting for splash animation")
                delay(3500) // Wait for remaining splash animation (total 4 seconds)
                navController.navigate("welcome_screen_1") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}