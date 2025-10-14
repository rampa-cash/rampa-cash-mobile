package com.example.rampacashmobile.domain.valueobjects

import org.junit.Test
import org.junit.Assert.*

class WalletAddressTest {

    @Test
    fun `WalletAddress creation with valid address should succeed`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"

        // When
        val walletAddress = WalletAddress.of(addressString)

        // Then
        assertEquals(addressString, walletAddress.value)
    }

    @Test
    fun `WalletAddress creation with empty string should throw exception`() {
        // Given
        val addressString = ""

        // When & Then
        try {
            WalletAddress.of(addressString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Wallet address cannot be empty", e.message)
        }
    }

    @Test
    fun `WalletAddress creation with blank string should throw exception`() {
        // Given
        val addressString = "   "

        // When & Then
        try {
            WalletAddress.of(addressString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Wallet address cannot be empty", e.message)
        }
    }

    @Test
    fun `WalletAddress creation with invalid format should throw exception`() {
        // Given
        val addressString = "invalid-address-format"

        // When & Then
        try {
            WalletAddress.of(addressString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid wallet address format") == true)
        }
    }

    @Test
    fun `WalletAddress creation with too short address should throw exception`() {
        // Given
        val addressString = "short"

        // When & Then
        try {
            WalletAddress.of(addressString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid wallet address format") == true)
        }
    }

    @Test
    fun `WalletAddress creation with too long address should throw exception`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM" + "extra"

        // When & Then
        try {
            WalletAddress.of(addressString)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid wallet address format") == true)
        }
    }

    @Test
    fun `WalletAddress equals should work correctly`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
        val walletAddress1 = WalletAddress.of(addressString)
        val walletAddress2 = WalletAddress.of(addressString)

        // When & Then
        assertEquals(walletAddress1, walletAddress2)
    }

    @Test
    fun `WalletAddress hashCode should work correctly`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
        val walletAddress1 = WalletAddress.of(addressString)
        val walletAddress2 = WalletAddress.of(addressString)

        // When & Then
        assertEquals(walletAddress1.hashCode(), walletAddress2.hashCode())
    }

    @Test
    fun `WalletAddress toString should return the address value`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
        val walletAddress = WalletAddress.of(addressString)

        // When
        val result = walletAddress.toString()

        // Then
        assertEquals(addressString, result)
    }

    @Test
    fun `WalletAddress toSolanaPublicKey should work correctly`() {
        // Given
        val addressString = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM"
        val walletAddress = WalletAddress.of(addressString)

        // When
        val publicKey = walletAddress.toSolanaPublicKey()

        // Then
        assertNotNull(publicKey)
        assertEquals(addressString, publicKey.base58())
    }

    @Test
    fun `WalletAddress toSolanaPublicKey with invalid address should throw exception`() {
        // Given
        val addressString = "invalid-address"
        val walletAddress = WalletAddress.of(addressString)

        // When & Then
        try {
            walletAddress.toSolanaPublicKey()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid wallet address format") == true)
        }
    }
}
