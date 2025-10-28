package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import com.example.rampacashmobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendAmountScreen(
    navController: NavController,
    tokenSymbol: String,
    recipientAddress: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }

    val viewState by viewModel.viewState.collectAsState()
    val balance = remember(tokenSymbol, viewState.usdcBalance, viewState.eurcBalance, viewState.solBalance) {
        when (tokenSymbol.uppercase()) {
            "USDC" -> viewState.usdcBalance
            "EURC" -> viewState.eurcBalance
            "SOL" -> viewState.solBalance
            else -> 0.0
        }
    }

    val currencySymbol = when (tokenSymbol.uppercase()) {
        "EURC" -> "€"
        "USDC" -> "$"
        "SOL" -> "◎"
        else -> ""
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Status bar spacer and header with back button (same pattern as TransfersScreen)
        item {
            Spacer(modifier = Modifier.height(100.dp))
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
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Centered amount
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16F096)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) amount = filtered
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xCCFFFFFF)
                        ),
                        decorationBox = { inner ->
                            if (amount.isEmpty()) {
                                Text(text = "0.00", fontSize = 56.sp, color = Color(0x66FFFFFF))
                            }
                            inner()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Centered available balance
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Available Balance ", color = Color(0xFFF1F2F3), fontSize = 16.sp)
                Text(text = "$currencySymbol${String.format("%.2f", balance)}", color = Color(0xFFFFFDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Spacer to allow visual breathing room like Figma
        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Bottom button
        item {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    val amt = amount.ifBlank { "0" }
                    navController.navigate("send_confirm/$tokenSymbol/$recipientAddress/$amt")
                },
                enabled = amountValue > 0.0 && amountValue <= balance,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B2E31),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B2E31).copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) { Text("Continue") }
        }
    }
}


