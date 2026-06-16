package com.botfleet.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.botfleet.android.ui.screens.botdetail.BotDetailScreen
import com.botfleet.android.ui.screens.dashboard.DashboardScreen
import com.botfleet.android.ui.screens.login.LoginScreen
import com.botfleet.android.ui.screens.register.RegisterScreen
import com.botfleet.android.ui.screens.settings.SettingsScreen
import com.botfleet.android.ui.screens.upload.UploadScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Upload : Screen("upload")
    object BotDetail : Screen("bot/{botId}") {
        fun createRoute(botId: String) = "bot/$botId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToBotDetail = { botId ->
                    navController.navigate(Screen.BotDetail.createRoute(botId))
                },
                onNavigateToUpload = { navController.navigate(Screen.Upload.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Upload.route) {
            UploadScreen(
                onNavigateBack = { navController.popBackStack() },
                onUploaded = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.BotDetail.route,
            arguments = listOf(navArgument("botId") { type = NavType.StringType })
        ) { backStackEntry ->
            val botId = backStackEntry.arguments?.getString("botId") ?: ""
            BotDetailScreen(
                botId = botId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
