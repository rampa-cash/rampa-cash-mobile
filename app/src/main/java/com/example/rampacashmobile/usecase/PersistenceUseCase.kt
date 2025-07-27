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
        return when (connection) {
            is Connected -> connection
            is Web3AuthConnected -> connection
            is NotConnected -> {
                // Check for Web3Auth session first
                val web3AuthKey = sharedPreferences.getString(WEB3AUTH_PUBKEY_KEY, "")
                val web3AuthPrivateKey = sharedPreferences.getString(WEB3AUTH_PRIVATE_KEY, "")
                val web3AuthLabel = sharedPreferences.getString(WEB3AUTH_ACCOUNT_LABEL, "")
                val web3AuthProvider = sharedPreferences.getString(WEB3AUTH_PROVIDER, "")
                val web3AuthUserInfo = sharedPreferences.getString(WEB3AUTH_USER_INFO, "")

                if (!web3AuthKey.isNullOrEmpty() && !web3AuthPrivateKey.isNullOrEmpty() && 
                    !web3AuthLabel.isNullOrEmpty() && !web3AuthProvider.isNullOrEmpty()) {
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

                val newConn = if (key.isNullOrEmpty() || token.isNullOrEmpty()) {
                    NotConnected
                } else {
                    Connected(SolanaPublicKey.from(key), accountLabel, token)
                }

                connection = newConn
                return newConn
            }
        }
    }

    fun persistConnection(pubKey: SolanaPublicKey, accountLabel: String, token: String) {
        sharedPreferences.edit {
            putString(PUBKEY_KEY, pubKey.base58())
            putString(ACCOUNT_LABEL, accountLabel)
            putString(AUTH_TOKEN_KEY, token)
        }

        connection = Connected(pubKey, accountLabel, token)
    }

    fun persistWeb3AuthConnection(
        pubKey: SolanaPublicKey, 
        accountLabel: String, 
        privateKey: String,
        providerName: String,
        userInfo: String
    ) {
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
    }

    fun clearConnection() {
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