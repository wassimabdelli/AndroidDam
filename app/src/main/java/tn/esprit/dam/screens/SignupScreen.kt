package tn.esprit.dam.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.models.AuthUiState
import tn.esprit.dam.models.AuthViewModel // Import the ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

// ----------------------------------------------------------------------------------
// --- PUBLIC ENTRY POINT ---
// ----------------------------------------------------------------------------------
@Composable
fun SignupScreen(
    navController: NavController,
    // Inject the ViewModel
    viewModel: AuthViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    // Handle Authentication Success or Failure (Side Effect)
    LaunchedEffect(uiState.isAuthenticated, uiState.errorMessage, viewModel.pendingVerificationEmail) {
        // If login/registration returned an authenticated user, navigate to Home
        if (uiState.isAuthenticated) {
            Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
            navController.navigate("HomeScreen") {
                popUpTo("SignupScreen") { inclusive = true }
            }
            return@LaunchedEffect
        }

        // If pendingVerificationEmail is set, registration created the account and we must verify
        val pendingEmail = viewModel.pendingVerificationEmail
        if (!pendingEmail.isNullOrBlank()) {
            // Show a toast then navigate to VerificationScreen and keep the signup off the back stack
            Toast.makeText(context, "Account created. Please verify your email: $pendingEmail", Toast.LENGTH_LONG).show()
            navController.navigate("VerificationScreen") {
                popUpTo("SignupScreen") { inclusive = true }
            }
            return@LaunchedEffect
        }

        // Fallback: existing message parsing - show toast and optional navigation when message indicates verification required
        if (uiState.errorMessage != null) {
            val message = uiState.errorMessage
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

            if (message.contains("Account created successfully", ignoreCase = true) ||
                message.contains("check your email", ignoreCase = true) ||
                message.contains("created successfully", ignoreCase = true) ||
                message.contains("verification", ignoreCase = true)) {
                delay(800)
                navController.navigate("VerificationScreen") {
                    popUpTo("SignupScreen") { inclusive = true }
                }
            }

            viewModel.clearError()
        }
    }

    SignupScreenContent(navController, viewModel, uiState)
}

// Roles mapping
private val roleMap = mapOf(
    "Player" to "JOUEUR",
    "Referee" to "ARBITRE",
    "Stadium Owner" to "OWNER"
)

