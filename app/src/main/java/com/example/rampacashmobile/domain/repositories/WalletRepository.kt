package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result

/**
 * Wallet repository interface defining data access operations for Wallet entities
 * 
 * This interface is part of the domain layer and defines the contract
 * for wallet data access without specifying implementation details
 */
interface WalletRepository {
    
    /**
     * Find a wallet by its unique ID
     * 
     * @param id The wallet ID
     * @return Result containing the wallet if found, or error if not found
     */
    suspend fun findById(id: String): Result<Wallet>

    /**
     * Find a wallet by its address
     * 
     * @param address The wallet address
     * @return Result containing the wallet if found, or error if not found
     */
    suspend fun findByAddress(address: WalletAddress): Result<Wallet>

    /**
     * Find all wallets for a specific user
     * 
     * @param userId The user ID
     * @return Result containing list of wallets
     */
    suspend fun findByUserId(userId: UserId): Result<List<Wallet>>

    /**
     * Find active wallets for a specific user
     * 
     * @param userId The user ID
     * @return Result containing list of active wallets
     */
    suspend fun findActiveByUserId(userId: UserId): Result<List<Wallet>>

    /**
     * Save a new wallet
     * 
     * @param wallet The wallet to save
     * @return Result indicating success or failure
     */
    suspend fun save(wallet: Wallet): Result<Unit>

    /**
     * Update an existing wallet
     * 
     * @param wallet The wallet to update
     * @return Result indicating success or failure
     */
    suspend fun update(wallet: Wallet): Result<Unit>

    /**
     * Delete a wallet by its ID
     * 
     * @param id The wallet ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: String): Result<Unit>

    /**
     * Check if a wallet exists by address
     * 
     * @param address The wallet address
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByAddress(address: WalletAddress): Result<Boolean>

    /**
     * Check if a wallet exists for a user
     * 
     * @param userId The user ID
     * @return Result containing true if exists, false otherwise
     */
    suspend fun existsByUserId(userId: UserId): Result<Boolean>

    /**
     * Find the primary wallet for a user
     * 
     * @param userId The user ID
     * @return Result containing the primary wallet if found
     */
    suspend fun findPrimaryByUserId(userId: UserId): Result<Wallet>

    /**
     * Set a wallet as primary for a user
     * 
     * @param userId The user ID
     * @param walletId The wallet ID to set as primary
     * @return Result indicating success or failure
     */
    suspend fun setPrimary(userId: UserId, walletId: String): Result<Unit>
}
