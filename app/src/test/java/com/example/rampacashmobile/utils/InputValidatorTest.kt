package com.example.rampacashmobile.utils

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Currency

class InputValidatorTest {

    @Test
    fun `validateEmail with valid email should succeed`() {
        // Given
        val email = "test@example.com"

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(email, result.data.value)
    }

    @Test
    fun `validateEmail with invalid email should fail`() {
        // Given
        val email = "invalid-email"

        // When
        val result = InputValidator.validateEmail(email)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateWalletAddress with valid address should succeed`() {
        // Given
        val address = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"

        // When
        val result = InputValidator.validateWalletAddress(address)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(address, result.data.value)
    }

    @Test
    fun `validateWalletAddress with invalid address should fail`() {
        // Given
        val address = "invalid-address"

        // When
        val result = InputValidator.validateWalletAddress(address)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateMoneyAmount with valid amount should succeed`() {
        // Given
        val amount = "100.50"
        val currency = "USD"

        // When
        val result = InputValidator.validateMoneyAmount(amount, currency)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(BigDecimal("100.50"), result.data.amount)
        assertEquals(Currency.USD, result.data.currency)
    }

    @Test
    fun `validateMoneyAmount with invalid amount should fail`() {
        // Given
        val amount = "invalid-amount"
        val currency = "USD"

        // When
        val result = InputValidator.validateMoneyAmount(amount, currency)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateMoneyAmount with unsupported currency should fail`() {
        // Given
        val amount = "100.50"
        val currency = "BTC"

        // When
        val result = InputValidator.validateMoneyAmount(amount, currency)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateMoneyAmount with amount below minimum should fail`() {
        // Given
        val amount = "0.005"
        val currency = "USD"

        // When
        val result = InputValidator.validateMoneyAmount(amount, currency)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateTransactionDescription with valid description should succeed`() {
        // Given
        val description = "Test transaction"

        // When
        val result = InputValidator.validateTransactionDescription(description)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Test transaction", result.data)
    }

    @Test
    fun `validateTransactionDescription with empty description should return default`() {
        // Given
        val description = ""

        // When
        val result = InputValidator.validateTransactionDescription(description)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("P2P Transfer", result.data)
    }

    @Test
    fun `validateTransactionDescription with too long description should fail`() {
        // Given
        val description = "a".repeat(251)

        // When
        val result = InputValidator.validateTransactionDescription(description)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateContactName with valid name should succeed`() {
        // Given
        val name = "John Doe"

        // When
        val result = InputValidator.validateContactName(name)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("John Doe", result.data)
    }

    @Test
    fun `validateContactName with empty name should fail`() {
        // Given
        val name = ""

        // When
        val result = InputValidator.validateContactName(name)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateContactName with too short name should fail`() {
        // Given
        val name = "A"

        // When
        val result = InputValidator.validateContactName(name)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateContactName with too long name should fail`() {
        // Given
        val name = "a".repeat(51)

        // When
        val result = InputValidator.validateContactName(name)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validatePhoneNumber with valid phone should succeed`() {
        // Given
        val phone = "+1234567890"

        // When
        val result = InputValidator.validatePhoneNumber(phone)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("+1234567890", result.data)
    }

    @Test
    fun `validatePhoneNumber with invalid phone should fail`() {
        // Given
        val phone = "invalid-phone"

        // When
        val result = InputValidator.validatePhoneNumber(phone)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validatePhoneNumber with empty phone should fail`() {
        // Given
        val phone = ""

        // When
        val result = InputValidator.validatePhoneNumber(phone)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validatePassword with valid password should succeed`() {
        // Given
        val password = "password123"

        // When
        val result = InputValidator.validatePassword(password)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("password123", result.data)
    }

    @Test
    fun `validatePassword with too short password should fail`() {
        // Given
        val password = "123"

        // When
        val result = InputValidator.validatePassword(password)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validatePassword without letter should fail`() {
        // Given
        val password = "12345678"

        // When
        val result = InputValidator.validatePassword(password)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validatePassword without digit should fail`() {
        // Given
        val password = "password"

        // When
        val result = InputValidator.validatePassword(password)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateUserId with valid ID should succeed`() {
        // Given
        val userId = "user_123"

        // When
        val result = InputValidator.validateUserId(userId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(userId, result.data.value)
    }

    @Test
    fun `validateUserId with invalid ID should fail`() {
        // Given
        val userId = ""

        // When
        val result = InputValidator.validateUserId(userId)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateTransactionId with valid ID should succeed`() {
        // Given
        val transactionId = "tx_123"

        // When
        val result = InputValidator.validateTransactionId(transactionId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(transactionId, result.data.value)
    }

    @Test
    fun `validateTransactionId with invalid ID should fail`() {
        // Given
        val transactionId = ""

        // When
        val result = InputValidator.validateTransactionId(transactionId)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateCurrency with valid currency should succeed`() {
        // Given
        val currency = "USD"

        // When
        val result = InputValidator.validateCurrency(currency)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(Currency.USD, result.data)
    }

    @Test
    fun `validateCurrency with invalid currency should fail`() {
        // Given
        val currency = "BTC"

        // When
        val result = InputValidator.validateCurrency(currency)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateNotEmpty with valid value should succeed`() {
        // Given
        val value = "test"
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNotEmpty(value, fieldName)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("test", result.data)
    }

    @Test
    fun `validateNotEmpty with empty value should fail`() {
        // Given
        val value = ""
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNotEmpty(value, fieldName)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateNumericRange with valid value should succeed`() {
        // Given
        val value = "5.5"
        val min = 0.0
        val max = 10.0
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNumericRange(value, min, max, fieldName)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(5.5, result.data, 0.001)
    }

    @Test
    fun `validateNumericRange with value below min should fail`() {
        // Given
        val value = "-1.0"
        val min = 0.0
        val max = 10.0
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNumericRange(value, min, max, fieldName)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateNumericRange with value above max should fail`() {
        // Given
        val value = "15.0"
        val min = 0.0
        val max = 10.0
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNumericRange(value, min, max, fieldName)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }

    @Test
    fun `validateNumericRange with invalid format should fail`() {
        // Given
        val value = "invalid"
        val min = 0.0
        val max = 10.0
        val fieldName = "Test Field"

        // When
        val result = InputValidator.validateNumericRange(value, min, max, fieldName)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.ValidationError)
    }
}
