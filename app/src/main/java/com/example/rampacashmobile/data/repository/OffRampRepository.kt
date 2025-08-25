package com.example.rampacashmobile.data.repository

// File: app/src/main/java/com/example/rampacashmobile/data/repository/OffRampRepository.kt

// You'll also need to define these data classes.
// You can put them in this file for now, or in a separate 'models/offramp' package.
data class SepaWithdrawalDetails(
    val amount: String,
    val currency: String, // e.g., "EUR"
    val accountHolderName: String,
    val iban: String,
    val bicSwift: String,
    val bankName: String, // Can be optional depending on provider
    val reference: String? // Optional
)

data class BitsoWithdrawalDetails(
    val amount: String,
    val currency: String, // e.g., "MXN", "COP"
    val countryCode: String, // e.g., "MX", "CO"
    val firstName: String,
    val lastName: String,
    val bankAccountNumber: String, // For CLABE, CBU, local account number
    val bankName: String?, // Can be optional or required based on country/Bitso
    // Add any other specific fields Bitso might require for withdrawal,
    // e.g., beneficiary_rfc for Mexico, etc.
)

/**
 * Interface for off-ramp operations, allowing withdrawal of funds to external accounts.
 */
interface OffRampRepository {

    /**
     * Initiates a SEPA (Single Euro Payments Area) withdrawal.
     * Typically used for providers like Transak or Ramp Network in Europe.
     *
     * @param details The SEPA withdrawal details.
     * @return A Result indicating success or failure.
     */
    suspend fun initiateSepaWithdrawal(details: SepaWithdrawalDetails): Result<Unit>

    /**
     * Initiates a withdrawal via Bitso.
     * Typically used for countries in Latin America.
     *
     * @param details The Bitso withdrawal details.
     * @return A Result indicating success or failure.
     */
    suspend fun initiateBitsoWithdrawal(details: BitsoWithdrawalDetails): Result<Unit>
}