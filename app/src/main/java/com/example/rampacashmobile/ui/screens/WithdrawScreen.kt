// File: app/src/main/java/com/example/rampacashmobile/ui/screens/WithdrawScreen.kt
package com.example.rampacashmobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.rampacashmobile.viewmodel.WithdrawViewModel
import com.example.rampacashmobile.ui.components.TopNavBar
// import com.example.rampacashmobile.viewmodel.WithdrawViewModel // You'll create this

// @HiltViewModel
// class WithdrawViewModel @Inject constructor() : ViewModel() {
//    // Add LiveData/StateFlow for UI state: amount, iban, bic, recipientName, error messages, loading state
//    var amount by mutableStateOf("")
//    var iban by mutableStateOf("")
//    var bic by mutableStateOf("")
//    var recipientName by mutableStateOf("")
//    // ...
// }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    navController: NavController,
    viewModel: WithdrawViewModel = hiltViewModel() // Uncomment when ViewModel is ready
) {
    // Dummy state holders for now - replace with ViewModel properties
    var amount by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("") }
    var bicSwift by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") } // Optional reference

    var selectedTokenSymbol by remember { mutableStateOf("USD") } // Example, make this dynamic

    // TODO: Fetch available tokens and balances from MainViewModel or a shared source

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Withdraw to Bank",
                navController = navController,
                showBackButton = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Select Token to Withdraw:", // TODO: Replace with a proper Token selector if needed
                style = MaterialTheme.typography.titleMedium
            )
            // For now, let's assume a token is implicitly selected or passed.
            // You might need a TokenSelector like in MainScreen if users can withdraw various tokens.
            Text(
                text = "Withdrawing: $selectedTokenSymbol", // Example display
                style = MaterialTheme.typography.bodyLarge
            )
            // TODO: Display available balance for the selected token.

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount to Withdraw") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text(
                "SEPA Bank Transfer Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = accountHolderName,
                onValueChange = { accountHolderName = it },
                label = { Text("Account Holder Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = iban,
                onValueChange = { iban = it },
                label = { Text("IBAN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bicSwift,
                onValueChange = { bicSwift = it },
                label = { Text("BIC/SWIFT Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = reference,
                onValueChange = { reference = it },
                label = { Text("Reference (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

            Button(
                onClick = {
                    // TODO: Implement validation and withdrawal logic
                    // 1. Validate inputs
                    // 2. Show confirmation dialog (recommended)
                    // 3. Call viewModel.initiateSepaWithdrawal(amount, iban, ...)
                    // 4. Handle loading and success/error states
                    println("Withdraw Clicked: Amount: $amount, IBAN: $iban, Name: $accountHolderName")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Proceed to Withdraw")
            }
        }
    }
}
