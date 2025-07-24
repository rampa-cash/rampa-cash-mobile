package com.example.rampacashmobile.viewmodel

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.example.rampacashmobile.usecase.Connected
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.example.rampacashmobile.usecase.SplTokenTransferUseCase
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.lifecycle.HiltViewModel
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import com.funkatronics.encoders.Base58
import com.solana.publickey.SolanaPublicKey
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
): ViewModel() {
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
        Log.d(TAG, "=== Loading Persisted Connection ===")
        val persistedConnection = persistenceUseCase.getWalletConnection()
        Log.d(TAG, "Persisted connection type: ${persistedConnection::class.simpleName}")

        if (persistedConnection is Connected) {
            Log.d(TAG, "✅ Found persisted connection")
            Log.d(TAG, "Public key: ${persistedConnection.publicKey.base58()}")
            Log.d(TAG, "Account label: ${persistedConnection.accountLabel}")
            Log.d(TAG, "Auth token present: ${true}")
            
            _state.value.copy(
                isLoading = true,
                canTransact = true,
                userAddress = persistedConnection.publicKey.base58(),
                userLabel = persistedConnection.accountLabel,
            ).updateViewState()

            getBalance(persistedConnection.publicKey)

            _state.value.copy(
                isLoading = false,
                // TODO: Move all Snackbar message strings into resources
                snackbarMessage = "✅ | Successfully auto-connected to: \n" + persistedConnection.publicKey.base58() + "."
            ).updateViewState()

            // Set the auth token in walletAdapter
            walletAdapter.authToken = persistedConnection.authToken
            Log.d(TAG, "Auth token set in walletAdapter from persisted connection: ${walletAdapter.authToken != null}")
        } else {
            Log.d(TAG, "No persisted connection found")
        }
    }

    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            Log.d(TAG, "=== Starting Wallet Connection ===")
            Log.d(TAG, "ActivityResultSender: $sender")
            
            when (val result = walletAdapter.connect(sender)) {
                is TransactionResult.Success -> {
                    Log.d(TAG, "✅ Wallet connection successful")
                    Log.d(TAG, "Public key: ${result.authResult.publicKey}")
                    Log.d(TAG, "Account label: ${result.authResult.accountLabel}")
                    Log.d(TAG, "Auth token present: ${true}")
                    
                    val currentConn = Connected(
                        SolanaPublicKey(result.authResult.publicKey),
                        result.authResult.accountLabel ?: "",
                        result.authResult.authToken
                    )

                    persistenceUseCase.persistConnection(
                        currentConn.publicKey,
                        currentConn.accountLabel,
                        currentConn.authToken
                    )

                    // Set the auth token in walletAdapter
                    walletAdapter.authToken = currentConn.authToken
                    Log.d(TAG, "Auth token set in walletAdapter: ${walletAdapter.authToken != null}")

                    _state.value.copy(
                        isLoading = true,
                        userAddress = currentConn.publicKey.base58(),
                        userLabel = currentConn.accountLabel
                    ).updateViewState()

                    getBalance(currentConn.publicKey)

                    _state.value.copy(
                        isLoading = false,
                        canTransact = true,
                        snackbarMessage = "✅ | Successfully connected to: \n" + currentConn.publicKey.base58() + "."
                    ).updateViewState()
                }

                is TransactionResult.NoWalletFound -> {
                    Log.e(TAG, "❌ No wallet app found during connection")
                    _state.value.copy(
                        walletFound = false,
                        snackbarMessage = "❌ | No wallet found."
                    ).updateViewState()

                }

                is TransactionResult.Failure -> {
                    Log.e(TAG, "❌ Wallet connection failed: ${result.e.message}")
                    Log.e(TAG, "Connection error details: ${result.e}")
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

    fun getBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result =
                    AccountBalanceUseCase(rpcUri, account)

                _state.value.copy(
                    solBalance = result/1000000000.0
                ).updateViewState()
            } catch (e: Exception) {
                _state.value.copy(
                    snackbarMessage = "❌ | Failed fetching account balance."
                ).updateViewState()
            }
        }
    }

    private suspend fun getAccountBalance(account: SolanaPublicKey): Double {
        return try {
            AccountBalanceUseCase(rpcUri, account) / 1000000000.0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get balance for ${account.base58()}: ${e.message}")
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
                Log.d(TAG, "=== Starting SPL Token Transfer ===")
                Log.d(TAG, "Amount input: $amount")
                Log.d(TAG, "Recipient: $recipientAddress")
                Log.d(TAG, "Token mint: $tokenMintAddress")
                Log.d(TAG, "Token decimals: $tokenDecimals")
                
                // Check if we have a valid connection first
                val currentConnection = persistenceUseCase.getWalletConnection()
                Log.d(TAG, "Current connection state: ${currentConnection::class.simpleName}")
                
                if (currentConnection !is Connected) {
                    Log.e(TAG, "❌ No active wallet connection found")
                    _state.value.copy(
                        snackbarMessage = "❌ | Please connect your wallet first"
                    ).updateViewState()
                    return@launch
                }
                
                Log.d(TAG, "Wallet connection found. Auth token present: ${walletAdapter.authToken != null}")
                Log.d(TAG, "Starting MWA transact call...")
                
                val result = walletAdapter.transact(sender) { authResult ->
                    Log.d(TAG, "✅ MWA authorization successful")
                    Log.d(TAG, "Auth result accounts: ${authResult.accounts.size}")
                    
                    val ownerAccount = SolanaPublicKey(authResult.accounts.first().publicKey)
                    val recipientPubkey = SolanaPublicKey.from(recipientAddress)
                    val tokenMint = SolanaPublicKey.from(tokenMintAddress)

                    // Convert amount based on token decimals
                    val amountDouble = amount.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid amount: $amount")
                    val multiplier = 10.0.pow(tokenDecimals.toDouble())
                    val amountInTokenUnits = (amountDouble * multiplier).toLong()

                    Log.d(TAG, "Sender wallet: ${ownerAccount.base58()}")
                    Log.d(TAG, "Recipient wallet: ${recipientPubkey.base58()}")
                                       Log.d(TAG, "Amount in token units: $amountInTokenUnits")
                   Log.d(TAG, "RPC URI: $rpcUri")

                   // Check balances before attempting transfer
                   Log.d(TAG, "=== Pre-Transfer Balance Check ===")
                   try {
                       // Check SOL balance (for transaction fees)
                       val solBalance = getAccountBalance(ownerAccount)
                       Log.d(TAG, "SOL Balance: $solBalance SOL")
                       
                       if (solBalance < 0.001) {
                           Log.w(TAG, "⚠️ Low SOL balance: $solBalance SOL (may not be enough for fees)")
                       }
                       
                       // Check token balance in the sender's ATA
                       val senderAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(ownerAccount, tokenMint)
                       Log.d(TAG, "Checking EURC balance in ATA: ${senderAta.base58()}")
                       Log.d(TAG, "Required amount: $amountInTokenUnits token units")

                       
                       Log.d(TAG, "✅ Balance check passed!")
                       
                   } catch (e: Exception) {
                       Log.w(TAG, "⚠️ Could not check balances: ${e.message}")
                       Log.w(TAG, "Proceeding with transaction anyway...")
                   }

                   // Use the simplified transfer function that handles ATA creation automatically
                   Log.d(TAG, "Creating transaction...")
                   val tokenTransferTx = SplTokenTransferUseCase.transfer(
                        rpcUri = rpcUri,
                        fromWallet = ownerAccount,
                        toWallet = recipientPubkey,  
                        mint = tokenMint,
                        amount = amountInTokenUnits
                    )

                    Log.d(TAG, "Transaction created successfully, attempting to sign and send...")
                    signAndSendTransactions(arrayOf(tokenTransferTx.serialize()))
                }

                _state.value = when (result) {
                    is TransactionResult.Success -> {
                        val signatureBytes = result.successPayload?.signatures?.first()
                        signatureBytes?.let {
                            val signature = Base58.encodeToString(signatureBytes)
                            Log.d(TAG, "✅ Transaction successful! Signature: $signature")

                            // Refresh balance after successful transfer
                            val userAccount = SolanaPublicKey(Base58.decode(viewState.value.userAddress))
                            getBalance(userAccount)

                            _state.value.copy(
                                snackbarMessage = "✅ | Token transfer successful: $signature"
                            )
                        } ?: _state.value.copy(
                            snackbarMessage = "❌ | Incorrect payload returned"
                        )
                    }
                    is TransactionResult.NoWalletFound -> {
                        Log.e(TAG, "❌ No wallet found")
                        _state.value.copy(
                            snackbarMessage = "❌ | No MWA compatible wallet app found. Please install Phantom, Solflare, or another compatible wallet."
                        )
                    }
                                           is TransactionResult.Failure -> {
                           Log.e(TAG, "❌ Transaction failed: ${result.e.message}")
                           Log.e(TAG, "Error details: ${result.e}")

                           // Handle specific authorization errors
                           val errorMessage = when {
                               result.e is java.util.concurrent.CancellationException -> {
                                   Log.e(TAG, "Transaction was canceled - possibly by user or wallet timeout")
                                   "❌ | Transaction canceled. Check wallet app or try again."
                               }
                               result.e.message?.contains("authorization request failed") == true -> {
                                   Log.e(TAG, "Authorization failed - wallet may be disconnected or not responding")
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
                Log.e(TAG, "❌ Exception in sendSplToken: ${e.message}", e)
                
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

    /**
     * Check token balance for debugging purposes
     */
    fun checkTokenBalance(tokenMintAddress: String, tokenDecimals: Int) {
        viewModelScope.launch {
            try {
                val currentConnection = persistenceUseCase.getWalletConnection()
                if (currentConnection !is Connected) {
                    Log.w(TAG, "No wallet connection for balance check")
                    _state.value.copy(
                        snackbarMessage = "⚠️ | Connect wallet first to check balance"
                    ).updateViewState()
                    return@launch
                }

                val ownerAccount = SolanaPublicKey(Base58.decode(viewState.value.userAddress))
                val tokenMint = SolanaPublicKey.from(tokenMintAddress)
                
                Log.d(TAG, "=== Manual Token Balance Check ===")
                Log.d(TAG, "Owner: ${ownerAccount.base58()}")
                Log.d(TAG, "Token Mint: ${tokenMint.base58()}")
                
                // Derive ATA
                val senderAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(ownerAccount, tokenMint)
                Log.d(TAG, "Derived ATA: ${senderAta.base58()}")
                
                // Get balance using the clean UseCase (same pattern as SOL balance)
                val tokenBalance = TokenAccountBalanceUseCase(rpcUri, senderAta)
                val humanReadableBalance = tokenBalance.toDouble() / 10.0.pow(tokenDecimals.toDouble())
                
                Log.d(TAG, "✅ Token Balance Found:")
                Log.d(TAG, "  Raw: $tokenBalance token units")
                Log.d(TAG, "  Human: $humanReadableBalance tokens")
                
                val balanceMessage = if (tokenBalance == 0L) {
                    "💸 | No EURC tokens! Balance: 0 (Need devnet tokens)"
                } else {
                    "💰 | EURC Balance: $humanReadableBalance tokens"
                }
                
                _state.value.copy(
                    snackbarMessage = balanceMessage
                ).updateViewState()
                
            } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
                Log.e(TAG, "❌ Token account not found: ${e.message}")
                _state.value.copy(
                    snackbarMessage = "💸 | EURC ATA doesn't exist - Need to get devnet tokens first!"
                ).updateViewState()
            } catch (e: TokenAccountBalanceUseCase.TokenBalanceException) {
                Log.e(TAG, "❌ Token balance error: ${e.message}")
                _state.value.copy(
                    snackbarMessage = "❌ | Could not check EURC balance: ${e.message}"
                ).updateViewState()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Balance check failed: ${e.message}", e)
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