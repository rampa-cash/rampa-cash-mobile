package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SendConfirmScreen(
    navController: NavController,
    tokenSymbol: String,
    recipientAddress: String,
    amount: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Confirm transfer", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFFDF8))
        Text(text = "You are sending $amount $tokenSymbol", color = Color(0xFFF1F2F3))
        Text(text = "To: ${recipientAddress.take(8)}...${recipientAddress.takeLast(8)}", color = Color(0xFFF1F2F3))

        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                // For now, navigate to success screen. Integration can hook into real send later.
                navController.navigate("send_success/$tokenSymbol/$recipientAddress/$amount") {
                    popUpTo("send_amount/{token}/{recipient}") { inclusive = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Send now") }
    }
}


