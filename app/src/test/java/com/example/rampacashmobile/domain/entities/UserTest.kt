package com.example.rampacashmobile.domain.entities

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import org.junit.Test
import org.junit.Assert.*

class UserTest {

    @Test
    fun `User creation with valid data should succeed`() {
        // Given
        val userId = UserId.of("user_123")
        val email = Email.of("test@example.com")
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val status = UserStatus.PENDING

        // When
        val result = User.create(userId, email, walletAddress, status)

        // Then
        assertTrue(result is Result.Success)
        val user = result.data
        assertEquals(userId, user.id)
        assertEquals(email, user.email)
        assertEquals(walletAddress, user.walletAddress)
        assertEquals(status, user.status)
    }

    @Test
    fun `User restore with valid data should succeed`() {
        // Given
        val userId = UserId.of("user_123")
        val email = Email.of("test@example.com")
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val status = UserStatus.ACTIVE
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        // When
        val result = User.restore(userId, email, walletAddress, status, createdAt, updatedAt)

        // Then
        assertTrue(result is Result.Success)
        val user = result.data
        assertEquals(userId, user.id)
        assertEquals(email, user.email)
        assertEquals(walletAddress, user.walletAddress)
        assertEquals(status, user.status)
        assertEquals(createdAt, user.createdAt)
        assertEquals(updatedAt, user.updatedAt)
    }

    @Test
    fun `User changeEmail with valid email should succeed`() {
        // Given
        val user = createTestUser()
        val newEmail = Email.of("newemail@example.com")

        // When
        val result = user.changeEmail(newEmail)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(newEmail, user.email)
    }

    @Test
    fun `User changeEmail when suspended should fail`() {
        // Given
        val user = createTestUser(UserStatus.SUSPENDED)
        val newEmail = Email.of("newemail@example.com")

        // When
        val result = user.changeEmail(newEmail)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `User activate when pending should succeed`() {
        // Given
        val user = createTestUser(UserStatus.PENDING)

        // When
        val result = user.activate()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(UserStatus.ACTIVE, user.status)
    }

    @Test
    fun `User activate when already active should fail`() {
        // Given
        val user = createTestUser(UserStatus.ACTIVE)

        // When
        val result = user.activate()

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `User suspend when active should succeed`() {
        // Given
        val user = createTestUser(UserStatus.ACTIVE)

        // When
        val result = user.suspend()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(UserStatus.SUSPENDED, user.status)
    }

    @Test
    fun `User suspend when already suspended should fail`() {
        // Given
        val user = createTestUser(UserStatus.SUSPENDED)

        // When
        val result = user.suspend()

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.BusinessRuleViolation)
    }

    @Test
    fun `User isActive should return correct status`() {
        // Given
        val activeUser = createTestUser(UserStatus.ACTIVE)
        val pendingUser = createTestUser(UserStatus.PENDING)
        val suspendedUser = createTestUser(UserStatus.SUSPENDED)

        // When & Then
        assertTrue(activeUser.isActive())
        assertFalse(pendingUser.isActive())
        assertFalse(suspendedUser.isActive())
    }

    @Test
    fun `User isPending should return correct status`() {
        // Given
        val activeUser = createTestUser(UserStatus.ACTIVE)
        val pendingUser = createTestUser(UserStatus.PENDING)
        val suspendedUser = createTestUser(UserStatus.SUSPENDED)

        // When & Then
        assertFalse(activeUser.isPending())
        assertTrue(pendingUser.isPending())
        assertFalse(suspendedUser.isPending())
    }

    @Test
    fun `User isSuspended should return correct status`() {
        // Given
        val activeUser = createTestUser(UserStatus.ACTIVE)
        val pendingUser = createTestUser(UserStatus.PENDING)
        val suspendedUser = createTestUser(UserStatus.SUSPENDED)

        // When & Then
        assertFalse(activeUser.isSuspended())
        assertFalse(pendingUser.isSuspended())
        assertTrue(suspendedUser.isSuspended())
    }

    @Test
    fun `User equals should work correctly`() {
        // Given
        val userId = UserId.of("user_123")
        val email = Email.of("test@example.com")
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val status = UserStatus.ACTIVE
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        val user1 = User.restore(userId, email, walletAddress, status, createdAt, updatedAt).data
        val user2 = User.restore(userId, email, walletAddress, status, createdAt, updatedAt).data

        // When & Then
        assertEquals(user1, user2)
    }

    @Test
    fun `User hashCode should work correctly`() {
        // Given
        val userId = UserId.of("user_123")
        val email = Email.of("test@example.com")
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val status = UserStatus.ACTIVE
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        val user1 = User.restore(userId, email, walletAddress, status, createdAt, updatedAt).data
        val user2 = User.restore(userId, email, walletAddress, status, createdAt, updatedAt).data

        // When & Then
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `User toString should include ID`() {
        // Given
        val user = createTestUser()

        // When
        val result = user.toString()

        // Then
        assertTrue(result.contains("User"))
        assertTrue(result.contains(user.id.value))
    }

    private fun createTestUser(status: UserStatus = UserStatus.ACTIVE): User {
        val userId = UserId.of("user_123")
        val email = Email.of("test@example.com")
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        return User.restore(userId, email, walletAddress, status, createdAt, updatedAt).data
    }
}
