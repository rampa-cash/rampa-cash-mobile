// File: app/src/main/java/com/example/rampacashmobile/ui/screens/LoginScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.painter.Painter
import com.example.rampacashmobile.R
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.example.rampacashmobile.ui.components.VerificationStatusBanner
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

                // Verification Status Banner (if user is logged in but needs verification)
                if (viewState.isWeb3AuthLoggedIn && viewState.showVerificationBanner) {
                    VerificationStatusBanner(
                        verificationStatus = viewState.userVerificationStatus,
                        userStatus = viewState.userStatus,
                        onCompleteProfileClick = {
                            // Navigate to profile completion screen
                            navController.navigate("profile_completion") {
                                popUpTo("login") { inclusive = false }
                            }
                        },
                        onDismiss = {
                            viewModel.dismissVerificationBanner()
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberDialog(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Country data with flag emoji, name, and country code
    data class Country(
        val flag: String,
        val name: String,
        val code: String,
        val maxLength: Int
    )

    val countries = listOf(
        // European Union Countries (Priority)
        Country("🇪🇸", "Spain", "+34", 9),
        Country("🇫🇷", "France", "+33", 9),
        Country("🇩🇪", "Germany", "+49", 11),
        Country("🇮🇹", "Italy", "+39", 10),
        Country("🇳🇱", "Netherlands", "+31", 9),
        Country("🇵🇹", "Portugal", "+351", 9),
        Country("🇧🇪", "Belgium", "+32", 9),
        Country("🇦🇹", "Austria", "+43", 10),
        Country("🇬🇷", "Greece", "+30", 10),
        Country("🇸🇪", "Sweden", "+46", 9),
        Country("🇩🇰", "Denmark", "+45", 8),
        Country("🇫🇮", "Finland", "+358", 9),
        Country("🇳🇴", "Norway", "+47", 8),
        Country("🇨🇭", "Switzerland", "+41", 9),
        Country("🇵🇱", "Poland", "+48", 9),
        Country("🇨🇿", "Czech Republic", "+420", 9),
        Country("🇭🇺", "Hungary", "+36", 9),
        Country("🇮🇪", "Ireland", "+353", 9),
        Country("🇱🇺", "Luxembourg", "+352", 9),
        Country("🇸🇰", "Slovakia", "+421", 9),
        Country("🇸🇮", "Slovenia", "+386", 8),
        Country("🇪🇪", "Estonia", "+372", 7),
        Country("🇱🇻", "Latvia", "+371", 8),
        Country("🇱🇹", "Lithuania", "+370", 8),
        Country("🇷🇴", "Romania", "+40", 9),
        Country("🇧🇬", "Bulgaria", "+359", 8),
        Country("🇭🇷", "Croatia", "+385", 8),

        // Latin American Countries (Priority)
        Country("🇨🇴", "Colombia", "+57", 10),
        Country("🇦🇷", "Argentina", "+54", 10),
        Country("🇧🇷", "Brazil", "+55", 11),
        Country("🇲🇽", "Mexico", "+52", 10),
        Country("🇵🇪", "Peru", "+51", 9),
        Country("🇨🇱", "Chile", "+56", 9),
        Country("🇪🇨", "Ecuador", "+593", 9),
        Country("🇻🇪", "Venezuela", "+58", 10),
        Country("🇺🇾", "Uruguay", "+598", 8),
        Country("🇵🇾", "Paraguay", "+595", 9),
        Country("🇧🇴", "Bolivia", "+591", 8),
        Country("🇬🇹", "Guatemala", "+502", 8),
        Country("🇭🇳", "Honduras", "+504", 8),
        Country("🇸🇻", "El Salvador", "+503", 8),
        Country("🇳🇮", "Nicaragua", "+505", 8),
        Country("🇨🇷", "Costa Rica", "+506", 8),
        Country("🇵🇦", "Panama", "+507", 8),
        Country("🇩🇴", "Dominican Republic", "+1", 10), // Uses +1 like US/Canada
        Country("🇨🇺", "Cuba", "+53", 8),
        Country("🇯🇲", "Jamaica", "+1", 10), // Uses +1 like US/Canada
        Country("🇹🇹", "Trinidad and Tobago", "+1", 10), // Uses +1 like US/Canada

        // Other Major Markets
        Country("🇺🇸", "United States", "+1", 10),
        Country("🇨🇦", "Canada", "+1", 10),
        Country("🇬🇧", "United Kingdom", "+44", 10),
        Country("🇯🇵", "Japan", "+81", 10),
        Country("🇰🇷", "South Korea", "+82", 10),
        Country("🇨🇳", "China", "+86", 11),
        Country("🇮🇳", "India", "+91", 10),
        Country("🇦🇺", "Australia", "+61", 9),
        Country("🇳🇿", "New Zealand", "+64", 9),
        Country("🇿🇦", "South Africa", "+27", 9),
        Country("🇹🇷", "Turkey", "+90", 10),
        Country("🇷🇺", "Russia", "+7", 10)
    )

    var selectedCountry by remember { mutableStateOf(countries[0]) } // Default to Spain (first EU country)
    var rawPhoneInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isValidNumber by remember { mutableStateOf(false) }

    // Helper function to format phone number for display
    fun formatPhoneForDisplay(digits: String, maxLength: Int): String {
        return when (maxLength) {
            7 -> when {
                digits.length <= 3 -> digits
                else -> "${digits.take(3)} ${digits.drop(3)}"
            }
            8 -> when {
                digits.length <= 2 -> digits
                digits.length <= 4 -> "${digits.take(2)} ${digits.drop(2)}"
                digits.length <= 6 -> "${digits.take(2)} ${digits.drop(2).take(2)} ${digits.drop(4)}"
                else -> "${digits.take(2)} ${digits.drop(2).take(2)} ${digits.drop(4).take(2)} ${digits.drop(6)}"
            }
            9 -> when {
                digits.length <= 3 -> digits
                digits.length <= 6 -> "${digits.take(3)} ${digits.drop(3)}"
                else -> "${digits.take(3)} ${digits.drop(3).take(3)} ${digits.drop(6)}"
            }
            10 -> when {
                digits.length <= 3 -> digits
                digits.length <= 6 -> "${digits.take(3)} ${digits.drop(3)}"
                else -> "${digits.take(3)} ${digits.drop(3).take(3)} ${digits.drop(6)}"
            }
            11 -> when {
                digits.length <= 3 -> digits
                digits.length <= 7 -> "${digits.take(3)} ${digits.drop(3)}"
                else -> "${digits.take(3)} ${digits.drop(3).take(4)} ${digits.drop(7)}"
            }
            else -> digits
        }
    }

    // Update validation and Web3Auth format when raw digits or country changes
    LaunchedEffect(rawPhoneInput, selectedCountry) {
        val digits = rawPhoneInput.filter { it.isDigit() }
        isValidNumber = digits.length >= selectedCountry.maxLength

        // Update the formatted phone number for Web3Auth
        if (digits.isNotEmpty()) {
            val web3AuthFormat = "${selectedCountry.code}-$digits"
            onPhoneNumberChange(web3AuthFormat)
        }
    }

    // Reset phone input when country changes
    LaunchedEffect(selectedCountry) {
        rawPhoneInput = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter your phone number") },
        text = {
            Column {
                Text(
                    "We'll send you a code. Tap the first field to reveal the code, then tap the SMS notification to autofill or enter the code manually.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Country Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCountry.flag} ${selectedCountry.name} ${selectedCountry.code}",
                        onValueChange = { },
                        label = { Text("Country") },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = country.flag,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "${country.name} ${country.code}",
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCountry = country
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Number Input - ULTIMATE CURSOR FIX: No real-time formatting
                OutlinedTextField(
                    value = rawPhoneInput,
                    onValueChange = { newValue ->
                        // Only allow digits and respect max length - NO FORMATTING in the field
                        val newDigits = newValue.filter { it.isDigit() }
                        if (newDigits.length <= selectedCountry.maxLength) {
                            rawPhoneInput = newDigits
                        }
                    },
                    label = { Text("Phone number") },
                    placeholder = {
                        Text(
                            when (selectedCountry.maxLength) {
                                7 -> "1234567"
                                8 -> "12345678"
                                9 -> "123456789"
                                10 -> "1234567890"
                                11 -> "12345678901"
                                else -> "Phone number"
                            }
                        )
                    },
                    leadingIcon = {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.code}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    supportingText = {
                        if (rawPhoneInput.isNotEmpty() && !isValidNumber) {
                            Text(
                                "Please enter at least ${selectedCountry.maxLength} digits",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9945FF)
                            )
                        } else if (rawPhoneInput.isEmpty()) {
                            Text(
                                "Enter your phone number (digits only)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = rawPhoneInput.isNotEmpty() && !isValidNumber
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = isValidNumber
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
