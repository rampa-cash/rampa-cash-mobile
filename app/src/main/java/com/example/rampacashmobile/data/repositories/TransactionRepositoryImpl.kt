package com.example.rampacashmobile.data.repositories

import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.entities.TransactionStatus
import com.example.rampacashmobile.domain.entities.TransactionType
import com.example.rampacashmobile.domain.repositories.TransactionRepository
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.TransactionId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor() : TransactionRepository {
    
    private val transactions = mutableMapOf<String, Transaction>()
    
    override suspend fun findById(id: TransactionId): Result<Transaction> {
        return try {
            val transaction = transactions[id.value]
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(DomainError.NotFound("Transaction with id ${id.value} not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transaction: ${e.message}"))
        }
    }
    
    override suspend fun findByUserId(userId: UserId): Result<List<Transaction>> {
        return try {
            val userTransactions = transactions.values.filter { it.userId == userId }
            Result.success(userTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions for user: ${e.message}"))
        }
    }
    
    override suspend fun findByUserId(userId: UserId, limit: Int, offset: Int): Result<List<Transaction>> {
        return try {
            val userTransactions = transactions.values
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
                .drop(offset)
                .take(limit)
            Result.success(userTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions for user: ${e.message}"))
        }
    }
    
    override suspend fun findByWalletAddress(walletAddress: WalletAddress): Result<List<Transaction>> {
        return try {
            val walletTransactions = transactions.values.filter { it.fromWallet == walletAddress || it.toWallet == walletAddress }
            Result.success(walletTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions for wallet: ${e.message}"))
        }
    }
    
    override suspend fun findByStatus(status: String): Result<List<Transaction>> {
        return try {
            val statusEnum = TransactionStatus.valueOf(status.uppercase())
            val statusTransactions = transactions.values.filter { it.status == statusEnum }
            Result.success(statusTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions by status: ${e.message}"))
        }
    }
    
    override suspend fun findByType(type: String): Result<List<Transaction>> {
        return try {
            val typeEnum = TransactionType.valueOf(type.uppercase())
            val typeTransactions = transactions.values.filter { it.transactionType == typeEnum }
            Result.success(typeTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions by type: ${e.message}"))
        }
    }
    
    override suspend fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Result<List<Transaction>> {
        return try {
            val rangeTransactions = transactions.values.filter { 
                it.createdAt.isAfter(startDate) && it.createdAt.isBefore(endDate) 
            }
            Result.success(rangeTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions by date range: ${e.message}"))
        }
    }
    
    override suspend fun findByUserIdAndDateRange(
        userId: UserId, 
        startDate: LocalDateTime, 
        endDate: LocalDateTime
    ): Result<List<Transaction>> {
        return try {
            val userRangeTransactions = transactions.values.filter { 
                it.userId == userId && 
                it.createdAt.isAfter(startDate) && 
                it.createdAt.isBefore(endDate) 
            }
            Result.success(userRangeTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transactions for user by date range: ${e.message}"))
        }
    }
    
    override suspend fun findBySolanaHash(solanaHash: String): Result<Transaction> {
        return try {
            val transaction = transactions.values.find { it.solanaHash == solanaHash }
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(DomainError.NotFound("Transaction with Solana hash $solanaHash not found"))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find transaction by Solana hash: ${e.message}"))
        }
    }
    
    override suspend fun save(transaction: Transaction): Result<Unit> {
        return try {
            transactions[transaction.id.value] = transaction
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to save transaction: ${e.message}"))
        }
    }
    
    override suspend fun update(transaction: Transaction): Result<Unit> {
        return try {
            transactions[transaction.id.value] = transaction
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to update transaction: ${e.message}"))
        }
    }
    
    override suspend fun delete(id: TransactionId): Result<Unit> {
        return try {
            transactions.remove(id.value)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to delete transaction: ${e.message}"))
        }
    }
    
    override suspend fun getCountByUserId(userId: UserId): Result<Int> {
        return try {
            val count = transactions.values.count { it.userId == userId }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to get transaction count: ${e.message}"))
        }
    }
    
    override suspend fun getCountByStatus(status: String): Result<Int> {
        return try {
            val statusEnum = TransactionStatus.valueOf(status.uppercase())
            val count = transactions.values.count { it.status == statusEnum }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to get transaction count by status: ${e.message}"))
        }
    }
    
    override suspend fun findRecentByUserId(userId: UserId, limit: Int): Result<List<Transaction>> {
        return try {
            val recentTransactions = transactions.values
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
                .take(limit)
            Result.success(recentTransactions)
        } catch (e: Exception) {
            Result.failure(DomainError.StorageError("Failed to find recent transactions: ${e.message}"))
        }
    }
}
