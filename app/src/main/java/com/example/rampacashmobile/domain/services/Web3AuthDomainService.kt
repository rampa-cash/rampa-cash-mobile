package com.example.rampacashmobile.domain.services

import com.example.rampacashmobile.domain.common.Result
import com.example.rampacashmobile.domain.common.DomainError
import com.example.rampacashmobile.usecase.PersistenceUseCase
import com.solana.publickey.SolanaPublicKey
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result data class for Web3Auth wallet connection
 */
data class Web3AuthWalletConnectionResult(
    val privateKey: String,
    val displayName: String,
    val providerName: String,
    val solanaPublicKey: String,
    val displayAddress: String
)

/**
 * Domain service for Web3Auth wallet operations
 * Handles Web3Auth-specific wallet connection and session persistence
 */
@Singleton
class Web3AuthDomainService @Inject constructor(
    private val persistenceUseCase: PersistenceUseCase
) {
    companion object {
        private const val TAG = "Web3AuthDomainService"
    }

    /**
     * Handle Web3Auth wallet connection and session persistence
     * This method focuses on Web3Auth-specific wallet management
     */
    fun handleWeb3AuthWalletConnection(
        web3AuthResponse: Web3AuthResponse, 
        provider: Provider, 
        solanaPublicKey: String, 
        displayAddress: String
    ): Result<Web3AuthWalletConnectionResult> {
        return try {
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

                Timber.d(TAG, "Web3Auth wallet connection successful with $providerName - Solana address: $solanaPublicKey")
                
                Result.Success(Web3AuthWalletConnectionResult(
                    privateKey = privateKey,
                    displayName = displayName,
                    providerName = providerName,
                    solanaPublicKey = solanaPublicKey,
                    displayAddress = displayAddress
                ))
            } else {
                Result.Failure(DomainError.UnknownError("No private key received from Web3Auth"))
            }
        } catch (e: Exception) {
            Timber.e(TAG, "Failed to handle Web3Auth wallet connection", e)
            Result.Failure(DomainError.UnknownError(e.message ?: "Unknown error"))
        }
    }
}
