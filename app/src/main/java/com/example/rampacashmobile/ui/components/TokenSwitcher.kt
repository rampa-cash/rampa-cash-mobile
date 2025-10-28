package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    selectedToken: Token,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.1f)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous token",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Token Display
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF1F2937)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Token Icon
                TokenIcon(
                    tokenSymbol = selectedToken.symbol,
                    size = 24.dp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Token Symbol
                Text(
                    text = selectedToken.symbol,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        // Next Button
        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.1f)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next token",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
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