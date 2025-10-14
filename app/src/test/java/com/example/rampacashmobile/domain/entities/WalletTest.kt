package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import org.junit.Test
import org.junit.Assert.*

class WalletTest {

    @Test
    fun `Wallet creation with valid data should succeed`() {
        // Given
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "My Wallet"

        // When
        val result = Wallet.create(userId, address, label)

        // Then
        assertTrue(result is Result.Success)
        val wallet = result.data
        assertEquals(userId, wallet.userId)
        assertEquals(address, wallet.address)
        assertEquals("My Wallet", wallet.label)
        assertTrue(wallet.isActive)
    }

    @Test
    fun `Wallet creation with empty label should use default`() {
        // Given
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = ""

        // When
        val result = Wallet.create(userId, address, label)

        // Then
        assertTrue(result is Result.Success)
        val wallet = result.data
        assertEquals("Wallet", wallet.label)
    }

    @Test
    fun `Wallet creation with blank label should use default`() {
        // Given
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "   "

        // When
        val result = Wallet.create(userId, address, label)

        // Then
        assertTrue(result is Result.Success)
        val wallet = result.data
        assertEquals("Wallet", wallet.label)
    }

    @Test
    fun `Wallet restore with valid data should succeed`() {
        // Given
        val id = "wallet_123"
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "My Wallet"
        val isActive = true
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        // When
        val result = Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt)

        // Then
        assertTrue(result is Result.Success)
        val wallet = result.data
        assertEquals(id, wallet.id)
        assertEquals(userId, wallet.userId)
        assertEquals(address, wallet.address)
        assertEquals(label, wallet.label)
        assertEquals(isActive, wallet.isActive)
        assertEquals(createdAt, wallet.createdAt)
        assertEquals(updatedAt, wallet.updatedAt)
    }

    @Test
    fun `Wallet updateLabel when active should succeed`() {
        // Given
        val wallet = createTestWallet(true)
        val newLabel = "Updated Wallet"

        // When
        val result = wallet.updateLabel(newLabel)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Updated Wallet", wallet.label)
    }

    @Test
    fun `Wallet updateLabel when inactive should fail`() {
        // Given
        val wallet = createTestWallet(false)
        val newLabel = "Updated Wallet"

        // When
        val result = wallet.updateLabel(newLabel)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `Wallet updateLabel with empty string should use default`() {
        // Given
        val wallet = createTestWallet(true)
        val newLabel = ""

        // When
        val result = wallet.updateLabel(newLabel)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Wallet", wallet.label)
    }

    @Test
    fun `Wallet activate when inactive should succeed`() {
        // Given
        val wallet = createTestWallet(false)

        // When
        val result = wallet.activate()

        // Then
        assertTrue(result is Result.Success)
        assertTrue(wallet.isActive)
    }

    @Test
    fun `Wallet activate when already active should fail`() {
        // Given
        val wallet = createTestWallet(true)

        // When
        val result = wallet.activate()

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `Wallet deactivate when active should succeed`() {
        // Given
        val wallet = createTestWallet(true)

        // When
        val result = wallet.deactivate()

        // Then
        assertTrue(result is Result.Success)
        assertFalse(wallet.isActive)
    }

    @Test
    fun `Wallet deactivate when already inactive should fail`() {
        // Given
        val wallet = createTestWallet(false)

        // When
        val result = wallet.deactivate()

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `Wallet isActiveStatus should return correct status`() {
        // Given
        val activeWallet = createTestWallet(true)
        val inactiveWallet = createTestWallet(false)

        // When & Then
        assertTrue(activeWallet.isActiveStatus())
        assertFalse(inactiveWallet.isActiveStatus())
    }

    @Test
    fun `Wallet isInactive should return correct status`() {
        // Given
        val activeWallet = createTestWallet(true)
        val inactiveWallet = createTestWallet(false)

        // When & Then
        assertFalse(activeWallet.isInactive())
        assertTrue(inactiveWallet.isInactive())
    }

    @Test
    fun `Wallet equals should work correctly`() {
        // Given
        val id = "wallet_123"
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "My Wallet"
        val isActive = true
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        val wallet1 = Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt).data
        val wallet2 = Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt).data

        // When & Then
        assertEquals(wallet1, wallet2)
    }

    @Test
    fun `Wallet hashCode should work correctly`() {
        // Given
        val id = "wallet_123"
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "My Wallet"
        val isActive = true
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        val wallet1 = Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt).data
        val wallet2 = Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt).data

        // When & Then
        assertEquals(wallet1.hashCode(), wallet2.hashCode())
    }

    @Test
    fun `Wallet toString should include ID and address`() {
        // Given
        val wallet = createTestWallet(true)

        // When
        val result = wallet.toString()

        // Then
        assertTrue(result.contains("Wallet"))
        assertTrue(result.contains(wallet.id))
        assertTrue(result.contains(wallet.address.value))
    }

    private fun createTestWallet(isActive: Boolean): Wallet {
        val id = "wallet_123"
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "My Wallet"
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        return Wallet.restore(id, userId, address, label, isActive, createdAt, updatedAt).data
    }
}
