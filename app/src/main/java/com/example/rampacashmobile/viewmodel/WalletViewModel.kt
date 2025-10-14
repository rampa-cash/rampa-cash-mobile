package com.example.rampacashmobile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampacashmobile.BuildConfig
import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.entities.Wallet
import com.example.rampacashmobile.domain.services.WalletDomainService
import com.example.rampacashmobile.domain.valueobjects.Money
import com.example.rampacashmobile.domain.valueobjects.Currency
import com.example.rampacashmobile.domain.valueobjects.UserId
import com.example.rampacashmobile.domain.valueobjects.WalletAddress
import com.example.rampacashmobile.constants.AppConstants
import com.example.rampacashmobile.utils.ErrorHandler
import com.example.rampacashmobile.solanautils.AssociatedTokenAccountUtils
import com.example.rampacashmobile.solanautils.TokenMints
import com.example.rampacashmobile.usecase.AccountBalanceUseCase
import com.example.rampacashmobile.usecase.TokenAccountBalanceUseCase
import com.solana.publickey.SolanaPublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency as JavaCurrency
import javax.inject.Inject

/**
 * ViewModel responsible for wallet-related operations
 * Uses DDD domain services for business logic
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletDomainService: WalletDomainService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "WalletViewModel"
    }

    private val rpcUri = android.net.Uri.parse(BuildConfig.RPC_URI)

    // Wallet state
    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState

    /**
     * Get SOL balance for a given wallet address
     */
    fun getSolBalance(walletAddress: WalletAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            _walletState.update { it.copy(isLoading = true, error = null) }
            
            val result = ErrorHandler.safeCall(
                operation = {
                    val publicKey = walletAddress.toSolanaPublicKey()
                    val balance = AccountBalanceUseCase(rpcUri, publicKey)
                    val solAmount = BigDecimal.valueOf(AppConstants.lamportsToSol(balance))
                    Money(solAmount, Currency.USD) // SOL is typically priced in USD
                },
                errorMessage = "Failed to get SOL balance"
            )
            
            when (result) {
                is Result.Success -> {
                    _walletState.update { 
                        it.copy(
                            isLoading = false,
                            solBalance = result.data,
                            error = null
                        )
                    }
                }
                is Result.Failure -> {
                    ErrorHandler.logError(result.error, TAG)
                    _walletState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Get EURC balance for a given wallet address
     */
    fun getEurcBalance(walletAddress: WalletAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ErrorHandler.safeCall(
                operation = {
                    val publicKey = walletAddress.toSolanaPublicKey()
                    val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
                    val eurcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, eurcMint)
                    
                    val balance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
                    val eurcAmount = BigDecimal.valueOf(AppConstants.tokenUnitsToAmount(balance, AppConstants.EURC_DECIMAL_PLACES))
                    Money(eurcAmount, Currency.EUR)
                },
                errorMessage = "Failed to get EURC balance"
            )
            
            when (result) {
                is Result.Success -> {
                    _walletState.update { 
                        it.copy(
                            eurcBalance = result.data,
                            error = null
                        )
                    }
                }
                is Result.Failure -> {
                    ErrorHandler.logError(result.error, TAG)
                    _walletState.update { 
                        it.copy(
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Get USDC balance for a given wallet address
     */
    fun getUsdcBalance(walletAddress: WalletAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = ErrorHandler.safeCall(
                operation = {
                    val publicKey = walletAddress.toSolanaPublicKey()
                    val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
                    val usdcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, usdcMint)
                    
                    val balance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
                    val usdcAmount = BigDecimal.valueOf(AppConstants.tokenUnitsToAmount(balance, AppConstants.USDC_DECIMAL_PLACES))
                    Money(usdcAmount, Currency.USD)
                },
                errorMessage = "Failed to get USDC balance"
            )
            
            when (result) {
                is Result.Success -> {
                    _walletState.update { 
                        it.copy(
                            usdcBalance = result.data,
                            error = null
                        )
                    }
                }
                is Result.Failure -> {
                    ErrorHandler.logError(result.error, TAG)
                    _walletState.update { 
                        it.copy(
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    /**
     * Refresh all balances for a wallet
     */
    fun refreshAllBalances(walletAddress: WalletAddress) {
        getSolBalance(walletAddress)
        getEurcBalance(walletAddress)
        getUsdcBalance(walletAddress)
    }

    /**
     * Check if wallet can send the specified amount
     */
    fun canSendAmount(amount: Money, currency: String): Boolean {
        val currentState = _walletState.value
        return when (currency.uppercase()) {
            AppConstants.SOL_SYMBOL -> currentState.solBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            AppConstants.EURC_SYMBOL -> currentState.eurcBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            AppConstants.USDC_SYMBOL -> currentState.usdcBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            else -> false
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _walletState.update { it.copy(error = null) }
    }

    /**
     * Get SOL balance and return Result for external use
     */
    suspend fun getSolBalanceResult(walletAddress: WalletAddress): Result<Money> {
        return ErrorHandler.safeCall(
            operation = {
                val publicKey = walletAddress.toSolanaPublicKey()
                val balance = AccountBalanceUseCase(rpcUri, publicKey)
                val solAmount = BigDecimal.valueOf(AppConstants.lamportsToSol(balance))
                Money(solAmount, Currency.USD) // SOL is typically priced in USD
            },
            errorMessage = "Failed to get SOL balance"
        )
    }

    /**
     * Get EURC balance and return Result for external use
     */
    suspend fun getEurcBalanceResult(walletAddress: WalletAddress): Result<Money> {
        return ErrorHandler.safeCall(
            operation = {
                val publicKey = walletAddress.toSolanaPublicKey()
                val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
                val eurcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, eurcMint)
                
                // Check if the ATA exists
                val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, eurcAta)
                if (!exists) {
                    Money(BigDecimal.ZERO, Currency.EUR)
                } else {
                    val tokenBalance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
                    val eurcAmount = BigDecimal.valueOf(AppConstants.tokenUnitsToAmount(tokenBalance, AppConstants.EURC_DECIMAL_PLACES))
                    Money(eurcAmount, Currency.EUR)
                }
            },
            errorMessage = "Failed to get EURC balance"
        )
    }

    /**
     * Get USDC balance and return Result for external use
     */
    suspend fun getUsdcBalanceResult(walletAddress: WalletAddress): Result<Money> {
        return ErrorHandler.safeCall(
            operation = {
                val publicKey = walletAddress.toSolanaPublicKey()
                val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
                val usdcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, usdcMint)
                
                // Check if the ATA exists
                val exists = AssociatedTokenAccountUtils.checkAccountExists(rpcUri, usdcAta)
                if (!exists) {
                    Money(BigDecimal.ZERO, Currency.USD)
                } else {
                    val tokenBalance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
                    val usdcAmount = BigDecimal.valueOf(AppConstants.tokenUnitsToAmount(tokenBalance, AppConstants.USDC_DECIMAL_PLACES))
                    Money(usdcAmount, Currency.USD)
                }
            },
            errorMessage = "Failed to get USDC balance"
        )
    }

    /**
     * Refresh all balances and return Result for external use
     */
    suspend fun refreshAllBalancesResult(walletAddress: WalletAddress): Result<Wallet> {
        return walletDomainService.loadWalletBalances(walletAddress)
    }
}

// WalletState is now defined in AppViewState.kt
