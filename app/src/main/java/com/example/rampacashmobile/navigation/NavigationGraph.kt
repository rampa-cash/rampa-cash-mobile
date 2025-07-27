// File: app/src/main/java/com/example/rampacashmobile/navigation/NavigationGraph.kt
package com.example.rampacashmobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rampacashmobile.ui.screens.*
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.types.Provider

@Composable
fun NavigationGraph(
    navController: NavHostController,
    intentSender: ActivityResultSender? = null,
    onWeb3AuthLogin: (Provider) -> Unit = {},
    onWeb3AuthLogout: () -> Unit = {}
) {
    // Single shared ViewModel instance for the entire app
    val viewModel: MainViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()

    // Global navigation effects based on authentication state
    LaunchedEffect(viewState.canTransact, viewState.isWeb3AuthLoggedIn) {
        val currentRoute = navController.currentDestination?.route

        // If user is authenticated and on login screen, navigate to dashboard
        if ((viewState.canTransact || viewState.isWeb3AuthLoggedIn) && currentRoute == "login") {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
        // If user is not authenticated and not on login screen, navigate to login
        else if (!viewState.canTransact && !viewState.isWeb3AuthLoggedIn && currentRoute != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Login Screen with Web3Auth and Wallet options
        composable("login") {
            LoginScreen(
                navController = navController,
                intentSender = intentSender,
                onWeb3AuthLogin = onWeb3AuthLogin,
                viewModel = viewModel
            )
        }

        // Main Dashboard (protected - only accessible when authenticated)
        composable("dashboard") {
            MainScreen(
                navController = navController,
                intentSender = intentSender,
                onWeb3AuthLogout = {
                    onWeb3AuthLogout()
                    // Navigate to login after logout
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // Send Screen (protected)
        composable("send") {
            SendScreen(navController = navController)
        }

        // Receive Screen (protected)
        composable("receive") {
            ReceiveScreen(navController = navController)
        }

        // Remove these routes for now until you create the screens:
        // - transaction-success
        // - settings
        // - about
        // - recharge
        // - card
        // - transfers
        // - rewards
    }
}