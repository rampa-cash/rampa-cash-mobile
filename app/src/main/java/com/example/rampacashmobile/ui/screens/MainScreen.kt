package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.ui.components.TokenTransferSection
import com.example.rampacashmobile.ui.components.WalletConnectionCard
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.types.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel = hiltViewModel(),
    onWeb3AuthLogin: (Provider) -> Unit = {}, // Added callback
    onWeb3AuthLogout: () -> Unit = {} // Add logout callback
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
                snackbarHostState.showSnackbar(message)
                viewModel.clearSnackBar()
            }
        }

        // Show transaction success screen or main screen
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

            // Authentication Options Section
            if (!viewState.canTransact) {
                Section(
                    sectionTitle = "Login Options",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Web3Auth Social Login
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🌐 Social Login",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Login with your favorite social account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // Direct Social Login Buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Google Login
                                OutlinedButton(
                                    onClick = { 
                                        if (!viewState.isWeb3AuthLoading) {
                                            onWeb3AuthLogin(Provider.GOOGLE)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !viewState.isWeb3AuthLoading
                                ) {
                                    if (viewState.isWeb3AuthLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.width(16.dp).height(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Connecting...")
                                    } else {
                                        Text("🔍 Continue with Google")
                                    }
                                }
                                
                                // Facebook Login  
                                OutlinedButton(
                                    onClick = { 
                                        if (!viewState.isWeb3AuthLoading) {
                                            onWeb3AuthLogin(Provider.FACEBOOK)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !viewState.isWeb3AuthLoading
                                ) {
                                    Text("📘 Continue with Facebook")
                                }
                                
                                // Twitter Login
                                OutlinedButton(
                                    onClick = { 
                                        if (!viewState.isWeb3AuthLoading) {
                                            onWeb3AuthLogin(Provider.TWITTER)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !viewState.isWeb3AuthLoading
                                ) {
                                    Text("🐦 Continue with Twitter")
                                }
                                
                                // Discord Login
                                OutlinedButton(
                                    onClick = { 
                                        if (!viewState.isWeb3AuthLoading) {
                                            onWeb3AuthLogin(Provider.DISCORD)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !viewState.isWeb3AuthLoading
                                ) {
                                    Text("🎮 Continue with Discord")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mobile Wallet Connection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📱 Mobile Wallet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connect your Solana mobile wallet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Button(
                                onClick = {
                                    if (intentSender != null) {
                                        viewModel.connect(intentSender)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = intentSender != null
                            ) {
                                Text("Connect Wallet", color = Color.White)
                            }

                            if (!viewState.walletFound) {
                                Text(
                                    text = "⚠️ No compatible wallet found. Please install Solflare or Phantom.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Disconnect Section (when user is connected)
                Section(
                    sectionTitle = "Account Management",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (viewState.isWeb3AuthLoggedIn) {
                            Button(
                                onClick = { onWeb3AuthLogout() }, // Use callback to Activity
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Logout", color = Color.White)
                            }
                        } else {
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
} 