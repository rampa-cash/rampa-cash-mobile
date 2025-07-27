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
import com.example.rampacashmobile.ui.screens.MainScreen
import com.example.rampacashmobile.ui.theme.RampaCashMobileTheme
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.Web3AuthResponse
import com.web3auth.core.types.Provider
import com.example.rampacashmobile.web3auth.Web3AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), Web3AuthManager.Web3AuthCallback {
    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var web3AuthManager: Web3AuthManager
    
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🏁 MainActivity onCreate")
        
        val sender = ActivityResultSender(this)

        // Initialize Web3Auth using the manager
        initializeWeb3Auth()

        // Handle initial intent data for Web3Auth redirects
        web3AuthManager.handleRedirect(intent?.data)

        enableEdgeToEdge()
        setContent {
            RampaCashMobileTheme {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        intentSender = sender,
                        web3AuthManager = web3AuthManager,
                        web3AuthCallback = this
                    )
                }
            }
        }
    }

    private fun initializeWeb3Auth() {
        val clientId = getString(R.string.web3auth_project_id)
        if (!web3AuthManager.initialize(clientId, this)) {
            Toast.makeText(this, "Web3Auth initialization failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "🔄 onNewIntent: ${intent.data}")
        setIntent(intent) // Important: update the intent
        
        // Handle Web3Auth redirects
        web3AuthManager.handleRedirect(intent.data)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ onResume")
        
        // Handle Web3Auth custom tabs being closed
        if (web3AuthManager.handleCustomTabsClosed()) {
            Toast.makeText(this, "Authentication was cancelled.", Toast.LENGTH_SHORT).show()
            viewModel.onWeb3AuthCancelled()
        }
        
        // Also check if we have any pending intent data
        intent?.data?.let { data ->
            Log.d(TAG, "📱 Checking intent data on resume: $data")
            web3AuthManager.handleRedirect(data)
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

    // Web3AuthManager.Web3AuthCallback implementations
    override fun onLoginSuccess(response: Web3AuthResponse, provider: Provider, solanaPublicKey: String, displayAddress: String) {
        runOnUiThread {
            viewModel.handleWeb3AuthSuccess(response, provider, solanaPublicKey, displayAddress)
        }
    }

    override fun onLoginError(message: String) {
        runOnUiThread {
            viewModel.setWeb3AuthError(message)
        }
    }

    override fun onLogoutSuccess() {
        runOnUiThread {
            viewModel.handleWeb3AuthLogout()
        }
    }

    override fun onLogoutError(message: String) {
        runOnUiThread {
            viewModel.setWeb3AuthError(message)
        }
    }

    override fun onLoading(isLoading: Boolean) {
        runOnUiThread {
            viewModel.setWeb3AuthLoading(isLoading)
        }
    }
}