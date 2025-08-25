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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rampacashmobile.R // Import your R file
import com.example.rampacashmobile.ui.components.TopNavBar
import com.example.rampacashmobile.viewmodel.WithdrawViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    navController: NavController,
    viewModel: WithdrawViewModel = hiltViewModel()
) {
    val context = LocalContext.current // For toast or other context needs

    // Observe states from ViewModel
    val amount = viewModel.amount
    val selectedCountryCode = viewModel.selectedCountryCode
    val currentRegion = viewModel.currentRegion
    val availableCountryOptions = viewModel.availableCountryOptions

    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val withdrawalSuccess = viewModel.withdrawalSuccess

    // State for the country dropdown menu
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    // Effect to handle success (e.g., show message and navigate back)
    LaunchedEffect(withdrawalSuccess) {
        if (withdrawalSuccess) {
            // Show success message (e.g., Toast)
            // Toast.makeText(context, "Withdrawal initiated successfully!", Toast.LENGTH_LONG).show()
            viewModel.clearMessages() // Reset success flag in VM
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = stringResource(R.string.withdraw_screen_title), // "Withdraw Funds"
                navController = navController,
                showBackButton = false // No back button for this screen
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Amount Field (Common to all) ---
            OutlinedTextField(
                value = amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.label_amount_to_withdraw, viewModel.selectedTokenSymbol)) }, // e.g., "Amount to Withdraw (USD)"
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), // NumberPassword to suggest numbers
                singleLine = true,
                isError = errorMessage?.contains("amount", ignoreCase = true) == true // Basic error highlighting
            )

            // --- Country Selector ---
            Text(
                text = stringResource(R.string.withdraw_select_country_banking), // "Select your country of banking:"
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = countryDropdownExpanded,
                onExpandedChange = { countryDropdownExpanded = !countryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCountryCode?.let { code ->
                        availableCountryOptions.find { it.first == code }?.second ?: code
                    } ?: stringResource(R.string.placeholder_select_country),
                    onValueChange = { /* Read only */ },
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_country)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = countryDropdownExpanded,
                    onDismissRequest = { countryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableCountryOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.second) },
                            onClick = {
                                viewModel.onCountrySelected(selectionOption.first)
                                countryDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // --- Spacer ---
            Spacer(modifier = Modifier.height(8.dp))

            // --- Conditional Forms based on Region ---
            if (selectedCountryCode != null) {
                when (currentRegion) {
                    "Europe" -> EuropeanWithdrawalForm(viewModel)
                    "LatinAmerica" -> LatinAmericanWithdrawalForm(viewModel)
                    else -> Text(
                        text = stringResource(R.string.withdraw_service_unavailable_country_selected, selectedCountryCode),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- Error Message Display ---
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // --- Spacer to push button to bottom ---
            Spacer(modifier = Modifier.weight(1f))

            // --- Withdraw Button ---
            Button(
                onClick = { viewModel.initiateWithdrawal() },
                enabled = !isLoading && selectedCountryCode != null && currentRegion != null, // Add more validation based on form fields
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.withdraw_button_action)) // e.g., "Proceed to Withdraw"
                }
            }
        }
    }
}

@Composable
fun EuropeanWithdrawalForm(viewModel: WithdrawViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.withdraw_sepa_details_europe), // "SEPA Bank Transfer Details (Europe)"
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.sepaAccountHolderName,
            onValueChange = viewModel::onSepaAccountHolderNameChange,
            label = { Text(stringResource(R.string.label_account_holder_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("Account holder name",ignoreCase = true) == true
        )
        OutlinedTextField(
            value = viewModel.sepaIban,
            onValueChange = viewModel::onSepaIbanChange,
            label = { Text(stringResource(R.string.label_iban)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("IBAN",ignoreCase = true) == true
        )
        OutlinedTextField(
            value = viewModel.sepaBicSwift,
            onValueChange = viewModel::onSepaBicSwiftChange,
            label = { Text(stringResource(R.string.label_bic_swift)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("BIC/SWIFT",ignoreCase = true) == true
        )
        OutlinedTextField(
            value = viewModel.sepaBankName,
            onValueChange = viewModel::onSepaBankNameChange,
            label = { Text(stringResource(R.string.label_bank_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = viewModel.sepaReference,
            onValueChange = viewModel::onSepaReferenceChange,
            label = { Text(stringResource(R.string.label_reference_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun LatinAmericanWithdrawalForm(viewModel: WithdrawViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.withdraw_latam_details_provider), // "Bank Details (Latin America - via Provider)"
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.latamFirstName,
            onValueChange = viewModel::onLatamFirstNameChange,
            label = { Text(stringResource(R.string.label_first_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("First name", ignoreCase = true) == true
        )
        OutlinedTextField(
            value = viewModel.latamLastName,
            onValueChange = viewModel::onLatamLastNameChange,
            label = { Text(stringResource(R.string.label_last_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("Last name", ignoreCase = true) == true
        )
        OutlinedTextField(
            value = viewModel.latamBankAccountNumber,
            onValueChange = viewModel::onLatamBankAccountNumberChange,
            // You can make the label dynamic based on country if needed
            label = {
                val labelText = when (viewModel.selectedCountryCode) {
                    "MX" -> stringResource(R.string.label_clabe_mx)
                    "AR" -> stringResource(R.string.label_cbu_ar) // Add this string
                    "CO" -> stringResource(R.string.label_account_number_co)
                    else -> stringResource(R.string.label_bank_account_number) // Generic
                }
                Text(labelText)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("Bank account number", ignoreCase = true) == true
        )
        // Bank Name might be optional for some LATAM countries/Bitso, required for others
        // You can add conditional logic for its display if needed, or based on Bitso's requirements
        OutlinedTextField(
            value = viewModel.latamBankName,
            onValueChange = viewModel::onLatamBankNameChange,
            label = { Text(stringResource(R.string.label_bank_name_optional_latam)) }, // e.g. "Bank Name (if applicable)"
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.errorMessage?.contains("Bank name", ignoreCase = true) == true && viewModel.selectedCountryCode == "CO" // Example
        )

        // Informational text for Bitso
        Text(
            stringResource(R.string.withdraw_provider_info_latam), // e.g., "Withdrawals processed by our partner for Latin America."
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
