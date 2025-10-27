package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampacashmobile.R

// Import the Token data class from MainScreen
import com.example.rampacashmobile.ui.screens.Token

@Composable
fun TokenSwitcher(
    tokens: List<Token>,
    selectedTokenIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedToken = tokens[selectedTokenIndex]
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFF0e0f10),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Token Selector - Clickable dropdown with menu
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Token Icon
                            TokenIcon(
                                tokenSymbol = selectedToken.symbol,
                                size = 24.dp
                            )
                            
                            // Token Name
                            Text(
                                text = selectedToken.name,
                                fontSize = 14.sp,
                                color = Color(0xFFf7f7f8),
                                fontWeight = FontWeight.Normal
                            )
                        }
                        
                        // Rotated arrow icon
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Toggle",
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(270f),
                            tint = Color.White
                        )
                    }
                }
                
                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1a1a1a), RoundedCornerShape(8.dp)),
                ) {
                    tokens.forEachIndexed { index, token ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TokenIcon(
                                        tokenSymbol = token.symbol,
                                        size = 24.dp
                                    )
                                    Text(
                                        text = token.name,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            },
                            onClick = {
                                if (index < selectedTokenIndex) {
                                    repeat(selectedTokenIndex - index) { onPrevious() }
                                } else if (index > selectedTokenIndex) {
                                    repeat(index - selectedTokenIndex) { onNext() }
                                }
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            
            // Balance Display - Figma design
            Text(
                text = "ALL ACCOUNT",
                fontSize = 12.sp,
                color = Color(0xFFfffdf8),
                letterSpacing = 0.8.sp,
                fontWeight = FontWeight.Normal
            )
            
            // Balance Amount - Large number
            Text(
                text = String.format(
                    "€%.2f",
                    selectedToken.balance
                ),
                fontSize = 52.sp,
                color = Color(0xFF23d3d5),
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                lineHeight = 52.sp
            )
            
            // Token indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(tokens.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == selectedTokenIndex) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == selectedTokenIndex) Color(0xFF23d3d5)
                                else Color(0xFF3e4247)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun TokenIcon(
    tokenSymbol: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    // Use actual drawable resources for token logos
    val drawableRes = when (tokenSymbol) {
        "SOL" -> R.drawable.solana_logo
        "USDC" -> R.drawable.usdc_logo
        "EURC" -> R.drawable.eurc_logo
        else -> R.drawable.logo_new // Fallback to app logo
    }
    
    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = "$tokenSymbol logo",
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentScale = ContentScale.Fit
    )
} 