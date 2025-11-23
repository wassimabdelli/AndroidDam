package tn.esprit.dam.screens

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.models.AuthViewModel

/**
 * Screen for users to enter the 6-digit verification code sent to their email
 * for password reset purposes.
 */
@Composable
fun VerificationResetScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    VerificationResetContent(navController, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationResetContent(navController: NavController, viewModel: AuthViewModel) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val backButtonBackground = MaterialTheme.colorScheme.surface

    val codeLength = 6
    var otpCode by remember { mutableStateOf(List(codeLength) { "" }) }
    var timeLeft by remember { mutableStateOf(30) }
    var timerRunning by remember { mutableStateOf(false) }

    val focusRequesters = remember { List(codeLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState // Observe the UI state

    // Get the email from the ViewModel state, which should have been set in ForgetPasswordScreen
    val pendingEmail = viewModel.pendingVerificationEmail ?: viewModel.uiState.user?.user?.email

    // Display error messages from ViewModel
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError() // Clear error after showing
        }
    }

    // --- Timer Management ---
    val timer = remember {
        object : CountDownTimer(30 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                timeLeft = 0
                timerRunning = false
            }
        }
    }

    // Controls when the timer should start/stop
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            timer.start()
        } else {
            timer.cancel()
        }
    }

    // Ensure timer is cancelled when the screen is disposed (navigated away)
    DisposableEffect(Unit) {
        onDispose {
            timer.cancel()
        }
    }

    // Initial setup
    LaunchedEffect(Unit) {
        timerRunning = true
        focusRequesters.first().requestFocus()
        viewModel.loadPendingVerificationEmail()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(24.dp)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Spacer(modifier = Modifier.height(60.dp))
                Text(
                    text = "Almost there",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Description text with highlighted email
                val annotatedDescription = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = secondaryTextColor, fontSize = 16.sp)) {
                        append("Please enter the 6-digit code sent to ")
                    }
                    withStyle(style = SpanStyle(color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
                        // Display the pending email or a placeholder if null
                        append(pendingEmail ?: "your email")
                    }
                    withStyle(style = SpanStyle(color = secondaryTextColor, fontSize = 16.sp)) {
                        append(" for verification.")
                    }
                }
                Text(text = annotatedDescription, modifier = Modifier.padding(bottom = 40.dp))

                // --- OTP Input Fields ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    otpCode.forEachIndexed { index, digit ->
                        BasicTextField(
                            value = digit,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    // Handle backspace/deletion
                                    val newOtpCode = otpCode.toMutableList()
                                    newOtpCode[index] = "" // Clear current digit
                                    otpCode = newOtpCode

                                    // Move focus to previous field
                                    if (index > 0) {
                                        focusRequesters[index - 1].requestFocus()
                                    }
                                } else if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    // Handle digit entry
                                    val newOtpCode = otpCode.toMutableList()
                                    newOtpCode[index] = newValue.take(1) // Take only the first character
                                    otpCode = newOtpCode

                                    // Move focus to next field or clear focus if last field
                                    if (index < codeLength - 1) {
                                        focusRequesters[index + 1].requestFocus()
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .size(50.dp)
                                .focusRequester(focusRequesters[index])
                                .background(inputBackgroundColor, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(top = 10.dp, bottom = 10.dp),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                color = primaryTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            ),
                            decorationBox = { innerTextField -> Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { innerTextField() } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- Verify Button ---
                Button(
                    onClick = {
                        val fullCode = otpCode.joinToString("")
                        if (fullCode.length == codeLength && fullCode.all { it.isDigit() }) {
                            viewModel.verifyForgotPasswordCode(fullCode) { success, message ->
                                scope.launch {
                                    if (success) {
                                        snackbarHostState.showSnackbar(message ?: "Code verified! Proceeding to reset password.")
                                        timer.cancel() // Stop the timer
                                        delay(600)

                                        // Pass the verified code as a navigation argument
                                        navController.navigate("SetNewPasswordScreen/$fullCode") {
                                            popUpTo("VerificationResetScreen") { inclusive = true }
                                        }

                                    } else {
                                        snackbarHostState.showSnackbar(message ?: "Verification failed. Please check the code and try again.")
                                    }
                                }
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please enter the complete 6-digit code.") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    enabled = !uiState.isLoading // Disable button while loading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "Verify", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Resend Code and Timer ---
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = {
                        if (!timerRunning) {
                            val emailToResend = pendingEmail
                            if (emailToResend != null) {
                                // Re-request the code from the backend
                                viewModel.forgotPassword(emailToResend) { s, m -> scope.launch { snackbarHostState.showSnackbar(m ?: if (s) "New code sent" else "Failed to resend code") } }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Error: No email available to resend verification. Please go back.") }
                            }
                            // Reset timer and state
                            timeLeft = 30
                            timerRunning = true
                            otpCode = List(codeLength) { "" }
                            focusRequesters.first().requestFocus()
                        }
                    }, enabled = !timerRunning && !uiState.isLoading) { // Also disable if loading
                        Text(
                            text = "Didn't receive any code? Resend Again",
                            color = if (timerRunning || uiState.isLoading) secondaryTextColor else primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Request a new code in ${String.format("%02d", timeLeft)}s",
                        color = secondaryTextColor,
                        fontSize = 12.sp
                    )
                }
            }

            // --- Back Button ---
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backButtonBackground)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}