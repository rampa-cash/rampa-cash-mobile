package com.example.rampacashmobile.web3auth

import android.content.Context
import android.net.Uri
import timber.log.Timber
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.*
import org.sol4k.Keypair
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for handling Web3Auth operations
 * Encapsulates all Web3Auth-specific logic for better separation of concerns
 */
@Singleton
class Web3AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "Web3AuthManager"
    }
    
    private var web3Auth: Web3Auth? = null
    
    /**
     * Callback interface for Web3Auth operations
     */
    interface Web3AuthCallback {
        fun onLoginSuccess(response: Web3AuthResponse, provider: Provider, solanaPublicKey: String, displayAddress: String)
        fun onLoginError(message: String)
        fun onLogoutSuccess()
        fun onLogoutError(message: String)
        fun onLoading(isLoading: Boolean)
    }
    
    /**
     * Initialize Web3Auth with the given client ID and context
     */
    fun initialize(clientId: String, activity: Context): Boolean {
        return try {
            Timber.d("🔧 Initializing Web3Auth...")
            web3Auth = Web3Auth(
                Web3AuthOptions(
                    clientId = clientId,
                    network = Network.SAPPHIRE_DEVNET,
                    buildEnv = BuildEnv.PRODUCTION,
                    redirectUrl = Uri.parse("com.example.rampacashmobile://auth"),
                    sessionTime = 86400 // 24 hours in seconds
                ),
                activity // Activity context - crucial for browser launching
            )
            Timber.d("✅ Web3Auth initialized successfully")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Web3Auth initialization failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check if Web3Auth has an existing session
     */
    fun hasExistingSession(): Boolean {
        return try {
            val web3AuthInstance = web3Auth
            if (web3AuthInstance == null) {
                Timber.w("⚠️ Web3Auth instance is null during session check")
                return false
            }
            
            // In Web3Auth Android SDK, getUserInfo() throws an exception if no session exists
            // So we need to catch that exception to determine if there's a session
            try {
                val userInfo = web3AuthInstance.getUserInfo()
                val hasSession = userInfo != null
                Timber.d("🔍 Web3Auth session check: hasSession = $hasSession")
                
                if (hasSession && userInfo != null) {
                    Timber.d("🔑 User session exists: ${userInfo.name ?: userInfo.email ?: "Unknown user"}")
                }
                
                hasSession
            } catch (e: Throwable) {
                // getUserInfo() throws java.lang.Error when no user is found
                // This is expected behavior when there's no active session
                Timber.d("🔑 No user session found in Web3Auth SDK: ${e.message}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to check Web3Auth session: ${e.message}")
            false
        }
    }
    
    /**
     * Get Web3Auth session info if available
     */
        fun getSessionInfo(): Triple<String, String, String>? {
        return try {
            val web3AuthInstance = web3Auth ?: return null

            // Check if user info is available (indicates active session)
            // getUserInfo() throws java.lang.Error when no session exists
            val userInfo = try {
                web3AuthInstance.getUserInfo()
            } catch (e: Throwable) {
                Timber.d(TAG, "🔍 No Web3Auth session available: ${e.message}")
                return null
            }
            
            if (userInfo == null) {
                Timber.d(TAG, "🔍 No Web3Auth session available")
                return null
            }
            
            // Get the Ed25519 private key using the correct method
            val ed25519PrivateKey = web3AuthInstance.getEd25519PrivKey()
            if (ed25519PrivateKey.isEmpty()) {
                Timber.d(TAG, "🔍 Web3Auth session exists but no private key available")
                return null
            }
            val solanaKeyPair = org.sol4k.Keypair.fromSecretKey(ed25519PrivateKey.hexToByteArray())
            val solanaPublicKey = solanaKeyPair.publicKey.toBase58()
            val displayAddress = "${solanaPublicKey.take(8)}...${solanaPublicKey.takeLast(8)}"
            
            Timber.d(TAG, "✅ Web3Auth session info retrieved")
            Triple(ed25519PrivateKey, solanaPublicKey, displayAddress)

        } catch (e: Exception) {
            Timber.e(TAG, "❌ Failed to get Web3Auth session info: ${e.message}", e)
            null
        }
    }
    
    /**
     * Start Web3Auth login process
     */
    fun login(provider: Provider, callback: Web3AuthCallback) {
        val web3AuthInstance = web3Auth
        if (web3AuthInstance == null) {
            callback.onLoginError("Web3Auth not initialized")
            return
        }
        
        Timber.d(TAG, "🚀 Starting Web3Auth login with provider: $provider")
        callback.onLoading(true)
        
        try {
            Timber.d(TAG, "🔧 Creating login params for $provider")
            val loginParams = LoginParams(provider)
            
            Timber.d(TAG, "🔑 Starting Web3Auth login...")
            val loginFuture = web3AuthInstance.login(loginParams)
            Timber.d(TAG, "📞 Login future created, browser should open...")
            
            // Handle completion using official pattern
            loginFuture.whenComplete { web3AuthResponse, error ->
                callback.onLoading(false)
                
                if (error == null && web3AuthResponse != null) {
                    Timber.d(TAG, "✅ Web3Auth login completed successfully!")
                    handleLoginSuccess(web3AuthResponse, provider, callback)
                } else {
                    Timber.e(TAG, "❌ Web3Auth login failed: ${error?.message}", error)
                    callback.onLoginError("Login failed: ${error?.message ?: "Unknown error"}")
                }
            }
            
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Web3Auth login setup failed: ${e.message}", e)
            callback.onLoading(false)
            callback.onLoginError("Setup failed: ${e.message}")
        }
    }
    
    /**
     * Start Web3Auth login process with phone number (SMS passwordless)
     */
    fun loginWithPhone(phoneNumber: String, callback: Web3AuthCallback) {
        val web3AuthInstance = web3Auth
        if (web3AuthInstance == null) {
            callback.onLoginError("Web3Auth not initialized")
            return
        }

        Timber.d(TAG, "🚀 Starting Web3Auth SMS login with phone: ${phoneNumber.take(3)}***")
        callback.onLoading(true)

        try {
            Timber.d(TAG, "🔧 Creating SMS login params with login_hint")
            val loginParams = LoginParams(
                loginProvider = Provider.SMS_PASSWORDLESS,
                extraLoginOptions = ExtraLoginOptions(
                    login_hint = phoneNumber
                )
            )

            Timber.d(TAG, "🔑 Starting Web3Auth SMS login...")
            val loginFuture = web3AuthInstance.login(loginParams)
            Timber.d(TAG, "📞 SMS login future created, SMS should be sent...")

            // Handle completion using official pattern
            loginFuture.whenComplete { web3AuthResponse, error ->
                callback.onLoading(false)

                if (error == null && web3AuthResponse != null) {
                    Timber.d(TAG, "✅ Web3Auth SMS login completed successfully!")
                    handleLoginSuccess(web3AuthResponse, Provider.SMS_PASSWORDLESS, callback)
                } else {
                    Timber.e(TAG, "❌ Web3Auth SMS login failed: ${error?.message}", error)
                    callback.onLoginError("SMS login failed: ${error?.message ?: "Unknown error"}")
                }
            }

        } catch (e: Exception) {
            Timber.e(TAG, "❌ Web3Auth SMS login setup failed: ${e.message}", e)
            callback.onLoading(false)
            callback.onLoginError("SMS setup failed: ${e.message}")
        }
    }

    /**
     * Start Web3Auth logout process
     */
    fun logout(callback: Web3AuthCallback) {
        val web3AuthInstance = web3Auth
        if (web3AuthInstance == null) {
            callback.onLogoutError("Web3Auth not initialized")
            return
        }
        
        Timber.d(TAG, "🚀 Starting Web3Auth logout")
        callback.onLoading(true)
        
        try {
            val logoutFuture = web3AuthInstance.logout()
            Timber.d(TAG, "📞 Logout future created...")
            
            // Handle completion using official pattern
            logoutFuture.whenComplete { result, error ->
                callback.onLoading(false)
                
                if (error == null) {
                    Timber.d(TAG, "✅ Web3Auth logout completed successfully!")
                    callback.onLogoutSuccess()
                } else {
                    // Check if this is the sessionId uninitialized error from restored sessions
                    val errorMessage = error?.message ?: ""
                    if (errorMessage.contains("sessionId has not been initialized", ignoreCase = true)) {
                        Timber.d(TAG, "ℹ️ Web3Auth logout skipped - no active SDK session (restored from storage)")
                        // This is expected for restored sessions - treat as successful logout
                        callback.onLogoutSuccess()
                    } else {
                        Timber.e(TAG, "❌ Web3Auth logout failed: ${error?.message}", error)
                        callback.onLogoutError("Logout failed: ${error?.message ?: "Unknown error"}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Web3Auth logout setup failed: ${e.message}", e)
            callback.onLoading(false)
            
            // Check if this is the sessionId error at the setup level too
            val errorMessage = e.message ?: ""
            if (errorMessage.contains("sessionId has not been initialized", ignoreCase = true)) {
                Timber.d(TAG, "ℹ️ Web3Auth logout skipped at setup - no active SDK session (restored from storage)")
                callback.onLogoutSuccess()
            } else {
                callback.onLogoutError("Logout setup failed: ${e.message}")
            }
        }
    }
    
    /**
     * Handle Web3Auth redirects
     */
    fun handleRedirect(data: Uri?): Boolean {
        if (data == null) return false
        
        Timber.d(TAG, "🔗 Handling redirect data: $data")
        
        // Check if this is a Web3Auth redirect
        if (data.scheme == "com.example.rampacashmobile" && data.host == "auth") {
            Timber.d(TAG, "✅ Valid Web3Auth redirect detected")
            
            return try {
                web3Auth?.setResultUrl(data)
                Timber.d(TAG, "📨 Web3Auth redirect handled: $data")
                true
            } catch (e: Exception) {
                Timber.e(TAG, "❌ Redirect handling failed: ${e.message}", e)
                false
            }
        } else {
            Timber.d(TAG, "ℹ️ Not a Web3Auth redirect: $data")
            return false
        }
    }
    
    /**
     * Check if custom tabs were closed by user
     */
    fun handleCustomTabsClosed(): Boolean {
        return if (Web3Auth.getCustomTabsClosed()) {
            Timber.d(TAG, "🌐 Web3Auth custom tabs were closed by user")
            Web3Auth.setCustomTabsClosed(false)
            true
        } else {
            false
        }
    }
    
    /**
     * Handle successful login and derive Solana keys
     */
    private fun handleLoginSuccess(
        web3AuthResponse: Web3AuthResponse, 
        provider: Provider, 
        callback: Web3AuthCallback
    ) {
        try {
            val privateKey = web3AuthResponse.privKey
            val userInfo = web3AuthResponse.userInfo
            
            if (privateKey != null) {
                Timber.d(TAG, "🔑 Web3Auth Private Key received: ${privateKey.take(10)}...")
                Timber.d(TAG, "👤 User Info: ${userInfo?.name ?: userInfo?.email ?: "Unknown"}")
                
                // Derive Solana public key from Web3Auth private key
                val keyDerivationResult = deriveSolanaKeys(privateKey)
                
                if (keyDerivationResult != null) {
                    val (solanaPublicKey, displayAddress) = keyDerivationResult
                    Timber.d(TAG, "🎯 Derived Solana Public Key: $solanaPublicKey")
                    Timber.d(TAG, "📍 Display Address: $displayAddress")
                    
                    callback.onLoginSuccess(web3AuthResponse, provider, solanaPublicKey, displayAddress)
                } else {
                    callback.onLoginError("Failed to derive Solana keys")
                }
            } else {
                Timber.e(TAG, "❌ No private key received from Web3Auth")
                callback.onLoginError("No private key received from Web3Auth")
            }
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Failed to handle login success: ${e.message}", e)
            callback.onLoginError("Failed to process login: ${e.message}")
        }
    }
    
    /**
     * Derive Solana public key from Web3Auth Ed25519 private key
     */
    private fun deriveSolanaKeys(privateKey: String): Pair<String, String>? {
        return try {
            val web3AuthInstance = web3Auth ?: throw IllegalStateException("Web3Auth not initialized")
            
            // Get Ed25519 private key from Web3Auth
            val ed25519PrivateKey = web3AuthInstance.getEd25519PrivKey()
            Timber.d(TAG, "🔐 Ed25519 Private Key: ${ed25519PrivateKey.take(10)}...")
            
            // Create Keypair from Ed25519 private key using sol4k
            val solanaKeyPair = Keypair.fromSecretKey(ed25519PrivateKey.hexToByteArray())
            
            // Get Solana public address
            val solanaPublicKey = solanaKeyPair.publicKey.toBase58()
            
            // Create display-friendly address
            val displayAddress = "${solanaPublicKey.take(8)}...${solanaPublicKey.takeLast(8)}"
            
            Pair(solanaPublicKey, displayAddress)
            
        } catch (e: Exception) {
            Timber.e(TAG, "❌ Failed to derive Solana keys: ${e.message}", e)
            null
        }
    }
}


/**
 * Extension function to convert hex string to byte array
 * Required for Web3Auth Ed25519 private key conversion
 */
private fun String.hexToByteArray(): ByteArray {
    return this.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}