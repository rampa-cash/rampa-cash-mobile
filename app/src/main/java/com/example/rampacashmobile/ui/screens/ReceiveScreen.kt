package com.example.rampacashmobile.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
            .background(Color(0xFF111827))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Receive Stablecoin",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF111827)
            )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stablecoin Selection
            StablecoinSelector(
                selectedToken = selectedToken,
                tokens = tokens,
                showDropdown = showTokenDropdown,
                onTokenSelect = { token ->
                    selectedToken = token
                    showTokenDropdown = false
                },
                onDropdownToggle = { showTokenDropdown = !showTokenDropdown }
            )

            // Network Selection (Static)
            NetworkSelector()

            // QR Code
            QRCodeSection(
                walletAddress = walletAddress,
                isConnected = isConnected
            )

            // Wallet Address
            WalletAddressSection(walletAddress = walletAddress)

            // Warning Box
            WarningSection()

            // Copy Address Button
            CopyAddressButton(
                walletAddress = walletAddress,
                isConnected = isConnected,
                onCopy = { address ->
                    copyToClipboard(context, address) { copySuccess = it }
                }
            )

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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message,
                            color = Color(0xFF10B981),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StablecoinSelector(
    selectedToken: ReceiveToken,
    tokens: List<ReceiveToken>,
    showDropdown: Boolean,
    onTokenSelect: (ReceiveToken) -> Unit,
    onDropdownToggle: () -> Unit
) {
    Column {
        Text(
            text = "STABLECOIN",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDropdownToggle() },
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
                        Image(
                            painter = painterResource(id = selectedToken.icon),
                            contentDescription = selectedToken.symbol,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = selectedToken.symbol,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color.White
                    )
                }
            }

            // Dropdown Menu
            if (showDropdown) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        tokens.forEach { token ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTokenSelect(token) }
                                    .padding(12.dp),
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
                                    fontSize = 18.sp
                                )
                            }
                            if (token != tokens.last()) {
                                Divider(
                                    color = Color(0xFF374151),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkSelector() {
    Column {
        Text(
            text = "NETWORK",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1F2937)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Solana",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun QRCodeSection(
    walletAddress: String,
    isConnected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnected) {
                        QRCode(
                            text = walletAddress,
                            size = 180
                        )
                    } else {
                        Text(
                            text = "Connect your wallet to generate a QR code",
                            color = Color(0xFF666666),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
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
private fun WalletAddressSection(walletAddress: String) {
    Column {
        Text(
            text = "WALLET ADDRESS",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = walletAddress,
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun WarningSection() {
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
                .padding(24.dp)
        ) {
            Text(
                text = "Check Before You Deposit",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WarningItem("Only send supported stablecoins.")
                WarningItem("Don't send SOL or other unsupported tokens.")
                WarningItem("Each address is unique.")
                WarningItem("Wrong deposits may be lost or take 1-2 weeks to recover (fees apply).")
            }
        }
    }
}

@Composable
private fun WarningItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = text,
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun CopyAddressButton(
    walletAddress: String,
    isConnected: Boolean,
    onCopy: (String) -> Unit
) {
    Button(
        onClick = { if (isConnected) onCopy(walletAddress) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isConnected,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.9f),
            disabledContainerColor = Color(0xFF4B5563)
        ),
        shape = RoundedCornerShape(32.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Copy",
                tint = if (isConnected) Color(0xFF111827) else Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Copy Address",
                color = if (isConnected) Color(0xFF111827) else Color(0xFF9CA3AF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
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