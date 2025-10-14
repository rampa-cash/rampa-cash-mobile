package com.example.rampacashmobile.domain.valueobjects

import org.junit.Test
import org.junit.Assert.*

class TransactionIdTest {

    @Test
    fun `TransactionId creation with valid ID should succeed`() {
        // Given
        val idString = "tx_123456789"

        // When
        val transactionId = TransactionId.of(idString)

        // Then
        assertEquals(idString, transactionId.value)
    }

    @Test
    fun `TransactionId creation with empty string should throw exception`() {
        // Given
        val idString = ""

        // When & Then
        try {
            TransactionId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Transaction ID cannot be empty", e.message)
        }
    }

    @Test
    fun `TransactionId creation with blank string should throw exception`() {
        // Given
        val idString = "   "

        // When & Then
        try {
            TransactionId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Transaction ID cannot be empty", e.message)
        }
    }

    @Test
    fun `TransactionId creation with too short ID should throw exception`() {
        // Given
        val idString = "a"

        // When & Then
        try {
            TransactionId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Transaction ID must be at least 3 characters long", e.message)
        }
    }

    @Test
    fun `TransactionId creation with too long ID should throw exception`() {
        // Given
        val idString = "a".repeat(256)

        // When & Then
        try {
            TransactionId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Transaction ID must be at most 255 characters long", e.message)
        }
    }

    @Test
    fun `TransactionId generate should create valid ID`() {
        // When
        val transactionId = TransactionId.generate()

        // Then
        assertNotNull(transactionId)
        assertTrue(transactionId.value.length >= 3)
        assertTrue(transactionId.value.length <= 255)
        assertTrue(transactionId.value.startsWith("tx_"))
    }

    @Test
    fun `TransactionId generate should create unique IDs`() {
        // When
        val transactionId1 = TransactionId.generate()
        val transactionId2 = TransactionId.generate()

        // Then
        assertNotEquals(transactionId1, transactionId2)
        assertNotEquals(transactionId1.value, transactionId2.value)
    }

    @Test
    fun `TransactionId equals should work correctly`() {
        // Given
        val idString = "tx_123456789"
        val transactionId1 = TransactionId.of(idString)
        val transactionId2 = TransactionId.of(idString)

        // When & Then
        assertEquals(transactionId1, transactionId2)
    }

    @Test
    fun `TransactionId hashCode should work correctly`() {
        // Given
        val idString = "tx_123456789"
        val transactionId1 = TransactionId.of(idString)
        val transactionId2 = TransactionId.of(idString)

        // When & Then
        assertEquals(transactionId1.hashCode(), transactionId2.hashCode())
    }

    @Test
    fun `TransactionId toString should return the ID value`() {
        // Given
        val idString = "tx_123456789"
        val transactionId = TransactionId.of(idString)

        // When
        val result = transactionId.toString()

        // Then
        assertEquals(idString, result)
    }

    @Test
    fun `TransactionId with valid characters should succeed`() {
        // Given
        val idString = "tx_123-abc_XYZ"

        // When
        val transactionId = TransactionId.of(idString)

        // Then
        assertEquals(idString, transactionId.value)
    }

    @Test
    fun `TransactionId with special characters should throw exception`() {
        // Given
        val idString = "tx@123#abc"

        // When & Then
        try {
            TransactionId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Transaction ID contains invalid characters", e.message)
        }
    }
}
