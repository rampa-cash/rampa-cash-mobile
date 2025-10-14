package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.net.Uri
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.R
import com.example.rampacashmobile.data.repository.UserRepository
import com.example.rampacashmobile.data.api.service.Web3AuthService
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.domain.services.TransactionDomainService
import com.example.rampacashmobile.domain.services.ContactDomainService
import com.example.rampacashmobile.constants.AppConstants
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.Web3AuthConnected
import com.example.rampacashmobile.usecase.NotConnected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.usecase.TransactionHistoryUseCase
import com.example.rampacashmobile.ui.screens.TransactionDetails
import com.example.rampacashmobile.ui.screens.Transaction
import com.example.rampacashmobile.ui.screens.TransactionType
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.publickey.SolanaPublicKey
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthResponse
import com.web3auth.core.types.Web3AuthOptions
import com.web3auth.core.types.Network
import com.web3auth.core.types.BuildEnv
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainViewState(
    val isLoading: Boolean = true,  // Start with loading=true to prevent premature navigation
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val eurcBalance: Double = 0.0,
    val usdcBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
    val fullAddressForCopy: String? = null, // Full address for clipboard copy
    val walletFound: Boolean = true,
    val memoTxSignature: String? = null,
    val snackbarMessage: String? = null,
    val showTransactionSuccess: Boolean = false,
    val transactionDetails: TransactionDetails? = null,
    // Web3Auth related state
    val isWeb3AuthLoading: Boolean = false,
    val loadingProvider: Provider? = null, // Track which specific provider is loading
    val isWeb3AuthLoggedIn: Boolean = false,
    val web3AuthUserInfo: String? = null,
    val web3AuthPrivateKey: String? = null,
    val web3AuthSolanaPublicKey: String? = null, // Full Solana public key for transactions
    // Transaction history
    val transactionHistory: List<Transaction> = emptyList(),
    val isLoadingTransactions: Boolean = false,
    // Onboarding navigation state
    val needsOnboardingNavigation: Boolean = false,
    val onboardingAuthProvider: String = "",
    val onboardingExistingEmail: String = "",
    val onboardingExistingPhone: String = ""
)

