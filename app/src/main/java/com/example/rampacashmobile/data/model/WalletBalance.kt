package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal

/**
 * WalletBalance data model matching backend WalletBalance entity
 * Tracks token balances for each wallet
 */
@Serializable
data class WalletBalance(
    val id: String? = null, // UUID from backend
    val walletId: String = "", // Foreign Key to Wallet
    val tokenType: TokenType = TokenType.USDC,
    @Contextual val balance: BigDecimal = BigDecimal.ZERO, // Token balance
    val lastUpdated: String? = null, // ISO timestamp from backend
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null // ISO timestamp from backend
) {
    val formattedBalance: String
        get() = "${balance.toPlainString()} ${tokenType.value}"

    val hasBalance: Boolean
        get() = balance > BigDecimal.ZERO

    val isEmpty: Boolean
        get() = balance == BigDecimal.ZERO

    val isUSDC: Boolean
        get() = tokenType == TokenType.USDC

    val isEURC: Boolean
        get() = tokenType == TokenType.EURC

    val isSOL: Boolean
        get() = tokenType == TokenType.SOL

    fun canSend(amount: BigDecimal): Boolean = balance >= amount

    fun getBalanceInDouble(): Double = balance.toDouble()

    fun getBalanceInFloat(): Float = balance.toFloat()

    companion object {
        fun createEmpty(tokenType: TokenType, walletId: String): WalletBalance = WalletBalance(
            walletId = walletId,
            tokenType = tokenType,
            balance = BigDecimal.ZERO
        )

        fun createWithBalance(
            tokenType: TokenType,
            walletId: String,
            balance: BigDecimal
        ): WalletBalance = WalletBalance(
            walletId = walletId,
            tokenType = tokenType,
            balance = balance
        )
    }
}
