package com.example.rampacashmobile.utils

import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.constants.AppConstants
import timber.log.Timber

/**
 * Centralized error handling utility for the application
 * 
 * This utility provides consistent error handling patterns using our domain
 * Result<T> type and DomainError hierarchy
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Wraps a suspend function in a Result type with proper error handling
     */
    suspend fun <T> safeCall(
        operation: suspend () -> T,
        errorMessage: String = "Operation failed"
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Timber.e(e, errorMessage)
            Result.failure(mapExceptionToDomainError(e, errorMessage))
        }
    }
    
    /**
     * Wraps a regular function in a Result type with proper error handling
     */
    fun <T> safeCallSync(
        operation: () -> T,
        errorMessage: String = "Operation failed"
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: Exception) {
            Timber.e(e, errorMessage)
            Result.failure(mapExceptionToDomainError(e, errorMessage))
        }
    }
    
    /**
     * Maps common exceptions to appropriate DomainError types
     */
    fun mapExceptionToDomainError(exception: Exception, context: String = ""): DomainError {
        return when (exception) {
            is IllegalArgumentException -> DomainError.ValidationError(
                message = "$context: ${exception.message}",
                cause = exception
            )
            is IllegalStateException -> DomainError.BusinessRuleViolation(
                message = "$context: ${exception.message}",
                cause = exception
            )
            is SecurityException -> DomainError.AuthorizationError(
                message = "$context: ${exception.message}",
                cause = exception
            )
            is UnsupportedOperationException -> DomainError.InvalidOperation(
                message = "$context: ${exception.message}",
                cause = exception
            )
            else -> DomainError.UnknownError(
                message = "$context: ${exception.message ?: "Unknown error occurred"}",
                cause = exception
            )
        }
    }
    
    /**
     * Maps network-related exceptions to DomainError
     */
    fun mapNetworkException(exception: Exception, context: String = ""): DomainError {
        return DomainError.NetworkError(
            message = "$context: ${exception.message ?: "Network error occurred"}",
            cause = exception
        )
    }
    
    /**
     * Maps authentication-related exceptions to DomainError
     */
    fun mapAuthException(exception: Exception, context: String = ""): DomainError {
        return DomainError.AuthenticationError(
            message = "$context: ${exception.message ?: "Authentication error occurred"}",
            cause = exception
        )
    }
    
    /**
     * Maps validation-related exceptions to DomainError
     */
    fun mapValidationException(exception: Exception, context: String = ""): DomainError {
        return DomainError.ValidationError(
            message = "$context: ${exception.message ?: "Validation error occurred"}",
            cause = exception
        )
    }
    
    /**
     * Maps business rule violations to DomainError
     */
    fun mapBusinessRuleException(exception: Exception, context: String = ""): DomainError {
        return DomainError.BusinessRuleViolation(
            message = "$context: ${exception.message ?: "Business rule violation"}",
            cause = exception
        )
    }
    
    /**
     * Logs an error with appropriate level based on error type
     */
    fun logError(error: DomainError, tag: String = TAG) {
        when (error) {
            is DomainError.ValidationError -> Timber.tag(tag).w(error.cause, "Validation Error: ${error.message}")
            is DomainError.BusinessRuleViolation -> Timber.tag(tag).w(error.cause, "Business Rule Violation: ${error.message}")
            is DomainError.NetworkError -> Timber.tag(tag).e(error.cause, "Network Error: ${error.message}")
            is DomainError.AuthenticationError -> Timber.tag(tag).e(error.cause, "Authentication Error: ${error.message}")
            is DomainError.AuthorizationError -> Timber.tag(tag).e(error.cause, "Authorization Error: ${error.message}")
            is DomainError.ExternalServiceError -> Timber.tag(tag).e(error.cause, "External Service Error: ${error.message}")
            is DomainError.UnknownError -> Timber.tag(tag).e(error.cause, "Unknown Error: ${error.message}")
            else -> Timber.tag(tag).w(error.cause, "Domain Error: ${error.message}")
        }
    }
    
    /**
     * Gets user-friendly error message for UI display
     */
    fun getUserFriendlyMessage(error: DomainError): String {
        return when (error) {
            is DomainError.ValidationError -> AppConstants.ERROR_INVALID_ADDRESS
            is DomainError.BusinessRuleViolation -> AppConstants.ERROR_INSUFFICIENT_FUNDS
            is DomainError.NetworkError -> AppConstants.ERROR_NETWORK_FAILURE
            is DomainError.AuthenticationError -> "Authentication failed. Please try again."
            is DomainError.AuthorizationError -> "You don't have permission to perform this action."
            is DomainError.ExternalServiceError -> "Service temporarily unavailable. Please try again later."
            is DomainError.NotFound -> "Requested resource not found."
            is DomainError.AlreadyExists -> "This item already exists."
            is DomainError.InsufficientFunds -> AppConstants.ERROR_INSUFFICIENT_FUNDS
            is DomainError.InvalidOperation -> "This operation is not allowed."
            is DomainError.UnknownError -> "An unexpected error occurred. Please try again."
            else -> error.message
        }
    }
}
