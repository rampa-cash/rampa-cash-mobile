package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.ui.components.TopNavBar

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

    val quickAmounts = remember(tokenSymbol) { listOf("10", "25", "50", "100") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        TopNavBar(navController = navController, showBackButton = true)
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Enter amount", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFFDF8))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Balance: ${String.format("%.2f", balance)} $tokenSymbol", color = Color(0xFFF1F2F3), fontSize = 14.sp)
            Text(
                text = "Max",
                color = Color(0xFF9A46FF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { amount = String.format("%.2f", balance) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF26292C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF62696F), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = amount,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) amount = filtered
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFFDF8)),
                    decorationBox = { inner ->
                        if (amount.isEmpty()) {
                            Text(text = "0.00", fontSize = 36.sp, color = Color(0xFFA3A8AE))
                        }
                        inner()
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(text = tokenSymbol, color = Color(0xFFF1F2F3), fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { qa ->
                AssistChip(
                    onClick = { amount = qa },
                    label = { Text("$qa") }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        val amountValue = amount.toDoubleOrNull() ?: 0.0
        Button(
            onClick = {
                val amt = amount.ifBlank { "0" }
                navController.navigate("send_confirm/$tokenSymbol/$recipientAddress/$amt")
            },
            enabled = amountValue > 0.0 && amountValue <= balance,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) { Text("Continue") }
    }
}


