package tn.esprit.dam.models

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.esprit.dam.data.AuthRepository

// --- Define the state the UI will observe ---
data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val user: AuthResponse? = null // This holds the whole response, including User
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize the repository, which requires the application context for DataStore
    private val repository = AuthRepository(application)

    // The state holder that the Composable will collect and react to
    var uiState by mutableStateOf(AuthUiState())
        private set

    // Holds the email address pending verification (set after registration) - make it observable
    var pendingVerificationEmail by mutableStateOf<String?>(null)
        private set

    // Indicates whether the current pending verification is for the forgot-password reset flow
    var pendingIsForPasswordReset by mutableStateOf(false)
        private set

    // When forgot-password code is verified, store the email and code so SetNewPasswordScreen can use them
    var forgotVerifiedEmail by mutableStateOf<String?>(null)
        private set
    var forgotVerifiedCode by mutableStateOf<String?>(null)
        private set

    // --- Core Authentication Logic (Login) ---

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, isAuthenticated = false, errorMessage = null)

        viewModelScope.launch {
            val credentials = LoginDto(email = email, password = password)
            val result = repository.login(credentials)

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== LOGIN SUCCESS ===")
                    Log.d("AuthViewModel", "Response: $response")
                    Log.d("AuthViewModel", "AccessToken: ${response.access_token}")
                    Log.d("AuthViewModel", "User: ${response.user}")

                    if (rememberMe) {
                        repository.saveRememberMe(true)
                        Log.d("AuthViewModel", "✅ Remember me preference saved: true")
                    } else {
                        repository.clearRememberMe()
                        Log.d("AuthViewModel", "❌ Remember me preference cleared (user unchecked)")
                    }

                    // Save user to DataStore (ALWAYS use the latest user from fetchUserById if available)
                    val userId = response.user?._id
                    val userToSave = if (!userId.isNullOrBlank()) {
                        val fetchedUser = repository.fetchUserById(userId)
                        fetchedUser ?: response.user
                    } else {
                        response.user
                    }
                    repository.saveUser(userToSave)

                    val updatedResponse = response.copy(user = userToSave)
                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = !response.access_token.isNullOrBlank(),
                        user = updatedResponse,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = error.message ?: "Login failed. Please check credentials."
                    )
                    println("Login Failure: ${error.message}")
                }
            )
        }
    }

    // --- Core Authentication Logic (Registration) ---
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String, // Still String input from UI
        birthDate: String,
        role: String,
        password: String
    ) {
        if (uiState.isLoading) return

        // 🏆 FIX: PRE-FLIGHT VALIDATION CHECK
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || phoneNumber.isBlank() || birthDate.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "All fields (Name, Email, Phone, Birth Date, Password) are required.")
            return
        }

        uiState = uiState.copy(isLoading = true, isAuthenticated = false, errorMessage = null)

        viewModelScope.launch {
            // FIX: The RegisterDto's parameter names were updated (likely in another file)
            // The DTO now expects 'firstName', 'lastName', 'phoneNumber', and 'birthDate'.
            val userData = RegisterDto(
                firstName = firstName,    // Correct parameter name is now 'firstName'
                lastName = lastName,      // Correct parameter name is now 'lastName'
                email = email,
                phoneNumber = phoneNumber,  // <-- Passed as Long now
                birthDate = birthDate,    // Correct parameter name is now 'birthDate'
                role = role,
                password = password
            )

            Log.d("AuthViewModel", "Calling repository.register()")
            val result = repository.register(userData)
            Log.d("AuthViewModel", "Repository call completed")

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== REGISTRATION SUCCESS IN VIEWMODEL ===")
                    Log.d("AuthViewModel", "Response: $response")
                    Log.d("AuthViewModel", "AccessToken: ${response.access_token}")
                    Log.d("AuthViewModel", "User: ${response.user}")

                    val hasToken = !response.access_token.isNullOrBlank()
                    Log.d("AuthViewModel", "Has token: $hasToken")

                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = hasToken,
                        user = response
                    )

                    val finalMessage = if (hasToken) {
                        "Registration successful! You are now logged in."
                    } else {
                        "Account created successfully. Please check your email for the verification link."
                    }

                    Log.d("AuthViewModel", "Final message: $finalMessage")
                    uiState = uiState.copy(errorMessage = finalMessage)
                    Log.d("AuthViewModel", "UI State updated with message")
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "=== REGISTRATION FAILURE IN VIEWMODEL ===")
                    Log.e("AuthViewModel", "Error: ${error.message}")
                    Log.e("AuthViewModel", "Error type: ${error.javaClass.simpleName}")
                    error.printStackTrace()

                    uiState = uiState.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        errorMessage = error.message ?: "Registration failed due to an unknown error."
                    )
                    Log.e("AuthViewModel", "UI State updated with error message: ${uiState.errorMessage}")
                }
            )
        }
    }

    // --- New: Resend Verification Email (Restored) ---

    fun resendVerificationEmail(email: String) {
        if (uiState.isLoading) return
        Log.d("AuthViewModel", "=== RESEND VERIFICATION EMAIL START ===")
        Log.d("AuthViewModel", "Email: $email")
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.resendVerification(email)
            Log.d("AuthViewModel", "Resend repository call completed")

            result.fold(
                onSuccess = { response ->
                    Log.d("AuthViewModel", "=== RESEND SUCCESS IN VIEWMODEL ===")
                    Log.d("AuthViewModel", "Response: $response")

                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Verification email re-sent successfully. Check your inbox."
                    )
                    Log.d("AuthViewModel", "Resend Success: Verification email re-sent successfully. Check your inbox.")
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "=== RESEND FAILURE IN VIEWMODEL ===")
                    Log.e("AuthViewModel", "Error: ${error.message}")
                    Log.e("AuthViewModel", "Error type: ${error.javaClass.simpleName}")
                    error.printStackTrace()

                    val errorMessage = error.message ?: "Failed to re-send verification email. Please contact support."
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                    Log.d("AuthViewModel", "Resend Failure: $errorMessage")
                }
            )
        }
    }

    // --- Verify Email ---
    fun verifyEmail(token: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            // Only use fields that exist in AuthResponse: user, access_token
            var emailToVerify = pendingVerificationEmail
                ?: uiState.user?.user?.email
                ?: uiState.user?.let { it.user?.email }

            if (emailToVerify.isNullOrBlank()) {
                try {
                    val persisted = repository.getPendingVerificationEmail()
                    if (!persisted.isNullOrBlank()) emailToVerify = persisted
                } catch (ex: Exception) {
                    Log.e("AuthViewModel", "[VERIFY] Failed to read persisted pending email: ${ex.message}")
                }
            }

            if (emailToVerify.isNullOrBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Email address not found. Please register again.")
                onComplete(false, "Email address not found")
                return@launch
            }

            val result = repository.verifyEmail(token, emailToVerify)
            result.fold(
                onSuccess = { response ->
                    // Only use .user from AuthResponse
                    val verifiedUser = response.user?.copy(isVerified = true, emailVerified = true)
                    val newAuthResp = response.copy(user = verifiedUser)
                    uiState = uiState.copy(isLoading = false, user = newAuthResp, errorMessage = null)
                    pendingVerificationEmail = null
                    try { repository.clearPendingVerificationEmail() } catch (ex: Exception) { }
                    onComplete(true, "Verification successful.")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Verification failed.")
                    onComplete(false, error.message ?: "Verification failed.")
                }
            )
        }
    }

    // --- Additional helper to extract email from AuthResponse ---
    private fun AuthResponse.extractEmail(): String? {
        return this.user?.email
    }

    // --- Load persisted pending verification email from DataStore into memory ---
    fun loadPendingVerificationEmail() {
        viewModelScope.launch {
            try {
                val persisted = repository.getPendingVerificationEmail()
                Log.d("AuthViewModel", "[LOAD] persisted pendingVerificationEmail on startup = $persisted")
                if (!persisted.isNullOrBlank()) {
                    pendingVerificationEmail = persisted
                }
            } catch (ex: Exception) {
                Log.e("AuthViewModel", "[LOAD] Failed to load persisted pending email: ${ex.message}")
            }
        }
    }

    // --- Other utility functions ---

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // --- Check Authentication State on Startup ---
    fun checkAuthState() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val token = repository.getToken()
            val rememberMe = repository.getRememberMe()
            val user = repository.getUser()

            Log.d("AuthViewModel", "=== CHECKING AUTH STATE ===")
            Log.d("AuthViewModel", "Token present: ${!token.isNullOrBlank()}")
            Log.d("AuthViewModel", "Token value: ${if (token.isNullOrBlank()) "null/empty" else "present"}")
            Log.d("AuthViewModel", "Remember me: $rememberMe")

            // Load persisted pending verification email as part of startup checks
            try {
                val persisted = repository.getPendingVerificationEmail()
                Log.d("AuthViewModel", "[CHECK] persisted pendingVerificationEmail = $persisted")
                if (!persisted.isNullOrBlank()) pendingVerificationEmail = persisted
            } catch (ex: Exception) {
                Log.e("AuthViewModel", "[CHECK] Failed to read persisted pending email: ${ex.message}")
            }

            if (rememberMe && !token.isNullOrBlank()) {
                // User is remembered and has a token, restore authentication state
                Log.d("AuthViewModel", "✅ User is remembered with valid token, restoring auth state")
                uiState = uiState.copy(
                    isAuthenticated = true,
                    isLoading = false,
                    user = if (user != null) AuthResponse(access_token = token, user = user) else null
                )
            } else {
                // Clear remember me if token is missing
                if (rememberMe && token.isNullOrBlank()) {
                    Log.d("AuthViewModel", "⚠️ Remember me is true but token is missing, clearing remember me")
                    repository.clearRememberMe()
                }
                Log.d("AuthViewModel", "❌ User not remembered or no token, setting isAuthenticated = false")
                uiState = uiState.copy(
                    isAuthenticated = false,
                    isLoading = false
                )
            }
        }
    }

    // --- Logout ---
    fun logout() {
        viewModelScope.launch {
            repository.clearToken()
            repository.clearRememberMe()
            uiState = AuthUiState()
            Log.d("AuthViewModel", "User logged out, token and remember me cleared")
        }
    }

    // --- Forgot-password flow helpers ---
    fun forgotPassword(email: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.forgotPassword(email)
            result.fold(
                onSuccess = { response ->
                    pendingVerificationEmail = email
                    pendingIsForPasswordReset = true
                    try { repository.savePendingVerificationEmail(email) } catch (_: Exception) { }
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onComplete(true, "Code sent to email")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Failed to send code")
                    onComplete(false, error.message ?: "Failed to send code")
                }
            )
        }
    }

    fun verifyForgotPasswordCode(code: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            var emailToVerify = pendingVerificationEmail
            if (emailToVerify.isNullOrBlank()) {
                try {
                    val persisted = repository.getPendingVerificationEmail()
                    if (!persisted.isNullOrBlank()) emailToVerify = persisted
                } catch (ex: Exception) { }
            }
            if (emailToVerify.isNullOrBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Email address not found. Please restart the forgot password flow.")
                onComplete(false, "Email address not found. Please restart the forgot password flow.")
                return@launch
            }
            val result = repository.verifyForgotPasswordCode(code, emailToVerify)
            result.fold(
                onSuccess = { response ->
                    forgotVerifiedEmail = emailToVerify
                    forgotVerifiedCode = code
                    try { repository.saveForgotPasswordContext(emailToVerify, code) } catch (_: Exception) { }
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onComplete(true, "Code verified")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Verification failed")
                    onComplete(false, error.message ?: "Verification failed")
                }
            )
        }
    }

    fun resetPassword(newPassword: String, confirmPassword: String, onComplete: (success: Boolean, message: String?) -> Unit) {
        if (uiState.isLoading) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            var email = forgotVerifiedEmail
            var code = forgotVerifiedCode
            if (email.isNullOrBlank() || code.isNullOrBlank()) {
                try {
                    val (persistedEmail, persistedCode) = repository.getForgotPasswordContext()
                    if (!persistedEmail.isNullOrBlank() && !persistedCode.isNullOrBlank()) {
                        email = persistedEmail
                        code = persistedCode
                        forgotVerifiedEmail = email
                        forgotVerifiedCode = code
                    }
                } catch (_: Exception) { }
            }
            if (email.isNullOrBlank() || code.isNullOrBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Reset context missing. Please request a new code.")
                onComplete(false, "Reset context missing. Please request a new code.")
                return@launch
            }
            val result = repository.resetForgotPassword(email, code, newPassword, confirmPassword)
            result.fold(
                onSuccess = { _ ->
                    pendingVerificationEmail = null
                    pendingIsForPasswordReset = false
                    forgotVerifiedEmail = null
                    forgotVerifiedCode = null
                    try {
                        repository.clearPendingVerificationEmail()
                        repository.clearForgotPasswordContext()
                    } catch (_: Exception) { }
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onComplete(true, "Password changed successfully")
                },
                onFailure = { error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = error.message ?: "Reset failed")
                    onComplete(false, error.message ?: "Reset failed")
                }
            )
        }
    }

    // --- New: Fetch and Update User by ID ---
    fun fetchAndSetUserById(id: String) {
        viewModelScope.launch {
            val user = repository.fetchUserById(id)
            if (user != null) {
                uiState = uiState.copy(user = uiState.user?.copy(user = user))
            }
        }
    }
}