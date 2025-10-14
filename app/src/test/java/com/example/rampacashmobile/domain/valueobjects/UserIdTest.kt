package com.example.rampacashmobile.domain.valueobjects

import org.junit.Test
import org.junit.Assert.*

class UserIdTest {

    @Test
    fun `UserId creation with valid ID should succeed`() {
        // Given
        val idString = "user_123456789"

        // When
        val userId = UserId.of(idString)

        // Then
        assertEquals(idString, userId.value)
    }

    @Test
    fun `UserId creation with empty string should throw exception`() {
        // Given
        val idString = ""

        // When & Then
        try {
            UserId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID cannot be empty", e.message)
        }
    }

    @Test
    fun `UserId creation with blank string should throw exception`() {
        // Given
        val idString = "   "

        // When & Then
        try {
            UserId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID cannot be empty", e.message)
        }
    }

    @Test
    fun `UserId creation with too short ID should throw exception`() {
        // Given
        val idString = "a"

        // When & Then
        try {
            UserId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID must be at least 3 characters long", e.message)
        }
    }

    @Test
    fun `UserId creation with too long ID should throw exception`() {
        // Given
        val idString = "a".repeat(256)

        // When & Then
        try {
            UserId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID must be at most 255 characters long", e.message)
        }
    }

    @Test
    fun `UserId generate should create valid ID`() {
        // When
        val userId = UserId.generate()

        // Then
        assertNotNull(userId)
        assertTrue(userId.value.length >= 3)
        assertTrue(userId.value.length <= 255)
        assertTrue(userId.value.startsWith("user_"))
    }

    @Test
    fun `UserId generate should create unique IDs`() {
        // When
        val userId1 = UserId.generate()
        val userId2 = UserId.generate()

        // Then
        assertNotEquals(userId1, userId2)
        assertNotEquals(userId1.value, userId2.value)
    }

    @Test
    fun `UserId equals should work correctly`() {
        // Given
        val idString = "user_123456789"
        val userId1 = UserId.of(idString)
        val userId2 = UserId.of(idString)

        // When & Then
        assertEquals(userId1, userId2)
    }

    @Test
    fun `UserId hashCode should work correctly`() {
        // Given
        val idString = "user_123456789"
        val userId1 = UserId.of(idString)
        val userId2 = UserId.of(idString)

        // When & Then
        assertEquals(userId1.hashCode(), userId2.hashCode())
    }

    @Test
    fun `UserId toString should return the ID value`() {
        // Given
        val idString = "user_123456789"
        val userId = UserId.of(idString)

        // When
        val result = userId.toString()

        // Then
        assertEquals(idString, result)
    }

    @Test
    fun `UserId with valid characters should succeed`() {
        // Given
        val idString = "user_123-abc_XYZ"

        // When
        val userId = UserId.of(idString)

        // Then
        assertEquals(idString, userId.value)
    }

    @Test
    fun `UserId with special characters should throw exception`() {
        // Given
        val idString = "user@123#abc"

        // When & Then
        try {
            UserId.of(idString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("User ID contains invalid characters", e.message)
        }
    }
}
