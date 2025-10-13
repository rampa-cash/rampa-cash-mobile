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
            try {
                _walletState.update { it.copy(isLoading = true, error = null) }
                
                val publicKey = walletAddress.toSolanaPublicKey()
                val balance = AccountBalanceUseCase(rpcUri, publicKey)
                val solAmount = BigDecimal.valueOf(balance / 1000000000.0)
                val money = Money(solAmount, Currency.USD) // SOL is typically priced in USD
                
                _walletState.update { 
                    it.copy(
                        isLoading = false,
                        solBalance = money,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get SOL balance", e)
                _walletState.update { 
                    it.copy(
                        isLoading = false,
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to fetch SOL balance: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Get EURC balance for a given wallet address
     */
    fun getEurcBalance(walletAddress: WalletAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val publicKey = walletAddress.toSolanaPublicKey()
                val eurcMint = SolanaPublicKey.from(TokenMints.EURC_DEVNET)
                val eurcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, eurcMint)
                
                val balance = TokenAccountBalanceUseCase(rpcUri, eurcAta)
                val eurcAmount = BigDecimal.valueOf(balance / 1000000.0) // EURC has 6 decimals
                val money = Money(eurcAmount, Currency.EUR)
                
                _walletState.update { 
                    it.copy(
                        eurcBalance = money,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get EURC balance", e)
                _walletState.update { 
                    it.copy(
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to fetch EURC balance: ${e.message}",
                            e
                        )
                    )
                }
            }
        }
    }

    /**
     * Get USDC balance for a given wallet address
     */
    fun getUsdcBalance(walletAddress: WalletAddress) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val publicKey = walletAddress.toSolanaPublicKey()
                val usdcMint = SolanaPublicKey.from(TokenMints.USDC_DEVNET)
                val usdcAta = AssociatedTokenAccountUtils.deriveAssociatedTokenAccount(publicKey, usdcMint)
                
                val balance = TokenAccountBalanceUseCase(rpcUri, usdcAta)
                val usdcAmount = BigDecimal.valueOf(balance / 1000000.0) // USDC has 6 decimals
                val money = Money(usdcAmount, Currency.USD)
                
                _walletState.update { 
                    it.copy(
                        usdcBalance = money,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get USDC balance", e)
                _walletState.update { 
                    it.copy(
                        error = com.example.rampacashmobile.domain.common.DomainError.NetworkError(
                            "Failed to fetch USDC balance: ${e.message}",
                            e
                        )
                    )
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
            "SOL" -> currentState.solBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            "EURC" -> currentState.eurcBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            "USDC" -> currentState.usdcBalance?.let { it.isGreaterThanOrEqual(amount) } ?: false
            else -> false
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _walletState.update { it.copy(error = null) }
    }
}

/**
 * State class for wallet-related UI state
 * Aligned with our domain Wallet entity
 */
data class WalletState(
    val isLoading: Boolean = false,
    val solBalance: Money? = null,
    val eurcBalance: Money? = null,
    val usdcBalance: Money? = null,
    val error: com.example.rampacashmobile.domain.common.DomainError? = null
)
