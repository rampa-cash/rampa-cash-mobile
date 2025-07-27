package com.example.rampacashmobile

import android.content.SharedPreferences
import android.util.Log

object DebugSessionHelper {
    private const val TAG = "DebugSessionHelper"
    
    fun debugAllStoredSessions(sharedPreferences: SharedPreferences) {
        Log.d(TAG, "=== SESSION STORAGE DEBUG ===")
        
        // Check Web3Auth sessions
        Log.d(TAG, "🔍 Web3Auth Session Data:")
        val web3AuthKey = sharedPreferences.getString("web3auth_pubkey", "")
        val web3AuthPrivateKey = sharedPreferences.getString("web3auth_private_key", "")
        val web3AuthLabel = sharedPreferences.getString("web3auth_account_label", "")
        val web3AuthProvider = sharedPreferences.getString("web3auth_provider", "")
        val web3AuthUserInfo = sharedPreferences.getString("web3auth_user_info", "")
        
        Log.d(TAG, "  web3auth_pubkey: ${if (web3AuthKey.isNullOrEmpty()) "EMPTY" else "EXISTS (${web3AuthKey.take(8)}...)"}")
        Log.d(TAG, "  web3auth_private_key: ${if (web3AuthPrivateKey.isNullOrEmpty()) "EMPTY" else "EXISTS (${web3AuthPrivateKey.take(8)}...)"}")
        Log.d(TAG, "  web3auth_account_label: '$web3AuthLabel'")
        Log.d(TAG, "  web3auth_provider: '$web3AuthProvider'")
        Log.d(TAG, "  web3auth_user_info: '$web3AuthUserInfo'")
        
        // Check MWA sessions
        Log.d(TAG, "🔍 MWA Session Data:")
        val mwaKey = sharedPreferences.getString("stored_pubkey", "")
        val mwaLabel = sharedPreferences.getString("stored_account_label", "")
        val mwaToken = sharedPreferences.getString("stored_auth_token", "")
        
        Log.d(TAG, "  stored_pubkey: ${if (mwaKey.isNullOrEmpty()) "EMPTY" else "EXISTS (${mwaKey.take(8)}...)"}")
        Log.d(TAG, "  stored_account_label: '$mwaLabel'")
        Log.d(TAG, "  stored_auth_token: ${if (mwaToken.isNullOrEmpty()) "EMPTY" else "EXISTS (${mwaToken.take(8)}...)"}")
        
        // Summary
        val hasWeb3Auth = !web3AuthKey.isNullOrEmpty() && !web3AuthPrivateKey.isNullOrEmpty() && !web3AuthLabel.isNullOrEmpty()
        val hasMwa = !mwaKey.isNullOrEmpty() && !mwaToken.isNullOrEmpty()
        
        Log.d(TAG, "📊 Session Summary:")
        Log.d(TAG, "  Web3Auth session valid: $hasWeb3Auth")
        Log.d(TAG, "  MWA session valid: $hasMwa")
        
        when {
            hasWeb3Auth && hasMwa -> Log.w(TAG, "⚠️ CONFLICT: Both sessions exist!")
            hasWeb3Auth -> Log.d(TAG, "✅ Web3Auth session should be restored")
            hasMwa -> Log.d(TAG, "✅ MWA session should be restored")
            else -> Log.d(TAG, "❌ No valid sessions found")
        }
        
        Log.d(TAG, "=== END SESSION DEBUG ===")
    }
} 