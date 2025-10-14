package com.example.rampacashmobile.domain.repositories

import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.valueobjects.TransactionId
import com.example.rampacashmobile.domain.valueobjects.UserId
import javax.inject.Inject

class MockTransactionRepository @Inject constructor() : TransactionRepository {
    
    private val transactions = mutableMapOf<String, Transaction>()
    
    override suspend fun findById(id: TransactionId): Result<Transaction> {
        return transactions[id.value]?.let { Result.success(it) }
            ?: Result.failure(com.example.rampacashmobile.domain.common.DomainError.NotFound("Transaction not found"))
    }
    
    override suspend fun findByUserId(userId: UserId): Result<List<Transaction>> {
        val userTransactions = transactions.values.filter { 
            it.fromWallet.userId == userId || it.toWallet.userId == userId 
        }
        return Result.success(userTransactions)
    }
    
    override suspend fun save(transaction: Transaction): Result<Unit> {
        transactions[transaction.id.value] = transaction
        return Result.success(Unit)
    }
    
    override suspend fun update(transaction: Transaction): Result<Unit> {
        transactions[transaction.id.value] = transaction
        return Result.success(Unit)
    }
    
    override suspend fun delete(id: TransactionId): Result<Unit> {
        transactions.remove(id.value)
        return Result.success(Unit)
    }
    
    fun addTransaction(transaction: Transaction) {
        transactions[transaction.id.value] = transaction
    }
    
    fun clear() {
        transactions.clear()
    }
}
