package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.TokenSwitcher
import com.example.rampacashmobile.viewmodel.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

// Test addresses from TokenTransferSection
data class TestAddress(val address: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    navController: NavController,
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Token switching state
    var selectedTokenIndex by remember { mutableIntStateOf(0) }

    // Form state
    var amount by remember { mutableStateOf("") }
    var selectedAddressIndex by remember { mutableIntStateOf(0) }
    var showDropdown by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Define tokens (same as MainScreen)
    val tokens = listOf(
        Token(
            symbol = "USDC",
            name = "USD Coin",
            balance = viewState.usdcBalance,
            icon = "usdc",
            primaryColor = Color(0xFF2DBCD6),
            secondaryColor = Color(0xFF2775CA),
            mintAddress = "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU"
        ),
        Token(
            symbol = "EURC",
            name = "Euro Coin",
            balance = viewState.eurcBalance,
            icon = "eurc",
            primaryColor = Color(0xFF006BCF),
            secondaryColor = Color(0xFF66A0D5),
            mintAddress = "HzwqbKZw8HxMN6bF2yFZNrht3c2iXXzpKcFu7uBEDKtr"
        ),
        Token(
            symbol = "SOL",
            name = "Solana",
            balance = viewState.solBalance,
            icon = "sol",
            primaryColor = Color(0xFF9945FF),
            secondaryColor = Color(0xFF14F195)
        )
    )

    // Test addresses (from TokenTransferSection)
    val testAddresses = listOf(
        TestAddress("2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc", "Henry Cavil"),
        TestAddress("DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG", "Ozzy Osbourne"),
        TestAddress("HP4GTtev4T3ifApvC88P3iydqm8Yhme4tvvzcazG7iEy", "Sydney Sweeney"),
        TestAddress("2FDPt2KnppnSw7uArZfxLTJi7iWPz6rerHDZzw3j34fn", "Thomas Müller")
    )

    // Token switching functions
    val nextToken = { selectedTokenIndex = (selectedTokenIndex + 1) % tokens.size }
    val prevToken = { selectedTokenIndex = (selectedTokenIndex - 1 + tokens.size) % tokens.size }
    val selectedToken = tokens[selectedTokenIndex]
    val selectedAddress = testAddresses[selectedAddressIndex]

    // Handle send transaction
    val handleSend = {
        if (amount.isNotEmpty() && intentSender != null) {
            isLoading = true
            error = null

            val amountValue = amount.toDoubleOrNull()
            if (amountValue != null && amountValue > 0) {
                if (selectedToken.symbol == "SOL") {
                    // Handle SOL transfer - would need to implement this in viewModel
                    // For now, show error that SOL transfer is not implemented yet
                    error = "SOL transfers not implemented yet. Use USDC or EURC."
                    isLoading = false
                } else if (selectedToken.mintAddress != null) {
                    // Handle SPL token transfer
                    viewModel.sendSplToken(
                        sender = intentSender,
                        recipientAddress = selectedAddress.address,
                        amount = amount,
                        tokenMintAddress = selectedToken.mintAddress,
                        tokenDecimals = if (selectedToken.symbol == "USDC" || selectedToken.symbol == "EURC") 6 else 9
                    )
                    // Don't set loading to false here - let the viewModel handle it
                    // isLoading will be managed by observing viewModel state
                } else {
                    error = "Token mint address not available"
                    isLoading = false
                }
            } else {
                error = "Please enter a valid amount"
                isLoading = false
            }
        }
    }

    // Update loading state based on viewModel state
    LaunchedEffect(viewState.isLoading) {
        isLoading = viewState.isLoading
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827)) // Dark background to match React
    ) {

        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Handle snackbar messages
            LaunchedEffect(viewState.snackbarMessage) {
                viewState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackBar()
                }
            }

            // Handle transaction success - navigate back to dashboard to show success screen
            LaunchedEffect(viewState.showTransactionSuccess, viewState.transactionDetails) {
                if (viewState.showTransactionSuccess && viewState.transactionDetails != null) {
                    // Navigate back to dashboard which will show the TransactionSuccessScreen
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Token Switcher
                TokenSwitcher(
                    selectedToken = selectedToken,
                    onPrevious = prevToken,
                    onNext = nextToken,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Send Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Amount Input
                    Column {
                        Text(
                            text = "Amount",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1F2937)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = {
                                        amount = it
                                        error = null
                                    },
                                    placeholder = {
                                        Text(
                                            "Enter ${selectedToken.symbol} amount",
                                            color = Color(0xFF6B7280)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = selectedToken.symbol,
                                    color = Color(0xFFD1D5DB),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Recipient Dropdown
                    Column {
                        Text(
                            text = "Recipient",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = showDropdown,
                            onExpandedChange = { showDropdown = !showDropdown }
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1F2937)
                                ),
                                modifier = Modifier.menuAnchor()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedAddress.label,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${selectedAddress.address.take(4)}...${
                                                selectedAddress.address.takeLast(
                                                    4
                                                )
                                            }",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 12.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = showDropdown,
                                onDismissRequest = { showDropdown = false }
                            ) {
                                testAddresses.forEachIndexed { index, address ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = address.label,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${address.address.take(8)}...${
                                                        address.address.takeLast(
                                                            8
                                                        )
                                                    }",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedAddressIndex = index
                                            showDropdown = false
                                            error = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Error message
                    error?.let { errorMessage ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x1AEF4444)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Send Button
                    Button(
                        onClick = handleSend,
                        enabled = amount.isNotEmpty() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color(0xFF6B7280)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Send Funds",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Snackbar positioned above navigation bar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        )
    }
}