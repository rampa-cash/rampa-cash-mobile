package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rampacashmobile.R
import com.example.rampacashmobile.ui.components.TopNavBar
import com.example.rampacashmobile.usecase.InvestmentDataUseCase
import com.example.rampacashmobile.viewmodel.InvestmentViewModel
import java.text.DecimalFormat
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    navController: NavController,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Navigation with Profile Button
        TopNavBar(
            title = "Invest",
            navController = navController,
            showBackButton = false,
            showProfileButton = true,
            showChatButton = false
        )
        
        // Content
        when {
            viewState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading tokenized stocks...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            viewState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unable to load data",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.refreshData() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Header with refresh button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tokenized Stocks",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = { viewModel.refreshData() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Prices",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    items(viewState.tokens) { token ->
                        TokenCard(
                            token = token,
                            onBuyClick = { clickedToken ->
                                // Show feedback to user
                                Toast.makeText(
                                    context,
                                    "Buy ${clickedToken.symbol} - Coming Soon!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // TODO: Navigate to buy screen or show buy dialog
                                // This could navigate to a dedicated buy screen with:
                                // navController.navigate("buy/${clickedToken.address}")
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenCard(
    token: InvestmentDataUseCase.InvestmentTokenInfo,
    onBuyClick: (InvestmentDataUseCase.InvestmentTokenInfo) -> Unit
) {
    val priceFormatter = DecimalFormat("$#,##0.00")
    val changeFormatter = DecimalFormat("+#,##0.00;-#,##0.00")
    val percentFormatter = DecimalFormat("+#,##0.00%;-#,##0.00%")
    
    val isPositive = token.priceChangePercentage24h >= 0
    val changeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    val iconResource = when (token.symbol) {
        "AMZNx" -> R.drawable.amazon_x
        "AAPLx" -> R.drawable.apple_x
        "GOOGLx" -> R.drawable.google_x
        "METAx" -> R.drawable.meta_x
        "NVDAx" -> R.drawable.nvidia_x
        "TSLAx" -> R.drawable.tesla_x
        "SPYx" -> R.drawable.sp_icon
        else -> R.drawable.investment_icon
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to token details */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token Icon
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = token.symbol,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Token Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = token.address.take(8) + "..." + token.address.takeLast(6),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Price Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = priceFormatter.format(token.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = changeFormatter.format(token.priceChange24h),
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = changeColor.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = percentFormatter.format(token.priceChangePercentage24h / 100),
                            style = MaterialTheme.typography.labelSmall,
                            color = changeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Buy Button
            IconButton(
                onClick = { onBuyClick(token) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Buy ${token.symbol}",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
} 