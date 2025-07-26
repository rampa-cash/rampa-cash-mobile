package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.ui.components.SupportedToken
import com.example.rampacashmobile.ui.components.TokenTransferSection
import com.example.rampacashmobile.ui.components.WalletConnectionCard
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rampa Cash - Mobile",
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
                println("🚨 DEBUG: UI LaunchedEffect triggered for message: $message")
                println("🚨 DEBUG: About to show snackbar...")
                // Show snackbar and wait for it to be displayed
                snackbarHostState.showSnackbar(message)
                println("🚨 DEBUG: Snackbar display completed")
                // Clear message after snackbar is shown
                viewModel.clearSnackBar()
                println("🚨 DEBUG: UI triggered clearSnackBar()")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Wallet Connection Card
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

            // SPL Token Transfer Section with Token Selection
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

            // Wallet Connection Section
            Section(
                sectionTitle = "Wallet Connection",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (intentSender != null && !viewState.canTransact) {
                                viewModel.connect(intentSender)
                            } else {
                                viewModel.disconnect()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewState.canTransact) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text(
                            text = if (viewState.canTransact) "Disconnect Wallet" else "Connect Wallet",
                            color = Color.White
                        )
                    }
                }

                if (!viewState.walletFound) {
                    Text(
                        text = "⚠️ | No compatible wallet found. Please install Solflare or Phantom.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
} 