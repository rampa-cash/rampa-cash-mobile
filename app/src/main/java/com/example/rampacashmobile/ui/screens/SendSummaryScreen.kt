package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.viewmodel.MainViewModel

@Composable
fun SendSummaryScreen(
    navController: NavController,
    tokenSymbol: String,
    recipientAddress: String,
    amount: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    val currencySymbol = when (tokenSymbol.uppercase()) {
        "EURC" -> "€"
        "USDC" -> "$"
        "SOL" -> "◎"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(77.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Success Icon
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF16F096),
                modifier = Modifier.size(68.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Big centered amount line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currencySymbol, fontSize = 44.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16F096))
            Spacer(Modifier.width(6.dp))
            Text(text = amount, fontSize = 44.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16F096))
        }
        Spacer(modifier = Modifier.height(8.dp))

        // To pill (same style as confirm)
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
        Spacer(modifier = Modifier.height(12.dp))

        // Amount sent section (card row)
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
                Text(text = "Amount sent", color = Color(0xFFF1F2F3), fontSize = 16.sp)
                Text(
                    text = "$currencySymbol$amount $tokenSymbol",
                    color = Color(0xFFFFFDF8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                // Reset success state then navigate
                viewModel.onTransactionSuccessDone()
                navController.navigate("transfers") { popUpTo("dashboard") { inclusive = false } }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}


