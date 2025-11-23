package tn.esprit.dam.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- DTO for Authentication Responses ---
data class AuthResponse(
    val access_token: String? = null,
    val user: User? = null
)

@Serializable
data class User(
    val _id: String? = null,
    val prenom: String? = null,
    val nom: String? = null,
    val email: String? = null,
    val age: String? = null,
    val tel: String? = null,
    val role: String? = null,
    val emailVerified: Boolean? = null,
    val isVerified: Boolean? = null
)

// --- DTO for User Login Request ---
@Serializable
data class LoginDto(
    val email: String,
    val password: String
)

// --- DTO for User Registration Request ---
@Serializable
data class RegisterDto(
    // UI: firstName -> API: prenom
    @SerialName("prenom")
    val firstName: String,

    // UI: lastName -> API: nom
    @SerialName("nom")
    val lastName: String,

    // API: email
    val email: String,

    // FIX: Changed to String. Phone numbers should almost always be Strings.
    @SerialName("tel")
    val phoneNumber: String,

    // UI: birthDate -> API: age (This must contain the date in "YYYY-MM-DD" format as a String)
    @SerialName("age")
    val birthDate: String,

    // API: role
    val role: String,

    // API: password
    val password: String
)


// --- Resend Verification Email Request DTO (Also used for Forgot Password step 1) ---
@Serializable
data class ResendVerificationDto(
    val email: String
)

// DTO for verify email (OTP code) (Also used for Forgot Password step 2)
@Serializable
data class VerifyEmailDto(
    val code: String,      // The OTP code
    val email: String      // The email to identify which user to verify
)

// --- Error Response Model ---
@Serializable
data class ErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)

// DTO for resetting password after forgot-password verification (Forgot Password step 3)
@Serializable
data class ResetPasswordDto(
    val email: String,
    val code: String,
    val newPassword: String,
    val confirmPassword: String
)