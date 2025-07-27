package com.example.rampacashmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.rampacashmobile.ui.screens.MainScreen
import com.example.rampacashmobile.ui.theme.RampaCashMobileTheme
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Web3AuthOptions
import com.web3auth.core.types.Network
import com.web3auth.core.types.BuildEnv
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var web3Auth: Web3Auth
    
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🏁 MainActivity onCreate")
        
        val sender = ActivityResultSender(this)

        // Initialize Web3Auth in Activity context (following official examples)
        initializeWeb3Auth()

        // Handle initial intent data for Web3Auth redirects
        handleIntentData(intent?.data)

        enableEdgeToEdge()
        setContent {
            RampaCashMobileTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        intentSender = sender,
                        onWeb3AuthLogin = { provider -> loginWithWeb3Auth(provider) },
                        onWeb3AuthLogout = { logoutWeb3Auth() } // Add logout callback
                    )
                }
            }
        }
    }

    private fun initializeWeb3Auth() {
        try {
            Log.d(TAG, "🔧 Initializing Web3Auth in Activity context...")
            web3Auth = Web3Auth(
                Web3AuthOptions(
                    clientId = getString(R.string.web3auth_project_id),
                    network = Network.SAPPHIRE_DEVNET, // Changed to match your dashboard config
                    buildEnv = BuildEnv.PRODUCTION,
                    redirectUrl = Uri.parse("com.example.rampacashmobile://auth")
                ),
                this // Activity context - crucial for browser launching
            )
            Log.d(TAG, "✅ Web3Auth initialized successfully in Activity")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Web3Auth initialization failed: ${e.message}", e)
            Toast.makeText(this, "Web3Auth initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle Web3Auth login directly in Activity (following official examples)
    fun loginWithWeb3Auth(provider: Provider) {
        Log.d(TAG, "🚀 Starting Web3Auth login with provider: $provider (in Activity)")
        
        // Update ViewModel state
        viewModel.setWeb3AuthLoading(true)

        try {
            Log.d(TAG, "🔧 Creating login params for $provider")
            val loginParams = LoginParams(provider)
            
            Log.d(TAG, "🔑 Starting Web3Auth login from Activity context...")
            
            // Call login from Activity context (this is key!)
            val loginFuture = web3Auth.login(loginParams)
            Log.d(TAG, "📞 Login future created from Activity, browser should open...")
            
            // Handle completion using official pattern
            loginFuture.whenComplete { web3AuthResponse, error ->
                runOnUiThread {
                    if (error == null && web3AuthResponse != null) {
                        Log.d(TAG, "✅ Web3Auth login completed successfully!")
                        
                        // Extract wallet public address from Web3Auth response
                        val privateKey = web3AuthResponse.privKey
                        var walletAddress = "Unknown"
                        
                        try {
                            if (privateKey != null) {
                                // For Solana, you can derive the public key from private key
                                // This is a simplified example - you might want to use proper Solana key derivation
                                walletAddress = "Solana Address: ${privateKey.take(8)}...${privateKey.takeLast(8)}"
                                Log.d(TAG, "🔑 Wallet Private Key: ${privateKey.take(10)}...")
                                Log.d(TAG, "📍 Wallet Address: $walletAddress")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to derive wallet address: ${e.message}")
                        }
                        
                        viewModel.handleWeb3AuthSuccess(web3AuthResponse, provider, walletAddress)
                    } else {
                        Log.e(TAG, "❌ Web3Auth login failed: ${error?.message}", error)
                        viewModel.setWeb3AuthError("Login failed: ${error?.message ?: "Unknown error"}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Web3Auth login setup failed: ${e.message}", e)
            viewModel.setWeb3AuthError("Setup failed: ${e.message}")
        }
    }

    // Handle Web3Auth logout directly in Activity (same instance as login)
    fun logoutWeb3Auth() {
        Log.d(TAG, "🚀 Starting Web3Auth logout from Activity")
        
        // Update ViewModel state to show loading
        viewModel.setWeb3AuthLoading(true)
        
        try {
            // Use the same web3Auth instance that was used for login
            val logoutFuture = web3Auth.logout()
            Log.d(TAG, "📞 Logout future created from Activity...")
            
            // Handle completion using official pattern
            logoutFuture.whenComplete { result, error ->
                runOnUiThread {
                    if (error == null) {
                        Log.d(TAG, "✅ Web3Auth logout completed successfully!")
                        viewModel.handleWeb3AuthLogout()
                    } else {
                        Log.e(TAG, "❌ Web3Auth logout failed: ${error?.message}", error)
                        viewModel.setWeb3AuthError("Logout failed: ${error?.message ?: "Unknown error"}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Web3Auth logout setup failed: ${e.message}", e)
            viewModel.setWeb3AuthError("Logout setup failed: ${e.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "🔄 onNewIntent: ${intent.data}")
        setIntent(intent) // Important: update the intent
        
        // Handle Web3Auth redirects
        handleIntentData(intent.data)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ onResume")
        
        // Handle Web3Auth custom tabs being closed
        if (Web3Auth.getCustomTabsClosed()) {
            Log.d(TAG, "🌐 Web3Auth custom tabs were closed by user")
            Toast.makeText(this, "Authentication was cancelled.", Toast.LENGTH_SHORT).show()
            Web3Auth.setCustomTabsClosed(false)
            
            // Notify ViewModel that auth was cancelled
            viewModel.onWeb3AuthCancelled()
        }
        
        // Also check if we have any pending intent data
        intent?.data?.let { data ->
            Log.d(TAG, "📱 Checking intent data on resume: $data")
            handleIntentData(data)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ onPause - app going to background (likely for OAuth)")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "⏹️ onStop - app fully backgrounded")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "▶️ onStart - app coming to foreground")
    }

    private fun handleIntentData(data: Uri?) {
        if (data != null) {
            Log.d(TAG, "🔗 Handling intent data: $data")
            
            // Check if this is a Web3Auth redirect
            if (data.scheme == "com.example.rampacashmobile" && data.host == "auth") {
                Log.d(TAG, "✅ Valid Web3Auth redirect detected")
                
                // Handle Web3Auth redirects
                try {
                    web3Auth.setResultUrl(data)
                    Log.d(TAG, "📨 Web3Auth redirect handled: $data")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Redirect handling failed: ${e.message}", e)
                    Toast.makeText(this, "Redirect handling failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "ℹ️ Intent data not a Web3Auth redirect: $data")
            }
        }
    }
}