// File: app/src/main/java/com/example/rampacashmobile/ui/screens/MainScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.ui.components.TokenTransferSection
import com.example.rampacashmobile.ui.components.WalletConnectionCard
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel = hiltViewModel(),
    onWeb3AuthLogout: () -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
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
            TransactionSuccessScreen(
                transactionDetails = viewState.transactionDetails!!,
                onDone = { viewModel.onTransactionSuccessDone() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Wallet Connection Card (show if connected)
                if (viewState.canTransact) {
                    WalletConnectionCard(
                        walletName = viewState.userLabel,
                        address = viewState.userAddress,
                        solBalance = viewState.solBalance,
                        eurcBalance = viewState.eurcBalance,
                        usdcBalance = viewState.usdcBalance,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // SPL Token Transfer Section (show if connected and intentSender available)
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

                // Account Management Section (always show when authenticated)
                Section(
                    sectionTitle = "Account Management",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (viewState.isWeb3AuthLoggedIn) {
                            // Web3Auth logout
                            Button(
                                onClick = { onWeb3AuthLogout() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Logout", color = Color.White)
                            }
                        } else if (viewState.canTransact) {
                            // Wallet disconnect
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

                // Quick Actions Section
                Section(
                    sectionTitle = "Quick Actions",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("send") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Send", color = Color.White)
                        }

                        Button(
                            onClick = { navController.navigate("receive") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Receive", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}