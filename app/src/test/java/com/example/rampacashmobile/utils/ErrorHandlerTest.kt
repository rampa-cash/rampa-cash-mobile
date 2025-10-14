package com.example.rampacashmobile.utils

import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.common.Result
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class ErrorHandlerTest {

    @Test
    fun `safeCall with successful operation should return success`() = runBlocking {
        // Given
        val expectedResult = "test result"
        val operation = suspend { expectedResult }

        // When
        val result = ErrorHandler.safeCall(operation)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedResult, result.data)
    }

    @Test
    fun `safeCall with exception should return failure`() = runBlocking {
        // Given
        val exception = RuntimeException("Test exception")
        val operation = suspend { throw exception }

        // When
        val result = ErrorHandler.safeCall(operation)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.UnknownError)
    }

    @Test
    fun `safeCallSync with successful operation should return success`() {
        // Given
        val expectedResult = "test result"
        val operation = { expectedResult }

        // When
        val result = ErrorHandler.safeCallSync(operation)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedResult, result.data)
    }

    @Test
    fun `safeCallSync with exception should return failure`() {
        // Given
        val exception = RuntimeException("Test exception")
        val operation = { throw exception }

        // When
        val result = ErrorHandler.safeCallSync(operation)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.UnknownError)
    }

    @Test
    fun `mapExceptionToDomainError with IOException should return NetworkError`() {
        // Given
        val exception = IOException("Network error")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection.", result.message)
    }

    @Test
    fun `mapExceptionToDomainError with SocketTimeoutException should return NetworkError`() {
        // Given
        val exception = SocketTimeoutException("Timeout")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection.", result.message)
    }

    @Test
    fun `mapExceptionToDomainError with UnknownHostException should return NetworkError`() {
        // Given
        val exception = UnknownHostException("Unknown host")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection.", result.message)
    }

    @Test
    fun `mapExceptionToDomainError with TimeoutException should return NetworkError`() {
        // Given
        val exception = TimeoutException("Timeout")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection.", result.message)
    }

    @Test
    fun `mapExceptionToDomainError with IllegalArgumentException should return ValidationError`() {
        // Given
        val exception = IllegalArgumentException("Invalid argument")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.ValidationError)
        assertEquals("Invalid input: Invalid argument", result.message)
    }

    @Test
    fun `mapExceptionToDomainError with unknown exception should return UnknownError`() {
        // Given
        val exception = RuntimeException("Unknown error")

        // When
        val result = ErrorHandler.mapExceptionToDomainError(exception)

        // Then
        assertTrue(result is DomainError.UnknownError)
        assertEquals("An unexpected error occurred. Please try again: Unknown error", result.message)
    }

    @Test
    fun `mapNetworkException with IOException should return NetworkError`() {
        // Given
        val exception = IOException("Network error")

        // When
        val result = ErrorHandler.mapNetworkException(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection.", result.message)
    }

    @Test
    fun `mapNetworkException with other exception should return NetworkError`() {
        // Given
        val exception = RuntimeException("Other error")

        // When
        val result = ErrorHandler.mapNetworkException(exception)

        // Then
        assertTrue(result is DomainError.NetworkError)
        assertEquals("Network error. Please check your internet connection: Other error", result.message)
    }

    @Test
    fun `mapAuthException should return AuthenticationError`() {
        // Given
        val exception = RuntimeException("Auth error")

        // When
        val result = ErrorHandler.mapAuthException(exception)

        // Then
        assertTrue(result is DomainError.AuthenticationError)
        assertEquals("Authentication failed: Auth error", result.message)
    }

    @Test
    fun `mapValidationException should return ValidationError`() {
        // Given
        val exception = RuntimeException("Validation error")

        // When
        val result = ErrorHandler.mapValidationException(exception)

        // Then
        assertTrue(result is DomainError.ValidationError)
        assertEquals("Validation failed: Validation error", result.message)
    }

    @Test
    fun `mapBusinessRuleException should return BusinessRuleViolation`() {
        // Given
        val exception = RuntimeException("Business rule error")

        // When
        val result = ErrorHandler.mapBusinessRuleException(exception)

        // Then
        assertTrue(result is DomainError.BusinessRuleViolation)
        assertEquals("Business rule violated: Business rule error", result.message)
    }

    @Test
    fun `getUserFriendlyMessage with NetworkError should return network message`() {
        // Given
        val error = DomainError.NetworkError("Network error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("Network error. Please check your internet connection.", result)
    }

    @Test
    fun `getUserFriendlyMessage with AuthenticationError should return auth message`() {
        // Given
        val error = DomainError.AuthenticationError("Auth error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("Authentication failed. Please log in again.", result)
    }

    @Test
    fun `getUserFriendlyMessage with AuthorizationError should return authorization message`() {
        // Given
        val error = DomainError.AuthorizationError("Auth error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("You don't have permission to perform this action.", result)
    }

    @Test
    fun `getUserFriendlyMessage with ValidationError should return validation message`() {
        // Given
        val error = DomainError.ValidationError("Validation error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("Validation error", result)
    }

    @Test
    fun `getUserFriendlyMessage with BusinessRuleViolation should return business rule message`() {
        // Given
        val error = DomainError.BusinessRuleViolation("Business rule error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("Business rule error", result)
    }

    @Test
    fun `getUserFriendlyMessage with ExternalServiceError should return external service message`() {
        // Given
        val error = DomainError.ExternalServiceError("External service error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("An external service is currently unavailable. Please try again later.", result)
    }

    @Test
    fun `getUserFriendlyMessage with UnknownError should return unknown error message`() {
        // Given
        val error = DomainError.UnknownError("Unknown error")

        // When
        val result = ErrorHandler.getUserFriendlyMessage(error)

        // Then
        assertEquals("An unexpected error occurred. Please try again.", result)
    }
}
