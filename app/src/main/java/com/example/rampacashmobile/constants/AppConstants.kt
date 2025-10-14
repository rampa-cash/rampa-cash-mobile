package com.example.rampacashmobile.constants

import java.math.BigDecimal

/**
 * Application constants for magic numbers, strings, and configuration values
 * 
 * This file centralizes all hardcoded values to improve maintainability
 * and reduce magic numbers throughout the codebase
 */
object AppConstants {
    
    // ============================================================================
    // TOKEN DECIMAL PLACES
    // ============================================================================
    
    /** SOL has 9 decimal places */
    const val SOL_DECIMAL_PLACES = 9
    
    /** USDC has 6 decimal places */
    const val USDC_DECIMAL_PLACES = 6
    
    /** EURC has 6 decimal places */
    const val EURC_DECIMAL_PLACES = 6
    
    // ============================================================================
    // TOKEN DIVISORS (10^decimal_places)
    // ============================================================================
    
    /** SOL divisor: 10^9 = 1,000,000,000 */
    const val SOL_DIVISOR = 1_000_000_000L
    
    /** USDC divisor: 10^6 = 1,000,000 */
    const val USDC_DIVISOR = 1_000_000L
    
    /** EURC divisor: 10^6 = 1,000,000 */
    const val EURC_DIVISOR = 1_000_000L
    
    // ============================================================================
    // TOKEN SYMBOLS
    // ============================================================================
    
    const val SOL_SYMBOL = "SOL"
    const val USDC_SYMBOL = "USDC"
    const val EURC_SYMBOL = "EURC"
    
    // ============================================================================
    // CURRENCY CODES
    // ============================================================================
    
    const val USD_CURRENCY_CODE = "USD"
    const val EUR_CURRENCY_CODE = "EUR"
    
    // ============================================================================
    // UI CONSTANTS
    // ============================================================================
    
    /** Default loading state for initial app load */
    const val DEFAULT_LOADING_STATE = true
    
    /** Default wallet found state */
    const val DEFAULT_WALLET_FOUND = true
    
    /** Default transaction capability state */
    const val DEFAULT_CAN_TRANSACT = false
    
    /** Default balance values */
    const val DEFAULT_BALANCE = 0.0
    
    // ============================================================================
    // VALIDATION CONSTANTS
    // ============================================================================
    
    /** Minimum amount value for transactions */
    val MIN_AMOUNT_VALUE = BigDecimal("0.01")
    
    /** Maximum description length for transactions */
    const val MAX_DESCRIPTION_LENGTH = 250
    
    /** Minimum password length */
    const val MIN_PASSWORD_LENGTH = 8
    
    // ============================================================================
    // NETWORK CONSTANTS
    // ============================================================================
    
    /** Default RPC URI for Solana network */
    const val DEFAULT_RPC_URI = "https://api.devnet.solana.com"
    
    /** Web3Auth redirect URI */
    const val WEB3AUTH_REDIRECT_URI = "com.example.rampacashmobile://auth"
    
    // ============================================================================
    // TRANSACTION CONSTANTS
    // ============================================================================
    
    /** Default transaction description */
    const val DEFAULT_TRANSACTION_DESCRIPTION = "Transaction"
    
    /** Default transaction memo */
    const val DEFAULT_TRANSACTION_MEMO = ""
    
    // ============================================================================
    // ERROR MESSAGES
    // ============================================================================
    
    const val ERROR_WALLET_NOT_CONNECTED = "❌ | Please connect your wallet first"
    const val ERROR_INSUFFICIENT_FUNDS = "❌ | Insufficient funds for transaction"
    const val ERROR_NETWORK_FAILURE = "❌ | Network error occurred"
    const val ERROR_INVALID_ADDRESS = "❌ | Invalid wallet address"
    const val ERROR_TRANSACTION_FAILED = "❌ | Transaction failed"
    
    // ============================================================================
    // SUCCESS MESSAGES
    // ============================================================================
    
    const val SUCCESS_WALLET_CONNECTED = "✅ | Wallet connected successfully"
    const val SUCCESS_TRANSACTION_SENT = "✅ | Transaction sent successfully"
    const val SUCCESS_CONTACT_SAVED = "✅ | Contact saved successfully"
    
    // ============================================================================
    // VALIDATION CONSTANTS
    // ============================================================================
    
    /** Minimum transaction amount */
    val MIN_TRANSACTION_AMOUNT = BigDecimal("0.000001")
    
    /** Maximum transaction amount */
    val MAX_TRANSACTION_AMOUNT = BigDecimal("1000000")
    
    /** Maximum contact name length */
    const val MAX_CONTACT_NAME_LENGTH = 100
    
    /** Maximum memo length */
    const val MAX_MEMO_LENGTH = 500
    
    // ============================================================================
    // TIMING CONSTANTS
    // ============================================================================
    
    /** Default delay for UI updates (milliseconds) */
    const val DEFAULT_UI_DELAY_MS = 100L
    
    /** Retry delay for failed operations (milliseconds) */
    const val RETRY_DELAY_MS = 1000L
    
    /** Maximum retry attempts */
    const val MAX_RETRY_ATTEMPTS = 3
    
    // ============================================================================
    // SOLANA SPECIFIC CONSTANTS
    // ============================================================================
    
    /** System Program ID */
    const val SYSTEM_PROGRAM_ID = "11111111111111111111111111111112"
    
    /** Compute Budget Program ID */
    const val COMPUTE_BUDGET_PROGRAM_ID = "ComputeBudget111111111111111111111111111111"
    
    /** Associated Token Program ID */
    const val ASSOCIATED_TOKEN_PROGRAM_ID = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL"
    
    /** Token Program ID */
    const val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    
    // ============================================================================
    // UTILITY FUNCTIONS
    // ============================================================================
    
    /**
     * Convert lamports to SOL
     */
    fun lamportsToSol(lamports: Long): Double {
        return lamports / SOL_DIVISOR.toDouble()
    }
    
    /**
     * Convert SOL to lamports
     */
    fun solToLamports(sol: Double): Long {
        return (sol * SOL_DIVISOR).toLong()
    }
    
    /**
     * Convert token units to human-readable amount
     */
    fun tokenUnitsToAmount(units: Long, decimals: Int): Double {
        val divisor = Math.pow(10.0, decimals.toDouble()).toLong()
        return units / divisor.toDouble()
    }
    
    /**
     * Convert human-readable amount to token units
     */
    fun amountToTokenUnits(amount: Double, decimals: Int): Long {
        val multiplier = Math.pow(10.0, decimals.toDouble()).toLong()
        return (amount * multiplier).toLong()
    }
    
    /**
     * Get token divisor by symbol
     */
    fun getTokenDivisor(symbol: String): Long {
        return when (symbol.uppercase()) {
            SOL_SYMBOL -> SOL_DIVISOR
            USDC_SYMBOL -> USDC_DIVISOR
            EURC_SYMBOL -> EURC_DIVISOR
            else -> 1L
        }
    }
    
    /**
     * Get token decimal places by symbol
     */
    fun getTokenDecimals(symbol: String): Int {
        return when (symbol.uppercase()) {
            SOL_SYMBOL -> SOL_DECIMAL_PLACES
            USDC_SYMBOL -> USDC_DECIMAL_PLACES
            EURC_SYMBOL -> EURC_DECIMAL_PLACES
            else -> 0
        }
    }
}
