package com.example.rampacashmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.TokenSwitcher
import com.example.rampacashmobile.ui.components.TopNavBar
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
    var recipientAddress by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf<String?>(null) } // Track selected contact name
    var showDropdown by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Track the last processed transaction to prevent repeated navigation
    var lastProcessedTransactionSignature by remember { mutableStateOf<String?>(null) }

    // Define tokens (same as MainScreen)
    val tokens = listOf(
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
            symbol = "USDC",
            name = "USD Coin",
            balance = viewState.usdcBalance,
            icon = "usdc",
            primaryColor = Color(0xFF2DBCD6),
            secondaryColor = Color(0xFF2775CA),
            mintAddress = "4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU"
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
        TestAddress("2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc", "Pedro"),
        TestAddress("DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG", "Mamá"),
        TestAddress("HP4GTtev4T3ifApvC88P3iydqm8Yhme4tvvzcazG7iEy", "Shakira <3"),
        TestAddress("2FDPt2KnppnSw7uArZfxLTJi7iWPz6rerHDZzw3j34fn", "Tio Luis")
    )

    // Token switching functions
    val nextToken = { selectedTokenIndex = (selectedTokenIndex + 1) % tokens.size }
    val prevToken = { selectedTokenIndex = (selectedTokenIndex - 1 + tokens.size) % tokens.size }
    val selectedToken = tokens[selectedTokenIndex]
    
    // Filter test addresses based on input and focus state
    val filteredAddresses = remember(recipientAddress, isInputFocused) {
        if (recipientAddress.isEmpty()) {
            if (isInputFocused) testAddresses else emptyList()
        } else {
            testAddresses.filter { address ->
                address.label.contains(recipientAddress, ignoreCase = true) ||
                address.address.contains(recipientAddress, ignoreCase = true)
            }
        }
    }

    // Handle send transaction
    val handleSend = {
        if (amount.isNotEmpty() && recipientAddress.isNotEmpty() && intentSender != null) {
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
                        recipientAddress = recipientAddress,
                        amount = amount,
                        tokenMintAddress = selectedToken.mintAddress,
                        tokenDecimals = if (selectedToken.symbol == "USDC" || selectedToken.symbol == "EURC") 6 else 9,
                        recipientName = recipientName
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
            // Top Navigation with Profile Button
            TopNavBar(
                title = "Send Funds",
                navController = navController,
                showBackButton = false,
                showProfileButton = true,
                showChatButton = false
            )
            
            // Handle snackbar messages
            LaunchedEffect(viewState.snackbarMessage) {
                viewState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackBar()
                }
            }

            // Handle transaction success - navigate to success screen
            LaunchedEffect(viewState.showTransactionSuccess, viewState.transactionDetails?.signature) {
                val transactionDetails = viewState.transactionDetails
                if (viewState.showTransactionSuccess && 
                    transactionDetails != null &&
                    transactionDetails.signature != lastProcessedTransactionSignature) {
                    
                    Log.d("SendScreen", "🎯 Navigating to TransactionSuccessScreen for ${transactionDetails.signature.take(8)}")
                    lastProcessedTransactionSignature = transactionDetails.signature
                    
                    // Navigate to dedicated transaction success screen
                    navController.navigate("transaction_success")
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
                    modifier = Modifier.padding(top = 0.dp, bottom = 16.dp)
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
                            fontSize = 18.sp,
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

                    // Recipient Input with Suggestions
                    Column {
                        Text(
                            text = "Recipient Address",
                            fontSize = 18.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Input Field
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1F2937)
                            )
                        ) {
                            OutlinedTextField(
                                value = recipientAddress,
                                onValueChange = { input ->
                                    recipientAddress = input
                                    // Clear contact name when manually typing (unless it matches a contact)
                                    if (recipientName != null) {
                                        val matchingContact = testAddresses.find { it.address == input }
                                        recipientName = matchingContact?.label
                                    }
                                    error = null
                                },
                                placeholder = {
                                    Text(
                                        "Enter address or search contacts",
                                        color = Color(0xFF6B7280)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        isInputFocused = focusState.isFocused
                                        showDropdown = focusState.isFocused
                                    },
                                singleLine = true
                            )
                        }

                        // Suggestions Dropdown (separate from input)
                        if (showDropdown && filteredAddresses.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1F2937)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column {
                                    // Header when showing all addresses (empty input but focused)
                                    if (recipientAddress.isEmpty()) {
                                        Text(
                                            text = "Saved Contacts",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
                                        )
                                    }
                                    
                                    filteredAddresses.forEach { address ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp, 8.dp)
                                                .clickable {
                                                    recipientAddress = address.address
                                                    recipientName = address.label // Set the contact name
                                                    showDropdown = false
                                                    isInputFocused = false
                                                    error = null
                                                },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = address.label,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "${address.address.take(8)}...${address.address.takeLast(8)}",
                                                    color = Color(0xFF9CA3AF),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        
                                        // Add divider between items (except last one)
                                        if (address != filteredAddresses.last()) {
                                            HorizontalDivider(
                                                color = Color(0xFF374151),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
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
                                fontSize = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Send Button
                    Button(
                        onClick = handleSend,
                        enabled = amount.isNotEmpty() && recipientAddress.isNotEmpty() && !isLoading,
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
                                modifier = Modifier.size(24.dp),
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
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        )
    }
}