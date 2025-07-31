package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
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

    // Handle snackbar messages
    LaunchedEffect(viewState.snackbarMessage) {
        viewState.snackbarMessage?.let { message ->
            // You can add Toast or Snackbar here if needed
            viewModel.clearSnackBar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_new),
                contentDescription = "Rampa.cash Logo",
                modifier = Modifier
                    .width(224.dp)
                    .padding(bottom = 24.dp)
            )

            // Welcome Text
            Text(
                text = "Welcome to Rampa Cash",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Subtitle
            Text(
                text = "BONK! edition",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF6B35), // Orange color for BONK branding
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Web3Auth Social Login Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌐 Social Login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Login with your favorite social account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Social Login Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Google Login
                        SocialLoginButton(
                            text = "Continue with Google",
                            icon = "🔍",
                            backgroundColor = Color(0xFFDB4437),
                            isLoading = viewState.loadingProvider == Provider.GOOGLE,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.GOOGLE)
                                    web3AuthManager.login(Provider.GOOGLE, web3AuthCallback)
                                }
                            }
                        )

                        // Facebook Login
                        SocialLoginButton(
                            text = "Continue with Facebook",
                            icon = "📘",
                            backgroundColor = Color(0xFF4267B2),
                            isLoading = viewState.loadingProvider == Provider.FACEBOOK,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.FACEBOOK)
                                    web3AuthManager.login(Provider.FACEBOOK, web3AuthCallback)
                                }
                            }
                        )

                        // Twitter Login
                        SocialLoginButton(
                            text = "Continue with Twitter",
                            icon = "🐦",
                            backgroundColor = Color(0xFF1DA1F2),
                            isLoading = viewState.loadingProvider == Provider.TWITTER,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.TWITTER)
                                    web3AuthManager.login(Provider.TWITTER, web3AuthCallback)
                                }
                            }
                        )

                        // Discord Login
                        SocialLoginButton(
                            text = "Continue with Discord",
                            icon = "🎮",
                            backgroundColor = Color(0xFF7289DA),
                            isLoading = viewState.loadingProvider == Provider.DISCORD,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.DISCORD)
                                    web3AuthManager.login(Provider.DISCORD, web3AuthCallback)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF374151)
                )
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mobile Wallet Connection Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 Mobile Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Connect your Solana mobile wallet (Phantom, Solflare)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Button(
                        onClick = {
                            if (intentSender != null) {
                                viewModel.connect(intentSender)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9945FF) // Solana purple
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = intentSender != null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚡", fontSize = 18.sp)
                            Text(
                                "Connect Wallet",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }

                    if (!viewState.walletFound) {
                        Text(
                            text = "⚠️ No compatible wallet found. Please install Phantom or Solflare.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Info
            Text(
                text = "Secure • Decentralized • Fast",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    icon: String,
    backgroundColor: Color,
    isLoading: Boolean,
    isAnyLoading: Boolean = false,
    onClick: () -> Unit
) {
    val isDisabled = isLoading || isAnyLoading
    
    Button(
        onClick = { if (!isDisabled) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp),
        enabled = !isDisabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Text("Connecting...", color = Color.White, fontWeight = FontWeight.Medium)
            } else {
                Text(icon, fontSize = 18.sp)
                Text(text, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}