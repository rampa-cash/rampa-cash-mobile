// File: app/src/main/java/com/example/rampacashmobile/viewmodel/WithdrawViewModel.kt
package com.example.rampacashmobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// Data class for UI state if it becomes complex
// data class WithdrawUiState(...)

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    // Inject your repositories or use cases for off-ramping here
    // private val offRampRepository: OffRampRepository
) : ViewModel() {

    var amount by mutableStateOf("")
    var accountHolderName by mutableStateOf("")
    var iban by mutableStateOf("")
    var bicSwift by mutableStateOf("")
    var bankName by mutableStateOf("")
    var reference by mutableStateOf("")

    var selectedTokenSymbol by mutableStateOf("USD") // Or fetch dynamically
    // var availableBalance by mutableStateOf(0.0) // Fetch this

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var withdrawalSuccess by mutableStateOf(false)
        private set

    fun onAmountChange(newAmount: String) {
        amount = newAmount
        // Add validation if needed
    }

    fun onAccountHolderNameChange(newName: String) {
        accountHolderName = newName
    }

    fun onIbanChange(newIban: String) {
        iban = newIban.uppercase().filter { !it.isWhitespace() } // Basic IBAN formatting
    }

    fun onBicSwiftChange(newBic: String) {
        bicSwift = newBic.uppercase().filter { !it.isWhitespace() }
    }

    fun onBankNameChange(newName: String) {
        bankName = newName
    }

    fun onReferenceChange(newRef: String) {
        reference = newRef
    }

    fun initiateWithdrawal() {
        // 1. Validate all fields (IBAN format, amount > 0, required fields not empty etc.)
        if (!isValid()) {
            // errorMessage = "Please fill all required fields correctly."
            return
        }

        isLoading = true
        errorMessage = null
        withdrawalSuccess = false

        // viewModelScope.launch {
        //    try {
        //        val result = offRampRepository.initiateSepaWithdrawal(
        //            token = selectedTokenSymbol,
        //            amount = amount.toDoubleOrNull() ?: 0.0,
        //            iban = iban,
        //            accountHolderName = accountHolderName,
        //            bic = bicSwift,
        //            bankName = bankName,
        //            reference = reference
        //        )
        //        if (result.isSuccess) {
        //            withdrawalSuccess = true
        //        } else {
        //            errorMessage = result.errorMessage ?: "Withdrawal failed."
        //        }
        //    } catch (e: Exception) {
        //        errorMessage = "An unexpected error occurred: ${e.message}"
        //    } finally {
        //        isLoading = false
        //    }
        // }
        println("Simulating withdrawal initiation...") // Replace with actual logic
        // Simulate network call
        // kotlinx.coroutines.GlobalScope.launch { // Use viewModelScope
        //    kotlinx.coroutines.delay(2000)
        //    isLoading = false
        //    // withdrawalSuccess = true // or errorMessage = "Failed"
        // }
    }

    private fun isValid(): Boolean {
        // Basic validation example
        if (amount.toDoubleOrNull() ?: 0.0 <= 0) {
            errorMessage = "Invalid amount."
            return false
        }
        if (accountHolderName.isBlank()) {
            errorMessage = "Account holder name is required."
            return false
        }
        if (iban.isBlank()) { // TODO: Add proper IBAN validation regex
            errorMessage = "IBAN is required."
            return false
        }
        if (bicSwift.isBlank()) { // TODO: Add proper BIC validation
            errorMessage = "BIC/SWIFT is required."
            return false
        }
        if (bankName.isBlank()) {
            errorMessage = "Bank name is required."
            return false
        }
        errorMessage = null
        return true
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetSuccessFlag() {
        withdrawalSuccess = false
    }
}
