package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import java.time.LocalDateTime

/**
 * Rich Wallet domain entity with business logic
 * 
 * This entity encapsulates all wallet-related business rules and operations
 */
class Wallet private constructor(
    val id: String,
    val userId: UserId,
    private var _address: WalletAddress,
    private var _label: String,
    private var _isActive: Boolean,
    private var _createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime
) {
    
    // Getters
    val address: WalletAddress get() = _address
    val label: String get() = _label
    val isActive: Boolean get() = _isActive
    val createdAt: LocalDateTime get() = _createdAt
    val updatedAt: LocalDateTime get() = _updatedAt

    companion object {
        fun create(
            userId: UserId,
            address: WalletAddress,
            label: String = ""
        ): Result<Wallet> {
            return try {
                val wallet = Wallet(
                    id = generateWalletId(),
                    userId = userId,
                    _address = address,
                    _label = label.trim().ifBlank { "Wallet" },
                    _isActive = true,
                    _createdAt = LocalDateTime.now(),
                    _updatedAt = LocalDateTime.now()
                )
                Result.success(wallet)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to create wallet: ${e.message}"))
            }
        }

        fun restore(
            id: String,
            userId: UserId,
            address: WalletAddress,
            label: String,
            isActive: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Result<Wallet> {
            return try {
                val wallet = Wallet(
                    id = id,
                    userId = userId,
                    _address = address,
                    _label = label,
                    _isActive = isActive,
                    _createdAt = createdAt,
                    _updatedAt = updatedAt
                )
                Result.success(wallet)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to restore wallet: ${e.message}"))
            }
        }

        private fun generateWalletId(): String {
            return "wallet_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }
    }

    // Business operations
    fun updateLabel(newLabel: String): Result<Unit> {
        return if (canUpdateLabel()) {
            _label = newLabel.trim().ifBlank { "Wallet" }
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(WalletCannotUpdateLabelException())
        }
    }

    fun activate(): Result<Unit> {
        return if (canActivate()) {
            _isActive = true
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(WalletCannotActivateException())
        }
    }

    fun deactivate(): Result<Unit> {
        return if (canDeactivate()) {
            _isActive = false
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(WalletCannotDeactivateException())
        }
    }

    // Business rules
    private fun canUpdateLabel(): Boolean = _isActive
    private fun canActivate(): Boolean = !_isActive
    private fun canDeactivate(): Boolean = _isActive

    // Status checks
    fun isActiveStatus(): Boolean = _isActive
    fun isInactive(): Boolean = !_isActive

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallet) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Wallet(id=$id, address=$_address, active=$_isActive)"
}
