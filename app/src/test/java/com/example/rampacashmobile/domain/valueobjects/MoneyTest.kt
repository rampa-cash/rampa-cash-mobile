package com.example.rampacashmobile.domain.valueobjects

import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `Money creation with valid amount should succeed`() {
        // Given
        val amount = BigDecimal("100.50")
        val currency = Currency.USD

        // When
        val money = Money(amount, currency)

        // Then
        assertEquals(amount, money.amount)
        assertEquals(currency, money.currency)
    }

    @Test
    fun `Money creation with zero amount should succeed`() {
        // Given
        val amount = BigDecimal.ZERO
        val currency = Currency.USD

        // When
        val money = Money(amount, currency)

        // Then
        assertEquals(amount, money.amount)
        assertEquals(currency, money.currency)
    }

    @Test
    fun `Money addition with same currency should succeed`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.USD)

        // When
        val result = money1 + money2

        // Then
        assertEquals(BigDecimal("150.75"), result.amount)
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `Money addition with different currencies should throw exception`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.EUR)

        // When & Then
        try {
            money1 + money2
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot add different currencies", e.message)
        }
    }

    @Test
    fun `Money subtraction with same currency should succeed`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.USD)

        // When
        val result = money1 - money2

        // Then
        assertEquals(BigDecimal("50.25"), result.amount)
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `Money subtraction with different currencies should throw exception`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.EUR)

        // When & Then
        try {
            money1 - money2
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot subtract different currencies", e.message)
        }
    }

    @Test
    fun `Money multiplication should succeed`() {
        // Given
        val money = Money(BigDecimal("100.50"), Currency.USD)
        val multiplier = BigDecimal("2.5")

        // When
        val result = money * multiplier

        // Then
        assertEquals(BigDecimal("251.25"), result.amount)
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `Money comparison with same currency should work correctly`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.USD)
        val money3 = Money(BigDecimal("100.50"), Currency.USD)

        // When & Then
        assertTrue(money1.isGreaterThan(money2))
        assertFalse(money2.isGreaterThan(money1))
        assertTrue(money1.isGreaterThanOrEqual(money3))
        assertTrue(money1.isLessThan(money2).not())
    }

    @Test
    fun `Money comparison with different currencies should throw exception`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("50.25"), Currency.EUR)

        // When & Then
        try {
            money1.isGreaterThan(money2)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Cannot compare different currencies", e.message)
        }
    }

    @Test
    fun `Money isZero should return true for zero amount`() {
        // Given
        val money = Money(BigDecimal.ZERO, Currency.USD)

        // When & Then
        assertTrue(money.isZero())
    }

    @Test
    fun `Money isZero should return false for non-zero amount`() {
        // Given
        val money = Money(BigDecimal("100.50"), Currency.USD)

        // When & Then
        assertFalse(money.isZero())
    }

    @Test
    fun `Money toString should format correctly`() {
        // Given
        val money = Money(BigDecimal("100.50"), Currency.USD)

        // When
        val result = money.toString()

        // Then
        assertEquals("100.50 USD", result)
    }

    @Test
    fun `Money equals should work correctly`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("100.50"), Currency.USD)
        val money3 = Money(BigDecimal("100.51"), Currency.USD)

        // When & Then
        assertEquals(money1, money2)
        assertNotEquals(money1, money3)
    }

    @Test
    fun `Money hashCode should work correctly`() {
        // Given
        val money1 = Money(BigDecimal("100.50"), Currency.USD)
        val money2 = Money(BigDecimal("100.50"), Currency.USD)

        // When & Then
        assertEquals(money1.hashCode(), money2.hashCode())
    }
}
