package com.example.rampacashmobile.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.rampacashmobile.ui.components.BottomNavBar
import com.example.rampacashmobile.ui.screens.*
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.viewmodel.InvestmentViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.example.rampacashmobile.ui.screens.WithdrawScreen
import com.example.rampacashmobile.viewmodel.WithdrawViewModel
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
    val showBottomBar = currentRoute != "login" &&
            !sharedViewState.isLoading &&
            !currentRoute.orEmpty().startsWith("tokenDetail/")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
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

            composable("user_onboarding/{authProvider}/{existingEmail}/{existingPhone}") { backStackEntry ->
                val authProvider = backStackEntry.arguments?.getString("authProvider") ?: ""
                val existingEmail = backStackEntry.arguments?.getString("existingEmail") ?: ""
                val existingPhone = backStackEntry.arguments?.getString("existingPhone") ?: ""

                UserOnboardingScreen(
                    navController = navController,
                    viewModel = sharedViewModel,
                    authProvider = authProvider,
                    existingEmail = existingEmail,
                    existingPhone = existingPhone
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

            // Ruta para detalles del token
            composable(
                route = "tokenDetail/{symbol}",
                arguments = listOf(
                    navArgument("symbol") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val tokenSymbol = backStackEntry.arguments?.getString("symbol") ?: ""
                val investmentViewModel: InvestmentViewModel = hiltViewModel()

                TokenDetailScreen(
                    navController = navController,
                    tokenSymbol = tokenSymbol,
                    viewModel = investmentViewModel
                )
            }

            // Nueva ruta para detalles del token (legacy - puede ser removida)
            composable(
                route = "investment/{investmentId}",
                arguments = listOf(
                    navArgument("investmentId") {
                        type = NavType.StringType
                    }
                )
            ) {
                // Crear una instancia de InvestmentViewModel en lugar de usar MainViewModel
                val investmentViewModel: InvestmentViewModel = hiltViewModel()

                InvestmentScreen(
                    navController = navController,
                    viewModel = investmentViewModel
                )
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

            composable("withdraw") {
                WithdrawScreen(
                    navController = navController,
                    viewModel = hiltViewModel<WithdrawViewModel>()
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
                Log.d("NavigationGraph", "📱 Transaction success route reached - hasDetails: ${sharedViewState.transactionDetails != null}, showSuccess: ${sharedViewState.showTransactionSuccess}")

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
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(500)
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
