package com.example.rampacashmobile.domain.services

import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.entities.TransactionType
import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.repositories.TransactionRepository
import com.example.rampacashmobile.domain.repositories.WalletRepository
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import javax.inject.Inject

/**
 * Transaction domain service containing business logic for transaction operations
 * 
 * This service encapsulates complex transaction-related business rules
 * that don't naturally belong to the Transaction entity itself
 */
class TransactionDomainService @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {
    
    /**
     * Process a new transaction
     * 
     * @param userId The user ID
     * @param fromWallet The sender wallet address
     * @param toWallet The recipient wallet address
     * @param amount The transaction amount
     * @param transactionType The type of transaction
     * @param description Optional transaction description
     * @return Result containing the created transaction
     */
    suspend fun processTransaction(
        userId: UserId,
        fromWallet: WalletAddress,
        toWallet: WalletAddress,
        amount: Money,
        transactionType: TransactionType,
        description: String = ""
    ): Result<Transaction> {
        return try {
            // Validate transaction
            validateTransaction(fromWallet, toWallet, amount)
                .flatMap {
                    // Create transaction
                    Transaction.create(userId, fromWallet, toWallet, amount, transactionType, description)
                        .flatMap { transaction ->
                            // Save transaction
                            transactionRepository.save(transaction)
                                .map { transaction }
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to process transaction: ${e.message}", e))
        }
    }

    /**
     * Confirm a transaction with Solana hash
     * 
     * @param transactionId The transaction ID
     * @param solanaHash The Solana transaction hash
     * @param fee Optional transaction fee
     * @return Result indicating success or failure
     */
    suspend fun confirmTransaction(
        transactionId: TransactionId,
        solanaHash: String,
        fee: Money? = null
    ): Result<Unit> {
        return try {
            transactionRepository.findById(transactionId)
                .flatMap { transaction ->
                    transaction.confirm(solanaHash, fee)
                        .flatMap {
                            transactionRepository.update(transaction)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to confirm transaction: ${e.message}", e))
        }
    }

    /**
     * Complete a transaction
     * 
     * @param transactionId The transaction ID
     * @return Result indicating success or failure
     */
    suspend fun completeTransaction(transactionId: TransactionId): Result<Unit> {
        return try {
            transactionRepository.findById(transactionId)
                .flatMap { transaction ->
                    transaction.complete()
                        .flatMap {
                            transactionRepository.update(transaction)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to complete transaction: ${e.message}", e))
        }
    }

    /**
     * Fail a transaction
     * 
     * @param transactionId The transaction ID
     * @param reason The failure reason
     * @return Result indicating success or failure
     */
    suspend fun failTransaction(transactionId: TransactionId, reason: String): Result<Unit> {
        return try {
            transactionRepository.findById(transactionId)
                .flatMap { transaction ->
                    transaction.fail(reason)
                        .flatMap {
                            transactionRepository.update(transaction)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to fail transaction: ${e.message}", e))
        }
    }

    /**
     * Cancel a transaction
     * 
     * @param transactionId The transaction ID
     * @param reason The cancellation reason
     * @return Result indicating success or failure
     */
    suspend fun cancelTransaction(transactionId: TransactionId, reason: String): Result<Unit> {
        return try {
            transactionRepository.findById(transactionId)
                .flatMap { transaction ->
                    transaction.cancel(reason)
                        .flatMap {
                            transactionRepository.update(transaction)
                        }
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to cancel transaction: ${e.message}", e))
        }
    }

    /**
     * Get user transactions
     * 
     * @param userId The user ID
     * @param limit Optional limit
     * @param offset Optional offset
     * @return Result containing list of transactions
     */
    suspend fun getUserTransactions(
        userId: UserId,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<Transaction>> {
        return if (limit != null && offset != null) {
            transactionRepository.findByUserId(userId, limit, offset)
        } else {
            transactionRepository.findByUserId(userId)
        }
    }

    /**
     * Get recent transactions for a user
     * 
     * @param userId The user ID
     * @param limit The maximum number of transactions
     * @return Result containing list of recent transactions
     */
    suspend fun getRecentTransactions(userId: UserId, limit: Int = 10): Result<List<Transaction>> {
        return transactionRepository.findRecentByUserId(userId, limit)
    }

    /**
     * Get transaction by ID
     * 
     * @param transactionId The transaction ID
     * @return Result containing the transaction
     */
    suspend fun getTransaction(transactionId: TransactionId): Result<Transaction> {
        return transactionRepository.findById(transactionId)
    }

    /**
     * Get transaction by Solana hash
     * 
     * @param solanaHash The Solana transaction hash
     * @return Result containing the transaction
     */
    suspend fun getTransactionBySolanaHash(solanaHash: String): Result<Transaction> {
        return transactionRepository.findBySolanaHash(solanaHash)
    }

    /**
     * Get transaction count for a user
     * 
     * @param userId The user ID
     * @return Result containing the transaction count
     */
    suspend fun getTransactionCount(userId: UserId): Result<Int> {
        return transactionRepository.getCountByUserId(userId)
    }

    /**
     * Validate transaction before processing
     * 
     * @param fromWallet The sender wallet
     * @param toWallet The recipient wallet
     * @param amount The transaction amount
     * @return Result indicating validation success or failure
     */
    private suspend fun validateTransaction(
        fromWallet: WalletAddress,
        toWallet: WalletAddress,
        amount: Money
    ): Result<Unit> {
        return when {
            fromWallet == toWallet -> Result.failure(CannotSendToSelfException())
            amount.isZero() -> Result.failure(ZeroAmountException())
            !amount.isPositive() -> Result.failure(InvalidTransactionAmountException("Amount must be positive"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Check if user can send transaction
     * 
     * @param userId The user ID
     * @param amount The transaction amount
     * @return Result indicating if user can send
     */
    suspend fun canUserSendTransaction(userId: UserId, amount: Money): Result<Boolean> {
        return try {
            // Get user's primary wallet
            walletRepository.findPrimaryByUserId(userId)
                .flatMap { wallet ->
                    // Check if wallet has sufficient balance
                    // This would typically involve checking the actual balance
                    // For now, we'll return true as a placeholder
                    Result.success(true)
                }
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Failed to check if user can send: ${e.message}", e))
        }
    }
}
