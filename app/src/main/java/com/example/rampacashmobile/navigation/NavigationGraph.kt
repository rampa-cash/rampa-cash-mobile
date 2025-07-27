// File: app/src/main/java/com/example/rampacashmobile/navigation/NavigationGraph.kt
package com.example.rampacashmobile.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rampacashmobile.ui.screens.LoginScreen
import com.example.rampacashmobile.ui.screens.MainScreen
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

    NavHost(
        navController = navController,
        startDestination = if (viewState.canTransact || viewState.isWeb3AuthLoggedIn) "dashboard" else "login"
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
    }
}