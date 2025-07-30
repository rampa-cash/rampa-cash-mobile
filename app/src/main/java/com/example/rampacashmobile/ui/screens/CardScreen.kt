package com.example.rampacashmobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.components.TopNavBar
import kotlinx.coroutines.delay

data class CardDetails(
    val cardNumber: String = "4000 1234 5678 9010",
    val expiryDate: String = "05/28",
    val cvv: String = "123",
    val cardholderName: String = "RAMPACASH USER",
    val balance: Double = 345.67,
    val isActive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScreen(navController: NavController) {
    var loading by remember { mutableStateOf(true) }
    var cardDetails by remember { mutableStateOf<CardDetails?>(null) }
    var showCardDetails by remember { mutableStateOf(false) }
    var showCVV by remember { mutableStateOf(false) }
    var copySuccess by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Simulate loading
    LaunchedEffect(Unit) {
        delay(800)
        cardDetails = CardDetails()
        loading = false
    }

    // Handle copy success message
    LaunchedEffect(copySuccess) {
        if (copySuccess != null) {
            delay(2000)
            copySuccess = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        // Top Navigation with Profile Button
        TopNavBar(
            title = "Card",
            navController = navController,
            showBackButton = false,
            showProfileButton = true,
            showChatButton = false
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (loading) {
            LoadingCard()
        } else if (cardDetails != null) {
            CardContent(
                cardDetails = cardDetails!!,
                showCardDetails = showCardDetails,
                showCVV = showCVV,
                copySuccess = copySuccess,
                onToggleDetails = { 
                    showCardDetails = !showCardDetails
                    if (!showCardDetails) showCVV = false
                },
                onToggleCVV = { showCVV = !showCVV },
                onCopyField = { text, field ->
                    copyToClipboard(context, text, field) { copySuccess = it }
                }
            )
        } else {
            NoCardAvailable()
        }
        }
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = Color(0xFF9945FF)
            )
            Text(
                text = "Loading your card...",
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun CardContent(
    cardDetails: CardDetails,
    showCardDetails: Boolean,
    showCVV: Boolean,
    copySuccess: String?,
    onToggleDetails: () -> Unit,
    onToggleCVV: () -> Unit,
    onCopyField: (String, String) -> Unit
) {
    // Card Display
    CardVisual(
        cardDetails = cardDetails,
        showDetails = showCardDetails,
        showCVV = showCVV
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Card Controls
    CardControls(
        showCardDetails = showCardDetails,
        showCVV = showCVV,
        onToggleDetails = onToggleDetails,
        onToggleCVV = onToggleCVV
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Card Details Section (when details are shown)
    if (showCardDetails) {
        CardDetailsSection(
            cardDetails = cardDetails,
            showCVV = showCVV,
            copySuccess = copySuccess,
            onCopyField = onCopyField
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // Card Status
    CardStatusSection(cardDetails = cardDetails)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Recent Transactions
    RecentTransactionsSection()
}

@Composable
private fun CardVisual(
    cardDetails: CardDetails,
    showDetails: Boolean,
    showCVV: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF),
                            Color(0xFF14F195)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Rampa Logo (top right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(55.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                
                // Card Number
                Text(
                    text = if (showDetails) cardDetails.cardNumber else "•••• •••• •••• ${cardDetails.cardNumber.takeLast(4)}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Expiry and CVV Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "VALID THRU",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (showDetails) cardDetails.expiryDate else "••/••",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Column {
                        Text(
                            text = "CVV",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (showDetails && showCVV) cardDetails.cvv else "•••",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                // Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Cardholder Name
                    Text(
                        text = cardDetails.cardholderName,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    
                    // VISA Logo
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DEBIT",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.sp,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "VISA",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardControls(
    showCardDetails: Boolean,
    showCVV: Boolean,
    onToggleDetails: () -> Unit,
    onToggleCVV: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = onToggleDetails,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (showCardDetails) Icons.Default.Lock else Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (showCardDetails) "Hide Card Details" else "Show Card Details",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
        
        if (showCardDetails) {
            Button(
                onClick = onToggleCVV,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (showCVV) "Hide CVV" else "Show CVV",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CardDetailsSection(
    cardDetails: CardDetails,
    showCVV: Boolean,
    copySuccess: String?,
    onCopyField: (String, String) -> Unit
) {
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
            Text(
                text = "Card Details",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card Number
            CardDetailRow(
                label = "Card Number",
                value = cardDetails.cardNumber,
                onCopy = { onCopyField(cardDetails.cardNumber.replace(" ", ""), "Card number") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Expiry and CVV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CardDetailRow(
                        label = "Expiry Date",
                        value = cardDetails.expiryDate,
                        onCopy = { onCopyField(cardDetails.expiryDate, "Expiry date") }
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    CardDetailRow(
                        label = "CVV",
                        value = if (showCVV) cardDetails.cvv else "•••",
                        onCopy = if (showCVV) { { onCopyField(cardDetails.cvv, "CVV") } } else null
                    )
                }
            }
            
            // Copy Success Message
            copySuccess?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = message,
                            color = Color(0xFF10B981),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDetailRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)?
) {
    Column {
        Text(
            text = label,
            color = Color(0xFF9CA3AF),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            
            if (onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                                            Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Copy",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(28.dp)
                        )
                }
            }
        }
    }
}

@Composable
private fun CardStatusSection(cardDetails: CardDetails) {
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
            Text(
                text = "Card Status",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (cardDetails.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                CircleShape
                            )
                    )
                    Text(
                        text = if (cardDetails.isActive) "Active" else "Inactive",
                        color = if (cardDetails.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Balance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Balance",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "$${String.format("%.2f", cardDetails.balance)}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Mock action */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (cardDetails.isActive) Icons.Default.Lock else Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (cardDetails.isActive) "Freeze Card" else "Activate Card",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Button(
                    onClick = { /* Mock action */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF374151)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Card Settings",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Recent Transactions",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sample transactions
        val transactions = listOf(
            Triple("Amazon", "May 1, 2025", "-$29.99"),
            Triple("Netflix", "April 28, 2025", "-$14.99"),
            Triple("Card Load", "April 25, 2025", "+$200.00")
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            transactions.forEach { (merchant, date, amount) ->
                TransactionItem(
                    merchant = merchant,
                    date = date,
                    amount = amount
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    merchant: String,
    date: String,
    amount: String
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Transaction Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF374151), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (merchant) {
                            "Amazon" -> Icons.Default.AccountBox
                            "Netflix" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Add
                        },
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = merchant,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = date,
                        color = Color(0xFF94A3B8),
                        fontSize = 16.sp
                    )
                }
            }
            
            Text(
                text = amount,
                color = if (amount.startsWith("+")) Color(0xFF10B981) else Color(0xFFEF4444),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NoCardAvailable() {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(50.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Card Available",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You don't have a virtual card yet. Apply for one to start making payments.",
                color = Color(0xFF9CA3AF),
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* Mock action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Apply for Card",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun copyToClipboard(
    context: Context,
    text: String,
    fieldName: String,
    onSuccess: (String) -> Unit
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(fieldName, text)
    clipboard.setPrimaryClip(clip)
    onSuccess("$fieldName copied!")
} 