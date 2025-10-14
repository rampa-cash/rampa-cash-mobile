package com.example.rampacashmobile.viewmodel

import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.solana.publickey.SolanaPublicKey
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Currency
import android.content.Context
import android.net.Uri

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {

    private lateinit var walletDomainService: WalletDomainService
    private lateinit var context: Context
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var rpcUri: Uri
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        walletDomainService = mockk()
        context = mockk()
        rpcUri = Uri.parse("https://api.devnet.solana.com")
        walletViewModel = WalletViewModel(walletDomainService, rpcUri)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSolBalance with valid wallet should update state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val expectedMoney = Money(BigDecimal("1.0"), Currency.USD)
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.success(expectedMoney)

        // When
        walletViewModel.getSolBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(expectedMoney, state.solBalance)
        assertNull(state.error)
    }

    @Test
    fun `getSolBalance with error should update error state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.NetworkError("Network error")
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.failure(error)

        // When
        walletViewModel.getSolBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `getEurcBalance with valid wallet should update state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val expectedMoney = Money(BigDecimal("1.0"), Currency.EUR)
        
        every { walletDomainService.getEurcBalance(walletAddress) } returns Result.success(expectedMoney)

        // When
        walletViewModel.getEurcBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(expectedMoney, state.eurcBalance)
        assertNull(state.error)
    }

    @Test
    fun `getEurcBalance with error should update error state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.NetworkError("Network error")
        
        every { walletDomainService.getEurcBalance(walletAddress) } returns Result.failure(error)

        // When
        walletViewModel.getEurcBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `getUsdcBalance with valid wallet should update state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val expectedMoney = Money(BigDecimal("1.0"), Currency.USD)
        
        every { walletDomainService.getUsdcBalance(walletAddress) } returns Result.success(expectedMoney)

        // When
        walletViewModel.getUsdcBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(expectedMoney, state.usdcBalance)
        assertNull(state.error)
    }

    @Test
    fun `getUsdcBalance with error should update error state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.NetworkError("Network error")
        
        every { walletDomainService.getUsdcBalance(walletAddress) } returns Result.failure(error)

        // When
        walletViewModel.getUsdcBalance(walletAddress)

        // Then
        val state = walletViewModel.walletState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `refreshAllBalances should call all balance methods`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.success(Money(BigDecimal("1.0"), Currency.USD))
        every { walletDomainService.getEurcBalance(walletAddress) } returns Result.success(Money(BigDecimal("1.0"), Currency.EUR))
        every { walletDomainService.getUsdcBalance(walletAddress) } returns Result.success(Money(BigDecimal("1.0"), Currency.USD))

        // When
        walletViewModel.refreshAllBalances(walletAddress)

        // Then
        verify { walletDomainService.getSolBalance(walletAddress) }
        verify { walletDomainService.getEurcBalance(walletAddress) }
        verify { walletDomainService.getUsdcBalance(walletAddress) }
    }

    @Test
    fun `canSendAmount with sufficient balance should return true`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val amount = Money(BigDecimal("0.5"), Currency.USD)
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.success(Money(BigDecimal("1.0"), Currency.USD))
        walletViewModel.getSolBalance(walletAddress)

        // When
        val canSend = walletViewModel.canSendAmount(amount, "SOL")

        // Then
        assertTrue(canSend)
    }

    @Test
    fun `canSendAmount with insufficient balance should return false`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val amount = Money(BigDecimal("2.0"), Currency.USD)
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.success(Money(BigDecimal("1.0"), Currency.USD))
        walletViewModel.getSolBalance(walletAddress)

        // When
        val canSend = walletViewModel.canSendAmount(amount, "SOL")

        // Then
        assertFalse(canSend)
    }

    @Test
    fun `canSendAmount with unknown currency should return false`() = runTest {
        // Given
        val amount = Money(BigDecimal("1.0"), Currency.USD)

        // When
        val canSend = walletViewModel.canSendAmount(amount, "UNKNOWN")

        // Then
        assertFalse(canSend)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val error = DomainError.NetworkError("Network error")
        
        every { walletDomainService.getSolBalance(walletAddress) } returns Result.failure(error)
        walletViewModel.getSolBalance(walletAddress)

        // Verify error is set
        assertNotNull(walletViewModel.walletState.value.error)

        // When
        walletViewModel.clearError()

        // Then
        assertNull(walletViewModel.walletState.value.error)
    }

    @Test
    fun `getSolBalanceResult should return correct result`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val expectedMoney = Money(BigDecimal("1.0"), Currency.USD)
        
        mockkStatic(AccountBalanceUseCase::class)
        every { AccountBalanceUseCase(rpcUri, any()) } returns 1000000000L // 1 SOL

        // When
        val result = walletViewModel.getSolBalanceResult(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedMoney.amount, result.data.amount)
        assertEquals(expectedMoney.currency, result.data.currency)
    }

    @Test
    fun `getEurcBalanceResult should return correct result`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        mockkStatic(TokenAccountBalanceUseCase::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns true
        every { TokenAccountBalanceUseCase(rpcUri, any()) } returns 1000000L // 1 EURC

        // When
        val result = walletViewModel.getEurcBalanceResult(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(BigDecimal("1.0"), result.data.amount)
        assertEquals(Currency.EUR, result.data.currency)
    }

    @Test
    fun `getUsdcBalanceResult should return correct result`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        
        mockkStatic(AssociatedTokenAccountUtils::class)
        mockkStatic(TokenAccountBalanceUseCase::class)
        
        every { AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(any(), any()) } returns SolanaPublicKey.from("ATokenGPvbdGVLXLb2P5b2egXyu2KQDGFogMcdT5c")
        every { AssociatedTokenAccountUtils.checkAccountExists(rpcUri, any()) } returns true
        every { TokenAccountBalanceUseCase(rpcUri, any()) } returns 1000000L // 1 USDC

        // When
        val result = walletViewModel.getUsdcBalanceResult(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(BigDecimal("1.0"), result.data.amount)
        assertEquals(Currency.USD, result.data.currency)
    }

    @Test
    fun `refreshAllBalancesResult should return correct result`() = runTest {
        // Given
        val walletAddress = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val expectedWallet = createTestWallet()
        
        every { walletDomainService.loadWalletBalances(walletAddress) } returns Result.success(expectedWallet)

        // When
        val result = walletViewModel.refreshAllBalancesResult(walletAddress)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedWallet, result.data)
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
