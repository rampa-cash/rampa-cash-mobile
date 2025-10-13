package com.example.rampacashmobile.viewmodel

import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.entities.Contact
import com.example.rampacashmobile.domain.valueobjects.Money
import com.example.rampacashmobile.ui.screens.Transaction
import com.example.rampacashmobile.ui.screens.TransactionDetails
import com.web3auth.core.types.Provider

/**
 * Main application view state that coordinates all domain-specific states
 * 
 * This state class serves as the main coordinator for the entire app,
 * containing references to focused domain states and global app state
 */
data class AppViewState(
    // Global app state
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
    
    // Domain-specific states
    val walletState: WalletState = WalletState(),
    val transactionState: TransactionState = TransactionState(),
    val contactState: ContactState = ContactState(),
    val authState: AuthState = AuthState(),
    
    // Navigation state
    val needsOnboardingNavigation: Boolean = false,
    val onboardingAuthProvider: String = "",
    val onboardingExistingEmail: String = "",
    val onboardingExistingPhone: String = ""
)

/**
 * Wallet-specific state aligned with Wallet domain entity
 * 
 * This state corresponds to wallet operations and balance management
 */
data class WalletState(
    val isLoading: Boolean = false,
    val canTransact: Boolean = false,
    val userAddress: String = "",
    val userLabel: String = "",
    val fullAddressForCopy: String? = null,
    val walletFound: Boolean = true,
    
    // Token balances using domain Money value objects
    val solBalance: Money? = null,
    val eurcBalance: Money? = null,
    val usdcBalance: Money? = null,
    
    val error: DomainError? = null
)

/**
 * Transaction-specific state aligned with Transaction domain entity
 * 
 * This state corresponds to transaction operations and history
 */
data class TransactionState(
    val isLoading: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val isCreatingTransaction: Boolean = false,
    val isLoadingDetails: Boolean = false,
    
    // Transaction data
    val transactionHistory: List<Transaction> = emptyList(),
    val selectedTransaction: TransactionDetails? = null,
    val lastCreatedTransaction: Transaction? = null,
    val memoTxSignature: String? = null,
    val showTransactionSuccess: Boolean = false,
    
    val error: DomainError? = null
)

/**
 * Contact-specific state aligned with Contact domain entity
 * 
 * This state corresponds to contact management operations
 */
data class ContactState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    
    // Contact data
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    
    val error: DomainError? = null
)

/**
 * Authentication-specific state for Web3Auth and MWA
 * 
 * This state corresponds to user authentication and session management
 */
data class AuthState(
    val isWeb3AuthLoading: Boolean = false,
    val loadingProvider: Provider? = null,
    val isWeb3AuthLoggedIn: Boolean = false,
    val web3AuthUserInfo: String? = null,
    val web3AuthPrivateKey: String? = null,
    val web3AuthSolanaPublicKey: String? = null,
    
    val error: DomainError? = null
)
