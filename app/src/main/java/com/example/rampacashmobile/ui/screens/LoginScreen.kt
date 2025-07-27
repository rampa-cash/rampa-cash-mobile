// File: app/src/main/java/com/example/rampacashmobile/ui/screens/LoginScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.types.Provider

@Composable
fun LoginScreen(
    navController: NavController,
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel,
    web3AuthManager: Web3AuthManager? = null,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback? = null
) {
    val viewState by viewModel.viewState.collectAsState()

    // Navigate to dashboard when authenticated
    LaunchedEffect(viewState.canTransact, viewState.isWeb3AuthLoggedIn) {
        if (viewState.canTransact || viewState.isWeb3AuthLoggedIn) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Welcome to Rampa Cash",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        // Web3Auth Social Login Section
        Section(
            sectionTitle = "Social Login",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
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

                    // Social Login Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Google Login
                        OutlinedButton(
                            onClick = {
                                if (!viewState.isWeb3AuthLoading && web3AuthManager != null && web3AuthCallback != null) {
                                    web3AuthManager.login(Provider.GOOGLE, web3AuthCallback)
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
                                if (!viewState.isWeb3AuthLoading && web3AuthManager != null && web3AuthCallback != null) {
                                    web3AuthManager.login(Provider.FACEBOOK, web3AuthCallback)
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
                                if (!viewState.isWeb3AuthLoading && web3AuthManager != null && web3AuthCallback != null) {
                                    web3AuthManager.login(Provider.TWITTER, web3AuthCallback)
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
                                if (!viewState.isWeb3AuthLoading && web3AuthManager != null && web3AuthCallback != null) {
                                    web3AuthManager.login(Provider.DISCORD, web3AuthCallback)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mobile Wallet Connection Section
        Section(
            sectionTitle = "Mobile Wallet",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
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
    }
}