package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * On-ramp request
 */
@Serializable
data class OnRampRequest(
    @Contextual val amount: BigDecimal,
    val fiatCurrency: String,
    val tokenType: String,
    val paymentMethod: String? = null
)

/**
 * Off-ramp request
 */
@Serializable
data class OffRampRequest(
    @Contextual val amount: BigDecimal,
    val tokenType: String,
    val bankAccount: BankAccountRequest
)

/**
 * Bank account request
 */
@Serializable
data class BankAccountRequest(
    val iban: String,
    val accountHolderName: String,
    val bankName: String? = null
)

/**
 * On-ramp response
 */
@Serializable
data class OnRampResponse(
    val id: String,
    val userId: String,
    val walletId: String,
    val type: String,
    @Contextual val amount: BigDecimal,
    @Contextual val fiatAmount: BigDecimal,
    val fiatCurrency: String,
    val tokenType: String,
    val status: String,
    val provider: String,
    val providerTransactionId: String? = null,
    @Contextual val exchangeRate: BigDecimal,
    @Contextual val fee: BigDecimal,
    val createdAt: String,
    val completedAt: String? = null,
    val failedAt: String? = null,
    val failureReason: String? = null
)

/**
 * Off-ramp response
 */
@Serializable
data class OffRampResponse(
    val id: String,
    @Contextual val amount: BigDecimal,
    @Contextual val fiatAmount: BigDecimal,
    val fiatCurrency: String,
    val tokenType: String,
    val status: String,
    @Contextual val exchangeRate: BigDecimal,
    @Contextual val fee: BigDecimal,
    val estimatedDelivery: String,
    val createdAt: String
)

/**
 * Process on-ramp request
 */
@Serializable
data class ProcessOnRampRequest(
    val providerTransactionId: String
)

/**
 * Process off-ramp request
 */
@Serializable
data class ProcessOffRampRequest(
    val providerTransactionId: String
)

/**
 * Fail on-ramp request
 */
@Serializable
data class FailOnRampRequest(
    val failureReason: String
)

/**
 * Fail off-ramp request
 */
@Serializable
data class FailOffRampRequest(
    val failureReason: String
)

/**
 * On-ramp statistics response
 */
@Serializable
data class OnRampStatsResponse(
    @Contextual val totalOnRamp: BigDecimal,
    @Contextual val totalFees: BigDecimal,
    @Contextual val completedOnRamp: BigDecimal,
    @Contextual val failedOnRamp: BigDecimal
)

/**
 * Off-ramp statistics response
 */
@Serializable
data class OffRampStatsResponse(
    @Contextual val totalOffRamp: BigDecimal,
    @Contextual val totalFees: BigDecimal,
    @Contextual val completedOffRamp: BigDecimal,
    @Contextual val failedOffRamp: BigDecimal
)
