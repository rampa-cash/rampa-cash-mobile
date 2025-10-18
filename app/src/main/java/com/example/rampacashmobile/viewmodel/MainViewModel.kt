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
import com.example.rampacashmobile.data.api.service.TokenRefreshService
import com.example.rampacashmobile.data.api.service.UserVerificationService
import com.example.rampacashmobile.data.api.ApiClient
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
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
    val web3AuthUserInfo: com.example.rampacashmobile.data.api.model.UserApiModel? = null,
    val web3AuthPrivateKey: String? = null,
    val web3AuthSolanaPublicKey: String? = null, // Full Solana public key for transactions
    // Transaction history
    val transactionHistory: List<Transaction> = emptyList(),
    val isLoadingTransactions: Boolean = false,
    // Onboarding navigation state
    val needsOnboardingNavigation: Boolean = false,
    val onboardingAuthProvider: String = "",
    val onboardingExistingEmail: String = "",
    val onboardingExistingPhone: String = "",
    // User verification status
    val userVerificationStatus: String? = null, // PENDING_VERIFICATION, VERIFIED, REJECTED
    val userStatus: String? = null, // ACTIVE, SUSPENDED, PENDING_VERIFICATION
    val missingFields: List<String> = emptyList(),
    val isUserVerified: Boolean = false,
    val showVerificationBanner: Boolean = false
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
        private val web3AuthService: Web3AuthService,
        private val tokenRefreshService: TokenRefreshService,
        private val apiClient: ApiClient,
        private val userVerificationService: UserVerificationService,
        // Web3Auth domain service for wallet-specific operations
        private val web3AuthDomainService: com.example.rampacashmobile.domain.services.Web3AuthDomainService,
        // Web3Auth state service for state management operations
        private val web3AuthStateService: com.example.rampacashmobile.domain.services.Web3AuthStateService
) : BaseViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }
    
    init {
        android.util.Log.d(TAG, "🚀🚀🚀 MainViewModel constructor called")
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
                Timber.d(TAG, "🚀 Initializing app...")
                
                // First, check if user has valid backend authentication
                if (isBackendAuthenticated()) {
                    Timber.d(TAG, "✅ User has valid backend authentication, loading user data...")
                    
                    // User is authenticated with backend, load their profile
                    loadAuthenticatedUserData()
                } else {
                    Timber.d(TAG, "❌ No valid backend authentication found")
                    
                    // Check if user has Web3Auth session but no backend auth
                    val user = userRepository.currentUser.value
                    if (user != null) {
                        Timber.d(TAG, "🔍 User has local session but no backend auth, clearing...")
                        // Clear local session if no backend auth
                        userRepository.clearUserData()
                    }
                    
                    // Show login screen
                    _state.update { it.copy(isLoading = false) }
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ Error during app initialization")
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
    
    /**
     * Load user data for authenticated users
     */
    private suspend fun loadAuthenticatedUserData() {
        try {
            Timber.d(TAG, "📱 Loading authenticated user data...")
            
            // For now, we'll get the user profile from the backend
            // In a real implementation, we might want to store the user data locally
            // and refresh it periodically or on app resume
            
            // Get user profile from backend
            val profileResult = web3AuthService.getUserProfile()
            
            when (profileResult) {
                is Result.Success -> {
                    val userProfile = profileResult.data
                    Timber.d(TAG, "✅ User profile loaded: ${userProfile.email}")
                    
                    // Convert UserProfileResponse to UserApiModel for consistency
                    val userApiModel = com.example.rampacashmobile.data.api.model.UserApiModel(
                        id = userProfile.id,
                        email = userProfile.email,
                        firstName = userProfile.firstName,
                        lastName = userProfile.lastName,
                        language = userProfile.language,
                        authProvider = userProfile.authProvider,
                        isActive = userProfile.isActive,
                        status = userProfile.status,
                        createdAt = userProfile.createdAt,
                        lastLoginAt = userProfile.lastLoginAt
                    )
                    
                    // Update UI state with user data
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isWeb3AuthLoggedIn = true,
                            web3AuthUserInfo = userApiModel,
                            userAddress = userProfile.email ?: "", // Temporary - should be wallet address
                            userLabel = "${userProfile.firstName ?: ""} ${userProfile.lastName ?: ""}"
                        )
                    }
                    
                    // Load additional user data (wallet, transactions, etc.)
                    loadUserWalletData()
                }
                is Result.Failure -> {
                    Timber.e(TAG, "❌ Failed to load user profile: ${profileResult.error.message}")
                    
                    // If profile loading fails, clear authentication and show login
                    clearAuthenticationState()
                    _state.update { it.copy(isLoading = false) }
                }
            }
            } catch (e: Exception) {
            Timber.e(TAG, "❌ Exception loading authenticated user data: ${e.message}", e)
            clearAuthenticationState()
            _state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Load user's wallet data and balances
     */
    private suspend fun loadUserWalletData() {
        try {
            Timber.d(TAG, "💰 Loading user wallet data...")
            
            // TODO: Load wallet balances, transaction history, etc.
            // For now, just set some default values
            _state.update { 
                it.copy(
                    solBalance = 0.0,
                    eurcBalance = 0.0,
                    usdcBalance = 0.0,
                    canTransact = true
                )
            }
            
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Exception loading wallet data: ${e.message}", e)
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
        android.util.Log.d(TAG, "setWeb3AuthLoading: $loading")
        web3AuthStateService.setWeb3AuthLoading(loading)
    }

    fun setWeb3AuthProviderLoading(provider: Provider) {
        android.util.Log.d(TAG, "setWeb3AuthProviderLoading: $provider")
        web3AuthStateService.setWeb3AuthProviderLoading(provider)
    }

    fun setWeb3AuthError(errorMessage: String) {
        android.util.Log.d(TAG, "setWeb3AuthError: $errorMessage")
        web3AuthStateService.setWeb3AuthError(errorMessage)
    }

    fun handleWeb3AuthSuccess(
        web3AuthResponse: Web3AuthResponse,
        provider: Provider,
        solanaPublicKey: String,
        displayAddress: String
    ) {
        android.util.Log.d(TAG, "🚀🚀🚀 ENTERING handleWeb3AuthSuccess method - FIRST LINE")
        android.util.Log.d(TAG, "🔐 Web3Auth login successful, exchanging token with backend...")
        android.util.Log.d(TAG, "🔍 Method parameters: provider=$provider, solanaPublicKey=$solanaPublicKey")
        android.util.Log.d(TAG, "🔍 Web3AuthResponse type: ${web3AuthResponse::class.simpleName}")
        android.util.Log.d(TAG, "🔍 Provider type: ${provider::class.simpleName}")
        android.util.Log.d(TAG, "🔍 SolanaPublicKey type: ${solanaPublicKey::class.simpleName}")
        android.util.Log.d(TAG, "🔍 DisplayAddress type: ${displayAddress::class.simpleName}")
        
        viewModelScope.launch {
            try {
                android.util.Log.d(TAG, "🚀 ENTERING viewModelScope.launch block")
                android.util.Log.d(TAG, "🔄 Starting Web3Auth token validation...")
                android.util.Log.d(TAG, "🔍 Web3Auth response: ${web3AuthResponse.userInfo?.name}")
                
                // First, handle Web3Auth wallet connection (delegate to Web3AuthDomainService)
                android.util.Log.d(TAG, "🔗 Delegating Web3Auth wallet connection to Web3AuthDomainService...")
                val walletConnectionResult = web3AuthDomainService.handleWeb3AuthWalletConnection(
                    web3AuthResponse, provider, solanaPublicKey, displayAddress
                )
                
                when (walletConnectionResult) {
                    is Result.Success -> {
                        android.util.Log.d(TAG, "✅ Web3Auth wallet connection successful")
                        val walletData = walletConnectionResult.data
                        android.util.Log.d(TAG, "🔑 Wallet data: ${walletData.displayName} (${walletData.providerName})")
                    }
                    is Result.Failure -> {
                        android.util.Log.e(TAG, "❌ Web3Auth wallet connection failed: ${walletConnectionResult.error.message}")
                        // Continue with backend validation anyway - wallet connection might still work
                    }
                }
                
                // Call Web3AuthService to exchange token with backend
                android.util.Log.d(TAG, "📞 About to call web3AuthService.validateWeb3AuthToken")
                val result = web3AuthService.validateWeb3AuthToken(web3AuthResponse)
                android.util.Log.d(TAG, "📞 web3AuthService.validateWeb3AuthToken completed")
                android.util.Log.d(TAG, "🔄 Web3Auth validation result: ${result::class.simpleName}")
                
                when (result) {
                    is Result.Success -> {
                        val response = result.data
                        android.util.Log.d(TAG, "✅ Backend token exchange successful")
                        android.util.Log.d(TAG, "👤 User: ${response.user.email ?: "No email"}")
                        android.util.Log.d(TAG, "🔑 API Token: ${response.accessToken.take(20)}...")
                        android.util.Log.d(TAG, "🔍 Full user response: ${response.user}")
                        
                        // Check user verification status
                        val isVerified = response.user.verificationStatus == "verified"
                        val showBanner = response.user.verificationStatus == "pending_verification"
                        
                        android.util.Log.d(TAG, "🔍 User verification status: ${response.user.verificationStatus}")
                        android.util.Log.d(TAG, "🔍 User status: ${response.user.status}")
                        android.util.Log.d(TAG, "🔍 Is verified: $isVerified")
                        android.util.Log.d(TAG, "🔍 Show verification banner: $showBanner")
                        
                        // Check if user has complete profile (Google, Apple, etc.)
                        val hasCompleteProfile = !response.user.email.isNullOrBlank() && 
                                                !response.user.firstName.isNullOrBlank() && 
                                                !response.user.lastName.isNullOrBlank()
                        
                        android.util.Log.d(TAG, "🔍 Profile completeness check:")
                        android.util.Log.d(TAG, "  - Email: ${response.user.email}")
                        android.util.Log.d(TAG, "  - FirstName: ${response.user.firstName}")
                        android.util.Log.d(TAG, "  - LastName: ${response.user.lastName}")
                        android.util.Log.d(TAG, "  - Has complete profile: $hasCompleteProfile")
                        android.util.Log.d(TAG, "  - Onboarding completed: ${userRepository.isOnboardingCompleted()}")
                        
                        // If user has complete profile, automatically mark onboarding as completed
                        if (hasCompleteProfile && !userRepository.isOnboardingCompleted()) {
                            android.util.Log.d(TAG, "✅ User has complete profile, marking onboarding as completed")
                            userRepository.completeOnboarding(
                                walletAddress = displayAddress,
                                authProvider = provider.toString().lowercase()
                            )
                        }
                        
                        // Update UI state with verification information
                        _state.value = _state.value.copy(
                            isWeb3AuthLoading = false,
                            loadingProvider = null,
                            isWeb3AuthLoggedIn = true,
                            web3AuthUserInfo = response.user,
                            web3AuthSolanaPublicKey = solanaPublicKey,
                            userAddress = displayAddress,
                            userVerificationStatus = response.user.verificationStatus,
                            userStatus = response.user.status,
                            isUserVerified = isVerified,
                            showVerificationBanner = showBanner
                        )
                        
                        // Now check if user needs onboarding after we have the complete profile
                        val needsOnboardingCheck = needsOnboarding()
                        android.util.Log.d(TAG, "🔍 Final onboarding check: $needsOnboardingCheck")
                        
                        if (needsOnboardingCheck) {
                            android.util.Log.d(TAG, "🎯 User needs onboarding after profile check - setting navigation flag")
                            val (existingEmail, existingPhone) = extractUserInfoFromAuth(web3AuthResponse, provider)
                            setNeedsOnboardingNavigation(provider.toString().lowercase(), existingEmail, existingPhone)
                        } else {
                            android.util.Log.d(TAG, "✅ User has complete profile - no onboarding needed")
                        }
                        
                        // Load additional user data (wallet, transactions, etc.) to enable canTransact
                        android.util.Log.d(TAG, "🔄 Loading authenticated user data to enable transactions...")
                        loadAuthenticatedUserData()
                    }
                    is Result.Failure -> {
                        android.util.Log.e(TAG, "❌ Backend token exchange failed: ${result.error.message}")
                        android.util.Log.e(TAG, "❌ Error type: ${result.error::class.simpleName}")
                        _state.value = _state.value.copy(
                            isWeb3AuthLoading = false,
                            loadingProvider = null,
                            isWeb3AuthLoggedIn = false,
                            web3AuthUserInfo = null
                        )
                    }
            }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "🚨 EXCEPTION in handleWeb3AuthSuccess: ${e::class.simpleName}")
                android.util.Log.e(TAG, "❌ Exception during backend token exchange: ${e.message}", e)
                android.util.Log.e(TAG, "🔍 Exception stack trace: ${e.stackTraceToString()}")
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
        viewModelScope.launch {
            try {
                Timber.d(TAG, "🚪 Starting complete logout process...")
                
                // 1. Logout from backend API
                val backendLogoutResult = web3AuthService.logout()
                
                when (backendLogoutResult) {
                    is Result.Success -> {
                        Timber.d(TAG, "✅ Backend logout successful")
                    }
                    is Result.Failure -> {
                        Timber.e(TAG, "❌ Backend logout failed: ${backendLogoutResult.error.message}")
                        // Continue with local logout even if backend fails
                    }
                }
                
                // 2. Clear all authentication state
                clearAuthenticationState()
                
                // 3. Clear local user data
                userRepository.clearUserData()
                
                // 4. Update UI state to show logged out
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isWeb3AuthLoggedIn = false,
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        userAddress = "",
                        userLabel = "",
                        canTransact = false,
                        solBalance = 0.0,
                        eurcBalance = 0.0,
                        usdcBalance = 0.0
                    )
                }
                
                Timber.d(TAG, "✅ Complete logout process finished")
                
                } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during logout: ${e.message}", e)
                
                // Even if there's an exception, clear local state
                clearAuthenticationState()
                userRepository.clearUserData()
            }
        }
    }

    fun handleWeb3AuthSessionRestore(privateKey: String, solanaPublicKey: String, displayAddress: String) {
        android.util.Log.d(TAG, "handleWeb3AuthSessionRestore: $displayAddress")
        web3AuthStateService.handleWeb3AuthSessionRestore(privateKey, solanaPublicKey, displayAddress)
    }

    fun handleWeb3AuthRedirect(data: Uri) {
        android.util.Log.d(TAG, "handleWeb3AuthRedirect: $data")
        web3AuthStateService.handleWeb3AuthRedirect(data)
    }

    fun onWeb3AuthCancelled() {
        android.util.Log.d(TAG, "onWeb3AuthCancelled")
        web3AuthStateService.onWeb3AuthCancelled()
    }
    
    /**
     * Handle Web3Auth logout error
     */
    fun handleWeb3AuthLogoutError(errorMessage: String) {
        Timber.e(TAG, "❌ Web3Auth logout error: $errorMessage")
        
        // Even if Web3Auth logout fails, we should still clear local state
        // This ensures the user can still log out locally
        viewModelScope.launch {
            try {
                // Clear all authentication state
                clearAuthenticationState()
                
                // Clear local user data
                userRepository.clearUserData()
                
                // Update UI state to show logged out
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isWeb3AuthLoggedIn = false,
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        userAddress = "",
                        userLabel = "",
                        canTransact = false,
                        solBalance = 0.0,
                        eurcBalance = 0.0,
                        usdcBalance = 0.0
                    )
                }
                
                Timber.d(TAG, "✅ Local logout completed despite Web3Auth error")
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during error logout: ${e.message}", e)
            }
        }
    }
    
    /**
     * Direct logout method for when Web3Auth session is not available
     * This can be called directly from UI when user wants to logout
     */
    fun performDirectLogout() {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "🚪 Performing direct logout...")
                
                // 1. Logout from backend API
                val backendLogoutResult = web3AuthService.logout()
                
                when (backendLogoutResult) {
                    is Result.Success -> {
                        Timber.d(TAG, "✅ Backend logout successful")
                    }
                    is Result.Failure -> {
                        Timber.e(TAG, "❌ Backend logout failed: ${backendLogoutResult.error.message}")
                        // Continue with local logout even if backend fails
                    }
                }
                
                // 2. Clear all authentication state
                clearAuthenticationState()
                
                // 3. Clear local user data
                userRepository.clearUserData()
                
                // 4. Update UI state to show logged out
                _state.update { 
                    it.copy(
                        isLoading = false,
            isWeb3AuthLoggedIn = false,
            web3AuthUserInfo = null,
            web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
            userAddress = "",
                        userLabel = "",
                        canTransact = false,
            solBalance = 0.0,
            eurcBalance = 0.0,
                        usdcBalance = 0.0
                    )
                }
                
                Timber.d(TAG, "✅ Direct logout completed")
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during direct logout: ${e.message}", e)
                
                // Even if there's an exception, clear local state
                clearAuthenticationState()
                userRepository.clearUserData()
            }
        }
    }

    fun handleWeb3AuthSplTransfer(
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        android.util.Log.d(TAG, "handleWeb3AuthSplTransfer: $recipientAddress, $amount, $tokenMintAddress")
        web3AuthStateService.handleWeb3AuthSplTransfer(recipientAddress, amount, tokenMintAddress)
    }

    // Onboarding methods
    fun setNeedsOnboardingNavigation(authProvider: String, existingEmail: String, existingPhone: String) {
        Timber.d(TAG, "🎯 Setting onboarding navigation: provider=$authProvider, email=$existingEmail, phone=$existingPhone")
        
        // Update UI state to trigger onboarding navigation
        _state.value = _state.value.copy(
            needsOnboardingNavigation = true,
            onboardingAuthProvider = authProvider,
            onboardingExistingEmail = existingEmail,
            onboardingExistingPhone = existingPhone
        )
    }

    fun clearOnboardingNavigation() {
        Timber.d(TAG, "🧹 Clearing onboarding navigation")
        
        // Clear onboarding navigation flags
        _state.value = _state.value.copy(
            needsOnboardingNavigation = false,
            onboardingAuthProvider = "",
            onboardingExistingEmail = "",
            onboardingExistingPhone = ""
        )
    }

    fun extractUserInfoFromAuth(response: Web3AuthResponse, provider: Provider): Pair<String, String> {
        Timber.d(TAG, "🔍 Extracting user info from Web3Auth response")
        
        val userInfo = response.userInfo
        val email = userInfo?.email ?: ""
        val name = userInfo?.name ?: ""

        return when (provider) {
            Provider.GOOGLE, Provider.APPLE -> {
                // Email is primary, extract name parts
                val nameParts = name.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: ""
                val lastName = nameParts.getOrNull(1) ?: ""

                // Update onboarding data with pre-filled info
                val onboardingData = com.example.rampacashmobile.data.model.OnboardingData(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    authProvider = provider.toString().lowercase()
                )
                userRepository.updateOnboardingData(onboardingData)

                Pair(email, "")
            }
            Provider.SMS_PASSWORDLESS -> {
                // Phone is primary
                val phoneNumber = extractPhoneNumberFromWeb3Auth(response)
                Pair("", phoneNumber)
            }
            else -> Pair("", "")
        }
    }

    fun needsOnboarding(): Boolean {
        // For users with complete profiles from backend (Google, Apple, etc.), 
        // we don't need onboarding even if local onboarding flag is not set
        val currentState = _state.value
        val hasCompleteProfile = currentState.web3AuthUserInfo?.let { userInfo ->
            !userInfo.email.isNullOrBlank() && 
            !userInfo.firstName.isNullOrBlank() && 
            !userInfo.lastName.isNullOrBlank()
        } ?: false
        
        // Only require onboarding if:
        // 1. Local onboarding is not completed AND
        // 2. User doesn't have a complete profile from backend
        return !userRepository.isOnboardingCompleted() && !hasCompleteProfile
    }

    fun completeUserOnboarding(onboardingData: com.example.rampacashmobile.data.model.OnboardingData) {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "✅ Completing user onboarding...")
                
                // Update user repository with onboarding data
                userRepository.updateOnboardingData(onboardingData)

                // Complete onboarding (this will mark it as completed internally)
                userRepository.completeOnboarding(
                    walletAddress = "", // Will be set by the onboarding screen
                    authProvider = onboardingData.authProvider
                )
                
                // Clear onboarding navigation
                clearOnboardingNavigation()
                
                // Now that onboarding is complete, try to validate the Web3Auth token again
                // This should work now that we have the required user information
                Timber.d(TAG, "🔄 Onboarding complete - user can now proceed with normal login flow")
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Failed to complete onboarding: ${e.message}", e)
            }
        }
    }

    /**
     * Extract phone number from Web3Auth response
     */
    private fun extractPhoneNumberFromWeb3Auth(web3AuthResponse: Web3AuthResponse): String {
        return try {
            // Try to get phone number from userInfo
            val userInfo = web3AuthResponse.userInfo
            val phoneNumber = userInfo?.name ?: userInfo?.verifierId ?: ""
            
            Timber.d(TAG, "📞 Extracted phone number from Web3Auth: $phoneNumber")
            phoneNumber
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Failed to extract phone number: ${e.message}", e)
            ""
        }
    }

    // User verification and operation restriction methods

    /**
     * Check if user can perform financial operations
     */
    fun canPerformFinancialOperations(): Boolean {
        val currentState = _state.value
        val isVerified = currentState.userVerificationStatus == "VERIFIED"
        val isActive = currentState.userStatus == "ACTIVE"
        
        Timber.d(TAG, "🔍 Can perform financial operations: verified=$isVerified, active=$isActive")
        return isVerified && isActive
    }

    /**
     * Check if user can browse the app
     */
    fun canBrowseApp(): Boolean {
        val currentState = _state.value
        val isActive = currentState.userStatus == "ACTIVE"
        val isPending = currentState.userStatus == "PENDING_VERIFICATION"
        
        Timber.d(TAG, "🔍 Can browse app: active=$isActive, pending=$isPending")
        return isActive || isPending
    }

    /**
     * Check if user should see verification banner
     */
    fun shouldShowVerificationBanner(): Boolean {
        val currentState = _state.value
        val isPendingVerification = currentState.userVerificationStatus == "PENDING_VERIFICATION"
        val isNotSuspended = currentState.userStatus != "SUSPENDED"
        
        Timber.d(TAG, "🔍 Should show verification banner: pending=$isPendingVerification, notSuspended=$isNotSuspended")
        return isPendingVerification && isNotSuspended
    }

    /**
     * Check if user account is suspended
     */
    fun isAccountSuspended(): Boolean {
        val currentState = _state.value
        val isSuspended = currentState.userStatus == "SUSPENDED"
        
        Timber.d(TAG, "🔍 Is account suspended: $isSuspended")
        return isSuspended
    }

    /**
     * Get missing fields for profile completion
     */
    fun getMissingFields(): List<String> {
        val currentState = _state.value
        val user = currentState.web3AuthUserInfo
        val missingFields = mutableListOf<String>()
        
        if (user?.email.isNullOrBlank()) missingFields.add("email")
        if (user?.firstName.isNullOrBlank()) missingFields.add("firstName")
        if (user?.lastName.isNullOrBlank()) missingFields.add("lastName")
        
        Timber.d(TAG, "🔍 Missing fields: $missingFields")
        return missingFields
    }

    /**
     * Navigate to profile completion screen
     */
    fun navigateToProfileCompletion() {
        Timber.d(TAG, "🎯 Navigating to profile completion screen")
        // TODO: Implement navigation to profile completion screen
        // This will be handled by the UI layer
    }

    /**
     * Dismiss verification banner
     */
    fun dismissVerificationBanner() {
        Timber.d(TAG, "❌ Dismissing verification banner")
        _state.update { it.copy(showVerificationBanner = false) }
    }

    /**
     * Refresh user verification status from backend
     */
    fun refreshVerificationStatus() {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "🔄 Refreshing verification status...")
                
                val result = userVerificationService.getVerificationStatus()
                
                when (result) {
                    is Result.Success -> {
                        val verificationStatus = result.data
                        Timber.d(TAG, "✅ Verification status refreshed")
                        Timber.d(TAG, "🔍 Status: ${verificationStatus.verificationStatus}")
                        Timber.d(TAG, "📋 Missing fields: ${verificationStatus.missingFields}")
                        
                        _state.update { 
                            it.copy(
                                userVerificationStatus = verificationStatus.verificationStatus,
                                missingFields = verificationStatus.missingFields,
                                isUserVerified = verificationStatus.isVerified,
                                showVerificationBanner = verificationStatus.verificationStatus == "PENDING_VERIFICATION"
                            )
                        }
                    }
                    is Result.Failure -> {
                        Timber.e(TAG, "❌ Failed to refresh verification status: ${result.error.message}")
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during verification status refresh: ${e.message}", e)
            }
        }
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

    override fun clearError() {
        logErrorClearing("MainViewModel")
        web3AuthStateService.clearError()
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

    /**
     * Check if user is authenticated with backend API
     */
    fun isBackendAuthenticated(): Boolean {
        return apiClient.isAuthenticated()
    }

    /**
     * Get valid access token
     */
    fun getValidAccessToken(): String? {
        return apiClient.getCurrentAccessToken()
    }

    /**
     * Check if token needs refresh
     */
    fun shouldRefreshToken(): Boolean {
        return tokenRefreshService.shouldRefreshToken()
    }

    /**
     * Force refresh the authentication token
     */
    suspend fun refreshAuthenticationToken(): Result<String> {
        return try {
            Timber.d(TAG, "🔄 Force refreshing authentication token...")
            val result = apiClient.refreshToken()
            
            when (result) {
                is Result.Success -> {
                    Timber.d(TAG, "✅ Token refreshed successfully")
                    Result.success(result.data)
                }
                is Result.Failure -> {
                    Timber.e(TAG, "❌ Token refresh failed: ${result.error.message}")
                    // If refresh fails, clear authentication state
                    clearAuthenticationState()
                    Result.failure(result.error)
                }
            }
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Exception during token refresh: ${e.message}", e)
            clearAuthenticationState()
            Result.failure(com.example.rampacashmobile.domain.common.DomainError.NetworkError("Token refresh failed: ${e.message}"))
        }
    }

    /**
     * Check if token can be refreshed
     */
    fun canRefreshToken(): Boolean {
        return apiClient.canRefreshToken()
    }

    /**
     * Clear authentication state and force re-login
     */
    fun clearAuthenticationState() {
        Timber.d(TAG, "🚪 Clearing authentication state...")
        
        // Clear backend tokens
        apiClient.clearAuthToken()
        
        // Update UI state
        _state.value = _state.value.copy(
            isWeb3AuthLoggedIn = false,
            web3AuthUserInfo = null,
            web3AuthPrivateKey = null,
            web3AuthSolanaPublicKey = null,
            userAddress = ""
        )
    }

    /**
     * Check authentication status and update UI accordingly
     */
    suspend fun checkAuthenticationStatus() {
        Timber.d(TAG, "🔍 Checking authentication status...")
        
        if (isBackendAuthenticated()) {
            Timber.d(TAG, "✅ User is authenticated with backend")
            
            // Check if token needs refresh
            if (shouldRefreshToken()) {
                Timber.d(TAG, "🔄 Token needs refresh, attempting refresh...")
                val refreshResult = refreshAuthenticationToken()
                
                when (refreshResult) {
                    is Result.Success -> {
                        Timber.d(TAG, "✅ Token refreshed successfully")
                    }
                    is Result.Failure -> {
                        Timber.e(TAG, "❌ Token refresh failed: ${refreshResult.error.message}")
                        clearAuthenticationState()
                    }
                }
            }
        } else {
            Timber.d(TAG, "❌ User is not authenticated with backend")
            // Clear authentication state if not authenticated
            clearAuthenticationState()
        }
    }
    
    /**
     * Handle app resume - check authentication and refresh if needed
     */
    fun onAppResume() {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "📱 App resumed, checking authentication...")
                
                if (isBackendAuthenticated()) {
                    Timber.d(TAG, "✅ User still authenticated, checking token validity...")
                    
                    // Check if token needs refresh
                    if (shouldRefreshToken()) {
                        Timber.d(TAG, "🔄 Token needs refresh on app resume...")
                        val refreshResult = refreshAuthenticationToken()
                        
                        when (refreshResult) {
                            is Result.Success -> {
                                Timber.d(TAG, "✅ Token refreshed on app resume")
                            }
                            is Result.Failure -> {
                                Timber.e(TAG, "❌ Token refresh failed on app resume: ${refreshResult.error.message}")
                                clearAuthenticationState()
                            }
                        }
                    }
                } else {
                    Timber.d(TAG, "❌ User no longer authenticated on app resume")
                    clearAuthenticationState()
                }

            } catch (e: Exception) {
                Timber.e(TAG, "❌ Exception during app resume check: ${e.message}", e)
            }
        }
    }
}