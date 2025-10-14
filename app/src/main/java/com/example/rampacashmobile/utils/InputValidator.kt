package com.example.rampacashmobile.utils

import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.valueobjects.Email
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.Money
import com.example.rampacashmobile.domain.valueobjects.Currency
import com.example.rampacashmobile.constants.AppConstants
import java.math.BigDecimal

/**
 * Input validation utility using domain value objects and ValidationError
 * 
 * This utility provides consistent input validation throughout the app
 * using our domain value objects and proper error handling
 */
object InputValidator {
    
    /**
     * Validates an email address using our Email value object
     * 
     * @param email The email string to validate
     * @return Result containing the validated Email or ValidationError
     */
    fun validateEmail(email: String): Result<Email> {
        return try {
            val emailValue = Email.of(email)
            Result.success(emailValue)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid email format: ${e.message}", e))
        }
    }
    
    /**
     * Validates a wallet address using our WalletAddress value object
     * 
     * @param address The wallet address string to validate
     * @return Result containing the validated WalletAddress or ValidationError
     */
    fun validateWalletAddress(address: String): Result<WalletAddress> {
        return try {
            val walletAddress = WalletAddress.of(address)
            Result.success(walletAddress)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid wallet address: ${e.message}", e))
        }
    }
    
    /**
     * Validates a money amount using our Money value object
     * 
     * @param amount The amount string to validate
     * @param currency The currency code
     * @return Result containing the validated Money or ValidationError
     */
    fun validateMoneyAmount(amount: String, currency: String): Result<Money> {
        return try {
            val amountValue = BigDecimal(amount)
            val currencyEnum = when (currency.uppercase()) {
                "USD" -> Currency.USD
                "EUR" -> Currency.EUR
                else -> return Result.failure(DomainError.ValidationError("Unsupported currency: $currency"))
            }
            
            val money = Money(amountValue, currencyEnum)
            
            // Check minimum amount
            if (money.amount < AppConstants.MIN_AMOUNT_VALUE) {
                return Result.failure(DomainError.ValidationError("Amount must be at least ${AppConstants.MIN_AMOUNT_VALUE}"))
            }
            
            Result.success(money)
        } catch (e: NumberFormatException) {
            Result.failure(DomainError.ValidationError("Invalid amount format: $amount", e))
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid money amount: ${e.message}", e))
        }
    }
    
    /**
     * Validates a transaction description
     * 
     * @param description The description to validate
     * @return Result containing the validated description or ValidationError
     */
    fun validateTransactionDescription(description: String): Result<String> {
        return try {
            val trimmedDescription = description.trim()
            
            if (trimmedDescription.length > AppConstants.MAX_DESCRIPTION_LENGTH) {
                return Result.failure(DomainError.ValidationError("Description too long. Maximum ${AppConstants.MAX_DESCRIPTION_LENGTH} characters allowed"))
            }
            
            Result.success(trimmedDescription.ifBlank { AppConstants.DEFAULT_TRANSACTION_DESCRIPTION })
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid description: ${e.message}", e))
        }
    }
    
    /**
     * Validates a contact name
     * 
     * @param name The name to validate
     * @return Result containing the validated name or ValidationError
     */
    fun validateContactName(name: String): Result<String> {
        return try {
            val trimmedName = name.trim()
            
            if (trimmedName.isBlank()) {
                return Result.failure(DomainError.ValidationError("Contact name cannot be empty"))
            }
            
            if (trimmedName.length < 2) {
                return Result.failure(DomainError.ValidationError("Contact name must be at least 2 characters"))
            }
            
            if (trimmedName.length > 50) {
                return Result.failure(DomainError.ValidationError("Contact name too long. Maximum 50 characters allowed"))
            }
            
            Result.success(trimmedName)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid contact name: ${e.message}", e))
        }
    }
    
    /**
     * Validates a phone number
     * 
     * @param phoneNumber The phone number to validate
     * @return Result containing the validated phone number or ValidationError
     */
    fun validatePhoneNumber(phoneNumber: String): Result<String> {
        return try {
            val trimmedPhone = phoneNumber.trim()
            
            if (trimmedPhone.isBlank()) {
                return Result.failure(DomainError.ValidationError("Phone number cannot be empty"))
            }
            
            // Basic phone number validation (digits, +, -, spaces, parentheses)
            val phoneRegex = Regex("^[\\+]?[0-9\\s\\-\\(\\)]{7,20}$")
            if (!phoneRegex.matches(trimmedPhone)) {
                return Result.failure(DomainError.ValidationError("Invalid phone number format"))
            }
            
            Result.success(trimmedPhone)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid phone number: ${e.message}", e))
        }
    }
    
