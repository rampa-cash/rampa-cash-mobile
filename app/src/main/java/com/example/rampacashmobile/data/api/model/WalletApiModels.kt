package com.example.rampacashmobile.data.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * Create wallet request
 */
@Serializable
data class CreateWalletRequest(
    val address: String,
    val publicKey: String,
    val walletType: String
)

/**
 * Connect wallet request
 */
@Serializable
data class ConnectWalletRequest(
    val walletType: String,
    val address: String,
    val publicKey: String
)

/**
 * Update wallet request
 */
@Serializable
data class UpdateWalletRequest(
    val address: String? = null,
    val publicKey: String? = null,
    val walletType: String? = null
)

/**
 * Wallet response
 */
@Serializable
data class WalletResponse(
    val id: String,
    val address: String,
    val publicKey: String,
    val walletType: String,
    val status: String,
    val createdAt: String,
    val balances: List<TokenBalanceResponse> = emptyList()
)

/**
 * Token balance response
 */
@Serializable
data class TokenBalanceResponse(
    val tokenType: String,
    @Contextual val balance: BigDecimal,
    val lastUpdated: String
)

/**
 * Wallet balance response
 */
@Serializable
data class WalletBalanceResponse(
    val balances: List<TokenBalanceResponse>
)

/**
 * Get specific wallet balance request
 */
@Serializable
data class GetWalletBalanceRequest(
    val tokenType: String
)

/**
 * Transfer request
 */
@Serializable
data class TransferRequest(
    val toAddress: String,
    @Contextual val amount: BigDecimal,
    val tokenType: String,
    val description: String? = null
)
