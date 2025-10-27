package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Note: Transaction history is fetched once in MainScreen on wallet connection
            
            // Handle snackbar messages
            LaunchedEffect(viewState.snackbarMessage) {
                viewState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackBar()
                }
            }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !viewState.canTransact && !viewState.isWeb3AuthLoggedIn -> {
                NoWalletContent()
            }
            viewState.transactionHistory.isEmpty() -> {
                EmptyContent(viewModel = viewModel)
            }
            else -> {
                TransactionsList(
                    navController = navController,
                    transactions = viewState.transactionHistory
                )
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
    navController: NavController,
    transactions: List<Transaction>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 90.dp
        )
    ) {
        // Header and back button
        item {
            Spacer(modifier = Modifier.height(77.dp)) // Space for status bar
            
            // Header row with back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Back button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                // Title - absolute center
                Text(
                    text = "Recent Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFFFFDF8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-22).dp) // Offset to compensate for back button
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp)) // Small gap before transactions
        }
        
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    // Format time
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(transaction.date)
    
    // Determine if received or sent
    val isReceived = transaction.transactionType == TransactionType.RECEIVED
    val amountColor = if (isReceived) Color(0xFFA9EABF) else Color(0xFFFDA0B6) // Success/Error colors from Figma
    val amountPrefix = if (isReceived) "+" else "-"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon + Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Token Icon - circular container
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = transaction.tokenIcon),
                        contentDescription = "${transaction.tokenSymbol} logo",
                        contentScale = ContentScale.Fit
                    )
                }

                // Transaction Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title - show token symbol
                    Text(
                        text = transaction.tokenSymbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFDF8),
                        lineHeight = (16 * 1.14).sp
                    )
                    
                    // Timestamp with calendar icon
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Calendar icon - using Text for now
                        Text(
                            text = "📅",
                            fontSize = 12.sp,
                            color = Color(0xFFF1F2F3),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "TODAY, $timeStr".uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFF1F2F3),
                            lineHeight = (12 * 1.4).sp
                        )
                    }
                }
            }
            
            // Right side: Amount
            Text(
                text = "$amountPrefix$${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = amountColor,
                lineHeight = (16 * 1.4).sp,
                textAlign = TextAlign.End,
                letterSpacing = 0.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// Note: Mock transaction generation removed - now using real transaction data from RPC 