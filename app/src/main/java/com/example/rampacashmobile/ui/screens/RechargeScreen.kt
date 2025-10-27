package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.ui.theme.RampaColors
import com.example.rampacashmobile.viewmodel.MainViewModel

// Recharge method enum
enum class RechargeMethod(val displayName: String, val description: String) {
    BANK("Bank transfer", "Top up instantly"),
    CARD("Credit or debit card", "Top up instantly using your Visa or Mastercard."),
    CRYPTO("Stablecoins", "Deposit USDC or USDT from your crypto wallet—fast and borderless.")
}

// Recharge method option data class
data class RechargeMethodOption(
    val method: RechargeMethod,
    val icon: String
)

@Composable
fun RechargeScreen(
    navController: NavController,
    viewModel: MainViewModel? = null
) {
    val viewState = viewModel?.viewState?.collectAsState()?.value

    // Recharge method options matching Figma design
    val rechargeMethods = listOf(
        RechargeMethodOption(RechargeMethod.BANK, "bank"),
        RechargeMethodOption(RechargeMethod.CARD, "card"),
        RechargeMethodOption(RechargeMethod.CRYPTO, "crypto")
    )

    val walletAddress = viewState?.userAddress ?: "Not connected"
    val displayAddress = if (walletAddress.isNotEmpty() && walletAddress != "Not connected") {
        "${walletAddress.take(6)}...${walletAddress.takeLast(4)}"
    } else {
        "6qHNzW…mzx1" // Placeholder from Figma
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
        modifier = Modifier
                .offset(x = 15.dp, y = 77.dp)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 15.dp, top = 145.dp, end = 17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and subtitle matching Figma
            Text(
                text = "Add funds",
                fontSize = 32.sp,
                fontWeight = FontWeight(500), // Medium
                color = Color(0xFFFFFDF8), // --text/normal
                lineHeight = 32.sp
            )
            
            Text(
                text = "Top up your wallet instantly using card, bank, or stablecoins.",
                fontSize = 16.sp,
                fontWeight = FontWeight(500), // Medium
                color = Color(0xFFF1F2F3), // --text/normal-2
                lineHeight = (16 * 1.14).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Method selection section
                MethodSelectionSection(
                    rechargeMethods = rechargeMethods,
                    onMethodSelect = { method ->
                    // Navigate to method-specific flow
                    // TODO: Implement navigation for each method
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Information text matching Figma
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Info icon - circular icon with dark background matching Figma
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            Color(0xFF26292C), // --background/dim from Figma
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        tint = Color(0xFFF1F2F3), // --text/normal-2
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    text = "Funds go to wallet $displayAddress. Processing time varies by method.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400), // Regular/Italic
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFFF1F2F3), // --text/normal-2
                    lineHeight = (14 * 1.14).sp
                )
            }
        }
    }
}

@Composable
private fun MethodSelectionSection(
    rechargeMethods: List<RechargeMethodOption>,
    onMethodSelect: (RechargeMethod) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Select a method",
            fontSize = 16.sp,
            fontWeight = FontWeight(400), // Regular
            color = Color(0xFFFFFDF8), // --text/normal
            lineHeight = (16 * 1.4).sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

            rechargeMethods.forEach { methodOption ->
            // Card matching Figma design
            // Background: rgba(255,255,255,0.2) - 20% white opacity
            Box(
                    modifier = Modifier
                        .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onMethodSelect(methodOption.method) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon container with border
                        // Background: #26292c with border #62696f
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color(0xFF26292C), // --background/dim
                                    CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF62696F), // --outline/outline-i
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Placeholder for icon
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = methodOption.method.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Title and description
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = methodOption.method.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400), // Regular
                                color = Color(0xFFFFFDF8), // --text/normal
                                lineHeight = (16 * 1.14).sp
                            )
                            Text(
                                text = methodOption.method.description,
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400), // Regular
                                color = Color(0xFFF1F2F3), // --text/normal-2
                                lineHeight = (14 * 1.4).sp
                            )
                        }
                    }

                    // Right arrow icon
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Select",
                        tint = Color(0xFFFFFDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
} 