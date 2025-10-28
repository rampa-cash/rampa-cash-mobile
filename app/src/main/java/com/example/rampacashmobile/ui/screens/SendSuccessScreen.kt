package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        Text(text = "Transfer sent!", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFFDF8))
        Spacer(Modifier.height(8.dp))
        Text(text = "$amount $tokenSymbol to ${recipientAddress.take(6)}...${recipientAddress.takeLast(6)}", color = Color(0xFFF1F2F3))
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } }) {
            Text("Done")
        }
    }
}


