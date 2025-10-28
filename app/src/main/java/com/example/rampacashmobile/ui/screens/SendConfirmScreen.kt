package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.solanautils.TokenMints
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@Composable
fun SendConfirmScreen(
    navController: NavController,
    tokenSymbol: String,
    recipientAddress: String,
    amount: String,
    viewModel: MainViewModel = hiltViewModel(),
    intentSender: ActivityResultSender? = null
) {
    val currencySymbol = when (tokenSymbol.uppercase()) {
        "EURC" -> "€"
        "USDC" -> "$"
        "SOL" -> "◎"
        else -> ""
    }

    val viewState = viewModel.viewState.collectAsState().value
    val balance = when (tokenSymbol.uppercase()) {
        "USDC" -> viewState.usdcBalance
        "EURC" -> viewState.eurcBalance
        "SOL" -> viewState.solBalance
        else -> 0.0
    }

    // Navigate to summary when transaction succeeds
    var lastProcessedSignature by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(viewState.showTransactionSuccess, viewState.transactionDetails?.signature) {
        val signature = viewState.transactionDetails?.signature
        if (viewState.showTransactionSuccess && signature != null && signature != lastProcessedSignature) {
            lastProcessedSignature = signature
            navController.navigate("transaction_success")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(77.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Centered amount in teal
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF16F096)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = amount,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF16F096)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Available Balance ", color = Color(0xFFF1F2F3), fontSize = 16.sp)
                Text(text = "$currencySymbol${String.format("%.2f", balance)}", color = Color(0xFFFFFDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipient pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF26292C),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "To", color = Color(0xFFF1F2F3), fontSize = 16.sp)
                    Text(
                        text = "${recipientAddress.take(10)}...${recipientAddress.takeLast(6)}",
                        color = Color(0xFFFFFDF8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            val canSend = amountValue > 0.0 && amountValue <= balance && intentSender != null
            Button(
                onClick = {
                    val mint = when (tokenSymbol.uppercase()) {
                        "EURC" -> TokenMints.EURC_DEVNET
                        "USDC" -> TokenMints.USDC_DEVNET
                        else -> null
                    }
                    if (mint != null && intentSender != null) {
                        val decimals = if (tokenSymbol.uppercase() in listOf("USDC", "EURC")) 6 else 9
                        viewModel.sendSplToken(
                            sender = intentSender,
                            recipientAddress = recipientAddress,
                            amount = amount,
                            tokenMintAddress = mint,
                            tokenDecimals = decimals,
                            recipientName = null
                        )
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Send now")
            }
        }
    }
}


