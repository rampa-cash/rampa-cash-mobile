package com.example.rampacashmobile.data.model

import java.math.BigDecimal
import java.util.regex.Pattern

/**
 * Data validation utilities for all entities
 * Ensures data integrity and compliance with backend requirements
 */
object DataValidation {
    
    // Email validation pattern
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    // Phone validation pattern (international format)
    private val PHONE_PATTERN = Pattern.compile(
        "^\\+[1-9]\\d{1,14}$"
    )
    
    // Solana address validation pattern
    private val SOLANA_ADDRESS_PATTERN = Pattern.compile(
        "^[1-9A-HJ-NP-Za-km-z]{32,44}$"
    )
    
    // Card number validation pattern (basic)
    private val CARD_NUMBER_PATTERN = Pattern.compile(
        "^[0-9]{4}\\s?[0-9]{4}\\s?[0-9]{4}\\s?[0-9]{4}$"
    )

    /**
     * Validates User entity
     */
    fun validateUser(user: User): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (user.firstName.isBlank()) errors.add("First name is required")
        if (user.lastName.isBlank()) errors.add("Last name is required")
        if (user.email.isBlank()) errors.add("Email is required")
        if (user.authProviderId.isBlank()) errors.add("Auth provider ID is required")
        
        // Field length validation
        if (user.firstName.length > 50) errors.add("First name must be 50 characters or less")
        if (user.lastName.length > 50) errors.add("Last name must be 50 characters or less")
        if (user.firstName.length < 1) errors.add("First name must be at least 1 character")
        if (user.lastName.length < 1) errors.add("Last name must be at least 1 character")
        
        // Email validation
        if (user.email.isNotBlank() && !isValidEmail(user.email)) {
            errors.add("Invalid email format")
        }
        
        // Phone validation (optional)
        if (user.phone?.isNotBlank() == true && !isValidPhone(user.phone)) {
            errors.add("Invalid phone format. Use international format (+1234567890)")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates Contact entity
     */
    fun validateContact(contact: Contact): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (contact.ownerId.isBlank()) errors.add("Owner ID is required")
        if (contact.displayName.isBlank()) errors.add("Display name is required")
        
        // Field length validation
        if (contact.displayName.length > 100) errors.add("Display name must be 100 characters or less")
        if (contact.displayName.length < 1) errors.add("Display name must be at least 1 character")
        
        // At least one contact identifier must be provided
        val hasContactIdentifier = contact.contactUserId?.isNotBlank() == true ||
                contact.email?.isNotBlank() == true ||
                contact.phone?.isNotBlank() == true
        
        if (!hasContactIdentifier) {
            errors.add("Either contact user ID, email, or phone must be provided")
        }
        
        // Email validation (optional)
        if (contact.email?.isNotBlank() == true && !isValidEmail(contact.email)) {
            errors.add("Invalid email format")
        }
        
        // Phone validation (optional)
        if (contact.phone?.isNotBlank() == true && !isValidPhone(contact.phone)) {
            errors.add("Invalid phone format. Use international format (+1234567890)")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates Wallet entity
     */
    fun validateWallet(wallet: Wallet): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (wallet.userId.isBlank()) errors.add("User ID is required")
        if (wallet.address.isBlank()) errors.add("Wallet address is required")
        if (wallet.publicKey.isBlank()) errors.add("Public key is required")
        
        // Solana address validation
        if (wallet.address.isNotBlank() && !isValidSolanaAddress(wallet.address)) {
            errors.add("Invalid Solana wallet address format")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates Transaction entity
     */
    fun validateTransaction(transaction: Transaction): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (transaction.senderId.isBlank()) errors.add("Sender ID is required")
        if (transaction.recipientId.isBlank()) errors.add("Recipient ID is required")
        if (transaction.senderWalletId.isBlank()) errors.add("Sender wallet ID is required")
        if (transaction.recipientWalletId.isBlank()) errors.add("Recipient wallet ID is required")
        
        // Amount validation
        if (transaction.amount <= BigDecimal.ZERO) {
            errors.add("Amount must be greater than zero")
        }
        
        // Fee validation
        if (transaction.fee < BigDecimal.ZERO) {
            errors.add("Fee cannot be negative")
        }
        
        // Sender and recipient must be different
        if (transaction.senderId == transaction.recipientId) {
            errors.add("Sender and recipient must be different users")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates VISACard entity
     */
    fun validateVISACard(card: VISACard): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (card.userId.isBlank()) errors.add("User ID is required")
        if (card.cardNumber.isBlank()) errors.add("Card number is required")
        
        // Card number validation
        if (card.cardNumber.isNotBlank() && !isValidCardNumber(card.cardNumber)) {
            errors.add("Invalid card number format")
        }
        
        // Amount validation
        if (card.dailyLimit <= BigDecimal.ZERO) {
            errors.add("Daily limit must be greater than zero")
        }
        if (card.monthlyLimit <= BigDecimal.ZERO) {
            errors.add("Monthly limit must be greater than zero")
        }
        if (card.balance < BigDecimal.ZERO) {
            errors.add("Balance cannot be negative")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates OnOffRamp entity
     */
    fun validateOnOffRamp(ramp: OnOffRamp): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (ramp.userId.isBlank()) errors.add("User ID is required")
        if (ramp.walletId.isBlank()) errors.add("Wallet ID is required")
        if (ramp.provider.isBlank()) errors.add("Provider is required")
        
        // Amount validation
        if (ramp.amount <= BigDecimal.ZERO) {
            errors.add("Amount must be greater than zero")
        }
        if (ramp.fiatAmount <= BigDecimal.ZERO) {
            errors.add("Fiat amount must be greater than zero")
        }
        if (ramp.exchangeRate <= BigDecimal.ZERO) {
            errors.add("Exchange rate must be greater than zero")
        }
        if (ramp.fee < BigDecimal.ZERO) {
            errors.add("Fee cannot be negative")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates Inquiry entity
     */
    fun validateInquiry(inquiry: Inquiry): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Required fields
        if (inquiry.name.isBlank()) errors.add("Name is required")
        if (inquiry.email.isBlank()) errors.add("Email is required")
        
        // Email validation
        if (inquiry.email.isNotBlank() && !isValidEmail(inquiry.email)) {
            errors.add("Invalid email format")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    // Helper validation methods
    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return PHONE_PATTERN.matcher(phone).matches()
    }

    private fun isValidSolanaAddress(address: String): Boolean {
        return SOLANA_ADDRESS_PATTERN.matcher(address).matches()
    }

    private fun isValidCardNumber(cardNumber: String): Boolean {
        return CARD_NUMBER_PATTERN.matcher(cardNumber).matches()
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
    val isInvalid: Boolean get() = this is Invalid
}
