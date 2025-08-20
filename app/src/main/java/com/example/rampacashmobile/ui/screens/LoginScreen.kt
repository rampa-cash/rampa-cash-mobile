// File: app/src/main/java/com/example/rampacashmobile/ui/screens/LoginScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
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

    // Check for existing session on app startup
    LaunchedEffect(Unit) {
        viewModel.loadConnection()
    }

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
            viewModel.clearSnackBar()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark slate
                        Color(0xFF1E293B), // Lighter slate
                        Color(0xFF111827)  // Original dark
                    )
                )
            )
    ) {
        // Show loading during session restoration
        if (viewState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF9945FF),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section: Logo and Welcome
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 60.dp)
                ) {
                    // Logo with subtle glow effect
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.rampa_bonk),
                            contentDescription = "Rampa BONK Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Modern welcome text with improved typography
                    Text(
                        text = "Welcome to Rampa",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = "ft. BONK!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF6B35),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )
                }

                // Middle Section: Modern Login Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title for login section
                    Text(
                        text = "Choose your login method",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Modern Social Login Buttons - Minimal Design
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernSocialButton(
                            text = "Continue with Google",
                            icon = "🔍",
                            isLoading = viewState.loadingProvider == Provider.GOOGLE,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.GOOGLE)
                                    web3AuthManager.login(Provider.GOOGLE, web3AuthCallback)
                                }
                            }
                        )

                        ModernSocialButton(
                            text = "Continue with Facebook",
                            icon = "📘",
                            isLoading = viewState.loadingProvider == Provider.FACEBOOK,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.FACEBOOK)
                                    web3AuthManager.login(Provider.FACEBOOK, web3AuthCallback)
                                }
                            }
                        )

                        ModernSocialButton(
                            text = "Continue with Twitter",
                            icon = "🐦",
                            isLoading = viewState.loadingProvider == Provider.TWITTER,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.TWITTER)
                                    web3AuthManager.login(Provider.TWITTER, web3AuthCallback)
                                }
                            }
                        )
                    }

                    // Modern OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF334155),
                                            Color(0xFF475569)
                                        )
                                    )
                                )
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF475569),
                                            Color(0xFF334155),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // Modern Wallet Connect Button
                    ModernWalletButton(
                        enabled = intentSender != null,
                        onClick = {
                            if (intentSender != null) {
                                viewModel.connect(intentSender)
                            }
                        }
                    )

                    if (!viewState.walletFound) {
                        Text(
                            text = "⚠️ Install Phantom or Solflare to continue",
                            fontSize = 13.sp,
                            color = Color(0xFFEF4444),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Bottom Section: Modern Footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 12.sp)
                        Text("Secure", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("•", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("⚡", fontSize = 12.sp)
                        Text("Fast", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("•", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("🌐", fontSize = 12.sp)
                        Text("Decentralized", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernSocialButton(
    text: String,
    icon: String,
    isLoading: Boolean,
    isAnyLoading: Boolean = false,
    onClick: () -> Unit
) {
    val isDisabled = isLoading || isAnyLoading

    OutlinedButton(
        onClick = { if (!isDisabled) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFF64748B)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDisabled) Color(0xFF374151) else Color(0xFF475569)
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = !isDisabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF9945FF)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ModernWalletButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9945FF),
            disabledContainerColor = Color(0xFF9945FF).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📱", fontSize = 20.sp)
            Text(
                "Connect Mobile Wallet",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}