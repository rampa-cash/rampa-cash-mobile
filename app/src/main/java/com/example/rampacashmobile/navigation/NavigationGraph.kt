// File: app/src/main/java/com/example/rampacashmobile/navigation/NavigationGraph.kt
package com.example.rampacashmobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.rampacashmobile.ui.components.BottomNavBar
import com.example.rampacashmobile.ui.screens.*
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@Composable
fun NavigationGraph(
    navController: NavHostController,
    intentSender: ActivityResultSender? = null,
    web3AuthManager: Web3AuthManager,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback
) {
    // Single shared ViewModel
    val viewModel: MainViewModel = hiltViewModel()
    val viewState by viewModel.viewState.collectAsState()

    // Properly observe navigation state changes
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom navigation when not on login screen
    val showBottomBar = currentRoute != "login"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (viewState.canTransact || viewState.isWeb3AuthLoggedIn) "dashboard" else "login",
            modifier = if (showBottomBar) Modifier.padding(paddingValues) else Modifier
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    intentSender = intentSender,
                    viewModel = viewModel,
                    web3AuthManager = web3AuthManager,
                    web3AuthCallback = web3AuthCallback
                )
            }

            composable("dashboard") {
                MainScreen(
                    navController = navController,
                    intentSender = intentSender,
                    viewModel = viewModel,
                    web3AuthManager = web3AuthManager,
                    web3AuthCallback = web3AuthCallback
                )
            }

            composable("transfers") {
                TransfersScreen(navController = navController)
            }

            composable("send") {
                SendScreen(navController = navController)
            }

            composable("rewards") {
                RewardsScreen(navController = navController)
            }

            composable("card") {
                CardScreen(navController = navController)
            }
        }
    }
}