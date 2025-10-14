package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.net.Uri
import timber.log.Timber
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.R
import com.example.rampacashmobile.data.repository.UserRepository
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.domain.services.TransactionDomainService
import com.example.rampacashmobile.domain.services.ContactDomainService
import com.example.rampacashmobile.constants.AppConstants
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.Web3AuthConnected
import com.example.rampacashmobile.usecase.NotConnected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.DebugSessionHelper
import com.example.rampacashmobile.usecase.SplTokenTransferUseCase
import com.example.rampacashmobile.usecase.ManualSplTokenTransferUseCase
import com.example.rampacashmobile.usecase.Web3AuthSplTransferUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.example.rampacashmobile.usecase.TransferConfig
import com.example.rampacashmobile.usecase.TransactionHistoryUseCase
import com.example.rampacashmobile.ui.screens.TransactionDetails
import com.example.rampacashmobile.ui.screens.Transaction
import com.example.rampacashmobile.ui.screens.TransactionType
import com.funkatronics.encoders.Base58
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

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
    private val contactDomainService: ContactDomainService
) : ViewModel() {
    private val rpcUri = BuildConfig.RPC_URI.toUri()

    companion object {
        private const val TAG = "MainViewModel"
    }

    // Track if this is the first time loading connections (to show welcome snackbar)
    private var isInitialLoad = true

    // Lazy Web3Auth initialization to prevent blocking during app startup
    private val web3AuthLazy: Web3Auth by lazy {
        Timber.d("🔧 Creating Web3Auth instance...")
        try {
            Web3Auth(
                Web3AuthOptions(
                    clientId = context.getString(R.string.web3auth_project_id),
                    network = Network.SAPPHIRE_DEVNET, // Changed to match your dashboard config
                    buildEnv = BuildEnv.PRODUCTION,
                    redirectUrl = Uri.parse("com.example.rampacashmobile://auth")
                ),
                context
            ).also {
                Timber.d("✅ Web3Auth instance created successfully")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to create Web3Auth instance: ${e.message}")
            throw e
        }
    }

    private fun MainViewState.updateViewState() {
        _state.update { this }
    }

    private val _state = MutableStateFlow(MainViewState())

    val viewState: StateFlow<MainViewState>
        get() = _state

    fun loadConnection() {
        Timber.d("🔄 loadConnection() called - checking for persisted sessions...")

        // Debug current session storage state
        DebugSessionHelper.debugAllStoredSessions(
            context.getSharedPreferences("scaffold_prefs", android.content.Context.MODE_PRIVATE)
        )

        // Set loading state while checking for sessions
        _state.value.copy(isLoading = true).updateViewState()

        val persistedConnection = persistenceUseCase.getWalletConnection()
        Timber.d("🔍 Persistence check result: ${persistedConnection::class.simpleName}")

        when (persistedConnection) {
            is Connected -> {
                Timber.d("🔄 Restoring MWA session: ${persistedConnection.accountLabel}")
                _state.value.copy(
                    isLoading = true,
                    canTransact = true,
                    userAddress = persistedConnection.publicKey.base58(),
                    userLabel = persistedConnection.accountLabel,
                    fullAddressForCopy = persistedConnection.publicKey.base58(), // Full address for copy
                ).updateViewState()

                getSolanaBalance(persistedConnection.publicKey)
                getEurcBalance(persistedConnection.publicKey)
                getUsdcBalance(persistedConnection.publicKey)

                _state.value.copy(
                    isLoading = false,
                    // Only show welcome snackbar on initial app load, not on navigation
                    snackbarMessage = if (isInitialLoad) "✅ | Successfully auto-connected to MWA wallet: ${persistedConnection.accountLabel}" else null
                ).updateViewState()

                // Set the auth token in walletAdapter
                walletAdapter.authToken = persistedConnection.authToken
            }

            is Web3AuthConnected -> {
                Timber.d("🔄 Restoring Web3Auth session: ${persistedConnection.accountLabel}")

                // Create display-friendly address
                val fullAddress = persistedConnection.publicKey.base58()
                val displayAddress = "${fullAddress.take(8)}...${fullAddress.takeLast(8)}"

                _state.value.copy(
                    isLoading = true,
                    isWeb3AuthLoggedIn = true,
                    web3AuthUserInfo = persistedConnection.accountLabel,
                    web3AuthPrivateKey = persistedConnection.privateKey,
                    web3AuthSolanaPublicKey = fullAddress,
                    canTransact = true,
                    userLabel = "${persistedConnection.accountLabel} (via ${persistedConnection.providerName})",
                    userAddress = displayAddress,
                    fullAddressForCopy = fullAddress,
                ).updateViewState()

                getSolanaBalance(persistedConnection.publicKey)
                getEurcBalance(persistedConnection.publicKey)
                getUsdcBalance(persistedConnection.publicKey)

                _state.value.copy(
                    isLoading = false,
                    // Only show welcome snackbar on initial app load, not on navigation
                    snackbarMessage = if (isInitialLoad) "✅ | Successfully auto-connected to Web3Auth: ${persistedConnection.accountLabel}" else null
                ).updateViewState()
            }

            is NotConnected -> {
                Timber.d("🔄 No persisted session found - setting loading to false")
                _state.value.copy(isLoading = false).updateViewState()
                // No persisted session - stay in login state
            }
        }

        // Mark initial load as complete
        isInitialLoad = false
    }

    private var isLoadingTransactionHistory = false

    fun getTransactionHistory() {
        if (isLoadingTransactionHistory) {
            Timber.d("⏸️ Transaction history fetch already in progress, skipping...")
            return
        }

        viewModelScope.launch {
            try {
                isLoadingTransactionHistory = true
                val currentState = _state.value

                // Debug current state
                Timber.d("🔍 Transaction History Debug:")
                Timber.d("- canTransact: ${currentState.canTransact}")
                Timber.d("- isWeb3AuthLoggedIn: ${currentState.isWeb3AuthLoggedIn}")
                Timber.d(TAG, "- userAddress: ${currentState.userAddress}")
                Timber.d(TAG, "- fullAddressForCopy: ${currentState.fullAddressForCopy}")
                Timber.d(TAG, "- web3AuthSolanaPublicKey: ${currentState.web3AuthSolanaPublicKey}")

                // Determine wallet address based on connection type
                val walletAddress = when {
                    currentState.isWeb3AuthLoggedIn && !currentState.web3AuthSolanaPublicKey.isNullOrEmpty() -> {
                        Timber.d(TAG, "📱 Using Web3Auth address for transaction history")
                        currentState.web3AuthSolanaPublicKey
                    }
                    currentState.canTransact && !currentState.fullAddressForCopy.isNullOrEmpty() -> {
                        Timber.d(TAG, "📱 Using MWA address for transaction history")
                        currentState.fullAddressForCopy
                    }
                    !currentState.userAddress.isNullOrEmpty() -> {
                        Timber.d(TAG, "📱 Using userAddress for transaction history")
                        currentState.userAddress
                    }
                    else -> {
                        Timber.w(TAG, "🔍 No wallet address available for transaction history")
                        Timber.w(TAG, "State dump: canTransact=${currentState.canTransact}, isWeb3Auth=${currentState.isWeb3AuthLoggedIn}")
                        _state.update { it.copy(
                            isLoadingTransactions = false,
                            snackbarMessage = "❌ | No wallet connected"
                        )}
                        isLoadingTransactionHistory = false
                        return@launch
                    }
                }

                Timber.d(TAG, "🔍 Fetching transaction history for wallet: ${walletAddress.take(8)}...")

                _state.update { it.copy(isLoadingTransactions = true) }

                // Delegate to TransactionViewModel
                val userId = UserId.of(walletAddress) // Use wallet address as user ID for now
                val result = transactionDomainService.getUserTransactions(userId)
                
                when (result) {
                    is Result.Success -> {
                        val domainTransactions = result.data
                        val uiTransactions = domainTransactions.map { domainTransaction ->
                            Transaction(
                                id = domainTransaction.id.value,
                                recipient = domainTransaction.toWallet.value,
                                sender = domainTransaction.fromWallet.value,
                                amount = domainTransaction.amount.amount.toDouble(),
                                date = java.util.Date.from(domainTransaction.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant()),
                                description = domainTransaction.description,
                                currency = domainTransaction.currency.code,
                                transactionType = when (domainTransaction.transactionType) {
                                    com.example.rampacashmobile.domain.entities.TransactionType.SEND -> TransactionType.SENT
                                    com.example.rampacashmobile.domain.entities.TransactionType.RECEIVE -> TransactionType.RECEIVED
                                    com.example.rampacashmobile.domain.entities.TransactionType.TRANSFER -> TransactionType.SENT
                                },
                                tokenSymbol = domainTransaction.currency.code,
                                tokenIcon = R.drawable.solana_logo,
                                tokenName = domainTransaction.currency.code
                            )
                        }
                        _state.update { it.copy(
                            transactionHistory = uiTransactions,
                            isLoadingTransactions = false
                        )}
                        Timber.d(TAG, "✅ Transaction history loaded: ${uiTransactions.size} transactions")
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _state.update { it.copy(
                            isLoadingTransactions = false,
                            snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)
                        )}
                    }
                }

            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to load transaction history")
                ErrorHandler.logError(error, TAG)
                _state.update { it.copy(
                    isLoadingTransactions = false,
                    snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)
                )}
            } finally {
                isLoadingTransactionHistory = false
            }
        }
    }

    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            when (val result = walletAdapter.connect(sender)) {
                is TransactionResult.Success -> {
                    val currentConn = Connected(
                        SolanaPublicKey(result.authResult.publicKey),
                        result.authResult.accountLabel ?: "",
                        result.authResult.authToken
                    )

                    Timber.d(TAG, "💾 About to persist MWA connection for: ${currentConn.accountLabel}")
                    persistenceUseCase.persistConnection(
                        currentConn.publicKey, currentConn.accountLabel, currentConn.authToken
                    )

                    // Verify persistence worked
                    val testConnection = persistenceUseCase.getWalletConnection()
                    Timber.d(TAG, "🧪 Persistence verification: ${testConnection::class.simpleName}")

                    // Set the auth token in walletAdapter
                    walletAdapter.authToken = currentConn.authToken
                    _state.value.copy(
                        isLoading = true,
                        userAddress = currentConn.publicKey.base58(),
                        userLabel = currentConn.accountLabel,
                        fullAddressForCopy = currentConn.publicKey.base58() // Full address for copy
                    ).updateViewState()

                    getSolanaBalance(currentConn.publicKey)
                    getEurcBalance(currentConn.publicKey)
                    getUsdcBalance(currentConn.publicKey)

                    _state.value.copy(
                        isLoading = false,
                        canTransact = true,
                        snackbarMessage = "✅ | Successfully connected to: \n" + currentConn.publicKey.base58() + "."
                    ).updateViewState()
                }

                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(
                        walletFound = false, snackbarMessage = "❌ | No wallet found."
                    ).updateViewState()
                }

                is TransactionResult.Failure -> {
                    _state.value.copy(
                        isLoading = false,
                        canTransact = false,
                        userAddress = "",
                        userLabel = "",
                        fullAddressForCopy = null, // Clear the full address
                        snackbarMessage = "❌ | Failed connecting to wallet: " + result.e.message
                    ).updateViewState()
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            // Clear the loading flag to allow fresh transaction fetching on reconnect
            isLoadingTransactionHistory = false

            val conn = persistenceUseCase.getWalletConnection()
            when (conn) {
                is Connected -> {
                    persistenceUseCase.clearConnection()
                    // Reset to clean disconnected state
                    _state.value.copy(
                        isLoading = true, // Keep loading during logout transition
                        canTransact = false,
                        isWeb3AuthLoggedIn = false,
                        loadingProvider = null,
                        userAddress = "",
                        userLabel = "",
                        fullAddressForCopy = "",
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        // Clear transaction history and balances
                        transactionHistory = emptyList(),
                        isLoadingTransactions = false,
                        solBalance = 0.0,
                        usdcBalance = 0.0,
                        eurcBalance = 0.0,
                        snackbarMessage = "✅ | Disconnected from MWA wallet."
                    ).updateViewState()
                }
                is Web3AuthConnected -> {
                    persistenceUseCase.clearConnection()
                    // Reset to clean disconnected state
                    _state.value.copy(
                        isLoading = true, // Keep loading during logout transition
                        canTransact = false,
                        isWeb3AuthLoggedIn = false,
                        loadingProvider = null,
                        userAddress = "",
                        userLabel = "",
                        fullAddressForCopy = "",
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        // Clear transaction history and balances
                        transactionHistory = emptyList(),
                        isLoadingTransactions = false,
                        solBalance = 0.0,
                        usdcBalance = 0.0,
                        eurcBalance = 0.0,
                        snackbarMessage = "✅ | Disconnected from Web3Auth."
                    ).updateViewState()
                }
                is NotConnected -> {
                    // Already disconnected - ensure clean state
                    _state.value.copy(
                        isLoading = true, // Keep loading during logout transition
                        canTransact = false,
                        isWeb3AuthLoggedIn = false,
                        loadingProvider = null,
                        userAddress = "",
                        userLabel = "",
                        fullAddressForCopy = "",
                        web3AuthUserInfo = null,
                        web3AuthPrivateKey = null,
                        web3AuthSolanaPublicKey = null,
                        // Clear transaction history and balances
                        transactionHistory = emptyList(),
                        isLoadingTransactions = false,
                        solBalance = 0.0,
                        usdcBalance = 0.0,
                        eurcBalance = 0.0,
                        snackbarMessage = "ℹ️ | No active connection to disconnect."
                    ).updateViewState()
                }
            }
        }
    }

    fun getSolanaBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getSolBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _state.value.copy(
                            solBalance = money.amount.toDouble()
                        ).updateViewState()
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _state.value.copy(
                            snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)
                        ).updateViewState()
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get SOL balance")
                ErrorHandler.logError(error, TAG)
                _state.value.copy(
                    snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)
                ).updateViewState()
            }
        }
    }

    /**
     * Refresh balances after a successful transaction with proper timing and retry logic
     * Delegates to WalletViewModel for balance operations
     */
    private fun refreshBalancesAfterTransaction(account: SolanaPublicKey, signature: String) {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "🔄 Refreshing balances after transaction: ${signature.take(8)}...")
                Timber.d(TAG, "🔄 Account for balance refresh: ${account.base58()}")

                // Wait longer for transaction confirmation (blockchain updates can be slow)
                Timber.d(TAG, "⏳ Waiting 4 seconds for blockchain confirmation...")
                delay(4000) // 4 seconds initial delay (increased from 2)

                val walletAddress = WalletAddress.of(account)

                // Refresh all balances using WalletViewModel
                val refreshResult = walletDomainService.loadWalletBalances(walletAddress)
                
                when (refreshResult) {
                    is Result.Success -> {
                        // Refresh individual balances since Wallet entity doesn't contain balances
                        val solResult = walletDomainService.getSolBalance(walletAddress)
                        val eurcResult = walletDomainService.getEurcBalance(walletAddress)
                        val usdcResult = walletDomainService.getUsdcBalance(walletAddress)
                        
                        val solBalance = when (solResult) {
                            is Result.Success -> solResult.data.amount.toDouble()
                            is Result.Failure -> 0.0
                        }
                        
                        val eurcBalance = when (eurcResult) {
                            is Result.Success -> eurcResult.data.amount.toDouble()
                            is Result.Failure -> 0.0
                        }
                        
                        val usdcBalance = when (usdcResult) {
                            is Result.Success -> usdcResult.data.amount.toDouble()
                            is Result.Failure -> 0.0
                        }
                        
                        _state.value.copy(
                            solBalance = solBalance,
                            eurcBalance = eurcBalance,
                            usdcBalance = usdcBalance
                        ).updateViewState()
                        
                        Timber.d(TAG, "✅ Balance refresh completed for transaction ${signature.take(8)}")
                        Timber.d(TAG, "💰 Final balances - SOL: $solBalance, EURC: $eurcBalance, USDC: $usdcBalance")
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(refreshResult.error, TAG)
                        Timber.e(TAG, "⚠️ Balance refresh failed for transaction ${signature.take(8)}: ${refreshResult.error.message}")
                    }
                }

            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to refresh balances after transaction")
                ErrorHandler.logError(error, TAG)
                Timber.e(TAG, "⚠️ Balance refresh failed for transaction ${signature.take(8)}: ${e.message}")
                // Don't update UI state on refresh failure - keep existing balances
            }
        }
    }


    fun getEurcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getEurcBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _state.value.copy(
                            eurcBalance = money.amount.toDouble()
                        ).updateViewState()
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _state.value.copy(
                            snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)
                        ).updateViewState()
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get EURC balance")
                ErrorHandler.logError(error, TAG)
                _state.value.copy(
                    snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)
                ).updateViewState()
            }
        }
    }

    fun getUsdcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getUsdcBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _state.value.copy(
                            usdcBalance = money.amount.toDouble()
                        ).updateViewState()
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _state.value.copy(
                            snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)
                        ).updateViewState()
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get USDC balance")
                ErrorHandler.logError(error, TAG)
                _state.value.copy(
                    snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)
                ).updateViewState()
            }
        }
    }


    private suspend fun getAccountBalance(account: SolanaPublicKey): Double {
        return try {
            AccountBalanceUseCase(rpcUri, account) / 1000000000.0
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Transfer SPL Tokens using the simplified higher-level function
     * Automatically handles ATA creation if needed
     */
    fun sendSplToken(
        sender: ActivityResultSender,
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        viewModelScope.launch {
            try {
                // Check if user is logged in via Web3Auth
                if (viewState.value.isWeb3AuthLoggedIn) {
                    handleWeb3AuthSplTransfer(recipientAddress, amount, tokenMintAddress, tokenDecimals, recipientName)
                    return@launch
                }

                // Show which implementation is being used
                if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                    Timber.d(TAG, "${TransferConfig.getImplementationEmoji()} Using ${TransferConfig.getImplementationName()}")
                }

                // Check if we have a valid connection first
                val currentConnection = persistenceUseCase.getWalletConnection()

                if (currentConnection !is Connected) {
                    _state.value.copy(
                        snackbarMessage = "❌ | Please connect your wallet first"
                    ).updateViewState()
                    return@launch
                }

                val result = walletAdapter.transact(sender) { authResult ->
                    val ownerAccount = SolanaPublicKey(authResult.accounts.first().publicKey)
                    val recipientPubkey = SolanaPublicKey.from(recipientAddress)
                    val tokenMint = SolanaPublicKey.from(tokenMintAddress)

                    // Convert amount based on token decimals
                    val amountDouble = amount.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Invalid amount: $amount")
                    val multiplier = 10.0.pow(tokenDecimals.toDouble())
                    val amountInTokenUnits = (amountDouble * multiplier).toLong()

                    // Choose implementation based on configuration
                    if (TransferConfig.USE_MANUAL_TRANSFER) {
                        if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                            Timber.d(TAG, "🔧 Building transaction manually (bypasses web3-solana bugs)")
                        }

                        // Use manual implementation that bypasses library serialization bugs
                        val transactionBytes = try {
                            ManualSplTokenTransferUseCase.transfer(
                                rpcUri = rpcUri,
                                fromWallet = ownerAccount,
                                toWallet = recipientPubkey,
                                mint = tokenMint,
                                amount = amountInTokenUnits
                            )
                        } catch (e: Exception) {
                            Timber.e(TAG, "❌ Manual transfer failed: ${e.message}", e)
                            throw RuntimeException("Manual transfer failed: ${e.message}", e)
                        }

                        signAndSendTransactions(arrayOf(transactionBytes))
                    } else {
                        if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                            Timber.d(TAG, "📚 Using web3-solana library transaction building")
                        }

                        // Use original implementation via web3-solana library
                        val tokenTransferTx = SplTokenTransferUseCase.transfer(
                            rpcUri = rpcUri,
                            fromWallet = ownerAccount,
                            toWallet = recipientPubkey,
                            mint = tokenMint,
                            amount = amountInTokenUnits
                        )

                        signAndSendTransactions(arrayOf(tokenTransferTx.serialize()))
                    }
                }

                _state.value = when (result) {
                    is TransactionResult.Success -> {
                        val signatureBytes = result.successPayload?.signatures?.first()
                        signatureBytes?.let {
                            val signature = Base58.encodeToString(signatureBytes)

                            // Refresh balances after successful transfer (with delay for blockchain confirmation)
                            // Note: For regular wallet users, userAddress is the full Base58 address
                            // For Web3Auth users, we should use web3AuthSolanaPublicKey instead
                            val userAccount = if (viewState.value.isWeb3AuthLoggedIn) {
                                // Use full public key for Web3Auth users
                                SolanaPublicKey.from(viewState.value.web3AuthSolanaPublicKey!!)
                            } else {
                                // Use decoded address for regular wallet users
                                SolanaPublicKey(Base58.decode(viewState.value.userAddress))
                            }
                            refreshBalancesAfterTransaction(userAccount, signature)

                            // Determine token symbol
                            val tokenSymbol = when (tokenMintAddress) {
                                TokenMints.EURC_DEVNET -> "EURC"
                                TokenMints.USDC_DEVNET -> "USDC"
                                else -> "Token"
                            }

                            // Create transaction details for success screen
                            val transactionDetails = TransactionDetails(
                                signature = signature,
                                amount = amount,
                                tokenSymbol = tokenSymbol,
                                recipientAddress = recipientAddress,
                                recipientName = recipientName,
                                timestamp = System.currentTimeMillis(),
                                isDevnet = true // Update this based on your network configuration
                            )

                            // Navigate to success screen instead of showing snackbar
                            _state.value.copy(
                                showTransactionSuccess = true,
                                transactionDetails = transactionDetails
                            )
                        } ?: _state.value.copy(
                            snackbarMessage = "❌ | Incorrect payload returned"
                        )
                    }

                    is TransactionResult.NoWalletFound -> {
                        _state.value.copy(
                            snackbarMessage = "❌ | No MWA compatible wallet app found. Please install Phantom, Solflare, or another compatible wallet."
                        )
                    }

                    is TransactionResult.Failure -> {
                        // Handle specific authorization errors
                        val errorMessage = when {
                            result.e is java.util.concurrent.CancellationException -> {
                                "❌ | Transaction canceled. Check wallet app or try again."
                            }

                            result.e.message?.contains("authorization request failed") == true -> {
                                "❌ | Wallet authorization failed. Try reconnecting your wallet."
                            }

                            result.e.message?.contains("User declined") == true -> {
                                "❌ | Transaction was declined by user"
                            }

                            result.e.message?.contains("Wallet not found") == true -> {
                                "❌ | Wallet app not found. Please install a compatible wallet."
                            }

                            result.e.message?.contains("insufficient") == true -> {
                                "❌ | Insufficient balance. Check your USDC and SOL balances."
                            }

                            else -> {
                                "❌ | Transaction failed: ${result.e.message}"
                            }
                        }

                        _state.value.copy(snackbarMessage = errorMessage)
                    }
                }.also { it.updateViewState() }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("authorization") == true -> {
                        "❌ | Wallet authorization error. Please reconnect your wallet."
                    }

                    e.message?.contains("ExecutionException") == true -> {
                        "❌ | Wallet communication error. Please try again or reconnect."
                    }

                    else -> {
                        "❌ | Error: ${e.message}"
                    }
                }

                _state.value.copy(
                    snackbarMessage = errorMessage
                ).updateViewState()
            }
        }
    }

    fun checkATA(
        recipientAddress: String,
        tokenMintAddress: String
    ) {
        // Validate recipient address using InputValidator
        val addressValidation = com.example.rampacashmobile.utils.InputValidator.validateWalletAddress(recipientAddress)
        if (addressValidation is Result.Failure) {
            Timber.w(TAG, "checkATA: Invalid recipient address: ${addressValidation.error.message}")
            _state.value.copy(
                snackbarMessage = ErrorHandler.getUserFriendlyMessage(addressValidation.error)
            ).updateViewState()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tokenMint = SolanaPublicKey.from(tokenMintAddress)
                val recipientPubkey = SolanaPublicKey.from(recipientAddress)

                val atAccount = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(
                    recipientPubkey, tokenMint
                )

                val toStringATA = atAccount.toString()
                Timber.d(TAG, "checkATA: Deriving ATA $toStringATA for recipient: $recipientAddress")

                // Check if the ATA exists on-chain
                Timber.d(TAG, "checkATA: Checking if ATA exists on-chain...")
                val ataExists = try {
                    AssociatedTokenAccountUtils.checkAccountExists(rpcUri, atAccount)
                } catch (e: Exception) {
                    Timber.e(TAG, "checkATA: Failed to check ATA existence: ${e.message}", e)
                    null
                }

                val existenceStatus = when (ataExists) {
                    true -> "✅ EXISTS"
                    false -> "❌ DOES NOT EXIST"
                    null -> "⚠️ UNKNOWN (check failed)"
                }

                _state.value.copy(
                    snackbarMessage = "🔗 ATA for ${recipientAddress.take(8)}...${recipientAddress.takeLast(8)}: ${toStringATA.take(8)}...${toStringATA.takeLast(8)} | $existenceStatus"
                ).updateViewState()

            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to check ATA")
                ErrorHandler.logError(error, TAG)
                _state.value.copy(
                    snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)
                ).updateViewState()
            }
        }
    }

    /**
     * Check token balance for debugging purposes
     */
    fun checkTokenBalance(tokenMintAddress: String, tokenDecimals: Int) {
        viewModelScope.launch {
            try {
                // Get the user's Solana public key (either from Web3Auth or Mobile Wallet)
                val userPublicKey = if (viewState.value.isWeb3AuthLoggedIn) {
                    // Use Web3Auth derived Solana public key
                    viewState.value.web3AuthSolanaPublicKey?.let { SolanaPublicKey.from(it) }
                } else {
                    // Use Mobile Wallet Adapter public key
                    val currentConnection = persistenceUseCase.getWalletConnection()
                    if (currentConnection is Connected) {
                        currentConnection.publicKey
                    } else null
                }

                if (userPublicKey == null) {
                    _state.value.copy(
                        snackbarMessage = "⚠️ | Please connect a wallet or login with Web3Auth first"
                    ).updateViewState()
                    return@launch
                }

                val tokenMint = SolanaPublicKey.from(tokenMintAddress)

                // Derive ATA
                Timber.d(TAG, "checkTokenBalance: Deriving ATA for ${userPublicKey.base58()}")
                val senderAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(
                    userPublicKey, tokenMint
                )
                Timber.d(TAG, "Sender ATA: $senderAta")

                // Determine token symbol based on mint address
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }

                // Get balance using the clean UseCase (same pattern as SOL balance)
                val tokenBalance = TokenAccountBalanceUseCase(rpcUri, senderAta)
                val humanReadableBalance =
                    tokenBalance.toDouble() / 10.0.pow(tokenDecimals.toDouble())

                val balanceMessage = if (tokenBalance == 0L) {
                    "💸 | No $tokenSymbol tokens! Balance: 0 (Need devnet tokens)"
                } else {
                    "💰 | $tokenSymbol Balance: $humanReadableBalance tokens"
                }

                _state.value.copy(
                    snackbarMessage = balanceMessage
                ).updateViewState()

            } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }
                _state.value.copy(
                    snackbarMessage = "💸 | $tokenSymbol ATA doesn't exist - Need to get devnet tokens first!"
                ).updateViewState()
            } catch (e: TokenAccountBalanceUseCase.TokenBalanceException) {
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }
                _state.value.copy(
                    snackbarMessage = "❌ | Could not check $tokenSymbol balance: ${e.message}"
                ).updateViewState()
            } catch (e: Exception) {
                _state.value.copy(
                    snackbarMessage = "❌ | Balance check failed: ${e.message}"
                ).updateViewState()
            }
        }
    }

    fun clearSnackBar() {
        _state.value.copy(
            snackbarMessage = null
        ).updateViewState()
    }

    /**
     * Navigate back from transaction success screen to main screen
     */
    fun onTransactionSuccessDone() {
        Timber.d(TAG, "🔙 User clicked Done - navigating back from success screen")
        _state.value.copy(
            showTransactionSuccess = false,
            transactionDetails = null
        ).updateViewState()
    }

    // Web3Auth methods
    fun setWeb3AuthLoading(loading: Boolean) {
        _state.value.copy(
            isWeb3AuthLoading = loading,
            loadingProvider = if (loading) _state.value.loadingProvider else null
        ).updateViewState()
    }

    fun setWeb3AuthProviderLoading(provider: Provider) {
        _state.value.copy(
            isWeb3AuthLoading = true,
            loadingProvider = provider
        ).updateViewState()
    }

    fun setWeb3AuthError(errorMessage: String) {
        _state.value.copy(
            isWeb3AuthLoading = false,
            loadingProvider = null,
            snackbarMessage = "❌ | $errorMessage"
        ).updateViewState()
    }

    fun handleWeb3AuthSuccess(web3AuthResponse: Web3AuthResponse, provider: Provider, solanaPublicKey: String, displayAddress: String) {
        try {
            val privateKey = web3AuthResponse.privKey
            val userInfo = web3AuthResponse.userInfo

            if (privateKey != null) {
                val providerName = when(provider) {
                    Provider.GOOGLE -> "Google"
                    Provider.FACEBOOK -> "Facebook"
                    Provider.TWITTER -> "Twitter"
                    Provider.DISCORD -> "Discord"
                    Provider.APPLE -> "Apple"
                    else -> provider.name
                }

                val displayName = userInfo?.name ?: userInfo?.email ?: "Web3Auth User"

                // Persist Web3Auth session
                try {
                    Timber.d(TAG, "💾 About to persist Web3Auth session for: $displayName")
                    persistenceUseCase.persistWeb3AuthConnection(
                        pubKey = SolanaPublicKey.from(solanaPublicKey),
                        accountLabel = displayName,
                        privateKey = privateKey,
                        providerName = providerName,
                        userInfo = userInfo?.name ?: userInfo?.email ?: ""
                    )
                    Timber.d(TAG, "✅ Web3Auth session persisted successfully")

                    // Verify persistence worked
                    val testConnection = persistenceUseCase.getWalletConnection()
                    Timber.d(TAG, "🧪 Persistence verification: ${testConnection::class.simpleName}")
                } catch (e: Exception) {
                    Timber.e(TAG, "⚠️ Failed to persist Web3Auth session: ${e.message}", e)
                    // Continue anyway - session will work for this app session
                }

                _state.value.copy(
                    isWeb3AuthLoading = false,
                    loadingProvider = null,
                    isWeb3AuthLoggedIn = true,
                    web3AuthUserInfo = displayName,
                    web3AuthPrivateKey = privateKey,
                    web3AuthSolanaPublicKey = solanaPublicKey, // Store full public key for transactions
                    canTransact = true,
                    userLabel = "$displayName (via $providerName)",
                    userAddress = displayAddress, // Use the display-friendly address for Web3Auth users
                    fullAddressForCopy = solanaPublicKey, // Full address for copy
                    snackbarMessage = "✅ | Successfully logged in with $providerName!"
                ).updateViewState()

                Timber.d(TAG, "Web3Auth login successful with $providerName - Solana address: $solanaPublicKey")

                // Load balances for Web3Auth user
                try {
                    val userPublicKey = SolanaPublicKey.from(solanaPublicKey)
                    getSolanaBalance(userPublicKey)
                    getEurcBalance(userPublicKey)
                    getUsdcBalance(userPublicKey)
                    Timber.d(TAG, "Loading balances for Web3Auth user: $solanaPublicKey")
                } catch (e: Exception) {
                    Timber.e(TAG, "Failed to load balances for Web3Auth user: ${e.message}", e)
                }
            } else {
                throw Exception("No private key received from Web3Auth")
            }
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to handle Web3Auth response", e)
            _state.value.copy(
                isWeb3AuthLoading = false,
                snackbarMessage = "❌ | Failed to process login response: ${e.message}"
            ).updateViewState()
        }
    }

    // Login is now handled directly by MainActivity
    fun loginWithWeb3Auth(provider: Provider) {
        Timber.d(TAG, "🚀 ViewModel: Web3Auth login request for provider: $provider (delegated to MainActivity)")
        // The actual login call is now in MainActivity - this method primarily exists for logging
    }

    // Handle successful logout from MainActivity
    fun handleWeb3AuthLogout() {
        // Clear persisted session
        persistenceUseCase.clearConnection()

        _state.value.copy(
            isWeb3AuthLoading = false,
            isWeb3AuthLoggedIn = false,
            web3AuthUserInfo = null,
            web3AuthPrivateKey = null,
            web3AuthSolanaPublicKey = null, // Clear the Solana public key
            canTransact = false,
            userLabel = "",
            userAddress = "",
            fullAddressForCopy = null, // Clear the full address
            solBalance = 0.0,
            eurcBalance = 0.0,
            usdcBalance = 0.0,
            snackbarMessage = "✅ | Successfully logged out from Web3Auth"
        ).updateViewState()

        Timber.d(TAG, "Web3Auth logout completed successfully")
    }

    // Handle Web3Auth session restoration on app startup
    fun handleWeb3AuthSessionRestore(privateKey: String, solanaPublicKey: String, displayAddress: String) {
        try {
            Timber.d(TAG, "🔄 Restoring Web3Auth session from Web3Auth SDK")

            _state.value.copy(
                isWeb3AuthLoading = false,
                isWeb3AuthLoggedIn = true,
                web3AuthUserInfo = "Restored User",
                web3AuthPrivateKey = privateKey,
                web3AuthSolanaPublicKey = solanaPublicKey,
                canTransact = true,
                userLabel = "Restored User (via Web3Auth)",
                userAddress = displayAddress,
                fullAddressForCopy = solanaPublicKey,
                snackbarMessage = "✅ | Web3Auth session restored!"
            ).updateViewState()

            // Load balances for restored Web3Auth user
            try {
                val userPublicKey = SolanaPublicKey.from(solanaPublicKey)
                getSolanaBalance(userPublicKey)
                getEurcBalance(userPublicKey)
                getUsdcBalance(userPublicKey)
                Timber.d(TAG, "Loading balances for restored Web3Auth user: $solanaPublicKey")
            } catch (e: Exception) {
                Timber.e(TAG, "Failed to load balances for restored Web3Auth user: ${e.message}", e)
            }

        } catch (e: Exception) {
            Timber.e(TAG, "Failed to handle Web3Auth session restoration", e)
            _state.value.copy(
                isWeb3AuthLoading = false,
                snackbarMessage = "❌ | Failed to restore Web3Auth session: ${e.message}"
            ).updateViewState()
        }
    }

    /**
     * Handle Web3Auth redirect URLs from intent data
     */
    fun handleWeb3AuthRedirect(data: Uri) {
        try {
            // Handle the redirect data without blocking
            web3AuthLazy.setResultUrl(data)
            Timber.d(TAG, "Web3Auth redirect handled: ${data}")
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to handle Web3Auth redirect: ${e.message}", e)
        }
    }

    /**
     * Handle when user cancels Web3Auth by closing the browser
     */
    fun onWeb3AuthCancelled() {
        Timber.d(TAG, "🚫 Web3Auth cancelled by user")
        _state.value.copy(
            isWeb3AuthLoading = false,
            loadingProvider = null,
            snackbarMessage = "🚫 | Authentication cancelled"
        ).updateViewState()
    }

    /**
     * Set flag for onboarding navigation (called from MainActivity)
     */
    fun setNeedsOnboardingNavigation(authProvider: String, existingEmail: String, existingPhone: String) {
        _state.value.copy(
            needsOnboardingNavigation = true,
            onboardingAuthProvider = authProvider,
            onboardingExistingEmail = existingEmail,
            onboardingExistingPhone = existingPhone
        ).updateViewState()
    }

    /**
     * Clear onboarding navigation flag
     */
    fun clearOnboardingNavigation() {
        _state.value.copy(
            needsOnboardingNavigation = false,
            onboardingAuthProvider = "",
            onboardingExistingEmail = "",
            onboardingExistingPhone = ""
        ).updateViewState()
    }

    // User data access
    val currentUser = userRepository.currentUser
    val onboardingData = userRepository.onboardingData

    /**
     * Extract user info from Web3Auth response for onboarding
     */
    fun extractUserInfoFromAuth(response: Web3AuthResponse, provider: Provider): Pair<String, String> {
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
                Pair("", "") // Phone number should be available from login process
            }
            else -> Pair("", "")
        }
    }

    /**
     * Check if user needs onboarding
     */
    fun needsOnboarding(): Boolean {
        return !userRepository.isOnboardingCompleted() &&
                (_state.value.isWeb3AuthLoggedIn || _state.value.canTransact)
    }

    /**
     * Complete user onboarding with collected information
     */
    fun completeUserOnboarding(onboardingData: com.example.rampacashmobile.data.model.OnboardingData) {
        viewModelScope.launch {
            try {
                userRepository.updateOnboardingData(onboardingData)

                // Get current wallet address and auth provider from state
                val walletAddressString = _state.value.web3AuthSolanaPublicKey ?: _state.value.userAddress
                val authProvider = getAuthProviderFromState()

                // Use value objects for type safety
                val walletAddress = WalletAddress.of(walletAddressString)
                val userId = UserId.generate() // Generate a new user ID

                // Complete onboarding and create user
                userRepository.completeOnboarding(walletAddress.value, authProvider)

                Timber.d(TAG, "✅ User onboarding completed successfully")
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to complete user onboarding")
                ErrorHandler.logError(error, TAG)
                showSnackBar(ErrorHandler.getUserFriendlyMessage(error))
            }
        }
    }

    /**
     * Get auth provider from current state
     */
    private fun getAuthProviderFromState(): String {
        return when {
            _state.value.isWeb3AuthLoggedIn -> {
                // Could be determined from the provider used for login
                // For now, return "web3auth" - can be made more specific
                "web3auth"
            }
            _state.value.canTransact -> "wallet"
            else -> "unknown"
        }
    }

    /**
     * Show snackbar message
     */
    private fun showSnackBar(message: String) {
        _state.value.copy(
            snackbarMessage = message
        ).updateViewState()
    }

    /**
     * Handle Web3Auth SPL token transfer using the actual Web3AuthSplTransferUseCase
     */
    private fun handleWeb3AuthSplTransfer(
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        viewModelScope.launch {
            try {
                val currentState = viewState.value

                // Validate Web3Auth state
                val web3AuthPrivateKey = currentState.web3AuthPrivateKey
                val web3AuthPublicKey = currentState.web3AuthSolanaPublicKey

                if (web3AuthPrivateKey == null || web3AuthPublicKey == null) {
                    _state.value.copy(
                        snackbarMessage = "❌ | Web3Auth session invalid. Please login again."
                    ).updateViewState()
                    return@launch
                }

                // Show which implementation is being used
                if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                    Timber.d(TAG, "🔑 Using Web3Auth SPL Transfer (local signing)")
                }

                _state.value.copy(
                    snackbarMessage = "🔄 | Processing Web3Auth SPL transfer..."
                ).updateViewState()

                // Parse and validate inputs
                val fromWallet = SolanaPublicKey.from(web3AuthPublicKey)
                val recipientPubkey = SolanaPublicKey.from(recipientAddress)
                val tokenMint = SolanaPublicKey.from(tokenMintAddress)

                // Convert amount based on token decimals
                val amountDouble = amount.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Invalid amount: $amount")
                val multiplier = 10.0.pow(tokenDecimals.toDouble())
                val amountInTokenUnits = (amountDouble * multiplier).toLong()

                Timber.d(TAG, "Web3Auth SPL Transfer Details:")
                Timber.d(TAG, "From: ${fromWallet.base58()}")
                Timber.d(TAG, "To: ${recipientPubkey.base58()}")
                Timber.d(TAG, "Mint: ${tokenMint.base58()}")
                Timber.d(TAG, "Amount: $amountInTokenUnits ($amount tokens)")

                // Execute Web3Auth transfer
                val signature = Web3AuthSplTransferUseCase.transfer(
                    rpcUri = rpcUri,
                    web3AuthPrivateKey = web3AuthPrivateKey,
                    fromWallet = fromWallet,
                    toWallet = recipientPubkey,
                    mint = tokenMint,
                    amount = amountInTokenUnits
                )

                Timber.d(TAG, "✅ Web3Auth SPL transfer successful: $signature")

                // Determine token symbol
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }

                // Create transaction details for success screen
                val transactionDetails = TransactionDetails(
                    signature = signature,
                    amount = amount,
                    tokenSymbol = tokenSymbol,
                    recipientAddress = recipientAddress,
                    recipientName = recipientName,
                    timestamp = System.currentTimeMillis(),
                    isDevnet = true // Update this based on your network configuration
                )

                // Navigate to success screen (same as MWA wallet flow)
                Timber.d(TAG, "🎯 Setting showTransactionSuccess = true for Web3Auth transfer")
                Timber.d(TAG, "Transaction details: signature=${signature.take(8)}, amount=$amount, token=$tokenSymbol")

                _state.value.copy(
                    showTransactionSuccess = true,
                    transactionDetails = transactionDetails
                ).updateViewState()

                // Refresh balances after successful transfer (with delay for blockchain confirmation)
                refreshBalancesAfterTransaction(fromWallet, signature)

                Timber.d(TAG, "🎯 Web3Auth SPL transfer completed successfully - showing success screen")

            } catch (e: Exception) {
                Timber.e(TAG, "❌ Web3Auth SPL transfer failed: ${e.message}", e)
                _state.value.copy(
                    snackbarMessage = "❌ | Transfer failed: ${e.message}"
                ).updateViewState()
            }
        }
    }
}
