package tn.esprit.dam.screens

import android.os.CountDownTimer
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.models.AuthViewModel

// ----------------------------------------------------------------------------------
// --- IMPORTANT: Signature Modified to accept Theme State and Toggle Function ---
// ----------------------------------------------------------------------------------
@Composable
fun VerificationScreen(
    navController: NavController,
    // Inject the AuthViewModel so we can call verifyEmail()
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    // Add parameters for theme state management (It only reads isDarkTheme for colors)
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {} // Kept for consistency, but unused here
) {
    VerificationScreenContent(navController, viewModel)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreenContent(navController: NavController, viewModel: AuthViewModel) {
    // ACCESS COLORS VIA MATERIALTHEME
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.outline

    // Fallback for the back button background (using dark surface/card color)
    val backButtonBackground = MaterialTheme.colorScheme.surface

    // Constants
    val codeLength = 6

    // State for the 6-digit OTP code
    var otpCode by remember { mutableStateOf(List(codeLength) { "" }) }

    // State for the resend timer
    var timeLeft by remember { mutableStateOf(30) }
    var timerRunning by remember { mutableStateOf(false) }

    // Focus Management
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Snackbar Host State for displaying messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Display the email we have pending verification (if any)
    val pendingEmail = viewModel.pendingVerificationEmail ?: viewModel.uiState.user?.user?.email

    // Countdown Timer logic
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            val timer = object : CountDownTimer(timeLeft * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) { timeLeft = (millisUntilFinished / 1000).toInt() }
                override fun onFinish() { timeLeft = 0; timerRunning = false }
            }
            timer.start()
        }
    }

    // Start timer and set initial focus automatically
    LaunchedEffect(Unit) {
        timerRunning = true
        focusRequesters.first().requestFocus()
        // Ensure we load any persisted pending verification email into the ViewModel when the screen appears
        Log.d("VerificationScreen", "LaunchedEffect: loading persisted pending verification email into ViewModel")
        viewModel.loadPendingVerificationEmail()
    }

    // --- SCaffold added to host the Snackbar ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor) // Dynamic background
                .padding(24.dp)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "Almost there",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor // Dynamic primary text color
                    ,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val annotatedDescription = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = secondaryTextColor, fontSize = 16.sp)) {
                        append("Please enter the 6-digit code sent to your\nemail ")
                    }
                    withStyle(style = SpanStyle(color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
                        append(pendingEmail ?: "your email")
                    }
                    withStyle(style = SpanStyle(color = secondaryTextColor, fontSize = 16.sp)) {
                        append(" for\nverification.")
                    }
                }
                Text(
                    text = annotatedDescription,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // OTP Code Input Fields (Dynamic Colors)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    otpCode.forEachIndexed { index, digit ->
                        BasicTextField(
                            value = digit,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() && digit.isEmpty() && index > 0) {
                                    val newOtpCode = otpCode.toMutableList()
                                    newOtpCode[index - 1] = ""
                                    otpCode = newOtpCode
                                    focusRequesters[index - 1].requestFocus()
                                }
                                else if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                    val newOtpCode = otpCode.toMutableList()
                                    newOtpCode[index] = newValue
                                    otpCode = newOtpCode

                                    if (newValue.isNotEmpty() && index < codeLength - 1) {
                                        focusRequesters[index + 1].requestFocus()
                                    } else if (index == codeLength - 1 && newValue.isNotEmpty()) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .size(50.dp)
                                .focusRequester(focusRequesters[index])
                                .background(inputBackgroundColor, RoundedCornerShape(12.dp)) // Dynamic input background
                                .clip(RoundedCornerShape(12.dp))
                                .wrapContentHeight(Alignment.CenterVertically)
                                .padding(top = 10.dp, bottom = 10.dp),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                color = primaryTextColor, // Dynamic primary text color
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Verify Button (Dynamic Color)
                Button(
                    onClick = {
                        val fullCode = otpCode.joinToString("")
                        if (fullCode.length == codeLength && fullCode.all { it.isDigit() }) {
                            // Debug log: show the OTP and the pending email when verify is triggered
                            Log.d("VerificationScreen", "Verify button pressed. Code=$fullCode, pendingEmail=${viewModel.pendingVerificationEmail}")

                            // Decide which verification method to call depending on flow
                            if (viewModel.pendingIsForPasswordReset) {
                                // This is the forgot-password flow
                                viewModel.verifyForgotPasswordCode(fullCode) { success, message ->
                                    scope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar(
                                                message = message ?: "Code verified!",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                            // Navigate to SetNewPasswordScreen to reset password
                                            delay(600)
                                            navController.navigate("SetNewPasswordScreen") {
                                                popUpTo("VerificationScreen") { inclusive = true }
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = message ?: "Verification failed: wrong code.",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Regular account verification flow
                                viewModel.verifyEmail(fullCode) { success, message ->
                                    scope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar(
                                                message = message ?: "Verification successful!",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                            // Navigate to LoginScreen after short delay
                                            delay(600)
                                            navController.navigate("LoginScreen") {
                                                popUpTo("VerificationScreen") { inclusive = true }
                                            }
                                        } else {
                                            // Verification failed (wrong code or server error)
                                            snackbarHostState.showSnackbar(
                                                message = message ?: "Verification failed: wrong code.",
                                                withDismissAction = true,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Please enter the complete 6-digit code.",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor // Dynamic primary color
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Verify",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Resend Code Section (Dynamic Colors)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = {
                            if (!timerRunning) {
                                // Use ViewModel to resend using pending email if available
                                val emailToResend = pendingEmail
                                if (emailToResend != null) {
                                    if (viewModel.pendingIsForPasswordReset) {
                                        // Resend forgot-password code
                                        viewModel.forgotPassword(emailToResend) { s, m ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(m ?: if (s) "Code sent" else "Failed to resend")
                                            }
                                        }
                                    } else {
                                        // Resend account verification code
                                        viewModel.resendVerificationEmail(emailToResend)
                                    }
                                } else {
                                    // fallback: show snackbar
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No email available to resend verification.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                timeLeft = 30
                                timerRunning = true
                                otpCode = List(codeLength) { "" }
                                focusRequesters.first().requestFocus()
                            }
                        },
                        enabled = !timerRunning
                    ) {
                        Text(
                            text = "Didn't receive any code? Resend Again",
                            color = if (timerRunning) secondaryTextColor else primaryColor, // Dynamic colors
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Request a new code in ${String.format("%02d", timeLeft)}s",
                        color = secondaryTextColor, // Dynamic secondary color
                        fontSize = 12.sp
                    )
                }
            }

            // Back Button (Bottom-left aligned, Dynamic Color)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backButtonBackground) // Dynamic back button background
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Signup",
                    tint = MaterialTheme.colorScheme.onSurface // Text/Icon color on surface
                )
            }

            // --- REMOVED: Theme Toggle Button ---
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun VerificationScreenPreview() {
    // Wrap preview in DAMTheme manually for theme context
    // tn.esprit.dam.ui.theme.DAMTheme(darkTheme = false) {
    VerificationScreen(navController = rememberNavController())
    // }
}