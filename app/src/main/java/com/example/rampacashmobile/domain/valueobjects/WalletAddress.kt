package com.example.rampacashmobile.domain.valueobjects

import com.solana.publickey.SolanaPublicKey

/**
 * WalletAddress value object representing a Solana wallet address
 * 
 * This value object enforces business rules:
 * - Address must be a valid Solana public key
 * - Address cannot be empty or null
 * - Provides safe operations for address handling
 */
@JvmInline
value class WalletAddress private constructor(
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Wallet address cannot be blank" }
        require(isValidSolanaAddress(value)) { "Invalid Solana wallet address format" }
    }

    companion object {
        fun of(address: String): WalletAddress {
            return WalletAddress(address.trim())
        }

        fun of(publicKey: SolanaPublicKey): WalletAddress {
            return WalletAddress(publicKey.base58())
        }

        private fun isValidSolanaAddress(address: String): Boolean {
            return try {
                // Basic validation for Solana address format
                // Solana addresses are base58 encoded and typically 32-44 characters
                address.length in 32..44 && address.matches(Regex("^[1-9A-HJ-NP-Za-km-z]+$"))
            } catch (e: Exception) {
                false
            }
        }
    }

    fun toSolanaPublicKey(): SolanaPublicKey {
        return try {
            SolanaPublicKey.from(value)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Solana public key: $value", e)
        }
    }

    fun toShortFormat(): String {
        return if (value.length > 8) {
            "${value.take(4)}...${value.takeLast(4)}"
        } else {
            value
        }
    }

    fun toDisplayFormat(): String {
        return if (value.length > 12) {
            "${value.take(6)}...${value.takeLast(6)}"
        } else {
            value
        }
    }

    override fun toString(): String = value
}

/**
 * Extension function to convert String to WalletAddress
 */
fun String.toWalletAddress(): WalletAddress = WalletAddress.of(this)

/**
 * Extension function to convert SolanaPublicKey to WalletAddress
 */
fun SolanaPublicKey.toWalletAddress(): WalletAddress = WalletAddress.of(this)
