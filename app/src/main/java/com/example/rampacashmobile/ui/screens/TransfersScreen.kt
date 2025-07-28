package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun TransfersScreen(navController: NavController) {
    var loading by remember { mutableStateOf(true) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    // Simulate loading and fetch mock data
    LaunchedEffect(Unit) {
        delay(1500) // Simulate network delay
        transactions = generateMockTransactions()
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        // Content
        if (loading) {
            LoadingContent()
        } else if (transactions.isEmpty()) {
            EmptyContent()
        } else {
            TransactionsList(transactions = transactions)
        }
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
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            color = Color(0xFF9945FF)
        )
    }
}

@Composable
private fun EmptyContent() {
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
            Text(
                text = "No transaction history found",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }
    }
}

@Composable
private fun TransactionsList(transactions: List<Transaction>) {
    val groupedTransactions = remember(transactions) {
        groupTransactionsByDate(transactions)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
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
                            String.format("%.5f", transaction.amount)
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

// Generate mock transaction data
private fun generateMockTransactions(): List<Transaction> {
    val calendar = Calendar.getInstance()

    return listOf(
        Transaction(
            id = "tx1",
            recipient = "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG",
            sender = "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc",
            amount = 25.50,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            description = "Received",
            currency = "USDC",
            transactionType = TransactionType.RECEIVED,
            tokenSymbol = "USDC",
            tokenIcon = R.drawable.usdc_logo
        ),
        Transaction(
            id = "tx2",
            recipient = "HP4GTtev4T3ifApvC88P3iydqm8Yhme4tvvzcazG7iEy",
            sender = "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc",
            amount = 100.00,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            description = "Sent",
            currency = "EURC",
            transactionType = TransactionType.SENT,
            tokenSymbol = "EURC",
            tokenIcon = R.drawable.eurc_logo
        ),
        Transaction(
            id = "tx3",
            recipient = "2FDPt2KnppnSw7uArZfxLTJi7iWPz6rerHDZzw3j34fn",
            sender = "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG",
            amount = 0.05,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            description = "Received",
            currency = "SOL",
            transactionType = TransactionType.RECEIVED,
            tokenSymbol = "SOL",
            tokenIcon = R.drawable.solana_logo
        ),
        Transaction(
            id = "tx4",
            recipient = "HP4GTtev4T3ifApvC88P3iydqm8Yhme4tvvzcazG7iEy",
            sender = "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc",
            amount = 50.00,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
            description = "Sent",
            currency = "USDC",
            transactionType = TransactionType.SENT,
            tokenSymbol = "USDC",
            tokenIcon = R.drawable.usdc_logo
        ),
        Transaction(
            id = "tx5",
            recipient = "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc",
            sender = "2FDPt2KnppnSw7uArZfxLTJi7iWPz6rerHDZzw3j34fn",
            amount = 250.75,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -4) }.time,
            description = "Received",
            currency = "EURC",
            transactionType = TransactionType.RECEIVED,
            tokenSymbol = "EURC",
            tokenIcon = R.drawable.eurc_logo
        ),
        Transaction(
            id = "tx6",
            recipient = "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG",
            sender = "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc",
            amount = 0.10,
            date = calendar.apply { add(Calendar.DAY_OF_MONTH, -5) }.time,
            description = "Sent",
            currency = "SOL",
            transactionType = TransactionType.SENT,
            tokenSymbol = "SOL",
            tokenIcon = R.drawable.solana_logo
        )
    )
} 