package com.example.rampacashmobile.viewmodel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.usecase.SplTokenTransferUseCase
import com.example.rampacashmobile.usecase.ManualSplTokenTransferUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.example.rampacashmobile.usecase.TransferConfig
import com.funkatronics.encoders.Base58
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import com.solana.publickey.SolanaPublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val walletFound: Boolean = true,
    val memoTxSignature: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase
) : ViewModel() {
    private val rpcUri = BuildConfig.RPC_URI.toUri()

    companion object {
        private const val TAG = "MainViewModel"
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
                        userLabel = currentConn.accountLabel
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

                            // Refresh balances after successful transfer
                            val userAccount =
                                SolanaPublicKey(Base58.decode(viewState.value.userAddress))
                            getSolanaBalance(userAccount)
                            getEurcBalance(userAccount)
                            getUsdcBalance(userAccount)

                            _state.value.copy(
                                snackbarMessage = "✅ | Token transfer successful: $signature"
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

        val tokenMint = SolanaPublicKey.from(tokenMintAddress)
        val recipientPubkey = SolanaPublicKey.from(recipientAddress)

        val atAccount = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(
            recipientPubkey, tokenMint
        )

        val toStringATA = atAccount.toString()
        Log.d(TAG, "checkTokenBalance: Deriving ATA $toStringATA")

        _state.value.copy(
            snackbarMessage = "⚠️ | Ata for recipient: $recipientAddress is $toStringATA"
        ).updateViewState()

        return
    }



    /**
     * Check token balance for debugging purposes
     */
    fun checkTokenBalance(tokenMintAddress: String, tokenDecimals: Int) {
        viewModelScope.launch {
            try {
                val currentConnection = persistenceUseCase.getWalletConnection()
                if (currentConnection !is Connected) {
                    _state.value.copy(
                        snackbarMessage = "⚠️ | Connect wallet first to check balance"
                    ).updateViewState()
                    return@launch
                }

                val ownerAccount = SolanaPublicKey(Base58.decode(viewState.value.userAddress))
                val tokenMint = SolanaPublicKey.from(tokenMintAddress)

                // Derive ATA
                Log.d(TAG, "checkTokenBalance: Deriving ATA")
                val senderAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(
                    ownerAccount, tokenMint
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
}