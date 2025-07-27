package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.R
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.usecase.SplTokenTransferUseCase
import com.example.rampacashmobile.usecase.ManualSplTokenTransferUseCase
import com.example.rampacashmobile.usecase.Web3AuthSplTransferUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.example.rampacashmobile.usecase.TransferConfig
import com.example.rampacashmobile.ui.screens.TransactionDetails
import com.funkatronics.encoders.Base58
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import com.solana.publickey.SolanaPublicKey
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.LoginParams
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.math.pow

data class MainViewState(
    val isLoading: Boolean = false,
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
    val isWeb3AuthLoggedIn: Boolean = false,
    val web3AuthUserInfo: String? = null,
    val web3AuthPrivateKey: String? = null,
    val web3AuthSolanaPublicKey: String? = null // Full Solana public key for transactions
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val rpcUri = BuildConfig.RPC_URI.toUri()

    companion object {
        private const val TAG = "MainViewModel"
    }

    // Lazy Web3Auth initialization to prevent blocking during app startup
    private val web3AuthLazy: Web3Auth by lazy {
        Log.d(TAG, "🔧 Creating Web3Auth instance...")
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
                Log.d(TAG, "✅ Web3Auth instance created successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create Web3Auth instance: ${e.message}", e)
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
        val persistedConnection = persistenceUseCase.getWalletConnection()

        if (persistedConnection is Connected) {
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
                // TODO: Move all Snackbar message strings into resources
                snackbarMessage = "✅ | Successfully auto-connected to: \n" + persistedConnection.publicKey.base58() + "."
            ).updateViewState()

            // Set the auth token in walletAdapter
            walletAdapter.authToken = persistedConnection.authToken
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

                    persistenceUseCase.persistConnection(
                        currentConn.publicKey, currentConn.accountLabel, currentConn.authToken
                    )

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
            val conn = persistenceUseCase.getWalletConnection()
            if (conn is Connected) {
                persistenceUseCase.clearConnection()

                MainViewState().copy(
                    snackbarMessage = "✅ | Disconnected from wallet."
                ).updateViewState()
            }
        }
    }

    fun getSolanaBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = AccountBalanceUseCase(rpcUri, account)

                _state.value.copy(
                    solBalance = result / 1000000000.0
                ).updateViewState()
            } catch (e: Exception) {
                _state.value.copy(
                    snackbarMessage = "❌ | Failed fetching account balance."
                ).updateViewState()
            }
        }
    }

    /**
     * Refresh balances after a successful transaction with proper timing and retry logic
     */
    private fun refreshBalancesAfterTransaction(account: SolanaPublicKey, signature: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Refreshing balances after transaction: ${signature.take(8)}...")
                Log.d(TAG, "🔄 Account for balance refresh: ${account.base58()}")
                
                // Wait longer for transaction confirmation (blockchain updates can be slow)
                Log.d(TAG, "⏳ Waiting 4 seconds for blockchain confirmation...")
                delay(4000) // 4 seconds initial delay (increased from 2)
                
                // Refresh SOL balance immediately (usually faster to update)
                getSolanaBalance(account)
                
                // Refresh token balances with retry logic (these can take longer)
                Log.d(TAG, "🔄 Starting token balance refresh with retries...")
                refreshTokenBalanceWithRetry(account, "EURC")
                refreshTokenBalanceWithRetry(account, "USDC")
                
                Log.d(TAG, "✅ Balance refresh completed for transaction ${signature.take(8)}")
                Log.d(TAG, "🎯 Current state after balance refresh - showTransactionSuccess: ${viewState.value.showTransactionSuccess}")
                Log.d(TAG, "💰 Final balances - EURC: ${viewState.value.eurcBalance}, USDC: ${viewState.value.usdcBalance}")
                
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Balance refresh failed for transaction ${signature.take(8)}: ${e.message}")
                // Don't update UI state on refresh failure - keep existing balances
            }
        }
    }
    
    /**
     * Refresh token balance with retry logic and graceful error handling
     */
    private fun refreshTokenBalanceWithRetry(account: SolanaPublicKey, tokenSymbol: String) {
        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 5 // Increased from 3 to 5 attempts
            
            while (attempts < maxAttempts) {
                try {
                    delay(attempts * 2000L) // 0s, 2s, 4s, 6s, 8s delays (longer delays)
                    
                    Log.d(TAG, "🔍 Fetching $tokenSymbol balance (attempt ${attempts + 1}/$maxAttempts)...")
                    Log.d(TAG, "🔍 Account: ${account.base58()}")
                    
                    when (tokenSymbol) {
                        "EURC" -> getEurcBalanceRobust(account)
                        "USDC" -> getUsdcBalanceRobust(account)
                    }
                    
                    Log.d(TAG, "✅ $tokenSymbol balance refreshed successfully")
                    return@launch // Success - exit retry loop
                    
                } catch (e: Exception) {
                    attempts++
                    Log.w(TAG, "⚠️ $tokenSymbol balance fetch attempt $attempts failed: ${e.message}")
                    
                    if (attempts >= maxAttempts) {
                        Log.e(TAG, "❌ $tokenSymbol balance refresh failed after $maxAttempts attempts")
                        // Don't reset balance to 0 - keep existing value
                    } else {
                        Log.d(TAG, "🔄 Will retry $tokenSymbol balance fetch in ${(attempts * 2)} seconds...")
                    }
                }
            }
        }
    }

    fun getEurcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
                val eurcAta =
                    AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(account, eurcMint)

                // 1. Check if the ATA exists
                val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, eurcAta)
                if (!exists) {
                    _state.value.copy(
                        eurcBalance = 0.0, snackbarMessage = "💸 | EURC account does not exist yet."
                    ).updateViewState()
                    return@launch
                }

                // 2. If it exists, fetch the balance
                val tokenBalance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
                val humanReadableBalance = tokenBalance.toDouble() / 10.0.pow(6.0)

                _state.value.copy(
                    eurcBalance = humanReadableBalance
                ).updateViewState()

            } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
                _state.value.copy(
                    eurcBalance = 0.0
                ).updateViewState()
            } catch (e: Exception) {
                _state.value.copy(
                    eurcBalance = 0.0, snackbarMessage = "❌ | Failed fetching EURC balance."
                ).updateViewState()
            }
        }
    }

    fun getUsdcBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
                val usdcAta =
                    AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(account, usdcMint)

                // 1. Check if the ATA exists
                val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, usdcAta)
                if (!exists) {
                    _state.value.copy(
                        usdcBalance = 0.0
                    ).updateViewState()
                    return@launch
                }

                // 2. If it exists, fetch the balance
                val tokenBalance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
                val humanReadableBalance = tokenBalance.toDouble() / 10.0.pow(6.0)

                _state.value.copy(
                    usdcBalance = humanReadableBalance
                ).updateViewState()

            } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
                _state.value.copy(
                    usdcBalance = 0.0
                ).updateViewState()
            } catch (e: Exception) {
                _state.value.copy(
                    usdcBalance = 0.0
                ).updateViewState()
            }
        }
    }
    
    /**
     * Robust EURC balance fetching that preserves existing balance on errors
     */
    private suspend fun getEurcBalanceRobust(account: SolanaPublicKey) {
        try {
            val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
            val eurcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(account, eurcMint)
            
            Log.d(TAG, "💰 Fetching EURC balance for account: ${account.base58()}")
            Log.d(TAG, "💰 EURC ATA: ${eurcAta.base58()}")

            // Check if the ATA exists
            val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, eurcAta)
            if (!exists) {
                Log.d(TAG, "💰 EURC ATA does not exist - setting balance to 0")
                // Only update if we're sure the account doesn't exist
                _state.value.copy(
                    eurcBalance = 0.0
                ).updateViewState()
                return
            }

            // Fetch the balance
            val tokenBalance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
            val humanReadableBalance = tokenBalance.toDouble() / 10.0.pow(6.0)
            
            Log.d(TAG, "💰 EURC raw balance: $tokenBalance, human readable: $humanReadableBalance")
            Log.d(TAG, "💰 Previous EURC balance: ${viewState.value.eurcBalance}")

            _state.value.copy(
                eurcBalance = humanReadableBalance
            ).updateViewState()
            
            Log.d(TAG, "💰 EURC balance updated to: $humanReadableBalance")

        } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
            Log.d(TAG, "💰 EURC account not found - setting balance to 0")
            // Account doesn't exist - set to 0
            _state.value.copy(
                eurcBalance = 0.0
            ).updateViewState()
        } catch (e: Exception) {
            // Temporary error - keep existing balance, don't reset to 0
            Log.w(TAG, "💰 EURC balance fetch failed (keeping existing): ${e.message}")
            throw e // Re-throw for retry logic
        }
    }
    
    /**
     * Robust USDC balance fetching that preserves existing balance on errors
     */
    private suspend fun getUsdcBalanceRobust(account: SolanaPublicKey) {
        try {
            val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
            val usdcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(account, usdcMint)

            // Check if the ATA exists
            val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, usdcAta)
            if (!exists) {
                // Only update if we're sure the account doesn't exist
                _state.value.copy(
                    usdcBalance = 0.0
                ).updateViewState()
                return
            }

            // Fetch the balance
            val tokenBalance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
            val humanReadableBalance = tokenBalance.toDouble() / 10.0.pow(6.0)

            _state.value.copy(
                usdcBalance = humanReadableBalance
            ).updateViewState()

        } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
            // Account doesn't exist - set to 0
            _state.value.copy(
                usdcBalance = 0.0
            ).updateViewState()
        } catch (e: Exception) {
            // Temporary error - keep existing balance, don't reset to 0
            Log.w(TAG, "USDC balance fetch failed (keeping existing): ${e.message}")
            throw e // Re-throw for retry logic
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
        tokenDecimals: Int = 6
    ) {
        viewModelScope.launch {
            try {
                // Check if user is logged in via Web3Auth
                if (viewState.value.isWeb3AuthLoggedIn) {
                    handleWeb3AuthSplTransfer(recipientAddress, amount, tokenMintAddress, tokenDecimals)
                    return@launch
                }
                
                // Show which implementation is being used
                if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                    Log.d(TAG, "${TransferConfig.getImplementationEmoji()} Using ${TransferConfig.getImplementationName()}")
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
                            Log.d(TAG, "🔧 Building transaction manually (bypasses web3-solana bugs)")
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
                            Log.e(TAG, "❌ Manual transfer failed: ${e.message}", e)
                            throw RuntimeException("Manual transfer failed: ${e.message}", e)
                        }
                        
                        signAndSendTransactions(arrayOf(transactionBytes))
                    } else {
                        if (TransferConfig.ENABLE_TRANSFER_LOGGING) {
                            Log.d(TAG, "📚 Using web3-solana library transaction building")
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
        // Validate recipient address before proceeding
        if (recipientAddress.isBlank()) {
            Log.w(TAG, "checkATA: Recipient address is blank")
            _state.value.copy(
                snackbarMessage = "⚠️ | Please enter a valid recipient address"
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
                Log.d(TAG, "checkATA: Deriving ATA $toStringATA for recipient: $recipientAddress")

                // Check if the ATA exists on-chain
                Log.d(TAG, "checkATA: Checking if ATA exists on-chain...")
                val ataExists = try {
                    AssociatedTokenAccountUtils.checkAccountExists(rpcUri, atAccount)
                } catch (e: Exception) {
                    Log.e(TAG, "checkATA: Failed to check ATA existence: ${e.message}", e)
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
                Log.e(TAG, "checkATA: Failed to derive ATA for recipient: $recipientAddress", e)
                _state.value.copy(
                    snackbarMessage = "❌ | Invalid recipient address: ${e.message}"
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
                Log.d(TAG, "checkTokenBalance: Deriving ATA for ${userPublicKey.base58()}")
                val senderAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(
                    userPublicKey, tokenMint
                )
                Log.d(TAG, "Sender ATA: $senderAta")

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
        Log.d(TAG, "🔙 User clicked Done - navigating back from success screen")
        _state.value.copy(
            showTransactionSuccess = false,
            transactionDetails = null
        ).updateViewState()
    }

    // Web3Auth methods
    fun setWeb3AuthLoading(loading: Boolean) {
        _state.value.copy(
            isWeb3AuthLoading = loading
        ).updateViewState()
    }

    fun setWeb3AuthError(errorMessage: String) {
        _state.value.copy(
            isWeb3AuthLoading = false,
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
                
                _state.value.copy(
                    isWeb3AuthLoading = false,
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
                
                Log.d(TAG, "Web3Auth login successful with $providerName - Solana address: $solanaPublicKey")
                
                // Load balances for Web3Auth user
                try {
                    val userPublicKey = SolanaPublicKey.from(solanaPublicKey)
                    getSolanaBalance(userPublicKey)
                    getEurcBalance(userPublicKey)
                    getUsdcBalance(userPublicKey)
                    Log.d(TAG, "Loading balances for Web3Auth user: $solanaPublicKey")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load balances for Web3Auth user: ${e.message}", e)
                }
            } else {
                throw Exception("No private key received from Web3Auth")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle Web3Auth response", e)
            _state.value.copy(
                isWeb3AuthLoading = false,
                snackbarMessage = "❌ | Failed to process login response: ${e.message}"
            ).updateViewState()
        }
    }

    // Login is now handled directly by MainActivity
    fun loginWithWeb3Auth(provider: Provider) {
        Log.d(TAG, "🚀 ViewModel: Web3Auth login request for provider: $provider (delegated to MainActivity)")
        // The actual login call is now in MainActivity - this method primarily exists for logging
    }

    // Handle successful logout from MainActivity
    fun handleWeb3AuthLogout() {
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
        
        Log.d(TAG, "Web3Auth logout completed successfully")
    }



    /**
     * Handle Web3Auth redirect URLs from intent data
     */
    fun handleWeb3AuthRedirect(data: Uri) {
        try {
            // Handle the redirect data without blocking
            web3AuthLazy.setResultUrl(data)
            Log.d(TAG, "Web3Auth redirect handled: ${data}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle Web3Auth redirect: ${e.message}", e)
        }
    }

    /**
     * Handle when user cancels Web3Auth by closing the browser
     */
    fun onWeb3AuthCancelled() {
        Log.d(TAG, "🚫 Web3Auth cancelled by user")
        _state.value.copy(
            isWeb3AuthLoading = false,
            snackbarMessage = "🚫 | Authentication cancelled"
        ).updateViewState()
    }

    /**
     * Handle SPL token transfer for Web3Auth users
     */
    private fun handleWeb3AuthSplTransfer(
        recipientAddress: String,
        amount: String,
        tokenMintAddress: String,
        tokenDecimals: Int = 6
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
                    Log.d(TAG, "🔑 Using Web3Auth SPL Transfer (local signing)")
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

                Log.d(TAG, "Web3Auth SPL Transfer Details:")
                Log.d(TAG, "From: ${fromWallet.base58()}")
                Log.d(TAG, "To: ${recipientPubkey.base58()}")
                Log.d(TAG, "Mint: ${tokenMint.base58()}")
                Log.d(TAG, "Amount: $amountInTokenUnits ($amount tokens)")

                // Execute Web3Auth transfer
                val signature = Web3AuthSplTransferUseCase.transfer(
                    rpcUri = rpcUri,
                    web3AuthPrivateKey = web3AuthPrivateKey,
                    fromWallet = fromWallet,
                    toWallet = recipientPubkey,
                    mint = tokenMint,
                    amount = amountInTokenUnits
                )

                Log.d(TAG, "✅ Web3Auth SPL transfer successful: $signature")

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
                    timestamp = System.currentTimeMillis(),
                    isDevnet = true // Update this based on your network configuration
                )

                // Navigate to success screen (same as MWA wallet flow)
                Log.d(TAG, "🎯 Setting showTransactionSuccess = true for Web3Auth transfer")
                Log.d(TAG, "Transaction details: signature=${signature.take(8)}, amount=$amount, token=$tokenSymbol")
                
                _state.value.copy(
                    showTransactionSuccess = true,
                    transactionDetails = transactionDetails
                ).updateViewState()

                // Refresh balances after successful transfer (with delay for blockchain confirmation)
                refreshBalancesAfterTransaction(fromWallet, signature)
                
                Log.d(TAG, "🎯 Web3Auth SPL transfer completed successfully - showing success screen")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Web3Auth SPL transfer failed: ${e.message}", e)
                _state.value.copy(
                    snackbarMessage = "❌ | Transfer failed: ${e.message}"
                ).updateViewState()
            }
        }
    }
}