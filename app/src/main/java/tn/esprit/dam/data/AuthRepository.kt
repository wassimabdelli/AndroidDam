package tn.esprit.dam.data

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import tn.esprit.dam.models.AuthResponse
import tn.esprit.dam.models.LoginDto
import tn.esprit.dam.models.RegisterDto
import tn.esprit.dam.models.ErrorResponse
import tn.esprit.dam.models.ResendVerificationDto
import tn.esprit.dam.models.VerifyEmailDto
import tn.esprit.dam.models.ResetPasswordDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// 1. Setup DataStore for saving the JWT token
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val app: Application) {

    // Define DataStore keys
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("jwt_token") // CHANGED from "auth_token" to "jwt_token" to match RetrofitClient
        private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")
        // Pending verification email key, also used to temporarily store the email during password reset
        private val PENDING_EMAIL_KEY = stringPreferencesKey("pending_verification_email")
        // Forgot password context keys
        private val FORGOT_PASSWORD_EMAIL_KEY = stringPreferencesKey("forgot_password_email")
        private val FORGOT_PASSWORD_CODE_KEY = stringPreferencesKey("forgot_password_code")
        private val USER_JSON_KEY = stringPreferencesKey("user_json")
    }

    // Get the Retrofit service instance
    private val authService: AuthService = RetrofitClient.authService

    // --- Token Management ---
    suspend fun saveToken(token: String) {
        app.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return app.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }.first()
    }

    suspend fun clearToken() {
        app.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

    // --- Remember Me Management ---
    suspend fun saveRememberMe(rememberMe: Boolean) {
        app.dataStore.edit { preferences ->
            preferences[REMEMBER_ME_KEY] = rememberMe
        }
    }

    suspend fun getRememberMe(): Boolean {
        return app.dataStore.data.map { preferences ->
            preferences[REMEMBER_ME_KEY] ?: false
        }.first()
    }

    suspend fun clearRememberMe() {
        app.dataStore.edit { preferences ->
            preferences.remove(REMEMBER_ME_KEY)
        }
    }

    // --- Pending Email Management (for Verification/Password Reset) ---
    suspend fun savePendingVerificationEmail(email: String?) {
        app.dataStore.edit { preferences ->
            if (email == null) preferences.remove(PENDING_EMAIL_KEY)
            else preferences[PENDING_EMAIL_KEY] = email
        }
    }

    suspend fun getPendingVerificationEmail(): String? {
        return app.dataStore.data.map { preferences ->
            preferences[PENDING_EMAIL_KEY]
        }.first()
    }

    suspend fun clearPendingVerificationEmail() {
        app.dataStore.edit { preferences ->
            preferences.remove(PENDING_EMAIL_KEY)
        }
    }

    // --- Forgot Password Context Management ---
    suspend fun saveForgotPasswordContext(email: String?, code: String?) {
        app.dataStore.edit { preferences ->
            if (email == null) preferences.remove(FORGOT_PASSWORD_EMAIL_KEY)
            else preferences[FORGOT_PASSWORD_EMAIL_KEY] = email
            if (code == null) preferences.remove(FORGOT_PASSWORD_CODE_KEY)
            else preferences[FORGOT_PASSWORD_CODE_KEY] = code
        }
    }

    suspend fun getForgotPasswordContext(): Pair<String?, String?> {
        val prefs = app.dataStore.data.first()
        val email = prefs[FORGOT_PASSWORD_EMAIL_KEY]
        val code = prefs[FORGOT_PASSWORD_CODE_KEY]
        return Pair(email, code)
    }

    suspend fun clearForgotPasswordContext() {
        app.dataStore.edit { preferences ->
            preferences.remove(FORGOT_PASSWORD_EMAIL_KEY)
            preferences.remove(FORGOT_PASSWORD_CODE_KEY)
        }
    }

    // --- User Management ---
    suspend fun saveUser(user: tn.esprit.dam.models.User?) {
        app.dataStore.edit { preferences ->
            if (user == null) {
                preferences.remove(USER_JSON_KEY)
            } else {
                preferences[USER_JSON_KEY] = Json.encodeToString(tn.esprit.dam.models.User.serializer(), user)
            }
        }
    }

    suspend fun getUser(): tn.esprit.dam.models.User? {
        val json = app.dataStore.data.map { it[USER_JSON_KEY] }.first()
        return if (json.isNullOrBlank()) null else Json.decodeFromString(tn.esprit.dam.models.User.serializer(), json)
    }

    suspend fun clearUser() {
        app.dataStore.edit { it.remove(USER_JSON_KEY) }
    }

    // --- API Calls: Authentication ---
    suspend fun login(credentials: LoginDto): Result<AuthResponse> {
        val startTime = System.currentTimeMillis()
        return try {
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Log.d("AuthRepository", "=== LOGIN START ===")
            Log.d("AuthRepository", "Logging in with email: ${credentials.email}")
            Log.d("AuthRepository", "Start time: ${java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(startTime))}")

            // Add timeout wrapper (90 seconds - Render free tier can take time to wake up)
            Log.d("AuthRepository", "Calling authService.login() with 90s timeout...")
            Log.d("AuthRepository", "Note: Render free tier servers may take 30-60s to wake up")
            val httpResponse = withTimeout(90_000) {
                authService.login(credentials)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: throw IOException("Login successful but response body is null.")

            val elapsedTime = System.currentTimeMillis() - startTime
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Log.d("AuthRepository", "=== LOGIN API CALL SUCCEEDED ===")
            Log.d("AuthRepository", "Response received after ${elapsedTime}ms")

            // Normalize user object: prefer response.user or response.data
            val user = response.user
            val finalResponse = response
            Log.d("AuthRepository", "Normalized response user: ${finalResponse.user}")
            val tokenToSave = finalResponse.access_token
            Log.d("AuthRepository", "Token in response: access_token=${finalResponse.access_token}")
            if (!tokenToSave.isNullOrBlank()) {
                Log.d("AuthRepository", "Token found in response, saving to DataStore")
                saveToken(tokenToSave)
            } else {
                Log.w("AuthRepository", "⚠️ No token in login response body. Proceeding (assuming session/cookie auth).")
            }

            // Save user info after login
            saveUser(response.user)

            Log.d("AuthRepository", "=== LOGIN SUCCESS ===")
            Log.d("AuthRepository", "═══════════════════════════════════════════════════")
            Result.success(finalResponse)

        } catch (e: Exception) {
            val elapsedTime = System.currentTimeMillis() - startTime
            Log.e("AuthRepository", "═══════════════════════════════════════════════════")
            Log.e("AuthRepository", "=== LOGIN FAILED ===")
            Log.e("AuthRepository", "Failed after ${elapsedTime}ms")
            e.printStackTrace()

            if (e is TimeoutCancellationException) {
                Log.e("AuthRepository", "❌ Login request timed out after ${elapsedTime}ms (90s limit)")
                Result.failure(Exception("Le serveur met trop de temps à répondre. Si c'est un serveur Render en veille, la première requête peut prendre 30-60 secondes. Réessayez dans quelques instants."))
            } else {
                Result.failure(handleNetworkException(e, "Login failed"))
            }
        }
    }

    suspend fun register(userData: RegisterDto): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== REGISTRATION START ===")
            Log.d("AuthRepository", "Registering user with email: ${userData.email}")

            // Add timeout wrapper (90 seconds total timeout)
            val httpResponse = withTimeout(90_000) {
                authService.register(userData)
            }

            // Check if response is successful (200-299)
            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            Log.d("AuthRepository", "=== API CALL SUCCEEDED ===")

            val tokenToSave = response.access_token
            Log.d("AuthRepository", "Token in response: access_token=${response.access_token}")
            if (!tokenToSave.isNullOrBlank()) {
                Log.d("AuthRepository", "Token found, saving to DataStore")
                saveToken(tokenToSave)
            } else {
                Log.d("AuthRepository", "No token in response (expected for email verification flow)")
            }

            // Save user info after registration
            saveUser(response.user)

            // Persist pending email so the verification screen can access it
            val responseEmail = response.user?.email ?: userData.email
            if (!responseEmail.isNullOrBlank()) {
                Log.d("AuthRepository", "Persisting pending verification email to DataStore: $responseEmail")
                savePendingVerificationEmail(responseEmail)
            }

            Log.d("AuthRepository", "=== REGISTRATION SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== REGISTRATION FAILED ===")
            e.printStackTrace()

            if (e is TimeoutCancellationException) {
                Log.e("AuthRepository", "Request timed out after 90 seconds")
                Result.failure(Exception("Request timed out. The server is taking too long to respond. Please check your internet connection and try again."))
            } else {
                Result.failure(handleNetworkException(e, "Registration failed"))
            }
        }
    }

    // --- API Calls: Email Verification ---

    suspend fun resendVerification(email: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== RESEND VERIFICATION START ===")

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Invalid email address format."))
            }

            val emailDto = ResendVerificationDto(email = email.trim())

            val httpResponse = withTimeout(60_000) {
                authService.resendVerification(emailDto)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            Log.d("AuthRepository", "=== RESEND SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== RESEND FAILED ===")
            e.printStackTrace()

            if (e is TimeoutCancellationException) {
                Result.failure(Exception("Request timed out. Please try again."))
            } else {
                Result.failure(handleNetworkException(e, "Resending verification code failed"))
            }
        }
    }

    suspend fun verifyEmail(code: String, email: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== VERIFY EMAIL START ===")

            val dto = VerifyEmailDto(code = code, email = email)

            val httpResponse = withTimeout(60_000) {
                authService.verifyEmail(dto)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            // Clear pending email after successful verification
            clearPendingVerificationEmail()

            Log.d("AuthRepository", "=== VERIFY EMAIL SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== VERIFY EMAIL FAILED ===")
            e.printStackTrace()
            if (e is TimeoutCancellationException) {
                Result.failure(Exception("Request timed out. Please try again."))
            } else {
                Result.failure(handleNetworkException(e, "Verification failed"))
            }
        }
    }

    // --- API Calls: Forgot Password Flow ---

    /**
     * Step 1: Request a password reset code for the given email.
     * Maps to: POST /api/v1/auth/forgot-password
     */
    suspend fun forgotPassword(email: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 1: SEND CODE) START ===")

            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Invalid email address format."))
            }

            val dto = ResendVerificationDto(email = email.trim())

            val httpResponse = withTimeout(60_000) {
                authService.forgotPassword(dto)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            // Persist the email so the next step (Verify Code) can access it
            savePendingVerificationEmail(email.trim())

            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 1) SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== FORGOT PASSWORD (STEP 1) FAILED ===")
            e.printStackTrace()
            if (e is TimeoutCancellationException) {
                Result.failure(Exception("Request timed out. Please try again."))
            } else {
                Result.failure(handleNetworkException(e, "Forgot password failed (Sending code)"))
            }
        }
    }

    /**
     * Step 2: Verify the password reset code.
     * Maps to: POST /api/v1/auth/forgot-password/verify-code
     */
    suspend fun verifyForgotPasswordCode(code: String, email: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 2: VERIFY CODE) START ===")

            val dto = VerifyEmailDto(code = code, email = email)

            val httpResponse = withTimeout(60_000) {
                authService.verifyForgotPasswordCode(dto)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            // We keep the email in PENDING_EMAIL_KEY for the final reset step (Step 3)

            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 2) SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== FORGOT PASSWORD (STEP 2) FAILED ===")
            e.printStackTrace()
            if (e is TimeoutCancellationException) {
                Result.failure(Exception("Request timed out. Please try again."))
            } else {
                Result.failure(handleNetworkException(e, "Verify forgot code failed"))
            }
        }
    }

    /**
     * Step 3: Reset the password using the email, code, and new password.
     * Maps to: POST /api/v1/auth/forgot-password/reset
     */
    suspend fun resetForgotPassword(email: String, code: String, newPassword: String, confirmPassword: String): Result<AuthResponse> {
        return try {
            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 3: RESET) START ===")

            val dto = ResetPasswordDto(email = email, code = code, newPassword = newPassword, confirmPassword = confirmPassword)

            val httpResponse = withTimeout(60_000) {
                authService.resetForgotPassword(dto)
            }

            if (!httpResponse.isSuccessful) {
                throw HttpException(httpResponse)
            }

            val response = httpResponse.body() ?: AuthResponse()

            // Clear pending email after successful password reset
            clearPendingVerificationEmail()

            Log.d("AuthRepository", "=== FORGOT PASSWORD (STEP 3) SUCCESS ===")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== FORGOT PASSWORD (STEP 3) FAILED ===")
            e.printStackTrace()
            if (e is TimeoutCancellationException) {
                Result.failure(Exception("Request timed out. Please try again."))
            } else {
                Result.failure(handleNetworkException(e, "Reset password failed"))
            }
        }
    }

    suspend fun fetchUserById(id: String): tn.esprit.dam.models.User? {
        val response = authService.getUserById(id)
        return if (response.isSuccessful) response.body() else null
    }

    // --- Shared Exception Handling Logic ---

    private suspend fun handleNetworkException(e: Exception, contextMessage: String): Exception {
        return withContext(Dispatchers.Main) {
            when (e) {
                is HttpException -> {
                    Log.e("AuthRepository", "HTTP Exception: Code ${e.code()}")
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        Log.e("AuthRepository", "Failed to read error body: ${ex.message}")
                        null
                    }
                    Log.e("AuthRepository", "Error body: $errorBody")

                    val serverMessage = try {
                        if (errorBody != null && errorBody.isNotBlank()) {
                            try {
                                // Attempt to parse as standard ErrorResponse
                                val errorResponse = Json.decodeFromString<ErrorResponse>(errorBody)
                                errorResponse.message ?: errorResponse.error ?: errorBody
                            } catch (parseError: Exception) {
                                // If parsing fails, try manual extraction (for non-standard API responses)
                                Log.e("AuthRepository", "Failed to parse as ErrorResponse, trying manual extraction")
                                val messageMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)
                                messageMatch?.groupValues?.get(1) ?: errorBody
                            }
                        } else {
                            null
                        }
                    } catch (jsonError: Exception) {
                        Log.e("AuthRepository", "Failed to parse error JSON: ${jsonError.message}")
                        errorBody
                    }

                    // Provide user-friendly error messages based on status code and server response
                    val userFriendlyMessage = when (e.code()) {
                        409 -> serverMessage ?: "This email is already registered. Please use a different email or try logging in."
                        400 -> serverMessage ?: "Invalid request. Please check your input."
                        401 -> serverMessage ?: "Authentication failed. Please check your credentials."
                        403 -> serverMessage ?: "Access denied. You don't have permission to perform this action."
                        404 -> serverMessage ?: "Resource not found."
                        500 -> serverMessage ?: "Server error. Please try again later."
                        else -> serverMessage ?: e.message() ?: "An error occurred. Please try again."
                    }

                    Log.e("AuthRepository", "Final error message: $userFriendlyMessage")
                    Exception(userFriendlyMessage)
                }
                is SocketTimeoutException -> {
                    Log.e("AuthRepository", "SocketTimeoutException: ${e.message}")
                    Exception("Network Error: Request timed out. The server might be waking up. Please try again.")
                }
                is UnknownHostException -> {
                    Log.e("AuthRepository", "UnknownHostException: ${e.message}")
                    val errorMsg = e.message ?: ""
                    when {
                        errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
                                errorMsg.contains("No address associated with hostname", ignoreCase = true) -> {
                            Exception("Erreur de connexion: Impossible de joindre le serveur.\n\nVérifiez:\n• Votre connexion Internet\n• Si le serveur est en ligne (Render peut être en veille)\n• Réessayez dans quelques instants")
                        }
                        else -> {
                            Exception("Erreur réseau: Impossible de résoudre le nom du serveur. Vérifiez votre connexion Internet.")
                        }
                    }
                }
                is IOException -> {
                    Log.e("AuthRepository", "IOException: ${e.message}")
                    val errorMsg = e.message ?: ""
                    when {
                        errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
                                errorMsg.contains("No address associated with hostname", ignoreCase = true) -> {
                            Exception("Erreur de connexion: Impossible de joindre le serveur.\n\nVérifiez:\n• Votre connexion Internet\n• Si le serveur est en ligne (Render peut être en veille)\n• Réessayez dans quelques instants")
                        }
                        errorMsg.contains("timeout", ignoreCase = true) -> {
                            Exception("Délai d'attente dépassé. Le serveur met trop de temps à répondre. Réessayez dans quelques instants.")
                        }
                        else -> {
                            Exception("Erreur réseau: ${e.message ?: "Vérifiez votre connexion Internet."}")
                        }
                    }
                }
                is SerializationException -> {
                    Log.e("AuthRepository", "SerializationException: ${e.message}")
                    Log.e("AuthRepository", "This usually means the API response structure doesn't match AuthResponse")
                    Exception("Data Error: Failed to parse server response. (JSON Mismatch) - The server response format may have changed.")
                }
                else -> {
                    Log.e("AuthRepository", "Unknown exception: ${e.javaClass.simpleName} - ${e.message}")
                    e.printStackTrace()
                    Exception("$contextMessage: ${e.message ?: "An unknown error occurred."}")
                }
            }
        }
    }
}