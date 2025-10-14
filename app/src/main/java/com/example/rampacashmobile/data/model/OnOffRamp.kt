package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * OnOffRamp data model matching backend OnOffRamp entity
 * Represents fiat currency conversion service records
 */
@Serializable
data class OnOffRamp(
    val id: String? = null, // UUID from backend
    val userId: String = "", // Foreign Key to User
    val walletId: String = "", // Foreign Key to Wallet
    val type: RampType = RampType.ONRAMP,
    @Contextual val amount: BigDecimal = BigDecimal.ZERO, // Crypto amount
    @Contextual val fiatAmount: BigDecimal = BigDecimal.ZERO, // Fiat amount
    val fiatCurrency: String = "EUR", // Fiat currency code
    val tokenType: TokenType = TokenType.USDC, // Crypto token type
    val status: RampStatus = RampStatus.PENDING,
    val provider: String = "", // e.g., 'stripe', 'sepa_provider'
    val providerTransactionId: String? = null, // External provider ID
    @Contextual val exchangeRate: BigDecimal = BigDecimal.ZERO, // Exchange rate
    @Contextual val fee: BigDecimal = BigDecimal.ZERO, // Transaction fee
    val createdAt: String? = null, // ISO timestamp from backend
    val completedAt: String? = null, // ISO timestamp from backend
    val failedAt: String? = null, // ISO timestamp from backend
    val failureReason: String? = null // Reason for failure
) {
    val isCompleted: Boolean
        get() = status == RampStatus.COMPLETED

    val isFailed: Boolean
        get() = status == RampStatus.FAILED

    val isPending: Boolean
        get() = status == RampStatus.PENDING

    val isProcessing: Boolean
        get() = status == RampStatus.PROCESSING

    val effectiveAmount: BigDecimal
        get() = amount - fee

    val totalCost: BigDecimal
        get() = fiatAmount + fee

    val formattedAmount: String
        get() = "${amount.toPlainString()} ${tokenType.value}"

    val formattedFiatAmount: String
        get() = "${fiatAmount.toPlainString()} $fiatCurrency"

    val formattedExchangeRate: String
        get() = "1 ${tokenType.value} = ${exchangeRate.toPlainString()} $fiatCurrency"

    val formattedFee: String
        get() = "${fee.toPlainString()} ${tokenType.value}"

    fun canCancel(): Boolean = isPending

    fun canRetry(): Boolean = isFailed

    fun getStatusDisplayText(): String = when (status) {
        RampStatus.PENDING -> "Pending"
        RampStatus.PROCESSING -> "Processing"
        RampStatus.COMPLETED -> "Completed"
        RampStatus.FAILED -> "Failed: ${failureReason ?: "Unknown error"}"
    }
}

@Serializable
enum class RampType(val value: String) {
    ONRAMP("onramp"),
    OFFRAMP("offramp");

    companion object {
        fun fromValue(value: String): RampType = values().find { it.value == value } ?: ONRAMP
    }
}

@Serializable
enum class RampStatus(val value: String) {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed");

    companion object {
        fun fromValue(value: String): RampStatus = values().find { it.value == value } ?: PENDING
    }
}

@Serializable
enum class TokenType(val value: String) {
    USDC("USDC"),
    EURC("EURC"),
    SOL("SOL");

    companion object {
        fun fromValue(value: String): TokenType = values().find { it.value == value } ?: USDC
    }
}
