package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import java.time.LocalDateTime

/**
 * Rich Transaction domain entity with business logic
 * 
 * This entity encapsulates all transaction-related business rules and operations
 */
class Transaction private constructor(
    val id: TransactionId,
    val userId: UserId,
    private var _fromWallet: WalletAddress,
    private var _toWallet: WalletAddress,
    private var _amount: Money,
    private var _currency: Currency,
    private var _status: TransactionStatus,
    private var _transactionType: TransactionType,
    private var _description: String,
    private var _solanaHash: String?,
    private var _fee: Money?,
    private var _createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime,
    private var _completedAt: LocalDateTime?
) {
    
    // Getters
    val fromWallet: WalletAddress get() = _fromWallet
    val toWallet: WalletAddress get() = _toWallet
    val amount: Money get() = _amount
    val currency: Currency get() = _currency
    val status: TransactionStatus get() = _status
    val transactionType: TransactionType get() = _transactionType
    val description: String get() = _description
    val solanaHash: String? get() = _solanaHash
    val fee: Money? get() = _fee
    val createdAt: LocalDateTime get() = _createdAt
    val updatedAt: LocalDateTime get() = _updatedAt
    val completedAt: LocalDateTime? get() = _completedAt

    companion object {
        fun create(
            userId: UserId,
            fromWallet: WalletAddress,
            toWallet: WalletAddress,
            amount: Money,
            transactionType: TransactionType,
            description: String = ""
        ): Result<Transaction> {
            return try {
                val transaction = Transaction(
                    id = TransactionId.generate(),
                    userId = userId,
                    _fromWallet = fromWallet,
                    _toWallet = toWallet,
                    _amount = amount,
                    _currency = amount.currency,
                    _status = TransactionStatus.PENDING,
                    _transactionType = transactionType,
                    _description = description.trim(),
                    _solanaHash = null,
                    _fee = null,
                    _createdAt = LocalDateTime.now(),
                    _updatedAt = LocalDateTime.now(),
                    _completedAt = null
                )
                Result.success(transaction)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to create transaction: ${e.message}"))
            }
        }

        fun restore(
            id: TransactionId,
            userId: UserId,
            fromWallet: WalletAddress,
            toWallet: WalletAddress,
            amount: Money,
            currency: Currency,
            status: TransactionStatus,
            transactionType: TransactionType,
            description: String,
            solanaHash: String?,
            fee: Money?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
            completedAt: LocalDateTime?
        ): Result<Transaction> {
            return try {
                val transaction = Transaction(
                    id = id,
                    userId = userId,
                    _fromWallet = fromWallet,
                    _toWallet = toWallet,
                    _amount = amount,
                    _currency = currency,
                    _status = status,
                    _transactionType = transactionType,
                    _description = description,
                    _solanaHash = solanaHash,
                    _fee = fee,
                    _createdAt = createdAt,
                    _updatedAt = updatedAt,
                    _completedAt = completedAt
                )
                Result.success(transaction)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to restore transaction: ${e.message}"))
            }
        }
    }

    // Business operations
    fun confirm(solanaHash: String, fee: Money? = null): Result<Unit> {
        return if (canConfirm()) {
            _solanaHash = solanaHash
            _fee = fee
            _status = TransactionStatus.CONFIRMED
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(TransactionCannotConfirmException())
        }
    }

    fun complete(): Result<Unit> {
        return if (canComplete()) {
            _status = TransactionStatus.COMPLETED
            _completedAt = LocalDateTime.now()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(TransactionCannotCompleteException())
        }
    }

    fun fail(reason: String): Result<Unit> {
        return if (canFail()) {
            _status = TransactionStatus.FAILED
            _description = "$_description - Failed: $reason".trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(TransactionCannotFailException())
        }
    }

    fun cancel(reason: String): Result<Unit> {
        return if (canCancel()) {
            _status = TransactionStatus.CANCELLED
            _description = "$_description - Cancelled: $reason".trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(TransactionCannotCancelException())
        }
    }

    fun updateDescription(newDescription: String): Result<Unit> {
        return if (canUpdateDescription()) {
            _description = newDescription.trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(TransactionCannotUpdateDescriptionException())
        }
    }

    // Business rules
    private fun canConfirm(): Boolean = _status == TransactionStatus.PENDING
    private fun canComplete(): Boolean = _status == TransactionStatus.CONFIRMED
    private fun canFail(): Boolean = _status in listOf(TransactionStatus.PENDING, TransactionStatus.CONFIRMED)
    private fun canCancel(): Boolean = _status == TransactionStatus.PENDING
    private fun canUpdateDescription(): Boolean = _status == TransactionStatus.PENDING

    // Status checks
    fun isPending(): Boolean = _status == TransactionStatus.PENDING
    fun isConfirmed(): Boolean = _status == TransactionStatus.CONFIRMED
    fun isCompleted(): Boolean = _status == TransactionStatus.COMPLETED
    fun isFailed(): Boolean = _status == TransactionStatus.FAILED
    fun isCancelled(): Boolean = _status == TransactionStatus.CANCELLED
    fun isFinal(): Boolean = _status in listOf(TransactionStatus.COMPLETED, TransactionStatus.FAILED, TransactionStatus.CANCELLED)

    // Type checks
    fun isSend(): Boolean = _transactionType == TransactionType.SEND
    fun isReceive(): Boolean = _transactionType == TransactionType.RECEIVE
    fun isTransfer(): Boolean = _transactionType == TransactionType.TRANSFER

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Transaction(id=$id, amount=$_amount, status=$_status, type=$_transactionType)"
}

/**
 * Transaction status enumeration
 */
enum class TransactionStatus {
    PENDING,    // Transaction created but not yet confirmed
    CONFIRMED,  // Transaction confirmed on blockchain
    COMPLETED,  // Transaction fully completed
    FAILED,     // Transaction failed
    CANCELLED   // Transaction cancelled
}

/**
 * Transaction type enumeration
 */
enum class TransactionType {
    SEND,       // User sending money
    RECEIVE,    // User receiving money
    TRANSFER    // Internal transfer between user's wallets
}
