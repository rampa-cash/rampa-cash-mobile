package com.example.rampacashmobile.viewmodel

import android.content.Context
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.Web3AuthConnected
import com.example.rampacashmobile.usecase.NotConnected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.DebugSessionHelper
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import com.solana.publickey.SolanaPublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for wallet connection and balance management
 * Handles MWA connections, balance fetching, and connection state
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase,
    private val walletDomainService: WalletDomainService,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    companion object {
        private const val TAG = "ConnectionViewModel"
    }

    private val rpcUri = android.net.Uri.parse(BuildConfig.RPC_URI)

    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Track if this is the first time loading connections
    private var isInitialLoad = true

    /**
     * Load existing connection from persistence
     */
    fun loadConnection() {
        Timber.d("🔄 loadConnection() called - checking for persisted sessions...")

        // Debug current session storage state
        DebugSessionHelper.debugAllStoredSessions(
            context.getSharedPreferences("scaffold_prefs", android.content.Context.MODE_PRIVATE)
        )

        // Set loading state while checking for sessions
        _connectionState.update { it.copy(isLoading = true) }

        val persistedConnection = persistenceUseCase.getWalletConnection()
        Timber.d("🔍 Persistence check result: ${persistedConnection::class.simpleName}")

        when (persistedConnection) {
            is Connected -> {
                Timber.d("🔄 Restoring MWA session: ${persistedConnection.accountLabel}")
                _connectionState.update { 
                    it.copy(
                        isLoading = true,
                        canTransact = true,
                        userAddress = persistedConnection.publicKey.base58(),
                        userLabel = persistedConnection.accountLabel,
                        fullAddressForCopy = persistedConnection.publicKey.base58(),
                        connectionType = "MWA"
                    )
                }

                // Load balances
                loadBalances(persistedConnection.publicKey)

                _connectionState.update { 
                    it.copy(
                        isLoading = false,
                        snackbarMessage = if (isInitialLoad) "✅ | Successfully auto-connected to MWA wallet: ${persistedConnection.accountLabel}" else null
                    )
                }

                // Set the auth token in walletAdapter
                walletAdapter.authToken = persistedConnection.authToken
            }

            is Web3AuthConnected -> {
                Timber.d("🔄 Restoring Web3Auth session: ${persistedConnection.accountLabel}")

                // Create display-friendly address
                val fullAddress = persistedConnection.publicKey.base58()
                val displayAddress = "${fullAddress.take(8)}...${fullAddress.takeLast(8)}"

                _connectionState.update { 
                    it.copy(
                        isLoading = true,
                        isWeb3AuthLoggedIn = true,
                        userInfo = persistedConnection.accountLabel,
                        privateKey = persistedConnection.privateKey,
                        solanaPublicKey = fullAddress,
                        canTransact = true,
                        userLabel = "${persistedConnection.accountLabel} (via ${persistedConnection.providerName})",
                        userAddress = displayAddress,
                        fullAddressForCopy = fullAddress,
                        connectionType = "Web3Auth"
                    )
                }

                // Load balances
                loadBalances(persistedConnection.publicKey)

                _connectionState.update { 
                    it.copy(
                        isLoading = false,
                        snackbarMessage = if (isInitialLoad) "✅ | Successfully auto-connected to Web3Auth: ${persistedConnection.accountLabel}" else null
                    )
                }
            }

            is NotConnected -> {
                Timber.d("🔄 No persisted session found - setting loading to false")
                _connectionState.update { it.copy(isLoading = false) }
            }
        }

        // Mark initial load as complete
        isInitialLoad = false
    }

    /**
     * Connect to wallet via MWA
     */
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
                    
                    _connectionState.update { 
                        it.copy(
                            isLoading = true,
                            userAddress = currentConn.publicKey.base58(),
                            userLabel = currentConn.accountLabel,
                            fullAddressForCopy = currentConn.publicKey.base58(),
                            connectionType = "MWA"
                        )
                    }

                    // Load balances
                    loadBalances(currentConn.publicKey)

                    _connectionState.update { 
                        it.copy(
                            isLoading = false,
                            canTransact = true,
                            snackbarMessage = "✅ | Successfully connected to: \n" + currentConn.publicKey.base58() + "."
                        )
                    }
                }

                is TransactionResult.NoWalletFound -> {
                    _connectionState.update { 
                        it.copy(
                            walletFound = false, 
                            snackbarMessage = "❌ | No wallet found."
                        )
                    }
                }

                is TransactionResult.Failure -> {
                    _connectionState.update { 
                        it.copy(
                            isLoading = false,
                            canTransact = false,
                            userAddress = "",
                            userLabel = "",
                            fullAddressForCopy = null,
                            snackbarMessage = "❌ | Failed connecting to wallet: " + result.e.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Disconnect from current wallet
     */
    fun disconnect() {
        viewModelScope.launch {
            val conn = persistenceUseCase.getWalletConnection()
            when (conn) {
                is Connected -> {
                    persistenceUseCase.clearConnection()
                    resetToDisconnectedState("✅ | Disconnected from MWA wallet.")
                }
                is Web3AuthConnected -> {
                    persistenceUseCase.clearConnection()
                    resetToDisconnectedState("✅ | Disconnected from Web3Auth.")
                }
                is NotConnected -> {
                    resetToDisconnectedState("ℹ️ | No active connection to disconnect.")
                }
            }
        }
    }

    /**
     * Load balances for a given account
     */
    fun loadBalances(account: SolanaPublicKey) {
        getSolanaBalance(account)
        getEurcBalance(account)
        getUsdcBalance(account)
    }

    /**
     * Get SOL balance for a given account
     */
    fun getSolanaBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getSolBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _connectionState.update { it.copy(solBalance = money.amount.toDouble()) }
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)) }
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get SOL balance")
                ErrorHandler.logError(error, TAG)
                _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)) }
            }
        }
    }

    /**
     * Get EURC balance for a given account
     */
    fun getEurcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getEurcBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _connectionState.update { it.copy(eurcBalance = money.amount.toDouble()) }
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)) }
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get EURC balance")
                ErrorHandler.logError(error, TAG)
                _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)) }
            }
        }
    }

    /**
     * Get USDC balance for a given account
     */
    fun getUsdcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.getUsdcBalance(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val money = result.data
                        _connectionState.update { it.copy(usdcBalance = money.amount.toDouble()) }
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)) }
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to get USDC balance")
                ErrorHandler.logError(error, TAG)
                _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)) }
            }
        }
    }

    /**
     * Refresh balances after a successful transaction
     */
    fun refreshBalancesAfterTransaction(account: SolanaPublicKey, signature: String) {
        viewModelScope.launch {
            try {
                Timber.d(TAG, "🔄 Refreshing balances after transaction: ${signature.take(8)}...")
                Timber.d(TAG, "🔄 Account for balance refresh: ${account.base58()}")

                // Wait longer for transaction confirmation (blockchain updates can be slow)
                Timber.d(TAG, "⏳ Waiting 4 seconds for blockchain confirmation...")
                delay(4000) // 4 seconds initial delay

                val walletAddress = WalletAddress.of(account)
                val result = walletDomainService.loadWalletBalances(walletAddress)
                
                when (result) {
                    is Result.Success -> {
                        val wallet = result.data
                        // Update individual balances
                        val solResult = walletDomainService.getSolBalance(walletAddress)
                        val eurcResult = walletDomainService.getEurcBalance(walletAddress)
                        val usdcResult = walletDomainService.getUsdcBalance(walletAddress)
                        
                        if (solResult is Result.Success) {
                            _connectionState.update { it.copy(solBalance = solResult.data.amount.toDouble()) }
                        }
                        if (eurcResult is Result.Success) {
                            _connectionState.update { it.copy(eurcBalance = eurcResult.data.amount.toDouble()) }
                        }
                        if (usdcResult is Result.Success) {
                            _connectionState.update { it.copy(usdcBalance = usdcResult.data.amount.toDouble()) }
                        }
                        Timber.d(TAG, "✅ Balances refreshed successfully after transaction")
                    }
                    is Result.Failure -> {
                        ErrorHandler.logError(result.error, TAG)
                        _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(result.error)) }
                    }
                }
            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to refresh balances after transaction")
                ErrorHandler.logError(error, TAG)
                _connectionState.update { it.copy(snackbarMessage = ErrorHandler.getUserFriendlyMessage(error)) }
            }
        }
    }

    /**
     * Clear snackbar message
     */
    fun clearSnackBar() {
        _connectionState.update { it.copy(snackbarMessage = null) }
    }

    /**
     * Clear error state
     */
    override fun clearError() {
        logErrorClearing("ConnectionViewModel")
        clearErrorInState(_connectionState) { it.copy(error = null) }
    }

    /**
     * Reset to disconnected state
     */
    private fun resetToDisconnectedState(message: String) {
        _connectionState.update { 
            it.copy(
                isLoading = true, // Keep loading during logout transition
                canTransact = false,
                isWeb3AuthLoggedIn = false,
                userAddress = "",
                userLabel = "",
                fullAddressForCopy = "",
                userInfo = null,
                privateKey = null,
                solanaPublicKey = null,
                connectionType = null,
                // Clear balances
                solBalance = 0.0,
                usdcBalance = 0.0,
                eurcBalance = 0.0,
                snackbarMessage = message
            )
        }
    }
}

/**
 * Connection state data class
 */
data class ConnectionState(
    val isLoading: Boolean = true,
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val eurcBalance: Double = 0.0,
    val usdcBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
    val fullAddressForCopy: String? = null,
    val walletFound: Boolean = true,
    val snackbarMessage: String? = null,
    // Web3Auth related state
    val isWeb3AuthLoggedIn: Boolean = false,
    val userInfo: String? = null,
    val privateKey: String? = null,
    val solanaPublicKey: String? = null,
    val connectionType: String? = null, // "MWA" or "Web3Auth"
    val error: String? = null
)
