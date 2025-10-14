package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * Create transaction request
 */
@Serializable
data class CreateTransactionRequest(
    val senderId: String,
    val recipientId: String,
    val senderWalletId: String,
    val recipientWalletId: String,
    @Contextual val amount: BigDecimal,
    val tokenType: String,
    val description: String? = null,
    @Contextual val fee: BigDecimal = BigDecimal.ZERO
)

/**
 * Transaction response
 */
@Serializable
data class TransactionResponse(
    val id: String,
    val senderId: String,
    val recipientId: String,
    @Contextual val amount: BigDecimal,
    val tokenType: String,
    val status: String,
    val description: String? = null,
    @Contextual val fee: BigDecimal,
    val createdAt: String,
    val confirmedAt: String? = null,
    val failedAt: String? = null,
    val failureReason: String? = null,
    val solanaTransactionHash: String? = null
)

/**
 * Confirm transaction request
 */
@Serializable
data class ConfirmTransactionRequest(
    val solanaTransactionHash: String
)

/**
 * Transaction statistics response
 */
@Serializable
data class TransactionStatsResponse(
    @Contextual val totalSent: BigDecimal,
    @Contextual val totalReceived: BigDecimal,
    @Contextual val totalFees: BigDecimal,
    val transactionCount: Int
)
