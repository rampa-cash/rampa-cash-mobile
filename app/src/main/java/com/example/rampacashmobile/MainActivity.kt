package com.example.rampacashmobile

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rampacashmobile.solanautils.TokenDecimals
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.ui.theme.RampaCashMobileTheme
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint

// Token selection data class
data class SupportedToken(
    val name: String, val symbol: String, val mintAddress: String, val decimals: Int
)

// Available tokens for selection
val supportedTokens = listOf(
    SupportedToken("Euro Coin", "EURC", TokenMints.EURC_DEVNET, TokenDecimals.EURC)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sender = ActivityResultSender(this)
        enableEdgeToEdge()
        setContent {
            RampaCashMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(sender)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreen(
    intentSender: ActivityResultSender? = null, viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Text(
                text = "Rampa Cash - Mobile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(all = 24.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LaunchedEffect(Unit) {
            viewModel.loadConnection()
        }

        LaunchedEffect(viewState.snackbarMessage) {
            viewState.snackbarMessage?.let { message ->
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.clearSnackBar()
            }
        }

        Column(
            modifier = Modifier.padding(padding)
        ) {
            if (viewState.canTransact) AccountInfo(
                walletName = viewState.userLabel,
                address = viewState.userAddress,
                solBalance = viewState.solBalance,
                eurcBalance = viewState.eurcBalance
            )

            // SPL Token Transfer Section with Token Selection
            if (viewState.canTransact && intentSender != null) {
                TokenTransferSection(
                    onTransfer = { token, recipient, amount ->
                        viewModel.sendSplToken(
                            sender = intentSender,
                            recipientAddress = recipient,
                            amount = amount,
                            tokenMintAddress = token.mintAddress,
                            tokenDecimals = token.decimals
                        )
                    }, onCheckBalance = { token ->
                        viewModel.checkTokenBalance(
                            tokenMintAddress = token.mintAddress, tokenDecimals = token.decimals
                        )
                    }, eurcBalance = viewState.eurcBalance,
                    onRecipientATA = { recipient, token ->
                        viewModel.checkATA(recipient, token.mintAddress)
                    }
                )
            }

            Row() {
                Button(
                    onClick = {
                        if (intentSender != null && !viewState.canTransact) viewModel.connect(
                            intentSender
                        )
                        else viewModel.disconnect()
                    }, modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(if (viewState.canTransact) "Disconnect" else "Connect")
                }
            }
        }
    }
}

@Composable
fun Section(sectionTitle: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun AccountInfo(walletName: String, address: String, solBalance: Number, eurcBalance: Number) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Connected Wallet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$walletName ($address)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // SOL Balance
            Text(
                text = "%.3f SOL".format(solBalance.toDouble()),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // EURC Balance
            Text(
                text = "%.2f EURC".format(eurcBalance.toDouble()),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4CAF50) // Green color for EURC
            )
        }
    }
}

fun isValidSolanaAddress(address: String): Boolean {
    return try {
        address.length in 32..44 && // Base58 Solana addresses are typically 32-44 chars
                address.all { it.isLetterOrDigit() } // Basic check
    } catch (e: Exception) {
        false
    }
}

@Composable
fun TokenSelectionDropdown(
    selectedToken: SupportedToken,
    onTokenSelected: (SupportedToken) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = "${selectedToken.symbol} - ${selectedToken.name}",
            onValueChange = { },
            readOnly = true,
            label = { Text("Select Token") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true })

        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            supportedTokens.forEach { token ->
                DropdownMenuItem(text = {
                    Column {
                        Text(
                            text = token.symbol,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = token.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }, onClick = {
                    onTokenSelected(token)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun TokenTransferSection(
    onTransfer: (token: SupportedToken, recipientAddress: String, amount: String) -> Unit,
    onCheckBalance: (token: SupportedToken) -> Unit = {},
    eurcBalance: Double = 0.0,
    onRecipientATA: (sender: String, token: SupportedToken) -> Unit
) {
    var selectedToken by remember { mutableStateOf(supportedTokens.first()) }
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isValidAddress by remember { mutableStateOf(true) }

    // Test addresses for development (Devnet)
    val testAddresses = listOf(
        "2HbczxxnXRUNWF5ASJxxXac9aNhywdfNkS6HukJbYsAc" to "Test Wallet 1", // solflare
        "DLCvDmn2t294CseF87Q3YscSNritr7szsYraMp16oEEG" to "Test Wallet 2", // phantomwallet
    )

    Section(sectionTitle = "Token Transfer") {
        // Token Selection Dropdown
        TokenSelectionDropdown(
            selectedToken = selectedToken,
            onTokenSelected = { selectedToken = it },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show current balance placeholder (you might want to fetch actual token balance)
        Text(
            text = "Available: %.2f ${selectedToken.symbol}".format(eurcBalance),
            style = MaterialTheme.typography.bodySmall,
            color = if (eurcBalance > 0) Color(0xFF4CAF50) else Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Recipient Address Input with validation
        OutlinedTextField(
            value = recipientAddress,
            onValueChange = {
                recipientAddress = it
                isValidAddress = isValidSolanaAddress(it)
            },
            label = { Text("Recipient Address") },
            placeholder = { Text("Enter Solana address...") },
            isError = !isValidAddress && recipientAddress.isNotBlank(),
            supportingText = if (!isValidAddress && recipientAddress.isNotBlank()) {
                { Text("Invalid Solana address") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true)

        // Test Address Quick Fill Buttons (Development Helper)
        Text(
            text = "Quick Fill (Dev Only):",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            testAddresses.forEach { (address, name) ->
                Button(
                    onClick = {
                        recipientAddress = address
                        isValidAddress = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = name, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            placeholder = { Text("0.000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true
        )

        // Quick Amount Buttons (Development Helper)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            listOf("0.01", "0.1", "1.0").forEach { testAmount ->
                Button(
                    onClick = { amount = testAmount },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = testAmount, style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Transfer Button
        Button(
            onClick = {
                onTransfer(selectedToken, recipientAddress, amount)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = recipientAddress.isNotBlank() && amount.isNotBlank() && isValidAddress && amount.toDoubleOrNull() != null && amount.toDouble() > 0
        ) {
            Text(text = "Send $amount ${selectedToken.symbol}")
        }

        // Quick check ATA for Sender
        Button(
            onClick = {
                onRecipientATA(recipientAddress, selectedToken)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = "🔍 Check $recipientAddress ATA (Debug)",
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