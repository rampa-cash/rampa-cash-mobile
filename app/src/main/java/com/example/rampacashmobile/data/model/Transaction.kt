package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * Transaction data model matching backend Transaction entity
 * Record of money transfers between users
 */
@Serializable
data class Transaction(
    val id: String? = null, // UUID from backend
    val senderId: String = "", // Foreign Key to User
    val recipientId: String = "", // Foreign Key to User
    val senderWalletId: String = "", // Foreign Key to Wallet
    val recipientWalletId: String = "", // Foreign Key to Wallet
    @Contextual val amount: BigDecimal = BigDecimal.ZERO, // Transfer amount
    val tokenType: TokenType = TokenType.USDC, // Token type
    val status: TransactionStatus = TransactionStatus.PENDING,
    val solanaTransactionHash: String? = null, // Solana transaction hash
    val description: String? = null, // User-provided description
    @Contextual val fee: BigDecimal = BigDecimal.ZERO, // Transaction fee
    val createdAt: String? = null, // ISO timestamp from backend
    val confirmedAt: String? = null, // ISO timestamp from backend
    val failedAt: String? = null, // ISO timestamp from backend
    val failureReason: String? = null // Reason for failure
) {
    val isPending: Boolean
        get() = status == TransactionStatus.PENDING

    val isConfirmed: Boolean
        get() = status == TransactionStatus.CONFIRMED

    val isFailed: Boolean
        get() = status == TransactionStatus.FAILED

    val isCancelled: Boolean
        get() = status == TransactionStatus.CANCELLED

    val isCompleted: Boolean
        get() = status == TransactionStatus.CONFIRMED

    val isFinal: Boolean
        get() = status in listOf(TransactionStatus.CONFIRMED, TransactionStatus.FAILED, TransactionStatus.CANCELLED)

    val effectiveAmount: BigDecimal
        get() = amount - fee

    val formattedAmount: String
        get() = "${amount.toPlainString()} ${tokenType.value}"

    val formattedFee: String
        get() = "${fee.toPlainString()} ${tokenType.value}"

    val formattedEffectiveAmount: String
        get() = "${effectiveAmount.toPlainString()} ${tokenType.value}"

    val hasSolanaHash: Boolean
        get() = solanaTransactionHash?.isNotBlank() == true

    val shortSolanaHash: String
        get() = if (solanaTransactionHash?.length ?: 0 > 8) {
            "${solanaTransactionHash?.take(4)}...${solanaTransactionHash?.takeLast(4)}"
        } else {
            solanaTransactionHash ?: ""
        }

    val statusDisplayText: String
        get() = when (status) {
            TransactionStatus.PENDING -> "Pending"
            TransactionStatus.CONFIRMED -> "Confirmed"
            TransactionStatus.FAILED -> "Failed: ${failureReason ?: "Unknown error"}"
            TransactionStatus.CANCELLED -> "Cancelled"
        }

    fun canCancel(): Boolean = isPending

    fun canRetry(): Boolean = isFailed

    fun getAmountInDouble(): Double = amount.toDouble()

    fun getFeeInDouble(): Double = fee.toDouble()

    fun getEffectiveAmountInDouble(): Double = effectiveAmount.toDouble()
}

@Serializable
enum class TransactionStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String): TransactionStatus = values().find { it.value == value } ?: PENDING
    }
}
