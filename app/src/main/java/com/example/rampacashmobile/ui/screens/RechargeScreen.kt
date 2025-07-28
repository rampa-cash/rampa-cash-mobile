package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.ui.components.TopNavBar
import kotlinx.coroutines.delay

// Recharge method enum
enum class RechargeMethod(val displayName: String, val description: String, val icon: String) {
    BANK("Bank Transfer", "Transfer funds from your bank account (2-3 business days)", "bank"),
    CARD("Credit/Debit Card", "Pay with your credit or debit card (instant)", "card"),
    CRYPTO("Cryptocurrency", "Deposit using cryptocurrencies (fast confirmation)", "crypto")
}

// Recharge method option data class
data class RechargeMethodOption(
    val method: RechargeMethod,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    navController: NavController,
    viewModel: MainViewModel? = null
) {
    val viewState = viewModel?.viewState?.collectAsState()?.value
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<RechargeMethod?>(null) }
    var showAmountInput by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Quick amount options
    val quickAmounts = listOf(50, 100, 200, 500)

    // Recharge method options
    val rechargeMethods = listOf(
        RechargeMethodOption(RechargeMethod.BANK, Icons.Default.Info), // Using available icons
        RechargeMethodOption(RechargeMethod.CARD, Icons.Default.Info),
        RechargeMethodOption(RechargeMethod.CRYPTO, Icons.Default.Info)
    )

    val walletAddress = viewState?.userAddress ?: "Not connected"

    // Handle processing simulation
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(1500)
            isProcessing = false
            // Navigate back to dashboard with success state
            navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        // Top Navigation Bar
        TopNavBar(
            title = "Recharge Account",
            navController = navController,
            showBackButton = true,
            showProfileButton = true,
            showChatButton = false
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp), // Add bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!showAmountInput) {
                // Method Selection
                MethodSelectionSection(
                    rechargeMethods = rechargeMethods,
                    onMethodSelect = { method ->
                        selectedMethod = method
                        showAmountInput = true
                    }
                )
            } else {
                // Amount Input
                AmountInputSection(
                    amount = amount,
                    onAmountChange = { newAmount ->
                        // Allow only numbers and decimal point
                        amount = newAmount.replace(Regex("[^0-9.]"), "")
                    },
                    quickAmounts = quickAmounts,
                    onQuickAmountSelect = { amt ->
                        amount = amt.toString()
                    },
                    selectedMethod = selectedMethod,
                    rechargeMethods = rechargeMethods,
                    onChangeMethod = {
                        showAmountInput = false
                        selectedMethod = null
                    },
                    isProcessing = isProcessing,
                    onSubmit = {
                        if (amount.isNotEmpty() && selectedMethod != null && !isProcessing) {
                            isProcessing = true
                        }
                    }
                )
            }

            // Information Section
            InformationSection(walletAddress = walletAddress)
        }
    }
}

@Composable
private fun MethodSelectionSection(
    rechargeMethods: List<RechargeMethodOption>,
    onMethodSelect: (RechargeMethod) -> Unit
) {
    Column {
        Text(
            text = "Select Recharge Method",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rechargeMethods.forEach { methodOption ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMethodSelect(methodOption.method) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon container
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFF374151),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = methodOption.icon,
                                contentDescription = methodOption.method.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = methodOption.method.displayName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = methodOption.method.description,
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    quickAmounts: List<Int>,
    onQuickAmountSelect: (Int) -> Unit,
    selectedMethod: RechargeMethod?,
    rechargeMethods: List<RechargeMethodOption>,
    onChangeMethod: () -> Unit,
    isProcessing: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Amount Input
        Column {
            Text(
                text = "Enter Amount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dollar input field
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = {
                    Text(
                        text = "0.00",
                        color = Color(0xFF9CA3AF)
                    )
                },
                leadingIcon = {
                    Text(
                        text = "$",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick amounts
            Text(
                text = "Quick Amounts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amt ->
                    Button(
                        onClick = { onQuickAmountSelect(amt) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (amount == amt.toString()) Color(0xFF6366F1) else Color(0xFF374151),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$${amt}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Selected Method
        selectedMethod?.let { method ->
            Column {
                Text(
                    text = "Selected Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF374151),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = rechargeMethods.find { it.method == method }?.icon ?: Icons.Default.Info,
                                contentDescription = method.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = method.displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(onClick = onChangeMethod) {
                            Text(
                                text = "Change",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = onSubmit,
            enabled = amount.isNotEmpty() && !isProcessing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isProcessing) Color(0xFF4F46E5) else Color(0xFF6366F1),
                disabledContainerColor = Color(0xFF4B5563)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (isProcessing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Text(
                        text = "Processing...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = "Add Funds - $${amount.ifEmpty { "0.00" }}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InformationSection(walletAddress: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Information",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Information",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            val displayAddress = if (walletAddress.isNotEmpty() && walletAddress != "Not connected") {
                "${walletAddress.take(6)}...${walletAddress.takeLast(4)}"
            } else {
                "your connected wallet"
            }

            Text(
                text = "Funds will be added to your wallet at $displayAddress. Depending on the method, processing times may vary.",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
} 