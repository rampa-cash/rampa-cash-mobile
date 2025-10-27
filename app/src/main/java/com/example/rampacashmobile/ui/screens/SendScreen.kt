package com.example.rampacashmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.rampacashmobile.R
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontStyle
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Use LazyColumn to match InvestmentScreen and LearnScreen structure
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 90.dp
            )
        ) {
            // Header with TopNavBar
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                TopNavBar(
                    navController = navController,
                    showBackButton = false
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Title and description (same item to reduce spacing)
            item {
                Text(
                    text = "Send Funds",
                    fontSize = 32.sp,
                    fontWeight = FontWeight(500), // Medium
                    color = Color(0xFFFFFDF8),
                    lineHeight = 32.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Move money securely to anyone, anywhere, instantly on Solana",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFF1F2F3), // --text/normal-2
                    lineHeight = (16 * 1.14).sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Token/Balance Selection
            item {
                TokenBalanceCard(
                tokens = tokens,
                selectedTokenIndex = selectedTokenIndex,
                onPrevious = prevToken,
                    onNext = nextToken
                )
            }
            
            // Recipient Address Input
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                Text(
                    text = "Where you're sending the funds",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFFFFDF8), // --text/normal
                    lineHeight = (16 * 1.14).sp
                )

                // Input Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Color(0xFF26292C), // --background/dim
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF62696F), // --outline/outline-i
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 17.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFA3A8AE),
                            modifier = Modifier.size(20.dp)
                        )
                        BasicTextField(
                            value = recipientAddress,
                            onValueChange = { input ->
                                recipientAddress = input
                                if (recipientName != null) {
                                    val matchingContact = testAddresses.find { it.address == input }
                                    recipientName = matchingContact?.label
                                }
                                error = null
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = if (recipientAddress.isEmpty()) Color(0xFFA3A8AE) else Color(0xFFFFFDF8)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    isInputFocused = focusState.isFocused
                                    showDropdown = focusState.isFocused
                                },
                            decorationBox = { innerTextField ->
                                if (recipientAddress.isEmpty()) {
                                    Text(
                                        text = "Search for contact to send",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFFA3A8AE), // --text/less-emphasis
                                        lineHeight = (16 * 1.4).sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // Info message
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ensure this is a valid Solana address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFFFFFDF8), // --text/normal
                        lineHeight = (14 * 1.4).sp
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

@Composable
private fun TokenBalanceCard(
    tokens: List<Token>,
    selectedTokenIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Choose which balance to send from",
            fontSize = 16.sp,
            fontWeight = FontWeight(400), // Regular
            color = Color(0xFFFFFDF8), // --text/normal
            lineHeight = (16 * 1.14).sp
        )
        
        val selectedToken = tokens[selectedTokenIndex]
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token Icon - circular container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFF26292C), // --background/dim
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF62696F), // --outline/outline-i
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Token icon placeholder
                    Text(
                        text = selectedToken.symbol.take(1),
                        fontSize = 16.sp,
                        color = Color(0xFFFFFDF8)
                    )
                }
                
                // Token symbol and balance
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedToken.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = Color(0xFFFFFDF8), // --text/normal
                        lineHeight = (16 * 1.14).sp
                    )
                }
                
                // Balance - formatted nicely
                Text(
                    text = "$${String.format("%.2f", selectedToken.balance)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFA9EABF), // --text/success
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}