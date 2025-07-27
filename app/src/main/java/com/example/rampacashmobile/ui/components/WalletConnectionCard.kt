package com.example.rampacashmobile.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampacashmobile.ui.screens.Token

@Composable
fun WalletConnectionCard(
    selectedToken: Token,
    walletName: String,
    address: String,
    fullAddressForCopy: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    fun copyAddressToClipboard() {
        val addressToCopy = fullAddressForCopy ?: address
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wallet Address", addressToCopy)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(
            context, 
            "Address copied to clipboard!\n${addressToCopy.take(12)}...${addressToCopy.takeLast(12)}", 
            Toast.LENGTH_SHORT
        ).show()
    }
    
    // Create gradient background based on selected token (similar to React Dashboard)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            selectedToken.primaryColor,
            selectedToken.secondaryColor
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f) // Credit card aspect ratio
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardGradient)
            .clickable { copyAddressToClipboard() } // Make entire card clickable
    ) {
        // Token Icon (top right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            TokenIcon(
                tokenSymbol = selectedToken.symbol,
                size = 40.dp
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Token info and balance (top section)
            Column {
                Text(
                    text = selectedToken.name,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = when (selectedToken.symbol) {
                        "SOL" -> selectedToken.balance.let { balance ->
                            String.format("%.${if (balance < 1) 5 else 2}f", balance)
                        }
                        "USDC" -> String.format("$%.2f", selectedToken.balance)
                        "EURC" -> String.format("€%.2f", selectedToken.balance)
                        else -> String.format("%.2f", selectedToken.balance)
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            
            // Bottom section - Rampa branding (similar to React Dashboard)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CRYPTO",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Normal
                )
                
                Text(
                    text = "rampa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
} 