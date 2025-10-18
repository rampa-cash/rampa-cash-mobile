package com.example.rampacashmobile.domain.common

/**
 * Domain error types for the application
 * 
 * This sealed class represents all possible domain errors that can occur
 * in the application, providing a structured way to handle errors
 */
sealed class DomainError {
    abstract val message: String
    abstract val cause: Throwable?

    /**
     * Validation error - when input data is invalid
     */
    data class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Business rule violation error
     */
    data class BusinessRuleViolation(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Not found error - when a resource is not found
     */
    data class NotFound(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Already exists error - when trying to create something that already exists
     */
    data class AlreadyExists(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Insufficient funds error - when there are not enough funds for a transaction
     */
    data class InsufficientFunds(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Invalid operation error - when an operation is not allowed
     */
    data class InvalidOperation(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Network error - when there's a network-related issue
     */
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Authentication error - when there's an authentication issue
     */
    data class AuthenticationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Authorization error - when there's an authorization issue
     */
    data class AuthorizationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * External service error - when an external service fails
     */
    data class ExternalServiceError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Storage error - when there's a data storage issue
     */
    data class StorageError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Persistence error - when there's a data persistence issue
     */
    data class PersistenceError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Rate limit error - when API rate limit is exceeded
     */
    data class RateLimitError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Server error - when there's a server-side error
     */
    data class ServerError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Not found error - when a resource is not found (alias for NotFound)
     */
    data class NotFoundError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Phone login needs onboarding error - when phone login requires profile completion
     */
    data class PhoneLoginNeedsOnboarding(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    /**
     * Unknown error - for unexpected errors
     */
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : DomainError()

    override fun toString(): String = "${this::class.simpleName}: $message"
}
