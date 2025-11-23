package tn.esprit.dam.ui.theme


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 1. Define the Color Palette Structure
@Immutable
data class CustomColors(
    val primaryGreen: Color,
    val background: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color, // Used for grayed-out or secondary text
    val inputBackground: Color,
    val isLight: Boolean // Indicates which theme is currently active
)

// 2. Define the Light and Dark Color Schemes

// The primary color often remains constant
private val PrimaryGreen = Color(0xFF4CAF50)

// --- LIGHT COLORS ---
val LightCustomColors = CustomColors(
    primaryGreen = PrimaryGreen,
    background = Color(0xFFEFEFEF),      // Light Grey for outer background
    cardBackground = Color.White,        // White for main surface/card
    textPrimary = Color.Black,           // Black text
    textSecondary = Color.Gray,          // Standard Gray text
    inputBackground = Color(0xFFF5F5F5), // Lightest Grey for text fields
    isLight = true
)

// --- DARK COLORS ---
val DarkCustomColors = CustomColors(
    primaryGreen = PrimaryGreen,
    background = Color(0xFF1A1A2E),      // Dark Blue/Grey for outer background
    cardBackground = Color(0xFF2E2E4A),  // Darker Blue/Grey for main surface/card
    textPrimary = Color.White,           // White text
    textSecondary = Color(0xFFB0B0C0),   // Light Grey text
    inputBackground = Color(0xFF3A3A5A), // Dark Input Field background
    isLight = false
)

// 3. Create the Composition Local Provider
val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

// 4. Custom Theme Composable
@Composable
fun AppTheme(
    isDarkTheme: Boolean = false, // This state MUST be passed from a global source (like MainActivity/ViewModel)
    content: @Composable () -> Unit
) {
    val targetColors = if (isDarkTheme) DarkCustomColors else LightCustomColors

    // Animate all color changes smoothly
    val animatedColors = CustomColors(
        primaryGreen = targetColors.primaryGreen, // No animation on static color
        background = animateColorAsState(targetColors.background, tween(500)).value,
        cardBackground = animateColorAsState(targetColors.cardBackground, tween(500)).value,
        textPrimary = animateColorAsState(targetColors.textPrimary, tween(500)).value,
        textSecondary = animateColorAsState(targetColors.textSecondary, tween(500)).value,
        inputBackground = animateColorAsState(targetColors.inputBackground, tween(500)).value,
        isLight = targetColors.isLight
    )

    // Provide the animated colors to the Composition Local
    CompositionLocalProvider(LocalCustomColors provides animatedColors) {
        content()
    }
}

// 5. Utility accessor for composables
object AppTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}