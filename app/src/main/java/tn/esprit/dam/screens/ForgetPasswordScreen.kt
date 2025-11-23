package tn.esprit.dam.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.models.AuthViewModel // Assuming AuthViewModel is in this path

/**
 * Main Composable for the Forget Password Screen.
 */
@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    // Theme state management is often handled outside, but we include it for the toggle functionality
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {}
) {
    // Inject AuthViewModel here to manage dependencies
    val viewModel: AuthViewModel = viewModel()

    // Use rememberUpdatedState for callbacks to prevent unnecessary recompositions
    val currentOnToggleTheme by rememberUpdatedState(onToggleTheme)

    ForgetPasswordContent(
        navController = navController,
        isDarkTheme = isDarkTheme,
        onToggleTheme = currentOnToggleTheme,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordContent(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AuthViewModel
) {
    // 1. Dynamic Colors using MaterialTheme.colorScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val hintColor = secondaryTextColor

    // 2. State Management
    var emailOrMobile by remember { mutableStateOf("") }
    // Ensure we default to email since mobile is not supported yet
    var isEmailSelected by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState // Observe UI state for loading/errors

    // Display error messages from ViewModel
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            // Check if the message is a success message from the forgotPassword call
            if (errorMessage.contains("sent successfully", ignoreCase = true) || errorMessage.contains("Code sent", ignoreCase = true)) {
                // Ignore success messages here as we handle navigation in the button click
                // and the ViewModel handles the clearing of errors.
            } else {
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.clearError() // Clear error after showing
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
            // 3. Header Text
            Text(
                text = "Forgot Password?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            Text(
                text = "No worries, We got you.",
                fontSize = 16.sp,
                color = secondaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // 4. Illustration (Placeholder - Replace R.drawable.forgotpasswordillus with your actual resource ID)
            // NOTE: Using a simple colored Box as a placeholder since R.drawable.forgotpasswordillus is not accessible.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkTheme) Color(0xFF324A38).copy(alpha = 0.5f) else Color(0xFFEFF8F1),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Placeholder for illustration
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Email Icon",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "We'll send you a verification code to reset it.",
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. Input Toggle (Email/Mobile Number)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                InputToggleText(
                    text = "Email Address",
                    isSelected = isEmailSelected,
                    onClick = { isEmailSelected = true; emailOrMobile = "" },
                    primaryColor = primaryColor,
                    secondaryTextColor = secondaryTextColor
                )
                Spacer(modifier = Modifier.width(24.dp))
                InputToggleText(
                    text = "Mobile Number?",
                    isSelected = !isEmailSelected,
                    // Prevent switching to mobile number since it's not supported
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Mobile number reset is not supported by the backend yet.")
                        }
                    },
                    primaryColor = primaryColor,
                    secondaryTextColor = secondaryTextColor
                )
            }

            // 6. Text Field Input
            val inputTypeLabel = if (isEmailSelected) "Enter email address" else "Mobile number not supported"
            OutlinedTextField(
                value = emailOrMobile,
                onValueChange = { emailOrMobile = it },
                label = null,
                placeholder = {
                    Text(
                        text = inputTypeLabel,
                        color = hintColor
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isEmailSelected) KeyboardType.Email else KeyboardType.Phone
                ),
                singleLine = true,
                enabled = isEmailSelected, // Disable input if mobile is selected (though we prevent selection now)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor,
                    disabledContainerColor = inputBackgroundColor.copy(alpha = 0.5f),
                    disabledTextColor = secondaryTextColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 7. Send Code Button
            val isButtonEnabled = emailOrMobile.isNotBlank() && isEmailSelected && !uiState.isLoading

            Button(
                onClick = {
                    scope.launch {
                        // The isEmailSelected guard is mostly for UI clarity, but we ensure the input is valid.
                        viewModel.forgotPassword(emailOrMobile) { success, message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message ?: if (success) "Code sent successfully" else "Failed to send code")
                            }
                            if (success) {
                                // Navigate to the verification screen
                                navController.navigate("VerificationResetScreen")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                enabled = isButtonEnabled
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Send Code",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 8. Back to Log In link (Bottom)
            Spacer(modifier = Modifier.weight(1f)) // Pushes content to the top
            TextButton(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "← Back to log in?",
                    color = primaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun InputToggleText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) primaryColor else secondaryTextColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(if (isSelected) 100.dp else 0.dp)
                .height(2.dp)
                .background(primaryColor)
        )
    }
}