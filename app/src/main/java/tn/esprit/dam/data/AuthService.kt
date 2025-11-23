package tn.esprit.dam.data

import tn.esprit.dam.models.LoginDto
import tn.esprit.dam.models.RegisterDto
import tn.esprit.dam.models.AuthResponse
import tn.esprit.dam.models.ResendVerificationDto
import tn.esprit.dam.models.VerifyEmailDto
import tn.esprit.dam.models.ResetPasswordDto
import tn.esprit.dam.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {

    // User Authentication
    @POST("auth/register")
    suspend fun register(@Body userData: RegisterDto): Response<AuthResponse>

    // Updated to use Response for proper error handling
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginDto): Response<AuthResponse>

    // Social Authentication
    @GET("auth/google")
    suspend fun authGoogle(): Response<AuthResponse>

    @GET("auth/facebook")
    suspend fun authFacebook(): Response<AuthResponse>

    // Email Verification Flow (e.g., after Sign Up)
    // Endpoint: POST /api/v1/auth/verify-code
    @POST("auth/verify-code")
    suspend fun verifyEmail(@Body dto: VerifyEmailDto): Response<AuthResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body emailDto: ResendVerificationDto): Response<AuthResponse>

    // --- Forgot Password Flow (Maps to your NestJS endpoints) ---

    /**
     * 1. Request a password reset code.
     * Maps to: POST /api/v1/auth/forgot-password
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body emailDto: ResendVerificationDto): Response<AuthResponse>

    /**
     * 2. Verify the password reset code.
     * Maps to: POST /api/v1/auth/forgot-password/verify-code
     */
    @POST("auth/forgot-password/verify-code")
    suspend fun verifyForgotPasswordCode(@Body dto: VerifyEmailDto): Response<AuthResponse>

    /**
     * 3. Reset the password using the email, code, and new password.
     * Maps to: POST /api/v1/auth/forgot-password/reset
     */
    @POST("auth/forgot-password/reset")
    suspend fun resetForgotPassword(@Body dto: ResetPasswordDto): Response<AuthResponse>

    // Get User by ID
    @GET("api/v1/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    // Create Coupe (POST)
    @POST("api/v1/create-coupe")
    suspend fun createCoupe(@Body request: tn.esprit.dam.api.dto.CreateCoupeRequest): retrofit2.Response<Unit>
}