package tn.esprit.dam.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.R
import tn.esprit.dam.data.ApiConfig
import tn.esprit.dam.models.AuthViewModel // <-- CORRECTED IMPORT
import androidx.compose.runtime.LaunchedEffect
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// -----------------------------------------------------------------------------
// --- PUBLIC ENTRY POINT ---
// -----------------------------------------------------------------------------
@Composable
fun LoginScreen(
    navController: NavController,
    // Inject the ViewModel
    viewModel: AuthViewModel = viewModel()
) {
    val uiState = viewModel.uiState // Get the current state
    val context = LocalContext.current // Used for Toast messages

    // Track previous authentication state to detect changes
    var previousAuthenticated by remember { mutableStateOf(false) }
    
    // ---------------------------------------------------------------------
    // Handle Authentication Success or Failure (Side Effect)
    // ---------------------------------------------------------------------
    LaunchedEffect(uiState.isAuthenticated, uiState.isLoading) {
        android.util.Log.d("LoginScreen", "LaunchedEffect triggered - isAuthenticated: ${uiState.isAuthenticated}, isLoading: ${uiState.isLoading}, previous: $previousAuthenticated")
        
        // Only navigate if authentication state changed from false to true
        if (uiState.isAuthenticated && !previousAuthenticated && !uiState.isLoading) {
            previousAuthenticated = true
            
            // Success: User is authenticated, navigate to Home Screen
            android.util.Log.d("LoginScreen", "=== NAVIGATION TO HOMESCREEN ===")
            android.util.Log.d("LoginScreen", "isAuthenticated = ${uiState.isAuthenticated}")
            android.util.Log.d("LoginScreen", "User: ${uiState.user?.user?.email ?: "N/A"}")
            android.util.Log.d("LoginScreen", "Token present: ${uiState.user?.access_token != null}")

            // Show success message
            Toast.makeText(context, "Login successful! Welcome back.", Toast.LENGTH_SHORT).show()
            
            // Small delay to ensure state is fully updated and UI is ready
            delay(300)
            
            // Navigate to HomeScreen and clear back stack
            try {
                android.util.Log.d("LoginScreen", "Attempting navigation to HomeScreen...")
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                android.util.Log.d("LoginScreen", "Current route before navigation: $currentRoute")
                
                // Use withContext to ensure we're on the main thread
                withContext(Dispatchers.Main) {
                    navController.navigate("HomeScreen") {
                        // Clear the entire back stack up to and including LoginScreen
                        popUpTo("LoginScreen") { inclusive = true }
                        // Prevent multiple instances
                        launchSingleTop = true
                        // Clear any previous navigation state
                        restoreState = false
                    }
                }
                android.util.Log.d("LoginScreen", "Navigation command sent to HomeScreen")
                
                // Verify navigation happened
                delay(300)
                val newRoute = navController.currentBackStackEntry?.destination?.route
                android.util.Log.d("LoginScreen", "Current route after navigation: $newRoute")
                
                if (newRoute != "HomeScreen") {
                    android.util.Log.e("LoginScreen", "Navigation failed! Still on route: $newRoute")
                    // Try alternative navigation method - clear entire stack
                    withContext(Dispatchers.Main) {
                        navController.navigate("HomeScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    android.util.Log.d("LoginScreen", "✅ Navigation successful! Now on HomeScreen")
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginScreen", "Navigation error: ${e.message}")
                android.util.Log.e("LoginScreen", "Error type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                
                // Fallback: try simple navigation
                try {
                    withContext(Dispatchers.Main) {
                        navController.navigate("HomeScreen")
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("LoginScreen", "Fallback navigation also failed: ${e2.message}")
                }
            }
        } else if (!uiState.isAuthenticated) {
            previousAuthenticated = false
        }
    }
    
    // Separate LaunchedEffect for error messages
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null && !uiState.isAuthenticated) {
            val message = uiState.errorMessage
            android.util.Log.d("LoginScreen", "Error message received: $message")
            
            // Check if error is about email verification
            val isEmailVerificationError = message.contains("vérifier votre adresse email", ignoreCase = true) ||
                message.contains("verify your email", ignoreCase = true) ||
                message.contains("email verification", ignoreCase = true) ||
                message.contains("vérifiez votre boîte", ignoreCase = true)
            
            if (!isEmailVerificationError) {
                // For non-verification errors, show Toast and auto-clear after a delay
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                // Auto-clear error after 5 seconds for non-verification errors
                delay(5000)
                viewModel.clearError()
            }
            // For email verification errors, keep the error visible in the UI banner
            // so the user can see the resend button
        }
    }

    LoginScreenContent(
        navController = navController,
        viewModel = viewModel,
        // The type reference here also changes to tn.esprit.dam.models.AuthUiState
        uiState = uiState
    )
}

// -----------------------------------------------------------------------------
// --- CONTENT COMPOSABLE ---
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    navController: NavController,
    viewModel: AuthViewModel,
    uiState: tn.esprit.dam.models.AuthUiState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showResendVerification by remember { mutableStateOf(false) }
    val context = LocalContext.current // Needed for opening web links

    // Backend base URL (centralized)
    // Use ApiConfig so all parts of the app point to the same host (local dev: 10.0.2.2)
    val BASE_API_URL = ApiConfig.WEB_BASE_URL

    // --- UI Colors ---
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardSurfaceColor = MaterialTheme.colorScheme.surface
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val textOnPrimary = MaterialTheme.colorScheme.onPrimary


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(cardSurfaceColor)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Header + Illustration remains the same)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.login_illustration),
                    contentDescription = "Login Illustration",
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "sign in to access your account",
                        fontSize = 14.sp,
                        color = secondaryTextColor
                    )
                }
            }
            // ... (End of Header)

            // Error/Success Banner - Show prominently when there's a message
            if (uiState.errorMessage != null) {
                val message = uiState.errorMessage
                val isEmailVerificationError = message.contains("vérifier votre adresse email", ignoreCase = true) ||
                        message.contains("verify your email", ignoreCase = true) ||
                        message.contains("email verification", ignoreCase = true) ||
                        message.contains("vérifiez votre boîte", ignoreCase = true)
                
                val isSuccessMessage = message.contains("re-sent", ignoreCase = true) ||
                        message.contains("envoyé", ignoreCase = true) ||
                        message.contains("sent successfully", ignoreCase = true) ||
                        message.contains("successfully", ignoreCase = true) ||
                        message.contains("succès", ignoreCase = true)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isSuccessMessage -> Color(0xFFD4EDDA) // Success green background
                                isEmailVerificationError -> Color(0xFFFFF3CD) // Warning yellow background
                                else -> Color(0xFFF8D7DA) // Error red background
                            }
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = if (isSuccessMessage) "Success" else "Error",
                            tint = when {
                                isSuccessMessage -> Color(0xFF155724) // Success green
                                isEmailVerificationError -> Color(0xFF856404) // Warning yellow
                                else -> Color(0xFF721C24) // Error red
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = message,
                            color = when {
                                isSuccessMessage -> Color(0xFF155724) // Success green
                                isEmailVerificationError -> Color(0xFF856404) // Warning yellow
                                else -> Color(0xFF721C24) // Error red
                            },
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = when {
                                    isSuccessMessage -> Color(0xFF155724) // Success green
                                    isEmailVerificationError -> Color(0xFF856404) // Warning yellow
                                    else -> Color(0xFF721C24) // Error red
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Field
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your email", color = secondaryTextColor.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    cursorColor = primaryColor,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email Icon",
                        tint = secondaryTextColor
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    cursorColor = primaryColor,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = secondaryTextColor
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remember me + Forgot password (unchanged)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            uncheckedColor = secondaryTextColor
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Remember me",
                        fontSize = 12.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                TextButton(onClick = { navController.navigate("ForgotPasswordScreen") }) {
                    Text(
                        text = "Forgot password?",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 1. Next button (Primary action) - NOW CALLS THE VIEWMODEL
            Button(
                onClick = {
                    viewModel.login(email, password, rememberMe)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                enabled = !uiState.isLoading // Disable button while loading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = textOnPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Next",
                        color = textOnPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = textOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show Resend Verification Email button if error is about email verification
            LaunchedEffect(uiState.errorMessage) {
                if (uiState.errorMessage != null) {
                    val message = uiState.errorMessage
                    showResendVerification = message.contains("vérifier votre adresse email", ignoreCase = true) ||
                            message.contains("verify your email", ignoreCase = true) ||
                            message.contains("email verification", ignoreCase = true) ||
                            message.contains("vérifiez votre boîte", ignoreCase = true)
                } else {
                    showResendVerification = false
                }
            }

            if (showResendVerification && email.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.resendVerificationEmail(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryColor
                    ),
                    border = BorderStroke(1.dp, primaryColor),
                    enabled = !uiState.isLoading && email.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = primaryColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Resend Email",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Resend Verification Email",
                            color = primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Social Login Buttons (Side-by-Side Row) - Now open web links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, ("$BASE_API_URL/api/v1/auth/google").toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .padding(end = 6.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryTextColor
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF4285F4) // Google Blue
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Login with Google",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Facebook Button
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, ("$BASE_API_URL/auth/facebook").toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .padding(start = 6.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = primaryTextColor
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFF1877F2) // Facebook Blue
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Login with Facebook",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp),
                        tint = Color(0xFF1877F2)
                    )
                    Text(
                        text = "Facebook",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Register (unchanged)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New member ? ", color = primaryTextColor, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate("SignupScreen") }) {
                    Text(
                        text = "Register now",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}