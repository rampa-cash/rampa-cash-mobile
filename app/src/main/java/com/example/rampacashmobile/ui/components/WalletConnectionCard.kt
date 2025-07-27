package com.example.rampacashmobile.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WalletConnectionCard(
    walletName: String,
    address: String,
    solBalance: Double,
    eurcBalance: Double,
    usdcBalance: Double,
    fullAddressForCopy: String? = null, // Full address to copy to clipboard
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
    
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Connected Wallet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            
            // Clickable address row with copy icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { copyAddressToClipboard() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$walletName ($address)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy address",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // SOL Balance
            Text(
                text = "%.3f SOL".format(solBalance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // EURC Balance
            Text(
                text = "%.2f EURC".format(eurcBalance),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4CAF50) // Green color for EURC
            )

            // USDC Balance
            Text(
                text = "%.2f USDC".format(usdcBalance),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF2196F3) // Blue color for USDC
            )
        }
    }
} 