package com.example.rampacashmobile.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.solanautils.TokenDecimals

@Composable
fun TokenTransferSection(
    onTransfer: (token: SupportedToken, recipientAddress: String, amount: String) -> Unit,
    onCheckBalance: (token: SupportedToken) -> Unit = {},
    eurcBalance: Double = 0.0,
    usdcBalance: Double = 0.0,
    onRecipientATA: (sender: String, token: SupportedToken) -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedTokens = listOf(
        SupportedToken("Euro Coin", "EURC", TokenMints.EURC_DEVNET, TokenDecimals.EURC),
        SupportedToken("USD Coin", "USDC", TokenMints.USDC_DEVNET, TokenDecimals.USDC)
    )
    
    var selectedToken by remember { mutableStateOf(supportedTokens.first()) }
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isValidAddress by remember { mutableStateOf(true) }

    // Test addresses for development (Devnet)
    val testAddresses = listOf(
        "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc" to "User 1 Solflare", // solflare
        "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG" to "User 1 PW", // phantomwallet
        "HP4GTtev4T3ifApvC88P3iydqm8Yhme4tvvzcazG7iEy" to "User 2 Solflare", // phantomwallet
        "2FDPt2KnppnSw7uArZfxLTJi7iWPz6rerHDZzw3j34fn" to "User 2 PW", // phantomwallet
    )

    Section(sectionTitle = "Token Transfer", modifier = modifier) {
        // Token Selection Dropdown
        TokenSelectionDropdown(
            selectedToken = selectedToken,
            availableTokens = supportedTokens,
            onTokenSelected = { selectedToken = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show current balance for the selected token
        val currentBalance = when (selectedToken.symbol) {
            "EURC" -> eurcBalance
            "USDC" -> usdcBalance
            else -> 0.0
        }
        
        Text(
            text = "Available: %.2f ${selectedToken.symbol}".format(currentBalance),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Test Address Quick Fill Buttons (Development Helper)
        Text(
            text = "Quick Fill (Dev):",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            testAddresses.forEachIndexed { index, (address, label) ->
                Button(
                    onClick = {
                        recipientAddress = address
                        isValidAddress = isValidSolanaAddress(address)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                }
                if (index < testAddresses.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Recipient Address Input
        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { 
                recipientAddress = it
                isValidAddress = isValidSolanaAddress(it)
            },
            label = { Text("Recipient Address") },
            placeholder = { Text("Enter Solana wallet address") },
            isError = !isValidAddress && recipientAddress.isNotEmpty(),
            supportingText = if (!isValidAddress && recipientAddress.isNotEmpty()) {
                { Text("Invalid Solana address format", color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Quick Amount Buttons (Development Helper)
        Text(
            text = "Quick Amounts:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            listOf("0.1", "1.0", "5.0").forEach { quickAmount ->
                Button(
                    onClick = { amount = quickAmount },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("$quickAmount ${selectedToken.symbol}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Transfer Button
        Button(
            onClick = {
                onTransfer(selectedToken, recipientAddress, amount)
            },
            enabled = recipientAddress.isNotEmpty() && amount.isNotEmpty() && isValidAddress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Send ${selectedToken.symbol}")
        }

        Button(
            onClick = {
                onRecipientATA(recipientAddress, selectedToken)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "🔗 Check ATA & Existence (Debug)",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Quick balance check button for debugging
        Button(
            onClick = {
                onCheckBalance(selectedToken)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = "🔍 Check ${selectedToken.symbol} Balance (Debug)",
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}

fun isValidSolanaAddress(address: String): Boolean {
    return try {
        address.length in 32..44 && // Base58 Solana addresses are typically 32-44 chars
                address.all { it.isLetterOrDigit() } && // Base58 uses alphanumeric chars
                !address.contains('0') && !address.contains('O') && // Base58 excludes 0, O, I, l
                !address.contains('I') && !address.contains('l')
    } catch (e: Exception) {
        false
    }
} 