package com.example.rampacashmobile.viewmodel

import com.example.rampacashmobile.domain.services.TransactionDomainService
import com.example.rampacashmobile.domain.valueobjects.*
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.entities.TransactionType
import com.example.rampacashmobile.usecase.TransactionHistoryUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private lateinit var transactionDomainService: TransactionDomainService
    private lateinit var transactionHistoryUseCase: TransactionHistoryUseCase
    private lateinit var context: Context
    private lateinit var transactionViewModel: TransactionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionDomainService = mockk()
        transactionHistoryUseCase = mockk()
        context = mockk()
        transactionViewModel = TransactionViewModel(transactionDomainService, transactionHistoryUseCase, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadTransactionHistory with valid user should update state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val transactions = listOf(createTestTransaction())
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.success(transactions)

        // When
        transactionViewModel.loadTransactionHistory(userId)

        // Then
        val state = transactionViewModel.transactionState.value
        assertEquals(1, state.transactionHistory.size)
        assertFalse(state.isLoadingHistory)
        assertNull(state.error)
    }

    @Test
    fun `loadTransactionHistory with error should update error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val error = DomainError.NetworkError("Network error")
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.failure(error)

        // When
        transactionViewModel.loadTransactionHistory(userId)

        // Then
        val state = transactionViewModel.transactionState.value
        assertEquals(error, state.error)
        assertFalse(state.isLoadingHistory)
    }

    @Test
    fun `loadTransactionHistoryResult with valid user should return correct result`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val transactions = listOf(createTestTransaction())
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.success(transactions)

        // When
        val result = transactionViewModel.loadTransactionHistoryResult(userId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(1, result.data.size)
        assertEquals("Test Transaction", result.data[0].description)
    }

    @Test
    fun `loadTransactionHistoryResult with error should return error result`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val error = DomainError.NetworkError("Network error")
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.failure(error)

        // When
        val result = transactionViewModel.loadTransactionHistoryResult(userId)

        // Then
        assertTrue(result is Result.Failure)
        assertEquals(error, result.error)
    }

    @Test
    fun `createTransaction with valid data should succeed`() = runTest {
        // Given
        val fromWallet = createTestWallet()
        val toWallet = createTestWallet()
        val amount = Money(BigDecimal("100.0"), Currency.USD)
        val description = "Test transaction"
        val transaction = createTestTransaction()
        
        every { transactionDomainService.createTransaction(fromWallet, toWallet, amount, description) } returns Result.success(transaction)

        // When
        transactionViewModel.createTransaction(fromWallet, toWallet, amount, description)

        // Then
        verify { transactionDomainService.createTransaction(fromWallet, toWallet, amount, description) }
    }

    @Test
    fun `createTransaction with error should update error state`() = runTest {
        // Given
        val fromWallet = createTestWallet()
        val toWallet = createTestWallet()
        val amount = Money(BigDecimal("100.0"), Currency.USD)
        val description = "Test transaction"
        val error = DomainError.BusinessRuleViolation("Insufficient funds")
        
        every { transactionDomainService.createTransaction(fromWallet, toWallet, amount, description) } returns Result.failure(error)

        // When
        transactionViewModel.createTransaction(fromWallet, toWallet, amount, description)

        // Then
        val state = transactionViewModel.transactionState.value
        assertEquals(error, state.error)
    }

    @Test
    fun `convertToUITransaction should convert domain transaction to UI transaction`() = runTest {
        // Given
        val domainTransaction = createTestTransaction()

        // When
        val uiTransaction = transactionViewModel.convertToUITransaction(domainTransaction)

        // Then
        assertEquals(domainTransaction.id.value, uiTransaction.id)
        assertEquals(domainTransaction.description, uiTransaction.description)
        assertEquals(domainTransaction.amount.amount, uiTransaction.amount)
        assertEquals(domainTransaction.amount.currency.currencyCode, uiTransaction.currency)
        assertEquals(domainTransaction.type.name, uiTransaction.type.name)
    }

    @Test
    fun `convertToUITransaction with empty description should use default`() = runTest {
        // Given
        val domainTransaction = createTestTransaction(description = "")

        // When
        val uiTransaction = transactionViewModel.convertToUITransaction(domainTransaction)

        // Then
        assertEquals("P2P Transfer", uiTransaction.description)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        val error = DomainError.NetworkError("Network error")
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.failure(error)
        transactionViewModel.loadTransactionHistory(userId)

        // Verify error is set
        assertNotNull(transactionViewModel.transactionState.value.error)

        // When
        transactionViewModel.clearError()

        // Then
        assertNull(transactionViewModel.transactionState.value.error)
    }

    @Test
    fun `loadTransactionHistory should set loading state correctly`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        
        every { transactionDomainService.getUserTransactions(userId) } returns Result.success(emptyList())

        // When
        transactionViewModel.loadTransactionHistory(userId)

        // Then
        val state = transactionViewModel.transactionState.value
        assertFalse(state.isLoadingHistory)
    }

    @Test
    fun `loadTransactionHistory with exception should handle error`() = runTest {
        // Given
        val userId = UserId.of("user_123")
        
        every { transactionDomainService.getUserTransactions(userId) } throws Exception("Network error")

        // When
        transactionViewModel.loadTransactionHistory(userId)

        // Then
        val state = transactionViewModel.transactionState.value
        assertTrue(state.error is DomainError.NetworkError)
        assertFalse(state.isLoadingHistory)
    }

    private fun createTestTransaction(description: String = "Test Transaction"): Transaction {
        val id = TransactionId.of("tx_123")
        val fromWallet = createTestWallet()
        val toWallet = createTestWallet()
        val amount = Money(BigDecimal("100.0"), Currency.USD)
        val type = TransactionType.SEND
        val createdAt = java.time.LocalDateTime.now()

        return Transaction.restore(id, fromWallet, toWallet, amount, description, type, createdAt).data
    }

    private fun createTestWallet(): com.example.rampacashmobile.domain.entities.Wallet {
        val userId = UserId.of("user_123")
        val address = WalletAddress.of("9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM")
        val label = "Test Wallet"
        val createdAt = java.time.LocalDateTime.now()
        val updatedAt = java.time.LocalDateTime.now()

        return com.example.rampacashmobile.domain.entities.Wallet.restore("wallet_123", userId, address, label, true, createdAt, updatedAt).data
    }
}
