package com.example.atkit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.atkit.ui.SessionViewModel
import com.example.atkit.ui.screens.*

@Composable
fun OralVisNavigation(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                sessionViewModel = sessionViewModel
            )
        }
        composable("camera/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            CameraScreen(
                sessionId = sessionId,
                navController = navController,
                sessionViewModel = sessionViewModel
            )
        }
        composable("session_end/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionEndScreen(
                sessionId = sessionId,
                navController = navController,
                sessionViewModel = sessionViewModel
            )
        }
        composable("search") {
            SearchScreen(
                navController = navController,
                sessionViewModel = sessionViewModel
            )
        }
        composable("session_detail/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionDetailScreen(
                sessionId = sessionId,
                navController = navController,
                sessionViewModel = sessionViewModel
            )
        }
    }
}
