package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * Create VISA card request
 */
@Serializable
data class CreateVISACardRequest(
    val cardType: String,
    @Contextual val dailyLimit: BigDecimal,
    @Contextual val monthlyLimit: BigDecimal
)

/**
 * Update VISA card request
 */
@Serializable
data class UpdateVISACardRequest(
    val cardType: String? = null,
    @Contextual val dailyLimit: BigDecimal? = null,
    @Contextual val monthlyLimit: BigDecimal? = null
)

/**
 * VISA card response
 */
@Serializable
data class VISACardResponse(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val cardType: String,
    val status: String,
    @Contextual val balance: BigDecimal,
    @Contextual val dailyLimit: BigDecimal,
    @Contextual val monthlyLimit: BigDecimal,
    val createdAt: String,
    val activatedAt: String? = null,
    val expiresAt: String
)

/**
 * Update VISA card balance request
 */
@Serializable
data class UpdateVISACardBalanceRequest(
    @Contextual val amount: BigDecimal
)

/**
 * Check VISA card spending limits request
 */
@Serializable
data class CheckVISACardSpendingLimitsRequest(
    @Contextual val amount: BigDecimal
)

/**
 * Check VISA card spending limits response
 */
@Serializable
data class CheckVISACardSpendingLimitsResponse(
    val canSpend: Boolean,
    @Contextual val dailyRemaining: BigDecimal,
    @Contextual val monthlyRemaining: BigDecimal,
    @Contextual val requestedAmount: BigDecimal
)

/**
 * VISA card statistics response
 */
@Serializable
data class VISACardStatsResponse(
    val totalCards: Int,
    val activeCards: Int,
    val suspendedCards: Int,
    val cancelledCards: Int,
    val expiredCards: Int
)
