// File: app/src/main/java/com/example/rampacashmobile/ui/screens/MainScreen.kt
package com.example.rampacashmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.ui.components.TokenTransferSection
import com.example.rampacashmobile.ui.components.WalletConnectionCard
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel,
    web3AuthManager: Web3AuthManager? = null,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback? = null
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Redirect to login if not authenticated
    LaunchedEffect(viewState.canTransact, viewState.isWeb3AuthLoggedIn) {
        if (!viewState.canTransact && !viewState.isWeb3AuthLoggedIn) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Rampa Cash - Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
        LaunchedEffect(Unit) {
            viewModel.loadConnection()
        }

        LaunchedEffect(viewState.snackbarMessage) {
            viewState.snackbarMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
                viewModel.clearSnackBar()
            }
        }

        // Show transaction success screen or main dashboard
        if (viewState.showTransactionSuccess && viewState.transactionDetails != null) {
            Log.d("MainScreen", "🎯 Showing TransactionSuccessScreen - signature: ${viewState.transactionDetails!!.signature.take(8)}")
            TransactionSuccessScreen(
                transactionDetails = viewState.transactionDetails!!,
                onDone = { 
                    Log.d("MainScreen", "🔙 TransactionSuccessScreen onDone called")
                    viewModel.onTransactionSuccessDone() 
                }
            )
        } else {
            Log.d("MainScreen", "📱 Showing main content - showTransactionSuccess: ${viewState.showTransactionSuccess}, hasDetails: ${viewState.transactionDetails != null}")
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Wallet Connection Card (only show when connected)
                if (viewState.canTransact) {
                    WalletConnectionCard(
                        walletName = viewState.userLabel,
                        address = viewState.userAddress,
                        solBalance = viewState.solBalance,
                        eurcBalance = viewState.eurcBalance,
                        usdcBalance = viewState.usdcBalance,
                        fullAddressForCopy = viewState.fullAddressForCopy,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // SPL Token Transfer Section (only show when connected)
                if (viewState.canTransact && intentSender != null) {
                    TokenTransferSection(
                        onTransfer = { token, recipient, amount ->
                            viewModel.sendSplToken(
                                sender = intentSender,
                                recipientAddress = recipient,
                                amount = amount,
                                tokenMintAddress = token.mintAddress,
                                tokenDecimals = token.decimals
                            )
                        },
                        onCheckBalance = { token ->
                            viewModel.checkTokenBalance(
                                tokenMintAddress = token.mintAddress,
                                tokenDecimals = token.decimals
                            )
                        },
                        eurcBalance = viewState.eurcBalance,
                        usdcBalance = viewState.usdcBalance,
                        onRecipientATA = { recipient, token ->
                            viewModel.checkATA(recipient, token.mintAddress)
                        }
                    )
                }

                // Account Management Section
                Section(
                    sectionTitle = "Account Management",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (viewState.isWeb3AuthLoggedIn) {
                            Button(
                                onClick = {
                                    if (web3AuthManager != null && web3AuthCallback != null) {
                                        web3AuthManager.logout(web3AuthCallback)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Logout", color = Color.White)
                            }
                        } else if (viewState.canTransact) {
                            Button(
                                onClick = { viewModel.disconnect() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Disconnect Wallet", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}