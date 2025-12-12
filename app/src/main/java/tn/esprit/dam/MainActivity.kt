package tn.esprit.dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tn.esprit.dam.screens.CreateTournamentScreen
import tn.esprit.dam.screens.EventsScreen
import tn.esprit.dam.screens.ForgetPasswordScreen
import tn.esprit.dam.screens.FriendsScreen
import tn.esprit.dam.screens.HomeScreen
import tn.esprit.dam.screens.LoginScreen
import tn.esprit.dam.screens.MyStaffScreen
import tn.esprit.dam.screens.Negotiation
import tn.esprit.dam.screens.PasswordChangedScreen
import tn.esprit.dam.screens.PlacmentScreen
import tn.esprit.dam.screens.PlanScreen
import tn.esprit.dam.screens.ProfileScreen // Keep this import
import tn.esprit.dam.screens.ProfileScreenSettings
import tn.esprit.dam.screens.RecruteScreen
import tn.esprit.dam.screens.SetNewPasswordScreen
import tn.esprit.dam.screens.SignupScreen
import tn.esprit.dam.screens.SocialScreen
import tn.esprit.dam.screens.SplashScreen
import tn.esprit.dam.screens.TeamsScreen
import tn.esprit.dam.screens.TournamentCreateForumScreen
import tn.esprit.dam.screens.VerificationScreen
import tn.esprit.dam.screens.VerificationResetScreen
import tn.esprit.dam.screens.WelcomeScreen1
import tn.esprit.dam.screens.WelcomeScreen2
import tn.esprit.dam.screens.WelcomeScreen3
import tn.esprit.dam.screens.DetailMatchScreen
import tn.esprit.dam.screens.SeeMatchScreen
import tn.esprit.dam.screens.EditMaillotScreen
import tn.esprit.dam.ui.theme.DAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // FIX 1: Define the dark theme state
            var darkTheme by remember { mutableStateOf(true) }

            // FIX 2: Create the toggle function to change the state
            val onThemeToggle: () -> Unit = {
                darkTheme = !darkTheme
            }

            // FIX 3: Pass the current theme state to your DAMTheme
            DAMTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                // Define slide animation for navigation transitions
                val slideInAnimation = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))

                val slideOutAnimation = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))

                // Define slide animation for bottom navigation (less aggressive)
                val bottomNavSlideIn = slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(250)
                ) + fadeIn(animationSpec = tween(250))

                val bottomNavSlideOut = slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))

                // Define the NavHost for navigation
                NavHost(
                    navController = navController,
                    startDestination = "splash" // Start with splash screen
                ) {
                    composable(
                        route = "splash",
                        enterTransition = { fadeIn(animationSpec = tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) }
                    ) {
                        SplashScreen(navController = navController)
                    }
                    composable(
                        route = "welcome_screen_1",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        WelcomeScreen1(navController = navController)
                    }
                    composable(
                        route = "welcome_screen_2",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        WelcomeScreen2(navController = navController)
                    }
                    composable(
                        route = "welcome_screen_3",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        WelcomeScreen3(navController = navController)
                    }
                    composable(
                        route = "LoginScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        LoginScreen(navController = navController)
                    }
                    composable(
                        route = "SignUpScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        SignupScreen(navController = navController)
                    }
                    composable(
                        route = "VerificationScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        VerificationScreen(navController = navController)
                    }
                    composable(
                        route = "RecruteScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        RecruteScreen(navController = navController)
                    }
                    composable(
                        route = "Negotiation",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        Negotiation(navController = navController)
                    }
                    composable(
                        route = "MyStaffScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        MyStaffScreen(navController = navController)
                    }
                    composable(
                        route = "VerificationResetScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        // Dedicated screen for forgot-password verification (separate from signup verification)
                        VerificationResetScreen(navController = navController)
                    }

                    // Bottom navigation screens with smoother transitions
                    composable(
                        route = "HomeScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        HomeScreen(navController = navController)
                    }
                    composable(
                        route = "CreateTournamentScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        CreateTournamentScreen(navController = navController)
                    }
                    composable(
                        route = "TournamentCreateForumScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        // Pass navController and a dummy userId for now
                        TournamentCreateForumScreen(
                            navController = navController,
                            currentUserId = "USER_ID_PLACEHOLDER"
                        )
                    }

                    // Add placeholders for other bottom navigation items
                    composable(
                        route = "EventsScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        EventsScreen(navController = navController)
                    }
                    composable(
                        route = "SocialScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        SocialScreen(navController = navController)
                    }
                    composable(
                        route = "ProfileScreenSettings",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        // FIX: Pass the required darkTheme and onThemeToggle parameters
                        ProfileScreenSettings(
                            navController = navController,
                            darkTheme = darkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }

                    composable(
                        route = "FriendsScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        FriendsScreen(navController = navController)
                    }
                    composable(
                        route = "TeamsScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        TeamsScreen(navController = navController)
                    }
                    composable(
                        route = "PlacmentScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        PlacmentScreen(navController = navController)
                    }
                    composable(
                        route = "PlanScreen/{userId}/{nom}/{prenom}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("nom") { type = NavType.StringType },
                            navArgument("prenom") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        val nom = backStackEntry.arguments?.getString("nom") ?: ""
                        val prenom = backStackEntry.arguments?.getString("prenom") ?: ""
                        PlanScreen(navController = navController, userId = userId, nom = nom, prenom = prenom)
                    }

                    // CONSOLIDATED PROFILE SCREEN ROUTE: This is now the definitive profile screen.
                    composable(
                        route = "ProfileScreen",
                        enterTransition = { bottomNavSlideIn },
                        exitTransition = { bottomNavSlideOut },
                        popEnterTransition = { bottomNavSlideIn },
                        popExitTransition = { bottomNavSlideOut }
                    ) {
                        // FIX: Pass the required darkTheme and onThemeToggle parameters
                        ProfileScreen(
                            navController = navController,
                            darkTheme = darkTheme,
                            onThemeToggle = onThemeToggle
                        )
                    }

                    composable(
                        route = "ForgotPasswordScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        ForgetPasswordScreen(navController = navController)
                    }
                    // FIX FOR THE ERROR: We must define the route to include the 'verificationCode' argument
                    composable(
                        route = "SetNewPasswordScreen/{verificationCode}", // Define argument in the route
                        arguments = listOf(navArgument("verificationCode") { type = NavType.StringType }),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        // Retrieve the argument from the backStackEntry
                        val code = backStackEntry.arguments?.getString("verificationCode") ?: ""
                        SetNewPasswordScreen(
                            navController = navController,
                            verificationCode = code // Pass the retrieved argument to the Composable
                        )
                    }
                    composable(
                        route = "PasswordChangedScreen",
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) {
                        PasswordChangedScreen(navController = navController)
                    }

                    composable(
                        route = "DetailMatch/{matchId}/{eq1Id}/{eq2Id}",
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                            navArgument("eq1Id") { type = NavType.StringType },
                            navArgument("eq2Id") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                        val eq1Id = backStackEntry.arguments?.getString("eq1Id") ?: ""
                        val eq2Id = backStackEntry.arguments?.getString("eq2Id") ?: ""
                        DetailMatchScreen(navController = navController, matchId = matchId, eq1Id = eq1Id, eq2Id = eq2Id)
                    }
                    composable(
                        route = "DetailMatch/{matchId}/{eq1Id}/{eq2Id}/{categorie}",
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                            navArgument("eq1Id") { type = NavType.StringType },
                            navArgument("eq2Id") { type = NavType.StringType },
                            navArgument("categorie") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                        val eq1Id = backStackEntry.arguments?.getString("eq1Id") ?: ""
                        val eq2Id = backStackEntry.arguments?.getString("eq2Id") ?: ""
                        val categorie = backStackEntry.arguments?.getString("categorie")
                        DetailMatchScreen(navController = navController, matchId = matchId, eq1Id = eq1Id, eq2Id = eq2Id, coupeCategorie = categorie)
                    }
                    composable(
                        route = "SeeMatch/{matchId}/{eq1Id}/{eq2Id}",
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                            navArgument("eq1Id") { type = NavType.StringType },
                            navArgument("eq2Id") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                        val eq1Id = backStackEntry.arguments?.getString("eq1Id") ?: ""
                        val eq2Id = backStackEntry.arguments?.getString("eq2Id") ?: ""
                        SeeMatchScreen(navController = navController, matchId = matchId, eq1Id = eq1Id, eq2Id = eq2Id)
                    }
                    composable(
                        route = "SeeMatch/{matchId}/{eq1Id}/{eq2Id}/{categorie}",
                        arguments = listOf(
                            navArgument("matchId") { type = NavType.StringType },
                            navArgument("eq1Id") { type = NavType.StringType },
                            navArgument("eq2Id") { type = NavType.StringType },
                            navArgument("categorie") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                        val eq1Id = backStackEntry.arguments?.getString("eq1Id") ?: ""
                        val eq2Id = backStackEntry.arguments?.getString("eq2Id") ?: ""
                        val categorie = backStackEntry.arguments?.getString("categorie")
                        SeeMatchScreen(navController = navController, matchId = matchId, eq1Id = eq1Id, eq2Id = eq2Id, coupeCategorie = categorie)
                    }

                    composable(
                        route = "EditMaillot/{academieId}/{joueurId}",
                        arguments = listOf(
                            navArgument("academieId") { type = NavType.StringType },
                            navArgument("joueurId") { type = NavType.StringType }
                        ),
                        enterTransition = { slideInAnimation },
                        exitTransition = { slideOutAnimation }
                    ) { backStackEntry ->
                        val academieId = backStackEntry.arguments?.getString("academieId") ?: ""
                        val joueurId = backStackEntry.arguments?.getString("joueurId") ?: ""
                        EditMaillotScreen(navController = navController, academieId = academieId, joueurId = joueurId)
                    }
                }
            }
        }
    }
}
