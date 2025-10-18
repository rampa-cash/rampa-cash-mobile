package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.net.Uri
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.R
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.constants.AppConstants
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.usecase.Web3AuthSplTransferUseCase
import com.example.rampacashmobile.usecase.TransferConfig
import com.example.rampacashmobile.ui.screens.TransactionDetails
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

/**
 * ViewModel responsible for Web3Auth operations
 * Handles login, logout, session management, and Web3Auth-specific transfers
 */
@HiltViewModel
class Web3AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val persistenceUseCase: PersistenceUseCase,
    private val walletDomainService: WalletDomainService
) : BaseViewModel() {

    companion object {
        private const val TAG = "Web3AuthViewModel"
    }

    private val rpcUri = android.net.Uri.parse(BuildConfig.RPC_URI)

    // Web3Auth state
    private val _web3AuthState = MutableStateFlow(Web3AuthState())
    val web3AuthState: StateFlow<Web3AuthState> = _web3AuthState

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

    /**
     * Set Web3Auth loading state
     */
    fun setWeb3AuthLoading(loading: Boolean) {
        _web3AuthState.update { 
            it.copy(
                isLoading = loading,
                loadingProvider = if (loading) _web3AuthState.value.loadingProvider else null
            )
        }
    }

    /**
     * Set specific provider loading state
     */
    fun setWeb3AuthProviderLoading(provider: Provider) {
        _web3AuthState.update { 
            it.copy(
                isLoading = true,
                loadingProvider = provider
            )
        }
    }

    /**
     * Set Web3Auth error state
     */
    fun setWeb3AuthError(errorMessage: String) {
        _web3AuthState.update { 
            it.copy(
                isLoading = false,
                loadingProvider = null,
                error = errorMessage
            )
        }
    }

    /**
     * Handle successful Web3Auth login
     */

    /**
     * Handle Web3Auth logout
     */
    fun handleWeb3AuthLogout() {
        // Clear persisted session
        persistenceUseCase.clearConnection()

        _web3AuthState.update { 
            it.copy(
                isLoading = false,
                isLoggedIn = false,
                userInfo = null,
                privateKey = null,
                solanaPublicKey = null,
                providerName = null,
                displayAddress = null,
                error = null
            )
        }

        Timber.d(TAG, "Web3Auth logout completed successfully")
    }

    /**
     * Handle Web3Auth session restoration
     */
    fun handleWeb3AuthSessionRestore(
        privateKey: String, 
        solanaPublicKey: String, 
        displayAddress: String
    ) {
        try {
            Timber.d(TAG, "🔄 Restoring Web3Auth session from Web3Auth SDK")

            _web3AuthState.update { 
                it.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userInfo = "Restored User",
                    privateKey = privateKey,
                    solanaPublicKey = solanaPublicKey,
                    providerName = "Web3Auth",
                    displayAddress = displayAddress,
                    error = null
                )
            }

        } catch (e: Exception) {
            Timber.e(TAG, "Failed to handle Web3Auth session restoration", e)
            _web3AuthState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to restore Web3Auth session: ${e.message}"
                )
            }
        }
    }

    /**
     * Handle Web3Auth redirect URLs
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
     * Handle when user cancels Web3Auth
     */
    fun onWeb3AuthCancelled() {
        Timber.d(TAG, "🚫 Web3Auth cancelled by user")
        _web3AuthState.update { 
            it.copy(
                isLoading = false,
                loadingProvider = null,
                error = "Authentication cancelled"
            )
        }
    }

    /**
     * Handle Web3Auth SPL token transfer
     */
    fun handleWeb3AuthSplTransfer(
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6,
        recipientName: String? = null
    ) {
        viewModelScope.launch {
            try {
                val currentState = _web3AuthState.value

                // Validate Web3Auth state
                val web3AuthPrivateKey = currentState.privateKey
                val web3AuthPublicKey = currentState.solanaPublicKey

                if (web3AuthPrivateKey == null || web3AuthPublicKey == null) {
                    _web3AuthState.update { 
                        it.copy(error = "Web3Auth session invalid. Please login again.")
                    }
                    return@launch
                }

                // Show which implementation is being used
                if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                    Timber.d(TAG, "🔑 Using Web3Auth SPL Transfer (local signing)")
                }

                _web3AuthState.update { 
                    it.copy(
                        isTransferring = true,
                        error = null
                    )
                }

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

                _web3AuthState.update { 
                    it.copy(
                        isTransferring = false,
                        lastTransactionDetails = transactionDetails,
                        error = null
                    )
                }

                Timber.d(TAG, "🎯 Web3Auth SPL transfer completed successfully")

            } catch (e: Exception) {
                Timber.e(TAG, "❌ Web3Auth SPL transfer failed: ${e.message}", e)
                _web3AuthState.update { 
                    it.copy(
                        isTransferring = false,
                        error = "Transfer failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Clear error state
     */
    override fun clearError() {
        logErrorClearing("Web3AuthViewModel")
        clearErrorInState(_web3AuthState) { it.copy(error = null) }
    }

    /**
     * Clear last transaction details
     */
    fun clearLastTransaction() {
        _web3AuthState.update { it.copy(lastTransactionDetails = null) }
    }
}

/**
 * Web3Auth state data class
 */
data class Web3AuthState(
    val isLoading: Boolean = false,
    val loadingProvider: Provider? = null,
    val isLoggedIn: Boolean = false,
    val userInfo: String? = null,
    val privateKey: String? = null,
    val solanaPublicKey: String? = null,
    val providerName: String? = null,
    val displayAddress: String? = null,
    val isTransferring: Boolean = false,
    val lastTransactionDetails: TransactionDetails? = null,
    val error: String? = null
)
