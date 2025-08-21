// File: app/src/main/java/com/example/rampacashmobile/ui/screens/LoginScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.painter.Painter
// Ensure painterResource is imported: import androidx.compose.ui.res.painterResource
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
            // Consider showing a Snackbar here if you have a SnackbarHostState
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
                verticalArrangement = Arrangement.SpaceBetween // Keeps top and bottom content pushed out
            ) {
                // Top Section: Logo and Welcome
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 60.dp) // Maintain top padding
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.rampa_bonk),
                            contentDescription = "Rampa BONK Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                        color = Color(0xFFFF6B35), // BONK orange
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp) // Removed bottom padding from here
                    )
                }

                // Middle Section: Login Options with Adjusted Spacing
                Column(
                    modifier = Modifier.fillMaxWidth(), // This column will group login options
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // VVVVVV ADJUSTED SPACER (was implicitly part of the parent Column's SpaceBetween) VVVVVV
                    Spacer(modifier = Modifier.height(32.dp)) // Space after welcome text, before social buttons

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernSocialButton(
                            text = "Continue with Google",
                            iconPainter = painterResource(id = R.drawable.ic_logo_google),
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
                            iconPainter = painterResource(id = R.drawable.ic_logo_facebook),
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
                            text = "Continue with X",
                            iconPainter = painterResource(id = R.drawable.ic_logo_x),
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

                    // VVVVVV ADJUSTED SPACER VVVVVV
                    Spacer(modifier = Modifier.height(20.dp))

                    // Modern OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        // .padding(vertical = 20.dp), // Removed specific vertical padding here
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

                    // VVVVVV ADJUSTED SPACER VVVVVV
                    Spacer(modifier = Modifier.height(20.dp))

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
                            color = Color(0xFFEF4444), // Warning red
                            textAlign = TextAlign.Center,
                            // VVVVVV ADJUSTED PADDING/SPACER VVVVVV
                            modifier = Modifier.padding(top = 12.dp) // More space if warning is shown
                        )
                    }
                } // End of Middle Login Options Column

                // Bottom Section: Modern Footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp) // Maintain bottom padding
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
    iconPainter: Painter,
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
        border = BorderStroke(
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
                Image(
                    painter = iconPainter,
                    contentDescription = "$text logo",
                    modifier = Modifier.size(24.dp)
                )
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
            Text("📱", fontSize = 20.sp) // Keeping emoji for wallet connect as it's common
            Text(
                "Connect Mobile Wallet",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

