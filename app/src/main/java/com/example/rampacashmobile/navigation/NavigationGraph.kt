// File: app/src/main/java/com/example/rampacashmobile/navigation/NavigationGraph.kt
package com.example.rampacashmobile.navigation

import android.util.Log
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
    // Single shared ViewModel for the entire navigation graph
    val sharedViewModel: MainViewModel = hiltViewModel()
    val sharedViewState by sharedViewModel.viewState.collectAsState()

    // Properly observe navigation state changes
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom navigation when not on login screen and not in loading state
    val showBottomBar = currentRoute != "login" && !sharedViewState.isLoading

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login", // Start with login, let LoginScreen handle session restoration
            modifier = if (showBottomBar) Modifier.padding(paddingValues) else Modifier
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    intentSender = intentSender,
                    viewModel = sharedViewModel,
                    web3AuthManager = web3AuthManager,
                    web3AuthCallback = web3AuthCallback
                )
            }

            composable("dashboard") {
                MainScreen(
                    navController = navController,
                    intentSender = intentSender,
                    viewModel = sharedViewModel,
                    web3AuthManager = web3AuthManager,
                    web3AuthCallback = web3AuthCallback
                )
            }

            composable("transfers") {
                TransfersScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }

            composable("send") {
                SendScreen(
                    navController = navController,
                    intentSender = intentSender,
                    viewModel = sharedViewModel
                )
            }

            composable("investment") {
                InvestmentScreen(navController = navController)
            }

            composable("learn") {
                LearnScreen(navController = navController)
            }

            composable("card") {
                CardScreen(navController = navController)
            }
            
            composable("receive") { 
                ReceiveScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }
            composable("recharge") { 
                RechargeScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }
            composable("profile") { 
                ProfileScreen(
                    navController = navController,
                    viewModel = sharedViewModel,
                    web3AuthManager = web3AuthManager,
                    web3AuthCallback = web3AuthCallback
                )
            }
            
            composable("about") { 
                AboutScreen(navController = navController)
            }
            
            composable("transaction_success") {
                // Use the already shared ViewModel instance - NO MORE hiltViewModel()!
                Log.d("NavigationGraph", "📱 Transaction success route reached - hasDetails: ${sharedViewState.transactionDetails != null}, showSuccess: ${sharedViewState.showTransactionSuccess}")
                
                // Check if we have transaction details to show
                when {
                    sharedViewState.transactionDetails != null -> {
                        Log.d("NavigationGraph", "✅ Showing TransactionSuccessScreen")
                        TransactionSuccessScreen(
                            transactionDetails = sharedViewState.transactionDetails!!,
                            navController = navController,
                            onDone = {
                                sharedViewModel.onTransactionSuccessDone()
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }
                    else -> {
                        Log.d("NavigationGraph", "❌ No transaction details, redirecting to dashboard")
                        // If no transaction details after a short wait, go back to dashboard
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(500) // Short delay to allow state to propagate
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}