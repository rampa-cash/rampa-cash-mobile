package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.example.rampacashmobile.ui.components.TopNavBar
import com.example.rampacashmobile.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(
    val id: String,
    val recipient: String,
    val sender: String,
    val amount: Double,
    val date: Date,
    val description: String,
    val currency: String,
    val transactionType: TransactionType,
    val tokenSymbol: String,
    val tokenIcon: Int,
    val tokenName: String? = null
)

enum class TransactionType {
    SENT, RECEIVED
}

// Grouped transactions for date-based grouping
data class GroupedTransactions(
    val date: String,
    val transactions: List<Transaction>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Note: Transaction history is fetched once in MainScreen on wallet connection

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation with Profile Button
            TopNavBar(
                title = "Transfers",
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

            // Content
            when {
                !viewState.canTransact && !viewState.isWeb3AuthLoggedIn -> {
                    NoWalletContent()
                }
                viewState.transactionHistory.isEmpty() -> {
                    EmptyContent(viewModel = viewModel)
                }
                else -> {
                    // Show transactions with header and refresh button
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header with refresh button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaction History",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            IconButton(
                                onClick = { 
                                    viewModel.getTransactionHistory()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Transactions",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        TransactionsList(
                            transactions = viewState.transactionHistory,
                            modifier = Modifier.weight(1f)
                        )
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
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp,
            color = Color(0xFF9945FF)
        )
    }
}

@Composable
private fun NoWalletContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔗",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Connect Your Wallet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Connect your wallet to view transaction history",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📄",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "No Transactions Yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Your transaction history will appear here once you make your first transfer",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = { 
                        viewModel.getTransactionHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9945FF)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionsList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val groupedTransactions = remember(transactions) {
        groupTransactionsByDate(transactions)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp) // Space for bottom nav
    ) {
        items(groupedTransactions) { group ->
            TransactionGroup(group = group)
        }
    }
}

@Composable
private fun TransactionGroup(group: GroupedTransactions) {
    Column {
        // Date Header
        Text(
            text = group.date,
            color = Color(0xFF9CA3AF),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Transactions
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            group.transactions.forEach { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token Icon
            Image(
                painter = painterResource(id = transaction.tokenIcon),
                contentDescription = "${transaction.tokenSymbol} logo",
                modifier = Modifier
                    .size(48.dp)
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
                        text = if (transaction.transactionType == TransactionType.RECEIVED) "Received" else "Sent",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${if (transaction.transactionType == TransactionType.RECEIVED) "+" else "-"}${
                            String.format("%.2f", transaction.amount)
                        } ${transaction.tokenSymbol}",
                        color = if (transaction.transactionType == TransactionType.RECEIVED)
                            Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "From ${truncateAddress(transaction.sender)}",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
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

// Helper function to format date for grouping
private fun formatDateForGrouping(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(date)
}

// Helper function to group transactions by date
private fun groupTransactionsByDate(transactions: List<Transaction>): List<GroupedTransactions> {
    val grouped = transactions
        .sortedByDescending { it.date.time }
        .groupBy { formatDateForGrouping(it.date) }
        .map { (date, txs) ->
            GroupedTransactions(date = date, transactions = txs)
        }

    return grouped
}

// Note: Mock transaction generation removed - now using real transaction data from RPC 