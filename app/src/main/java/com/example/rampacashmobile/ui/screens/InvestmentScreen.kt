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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    navController: NavController,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
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

                // TopNavBar
                TopNavBar(
                    navController = navController,
                    showBackButton = false
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Title and description
            item {
                Text(
                    text = "Invest",
                    fontSize = 32.sp,
                    fontWeight = FontWeight(500), // Medium
                    color = Color(0xFFFFFDF8),
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Invest in tokenized assets with live data from Jupiter. Refresh to stay updated.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFFFFDF8),
                    lineHeight = (16 * 1.14).sp
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Content based on state
            if (viewState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .heightIn(min = 200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF23D3D5))
                    }
                }
            } else if (viewState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Unable to load data",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFDF8)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewState.error!!,
                                fontSize = 14.sp,
                                color = Color(0xFFF1F2F3),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.refreshData() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF23D3D5)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry", color = Color.White)
                            }
                            
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
                }
            } else {
                // Success state - show tokens
                items(viewState.tokens) { token ->
                    TokenCard(
                        token = token,
                        navController = navController,
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

@Composable
fun TokenCard(
    token: InvestmentDataUseCase.InvestmentTokenInfo,
    navController: NavController,
    onBuyClick: (InvestmentDataUseCase.InvestmentTokenInfo) -> Unit
) {
    val priceFormatter = DecimalFormat("£#,##0.00")
    val changeFormatter = DecimalFormat("+#,##0.00;-#,##0.00")
    val percentFormatter = DecimalFormat("+#,##0.00%;-#,##0.00%")
    
    val isPositive = token.priceChangePercentage24h >= 0
    val changeColor = if (isPositive) Color(0xFFA9EABF) else Color(0xFFFDA0B6) // Success/Error colors
    
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onBuyClick(token) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon + Info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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
                    Image(
                        painter = painterResource(id = iconResource),
                        contentDescription = token.symbol,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                // Token Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = token.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFDF8)
                    )
                    Text(
                        text = token.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF1F2F3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = token.address.take(8) + "..." + token.address.takeLast(6),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF1F2F3).copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right side: Price and Change
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = priceFormatter.format(token.price),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400), // Regular
                    color = Color(0xFFFFFDF8), // --text/normal
                    lineHeight = (16 * 1.4).sp,
                    letterSpacing = 0.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = changeFormatter.format(token.priceChange24h),
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = Color(0xFFFFFDF8), // --text/normal
                        lineHeight = (14 * 1.4).sp
                    )
                    Text(
                        text = percentFormatter.format(token.priceChangePercentage24h / 100),
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400), // Regular
                        color = changeColor, // Success or error color
                        lineHeight = (14 * 1.4).sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Buy icon on far right
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Buy ${token.symbol}",
                tint = Color(0xFFFFFDF8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}