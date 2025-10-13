package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.valueobjects.TransactionId
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import java.time.LocalDateTime

/**
 * Transaction repository interface defining data access operations for Transaction entities
 * 
 * This interface is part of the domain layer and defines the contract
 * for transaction data access without specifying implementation details
 */
interface TransactionRepository {
    
    /**
     * Find a transaction by its unique ID
     * 
     * @param id The transaction ID
     * @return Result containing the transaction if found, or error if not found
     */
    suspend fun findById(id: TransactionId): Result<Transaction>

    /**
     * Find all transactions for a specific user
     * 
     * @param userId The user ID
     * @return Result containing list of transactions
     */
    suspend fun findByUserId(userId: UserId): Result<List<Transaction>>

    /**
     * Find transactions for a specific user with pagination
     * 
     * @param userId The user ID
     * @param limit The maximum number of transactions to return
     * @param offset The number of transactions to skip
     * @return Result containing list of transactions
     */
    suspend fun findByUserId(userId: UserId, limit: Int, offset: Int): Result<List<Transaction>>

    /**
     * Find transactions for a specific wallet address
     * 
     * @param walletAddress The wallet address
     * @return Result containing list of transactions
     */
    suspend fun findByWalletAddress(walletAddress: WalletAddress): Result<List<Transaction>>

    /**
     * Find transactions by status
     * 
     * @param status The transaction status
     * @return Result containing list of transactions
     */
    suspend fun findByStatus(status: String): Result<List<Transaction>>

    /**
     * Find transactions by type
     * 
     * @param type The transaction type
     * @return Result containing list of transactions
     */
    suspend fun findByType(type: String): Result<List<Transaction>>

    /**
     * Find transactions within a date range
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Result containing list of transactions
     */
    suspend fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Result<List<Transaction>>

    /**
     * Find transactions for a user within a date range
     * 
     * @param userId The user ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Result containing list of transactions
     */
    suspend fun findByUserIdAndDateRange(
        userId: UserId, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Result<List<Transaction>>

    /**
     * Find transactions by Solana hash
     * 
     * @param solanaHash The Solana transaction hash
     * @return Result containing the transaction if found
     */
    suspend fun findBySolanaHash(solanaHash: String): Result<Transaction>

    /**
     * Save a new transaction
     * 
     * @param transaction The transaction to save
     * @return Result indicating success or failure
     */
    suspend fun save(transaction: Transaction): Result<Unit>

    /**
     * Update an existing transaction
     * 
     * @param transaction The transaction to update
     * @return Result indicating success or failure
     */
    suspend fun update(transaction: Transaction): Result<Unit>

    /**
     * Delete a transaction by its ID
     * 
     * @param id The transaction ID
     * @return Result indicating success or failure
     */
    suspend fun delete(id: TransactionId): Result<Unit>

    /**
     * Get transaction count for a user
     * 
     * @param userId The user ID
     * @return Result containing the transaction count
     */
    suspend fun getCountByUserId(userId: UserId): Result<Int>

    /**
     * Get transaction count by status
     * 
     * @param status The transaction status
     * @return Result containing the transaction count
     */
    suspend fun getCountByStatus(status: String): Result<Int>

    /**
     * Find recent transactions for a user
     * 
     * @param userId The user ID
     * @param limit The maximum number of transactions to return
     * @return Result containing list of recent transactions
     */
    suspend fun findRecentByUserId(userId: UserId, limit: Int = 10): Result<List<Transaction>>
}
