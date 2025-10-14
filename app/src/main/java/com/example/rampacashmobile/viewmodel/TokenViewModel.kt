package com.example.rampacashmobile.viewmodel

import android.content.Context
import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.constants.AppConstants
import com.example.rampacashmobile.utils.ErrorHandler
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
import com.solana.publickey.SolanaPublicKey
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
 * ViewModel responsible for SPL token operations
 * Handles token transfers, balance checking, and ATA operations
 */
@HiltViewModel
class TokenViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "TokenViewModel"
    }

    private val rpcUri = android.net.Uri.parse(BuildConfig.RPC_URI)

    // Token state
    private val _tokenState = MutableStateFlow(TokenState())
    val tokenState: StateFlow<TokenState> = _tokenState

    /**
     * Transfer SPL Tokens using MWA
     * TODO: Implement proper transaction signing - signAndSendTransactions function is missing
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
                // Check if we have a valid connection first
                val currentConnection = persistenceUseCase.getWalletConnection()

                if (currentConnection !is Connected) {
                    _tokenState.update { 
                        it.copy(error = "Please connect your wallet first")
                    }
                    return@launch
                }

                _tokenState.update { it.copy(isTransferring = true, error = null) }

                // TODO: Implement proper transaction signing
                // The signAndSendTransactions function is missing from the codebase
                _tokenState.update { 
                    it.copy(
                        isTransferring = false,
                        error = "Token transfer not implemented yet - signAndSendTransactions function missing"
                    )
                }

            } catch (e: Exception) {
                _tokenState.update { 
                    it.copy(
                        isTransferring = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Check Associated Token Account (ATA) for a recipient
     */
    fun checkATA(
        recipientAddress: String,
        tokenMintAddress: String
    ) {
        // Validate recipient address using InputValidator
        val addressValidation = com.example.rampacashmobile.utils.InputValidator.validateWalletAddress(recipientAddress)
        if (addressValidation is Result.Failure) {
            Timber.w(TAG, "checkATA: Invalid recipient address: ${addressValidation.error.message}")
            _tokenState.update { 
                it.copy(error = ErrorHandler.getUserFriendlyMessage(addressValidation.error))
            }
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

                _tokenState.update { 
                    it.copy(
                        ataInfo = "ATA for ${recipientAddress.take(8)}...${recipientAddress.takeLast(8)}: ${toStringATA.take(8)}...${toStringATA.takeLast(8)} | $existenceStatus"
                    )
                }

            } catch (e: Exception) {
                val error = ErrorHandler.mapNetworkException(e, "Failed to check ATA")
                ErrorHandler.logError(error, TAG)
                _tokenState.update { 
                    it.copy(error = ErrorHandler.getUserFriendlyMessage(error))
                }
            }
        }
    }

    /**
     * Check token balance for debugging purposes
     */
    fun checkTokenBalance(tokenMintAddress: String, tokenDecimals: Int) {
        viewModelScope.launch {
            try {
                // Get the user's Solana public key from persistence
                val currentConnection = persistenceUseCase.getWalletConnection()
                val userPublicKey = when (currentConnection) {
                    is Connected -> currentConnection.publicKey
                    is com.example.rampacashmobile.usecase.Web3AuthConnected -> currentConnection.publicKey
                    else -> null
                }

                if (userPublicKey == null) {
                    _tokenState.update { 
                        it.copy(error = "Please connect a wallet first")
                    }
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

                // Get balance using the clean UseCase
                val tokenBalance = TokenAccountBalanceUseCase(rpcUri, senderAta)
                val humanReadableBalance =
                    tokenBalance.toDouble() / 10.0.pow(tokenDecimals.toDouble())

                val balanceMessage = if (tokenBalance == 0L) {
                    "No $tokenSymbol tokens! Balance: 0 (Need devnet tokens)"
                } else {
                    "$tokenSymbol Balance: $humanReadableBalance tokens"
                }

                _tokenState.update { 
                    it.copy(tokenBalanceInfo = balanceMessage)
                }

            } catch (e: TokenAccountBalanceUseCase.TokenAccountNotFoundException) {
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }
                _tokenState.update { 
                    it.copy(error = "$tokenSymbol ATA doesn't exist - Need to get devnet tokens first!")
                }
            } catch (e: TokenAccountBalanceUseCase.TokenBalanceException) {
                val tokenSymbol = when (tokenMintAddress) {
                    TokenMints.EURC_DEVNET -> "EURC"
                    TokenMints.USDC_DEVNET -> "USDC"
                    else -> "Token"
                }
                _tokenState.update { 
                    it.copy(error = "Could not check $tokenSymbol balance: ${e.message}")
                }
            } catch (e: Exception) {
                _tokenState.update { 
                    it.copy(error = "Balance check failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _tokenState.update { it.copy(error = null) }
    }

    /**
     * Clear last transaction details
     */
    fun clearLastTransaction() {
        _tokenState.update { it.copy(lastTransactionDetails = null) }
    }

    /**
     * Clear ATA info
     */
    fun clearATAInfo() {
        _tokenState.update { it.copy(ataInfo = null) }
    }

    /**
     * Clear token balance info
     */
    fun clearTokenBalanceInfo() {
        _tokenState.update { it.copy(tokenBalanceInfo = null) }
    }
}

/**
 * Token state data class
 */
data class TokenState(
    val isTransferring: Boolean = false,
    val lastTransactionDetails: TransactionDetails? = null,
    val ataInfo: String? = null,
    val tokenBalanceInfo: String? = null,
    val error: String? = null
)