// API expects "YYYY-MM-DD". We display "dd/MM/yyyy"
private val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreenContent(
    navController: NavController,
    viewModel: AuthViewModel,
    uiState: AuthUiState
) {
    // ACCESS COLORS VIA MATERIALTHEME
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardSurfaceColor = MaterialTheme.colorScheme.surface
    val inputBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    // Tab state
    val tabs = listOf("Player", "Referee", "Stadium Owner")
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // Stage state (3 stages)
    var currentStage by remember { mutableStateOf(1) }
    
    // Form state - Stage 1: Full name (combined) and username
    var fullName by remember { mutableStateOf("") } // Combined name and surname
    var username by remember { mutableStateOf("") }
    
    // Form state - Stage 2: Contact info
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDateText by remember { mutableStateOf("") }
    
    // Form state - Stage 3: Security
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    // Validation errors
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    // State to track if registration was successful (for showing resend button)
    var registrationSuccessful by remember { mutableStateOf(false) }

    // Snackbar Host State for displaying messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Date Picker Dialog State
    val initialDateMillis = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }.timeInMillis
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    // Validation patterns
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    val passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$")

    // Date Picker Logic
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            birthDateText = DISPLAY_DATE_FORMAT.format(Date(millis))
                            birthDateError = null // Clear error on successful selection
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Validation functions for each stage
    fun validateStage1(): Boolean {
        fullNameError = null
        usernameError = null
        
        var isValid = true
        val nameParts = fullName.trim().split("\\s+".toRegex())
        
        if (fullName.trim().isEmpty()) {
            fullNameError = "Full name is required."
            isValid = false
        } else if (nameParts.size < 2) {
            fullNameError = "Please enter both first and last name."
            isValid = false
        } else if (nameParts.any { it.length < 2 }) {
            fullNameError = "Each name part must be at least 2 characters."
            isValid = false
        }
        
        if (username.trim().isEmpty()) {
            usernameError = "Username is required."
            isValid = false
        } else if (username.trim().length < 3) {
            usernameError = "Username must be at least 3 characters."
            isValid = false
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            usernameError = "Username can only contain letters, numbers, and underscores."
            isValid = false
        }
        
        return isValid
    }
    
    fun validateStage2(): Boolean {
        emailError = null
        phoneError = null
        birthDateError = null
        
        var isValid = true
        
        if (!emailPattern.matcher(email).matches()) {
            emailError = "Invalid email format."
            isValid = false
        }
        
        if (!phoneNumber.matches(Regex("^\\d{8}$"))) {
            phoneError = "Phone number must be exactly 8 digits."
            isValid = false
        }
        
        if (birthDateText.isEmpty()) {
            birthDateError = "Birth date is required."
            isValid = false
        } else {
            try {
                val birthDate = DISPLAY_DATE_FORMAT.parse(birthDateText)
                val eighteenYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }.time
                if (birthDate == null || birthDate.after(eighteenYearsAgo)) {
                    birthDateError = "You must be 18 or older to register."
                    isValid = false
                }
            } catch (e: Exception) {
                birthDateError = "Invalid date format. Use DD/MM/YYYY."
                isValid = false
            }
        }
        
        return isValid
    }
    
    fun validateStage3(): Boolean {
        passwordError = null
        confirmPasswordError = null
        termsError = null
        
        var isValid = true
        
        if (!passwordPattern.matcher(password).matches()) {
            passwordError = "Password must contain 6+ chars, uppercase, lowercase, and a number."
            isValid = false
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password."
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match."
            isValid = false
        }
        
        if (!agreedToTerms) {
            termsError = "You must agree to the terms and conditions."
            isValid = false
        }
        
        return isValid
    }
    
    fun handleNextStage() {
        when (currentStage) {
            1 -> if (validateStage1()) currentStage = 2
            2 -> if (validateStage2()) currentStage = 3
            3 -> if (validateStage3()) {
                // All stages validated, proceed with registration
                val nameParts = fullName.trim().split("\\s+".toRegex())
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = nameParts.drop(1).joinToString(" ")
                val selectedRole = roleMap[tabs[selectedTabIndex]] ?: "JOUEUR"
                
                val birthDate = DISPLAY_DATE_FORMAT.parse(birthDateText)
                if (birthDate != null) {
                    val apiBirthDate = API_DATE_FORMAT.format(birthDate)
                    viewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber,
                        birthDate = apiBirthDate,
                        role = selectedRole,
                        password = password
                    )
                }
            }
        }
    }
    
    fun handlePreviousStage() {
        if (currentStage > 1) {
            currentStage--
        }
    }

    // ------------------------------------

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Sign Up", color = primaryTextColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardSurfaceColor)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
            ) {
                // --- Tabs for Role Selection ---
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = cardSurfaceColor,
                    contentColor = primaryColor
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    "Signup as $title",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
                
                // --- Progress Indicator ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..3).forEach { stage ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentStage >= stage) primaryColor else secondaryTextColor.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stage.toString(),
                                    color = if (currentStage >= stage) Color.White else secondaryTextColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (stage < 3) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(2.dp)
                                        .background(
                                            if (currentStage > stage) primaryColor else secondaryTextColor.copy(alpha = 0.3f)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
                
                // --- Form Content (Scrollable) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(cardSurfaceColor)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // --- Stage 1: Name and Username ---
                    if (currentStage == 1) {
                        Text(
                            text = "Personal Information",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter your full name and choose a username",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Full Name Field (Combined)
                        TextField(
                            value = fullName,
                            onValueChange = { fullName = it; fullNameError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Full Name (First and Last)", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true,
                            isError = fullNameError != null,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                cursorColor = primaryColor,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Person Icon",
                                    tint = if (fullNameError != null) errorColor else secondaryTextColor
                                )
                            }
                        )
                        if (fullNameError != null) {
                            Text(
                                fullNameError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Username Field
                        TextField(
                            value = username,
                            onValueChange = { username = it; usernameError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Username", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            singleLine = true,
                            isError = usernameError != null,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                cursorColor = primaryColor,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Username Icon",
                                    tint = if (usernameError != null) errorColor else secondaryTextColor
                                )
                            }
                        )
                        if (usernameError != null) {
                            Text(
                                usernameError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                    
                    // --- Stage 2: Contact Information ---
                    if (currentStage == 2) {
                        Text(
                            text = "Contact Information",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter your contact details",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Email Field
                        TextField(
                            value = email,
                            onValueChange = { email = it; emailError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Valid email", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = emailError != null,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                cursorColor = primaryColor,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = if (emailError != null) errorColor else secondaryTextColor
                                )
                            }
                        )
                        if (emailError != null) {
                            Text(
                                emailError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Phone Number Field
                        TextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) { phoneNumber = it; phoneError = null } },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Phone number (8 digits)", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            isError = phoneError != null,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                cursorColor = primaryColor,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = if (phoneError != null) errorColor else secondaryTextColor
                                )
                            }
                        )
                        if (phoneError != null) {
                            Text(
                                phoneError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Birth Date Field
                        TextField(
                            value = birthDateText,
                            onValueChange = { /* Prevent manual entry */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            placeholder = { Text("Birth date (DD/MM/YYYY)", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            enabled = false,
                            isError = birthDateError != null,
                            colors = TextFieldDefaults.colors(
                                disabledIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                disabledTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }, enabled = !uiState.isLoading) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Calendar Icon",
                                        tint = if (birthDateError != null) errorColor else secondaryTextColor
                                    )
                                }
                            }
                        )
                        if (birthDateError != null) {
                            Text(
                                birthDateError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                    
                    // --- Stage 3: Security ---
                    if (currentStage == 3) {
                        Text(
                            text = "Security",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a strong password",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Password Field
                        TextField(
                            value = password,
                            onValueChange = { password = it; passwordError = null; if (confirmPassword.isNotEmpty()) confirmPasswordError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Strong password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = passwordError != null,
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
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = if (passwordError != null) errorColor else secondaryTextColor
                                    )
                                }
                            }
                        )
                        if (passwordError != null) {
                            Text(
                                passwordError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Confirm Password Field
                        TextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; confirmPasswordError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Confirm password", color = secondaryTextColor.copy(alpha = 0.7f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = confirmPasswordError != null,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = inputBackgroundColor,
                                unfocusedContainerColor = inputBackgroundColor,
                                disabledContainerColor = inputBackgroundColor,
                                cursorColor = primaryColor,
                                focusedTextColor = primaryTextColor,
                                unfocusedTextColor = primaryTextColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                                        tint = if (confirmPasswordError != null) errorColor else secondaryTextColor
                                    )
                                }
                            }
                        )
                        if (confirmPasswordError != null) {
                            Text(
                                confirmPasswordError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Terms Checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreedToTerms,
                                onCheckedChange = { agreedToTerms = it; termsError = null },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryColor,
                                    uncheckedColor = if (termsError != null) errorColor else secondaryTextColor
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            val termsText = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 12.sp)) {
                                    append("By checking the box you agree to our ")
                                }
                                withStyle(style = SpanStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                    append("Terms")
                                }
                                withStyle(style = SpanStyle(color = primaryTextColor, fontSize = 12.sp)) {
                                    append(" and ")
                                }
                                withStyle(style = SpanStyle(color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                                    append("Conditions")
                                }
                            }
                            Text(text = termsText, modifier = Modifier.padding(start = 8.dp))
                        }
                        if (termsError != null) {
                            Text(
                                termsError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // --- Navigation Buttons ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous Button
                        if (currentStage > 1) {
                            OutlinedButton(
                                onClick = { handlePreviousStage() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = primaryColor
                                ),
                                border = BorderStroke(1.dp, primaryColor)
                            ) {
                                Text("Previous", color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        // Next/Register Button
                        Button(
                            onClick = { handleNextStage() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(start = if (currentStage > 1) 8.dp else 0.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = if (currentStage == 3) "Register" else "Next",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = if (currentStage == 3) "Register" else "Next",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Track registration success when message appears
                    LaunchedEffect(uiState.errorMessage) {
                        if (uiState.errorMessage != null) {
                            val message = uiState.errorMessage
                            if (message.contains("Account created successfully", ignoreCase = true) || 
                                message.contains("check your email", ignoreCase = true)) {
                                registrationSuccessful = true
                            }
                            // Also show resend success messages
                            if (message.contains("re-sent", ignoreCase = true) || 
                                message.contains("resent", ignoreCase = true)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    }

                    // --- Resend Verification Email Button (shown after successful registration) ---
                    if (registrationSuccessful && !uiState.isAuthenticated && email.isNotBlank()) {
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
                            enabled = !uiState.isLoading
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

                    // --- 5. Social Login Buttons (Side-by-Side Row) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Button
                        OutlinedButton(
                            onClick = { /* Handle Google Signup */ },
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
                                imageVector = Icons.Default.Search,
                                contentDescription = "Signup with Google",
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                tint = Color(0xFF4285F4)
                            )
                            Text(
                                text = "Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Facebook Button
                        OutlinedButton(
                            onClick = { /* Handle Facebook Signup */ },
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
                                imageVector = Icons.Default.Star,
                                contentDescription = "Signup with Facebook",
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
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

                    // --- 6. Already a member? Login in (Dynamic Colors) ---
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Already a member? ", color = primaryTextColor, fontSize = 14.sp)
                        TextButton(onClick = { navController.navigate("LoginScreen") }) {
                            Text(text = "Login in", color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun SignupScreenPreview() {
    tn.esprit.dam.ui.theme.DAMTheme(darkTheme = false) {
        SignupScreen(
            navController = rememberNavController()
        )
    }
}
