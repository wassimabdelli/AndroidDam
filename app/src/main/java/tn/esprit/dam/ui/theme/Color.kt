package tn.esprit.dam.ui.theme

import androidx.compose.ui.graphics.Color

// --- 1. Base Colors (Updated to match request) ---
// --- LIGHT THEME PALETTE ---
// https://coolors.co/b3ddbe-fafdfb-60b17a-eff8f1-9faaa7-052224-85e4a0-1d1d1d-35944f-e5fee8
val Lt_Primary = Color(0xFF60B17A)       // Strong Primary Green
val Lt_Primary_Variant = Color(0xFF35944F) // Darker Primary (used for 'on' colors)
val Lt_Secondary = Color(0xFF85E4A0)     // Brighter Accent Green
val Lt_Background = Color(0xFFFAFDFA)    // Near White Base
val Lt_Surface = Color(0xFFEFF8F1)        // Off-White Surface (e.g., Cards)
val Lt_Text_On_Dark = Color(0xFFFAFDFA)   // White-ish text on dark surfaces
val Lt_Text_On_Light = Color(0xFF052224)  // Very Dark Text
val Lt_Outline = Color(0xFF9FAAA7)       // Subtle Grey/Outline
val Lt_Success = Color(0xFFB3DDBE)       // Lighter success/hint color

// --- DARK THEME PALETTE ---
// https://coolors.co/798785-547262-393433-ffffff-324a38-388e3c-28352e-b0eccc-1a241e-bbd7c6
val Dk_Primary = Color(0xFF388E3C)       // Primary Accent Green
val Dk_Primary_Variant = Color(0xFF547262) // Secondary/Variant Green
val Dk_Secondary = Color(0xFFB0ECCC)     // Bright Accent (used for 'on' colors)
val Dk_Background = Color(0xFF1A241E)    // Very Dark Greenish Base
val Dk_Surface = Color(0xFF28352E)        // Slightly Lighter Surface
val Dk_Text_On_Dark = Color(0xFFBBD7C6)   // Light, subtle text
val Dk_Text_On_Light = Color(0xFF393433)  // Dark text on light surfaces
val Dk_Outline = Color(0xFF798785)       // Subtle Grey/Outline
val Dk_Tertiary = Color(0xFF324A38)       // Tertiary Accent

// --- FUNCTIONAL / COMMON COLORS ---
val ErrorRed = Color(0xFFF44336) // Red for errors (kept standard for clarity)
val PrizeBlue = Color(0xFF03A9F4) // Blue accent (kept standard for clarity)

// Helper: Used for text color on primary/secondary
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// You can keep other specific colors if they are not theme-dependent,
// but it's best to use the standard ColorScheme for most UI elements.

// Example: Functional Colors (If they absolutely need to be outside the scheme)
val TabSelected = Lt_Primary
val TabUnselectedText = Dk_Text_On_Dark
// Dark Theme: Greyish Black Base
val DarkBackground = Color(0xFF1A1A1A) // Very Dark Grey
val DarkSurface = Color(0xFF2C2C2C)    // Slightly Lighter Dark Grey for Cards/Surfaces

// Light Theme: Whiteish Base
val LightBackground = Color(0xFFFAFAFA) // Near White Off-White
val LightSurface = Color(0xFFFFFFFF)    // Pure White

// --- 2. Text Colors ---

// Light Theme Text
val TextBlack = Color(0xFF1C1C1E)
val LightText = Color(0xFF000000)

// Dark Theme Text
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFFCCCCCC) // Used for subtle elements

// --- 3. Accent & Functional Colors (Updated to match request) ---

// Dark Green Variants (Used primarily in Dark Mode)
val DarkerGreen = Color(0xFF004D40) // Deep accent green
val MediumGreen = Color(0xFF1B5E20) // Primary Dark Forest Green
val LightGreen = Color(0xFF66BB6A)   // Bright accent on Dark Background

// Light Green Variants (Used primarily in Light Mode)
val LightPrimaryGreen = Color(0xFF388E3C) // Strong Primary Green for Light Mode
val LightSecondaryGreen = Color(0xFF81C784) // Soft Secondary Green for Light Mode

// Functional Colors (Fixed)
val LossText = Color(0xFFF44336)         // Red for losses/errors
val WinrateProgress = Color(0xFF00C853)  // Bright green for the Linear Progress Bar


// --- 4. Leaderboard Specific Colors (Kept for consistency) ---
val LeaderboardPrimary = Color(0xFF5E35B1)
val LeaderboardHeaderText = Color(0xFFFFFFFF)
val LeaderboardDarkGreen = Color(0xFF005051)
val LeaderboardRank1 = Color(0xFFFFC107)
val LeaderboardRank2 = Color(0xFF9E9E9E)
val LeaderboardRank3 = Color(0xFFA1887F)