    /**
     * Validates a password
     * 
     * @param password The password to validate
     * @return Result containing the validated password or ValidationError
     */
    fun validatePassword(password: String): Result<String> {
        return try {
            if (password.length < AppConstants.MIN_PASSWORD_LENGTH) {
                return Result.failure(DomainError.ValidationError("Password must be at least ${AppConstants.MIN_PASSWORD_LENGTH} characters"))
            }
            
            if (password.length > 128) {
                return Result.failure(DomainError.ValidationError("Password too long. Maximum 128 characters allowed"))
            }
            
            // Check for at least one letter and one number
            val hasLetter = password.any { it.isLetter() }
            val hasDigit = password.any { it.isDigit() }
            
            if (!hasLetter || !hasDigit) {
                return Result.failure(DomainError.ValidationError("Password must contain at least one letter and one number"))
            }
            
            Result.success(password)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid password: ${e.message}", e))
        }
    }
    
    /**
     * Validates a user ID
     * 
     * @param userId The user ID string to validate
     * @return Result containing the validated UserId or ValidationError
     */
    fun validateUserId(userId: String): Result<com.example.rampacashmobile.domain.valueobjects.UserId> {
        return try {
            val userIdValue = com.example.rampacashmobile.domain.valueobjects.UserId.of(userId)
            Result.success(userIdValue)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid user ID: ${e.message}", e))
        }
    }
    
    /**
     * Validates a transaction ID
     * 
     * @param transactionId The transaction ID string to validate
     * @return Result containing the validated TransactionId or ValidationError
     */
    fun validateTransactionId(transactionId: String): Result<com.example.rampacashmobile.domain.valueobjects.TransactionId> {
        return try {
            val transactionIdValue = com.example.rampacashmobile.domain.valueobjects.TransactionId.of(transactionId)
            Result.success(transactionIdValue)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid transaction ID: ${e.message}", e))
        }
    }
    
    /**
     * Validates a currency code
     * 
     * @param currency The currency code to validate
     * @return Result containing the validated Currency or ValidationError
     */
    fun validateCurrency(currency: String): Result<Currency> {
        return try {
            val currencyEnum = when (currency.uppercase()) {
                "USD" -> Currency.USD
                "EUR" -> Currency.EUR
                else -> return Result.failure(DomainError.ValidationError("Unsupported currency: $currency"))
            }
            Result.success(currencyEnum)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid currency: ${e.message}", e))
        }
    }
    
    /**
     * Validates a form field is not empty
     * 
     * @param value The value to validate
     * @param fieldName The name of the field for error messages
     * @return Result containing the validated value or ValidationError
     */
    fun validateNotEmpty(value: String, fieldName: String): Result<String> {
        return try {
            val trimmedValue = value.trim()
            if (trimmedValue.isBlank()) {
                return Result.failure(DomainError.ValidationError("$fieldName cannot be empty"))
            }
            Result.success(trimmedValue)
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid $fieldName: ${e.message}", e))
        }
    }
    
    /**
     * Validates a numeric range
     * 
     * @param value The value to validate
     * @param min The minimum value
     * @param max The maximum value
     * @param fieldName The name of the field for error messages
     * @return Result containing the validated value or ValidationError
     */
    fun validateNumericRange(value: String, min: Double, max: Double, fieldName: String): Result<Double> {
        return try {
            val numericValue = value.toDouble()
            if (numericValue < min || numericValue > max) {
                return Result.failure(DomainError.ValidationError("$fieldName must be between $min and $max"))
            }
            Result.success(numericValue)
        } catch (e: NumberFormatException) {
            Result.failure(DomainError.ValidationError("Invalid $fieldName format: $value", e))
        } catch (e: Exception) {
            Result.failure(DomainError.ValidationError("Invalid $fieldName: ${e.message}", e))
        }
    }
}
