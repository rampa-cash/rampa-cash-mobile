// File: app/src/main/java/com/example/rampacashmobile/data/repository/FakeOffRampRepository.kt
package com.example.rampacashmobile.data.repository

import kotlinx.coroutines.delay
import javax.inject.Inject

// This class implements the OffRampRepository interface
class FakeOffRampRepository @Inject constructor() : OffRampRepository {

    var simulateNetworkError: Boolean = false
    var simulateSpecificError: String? = null
    var networkDelayMillis: Long = 1500 // Simulate network latency

    override suspend fun initiateSepaWithdrawal(details: SepaWithdrawalDetails): Result<Unit> {
        delay(networkDelayMillis)
        println("FakeOffRampRepository: ==> Initiating SEPA Withdrawal")
        println("FakeOffRampRepository: Details: $details")

        return when {
            simulateNetworkError -> {
                println("FakeOffRampRepository: <== SEPA Withdrawal FAILED (Simulated Network Error)")
                Result.failure(Exception("Simulated SEPA network connection error."))
            }
            simulateSpecificError != null -> {
                println("FakeOffRampRepository: <== SEPA Withdrawal FAILED (Simulated Specific Error: $simulateSpecificError)")
                Result.failure(Exception(simulateSpecificError))
            }
            // Example of a fake validation
            details.iban.length < 10 -> { // Adjust this condition as needed for testing
                println("FakeOffRampRepository: <== SEPA Withdrawal FAILED (Fake IBAN too short)")
                Result.failure(Exception("Fake Validation: IBAN appears too short."))
            }
            else -> {
                println("FakeOffRampRepository: <== SEPA Withdrawal SUCCESS (Simulated)")
                Result.success(Unit)
            }
        }
    }

    override suspend fun initiateBitsoWithdrawal(details: BitsoWithdrawalDetails): Result<Unit> {
        delay(networkDelayMillis)
        println("FakeOffRampRepository: ==> Initiating Bitso Withdrawal")
        println("FakeOffRampRepository: Details: $details")

        return when {
            simulateNetworkError -> {
                println("FakeOffRampRepository: <== Bitso Withdrawal FAILED (Simulated Network Error)")
                Result.failure(Exception("Simulated Bitso network connection error."))
            }
            simulateSpecificError != null -> {
                println("FakeOffRampRepository: <== Bitso Withdrawal FAILED (Simulated Specific Error: $simulateSpecificError)")
                Result.failure(Exception(simulateSpecificError))
            }
            // Example of a fake validation
            details.bankAccountNumber.isBlank() -> {
                println("FakeOffRampRepository: <== Bitso Withdrawal FAILED (Fake Account Number Blank)")
                Result.failure(Exception("Fake Validation: Bank account number cannot be blank for Bitso."))
            }
            else -> {
                println("FakeOffRampRepository: <== Bitso Withdrawal SUCCESS (Simulated)")
                Result.success(Unit)
            }
        }
    }

    // Helper methods to configure the fake's behavior for testing
    // Renamed this function to avoid clash with the generated setter for simulateNetworkError
    fun configureSimulateNetworkError(shouldSimulate: Boolean) {
        simulateNetworkError = shouldSimulate
        simulateSpecificError = null // Clear specific error if network error is set
    }

    fun configureSimulateSpecificError(errorMessage: String?) {
        simulateSpecificError = errorMessage
        simulateNetworkError = false // Clear network error if specific error is set
    }
}
