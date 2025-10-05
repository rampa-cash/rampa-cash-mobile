package com.example.rampacashmobile.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SessionManager"
        private const val PREFS_NAME = "web3auth_session"
        private const val KEY_PRIVATE_KEY = "private_key"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_DISPLAY_ADDRESS = "display_address"
        private const val KEY_PROVIDER = "provider"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun storeWeb3AuthSession(
        privateKey: String,
        publicKey: String,
        displayAddress: String,
        provider: String
    ) {
        try {
            prefs.edit()
                .putString(KEY_PRIVATE_KEY, privateKey)
                .putString(KEY_PUBLIC_KEY, publicKey)
                .putString(KEY_DISPLAY_ADDRESS, displayAddress)
                .putString(KEY_PROVIDER, provider)
                .apply()

            Log.d(TAG, "✅ Web3Auth session stored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store Web3Auth session: ${e.message}", e)
            throw e
        }
    }

    fun getStoredPrivateKey(): String? {
        return prefs.getString(KEY_PRIVATE_KEY, null)
    }

    fun getStoredPublicKey(): String? {
        return prefs.getString(KEY_PUBLIC_KEY, null)
    }

    fun getStoredDisplayAddress(): String? {
        return prefs.getString(KEY_DISPLAY_ADDRESS, null)
    }

    fun getStoredProvider(): String? {
        return prefs.getString(KEY_PROVIDER, null)
    }

    fun clearSession() {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "✅ Web3Auth session cleared")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to clear Web3Auth session: ${e.message}", e)
        }
    }

    fun hasStoredSession(): Boolean {
        return !getStoredPrivateKey().isNullOrEmpty() && !getStoredPublicKey().isNullOrEmpty()
    }
}

