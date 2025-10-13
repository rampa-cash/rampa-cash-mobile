package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Transaction
import com.example.rampacashmobile.domain.entities.TransactionType
import com.example.rampacashmobile.domain.services.TransactionDomainService
import com.example.rampacashmobile.domain.valueobjects.Money
import com.example.rampacashmobile.domain.valueobjects.TransactionId
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.usecase.TransactionHistoryUseCase
import com.example.rampacashmobile.ui.screens.Transaction as UITransaction
import com.example.rampacashmobile.ui.screens.TransactionDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency
import javax.inject.Inject

/**
 * ViewModel responsible for transaction-related operations
 * Uses DDD domain services for business logic
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionDomainService: TransactionDomainService,
    private val transactionHistoryUseCase: TransactionHistoryUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "TransactionViewModel"
    }

    // Transaction state
    private val _transactionState = MutableStateFlow(TransactionState())
    val transactionState: StateFlow<TransactionState> = _transactionState

    /**
     * Load transaction history for a user
     */
    fun loadTransactionHistory(userId: UserId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionState.update { it.copy(isLoading = true, error = null) }
                
                // Use domain service to get transactions
                val result = transactionDomainService.getUserTransactions(userId)
                
                when (result) {
                    is Result.Success -> {
                        val uiTransactions = result.data.map { domainTransaction ->
                            convertToUITransaction(domainTransaction)
                        }
                        
                        _transactionState.update { 
                            it.copy(
                                isLoading = false,
                                transactions = uiTransactions,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _transactionState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load transaction history", e)
                _transactionState.update { 
                    it.copy(
                        isLoading = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to load transaction history: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Create a new transaction
     */
    fun createTransaction(
        userId: UserId,
        fromWallet: WalletAddress,
        toWallet: WalletAddress,
        amount: Money,
        transactionType: TransactionType,
        description: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionState.update { it.copy(isCreatingTransaction = true, error = null) }
                
                val result = transactionDomainService.processTransaction(
                    userId = userId,
                    fromWallet = fromWallet,
                    toWallet = toWallet,
                    amount = amount,
                    transactionType = transactionType,
                    description = description
                )
                
                when (result) {
                    is Result.Success -> {
                        val uiTransaction = convertToUITransaction(result.data)
                        _transactionState.update { 
                            it.copy(
                                isCreatingTransaction = false,
                                lastCreatedTransaction = uiTransaction,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _transactionState.update { 
                            it.copy(
                                isCreatingTransaction = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create transaction", e)
                _transactionState.update { 
                    it.copy(
                        isCreatingTransaction = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to create transaction: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Get transaction details by ID
     */
    fun getTransactionDetails(transactionId: TransactionId) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transactionState.update { it.copy(isLoadingDetails = true, error = null) }
                
                val result = transactionDomainService.getTransaction(transactionId)
                
                when (result) {
                    is Result.Success -> {
                        val uiTransaction = convertToUITransaction(result.data)
                        val details = TransactionDetails(
                            signature = uiTransaction.id,
                            amount = uiTransaction.amount.toString(),
                            tokenSymbol = uiTransaction.currency,
                            recipientAddress = uiTransaction.recipient,
                            recipientName = null,
                            timestamp = uiTransaction.date.time,
                            isDevnet = true
                        )
                        
                        _transactionState.update { 
                            it.copy(
                                isLoadingDetails = false,
                                selectedTransaction = details,
                                error = null
                            )
                        }
                    }
                    is Result.Failure -> {
                        _transactionState.update { 
                            it.copy(
                                isLoadingDetails = false,
                                error = result.error
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get transaction details", e)
                _transactionState.update { 
                    it.copy(
                        isLoadingDetails = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to get transaction details: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Clear selected transaction
     */
    fun clearSelectedTransaction() {
        _transactionState.update { it.copy(selectedTransaction = null) }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _transactionState.update { it.copy(error = null) }
    }

    /**
     * Convert domain Transaction to UI Transaction
     */
    private fun convertToUITransaction(domainTransaction: Transaction): UITransaction {
        return UITransaction(
            id = domainTransaction.id.value,
            recipient = domainTransaction.toWallet.value,
            sender = domainTransaction.fromWallet.value,
            amount = domainTransaction.amount.amount.toDouble(),
            date = java.util.Date(domainTransaction.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()),
            description = domainTransaction.description,
            currency = domainTransaction.currency.code,
            transactionType = when (domainTransaction.transactionType) {
                TransactionType.SEND -> com.example.rampacashmobile.ui.screens.TransactionType.SENT
                TransactionType.RECEIVE -> com.example.rampacashmobile.ui.screens.TransactionType.RECEIVED
                else -> com.example.rampacashmobile.ui.screens.TransactionType.SENT
            },
            tokenSymbol = domainTransaction.currency.code,
            tokenIcon = 0, // Default icon
            tokenName = null
        )
    }
}

/**
 * State class for transaction-related UI state
 * Aligned with our domain Transaction entity
 */
data class TransactionState(
    val isLoading: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val isCreatingTransaction: Boolean = false,
    val transactions: List<com.example.rampacashmobile.ui.screens.Transaction> = emptyList(),
    val selectedTransaction: TransactionDetails? = null,
    val lastCreatedTransaction: com.example.rampacashmobile.ui.screens.Transaction? = null,
    val error: com.example.rampacashmobile.domain.common.DomainError? = null
)
