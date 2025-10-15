package com.example.rampacashmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.rampacashmobile.navigation.NavigationGraph
import com.example.rampacashmobile.ui.theme.RampaCashMobileTheme
import com.example.rampacashmobile.viewmodel.MainViewModel
import com.example.rampacashmobile.web3auth.Web3AuthManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthResponse
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
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🏁 MainActivity onCreate")

        val sender = ActivityResultSender(this)

        // Initialize Web3Auth using the manager
        initializeWeb3Auth()

        // Handle initial intent data for Web3Auth redirects
        web3AuthManager.handleRedirect(intent?.data)

        enableEdgeToEdge()
        
        // Configure system UI for dark theme (light icons)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false  // Light icons on status bar
        controller.isAppearanceLightNavigationBars = false  // Light icons on navigation bar
        
        setContent {
            RampaCashMobileTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Use NavigationGraph instead of single MainScreen
                    NavigationGraph(
                        navController = navController,
                        intentSender = sender,
                        web3AuthManager = web3AuthManager,
                        web3AuthCallback = this@MainActivity
                    )
                }
            }
        }
    }

    private fun initializeWeb3Auth() {
        val clientId = getString(R.string.web3auth_project_id)
        if (!web3AuthManager.initialize(clientId, this)) {
            Toast.makeText(this, "Web3Auth initialization failed", Toast.LENGTH_LONG).show()
            return
        }
        
        // Check for existing Web3Auth session after initialization
        checkExistingWeb3AuthSession()
    }
    
    private fun checkExistingWeb3AuthSession() {
        try {
            Log.d(TAG, "🔍 Checking for existing Web3Auth session...")
            
            if (web3AuthManager.hasExistingSession()) {
                Log.d(TAG, "🔍 Found existing Web3Auth session in SDK - attempting to restore")
                
                val sessionInfo = web3AuthManager.getSessionInfo()
                if (sessionInfo != null) {
                    val (privateKey, solanaPublicKey, displayAddress) = sessionInfo
                    
                    Log.d(TAG, "✅ Web3Auth session restored from SDK successfully")
                    Log.d(TAG, "🔑 Solana Public Key: $solanaPublicKey")
                    Log.d(TAG, "📍 Display Address: $displayAddress")
                    
                    // Note: We don't have provider info from the restored session,
                    // but we can restore the session anyway
                    runOnUiThread {
                        viewModel.handleWeb3AuthSessionRestore(privateKey, solanaPublicKey, displayAddress)
                    }
                } else {
                    Log.w(TAG, "⚠️ Web3Auth session exists in SDK but couldn't retrieve session info")
                }
            } else {
                Log.d(TAG, "🔍 No existing Web3Auth session found in SDK")
                // The ViewModel will check SharedPreferences for stored sessions
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking Web3Auth session: ${e.message}", e)
            // If Web3Auth SDK check fails, ViewModel will still check SharedPreferences
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
        
        // Check authentication status when app resumes
        Log.d(TAG, "📱 MainActivity onResume - checking authentication status")
        viewModel.onAppResume()
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
            Log.d(TAG, "✅ Web3Auth login successful!")
            Log.d(TAG, "🔑 Solana Public Key: $solanaPublicKey")
            Log.d(TAG, "📍 Display Address: $displayAddress")

            // Handle Web3Auth success in ViewModel
            viewModel.handleWeb3AuthSuccess(response, provider, solanaPublicKey, displayAddress)

            // Extract user info for potential onboarding
            val (existingEmail, existingPhone) = viewModel.extractUserInfoFromAuth(response, provider)

            // Check if user needs onboarding
            if (viewModel.needsOnboarding()) {
                Log.d(TAG, "🎯 User needs onboarding - navigating to user_onboarding")


                // Since we can't directly access NavController from Activity, we'll use the ViewModel
                // to set a flag that the LoginScreen can observe
                viewModel.setNeedsOnboardingNavigation(provider.toString().lowercase(), existingEmail, existingPhone)
            }
        }
    }

    override fun onLoginError(message: String) {
        runOnUiThread {
            Log.e(TAG, "❌ Web3Auth login error: $message")
            viewModel.setWeb3AuthError(message)
        }
    }

    override fun onLogoutSuccess() {
        runOnUiThread {
            Log.d(TAG, "✅ Web3Auth logout successful!")
            viewModel.handleWeb3AuthLogout()
        }
    }

    override fun onLogoutError(message: String) {
        runOnUiThread {
            Log.e(TAG, "❌ Web3Auth logout error: $message")
            viewModel.handleWeb3AuthLogoutError(message)
        }
    }

    override fun onLoading(isLoading: Boolean) {
        runOnUiThread {
            viewModel.setWeb3AuthLoading(isLoading)
        }
    }
}