package com.example.rampacashmobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rampacashmobile.R
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.ui.components.TopNavBar
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

// Token data for stablecoin selection
data class ReceiveToken(
    val symbol: String,
    val name: String,
    val icon: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    navController: NavController,
    viewModel: MainViewModel? = null
) {
    val viewState = viewModel?.viewState?.collectAsState()?.value
    var selectedToken by remember { mutableStateOf(ReceiveToken("USDC", "USD Coin", R.drawable.usdc_logo)) }
    var showTokenDropdown by remember { mutableStateOf(false) }
    var copySuccess by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Available tokens (only stablecoins)
    val tokens = listOf(
        ReceiveToken("USDC", "USD Coin", R.drawable.usdc_logo),
        ReceiveToken("EURC", "Euro Coin", R.drawable.eurc_logo)
    )

    val walletAddress = viewState?.userAddress ?: "Not connected"
    val isConnected = !walletAddress.isEmpty() && walletAddress != "Not connected"

    // Handle copy success message
    LaunchedEffect(copySuccess) {
        if (copySuccess != null) {
            kotlinx.coroutines.delay(2000)
            copySuccess = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) { 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(top = 50.dp) // Add top margin to push content down
                .padding(bottom = 16.dp), // Reduced bottom padding since no bottom nav
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation with Back Button
            TopNavBar(
                navController = navController,
                showBackButton = true
            )
            
            // Header section
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Receive money",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFfffdf8)
                )
                Text(
                    text = "Share your wallet address to receive money instantly",
                    fontSize = 16.sp,
                    color = Color(0xFFf1f2f3),
                    lineHeight = 18.24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Currency dropdown
            CurrencySelector(
                selectedToken = selectedToken,
                tokens = tokens,
                showDropdown = showTokenDropdown,
                onTokenSelect = { token ->
                    selectedToken = token
                    showTokenDropdown = false
                },
                onDropdownToggle = { showTokenDropdown = !showTokenDropdown }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code
            QRCodeSection(
                walletAddress = walletAddress,
                isConnected = isConnected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Address with Copy button
            WalletAddressWithCopy(
                walletAddress = walletAddress,
                onCopy = { address ->
                    copyToClipboard(context, address) { copySuccess = it }
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            // Share address button
            Button(
                onClick = {
                    if (isConnected) {
                        copyToClipboard(context, walletAddress) { copySuccess = it }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfaf9f6),
                    disabledContainerColor = Color(0xFF4B5563)
                ),
                shape = RoundedCornerShape(99.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Share address",
                    color = Color(0xFF1a1c1e),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Copy Success Message
            copySuccess?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF10B981),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message,
                            color = Color(0xFF10B981),
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencySelector(
    selectedToken: ReceiveToken,
    tokens: List<ReceiveToken>,
    showDropdown: Boolean,
    onTokenSelect: (ReceiveToken) -> Unit,
    onDropdownToggle: () -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDropdownToggle() },
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = selectedToken.icon),
                        contentDescription = selectedToken.symbol,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = selectedToken.symbol,
                        color = Color(0xFFfffdf8),
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = onDropdownToggle,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1a1a1a), RoundedCornerShape(8.dp)),
        ) {
            tokens.forEach { token ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = token.icon),
                                contentDescription = token.symbol,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "${token.symbol} - ${token.name}",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    },
                    onClick = {
                        onTokenSelect(token)
                    }
                )
            }
        }
    }
}

@Composable
private fun QRCodeSection(
    walletAddress: String,
    isConnected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isConnected) {
            // White card with rounded corners containing the QR code
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QRCode(
                        text = walletAddress,
                        size = 180
                    )
                }
            }
        } else {
            Text(
                text = "Connect your wallet to generate a QR code",
                color = Color(0xFF666666),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QRCode(
    text: String,
    size: Int
) {
    val qrCodeBitmap = remember(text) {
        generateQRCode(text, size)
    }

    qrCodeBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(size.dp)
        )
    }
}

@Composable
private fun WalletAddressWithCopy(
    walletAddress: String,
    onCopy: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF62696f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Your wallet address",
                    fontSize = 14.sp,
                    color = Color(0xFFf1f2f3)
                )
                Text(
                    text = if (walletAddress.length > 16) {
                        "${walletAddress.take(8)}...${walletAddress.takeLast(8)}"
                    } else walletAddress,
                    fontSize = 16.sp,
                    color = Color(0xFFfffdf8)
                )
            }
            Button(
                onClick = { onCopy(walletAddress) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfcfcfd)
                ),
                shape = RoundedCornerShape(99.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "COPY",
                    fontSize = 12.sp,
                    color = Color(0xFF1a1c1e),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// Helper function to generate QR code
private fun generateQRCode(text: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y, 
                    if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                )
            }
        }
        bitmap
    } catch (e: WriterException) {
        null
    }
}

// Helper function to copy to clipboard
private fun copyToClipboard(
    context: Context,
    text: String,
    onSuccess: (String) -> Unit
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Wallet Address", text)
    clipboard.setPrimaryClip(clip)
    onSuccess("Address copied to clipboard!")
} 