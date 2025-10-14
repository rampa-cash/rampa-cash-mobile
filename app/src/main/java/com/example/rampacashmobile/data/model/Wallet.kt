package com.example.rampacashmobile.data.model

import kotlinx.serialization.Serializable

/**
 * Wallet data model matching backend Wallet entity
 * Represents non-custodial wallet containing token balances and wallet metadata
 */
@Serializable
data class Wallet(
    val id: String? = null, // UUID from backend
    val userId: String = "", // Foreign Key to User
    val address: String = "", // Solana wallet address
    val publicKey: String = "", // Solana public key
    val walletType: WalletType = WalletType.WEB3AUTH_MPC,
    val isActive: Boolean = true,
    val status: WalletStatus = WalletStatus.ACTIVE,
    val createdAt: String? = null, // ISO timestamp from backend
    val updatedAt: String? = null // ISO timestamp from backend
) {
    val isConnected: Boolean
        get() = isActive && status == WalletStatus.ACTIVE && address.isNotBlank()

    val shortAddress: String
        get() = if (address.length > 8) {
            "${address.take(4)}...${address.takeLast(4)}"
        } else {
            address
        }

    val displayName: String
        get() = when (walletType) {
            WalletType.WEB3AUTH_MPC -> "Web3Auth Wallet"
            WalletType.PHANTOM -> "Phantom Wallet"
            WalletType.SOLFLARE -> "Solflare Wallet"
        }

    fun canSend(): Boolean = isConnected

    fun canReceive(): Boolean = isConnected
}

@Serializable
enum class WalletType(val value: String) {
    WEB3AUTH_MPC("web3auth_mpc"),
    PHANTOM("phantom"),
    SOLFLARE("solflare");

    companion object {
        fun fromValue(value: String): WalletType = values().find { it.value == value } ?: WEB3AUTH_MPC
    }
}

@Serializable
enum class WalletStatus(val value: String) {
    ACTIVE("active"),
    SUSPENDED("suspended");

    companion object {
        fun fromValue(value: String): WalletStatus = values().find { it.value == value } ?: ACTIVE
    }
}
