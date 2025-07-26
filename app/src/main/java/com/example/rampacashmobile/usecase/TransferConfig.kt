package com.example.rampacashmobile.usecase

/**
 * Transfer Configuration
 * 
 * Controls which SPL token transfer implementation to use.
 * Switch between implementations easily by changing USE_MANUAL_TRANSFER.
 */
object TransferConfig {
    
    /**
     * Transfer Implementation Selection
     * 
     * true  = Manual Transfer (recommended for Solflare compatibility)
     *         - Bypasses web3-solana library serialization bugs
     *         - Uses direct RPC calls and manual transaction building
     *         - Fixes "transaction cannot be simulated" error in Solflare
     * 
     * false = Library Transfer (original implementation)
     *         - Uses web3-solana library's Message.Builder()
     *         - May have simulation issues with some wallets
     *         - Kept for compatibility and debugging
     */
    const val USE_MANUAL_TRANSFER = true
    
    /**
     * Debug logging for transfer operations
     */
    const val ENABLE_TRANSFER_LOGGING = true
    
    /**
     * Get current implementation name for logging/debugging
     */
    fun getImplementationName(): String {
        return if (USE_MANUAL_TRANSFER) {
            "Manual Transfer (Solflare-Compatible)"
        } else {
            "Library Transfer (web3-solana)"
        }
    }
    
    /**
     * Get emoji indicator for current implementation
     */
    fun getImplementationEmoji(): String {
        return if (USE_MANUAL_TRANSFER) "��" else "📚"
    }
} 