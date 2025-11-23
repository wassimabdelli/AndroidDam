package tn.esprit.dam.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tn.esprit.dam.R // Ensure you have access to your resources
import tn.esprit.dam.models.AuthViewModel

/**
 * Final screen in the password reset flow where the user sets a new password.
 * It uses the token verified in the previous step.
 */
@Composable
fun SetNewPasswordScreen(
    navController: NavController,
    verificationCode: String, // Receive the code from navigation
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    // Inject AuthViewModel here
    val viewModel: AuthViewModel = viewModel()
    // Pass the verification code to the content function
    SetNewPasswordContent(navController, viewModel, verificationCode, isDarkTheme, onToggleTheme)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetNewPasswordContent(
    navController: NavController,
    viewModel: AuthViewModel,
    verificationCode: String, // Not used, context is in ViewModel
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    // 1. Dynamic Colors using MaterialTheme.colorScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val hintColor = secondaryTextColor

    // 2. State Management
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState // Observe the ViewModel's state

    // Derived state for password validity/matching
    val minLength = 8 // Define minimum length requirement
    val isPasswordValid = newPassword.length >= minLength
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
    val canSubmit = passwordsMatch && isPasswordValid

    // Error observation
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    // Ensure the verification code is valid before proceeding
    LaunchedEffect(Unit) {
        if (verificationCode.isEmpty() || verificationCode == "null") { // Check for empty or "null" string placeholder
            scope.launch {
                snackbarHostState.showSnackbar("Error: Verification context missing. Please restart password reset flow.")
            }
            // Navigate back to the start of the reset flow if context is missing
            navController.navigate("ForgetPasswordScreen") {
                popUpTo("SetNewPasswordScreen") { inclusive = true }
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Top Left)
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryTextColor
                    )
                }

                // Theme Toggle Button (Top Right)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Header Text
            Text(
                text = "Set New Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Text(
                text = "Create a unique password.",
                fontSize = 16.sp,
                color = secondaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // 4. Illustration (Placeholder - Replace R.drawable.forgotpasswordillus with your actual resource ID)
            // Note: Assuming R.drawable.forgotpasswordillus exists in your project
            val illustration: Painter = painterResource(id = R.drawable.forgotpasswordillus)

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkTheme) Color(0xFF324A38).copy(alpha = 0.5f) else Color(0xFFEFF8F1),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = illustration,
                        contentDescription = "Set New Password Illustration",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. New Password Input Field
            Text(
                text = "New Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = primaryTextColor,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text("Create new password", color = hintColor) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = hintColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )

            // Password Validation Hint
            if (newPassword.isNotEmpty() && !isPasswordValid) {
                Text(
                    text = "Password must be at least $minLength characters long.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }


            // 6. Confirm Password Input Field
            Text(
                text = "Confirm Password",
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                color = primaryTextColor,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Re-enter password", color = hintColor) },
                singleLine = true,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle confirm password visibility", tint = hintColor)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                )
            )

            // Password Match Error Message
            if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                Text(
                    text = "Passwords do not match.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 7. Reset Password Button
            Button(
                onClick = {
                    errorMessage = null
                    Log.d("SetNewPasswordScreen", "Submit clicked: newPassword='$newPassword', confirmPassword='$confirmPassword'")
                    if (newPassword.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Please fill in both fields."
                        Log.d("SetNewPasswordScreen", "One or both fields are blank.")
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        Log.d("SetNewPasswordScreen", "Passwords do not match: newPassword='$newPassword', confirmPassword='$confirmPassword'")
                        return@Button
                    }
                    // Call resetPassword and handle navigation on success
                    Log.d("SetNewPasswordScreen", "Calling viewModel.resetPassword with newPassword='$newPassword'")
                    viewModel.resetPassword(newPassword, confirmPassword) { success, message ->
                        Log.d("SetNewPasswordScreen", "resetPassword callback: success=$success, message=$message")
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar(message ?: "Password reset successfully!")
                                // Navigate to LoginScreen after a short delay
                                kotlinx.coroutines.delay(800)
                                navController.navigate("LoginScreen") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                errorMessage = message
                                snackbarHostState.showSnackbar(message ?: "Failed to reset password.")
                            }
                        }
                    }
                },
                enabled = canSubmit && !uiState.isLoading, // Disable button while loading
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Reset Password",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 8. Reset password later? Link
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    // Navigate to login screen, popping the current screen
                    navController.popBackStack()
                    navController.navigate("LoginScreen")
                }
            ) {
                Text(
                    text = "Reset password later?",
                    color = primaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}