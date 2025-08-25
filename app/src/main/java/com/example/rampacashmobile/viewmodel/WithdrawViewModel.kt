// File: app/src/main/java/com/example/rampacashmobile/viewmodel/WithdrawViewModel.kt
package com.example.rampacashmobile.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// VVVVVV This is the Interface VVVVVV
import com.example.rampacashmobile.data.repository.OffRampRepository
// ^^^^^^ Hilt will inject either FakeOffRampRepository or a real one here
// VVVVVV Your Data Models (ensure these are defined as per your repository needs) VVVVVV
import com.example.rampacashmobile.data.repository.BitsoWithdrawalDetails
import com.example.rampacashmobile.data.repository.SepaWithdrawalDetails
// ^^^^^^ (Assuming these are now in the repository package as per FakeOffRampRepository example, adjust if elsewhere)
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    // The ViewModel depends on the ABSTRACTION (Interface), not a concrete implementation.
    private val offRampRepository: OffRampRepository
) : ViewModel() {

    // --- Shared State ---
    var amount by mutableStateOf("")
        private set
    var selectedTokenSymbol by mutableStateOf("USD")
        private set

    // --- Country and Region State ---
    var selectedCountryCode by mutableStateOf<String?>(null)
        private set

    val supportedCountries: Map<String, String> = mapOf(
        "ES" to "Europe", "DE" to "Europe", "FR" to "Europe", "IT" to "Europe",
        "CO" to "LatinAmerica", "MX" to "LatinAmerica", "AR" to "LatinAmerica"
    )

    val currentRegion: String? by derivedStateOf {
        selectedCountryCode?.let { supportedCountries[it.uppercase()] }
    }

    val availableCountryOptions: List<Pair<String, String>> = supportedCountries.keys
        .map { code ->
            val displayName = try { Locale("", code).displayCountry } catch (e: Exception) { code }
            Pair(code, displayName)
        }
        .sortedBy { it.second }

    // --- SEPA (Europe) Form State ---
    var sepaAccountHolderName by mutableStateOf("")
        private set
    var sepaIban by mutableStateOf("")
        private set
    var sepaBicSwift by mutableStateOf("")
        private set
    var sepaBankName by mutableStateOf("")
        private set
    var sepaReference by mutableStateOf("")
        private set

    // --- Bitso (LATAM) Form State ---
    var latamFirstName by mutableStateOf("")
        private set
    var latamLastName by mutableStateOf("")
        private set
    var latamBankAccountNumber by mutableStateOf("")
        private set
    var latamBankName by mutableStateOf("")
        private set

    // --- UI Control State ---
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var withdrawalSuccess by mutableStateOf(false)
        private set

    // --- Event Handlers ---
    fun onAmountChange(newAmount: String) {
        if (newAmount.all { it.isDigit() || it == '.' } && newAmount.count { it == '.' } <= 1) {
            amount = newAmount
        }
        clearErrorAndSuccess()
    }

    fun onCountrySelected(countryCode: String) {
        selectedCountryCode = countryCode.uppercase()
        resetFormFields()
        clearErrorAndSuccess()
    }

    // SEPA Field Handlers
    fun onSepaAccountHolderNameChange(newName: String) { sepaAccountHolderName = newName; clearErrorAndSuccess() }
    fun onSepaIbanChange(newIban: String) { sepaIban = newIban.uppercase().filter { !it.isWhitespace() }; clearErrorAndSuccess() }
    fun onSepaBicSwiftChange(newBic: String) { sepaBicSwift = newBic.uppercase().filter { !it.isWhitespace() }; clearErrorAndSuccess() }
    fun onSepaBankNameChange(newName: String) { sepaBankName = newName; clearErrorAndSuccess() }
    fun onSepaReferenceChange(newRef: String) { sepaReference = newRef; clearErrorAndSuccess() }

    // LATAM Field Handlers
    fun onLatamFirstNameChange(newName: String) { latamFirstName = newName; clearErrorAndSuccess() }
    fun onLatamLastNameChange(newName: String) { latamLastName = newName; clearErrorAndSuccess() }
    fun onLatamBankAccountNumberChange(newNumber: String) { latamBankAccountNumber = newNumber; clearErrorAndSuccess() }
    fun onLatamBankNameChange(newName: String) { latamBankName = newName; clearErrorAndSuccess() }

    private fun clearErrorAndSuccess() {
        errorMessage = null
        withdrawalSuccess = false
    }

    private fun resetFormFields() {
        amount = ""
        sepaAccountHolderName = ""; sepaIban = ""; sepaBicSwift = ""; sepaBankName = ""; sepaReference = ""
        latamFirstName = ""; latamLastName = ""; latamBankAccountNumber = ""; latamBankName = ""
    }

    fun initiateWithdrawal() {
        if (isLoading) return
        clearErrorAndSuccess()

        val currentAmount = amount.toDoubleOrNull()
        if (currentAmount == null || currentAmount <= 0) {
            errorMessage = "Please enter a valid amount." // Use stringResource
            return
        }
        if (selectedCountryCode == null) {
            errorMessage = "Please select your country." // Use stringResource
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                // VVVVVV The ViewModel calls the methods on the Interface VVVVVV
                val result: Result<Unit> = when (currentRegion) {
                    "Europe" -> {
                        if (!isValidSepaForm()) { isLoading = false; return@launch }
                        val details = SepaWithdrawalDetails(
                            amount = amount, currency = "EUR", // Determine currency
                            accountHolderName = sepaAccountHolderName, iban = sepaIban,
                            bicSwift = sepaBicSwift, bankName = sepaBankName,
                            reference = sepaReference.ifBlank { null }
                        )
                        offRampRepository.initiateSepaWithdrawal(details)
                    }
                    "LatinAmerica" -> {
                        if (!isValidLatamForm()) { isLoading = false; return@launch }
                        val details = BitsoWithdrawalDetails(
                            amount = amount, currency = getTargetCurrencyForLatam(selectedCountryCode!!),
                            countryCode = selectedCountryCode!!, firstName = latamFirstName,
                            lastName = latamLastName, bankAccountNumber = latamBankAccountNumber,
                            bankName = latamBankName.ifBlank { null }
                        )
                        offRampRepository.initiateBitsoWithdrawal(details)
                    }
                    else -> {
                        errorMessage = "Withdrawal service not available for the selected country." // Use stringResource
                        Result.failure(Exception("Unsupported region"))
                    }
                }
                // ^^^^^^ The ViewModel doesn't care if it's FakeOffRampRepository or the real one ^^^^^^

                if (result.isSuccess) {
                    withdrawalSuccess = true
                    resetFormFields()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Withdrawal failed." // Use stringResource
                }
            } catch (e: Exception) {
                errorMessage = "An unexpected error occurred: ${e.message}" // Use stringResource
            } finally {
                isLoading = false
            }
        }
    }

    private fun isValidSepaForm(): Boolean {
        if (sepaAccountHolderName.isBlank()) { errorMessage = "SEPA: Account holder name is required."; return false }
        if (sepaIban.isBlank()) { errorMessage = "SEPA: IBAN is required and must be valid."; return false } // TODO: Add real IBAN validation
        if (sepaBicSwift.isBlank()) { errorMessage = "SEPA: BIC/SWIFT is required."; return false } // TODO: Add real BIC validation
        return true
    }

    private fun isValidLatamForm(): Boolean {
        if (latamFirstName.isBlank() || latamLastName.isBlank()) { errorMessage = "LATAM: First and last name are required."; return false }
        if (latamBankAccountNumber.isBlank()) { errorMessage = "LATAM: Bank account number is required."; return false } // TODO: Add real CLABE/CBU validation
        if (latamBankName.isBlank() && selectedCountryCode == "CO") { errorMessage = "LATAM: Bank name is required for Colombia."; return false }
        return true
    }

    private fun getTargetCurrencyForLatam(countryCode: String): String {
        return when (countryCode.uppercase()) {
            "MX" -> "MXN"; "CO" -> "COP"; "AR" -> "ARS"
            else -> "USD" // Fallback
        }
    }

    fun clearMessages() {
        errorMessage = null
        withdrawalSuccess = false
    }
}

