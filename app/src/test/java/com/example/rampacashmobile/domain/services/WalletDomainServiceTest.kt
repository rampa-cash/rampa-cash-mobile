package com.example.rampacashmobile.domain.services

import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.repositories.WalletRepository
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.solana.publickey.SolanaPublicKey
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Currency
import android.net.Uri

class WalletDomainServiceTest {

    private lateinit var walletRepository: WalletRepository
    private lateinit var walletDomainService: WalletDomainService
    private lateinit var rpcUri: Uri

    @Before
    fun setUp() {
        walletRepository = mockk()
        rpcUri = Uri.parse("https://api.devnet.solana.com")
        walletDomainService = WalletDomainService(walletRepository, rpcUri)
    }

    @Test
    fun `loadWalletBalances with valid wallet should succeed`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val wallet = createTestWallet()
        
        every { walletRepository.findByAddress(walletAddress) } returns Result.success(wallet)
        mockkStatic(AccountBalanceUseCase::class)
        mockkStatic(AssociatedTokenAccountUtils::class)
        mockkStatic(TokenAccountBalanceUseCase::class)
        
        every { AccountBalanceUseCase(rpcUri, any()) } returns 1000000000L // 1 SOL
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns true
        every { TokenAccountBalanceUseCase(rpcUri, any()) } returns 1000000L // 1 token

        // When
        val result = walletDomainService.loadWalletBalances(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val loadedWallet = result.data
        assertEquals(wallet.id, loadedWallet.id)
        assertEquals(wallet.userId, loadedWallet.userId)
        assertEquals(wallet.address, loadedWallet.address)
    }

    @Test
    fun `loadWalletBalances when wallet not found should fail`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        every { walletRepository.findByAddress(walletAddress) } returns Result.failure(DomainError.NotFound("Wallet not found"))

        // When
        val result = walletDomainService.loadWalletBalances(walletAddress)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.NotFound)
    }

    @Test
    fun `getSolBalance with valid wallet should succeed`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AccountBalanceUseCase::class)
        every { AccountBalanceUseCase(rpcUri, any()) } returns 1000000000L // 1 SOL

        // When
        val result = walletDomainService.getSolBalance(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val money = result.data
        assertEquals(BigDecimal("1.0"), money.amount)
        assertEquals(Currency.USD, money.currency)
    }

    @Test
    fun `getEurcBalance with valid wallet should succeed`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        mockkStatic(TokenAccountBalanceUseCase::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns true
        every { TokenAccountBalanceUseCase(rpcUri, any()) } returns 1000000L // 1 EURC

        // When
        val result = walletDomainService.getEurcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val money = result.data
        assertEquals(BigDecimal("1.0"), money.amount)
        assertEquals(Currency.EUR, money.currency)
    }

    @Test
    fun `getEurcBalance when ATA does not exist should return zero`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns false

        // When
        val result = walletDomainService.getEurcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val money = result.data
        assertEquals(BigDecimal.ZERO, money.amount)
        assertEquals(Currency.EUR, money.currency)
    }

    @Test
    fun `getUsdcBalance with valid wallet should succeed`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        mockkStatic(TokenAccountBalanceUseCase::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns true
        every { TokenAccountBalanceUseCase(rpcUri, any()) } returns 1000000L // 1 USDC

        // When
        val result = walletDomainService.getUsdcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val money = result.data
        assertEquals(BigDecimal("1.0"), money.amount)
        assertEquals(Currency.USD, money.currency)
    }

    @Test
    fun `getUsdcBalance when ATA does not exist should return zero`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns false

        // When
        val result = walletDomainService.getUsdcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        val money = result.data
        assertEquals(BigDecimal.ZERO, money.amount)
        assertEquals(Currency.USD, money.currency)
    }

    @Test
    fun `getSolBalance with network error should fail`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AccountBalanceUseCase::class)
        every { AccountBalanceUseCase(rpcUri, any()) } throws Exception("Network error")

        // When
        val result = walletDomainService.getSolBalance(walletAddress)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.NetworkError)
    }

    @Test
    fun `getEurcBalance with network error should fail`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } throws Exception("Network error")

        // When
        val result = walletDomainService.getEurcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.NetworkError)
    }

    @Test
    fun `getUsdcBalance with network error should fail`() {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } throws Exception("Network error")

        // When
        val result = walletDomainService.getUsdcBalance(walletAddress)

        // Then
        assertTrue(result is Result.Failure)
        assertTrue(result.error is DomainError.NetworkError)
    }

    private fun createTestWallet(): Wallet {
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "Test Wallet"
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        return Wallet.restore("wallet_123", userId, address, label, true, createdAt, updatedAt).data
    }
}
