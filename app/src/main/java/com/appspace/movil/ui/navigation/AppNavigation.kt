package com.appspace.movil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appspace.movil.ui.screens.AgentChatScreen
import com.appspace.movil.ui.screens.CategoryScreen
import com.appspace.movil.ui.screens.DashboardScreen
import com.appspace.movil.ui.screens.ReportsScreen
import com.appspace.movil.ui.screens.SettingsScreen

/**
 * Navegación principal de la aplicación
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCategory = { category ->
                    navController.navigate(Screen.Category.withArgs(category))
                },
                onNavigateToAgent = {
                    navController.navigate(Screen.Agent.route)
                }
            )
        }
        
        composable(
            route = Screen.Category.routeWithArgs,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            CategoryScreen(
                category = category,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Agent.route) {
            AgentChatScreen()
        }
        
        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Definición de pantallas
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Category : Screen("category/{category}") {
        fun withArgs(category: String): String {
            return "category/$category"
        }
        val routeWithArgs: String
            get() = route
    }
    object Agent : Screen("agent")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