/**
 * MainViewModel - Coordinator that delegates to specialized ViewModels
 * This is a much smaller coordinator that manages the overall app state
 * and delegates specific operations to specialized ViewModels
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase,
    private val transactionHistoryUseCase: TransactionHistoryUseCase,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
    // DDD-aligned domain services
    private val walletDomainService: WalletDomainService,
    private val transactionDomainService: TransactionDomainService,
    private val contactDomainService: ContactDomainService,
    // Backend API services
    private val web3AuthService: Web3AuthService
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val rpcUri = android.net.Uri.parse(BuildConfig.RPC_URI)

    // Combined state from all specialized ViewModels
    private val _state = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _state

    init {
        // Initialize with loading state, then check for existing session
        _state.update { 
            MainViewState(
                    isLoading = true,
                        canTransact = false,
                        solBalance = 0.0,
                        eurcBalance = 0.0,
                        usdcBalance = 0.0,
                        userAddress = "",
                        userLabel = "",
                fullAddressForCopy = null,
                walletFound = true,
                memoTxSignature = null,
                snackbarMessage = null,
                showTransactionSuccess = false,
                transactionDetails = null,
                isWeb3AuthLoading = false,
                loadingProvider = null,
                isWeb3AuthLoggedIn = false,
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        transactionHistory = emptyList(),
                        isLoadingTransactions = false,
                needsOnboardingNavigation = false,
                onboardingAuthProvider = "",
                onboardingExistingEmail = "",
                onboardingExistingPhone = ""
            )
        }
        
        // Initialize the app state
        initializeApp()
    }
    
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Check for existing user session
                val user = userRepository.currentUser.value
                if (user != null) {
                    // User is logged in, load their data
                    loadUserData(user)
                } else {
                    // No user session, show login screen
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during app initialization")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private suspend fun loadUserData(user: com.example.rampacashmobile.data.model.User) {
        // TODO: Load user's wallet data, balances, etc.
        // For now, just set loading to false
        _state.update { 
            it.copy(
                isLoading = false,
                userAddress = user.email, // Temporary - should be wallet address
                userLabel = user.fullName
            )
        }
    }

    // Basic methods - TODO: Implement proper delegation to specialized ViewModels
    // For now, this is a placeholder that shows the structure

    fun loadConnection() {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "loadConnection() - TODO: Delegate to ConnectionViewModel")
    }

    fun connect(sender: ActivityResultSender) {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "connect() - TODO: Delegate to ConnectionViewModel")
    }

    fun disconnect() {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "disconnect() - TODO: Delegate to ConnectionViewModel")
    }

    fun getSolanaBalance(account: SolanaPublicKey) {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "getSolanaBalance() - TODO: Delegate to ConnectionViewModel")
    }

    fun getEurcBalance(account: SolanaPublicKey) {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "getEurcBalance() - TODO: Delegate to ConnectionViewModel")
    }

    fun getUsdcBalance(account: SolanaPublicKey) {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "getUsdcBalance() - TODO: Delegate to ConnectionViewModel")
    }

    fun refreshBalancesAfterTransaction(account: SolanaPublicKey, signature: String) {
        // TODO: Delegate to ConnectionViewModel
        Timber.d(TAG, "refreshBalancesAfterTransaction() - TODO: Delegate to ConnectionViewModel")
    }

    fun getTransactionHistory() {
        // TODO: Delegate to TransactionViewModel
        Timber.d(TAG, "getTransactionHistory() - TODO: Delegate to TransactionViewModel")
    }

    fun sendSplToken(
        sender: ActivityResultSender,
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        // TODO: Delegate to TokenViewModel
        Timber.d(TAG, "sendSplToken() - TODO: Delegate to TokenViewModel")
    }

    fun checkATA(recipientAddress: String, tokenMintAddress: String) {
        // TODO: Delegate to TokenViewModel
        Timber.d(TAG, "checkATA() - TODO: Delegate to TokenViewModel")
    }

    fun checkTokenBalance(tokenMintAddress: String, tokenDecimals: Int) {
        // TODO: Delegate to TokenViewModel
        Timber.d(TAG, "checkTokenBalance() - TODO: Delegate to TokenViewModel")
    }

    // Web3Auth methods
    fun setWeb3AuthLoading(loading: Boolean) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "setWeb3AuthLoading() - TODO: Delegate to Web3AuthViewModel")
    }

    fun setWeb3AuthProviderLoading(provider: Provider) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "setWeb3AuthProviderLoading() - TODO: Delegate to Web3AuthViewModel")
    }

    fun setWeb3AuthError(errorMessage: String) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "setWeb3AuthError() - TODO: Delegate to Web3AuthViewModel")
    }

    fun handleWeb3AuthSuccess(
        web3AuthResponse: Web3AuthResponse,
        provider: Provider,
        solanaPublicKey: String,
        displayAddress: String
    ) {
        Timber.d(TAG, "🔐 Web3Auth login successful, exchanging token with backend...")
        
        viewModelScope.launch {
            try {
                // Call Web3AuthService to exchange token with backend
                val result = web3AuthService.validateWeb3AuthToken(web3AuthResponse)
                
                when (result) {
                    is Result.Success -> {
                        val response = result.data
                        Timber.d(TAG, "✅ Backend token exchange successful")
                        Timber.d(TAG, "👤 User: ${response.user.email}")
                        Timber.d(TAG, "🔑 API Token: ${response.accessToken.take(20)}...")
                        
                        // Update UI state
                        _state.value = _state.value.copy(
                    isWeb3AuthLoading = false,
                    loadingProvider = null,
                    isWeb3AuthLoggedIn = true,
                            web3AuthUserInfo = response.user.email,
                            web3AuthSolanaPublicKey = solanaPublicKey,
                            userAddress = displayAddress
                        )
                    }
                    is Result.Failure -> {
                        Timber.e(TAG, "❌ Backend token exchange failed: ${result.error.message}")
                        _state.value = _state.value.copy(
                            isWeb3AuthLoading = false,
                            loadingProvider = null,
                            isWeb3AuthLoggedIn = false,
                            web3AuthUserInfo = null
                        )
                    }
            }
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during backend token exchange: ${e.message}", e)
                _state.value = _state.value.copy(
                    isWeb3AuthLoading = false,
                    loadingProvider = null,
                    isWeb3AuthLoggedIn = false,
                    web3AuthUserInfo = null
                )
            }
        }
    }

    fun handleWeb3AuthLogout() {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "handleWeb3AuthLogout() - TODO: Delegate to Web3AuthViewModel")
    }

    fun handleWeb3AuthSessionRestore(privateKey: String, solanaPublicKey: String, displayAddress: String) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "handleWeb3AuthSessionRestore() - TODO: Delegate to Web3AuthViewModel")
    }

    fun handleWeb3AuthRedirect(data: Uri) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "handleWeb3AuthRedirect() - TODO: Delegate to Web3AuthViewModel")
    }

    fun onWeb3AuthCancelled() {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "onWeb3AuthCancelled() - TODO: Delegate to Web3AuthViewModel")
    }

    fun handleWeb3AuthSplTransfer(
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        // TODO: Delegate to Web3AuthViewModel
        Timber.d(TAG, "handleWeb3AuthSplTransfer() - TODO: Delegate to Web3AuthViewModel")
    }

    // Onboarding methods
    fun setNeedsOnboardingNavigation(authProvider: String, existingEmail: String, existingPhone: String) {
        // TODO: Delegate to OnboardingViewModel
        Timber.d(TAG, "setNeedsOnboardingNavigation() - TODO: Delegate to OnboardingViewModel")
    }

    fun clearOnboardingNavigation() {
        // TODO: Delegate to OnboardingViewModel
        Timber.d(TAG, "clearOnboardingNavigation() - TODO: Delegate to OnboardingViewModel")
    }

    fun extractUserInfoFromAuth(response: Web3AuthResponse, provider: Provider): Pair<String, String> {
        // TODO: Delegate to OnboardingViewModel
        Timber.d(TAG, "extractUserInfoFromAuth() - TODO: Delegate to OnboardingViewModel")
        return Pair("", "")
    }

    fun needsOnboarding(): Boolean {
        // TODO: Delegate to OnboardingViewModel
        Timber.d(TAG, "needsOnboarding() - TODO: Delegate to OnboardingViewModel")
        return false
    }

    fun completeUserOnboarding(onboardingData: com.example.rampacashmobile.data.model.OnboardingData) {
        // TODO: Delegate to OnboardingViewModel
        Timber.d(TAG, "completeUserOnboarding() - TODO: Delegate to OnboardingViewModel")
    }

    // Utility methods
    fun clearSnackBar() {
        // TODO: Delegate to appropriate ViewModel
        Timber.d(TAG, "clearSnackBar() - TODO: Delegate to appropriate ViewModel")
    }

    fun onTransactionSuccessDone() {
        // TODO: Delegate to appropriate ViewModel
        Timber.d(TAG, "onTransactionSuccessDone() - TODO: Delegate to appropriate ViewModel")
    }

    fun clearError() {
        // TODO: Delegate to all ViewModels
        Timber.d(TAG, "clearError() - TODO: Delegate to all ViewModels")
    }

    // User data access
    val currentUser = userRepository.currentUser
    val onboardingData = userRepository.onboardingData

    private fun getAuthProviderFromState(): String {
        val currentState = _state.value
        return when {
            currentState.isWeb3AuthLoggedIn -> "web3auth"
            currentState.canTransact -> "wallet"
            else -> "unknown"
        }
    }
}