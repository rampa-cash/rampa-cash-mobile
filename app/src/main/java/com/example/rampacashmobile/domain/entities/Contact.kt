package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import java.time.LocalDateTime

/**
 * Rich Contact domain entity with business logic
 * 
 * This entity encapsulates all contact-related business rules and operations
 */
class Contact private constructor(
    val id: String,
    val userId: UserId,
    private var _name: String,
    private var _email: Email?,
    private var _phoneNumber: String?,
    private var _walletAddress: WalletAddress?,
    private var _isFavorite: Boolean,
    private var _createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime
) {
    
    // Getters
    val name: String get() = _name
    val email: Email? get() = _email
    val phoneNumber: String? get() = _phoneNumber
    val walletAddress: WalletAddress? get() = _walletAddress
    val isFavorite: Boolean get() = _isFavorite
    val createdAt: LocalDateTime get() = _createdAt
    val updatedAt: LocalDateTime get() = _updatedAt

    companion object {
        fun create(
            userId: UserId,
            name: String,
            email: Email? = null,
            phoneNumber: String? = null,
            walletAddress: WalletAddress? = null
        ): Result<Contact> {
            return try {
                validateContactData(name, email, phoneNumber, walletAddress)
                val contact = Contact(
                    id = generateContactId(),
                    userId = userId,
                    _name = name.trim(),
                    _email = email,
                    _phoneNumber = phoneNumber?.trim(),
                    _walletAddress = walletAddress,
                    _isFavorite = false,
                    _createdAt = LocalDateTime.now(),
                    _updatedAt = LocalDateTime.now()
                )
                Result.success(contact)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to create contact: ${e.message}"))
            }
        }

        fun restore(
            id: String,
            userId: UserId,
            name: String,
            email: Email?,
            phoneNumber: String?,
            walletAddress: WalletAddress?,
            isFavorite: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Result<Contact> {
            return try {
                val contact = Contact(
                    id = id,
                    userId = userId,
                    _name = name,
                    _email = email,
                    _phoneNumber = phoneNumber,
                    _walletAddress = walletAddress,
                    _isFavorite = isFavorite,
                    _createdAt = createdAt,
                    _updatedAt = updatedAt
                )
                Result.success(contact)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to restore contact: ${e.message}"))
            }
        }

        private fun generateContactId(): String {
            return "contact_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }

        private fun validateContactData(
            name: String,
            email: Email?,
            phoneNumber: String?,
            walletAddress: WalletAddress?
        ) {
            require(name.isNotBlank()) { "Contact name cannot be blank" }
            require(email != null || phoneNumber != null || walletAddress != null) { 
                "Contact must have at least one contact method (email, phone, or wallet address)" 
            }
        }
    }

    // Business operations
    fun updateName(newName: String): Result<Unit> {
        return if (canUpdateName()) {
            require(newName.isNotBlank()) { "Contact name cannot be blank" }
            _name = newName.trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotUpdateNameException())
        }
    }

    fun updateEmail(newEmail: Email?): Result<Unit> {
        return if (canUpdateEmail()) {
            _email = newEmail
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotUpdateEmailException())
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String?): Result<Unit> {
        return if (canUpdatePhoneNumber()) {
            _phoneNumber = newPhoneNumber?.trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotUpdatePhoneNumberException())
        }
    }

    fun updateWalletAddress(newWalletAddress: WalletAddress?): Result<Unit> {
        return if (canUpdateWalletAddress()) {
            _walletAddress = newWalletAddress
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotUpdateWalletAddressException())
        }
    }

    fun addToFavorites(): Result<Unit> {
        return if (canAddToFavorites()) {
            _isFavorite = true
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotAddToFavoritesException())
        }
    }

    fun removeFromFavorites(): Result<Unit> {
        return if (canRemoveFromFavorites()) {
            _isFavorite = false
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(ContactCannotRemoveFromFavoritesException())
        }
    }

    // Business rules
    private fun canUpdateName(): Boolean = true
    private fun canUpdateEmail(): Boolean = true
    private fun canUpdatePhoneNumber(): Boolean = true
    private fun canUpdateWalletAddress(): Boolean = true
    private fun canAddToFavorites(): Boolean = !_isFavorite
    private fun canRemoveFromFavorites(): Boolean = _isFavorite

    // Status checks
    fun hasEmail(): Boolean = _email != null
    fun hasPhoneNumber(): Boolean = _phoneNumber != null
    fun hasWalletAddress(): Boolean = _walletAddress != null
    fun hasAnyContactMethod(): Boolean = hasEmail() || hasPhoneNumber() || hasWalletAddress()
    fun isInFavorites(): Boolean = _isFavorite

    // Display methods
    fun getDisplayName(): String = _name
    fun getPrimaryContactMethod(): String {
        val walletAddress = _walletAddress
        val email = _email
        return when {
            walletAddress != null -> "Wallet: ${walletAddress.toDisplayFormat()}"
            email != null -> "Email: ${email.value}"
            _phoneNumber != null -> "Phone: $_phoneNumber"
            else -> _name
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Contact(id=$id, name=$_name, favorite=$_isFavorite)"
}
