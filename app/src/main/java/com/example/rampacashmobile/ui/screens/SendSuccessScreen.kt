package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SendSuccessScreen(
    navController: NavController,
    tokenSymbol: String,
    recipientAddress: String,
    amount: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon substitute (can be replaced with asset)
        Text(text = "✅", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Payment successful",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFFFFDF8),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$amount $tokenSymbol sent to ${recipientAddress.take(6)}...${recipientAddress.takeLast(6)}",
            color = Color(0xFFF1F2F3),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { navController.navigate("send_summary/$tokenSymbol/$recipientAddress/$amount") }) {
            Text("View details")
        }
    }
}


