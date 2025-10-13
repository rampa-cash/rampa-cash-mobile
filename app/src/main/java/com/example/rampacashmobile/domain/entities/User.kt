package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.exceptions.*
import java.time.LocalDateTime

/**
 * Rich User domain entity with business logic
 * 
 * This entity encapsulates all user-related business rules and operations
 */
class User private constructor(
    val id: UserId,
    private var _firstName: String,
    private var _lastName: String,
    private var _email: Email,
    private var _phoneNumber: String,
    private var _walletAddress: WalletAddress?,
    private var _authProvider: AuthProvider,
    private var _authProviderId: String,
    private var _status: UserStatus,
    private var _language: String,
    private var _profileImageUrl: String?,
    private var _isEmailVerified: Boolean,
    private var _isPhoneVerified: Boolean,
    private var _createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime
) {
    
    // Getters
    val firstName: String get() = _firstName
    val lastName: String get() = _lastName
    val email: Email get() = _email
    val phoneNumber: String get() = _phoneNumber
    val walletAddress: WalletAddress? get() = _walletAddress
    val authProvider: AuthProvider get() = _authProvider
    val authProviderId: String get() = _authProviderId
    val status: UserStatus get() = _status
    val language: String get() = _language
    val profileImageUrl: String? get() = _profileImageUrl
    val isEmailVerified: Boolean get() = _isEmailVerified
    val isPhoneVerified: Boolean get() = _isPhoneVerified
    val createdAt: LocalDateTime get() = _createdAt
    val updatedAt: LocalDateTime get() = _updatedAt

    // Computed properties
    val fullName: String get() = "$_firstName $_lastName".trim()
    val displayName: String get() = if (fullName.isNotBlank()) fullName else _email.value
    val initials: String get() = "${_firstName.firstOrNull()?.uppercaseChar() ?: ""}${_lastName.firstOrNull()?.uppercaseChar() ?: ""}"

    companion object {
        fun create(
            firstName: String,
            lastName: String,
            email: Email,
            phoneNumber: String,
            authProvider: AuthProvider,
            authProviderId: String,
            language: String = "en"
        ): Result<User> {
            return try {
                val user = User(
                    id = UserId.generate(),
                    _firstName = firstName.trim(),
                    _lastName = lastName.trim(),
                    _email = email,
                    _phoneNumber = phoneNumber.trim(),
                    _walletAddress = null,
                    _authProvider = authProvider,
                    _authProviderId = authProviderId,
                    _status = UserStatus.PENDING,
                    _language = language,
                    _profileImageUrl = null,
                    _isEmailVerified = false,
                    _isPhoneVerified = false,
                    _createdAt = LocalDateTime.now(),
                    _updatedAt = LocalDateTime.now()
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to create user: ${e.message}"))
            }
        }

        fun restore(
            id: UserId,
            firstName: String,
            lastName: String,
            email: Email,
            phoneNumber: String,
            walletAddress: WalletAddress?,
            authProvider: AuthProvider,
            authProviderId: String,
            status: UserStatus,
            language: String,
            profileImageUrl: String?,
            isEmailVerified: Boolean,
            isPhoneVerified: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): Result<User> {
            return try {
                val user = User(
                    id = id,
                    _firstName = firstName,
                    _lastName = lastName,
                    _email = email,
                    _phoneNumber = phoneNumber,
                    _walletAddress = walletAddress,
                    _authProvider = authProvider,
                    _authProviderId = authProviderId,
                    _status = status,
                    _language = language,
                    _profileImageUrl = profileImageUrl,
                    _isEmailVerified = isEmailVerified,
                    _isPhoneVerified = isPhoneVerified,
                    _createdAt = createdAt,
                    _updatedAt = updatedAt
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(DomainError.ValidationError("Failed to restore user: ${e.message}"))
            }
        }
    }

    // Business operations
    fun updatePersonalInfo(firstName: String, lastName: String): Result<Unit> {
        return if (canUpdatePersonalInfo()) {
            _firstName = firstName.trim()
            _lastName = lastName.trim()
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotUpdatePersonalInfoException())
        }
    }

    fun changeEmail(newEmail: Email): Result<Unit> {
        return if (canChangeEmail()) {
            _email = newEmail
            _isEmailVerified = false
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotChangeEmailException())
        }
    }

    fun changePhoneNumber(newPhoneNumber: String): Result<Unit> {
        return if (canChangePhoneNumber()) {
            _phoneNumber = newPhoneNumber.trim()
            _isPhoneVerified = false
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotChangePhoneNumberException())
        }
    }

    fun setWalletAddress(walletAddress: WalletAddress): Result<Unit> {
        return if (canSetWalletAddress()) {
            _walletAddress = walletAddress
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotSetWalletAddressException())
        }
    }

    fun verifyEmail(): Result<Unit> {
        return if (canVerifyEmail()) {
            _isEmailVerified = true
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotVerifyEmailException())
        }
    }

    fun verifyPhone(): Result<Unit> {
        return if (canVerifyPhone()) {
            _isPhoneVerified = true
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotVerifyPhoneException())
        }
    }

    fun activate(): Result<Unit> {
        return if (canActivate()) {
            _status = UserStatus.ACTIVE
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotActivateException())
        }
    }

    fun suspend(): Result<Unit> {
        return if (canSuspend()) {
            _status = UserStatus.SUSPENDED
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotSuspendException())
        }
    }

    fun deactivate(): Result<Unit> {
        return if (canDeactivate()) {
            _status = UserStatus.INACTIVE
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotDeactivateException())
        }
    }

    fun updateProfileImage(imageUrl: String): Result<Unit> {
        return if (canUpdateProfileImage()) {
            _profileImageUrl = imageUrl
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotUpdateProfileImageException())
        }
    }

    fun changeLanguage(language: String): Result<Unit> {
        return if (canChangeLanguage()) {
            _language = language
            _updatedAt = LocalDateTime.now()
            Result.success(Unit)
        } else {
            Result.failure(UserCannotChangeLanguageException())
        }
    }

    // Business rules
    private fun canUpdatePersonalInfo(): Boolean = _status != UserStatus.SUSPENDED
    private fun canChangeEmail(): Boolean = _status != UserStatus.SUSPENDED
    private fun canChangePhoneNumber(): Boolean = _status != UserStatus.SUSPENDED
    private fun canSetWalletAddress(): Boolean = _status != UserStatus.SUSPENDED && _walletAddress == null
    private fun canVerifyEmail(): Boolean = _status != UserStatus.SUSPENDED && !_isEmailVerified
    private fun canVerifyPhone(): Boolean = _status != UserStatus.SUSPENDED && !_isPhoneVerified
    private fun canActivate(): Boolean = _status == UserStatus.PENDING
    private fun canSuspend(): Boolean = _status == UserStatus.ACTIVE
    private fun canDeactivate(): Boolean = _status == UserStatus.ACTIVE
    private fun canUpdateProfileImage(): Boolean = _status != UserStatus.SUSPENDED
    private fun canChangeLanguage(): Boolean = _status != UserStatus.SUSPENDED

    // Status checks
    fun isActive(): Boolean = _status == UserStatus.ACTIVE
    fun isPending(): Boolean = _status == UserStatus.PENDING
    fun isSuspended(): Boolean = _status == UserStatus.SUSPENDED
    fun isInactive(): Boolean = _status == UserStatus.INACTIVE
    fun isFullyVerified(): Boolean = _isEmailVerified && _isPhoneVerified
    fun hasWallet(): Boolean = _walletAddress != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "User(id=$id, email=$_email, status=$_status)"
}

/**
 * User status enumeration
 */
enum class UserStatus {
    PENDING,    // User created but not yet activated
    ACTIVE,     // User is active and can use the app
    SUSPENDED,  // User is temporarily suspended
    INACTIVE    // User is inactive/deactivated
}

/**
 * Authentication provider enumeration
 */
enum class AuthProvider(val value: String) {
    GOOGLE("google"),
    APPLE("apple"),
    SMS("sms"),
    WALLET("wallet");

    companion object {
        fun fromString(value: String): AuthProvider? = values().find { it.value == value }
    }
}
