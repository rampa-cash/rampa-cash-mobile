// File: app/src/main/java/com/example/rampacashmobile/ui/screens/LoginScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }

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

    // Handle onboarding navigation
    LaunchedEffect(viewState.needsOnboardingNavigation) {
        if (viewState.needsOnboardingNavigation) {
            val authProvider = viewState.onboardingAuthProvider
            val existingEmail = viewState.onboardingExistingEmail
            val existingPhone = viewState.onboardingExistingPhone

            // Clear the navigation flag
            viewModel.clearOnboardingNavigation()

            // Navigate to onboarding
            navController.navigate("user_onboarding/$authProvider/$existingEmail/$existingPhone") {
                popUpTo("login") { inclusive = false }
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

    // Phone number input dialog
    if (showPhoneDialog) {
        PhoneNumberDialog(
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { phoneNumber = it },
            onConfirm = {
                if (phoneNumber.isNotBlank() && web3AuthManager != null && web3AuthCallback != null) {
                    showPhoneDialog = false
                    viewModel.setWeb3AuthProviderLoading(Provider.SMS_PASSWORDLESS)
                    web3AuthManager.loginWithPhone(phoneNumber.trim(), web3AuthCallback)
                }
            },
            onDismiss = {
                showPhoneDialog = false
                phoneNumber = ""
            }
        )
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
                            .size(300.dp) // Increased logo size
                            .padding(bottom = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.rampa_trsl_txt_w_bott_foreground),
                            contentDescription = "Rampa Logo",
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
                }

                // Middle Section: Login Options with Adjusted Spacing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ModernPhoneButton(
                            buttonText = "Continue with Phone",
                            iconPainter = painterResource(id = R.drawable.ic_logo_phone),
                            iconContentDescription = "Phone logo",
                            isLoading = viewState.loadingProvider == Provider.SMS_PASSWORDLESS,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                showPhoneDialog = true
                            }
                        )
                        ModernSocialButton(
                            buttonText = "Continue with",
                            iconPainter = painterResource(id = R.drawable.ic_logo_google),
                            iconContentDescription = "Google logo",
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
                            buttonText = "Continue with",
                            iconPainter = painterResource(id = R.drawable.ic_logo_apple),
                            iconContentDescription = "Apple logo",
                            isLoading = viewState.loadingProvider == Provider.APPLE,
                            isAnyLoading = viewState.isWeb3AuthLoading,
                            onClick = {
                                if (web3AuthManager != null && web3AuthCallback != null) {
                                    viewModel.setWeb3AuthProviderLoading(Provider.APPLE)
                                    web3AuthManager.login(Provider.APPLE, web3AuthCallback)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF334155), Color(0xFF475569))
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
                                        colors = listOf(Color(0xFF475569), Color(0xFF334155), Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                } // End of Middle Login Options Column

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
    buttonText: String,
    iconPainter: Painter,
    iconContentDescription: String,
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
                horizontalArrangement = Arrangement.Center // Centers "Text Logo" content
            ) {
                Text(
                    text = buttonText, // "Continue with"
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp) // Space between text and logo
                )
                Image(
                    painter = iconPainter,
                    contentDescription = iconContentDescription, // Specific description like "Google logo"
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernPhoneButton(
    buttonText: String,
    iconPainter: Painter,
    iconContentDescription: String,
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
                horizontalArrangement = Arrangement.Center // Centers "Text Logo" content
            ) {
                Text(
                    text = buttonText, // "Continue with Phone"
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp) // Space between text and logo
                )
                Image(
                    painter = iconPainter,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(20.dp) // Removed padding(end = 8.dp) and moved icon to right
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

@Composable
fun PhoneNumberDialog(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter your phone number") },
        text = {
            Column {
                Text(
                    "You will receive a code. Tap the first field to reveal the code, then tap the SMS notification to autofill or enter the code manually",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8) // Changed from Color(0xFF64748B) to lighter color for better contrast
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Phone number") },
                    placeholder = { Text("+1-2345678901") },
                    supportingText = {
                        Text(
                            "Format: +[country code]-[number]",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9945FF)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = phoneNumber.isNotBlank() && phoneNumber.contains("+") && phoneNumber.contains("-")
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
