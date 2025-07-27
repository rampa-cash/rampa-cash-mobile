package com.example.rampacashmobile.usecase

import android.content.SharedPreferences
import com.solana.publickey.SolanaPublicKey
import javax.inject.Inject
import androidx.core.content.edit

sealed class WalletConnection

object NotConnected : WalletConnection()

data class Connected(
    val publicKey: SolanaPublicKey,
    val accountLabel: String,
    val authToken: String
) : WalletConnection()

data class Web3AuthConnected(
    val publicKey: SolanaPublicKey,
    val accountLabel: String,
    val privateKey: String,
    val providerName: String,
    val userInfo: String
) : WalletConnection()

class PersistenceUseCase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    private var connection: WalletConnection = NotConnected

    fun getWalletConnection(): WalletConnection {
        android.util.Log.d("PersistenceUseCase", "🔍 getWalletConnection() called")
        android.util.Log.d("PersistenceUseCase", "Current connection in memory: ${connection::class.simpleName}")
        
        return when (connection) {
            is Connected -> {
                android.util.Log.d("PersistenceUseCase", "✅ Returning cached MWA connection: ${(connection as Connected).accountLabel}")
                connection
            }
            is Web3AuthConnected -> {
                android.util.Log.d("PersistenceUseCase", "✅ Returning cached Web3Auth connection: ${(connection as Web3AuthConnected).accountLabel}")
                connection
            }
            is NotConnected -> {
                android.util.Log.d("PersistenceUseCase", "🔍 No cached connection - checking SharedPreferences...")
                
                // Check for Web3Auth session first
                val web3AuthKey = sharedPreferences.getString(WEB3AUTH_PUBKEY_KEY, "")
                val web3AuthPrivateKey = sharedPreferences.getString(WEB3AUTH_PRIVATE_KEY, "")
                val web3AuthLabel = sharedPreferences.getString(WEB3AUTH_ACCOUNT_LABEL, "")
                val web3AuthProvider = sharedPreferences.getString(WEB3AUTH_PROVIDER, "")
                val web3AuthUserInfo = sharedPreferences.getString(WEB3AUTH_USER_INFO, "")

                android.util.Log.d("PersistenceUseCase", "🔍 Web3Auth stored data:")
                android.util.Log.d("PersistenceUseCase", "  - Key: ${if (web3AuthKey.isNullOrEmpty()) "EMPTY" else "EXISTS(${web3AuthKey.take(8)}...)"}")
                android.util.Log.d("PersistenceUseCase", "  - Private Key: ${if (web3AuthPrivateKey.isNullOrEmpty()) "EMPTY" else "EXISTS"}")
                android.util.Log.d("PersistenceUseCase", "  - Label: $web3AuthLabel")
                android.util.Log.d("PersistenceUseCase", "  - Provider: $web3AuthProvider")

                if (!web3AuthKey.isNullOrEmpty() && !web3AuthPrivateKey.isNullOrEmpty() && 
                    !web3AuthLabel.isNullOrEmpty() && !web3AuthProvider.isNullOrEmpty()) {
                    android.util.Log.d("PersistenceUseCase", "✅ Valid Web3Auth session found - restoring")
                    val web3AuthConn = Web3AuthConnected(
                        SolanaPublicKey.from(web3AuthKey), 
                        web3AuthLabel, 
                        web3AuthPrivateKey,
                        web3AuthProvider,
                        web3AuthUserInfo ?: ""
                    )
                    connection = web3AuthConn
                    return web3AuthConn
                }

                // Check for MWA session
                val key = sharedPreferences.getString(PUBKEY_KEY, "")
                val accountLabel = sharedPreferences.getString(ACCOUNT_LABEL, "") ?: ""
                val token = sharedPreferences.getString(AUTH_TOKEN_KEY, "")

                android.util.Log.d("PersistenceUseCase", "🔍 MWA stored data:")
                android.util.Log.d("PersistenceUseCase", "  - Key: ${if (key.isNullOrEmpty()) "EMPTY" else "EXISTS(${key.take(8)}...)"}")
                android.util.Log.d("PersistenceUseCase", "  - Label: $accountLabel")
                android.util.Log.d("PersistenceUseCase", "  - Token: ${if (token.isNullOrEmpty()) "EMPTY" else "EXISTS"}")

                val newConn = if (key.isNullOrEmpty() || token.isNullOrEmpty()) {
                    android.util.Log.d("PersistenceUseCase", "❌ No valid MWA session found")
                    NotConnected
                } else {
                    android.util.Log.d("PersistenceUseCase", "✅ Valid MWA session found - restoring")
                    Connected(SolanaPublicKey.from(key), accountLabel, token)
                }

                connection = newConn
                return newConn
            }
        }
    }

    fun persistConnection(pubKey: SolanaPublicKey, accountLabel: String, token: String) {
        android.util.Log.d("PersistenceUseCase", "💾 Persisting MWA connection:")
        android.util.Log.d("PersistenceUseCase", "  - Key: ${pubKey.base58()}")
        android.util.Log.d("PersistenceUseCase", "  - Label: $accountLabel")
        android.util.Log.d("PersistenceUseCase", "  - Token: ${token.take(10)}...")
        
        sharedPreferences.edit {
            putString(PUBKEY_KEY, pubKey.base58())
            putString(ACCOUNT_LABEL, accountLabel)
            putString(AUTH_TOKEN_KEY, token)
        }

        connection = Connected(pubKey, accountLabel, token)
        android.util.Log.d("PersistenceUseCase", "✅ MWA connection persisted successfully")
    }

    fun persistWeb3AuthConnection(
        pubKey: SolanaPublicKey, 
        accountLabel: String, 
        privateKey: String,
        providerName: String,
        userInfo: String
    ) {
        android.util.Log.d("PersistenceUseCase", "💾 Persisting Web3Auth connection:")
        android.util.Log.d("PersistenceUseCase", "  - Key: ${pubKey.base58()}")
        android.util.Log.d("PersistenceUseCase", "  - Label: $accountLabel")
        android.util.Log.d("PersistenceUseCase", "  - Provider: $providerName")
        android.util.Log.d("PersistenceUseCase", "  - Private Key: ${privateKey.take(10)}...")
        
        sharedPreferences.edit {
            putString(WEB3AUTH_PUBKEY_KEY, pubKey.base58())
            putString(WEB3AUTH_ACCOUNT_LABEL, accountLabel)
            putString(WEB3AUTH_PRIVATE_KEY, privateKey)
            putString(WEB3AUTH_PROVIDER, providerName)
            putString(WEB3AUTH_USER_INFO, userInfo)
            // Clear MWA session when saving Web3Auth session
            putString(PUBKEY_KEY, "")
            putString(ACCOUNT_LABEL, "")
            putString(AUTH_TOKEN_KEY, "")
        }

        connection = Web3AuthConnected(pubKey, accountLabel, privateKey, providerName, userInfo)
        android.util.Log.d("PersistenceUseCase", "✅ Web3Auth connection persisted successfully")
    }

    fun clearConnection() {
        android.util.Log.d("PersistenceUseCase", "🗑️ Clearing all persisted sessions...")
        
        sharedPreferences.edit {
            // Clear MWA session
            putString(PUBKEY_KEY, "")
            putString(ACCOUNT_LABEL, "")
            putString(AUTH_TOKEN_KEY, "")
            // Clear Web3Auth session
            putString(WEB3AUTH_PUBKEY_KEY, "")
            putString(WEB3AUTH_ACCOUNT_LABEL, "")
            putString(WEB3AUTH_PRIVATE_KEY, "")
            putString(WEB3AUTH_PROVIDER, "")
            putString(WEB3AUTH_USER_INFO, "")
        }

        connection = NotConnected
        android.util.Log.d("PersistenceUseCase", "✅ All sessions cleared successfully")
    }

    companion object {
        // MWA session keys
        const val PUBKEY_KEY = "stored_pubkey"
        const val ACCOUNT_LABEL = "stored_account_label"
        const val AUTH_TOKEN_KEY = "stored_auth_token"
        
        // Web3Auth session keys
        const val WEB3AUTH_PUBKEY_KEY = "web3auth_pubkey"
        const val WEB3AUTH_ACCOUNT_LABEL = "web3auth_account_label"
        const val WEB3AUTH_PRIVATE_KEY = "web3auth_private_key"
        const val WEB3AUTH_PROVIDER = "web3auth_provider"
        const val WEB3AUTH_USER_INFO = "web3auth_user_info"
    }
}