package tn.esprit.dam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Assuming Typography is defined elsewhere and is correct.

// --- Dark Theme Color Scheme (Using Dk_ colors) ---
private val DarkColorScheme = darkColorScheme(
    primary = Dk_Primary,          // Primary actions, buttons
    onPrimary = White,             // Text/Icon color on Primary
    secondary = Dk_Primary_Variant, // Secondary elements, floating buttons
    onSecondary = White,           // Text/Icon color on Secondary
    tertiary = Dk_Tertiary,        // Tertiary accent, optional
    onTertiary = Dk_Secondary,     // Text/Icon color on Tertiary

    background = Dk_Background,    // Main screen background
    onBackground = Dk_Text_On_Dark,// Primary text on background

    surface = Dk_Surface,          // Cards, sheets, containers
    onSurface = Dk_Text_On_Dark,   // Primary text on surface

    surfaceVariant = Dk_Surface,   // Variant for surfaces
    onSurfaceVariant = Dk_Text_On_Dark, // Text on surface variant

    outline = Dk_Outline,          // Borders, dividers
    error = ErrorRed
)

// --- Light Theme Color Scheme (Using Lt_ colors) ---
private val LightColorScheme = lightColorScheme(
    primary = Lt_Primary,          // Primary actions, buttons
    onPrimary = Lt_Text_On_Dark,   // Text/Icon color on Primary
    secondary = Lt_Secondary,      // Secondary elements, floating buttons
    onSecondary = Lt_Text_On_Dark, // Text/Icon color on Secondary
    tertiary = Lt_Primary_Variant, // Tertiary accent, optional
    onTertiary = Lt_Text_On_Dark,  // Text/Icon color on Tertiary

    background = Lt_Background,    // Main screen background
    onBackground = Lt_Text_On_Light,// Primary text on background

    surface = Lt_Surface,          // Cards, sheets, containers
    onSurface = Lt_Text_On_Light,  // Primary text on surface

    surfaceVariant = Lt_Surface,   // Variant for surfaces
    onSurfaceVariant = Lt_Text_On_Light, // Text on surface variant

    outline = Lt_Outline,          // Borders, dividers
    error = ErrorRed
)

@Composable
fun DAMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure this is correctly defined
        content = content
    )
}