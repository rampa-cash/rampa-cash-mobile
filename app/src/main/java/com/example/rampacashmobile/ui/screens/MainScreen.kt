package com.example.rampacashmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
import com.example.rampacashmobile.ui.components.Section
import com.example.rampacashmobile.ui.components.WalletConnectionCard
import com.example.rampacashmobile.ui.components.TokenSwitcher
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import java.util.*

// Token data class
data class Token(
    val symbol: String,
    val name: String,
    val balance: Double,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val mintAddress: String? = null
)

// Transaction data classes (shared with TransfersScreen)
data class MainTransaction(
    val id: String,
    val recipient: String,
    val sender: String,
    val amount: Double,
    val date: Date,
    val description: String,
    val currency: String,
    val transactionType: MainTransactionType,
    val tokenSymbol: String,
    val tokenIcon: Int
)

enum class MainTransactionType {
    SENT, RECEIVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel,
    web3AuthManager: Web3AuthManager? = null,
    web3AuthCallback: Web3AuthManager.Web3AuthCallback? = null
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Token switching state
    var selectedTokenIndex by remember { mutableIntStateOf(0) }

    // Define tokens similar to React Dashboard
    val tokens = listOf(
        Token(
            symbol = "USDC",
            name = "USD Coin",
            balance = viewState.usdcBalance,
            icon = "usdc", // We'll handle this differently in Compose
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

    // Token switching functions
    val nextToken = {
        selectedTokenIndex = (selectedTokenIndex + 1) % tokens.size
    }

    val prevToken = {
        selectedTokenIndex = (selectedTokenIndex - 1 + tokens.size) % tokens.size
    }

    val selectedToken = tokens[selectedTokenIndex]

    // Redirect to login if not authenticated (with delay for session restoration)
    LaunchedEffect(
        viewState.canTransact,
        viewState.isWeb3AuthLoggedIn,
        viewState.isLoading,
        viewState.userAddress
    ) {
        // Check if we're in a disconnected state (regardless of loading flag)
        val isDisconnected =
            !viewState.canTransact && !viewState.isWeb3AuthLoggedIn && viewState.userAddress.isEmpty()

        if (isDisconnected) {
            // If loading, it could be session restoration or logout - wait a bit
            if (viewState.isLoading) {
                kotlinx.coroutines.delay(500) // Shorter delay for logout transitions
            } else {
                kotlinx.coroutines.delay(1000) // Normal delay for session restoration
            }

            // Double-check we're still disconnected after delay
            if (!viewState.canTransact && !viewState.isWeb3AuthLoggedIn) {
                Log.d("MainScreen", "🔄 No active session found - redirecting to login")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
                            LaunchedEffect(Unit) {
            Log.d("MainScreen", "🔄 Starting session restoration...")
            viewModel.loadConnection()
        }
        
        // Fetch transaction history once when wallet is connected
        LaunchedEffect(viewState.canTransact, viewState.isWeb3AuthLoggedIn, viewState.userAddress) {
            if ((viewState.canTransact || viewState.isWeb3AuthLoggedIn) && 
                !viewState.userAddress.isNullOrEmpty() && 
                viewState.transactionHistory.isEmpty() &&
                !viewState.isLoadingTransactions) {
                Log.d("MainScreen", "🔄 Fetching transaction history (one-time)...")
                delay(1500) // Reasonable delay to ensure connection is established
                viewModel.getTransactionHistory()
            }
        }

            LaunchedEffect(viewState.snackbarMessage) {
                viewState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackBar()
                }
            }

            // Show loading screen during session restoration or logout
            if (viewState.isLoading || (!viewState.canTransact && !viewState.isWeb3AuthLoggedIn && viewState.userAddress.isEmpty())) {
                LoadingScreen()
            }
            // Show transaction success screen or main dashboard
            else if (viewState.showTransactionSuccess && viewState.transactionDetails != null) {
                Log.d(
                    "MainScreen",
                    "🎯 Showing TransactionSuccessScreen - signature: ${
                        viewState.transactionDetails!!.signature.take(8)
                    }"
                )
                TransactionSuccessScreen(
                    transactionDetails = viewState.transactionDetails!!,
                    onDone = {
                        Log.d("MainScreen", "🔙 TransactionSuccessScreen onDone called")
                        viewModel.onTransactionSuccessDone()
                    }
                )
            } else {
                Log.d(
                    "MainScreen",
                    "📱 Showing main content - showTransactionSuccess: ${viewState.showTransactionSuccess}, hasDetails: ${viewState.transactionDetails != null}"
                )
                                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                    // Token Switcher and Wallet Card (only show when connected)
                    if (viewState.canTransact) {
                        // Token Switcher
                        TokenSwitcher(
                            selectedToken = selectedToken,
                            onPrevious = prevToken,
                            onNext = nextToken,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Updated Wallet Connection Card with selected token
                        WalletConnectionCard(
                            selectedToken = selectedToken,
                            walletName = viewState.userLabel,
                            address = viewState.userAddress,
                            fullAddressForCopy = viewState.fullAddressForCopy,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Action buttons (Recharge and Receive)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            // Recharge Button
                            Button(
                                onClick = { /* Mock - no navigation */ },
                                modifier = Modifier.width(120.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4B5563)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Recharge",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Recharge",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Receive Button
                            Button(
                                onClick = { /* Mock - no navigation */ },
                                modifier = Modifier.width(120.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4B5563)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Receive",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Receive",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Recent Transfers Section
                        RecentTransfersSection(viewModel = viewModel)
                    }


                    // Account Management Section
                    Section(
                        sectionTitle = "Account Management"
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (viewState.isWeb3AuthLoggedIn) {
                                Button(
                                    onClick = {
                                        if (web3AuthManager != null && web3AuthCallback != null) {
                                            web3AuthManager.logout(web3AuthCallback)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Logout", color = Color.White)
                                }
                            } else if (viewState.canTransact) {
                                Button(
                                    onClick = { viewModel.disconnect() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Disconnect Wallet", color = Color.White)
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
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Loading...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Checking for saved sessions",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecentTransfersSection(viewModel: MainViewModel) {
    val viewState by viewModel.viewState.collectAsState()
    
    // Convert Transaction objects to MainTransaction and take last 3
    val recentTransactions = remember(viewState.transactionHistory) {
        viewState.transactionHistory.take(3).map { transaction ->
            MainTransaction(
                id = transaction.id,
                recipient = transaction.recipient,
                sender = transaction.sender,
                amount = transaction.amount,
                date = transaction.date,
                description = transaction.description,
                currency = transaction.currency,
                transactionType = when (transaction.transactionType) {
                    com.example.rampacashmobile.ui.screens.TransactionType.SENT -> MainTransactionType.SENT
                    com.example.rampacashmobile.ui.screens.TransactionType.RECEIVED -> MainTransactionType.RECEIVED
                },
                tokenSymbol = transaction.tokenSymbol,
                tokenIcon = transaction.tokenIcon
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Recent Transfers",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recentTransactions.isEmpty()) {
            if (viewState.isLoadingTransactions) {
                // Loading card when fetching transactions
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9945FF)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading transaction history...",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // No transactions card when not loading
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
                            .padding(32.dp, 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transaction history found",
                            color = Color(0xFF9CA3AF),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Transactions will appear here once you send or receive funds",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            // Show recent transactions
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recentTransactions.forEach { transaction ->
                    RecentTransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(transaction: MainTransaction) {
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
            // Token Icon
            Image(
                painter = painterResource(id = transaction.tokenIcon),
                contentDescription = "${transaction.tokenSymbol} logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transaction.transactionType == MainTransactionType.RECEIVED) "Received" else "Sent",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${if (transaction.transactionType == MainTransactionType.RECEIVED) "+" else "-"}${
                            String.format("%.2f", transaction.amount)
                        } ${transaction.tokenSymbol}",
                        color = if (transaction.transactionType == MainTransactionType.RECEIVED)
                            Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "From ${truncateAddress(transaction.sender)}",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Helper function to truncate addresses
private fun truncateAddress(address: String): String {
    return if (address.length < 12) address
    else "${address.take(4)}...${address.takeLast(4)}"
}

// Note: Using real transaction data from ViewModel instead of mock data